package com.osuserverlist.bjar.handlers.osu;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.osuserverlist.bjar.models.database.DbMap;
import com.osuserverlist.bjar.models.essentials.ModeStats;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.essentials.Score;
import com.osuserverlist.bjar.models.osu.SubmitResponse;
import com.osuserverlist.bjar.modules.crypt.ChecksumUtil;
import com.osuserverlist.bjar.modules.crypt.Rijndael32CBC;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.modules.osu.OsuMapDownloader;
import com.osuserverlist.bjar.modules.pp.IPerformanceCalculator;
import com.osuserverlist.bjar.modules.pp.OsuNativePerformanceCalculator;
import com.osuserverlist.bjar.modules.redis.Redis;
import com.osuserverlist.bjar.modules.web.engine.Host;
import com.osuserverlist.bjar.modules.web.engine.HttpMethod;
import com.osuserverlist.bjar.modules.web.engine.Path;
import com.osuserverlist.bjar.packets.server.handlers.user.UserStatsPacket;
import com.osuserverlist.bjar.server.Server;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UploadedFile;

@Host("osu.")
@Path("/web/osu-submit-modular-selector.php")
@HttpMethod("POST")
public class OsuSubmitModularHandler implements Handler {

    private final static Logger logger = LoggerFactory.getLogger(OsuSubmitModularHandler.class);
    private final static IPerformanceCalculator ppCalculator = new OsuNativePerformanceCalculator();

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        SubmitResponse submitResponse = SubmitResponse.fromContext(ctx);

        String keyStr = ("osu!-scoreburgr---------" + submitResponse.getOsuVersion());
        keyStr = String.format("%-32s", keyStr).substring(0, 32);
        byte[] aesKey = keyStr.getBytes(StandardCharsets.UTF_8);

        byte[] decryptedBytes = Rijndael32CBC.decrypt(submitResponse.getScoreEncrypted(), aesKey,
                submitResponse.getIv());
        String decrypted = new String(decryptedBytes, StandardCharsets.UTF_8);
        String[] data = decrypted.split(":");

        if (data.length < 13) {
            ctx.status(400).result("Malformed decrypted score data.");
            return;
        }

        Server server = Server.getInstance();
        String playerIdent = String.format("%s|%s", data[1].stripTrailing(), ctx.formParam("pass"));

        Player p = server.playerManager.getByApiIdent(playerIdent);
        if (p == null) {
            ctx.status(401).result("Invalid credentials.");
            return;
        }

        UploadedFile fileUpload = ctx.uploadedFile("score");
        if (fileUpload == null) {
            ctx.status(400).result("No replay file uploaded.");
            return;
        }

        Score s = Score.fromSubmission(data, p);

