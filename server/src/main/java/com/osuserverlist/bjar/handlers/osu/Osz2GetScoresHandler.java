package com.osuserverlist.bjar.handlers.osu;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.database.BeatmapEntity;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.essentials.Score;
import com.osuserverlist.bjar.models.osu.GameMode;
import com.osuserverlist.bjar.models.osu.MapWebRankedStatus;
import com.osuserverlist.bjar.models.osu.Mods;
import com.osuserverlist.bjar.models.osu.OsuClientModels.LeaderboardType;
import com.osuserverlist.bjar.modules.WebEngine.Host;
import com.osuserverlist.bjar.modules.WebEngine.HttpMethod;
import com.osuserverlist.bjar.modules.WebEngine.Path;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.packets.server.UserServerPackets.UserStatsPacket;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.skiincraft.api.ousu.entity.objects.Approval;

@Host("osu.")
@Path("/web/osu-osz2-getscores.php")
@HttpMethod("GET")
public class Osz2GetScoresHandler implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(Osz2GetScoresHandler.class);

    private static final String LEADERBOARD_CTE =
            "SELECT * FROM (" +
            "   SELECT s.*, u.name, u.country," +
            "          ROW_NUMBER() OVER (PARTITION BY s.userid ORDER BY s.score DESC) AS rn " +
            "   FROM scores s " +
            "   JOIN users u ON u.id = s.userid " +
            "   WHERE s.map_md5 = ? AND s.mode = ? AND s.status = 2 %s" +
            ") ranked_scores " +
            "WHERE rn = 1 " +
            "ORDER BY score DESC, name " +
            "LIMIT 100";

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        int mode             = ctx.queryParamAsClass("m",    Integer.class).required().get();
        int mods             = ctx.queryParamAsClass("mods", Integer.class).required().get();
        int leaderboardType  = ctx.queryParamAsClass("v",    Integer.class).required().get();

        String username     = ctx.queryParam("us");
        String passwordHash = ctx.queryParam("ha");

        Server server = Server.getInstance();

        Player player = server.playerManager.getByApiIdent(String.format("%s|%s", username, passwordHash));
        if (player == null) {
            ctx.status(401).result("Invalid credentials.");
            return;
        }

        GameMode gameMode = GameMode.fromValue(mode, mods);
        LeaderboardType type = LeaderboardType.getById(leaderboardType);

        // Update relax state and push a stats update if it changed
        boolean wasRelax = player.isRelaxEnabled();
        boolean isRelax  = (mods & Mods.Relax.getValue()) != 0 || (mods & Mods.Relax2.getValue()) != 0;

        if (wasRelax != isRelax) {
            player.sendPacket(new UserStatsPacket(player));
        }

        player.setRelaxEnabled(isRelax);
        player.setRealGameMode(gameMode.getValue());

        try (MySQL mysql = Database.getConnection()) {

            BeatmapEntity beatmap = server.osuAPIHandler.getBeatmapByHash(mysql, ctx.queryParam("c"));
            if (beatmap == null) {
                ctx.result("-1|false");
                return;
            }

            Score ownScore = fetchOwnScore(mysql, beatmap, gameMode, player);
            List<Score> scores = fetchLeaderboard(mysql, beatmap, gameMode, type, mods, player);

            assignRanks(scores);

            if (ownScore != null) {
                syncOwnScoreRank(ownScore, scores, player);
            }

            String response = buildResponse(beatmap, ownScore, scores);

            logger.debug("Generated {} leaderboard for player {} on map <{}> type [{}] ({} entries)",
                    type, player, beatmap.getId(), type.name(), scores.size());

            ctx.status(200).result(response);
        }
    }

    private Score fetchOwnScore(MySQL mysql, BeatmapEntity beatmap, GameMode gameMode, Player player)
            throws Exception {
        ResultSet rs = mysql.query(
                "SELECT s.*, u.name, u.country " +
                "FROM scores s " +
                "JOIN users u ON u.id = s.userid " +
                "WHERE s.map_md5 = ? AND s.mode = ? AND s.userid = ? AND s.status = 2 " +
                "ORDER BY s.score DESC LIMIT 1",
                beatmap.getMd5(),
                gameMode.getValue(),
                player.getId()
        ).executeQuery();

        return rs.next() ? Score.fromResultSet(rs, beatmap) : null;
    }

    private List<Score> fetchLeaderboard(MySQL mysql, BeatmapEntity beatmap, GameMode gameMode,
                                         LeaderboardType type, int mods, Player player)
            throws Exception {

        String query;
        Object[] params;

        switch (type) {
            case GLOBAL_MODS -> {
                // Only scores that share the exact same mod combination
                query  = String.format(LEADERBOARD_CTE, "AND s.mods = ?");
                params = new Object[]{ beatmap.getMd5(), (int) gameMode.getValue(), mods };
            }
            case FRIENDS -> {
                // Player's own scores + scores from people they have friended
                query  = String.format(LEADERBOARD_CTE,
                        "AND (s.userid = ? OR s.userid IN (" +
                        "  SELECT user2 FROM relationships WHERE user1 = ? AND type = 'friend'" +
                        "))");
                params = new Object[]{ beatmap.getMd5(), (int) gameMode.getValue(), player.getId(), player.getId() };
            }
            case COUNTRY -> {
                query  = String.format(LEADERBOARD_CTE, "AND u.country = ?");
                params = new Object[]{ beatmap.getMd5(), (int) gameMode.getValue(), String.valueOf(player.getCountry()) };
            }
            default -> {
                // GLOBAL — no extra filter
                query  = String.format(LEADERBOARD_CTE, "");
                params = new Object[]{ beatmap.getMd5(), (int) gameMode.getValue() };
            }
        }

        ResultSet rs = mysql.query(query, params).executeQuery();
        List<Score> scores = new ArrayList<>();
        while (rs.next()) {
            scores.add(Score.fromResultSet(rs, beatmap));
        }
        return scores;
    }


    private void assignRanks(List<Score> scores) {
        if (scores.isEmpty()) return;

        int currentRank = 1;
        int position    = 1;
        long prevScore  = scores.get(0).getScore();
        scores.get(0).setRank(1);

        for (int i = 1; i < scores.size(); i++) {
            position++;
            Score s = scores.get(i);

            if (s.getScore() != prevScore) {
                currentRank = position;
            }

            s.setRank(currentRank);
            prevScore = s.getScore();
        }
    }

    private void syncOwnScoreRank(Score ownScore, List<Score> scores, Player player) {
        for (Score s : scores) {
            if (s.getPlayerId() == player.getId()) {
                ownScore.setRank(s.getRank());
                return;
            }
        }
    }


    private String buildResponse(BeatmapEntity beatmap, Score ownScore, List<Score> scores) {
        StringBuilder sb = new StringBuilder();

        // Header line
        sb.append(String.format(
                "%d|false|%d|%d|%d|0\n0\n%s - %s [%s]\n0.0",
                MapWebRankedStatus.fromApproval(Approval.getById(beatmap.getStatus())).getId(),
                beatmap.getId(),
                beatmap.getSetId(),
                scores.size(),
                beatmap.getArtist(),
                beatmap.getTitle(),
                beatmap.getVersion()
        ));

        // Personal score (shown above the leaderboard in-client)
        if (ownScore != null) {
            sb.append(Score.buildScoreWebString(ownScore, ownScore.getId(), 1));
        } else {
            sb.append("\n");
        }

        // Leaderboard entries
        for (Score s : scores) {
            sb.append(Score.buildScoreWebString(s, s.getId(), 1));
        }

        return sb.toString();
    }
}