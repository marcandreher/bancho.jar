package com.osuserverlist.bjar.handlers.osu;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.osuserverlist.bjar.models.database.DbMap;
import com.osuserverlist.bjar.models.essentials.ModeStats;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.essentials.Score;
import com.osuserverlist.bjar.models.osu.SubmitResponse;
import com.osuserverlist.bjar.modules.crypt.ChecksumUtil;
import com.osuserverlist.bjar.modules.crypt.Rijndael32CBC;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.osu.OsuMapDownloader;
import com.osuserverlist.bjar.modules.web.engine.Host;
import com.osuserverlist.bjar.modules.web.engine.HttpMethod;
import com.osuserverlist.bjar.modules.web.engine.Path;
import com.osuserverlist.bjar.server.Server;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UploadedFile;
import rosu.pp.RosuFFI.Beatmap;
import rosu.pp.RosuFFI.Mode;
import rosu.pp.RosuFFI.Mods;
import rosu.pp.RosuFFI.Performance;
import rosu.pp.RosuFFI.RosuPPLib;

@Host("osu.")
@Path("/web/osu-submit-modular-selector.php")
@HttpMethod("POST")
public class OsuSubmitModularHandler implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        SubmitResponse submitResponse = SubmitResponse.fromContext(ctx);

        String keyStr = ("osu!-scoreburgr---------" + submitResponse.getOsuVersion());
        keyStr = String.format("%-32s", keyStr).substring(0, 32); // pad or truncate
        byte[] aesKey = keyStr.getBytes(StandardCharsets.UTF_8);

        // Decrypt score data
        byte[] decryptedBytes = Rijndael32CBC.decrypt(submitResponse.getScoreEncrypted(), aesKey,
                submitResponse.getIv());
        String decrypted = new String(decryptedBytes, StandardCharsets.UTF_8);
        String[] data = decrypted.split(":");

        if (data.length < 13) {
            ctx.status(400).result("Malformed decrypted score data.");
            return;
        }

        Score s = new Score();

        Player p = Server.getInstance().playerManager
                .getByApiIdent(String.format("%s|%s", data[1].stripTrailing(), ctx.formParam("pass")));
        if (p == null) {
            ctx.status(401).result("Invalid credentials.");
            return;
        }

        s.setPlayerId(p.getId());
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
        s.setAccuracy(calcAccFromScore(s));

        try (MySQL mysql = Database.getConnection()) {
            DbMap beatmap = Server.getInstance().osuAPIHandler.getBeatmapByHash(mysql,
                    submitResponse.getUpdatedBeatmapHash());
            if (beatmap == null) {
                ctx.status(400).result("Beatmap not found.");
                return;
            }
            s.setBeatmapId(beatmap.getId());
            OsuMapDownloader.downloadMap(s.getBeatmapId());

            byte[] map = Files.readAllBytes(Paths.get(".data/maps/" + s.getBeatmapId() + ".osu"));
            Beatmap ppBeatmap = new Beatmap(map);

            Performance perf = new Performance();
            perf.setAccuracy(s.getAccuracy());
            perf.setMisses(s.getNmiss());
            perf.setCombo(s.getMax_combo());
            perf.setMods(Mods.fromBits(s.getMods(), Mode.fromValue(s.getMode())));
            perf.setN300(s.getN300());
            perf.setN100(s.getN100());
            perf.setN50(s.getN50());
            perf.setNGeki(s.getNgeki());
            perf.setNKatu(s.getNkatu());

            RosuPPLib.PerformanceAttributes result = perf.calculate(ppBeatmap);

            double pp = 0.0;

            switch (result.mode) {
                case 0 -> pp = result.osu.t.pp; // osu
                case 1 -> pp = result.taiko.t.pp; // taiko
                case 2 -> pp = result.fruit.t.pp; // catch
                case 3 -> pp = result.mania.t.pp; // mania
            }

            perf.close();
            ppBeatmap.close();

            s.setPp(pp);
            s.setChecksum(ChecksumUtil.generateChecksum(s.toString()));

            // TODO: Handle best score stuff

            ResultSet bestScoreResult = mysql.query(
                    "SELECT score_id, user_id, map_md5, mode, max_combo, acc, pp, score FROM scores WHERE map_md5 = ? AND mode = ? AND user_id = ? ORDER BY score DESC LIMIT 1;",
                    beatmap.getMd5(), s.getMode(), s.getPlayerId()).executeQuery();

            boolean isPersonalBest;

            if (!bestScoreResult.next()) {
                isPersonalBest = true;
            } else {
                int bestScore = bestScoreResult.getInt("score");
                isPersonalBest = s.getScore() > bestScore;
            }

            mysql.exec(
                    "INSERT INTO `scores`(`map_md5`, `score`, `pp`, `acc`, `max_combo`, `mods`, `n300`, `n100`, `n50`, `nmiss`, `ngeki`, `nkatu`, `grade`, `status`, `mode`, `play_time`, `time_elapsed`, `client_flags`, `userid`, `perfect`, `online_checksum`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    beatmap.getMd5(), s.getScore(), s.getPp(), s.getAccuracy(), s.getMax_combo(), s.getMods(),
                    s.getN300(), s.getN100(), s.getN50(), s.getNmiss(), s.getNgeki(), s.getNkatu(), s.getGrade(), 1,
                    s.getMode(), s.getPlaytime(), 0, s.getFlags(), s.getPlayerId(), s.isPerfect(), s.getChecksum());

            // TODO : Increment passes and plays

            Integer scoreId = null;
            ResultSet scoreIdResult = mysql.query(
                    "SELECT `id` FROM `scores` WHERE `map_md5` = ? AND `user_id` = ? AND `mode` = ? ORDER BY `score` DESC LIMIT 1;",
                    beatmap.getMd5(), s.getPlayerId(), s.getMode()).executeQuery();
            if (scoreIdResult.next()) {
                scoreId = scoreIdResult.getInt("id");
            }

            if (scoreId == null) {
                ctx.status(500).result("Failed to retrieve score ID.");
                return;
            }

            UploadedFile fileUpload = ctx.uploadedFile("score");
            if (fileUpload == null) {
                ctx.status(400).result("No replay file uploaded.");
                return;
            }

            byte[] fileBytes = fileUpload.content().readAllBytes();
            String filename = scoreId + ".osr";
            Files.write(Paths.get(".data/replays").resolve(filename), fileBytes);

            ModeStats playerStats = p.getModeStats()[s.getMode()];

            List<String> ret = new ArrayList<>();

            ret.add(String.join("|",
                    "beatmapId:" + beatmap.getId(),
                    "beatmapSetId:" + beatmap.getSetId(),
                    "beatmapPlaycount:" + beatmap.getPlays(),
                    "beatmapPasscount:" + beatmap.getPasses(),
                    "approvedDate:" + beatmap.getLastUpdate()));

            ModeStats oldStats = new ModeStats(playerStats);
            playerStats.addScore(s, s.getPp());


        }

    }

    private String addChart(String name, Object prev, Object after) {
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
