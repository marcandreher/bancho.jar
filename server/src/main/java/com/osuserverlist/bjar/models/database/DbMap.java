package com.osuserverlist.bjar.models.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.osuserverlist.bjar.modules.database.MySQL;

import lombok.Data;
import me.skiincraft.api.ousu.entity.beatmap.Beatmap;

@Data
public class DbMap {
    private long id;
    private long setId;
    private int status;
    private String md5;
    private String artist;
    private String title;
    private String version;
    private String creator;
    private String filename;
    private String lastUpdate;
    private int totalLength;
    private int maxCombo;
    private boolean frozen;
    private int plays;
    private int passes;
    private int mode;
    private float bpm;
    private float cs;
    private float ar;
    private float od;
    private float hp;
    private float diff;

    public DbMap(ResultSet mapResult) throws SQLException {
        this.id = mapResult.getLong("id");
        this.setId = mapResult.getLong("set_id");
        this.status = mapResult.getInt("status");
        this.md5 = mapResult.getString("md5");
        this.artist = mapResult.getString("artist");
        this.title = mapResult.getString("title");
        this.version = mapResult.getString("version");
        this.creator = mapResult.getString("creator");
        this.filename = mapResult.getString("filename");
        this.lastUpdate = mapResult.getString("last_update");
        this.totalLength = mapResult.getInt("total_length");
        this.maxCombo = mapResult.getInt("max_combo");
        this.frozen = mapResult.getBoolean("frozen");
        this.plays = mapResult.getInt("plays");
        this.passes = mapResult.getInt("passes");
        this.mode = mapResult.getInt("mode");
        this.bpm = mapResult.getFloat("bpm");
        this.cs = mapResult.getFloat("cs");
        this.ar = mapResult.getFloat("ar");
        this.od = mapResult.getFloat("od");
        this.hp = mapResult.getFloat("hp");
        this.diff = mapResult.getFloat("diff");
    }

    public DbMap(Beatmap beatmap) {
        this.id = beatmap.getBeatmapId();
        this.setId = beatmap.getBeatmapSetId();
        this.status = beatmap.getApprovated().getId();
        this.md5 = beatmap.getFileMD5();
        this.artist = beatmap.getArtist();
        this.title = beatmap.getTitle();
        this.version = beatmap.getVersion();
        this.creator = beatmap.getCreatorName();
        this.filename = getFileName(beatmap);
        this.lastUpdate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneOffset.UTC)
        .format(Instant.parse(beatmap.getLastUpdateDate().toString()));
        this.totalLength = beatmap.getTotalLength();
        this.maxCombo = beatmap.getMaxCombo();
        this.frozen = false;
        this.plays = 0;
        this.passes = 0;
        this.mode = beatmap.getGameMode().getId();
        this.bpm = beatmap.getBPM();
        this.cs = beatmap.getDifficultApproach();
        this.ar = beatmap.getDifficultOverall();
        this.od = beatmap.getDifficultSize();
        this.hp = beatmap.getDifficultDrain();
        this.diff = (float)beatmap.getDifficultAim();
    }

    public static String getFileName(Beatmap beatmap) {
        return String.format("%s - %s [%s].osu", beatmap.getArtist(), beatmap.getTitle(), beatmap.getVersion());
    }

    public static void insert(MySQL mysql, int id, int setId, int status, String md5, String artist, String title,
            String version,
            String creator,
            String filename, String lastUpdate, int totalLength, int maxCombo, boolean frozen, int plays, int passes,
            int mode, float bpm, float cs, float ar, float od, float hp, float diff) {
        mysql.exec("INSERT INTO `maps` (`id`, `set_id`, `status`, `md5`, `artist`, `title`, `version`, `creator`, `filename`, " +
                "`last_update`, `total_length`, `max_combo`, `frozen`, `plays`, `passes`, `mode`, `bpm`, `cs`, `ar`, " +
                "`od`, `hp`, `diff`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, setId, status, md5, artist, title, version, creator, filename, lastUpdate, totalLength, maxCombo, frozen, plays, passes, mode, bpm, cs, ar, od, hp, diff);
    }
}
