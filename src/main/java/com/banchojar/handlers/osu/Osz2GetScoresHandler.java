package com.banchojar.handlers.osu;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;
import org.jooq.impl.DSL;

import com.banchojar.Player;
import com.banchojar.Server;
import com.banchojar.db.models.BeatmapRecord;
import com.banchojar.handlers.osu.models.LeaderboardType;
import com.banchojar.models.Score;
import com.banchojar.utils.Approval;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class Osz2GetScoresHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String modeStr = ctx.queryParam("m");

        if (modeStr == null) {
            ctx.status(400).result("Missing required query parameters.");
            return;
        }

        int mode = Integer.parseInt(modeStr);
        
        Player player = ctx.sessionAttribute("player");

        // TODO: Handle leaderboard type
        LeaderboardType.getById(Integer.parseInt(ctx.queryParam("v")));
        
        BeatmapRecord apiResponse;
        try {
            apiResponse = OsuAPIHandler.getBeatmapByHash(
                    Server.config.getOsuApiKey(),
                    ctx.queryParam("c"));
        } catch (Exception e) {
            ctx.status(200).result("-1|true");
            return;
        }

        StringBuilder sb = new StringBuilder();
        Approval approval = Approval.getById(apiResponse.status() + 1);

        // Fetch user's own best score
        org.jooq.Record ownScoreRecord = Server.dsl
                .select(DSL.asterisk())
                .from(DSL.table("scores"))
                .join(DSL.table("users")).on(DSL.field("scores.user_id").eq(DSL.field("users.id")))
                .where(
                        DSL.field("map_md5").eq(apiResponse.checksum())
                                .and(DSL.field("mode").eq(mode))
                                .and(DSL.field("scores.user_id").eq(player.getId())))
                .orderBy(DSL.field("score").desc())
                .limit(1)
                .fetchOne();
        Score ownScore = null;
        if (ownScoreRecord != null) {
            Score s = new Score();
            s.setPlayerId(ownScoreRecord.get(DSL.field("user_id", Integer.class)));
            s.setUsername(ownScoreRecord.get(DSL.field("username", String.class)));
            s.setPp(ownScoreRecord.get(DSL.field("pp", Double.class)));
            s.setMax_combo(ownScoreRecord.get(DSL.field("max_combo", Integer.class)));
            s.setScore(ownScoreRecord.get(DSL.field("score", Long.class)));
            s.setN300(ownScoreRecord.get(DSL.field("count_300", Integer.class)));
            s.setN100(ownScoreRecord.get(DSL.field("count_100", Integer.class)));
            s.setN50(ownScoreRecord.get(DSL.field("count_50", Integer.class)));
            s.setNmiss(ownScoreRecord.get(DSL.field("count_miss", Integer.class)));
            s.setNkatu(ownScoreRecord.get(DSL.field("count_katu", Integer.class)));
            s.setNgeki(ownScoreRecord.get(DSL.field("count_geki", Integer.class)));
            s.setPerfect(ownScoreRecord.get(DSL.field("perfect", Boolean.class)));
            s.setMods(ownScoreRecord.get(DSL.field("mods", Integer.class)));
            s.setChecksum(ownScoreRecord.get(DSL.field("checksum", String.class)));
            s.setPlaytime((int) ownScoreRecord.get(DSL.field("playtime", Timestamp.class)).toInstant().getEpochSecond()
                    / 100);

            ownScore = s;
        }

        // Global top scores (only one per user)
        var scores = DSL.table("scores");
        var users = DSL.table("users");
        var scoreList = new ArrayList<Score>();
        var rowNumber = DSL.rowNumber()
                .over()
                .partitionBy(DSL.field("user_id"))
                .orderBy(DSL.field("score").desc())
                .as("rn");

        var rankedScores = DSL
                .select(DSL.asterisk(), rowNumber)
                .from(scores)
                .where(
                        DSL.field("map_md5").eq(apiResponse.checksum())
                                .and(DSL.field("mode").eq(mode)))
                .asTable("ranked_scores");

        Server.dsl
                .select(DSL.asterisk())
                .from(rankedScores)
                .join(users).on(DSL.field("ranked_scores.user_id").eq(DSL.field("users.id")))
                .where(DSL.field("ranked_scores.rn").eq(1)) // this is now safe: rn is a field of the subquery
                .orderBy(DSL.field("ranked_scores.score").desc(), DSL.field("users.username"))
                .limit(100)
                .fetch()
                .forEach(record -> {
                    Score s = new Score();
                    s.setPlayerId(record.get(DSL.field("user_id", Integer.class)));
                    s.setUsername(record.get(DSL.field("username", String.class)));
                    s.setPp(record.get(DSL.field("pp", Double.class)));
                    s.setMax_combo(record.get(DSL.field("max_combo", Integer.class)));
                    s.setScore(record.get(DSL.field("score", Long.class)));
                    s.setN300(record.get(DSL.field("count_300", Integer.class)));
                    s.setN100(record.get(DSL.field("count_100", Integer.class)));
                    s.setN50(record.get(DSL.field("count_50", Integer.class)));
                    s.setNmiss(record.get(DSL.field("count_miss", Integer.class)));
                    s.setNkatu(record.get(DSL.field("count_katu", Integer.class)));
                    s.setNgeki(record.get(DSL.field("count_geki", Integer.class)));
                    s.setPerfect(record.get(DSL.field("perfect", Boolean.class)));
                    s.setMods(record.get(DSL.field("mods", Integer.class)));
                    s.setChecksum(record.get(DSL.field("checksum", String.class)));
                    int time = (int) record.get(DSL.field("playtime", Timestamp.class)).toInstant().getEpochSecond();
                    s.setPlaytime(time);
                    scoreList.add(s);
                });


        // Assign ranks based on position (players with same score get same rank)
        if (!scoreList.isEmpty()) {
            int currentRank = 1;
            int position = 1;
            long previousScore = scoreList.get(0).getScore();
            scoreList.get(0).setRank(currentRank);
            
            // Assign ranks correctly handling ties
            // In osu!, the best scores (highest values) get rank 1
            for (int i = 1; i < scoreList.size(); i++) {
                position++; // Position always increments
                Score currentScore = scoreList.get(i);
                
                // If score is different from previous, update the rank to current position
                if (currentScore.getScore() != previousScore) {
                    currentRank = position;
                }
                
                // Assign the current rank
                currentScore.setRank(currentRank);
                previousScore = currentScore.getScore();
            }
        }

        sb.append(String.format(
                "%s|false|%s|%s|%s\n0\n[bold:0,size:20]%s|%s\n%s",
                approval.getId(),
                apiResponse.beatmap_id(),
                apiResponse.beatmap_set_id(),
                scoreList.size(),
                apiResponse.artist(),
                apiResponse.title(),
                apiResponse.beatmap_diff()));

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

        ctx.status(200).result(sb.toString());
    }
}