        try (MySQL mysql = Database.getConnection()) {
            DbMap beatmap = server.osuAPIHandler.getBeatmapByHash(mysql, submitResponse.getUpdatedBeatmapHash());
            if (beatmap == null) {
                ctx.status(400).result("Beatmap not found.");
                return;
            }

            s.setBeatmapId(beatmap.getId());

            byte[] mapData = OsuMapDownloader.downloadMap(s.getBeatmapId());
            double pp = ppCalculator.calculate(s, mapData);
            s.setPp(pp);
            s.setChecksum(ChecksumUtil.generateChecksum(s.toString()));

            ResultSet bestScoreResult = mysql.query(
                    "SELECT id, score, pp, acc, max_combo FROM scores " +
                    "WHERE userid = ? AND map_md5 = ? AND mode = ? AND status = 1 " +
                    "ORDER BY score DESC LIMIT 1",
                    s.getPlayerId(), beatmap.getMd5(), s.getMode()).executeQuery();

            // Snapshot all previous-best values immediately — never re-read the cursor later.
            boolean hasPreviousBest = bestScoreResult.next();
            int    prevBestScore = hasPreviousBest ? bestScoreResult.getInt("score")     : 0;
            double prevBestPp    = hasPreviousBest ? bestScoreResult.getDouble("pp")     : 0.0;
            double prevBestAcc   = hasPreviousBest ? bestScoreResult.getDouble("acc")    : 0.0;
            int    prevBestCombo = hasPreviousBest ? bestScoreResult.getInt("max_combo") : 0;
            int    prevBestId    = hasPreviousBest ? bestScoreResult.getInt("id")        : -1;

            boolean isPersonalBest = !hasPreviousBest || s.getScore() > prevBestScore;

            // Resolve the player's previous map rank BEFORE inserting the new score,
            // so the leaderboard is not yet affected by it.
            int prevMapRank = 0;
            if (hasPreviousBest) {
                ResultSet prevRankResult = mysql.query(
                        "SELECT COUNT(*) + 1 AS osu_rank " +
                        "FROM (SELECT MAX(score) AS best_score FROM scores " +
                        "      WHERE map_md5 = ? AND mode = ? AND userid != ? AND status = 1 " +
                        "      GROUP BY userid) AS best_scores " +
                        "WHERE best_score > ?",
                        beatmap.getMd5(), s.getMode(), s.getPlayerId(), prevBestScore).executeQuery();
                if (prevRankResult.next()) {
                    prevMapRank = prevRankResult.getInt("osu_rank");
                }
            }

            // Insert the new score. status=1 only when it is a personal best.
            int scoreStatus = isPersonalBest ? 1 : 0;

            mysql.exec(
                    "INSERT INTO `scores`(`map_md5`, `score`, `pp`, `acc`, `max_combo`, `mods`, " +
                    "`n300`, `n100`, `n50`, `nmiss`, `ngeki`, `nkatu`, `grade`, `status`, `mode`, " +
                    "`play_time`, `time_elapsed`, `client_flags`, `userid`, `perfect`, `online_checksum`) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    beatmap.getMd5(), s.getScore(), s.getPp(), s.getAccuracy(), s.getMax_combo(),
                    s.getMods(), s.getN300(), s.getN100(), s.getN50(), s.getNmiss(), s.getNgeki(),
                    s.getNkatu(), s.getGrade(), scoreStatus, s.getMode(),
                    new java.sql.Timestamp(s.getPlaytime()), 0, s.getFlags(), s.getPlayerId(),
                    s.isPerfect(), s.getChecksum());

            ResultSet scoreIdResult = mysql.query("SELECT LAST_INSERT_ID() AS id").executeQuery();
            if (!scoreIdResult.next()) {
                ctx.status(500).result("Failed to retrieve score ID.");
                return;
            }
            s.setId(scoreIdResult.getInt("id"));

            // Demote the old personal best
            if (isPersonalBest && prevBestId != -1) {
                mysql.exec("UPDATE scores SET status = 0 WHERE id = ?", prevBestId);
            }

            // Resolve the new map rank AFTER inserting, excluding the player's own
            // other scores so they don't compete with themselves.
            int rank = 1;
            ResultSet rankResult = mysql.query(
                    "SELECT COUNT(*) + 1 AS osu_rank " +
                    "FROM (SELECT MAX(score) AS best_score FROM scores " +
                    "      WHERE map_md5 = ? AND mode = ? AND userid != ? AND status = 1 " +
                    "      GROUP BY userid) AS best_scores " +
                    "WHERE best_score > ?",
                    beatmap.getMd5(), s.getMode(), s.getPlayerId(), s.getScore()).executeQuery();
            if (rankResult.next()) {
                rank = rankResult.getInt("osu_rank");
            }

            // Weighted PP: only meaningful when this is a new personal best.
            // Using status=1 is safe here because we just finished demoting the old PB.
            double totalPp = 0.0;
            if (isPersonalBest) {
                ResultSet bestUserScoresResult = mysql.query(
                        "SELECT SUM(pp * POW(0.95, rn - 1)) AS weighted_pp " +
                        "FROM (SELECT s.pp, ROW_NUMBER() OVER (ORDER BY s.pp DESC) AS rn " +
                        "      FROM scores s " +
                        "      JOIN maps m ON s.map_md5 = m.md5 " +
                        "      WHERE s.userid = ? AND s.mode = ? " +
                        "        AND m.status IN (1, 8) AND s.status = 1) ranked",
                        s.getPlayerId(), s.getMode()).executeQuery();
                if (bestUserScoresResult.next()) {
                    totalPp = bestUserScoresResult.getDouble("weighted_pp");
                }
            }

            // Save replay file
            byte[] fileBytes = fileUpload.content().readAllBytes();
            Files.write(Paths.get("data/replays").resolve(s.getId() + ".osr"), fileBytes);

            ModeStats playerStats = p.getModeStats()[s.getMode()];
            ModeStats oldStats = new ModeStats(playerStats); // deep-copy before mutation

            if (isPersonalBest) {
                playerStats.addRankedScore(s, totalPp);
                Redis.getClient().zadd("bjar:leaderboard:" + s.getMode(), totalPp, String.valueOf(p.getId()));
                Long redisRank = Redis.getClient().zrevrank("bjar:leaderboard:" + s.getMode(), String.valueOf(p.getId()));
                playerStats.setGlobalRank((redisRank != null ? Math.toIntExact(redisRank) : -1) + 1);
            } else {
                playerStats.addUnrankedScore(s);
            }

            mysql.exec(
                    "UPDATE stats SET plays = ?, tscore = ?, rscore = ?, acc = ?, max_combo = ?, " +
                    "pp = ?, total_hits = ? WHERE id = ? AND mode = ?",
                    playerStats.getPlayCount(), playerStats.getTotalScore(), playerStats.getRankedScore(),
                    playerStats.getAccuracy(), playerStats.getMaxCombo(),
                    (int) Math.ceil(playerStats.getPp()), playerStats.getTotalHits(),
                    p.getId(), s.getMode());

            p.sendPacket(new UserStatsPacket(p));

            // ---- Build response ----

            List<String> ret = new ArrayList<>();

            ret.add(String.join("|",
                    "beatmapId:"       + beatmap.getId(),
                    "beatmapSetId:"    + beatmap.getSetId(),
                    "beatmapPlaycount:"+ beatmap.getPlays(),
                    "beatmapPasscount:"+ beatmap.getPasses(),
                    "approvedDate:"    + beatmap.getLastUpdate()));

            // Beatmap chart
            List<String> chart1 = new ArrayList<>();
            chart1.add("chartId:beatmap");
            chart1.add("chartUrl:https://osu.ppy.sh/b/" + beatmap.getId());
            chart1.add("chartName:Beatmap Ranking");

            if (isPersonalBest) {
                chart1.add(addChart("rank",     prevMapRank,                        rank));
                chart1.add(addChart("score",    hasPreviousBest ? prevBestScore : 0, s.getScore()));
                chart1.add(addChart("maxCombo", hasPreviousBest ? prevBestCombo : 0, s.getMax_combo()));
                chart1.add(addChart("accuracy", hasPreviousBest ? prevBestAcc * 100 : 0, s.getAccuracy() * 100));
                chart1.add(addChart("pp",       hasPreviousBest ? (int) Math.ceil(prevBestPp) : 0, (int) Math.ceil(s.getPp())));
            } else {
                chart1.add(addChart("rank",     prevMapRank,                        rank));
                chart1.add(addChart("score",    prevBestScore,                      s.getScore()));
                chart1.add(addChart("maxCombo", prevBestCombo,                      s.getMax_combo()));
                chart1.add(addChart("accuracy", prevBestAcc * 100,                  s.getAccuracy() * 100));
                chart1.add(addChart("pp",       (int) Math.ceil(prevBestPp),        (int) Math.ceil(s.getPp())));
            }

            chart1.add("onlineScoreId:" + s.getId());
            ret.add(String.join("|", chart1));

            // User chart
            List<String> chart2 = new ArrayList<>();
            chart2.add("chartId:overall");
            chart2.add("chartUrl:https://osu.ppy.sh/u/" + s.getPlayerId());
            chart2.add("chartName:User Ranking");

            if (isPersonalBest) {
                chart2.add(addChart("rank",        oldStats.getGlobalRank(),                  playerStats.getGlobalRank()));
                chart2.add(addChart("accuracy",    (int) (oldStats.getAccuracy()   * 100),    (int) (playerStats.getAccuracy() * 100)));
                chart2.add(addChart("maxCombo",    oldStats.getMaxCombo(),                     playerStats.getMaxCombo()));
                chart2.add(addChart("rankedScore", oldStats.getRankedScore(),                  playerStats.getRankedScore()));
                chart2.add(addChart("totalScore",  oldStats.getTotalScore(),                   playerStats.getTotalScore()));
                chart2.add(addChart("pp",          (int) Math.ceil(oldStats.getPp()),          (int) Math.ceil(playerStats.getPp())));
            } else {
                long curRank = playerStats.getGlobalRank();
                chart2.add(addChart("rank",        curRank,                                    curRank));
                chart2.add(addChart("accuracy",    (int) (playerStats.getAccuracy() * 100),    (int) (playerStats.getAccuracy() * 100)));
                chart2.add(addChart("maxCombo",    playerStats.getMaxCombo(),                   playerStats.getMaxCombo()));
                chart2.add(addChart("rankedScore", playerStats.getRankedScore(),                playerStats.getRankedScore()));
                chart2.add(addChart("totalScore",  playerStats.getTotalScore(),                 playerStats.getTotalScore()));
                chart2.add(addChart("pp",          (int) Math.ceil(playerStats.getPp()),        (int) Math.ceil(playerStats.getPp())));
            }

            // TODO: Achievements parsing
            chart2.add("achievements-new:");

            logger.info("Player {} submitted a score on {} ({}pp, PB={})",
                    p, beatmap.getArtist() + " - " + beatmap.getTitle(),
                    (int) Math.ceil(s.getPp()), isPersonalBest);

            ret.add(String.join("|", chart2));
            ctx.result(String.join("\n", ret));

        } catch (SQLException e) {
            ctx.status(500).result("An error occurred while processing the score.");
            logger.error("Error processing score submission", e);
        }
    }

    private String addChart(String name, Object prev, Object after) {
        String before   = (prev  != null) ? prev.toString()  : "";
        String afterStr = (after != null) ? after.toString() : "";
        return name + "Before:" + before + "|" + name + "After:" + afterStr;
    }
}