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

import com.osuserverlist.bjar.models.database.AchievementEntity;
import com.osuserverlist.bjar.models.database.BeatmapEntity;
import com.osuserverlist.bjar.models.essentials.ModeStats;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.essentials.Score;
import com.osuserverlist.bjar.models.osu.GameMode;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.models.osu.SubmitResponse;
import com.osuserverlist.bjar.modules.achievements.MevlEvaluator;
import com.osuserverlist.bjar.modules.calculations.IPerformanceCalculator;
import com.osuserverlist.bjar.modules.calculations.OsuNativePerformanceCalculator;
import com.osuserverlist.bjar.modules.crypt.ChecksumUtil;
import com.osuserverlist.bjar.modules.crypt.Rijndael32CBC;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.modules.osu.OsuMapDownloader;
import com.osuserverlist.bjar.modules.redis.Redis;
import com.osuserverlist.bjar.modules.web.engine.Host;
import com.osuserverlist.bjar.modules.web.engine.HttpMethod;
import com.osuserverlist.bjar.modules.web.engine.Path;
import com.osuserverlist.bjar.packets.server.handlers.chat.SendMessagePacket;
import com.osuserverlist.bjar.packets.server.handlers.user.UserStatsPacket;
import com.osuserverlist.bjar.repos.AchievementRepository;
import com.osuserverlist.bjar.repos.ScoreRepository;
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
    private final static MevlEvaluator jexlEvaluator = new MevlEvaluator();

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
        GameMode realGameMode = GameMode.fromValue(s.getMode(), s.getMods());

        try (MySQL mysql = Database.getConnection()) {
            BeatmapEntity beatmap = server.osuAPIHandler.getBeatmapByHash(mysql, submitResponse.getUpdatedBeatmapHash());
            if (beatmap == null) {
                ctx.status(400).result("Beatmap not found.");
                return;
            }

            s.setBeatmapId(beatmap.getId());

            byte[] mapData = OsuMapDownloader.downloadMap(s.getBeatmapId());
            double pp = ppCalculator.calculate(s, mapData);
            s.setPp(pp);
            s.setChecksum(ChecksumUtil.generateChecksum(s.toString()));

            ScoreRepository scoreRepo = new ScoreRepository(mysql);

            Score bestScore = scoreRepo.getBestScoreForPlayerOnBeatmap(s.getPlayerId(), beatmap.getMd5(), realGameMode.getValue());

            boolean hasPreviousBest = bestScore != null;

            boolean isPersonalBest = !hasPreviousBest || s.getScore() > bestScore.getScore();

            int prevMapRank = 0;
            if (hasPreviousBest) {
                prevMapRank = scoreRepo.getPreviousMapRank(beatmap.getMd5(), realGameMode.getValue(), s.getPlayerId(), bestScore.getScore());
            }

            int scoreStatus = (isPersonalBest && s.isPassed()) ? 1 : 0;

            scoreRepo.insertScore(s, beatmap, scoreStatus, realGameMode.getValue());
            
            Integer newScoreId = mysql.lastInsertId();
            if (newScoreId == null) {
                ctx.status(500).result("Failed to retrieve score ID.");
                return;
            }

            s.setId(newScoreId);

            // Demote the old personal best
            if (isPersonalBest && bestScore != null && bestScore.getId() != -1) {
                scoreRepo.updateScoreStatus(bestScore.getId(), 0);
            }

            int rank = scoreRepo.getRankOnBeatmap(beatmap.getMd5(), realGameMode.getValue(), s.getPlayerId(), s.getScore());
            
            if(rank == 1 && Privileges.fromInt(p.getServerPrivileges()).contains(Privileges.UNRESTRICTED)) {
                String ann = String.format(
                    "\u0001ACTION achieved #1 on %s with %.2f%% for %s.",
                    beatmap.toEmbed(),
                    s.getAccuracy(),
                    s.getPp()
                );

                server.channelManager.get("#announce").getPlayers().forEach(pl -> {
                    pl.sendPacket(new SendMessagePacket(server.botPlayer.getUsername(), ann, "#announce", server.botPlayer.getId()));
                });
            }

            // Weighted PP: only meaningful when this is a new personal best.
            // Using status=1 is safe here because we just finished demoting the old PB.
            double totalPp = 0.0;
            if (isPersonalBest && s.isPassed()) {
                ResultSet bestUserScoresResult = mysql.query(
                        "SELECT SUM(pp * POW(0.95, rn - 1)) AS weighted_pp FROM ( SELECT pp, ROW_NUMBER() OVER (ORDER BY pp DESC) AS rn FROM ( SELECT MAX(s.pp) AS pp FROM scores s JOIN maps m ON s.map_md5 = m.md5 WHERE s.userid = ? AND s.mode = ? AND m.status = 1 AND s.status = 1 GROUP BY s.map_md5 ) best_scores ) ranked;",
                        s.getPlayerId(), realGameMode.getValue()).executeQuery();
                if (bestUserScoresResult.next()) {
                    totalPp = bestUserScoresResult.getDouble("weighted_pp");
                }
            }

            // Save replay file
            byte[] fileBytes = fileUpload.content().readAllBytes();
            Files.write(Paths.get("data/replays").resolve(s.getId() + ".osr"), fileBytes);

            ModeStats playerStats = p.getModeStats()[realGameMode.getValue()];
            ModeStats oldStats = new ModeStats(playerStats); // deep-copy before mutation

            if (isPersonalBest && s.isPassed()) {
                playerStats.addRankedScore(s, totalPp);
                Redis.getClient().zadd("bjar:leaderboard:" + realGameMode.getValue(), totalPp, String.valueOf(p.getId()));
                Long redisRank = Redis.getClient().zrevrank("bjar:leaderboard:" + realGameMode.getValue(), String.valueOf(p.getId()));
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
                    p.getId(), realGameMode.getValue());

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
                chart1.add(addChart("score",    hasPreviousBest ? bestScore.getScore() : 0, s.getScore()));
                chart1.add(addChart("maxCombo", hasPreviousBest ? bestScore.getMax_combo() : 0, s.getMax_combo()));
                chart1.add(addChart("accuracy", hasPreviousBest ? bestScore.getAccuracy() * 100 : 0, s.getAccuracy() * 100));
                chart1.add(addChart("pp",       hasPreviousBest ? (int) Math.ceil(bestScore.getPp()) : 0, (int) Math.ceil(s.getPp())));
            } else {
                chart1.add(addChart("rank",     prevMapRank,                        rank));
                chart1.add(addChart("score",    hasPreviousBest ? bestScore.getScore() : 0, s.getScore()));
                chart1.add(addChart("maxCombo", hasPreviousBest ? bestScore.getMax_combo() : 0, s.getMax_combo()));
                chart1.add(addChart("accuracy", hasPreviousBest ? bestScore.getAccuracy() * 100 : 0, s.getAccuracy() * 100));
                chart1.add(addChart("pp",       hasPreviousBest ? (int) Math.ceil(bestScore.getPp()) : 0, (int) Math.ceil(s.getPp())));
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
            List<String> achievementStr = new ArrayList<>();
            AchievementRepository achievementRepo = new AchievementRepository(mysql);

            for (AchievementEntity achievement : server.achievementManager.getAll()) {
                if(!s.isPassed()) break;
                if (p.getUnlockedAchievements().contains(achievement.getId()))
                    continue;

                if (jexlEvaluator.evaluate(achievement.getCondition(), s, beatmap)) {
                    achievementRepo.addAchievementToPlayer(p.getId(), achievement.getId());
                    p.getUnlockedAchievements().add(achievement.getId());

                    achievementStr.add(
                        achievement.getFile()
                        + "+"
                        + achievement.getName()
                        + "+"
                        + achievement.getDescription()
                    );
                }
            }

            chart2.add("achievements-new:" + String.join("/", achievementStr));

            logger.info(
                "Player {} submitted a score on {} ({}pp, PB={})",
                p,
                beatmap.getArtist() + " - " + beatmap.getTitle(),
                (int) Math.ceil(s.getPp()),
                isPersonalBest
            );

            List<String> responseLines = new ArrayList<>();

            responseLines.add(String.join("|",
                    "beatmapId:" + beatmap.getId(),
                    "beatmapSetId:" + beatmap.getSetId(),
                    "beatmapPlaycount:" + beatmap.getPlays(),
                    "beatmapPasscount:" + beatmap.getPasses(),
                    "approvedDate:" + beatmap.getLastUpdate()
            ));

            responseLines.add(String.join("|", chart1));
            responseLines.add(String.join("|", chart2));

            ctx.result(String.join("\n", responseLines));
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