package com.banchojar.handlers.osu;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jooq.Record1;
import org.jooq.impl.DSL;

import com.banchojar.App;
import com.banchojar.Player;
import com.banchojar.Player.ModeStats;
import com.banchojar.Server;
import com.banchojar.db.models.BeatmapRecord;
import com.banchojar.models.Score;
import com.banchojar.packets.server.handlers.UserStatsHandler;
import com.banchojar.utils.ChecksumUtil;
import com.banchojar.utils.OsuMapDownloader;
import com.banchojar.utils.Rijndael32CBC;
import com.github.francesco149.koohii.Koohii;
import com.github.francesco149.koohii.Koohii.PPv2;
import com.github.francesco149.koohii.Koohii.PPv2Parameters;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UploadedFile;
import lombok.Data;

public class OsuSubmitModularHandler implements Handler {

    @Data
    public class SubmitResponse {
        private String token;
        private boolean exitedOut;
        private int failTime;
        private String updatedBeatmapHash;
        private String storyboardMd5;
        private String uniqueIds;
        private int scoreTime;
        private String osuVersion;
        private byte[] visualSettings;
        private byte[] iv;
        private byte[] clientHash;
        private byte[] scoreEncrypted;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        App.logger.info(ctx.formParamMap().toString());
        SubmitResponse submitResponse = new SubmitResponse();
        submitResponse.setToken(ctx.formParam("token"));
        submitResponse.setExitedOut(Boolean.parseBoolean(ctx.formParam("x")));
        submitResponse.setFailTime(Integer.parseInt(ctx.formParam("ft")));
        submitResponse.setUpdatedBeatmapHash(ctx.formParam("bmk"));
        submitResponse.setStoryboardMd5(ctx.formParam("sbk"));
        submitResponse.setUniqueIds(ctx.formParam("c1"));
        submitResponse.setScoreTime(Integer.parseInt(ctx.formParam("st")));
        submitResponse.setOsuVersion(ctx.formParam("osuver"));

        Decoder decoder = Base64.getDecoder();
        submitResponse.setVisualSettings(decoder.decode(ctx.formParam("fs")));
        submitResponse.setIv(decoder.decode(ctx.formParam("iv")));
        submitResponse.setClientHash(decoder.decode(ctx.formParam("s")));
        submitResponse.setScoreEncrypted(decoder.decode(ctx.formParam("score")));

        // Ensure AES key is exactly 32 bytes
        String keyStr = ("osu!-scoreburgr---------" + submitResponse.getOsuVersion());
        keyStr = String.format("%-32s", keyStr).substring(0, 32); // pad or truncate
        byte[] aesKey = keyStr.getBytes(StandardCharsets.UTF_8);

        // Decrypt score data
        byte[] decryptedBytes = Rijndael32CBC.decrypt(submitResponse.getScoreEncrypted(), aesKey, submitResponse.getIv());
        String decrypted = new String(decryptedBytes, StandardCharsets.UTF_8);
        String[] data = decrypted.split(":");

        if (data.length < 13) {
            ctx.status(400).result("Malformed decrypted score data.");
            return;
        }

        Score s = new Score();
        String username = data[1];
        String pwMd5 = ctx.formParam("pass");

        Integer id = Server.dsl.select(DSL.field("id"))
                .from(DSL.table("users"))
                .where(DSL.field("username").eq(username.replaceAll(" ", "")).and(DSL.field("password_hash").eq(pwMd5)))
                .fetchOne(DSL.field("id"), Integer.class);

        Optional<Player> p = (id == null) ? Optional.empty()
                : Server.players.values().stream()
                        .filter(player -> player.getId() == id)
                        .findFirst();

        if (id == null || p.isEmpty()) {
            ctx.status(400).result("Unauthorized");
            return;
        }

        s.setPlayerId(id);
        s.setN300(Integer.parseInt(data[3]));
        s.setN100(Integer.parseInt(data[4]));
        s.setN50(Integer.parseInt(data[5]));
        s.setNgeki(Integer.parseInt(data[6]));
        s.setNkatu(Integer.parseInt(data[7]));
        s.setNmiss(Integer.parseInt(data[8]));
        s.setScore(Integer.parseInt(data[9]));
        s.setMax_combo(Integer.parseInt(data[10]));
        s.setPerfect(Boolean.parseBoolean(data[11]));
        s.setGrade(data[12]);
        s.setMods(Integer.parseInt(data[13]));
        s.setPassed(Boolean.parseBoolean(data[14]));
        s.setMode(Integer.parseInt(data[15]));
        s.setPlaytime((int) (System.currentTimeMillis() / 1000));
        s.setFlags(Long.parseLong(data[16]));

