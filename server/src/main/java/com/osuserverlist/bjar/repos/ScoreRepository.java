package com.osuserverlist.bjar.repos;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.osuserverlist.bjar.models.database.BeatmapEntity;
import com.osuserverlist.bjar.models.essentials.Score;
import com.osuserverlist.bjar.modules.database.MySQL;

public class ScoreRepository {

    private final MySQL mysql;

    public ScoreRepository(MySQL mysql) {
        this.mysql = mysql;
    }

    public Score getBestScoreForPlayerOnBeatmap(int playerId, String beatmapMd5, int mode) throws SQLException {
        ResultSet bestScoreResult = mysql.query(GET_BEST_SCORE_FOR_PLAYER_ON_BEATMAP_QUERY, playerId, beatmapMd5, mode)
                .executeQuery();
        if (bestScoreResult.next()) {
            return Score.fromResultSet(bestScoreResult);
        }
        return null;
    }

    public int getPreviousMapRank(String beatmapMd5, int mode, int playerId, long score) throws SQLException {
        ResultSet rankResult = mysql.query(GET_PREVIOUS_MAP_RANK_QUERY, beatmapMd5, mode, playerId, score)
                .executeQuery();
        if (rankResult.next()) {
            return rankResult.getInt("osu_rank");
        }
        return 0;
    }

    public int getRankOnBeatmap(String beatmapMd5, int mode, int playerId, long score) throws SQLException {
        ResultSet rankResult = mysql.query(GET_RANK_FOR_MAP_QUERY, beatmapMd5, mode, score)
                .executeQuery();
        if (rankResult.next()) {
            return rankResult.getInt("osu_rank");
        }
        return 1;
    }

    public void insertScore(Score s, BeatmapEntity beatmap, int scoreStatus, int mode) throws SQLException {
        mysql.exec(INSERT_SCORE_QUERY, beatmap.getMd5(), s.getScore(), s.getPp(), s.getAccuracy(), s.getMax_combo(),
            s.getMods(), s.getN300(), s.getN100(), s.getN50(), s.getNmiss(), s.getNgeki(),
            s.getNkatu(), s.getGrade(), scoreStatus, mode,
            new java.sql.Timestamp(s.getPlaytime()), 0, s.getFlags(), s.getPlayerId(),
            s.isPerfect(), s.getChecksum());
    }

    public void updateScoreStatus(int scoreId, int newStatus) throws SQLException {
        mysql.exec(UPDATE_SCORE_STATUS_QUERY, newStatus, scoreId);
    }

    private final static String GET_BEST_SCORE_FOR_PLAYER_ON_BEATMAP_QUERY = "SELECT * FROM scores "+
            "WHERE userid = ? AND map_md5 = ? AND mode = ? AND status = 1 " +
            "ORDER BY score DESC LIMIT 1";

    private final static String GET_PREVIOUS_MAP_RANK_QUERY = "SELECT COUNT(*) + 1 AS osu_rank " +
            "FROM (SELECT MAX(score) AS best_score FROM scores " +
            "      WHERE map_md5 = ? AND mode = ? AND userid != ? AND status = 1 " +
            "      GROUP BY userid) AS best_scores " +
            "WHERE best_score > ?";

    private final static String GET_RANK_FOR_MAP_QUERY = "SELECT COUNT(*) + 1 AS osu_rank " +
            "FROM (SELECT MAX(score) AS best_score FROM scores " +
            "      WHERE map_md5 = ? AND mode = ? AND status = 1 " +
            "      GROUP BY userid) AS best_scores " +
            "WHERE best_score > ?";

    private final static String INSERT_SCORE_QUERY = "INSERT INTO `scores`(`map_md5`, `score`, `pp`, `acc`, `max_combo`, `mods`, " +
            "`n300`, `n100`, `n50`, `nmiss`, `ngeki`, `nkatu`, `grade`, `status`, `mode`, " +
            "`play_time`, `time_elapsed`, `client_flags`, `userid`, `perfect`, `online_checksum`) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final static String UPDATE_SCORE_STATUS_QUERY = "UPDATE scores SET status = ? WHERE id = ?;";
}
