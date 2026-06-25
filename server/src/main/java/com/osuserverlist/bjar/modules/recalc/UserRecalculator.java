package com.osuserverlist.bjar.modules.recalc;

import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.osuserverlist.bjar.models.osu.GameMode;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.modules.redis.Redis;

public class UserRecalculator {

    private static final Logger logger = LoggerFactory.getLogger(UserRecalculator.class);

    private static final String RECALC_USERS_QUERY =
            "SELECT * FROM `users` " +
            "LEFT JOIN `stats` ON `users`.`id` = `stats`.`id` " +
            "WHERE `stats`.`mode` = ? AND `stats`.`plays` > 0;";

    private static final String WEIGHTED_PP_QUERY =
            "SELECT SUM(pp * POW(0.95, rn - 1)) AS weighted_pp FROM ( " +
            "  SELECT pp, ROW_NUMBER() OVER (ORDER BY pp DESC) AS rn FROM ( " +
            "    SELECT MAX(s.pp) AS pp FROM scores s " +
            "    JOIN maps m ON s.map_md5 = m.md5 " +
            "    WHERE s.userid = ? AND s.mode = ? AND m.status = 1 AND s.status = 2 " +
            "    GROUP BY s.map_md5 " +
            "  ) best_scores " +
            ") ranked;";

    private final MySQL mysql;

    public UserRecalculator(MySQL mysql) {
        this.mysql = mysql;
    }

    /**
     * Recalculates weighted PP for all users in the given mode.
     *
     * @return number of users processed
     */
    public int recalcUsers(int mode, boolean force) {
        AtomicInteger count = new AtomicInteger(0);

        try {
            ResultSet userRs = mysql.query(RECALC_USERS_QUERY, mode).executeQuery();

            while (userRs.next()) {
                int userId = -1;
                try {
                    userId = userRs.getInt("id");
                    int oldPp = userRs.getInt("pp");

                    ResultSet bestScores = mysql.query(WEIGHTED_PP_QUERY, userId, mode).executeQuery();

                    if (!bestScores.next()) {
                        logger.warn("No best scores found for user <{}> in mode <{}>", userId, GameMode.fromValue(mode));
                        continue;
                    }

                    int newPp = bestScores.getInt("weighted_pp");
                    if (newPp != oldPp || force) {
                        logger.info("User <{}>: {}pp -> {}pp", userId, oldPp, newPp);
                        mysql.exec("UPDATE stats SET pp = ? WHERE id = ? AND `mode` = ?", newPp, userId, mode);
                        Redis.getClient().zadd("bjar:leaderboard:" + mode, newPp, String.valueOf(userId));
                    }

                    count.incrementAndGet();
                } catch (Exception e) {
                    logger.error("Error recalculating user <{}> in mode {}: {}", userId, mode, e.getMessage(), e);
                    // Continue to the next user — do not rethrow
                }
            }

        } catch (Exception e) {
            logger.error("Fatal error fetching users for mode {}: {}", mode, e.getMessage(), e);
        }

        return count.get();
    }
}