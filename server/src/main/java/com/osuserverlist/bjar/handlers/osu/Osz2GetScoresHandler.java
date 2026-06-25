package com.osuserverlist.bjar.handlers.osu;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.database.BeatmapEntity;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.essentials.Score;
import com.osuserverlist.bjar.models.osu.GameMode;
import com.osuserverlist.bjar.models.osu.MapWebRankedStatus;
import com.osuserverlist.bjar.models.osu.Mods;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.modules.web.engine.Host;
import com.osuserverlist.bjar.modules.web.engine.HttpMethod;
import com.osuserverlist.bjar.modules.web.engine.Path;
import com.osuserverlist.bjar.packets.server.handlers.user.UserStatsPacket;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.skiincraft.api.ousu.entity.objects.Approval;

@Host("osu.")
@Path("/web/osu-osz2-getscores.php")
@HttpMethod("GET")
public class Osz2GetScoresHandler implements Handler {

    private final static Logger logger = LoggerFactory.getLogger(Osz2GetScoresHandler.class);

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String modeStr = ctx.queryParam("m");

        if (modeStr == null) {
            ctx.status(400).result("Missing required query parameters.");
            return;
        }

        String username = ctx.queryParam("us");
        String passwordHash = ctx.queryParam("ha");

        Server server = Server.getInstance();

        Player player = server.playerManager.getByApiIdent(String.format("%s|%s", username, passwordHash));
        if (player == null) {
            ctx.status(401).result("Invalid credentials.");
            return;
        }
        int modeVn = Integer.parseInt(ctx.queryParam("m"));
        int modsInt = Integer.parseInt(ctx.queryParam("mods"));

        GameMode gameMode = GameMode.fromValue(modeVn, modsInt);

        boolean oldRelax = player.isRelaxEnabled();
        boolean newRelax =
            (modsInt & Mods.Relax.getValue()) != 0 ||
            (modsInt & Mods.Relax2.getValue()) != 0;

        if (oldRelax != newRelax) {
            player.sendPacket(new UserStatsPacket(player));
        }

        player.setRelaxEnabled(newRelax);
        player.setRealGameMode(gameMode.getValue());

        
        BeatmapEntity beatmap;
        try (MySQL mysql = Database.getConnection()) {
            beatmap = server.osuAPIHandler.getBeatmapByHash(mysql, ctx.queryParam("c"));

            if (beatmap == null) {
                ctx.result("-1|false");
                return;
            }

            ResultSet ownScoreResult = mysql.query(
                    "SELECT s.*, u.name " +
                            "FROM scores s " +
                            "JOIN users u ON u.id = s.userid " +
                            "WHERE s.map_md5 = ? AND s.mode = ? AND s.userid = ? AND s.status = 2 " +
                            "ORDER BY s.score DESC LIMIT 1",
                    beatmap.getMd5(),
                    gameMode.getValue(),
                    player.getId()).executeQuery();

            Score ownScore = null;
            if (ownScoreResult.next()) {
                ownScore = Score.fromResultSet(ownScoreResult, beatmap);
            }

            List<Score> scoreList = new ArrayList<>();

            ResultSet leaderboardResult = mysql.query(
                    "SELECT * FROM (" +
                            "   SELECT s.*, u.name, " +
                            "          ROW_NUMBER() OVER (PARTITION BY s.userid ORDER BY s.score DESC) AS rn " +
                            "   FROM scores s " +
                            "   JOIN users u ON u.id = s.userid " +
                            "   WHERE s.map_md5 = ? AND s.mode = ? AND s.status = 2" +
                            ") ranked_scores " +
                            "WHERE rn = 1 " +
                            "ORDER BY score DESC, name " +
                            "LIMIT 100",
                    beatmap.getMd5(),
                    gameMode.getValue()).executeQuery();

            while (leaderboardResult.next()) {
                Score score = Score.fromResultSet(leaderboardResult, beatmap);
                scoreList.add(score);
            }

            if (!scoreList.isEmpty()) {
                int currentRank = 1;
                int position = 1;

                long previousScore = scoreList.get(0).getScore();

                scoreList.get(0).setRank(1);

                for (int i = 1; i < scoreList.size(); i++) {
                    position++;

                    Score score = scoreList.get(i);

                    if (score.getScore() != previousScore) {
                        currentRank = position;
                    }

                    score.setRank(currentRank);
                    previousScore = score.getScore();
                }
            }

            StringBuilder sb = new StringBuilder();

            sb.append(String.format(
                    "%d|false|%d|%d|%d|0\n" +
                            "0\n" +
                            "%s - %s [%s]\n" +
                            "0.0",
                    MapWebRankedStatus.fromApproval(Approval.getById(beatmap.getStatus())).getId(),
                    beatmap.getId(),
                    beatmap.getSetId(),
                    scoreList.size(),
                    beatmap.getArtist(),
                    beatmap.getTitle(),
                    beatmap.getVersion()));

            if (ownScore == null) {
                sb.append("\n");
            }

            if (!scoreList.isEmpty()) {
                StringBuilder sb2 = new StringBuilder();

                // First, check and output player's own score if present
                if (ownScore != null) {
                    // Find player's score in the list to get its correct rank
                    for (Score s : scoreList) {
                        if (s.getPlayerId() == player.getId()) {
                            // Clone the rank from the player's score in the list
                            ownScore.setRank(s.getRank());
                            break;
                        }
                    }
                    // Display player's own score at the top
                    sb.append(Score.buildScoreWebString(ownScore, ownScore.getId(), 1));
                }

                // Then append all scores in their order
                for (Score s : scoreList) {
                    sb2.append(Score.buildScoreWebString(s, s.getId(), 1));
                }

                sb.append(sb2.toString());
            }
            logger.debug("Generated leaderboard response for player {}: ({} entries)", player.toString(),
                    scoreList.size());
            ctx.status(200).result(sb.toString());

        }

    }

}