        BeatmapRecord apiResponse = OsuAPIHandler.getBeatmapByHash(Server.config.getOsuApiKey(),
                submitResponse.getUpdatedBeatmapHash());

        OsuMapDownloader.downloadMap(apiResponse.beatmap_id());

        // TODO: Refactor this to use the cached downloaded map
        byte[] map = Files.readAllBytes(Paths.get(".data/maps/" + apiResponse.beatmap_id() + ".osu"));

        Koohii.Map beatmap = new Koohii.Parser()
                .map(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(map), StandardCharsets.UTF_8)));
        Koohii.DiffCalc stars = new Koohii.DiffCalc().calc(beatmap, s.getMods());

        PPv2Parameters pp = new Koohii.PPv2Parameters();
        pp.max_combo = beatmap.max_combo();
        pp.aim_stars = stars.aim;
        pp.speed_stars = stars.speed;
        pp.base_ar = beatmap.ar;
        pp.base_od = beatmap.od;
        pp.combo = s.getMax_combo();
        pp.nmiss = s.getNmiss();
        pp.n300 = s.getN300();
        pp.n100 = s.getN100();
        pp.n50 = s.getN50();
        pp.ncircles = apiResponse.circles();
        pp.nsliders = apiResponse.sliders();
        pp.nobjects = apiResponse.circles() + apiResponse.sliders();
        pp.beatmap = beatmap;
        pp.mods = s.getMods();
        
        PPv2 koohi = new PPv2(pp);

        s.setPp(koohi.total);
        s.setAccuracy(calcAccFromScore(s));

        s.setChecksum(ChecksumUtil.generateChecksum(s.toString()));

        org.jooq.Record currentBest = Server.dsl
                .select(
                        DSL.field("best_scores.score_id").as("score_id"),
                        DSL.field("best_scores.user_id").as("user_id"),
                        DSL.field("best_scores.map_md5").as("map_md5"),
                        DSL.field("best_scores.max_combo").as("max_combo"),
                        DSL.field("best_scores.acc").as("acc"),
                        DSL.field("best_scores.pp").as("pp"),
                        DSL.field("best_scores.mode").as("mode"),
                        DSL.field("best_scores.score").as("score"),
                        DSL.field("best_scores.rn").as("rn"))
                .from(
                        DSL.select(
                                DSL.field("score_id"),
                                DSL.field("user_id"),
                                DSL.field("map_md5"),
                                DSL.field("mode"),
                                DSL.field("max_combo"),
                                DSL.field("acc"),
                                DSL.field("pp"),
                                DSL.field("score"),
                                DSL.rowNumber()
                                        .over()
                                        .partitionBy(DSL.field("user_id"))
                                        .orderBy(DSL.field("score").desc())
                                        .as("rn"))
                                .from(DSL.table("scores"))
                                .where(DSL.field("map_md5").eq(apiResponse.checksum()))
                                .and(DSL.field("mode").eq(s.getMode()))
                                .asTable("best_scores"))
                .where(DSL.field("best_scores.rn").eq(1))
                .and(DSL.field("best_scores.user_id").eq(s.getPlayerId()))
                .fetchOne();
        boolean isPersonalBest = currentBest == null || s.getScore() > currentBest.get("score", Integer.class);

        Server.dsl.insertInto(DSL.table("scores"))
                .columns(
                        DSL.field("map_md5"),
                        DSL.field("user_id"),
                        DSL.field("score"),
                        DSL.field("max_combo"),
                        DSL.field("count_300"),
                        DSL.field("count_100"),
                        DSL.field("count_50"),
                        DSL.field("count_geki"),
                        DSL.field("count_katu"),
                        DSL.field("count_miss"),
                        DSL.field("perfect"),
                        DSL.field("mods"),
                        DSL.field("grade"),
                        DSL.field("playtime"),
                        DSL.field("mode"),
                        DSL.field("pp"),
                        DSL.field("acc"),
                        DSL.field("flags"),
                        DSL.field("diff"),
                        DSL.field("checksum"))
                .values(
                        apiResponse.checksum(),
                        s.getPlayerId(),
                        s.getScore(),
                        s.getMax_combo(),
                        s.getN300(),
                        s.getN100(),
                        s.getN50(),
                        s.getNgeki(),
                        s.getNkatu(),
                        s.getNmiss(),
                        s.isPerfect(),
                        s.getMods(),
                        s.getGrade(),
                        Timestamp.from(Instant.now()),
                        s.getMode(),
                        Math.ceil(s.getPp()),
                        s.getAccuracy(),
                        s.getFlags(),
                        stars.total,
                        s.getChecksum())
                .execute();

                Server.dsl.update(DSL.table("beatmaps"))
                .set(DSL.field("plays", Integer.class), DSL.field("plays", Integer.class).add(1))
                .set(DSL.field("passes", Integer.class), DSL.field("passes", Integer.class).add(s.isPassed() ? 1 : 0))
                .where(DSL.field("beatmap_id").eq(apiResponse.beatmap_id())).execute();

                Integer scoreId = 0;
                Integer rank = 0;

                scoreId = Server.dsl.select(DSL.field("score_id"))
                .from(DSL.table("scores"))
                .where(DSL.field("checksum").eq(s.getChecksum()))
                .fetchOne(DSL.field("score_id"), Integer.class);

        rank = Server.dsl
                .select(DSL.count().plus(1).as("rank"))
                .from(
                        Server.dsl.select(
                                DSL.field("user_id"),
                                DSL.max(DSL.field("score")).as("best_score"))
                                .from(DSL.table("scores"))
                                .where(DSL.field("map_md5").eq(apiResponse.checksum()))
                                .and(DSL.field("mode").eq(s.getMode()))
                                .groupBy(DSL.field("user_id"))
                                .asTable("best_scores"))
                .where(DSL.field("best_score").gt(s.getScore()))
                .fetchOne("rank", Integer.class);

        if (scoreId == null) {
            ctx.status(501).result("Failed to fetch score ID.");
            return;
        }

         UploadedFile fileUpload = ctx.uploadedFile("score");
        if (fileUpload == null) {
            ctx.status(400).result("No replay file uploaded.");
            return;
        }

        byte[] fileBytes = fileUpload.content().readAllBytes();
        Path uploadDir = Paths.get(".data/replays");
        Files.createDirectories(uploadDir);
        String filename = scoreId + ".osr";
        Files.write(uploadDir.resolve(filename), fileBytes);
        Player player = p.get();

        ModeStats playerStats = player.getModeStats()[s.getMode()];

        List<Record1<Integer>> bestScores = Server.dsl
                .select(DSL.field("s.pp", Integer.class))
                .from(DSL.table("scores").as("s"))
                .join(DSL.table("beatmaps").as("m"))
                .on(DSL.field("s.map_md5").eq(DSL.field("m.checksum")))
                .where(DSL.field("s.user_id").eq(p.get().getId()))
                .and(DSL.field("s.mode").eq(s.getMode()))
                .and(DSL.field("m.status").in(DSL.val(1), DSL.val(8))) // Corrected IN clause with individual values
                .orderBy(DSL.field("s.pp").desc())
                .fetch();

        BigDecimal weightedPp = BigDecimal.ZERO;
        float decay = 0.95f;

        for (int i = 0; i < bestScores.size(); i++) {
            float pp2 = bestScores.get(i).value1();
            float weight = (float) Math.pow(decay, i); // Use index `i` as exponent
            weightedPp = weightedPp.add(BigDecimal.valueOf(pp2 * weight));
        }
    

        List<String> ret = new ArrayList<>();
        // First line
        ret.add(String.join("|",
                "beatmapId:" + apiResponse.beatmap_id(),
                "beatmapSetId:" + apiResponse.beatmap_set_id(),
                "beatmapPlaycount:" + apiResponse.plays(),
                "beatmapPasscount:" + apiResponse.passes(),
                "approvedDate:" + apiResponse.approved_date()));

        ModeStats oldStats = new ModeStats(playerStats);
        playerStats.addScore(s, (short) weightedPp.setScale(0, RoundingMode.HALF_UP).intValue());

        Server.dsl.update(DSL.table("users_stats"))
        .set(DSL.field("ranked_score"), playerStats.getRankedScore())
        .set(DSL.field("total_score"), playerStats.getTotalScore())
        .set(DSL.field("play_count"), playerStats.getPlayCount())
        .set(DSL.field("accuracy"), playerStats.getAccuracy())
        .set(DSL.field("pp"), playerStats.getPp())
        .where(DSL.field("user_id").eq(s.getPlayerId()))
        .and(DSL.field("mode").eq(s.getMode()))
        .execute();


        player.addPacketToStack(new UserStatsHandler(player));
        
        ArrayList<String> chart1 = new ArrayList<>();
        chart1.add("chartId:overall");
        chart1.add("chartUrl:" + "https://osu.ppy.sh/b/" + apiResponse.beatmap_id());
        chart1.add("chartName:Beatmap Ranking");
        if (currentBest != null) {
            chart1.add(add_chart("rank", currentBest.get("rn", Integer.class), rank));
            chart1.add(add_chart("score", currentBest.get("score", Integer.class), s.getScore()));
            chart1.add(add_chart("maxCombo", currentBest.get("max_combo", Integer.class), s.getMax_combo()));
            chart1.add(add_chart("rankedScore", currentBest.get("score", Integer.class), s.getScore()));
            chart1.add(add_chart("totalScore", currentBest.get("score", Integer.class), s.getScore()));
            chart1.add(add_chart("accuracy",
                 (currentBest.get("acc", Double.class) * 100),
                   (s.getAccuracy() * 100)));
            chart1.add(add_chart("pp", currentBest.get("pp", Integer.class), (int) Math.ceil(s.getPp())));
        } else {
            // First score on this map
            chart1.add(add_chart("rank", 0, rank));
            chart1.add(add_chart("score", 0, s.getScore()));
            chart1.add(add_chart("maxCombo", 0, s.getMax_combo()));
            chart1.add(add_chart("accuracy", 0, (int) (s.getAccuracy() * 100)));
            chart1.add(add_chart("pp", 0, (int) Math.ceil(s.getPp())));
        }

        chart1.add("onlineScoreId:" + s.getId());
        ret.add(String.join("|", chart1));

        // Third line - user chart
        List<String> chart2 = new ArrayList<>();

        chart2.add("chartId:overall");
        chart2.add("chartUrl:" + "https://osu.ppy.sh/u/" + s.getPlayerId());
        chart2.add("chartName:User Ranking");

        // Add user stats comparison only if this was a new personal best
        if (isPersonalBest) {
            chart2.add(add_chart("rank", oldStats.getGlobalRank(), playerStats.getGlobalRank()));
            chart2.add(add_chart("accuracy",
                    (int) (oldStats.getAccuracy() * 100),
                    (int) (playerStats.getAccuracy() * 100)));
            chart2.add(add_chart("maxCombo", oldStats.getMaxCombo(), playerStats.getMaxCombo()));
            chart2.add(add_chart("rankedScore", oldStats.getRankedScore(), playerStats.getRankedScore()));
            chart2.add(add_chart("totalScore", oldStats.getTotalScore(), playerStats.getTotalScore()));
            chart2.add(add_chart("pp", (int) Math.ceil(oldStats.getPp()), (int) Math.ceil(playerStats.getPp())));
        } else {
            // No change in user stats
            chart2.add(add_chart("rank", playerStats.getGlobalRank(), playerStats.getGlobalRank()));
            chart2.add(add_chart("accuracy", (int) (playerStats.getAccuracy() * 100),
                    (int) (playerStats.getAccuracy() * 100)));
            chart2.add(add_chart("maxCombo", playerStats.getMaxCombo(), playerStats.getMaxCombo()));
            chart2.add(add_chart("rankedScore", playerStats.getRankedScore(), playerStats.getRankedScore()));
            chart2.add(add_chart("totalScore", playerStats.getTotalScore(), playerStats.getTotalScore()));
            chart2.add(add_chart("pp", (int) Math.ceil(playerStats.getPp()), (int) Math.ceil(playerStats.getPp())));
        }
    
        chart2.add("achievements-new:osu-combo-500+deez+nuts");
        ret.add(String.join("|", chart2));
        ctx.result(String.join("\n", ret));


    }

    private String add_chart(String name, Object prev, Object after) {
        String before = (prev != null) ? prev.toString() : "";
        String afterStr = (after != null) ? after.toString() : "";
        return name + "Before:" + before + "|" + name + "After:" + afterStr;
    }


    private float calcAccFromScore(Score s) {
        int n300 = s.getN300();
        int n100 = s.getN100();
        int n50 = s.getN50();
        int nMiss = s.getNmiss(); // Make sure you have this

        int totalHits = n300 + n100 + n50 + nMiss;
        if (totalHits == 0)
            return 0f;

        float acc = (n50 * 50 + n100 * 100 + n300 * 300) / (float) (totalHits * 300);
        return acc;
    }

}
