package com.osuserverlist.bjar.modules.recalc;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.osuserverlist.bjar.models.essentials.Score;
import com.osuserverlist.bjar.models.osu.GameMode;
import com.osuserverlist.bjar.modules.calculations.IPerformanceCalculator;
import com.osuserverlist.bjar.modules.calculations.OsuNativePerformanceCalculator;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.modules.osu.OsuMapDownloader;

public class ScoreRecalculator {

    private static final Logger logger = LoggerFactory.getLogger(ScoreRecalculator.class);

    private static final int SCORE_THREAD_COUNT = 3;

    private static final String RECALC_SCORES_QUERY =
            "SELECT *, `maps`.`id` AS `map_id` FROM scores " +
            "INNER JOIN maps ON scores.map_md5 = maps.md5 " +
            "WHERE scores.status = 2 AND scores.mode = ? " +
            "ORDER BY scores.pp DESC";

    /**
     * Recalculates PP for all scores in the given mode in parallel.
     *
     * @return number of scores successfully processed
     */
    public int recalcScores(int mode, boolean force) {
        List<ScoreRow> rows = fetchScoreRows(mode);
        if (rows.isEmpty()) {
            logger.info("No scores found for mode {}", GameMode.fromValue(mode));
            return 0;
        }

        logger.info("Dispatching {} scores for mode {} across {} threads",
                rows.size(), GameMode.fromValue(mode), SCORE_THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(SCORE_THREAD_COUNT);
        List<Future<?>> futures = new ArrayList<>(rows.size());

        for (ScoreRow row : rows) {
            futures.add(executor.submit(() -> processScore(row, mode, successCount, force)));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                logger.error("Unexpected executor-level error: {}", e.getMessage(), e);
            }
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.MINUTES)) {
                logger.warn("Score executor for mode {} timed out — forcing shutdown.", mode);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Score recalc interrupted for mode {}", mode, e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        return successCount.get();
    }

    private List<ScoreRow> fetchScoreRows(int mode) {
        List<ScoreRow> rows = new ArrayList<>();
        MySQL mysql = Database.getConnection();
        try {
            ResultSet rs = mysql.query(RECALC_SCORES_QUERY, mode).executeQuery();
            while (rs.next()) {
                try {
                    rows.add(ScoreRow.fromResultSet(rs, mode));
                } catch (Exception e) {
                    logger.error("Failed to read score row in mode {}: {}", mode, e.getMessage(), e);
                    // Skip this row; continue collecting the rest
                }
            }
        } catch (Exception e) {
            logger.error("Fatal error fetching scores for mode {}: {}", mode, e.getMessage(), e);
        } finally {
            mysql.close();
        }
        return rows;
    }

    private void processScore(ScoreRow row, int mode, AtomicInteger successCount, boolean force) {
        MySQL mysql = Database.getConnection();
        try {
            IPerformanceCalculator calculator = new OsuNativePerformanceCalculator();
            byte[] mapData = OsuMapDownloader.downloadMap(row.mapId);
            double newPP = calculator.calculate(row.score, mapData);

            if (Math.abs(newPP - row.oldPP) > 0.01 || force) {
                logger.info("Score <{}>: {}pp -> {}pp",
                        row.score.getId(), row.oldPP, Math.round(newPP * 1000.0) / 1000.0);
                mysql.exec("UPDATE scores SET pp = ? WHERE id = ?", newPP, row.score.getId());
            }

            successCount.incrementAndGet();
        } catch (Exception e) {
            logger.error("Error recalculating score <{}> in mode {}: {}",
                    row.score.getId(), mode, e.getMessage(), e);
            // Do not rethrow — all other scores must keep going
        } finally {
            mysql.close();
        }
    }

    private static class ScoreRow {
        final Score score;
        final long mapId;
        final double oldPP;

        ScoreRow(Score score, long mapId, double oldPP) {
            this.score = score;
            this.mapId = mapId;
            this.oldPP = oldPP;
        }

        static ScoreRow fromResultSet(ResultSet rs, int mode) throws Exception {
            Score score = Score.fromResultSet(rs);
            score.setMode(GameMode.toVanillaMode(GameMode.fromValue(mode)).getValue());
            long mapId = rs.getLong("map_id");
            double oldPP = rs.getDouble("pp");
            return new ScoreRow(score, mapId, oldPP);
        }
    }
}