package com.osuserverlist.bjar.models.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.server.Server;

import lombok.Data;
import me.skiincraft.api.ousu.entity.beatmap.Beatmap;

@Data
public class BeatmapEntity {
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

    public String toEmbed() {
        return String.format("[https://%s/b/%s %s - %s [%s]]", Server.getInstance().domain, id, artist, title, version);
    }

    public static BeatmapEntity fromBeatmap(Beatmap beatmap) {
        BeatmapEntity beatmapEntity = new BeatmapEntity();
        beatmapEntity.id = beatmap.getBeatmapId();
        beatmapEntity.setId = beatmap.getBeatmapSetId();
        beatmapEntity.status = beatmap.getApprovated().getId();
        beatmapEntity.md5 = beatmap.getFileMD5();
        beatmapEntity.artist = beatmap.getArtist();
        beatmapEntity.title = beatmap.getTitle();
        beatmapEntity.version = beatmap.getVersion();
        beatmapEntity.creator = beatmap.getCreatorName();
        beatmapEntity.filename = getFileName(beatmap);
        beatmapEntity.lastUpdate = OffsetDateTime.parse(beatmap.getLastUpdateDate().toString())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        beatmapEntity.totalLength = beatmap.getTotalLength();
        beatmapEntity.maxCombo = beatmap.getMaxCombo();
        beatmapEntity.frozen = false;
        beatmapEntity.plays = 0;
        beatmapEntity.passes = 0;
        beatmapEntity.mode = beatmap.getGameMode().getId();
        beatmapEntity.bpm = beatmap.getBPM();
        beatmapEntity.cs = beatmap.getDifficultApproach();
        beatmapEntity.ar = beatmap.getDifficultOverall();
        beatmapEntity.od = beatmap.getDifficultSize();
        beatmapEntity.hp = beatmap.getDifficultDrain();
        beatmapEntity.diff = (float)beatmap.getDifficultAim();
        return beatmapEntity;
    }

    public static BeatmapEntity fromResultSet(ResultSet mapResult) throws SQLException {
        BeatmapEntity beatmapEntity = new BeatmapEntity();
        beatmapEntity.id = mapResult.getLong("id");
        beatmapEntity.setId = mapResult.getLong("set_id");
        beatmapEntity.status = mapResult.getInt("status");
        beatmapEntity.md5 = mapResult.getString("md5");
        beatmapEntity.artist = mapResult.getString("artist");
        beatmapEntity.title = mapResult.getString("title");
        beatmapEntity.version = mapResult.getString("version");
        beatmapEntity.creator = mapResult.getString("creator");
        beatmapEntity.filename = mapResult.getString("filename");
        beatmapEntity.lastUpdate = mapResult.getString("last_update");
        beatmapEntity.totalLength = mapResult.getInt("total_length");
        beatmapEntity.maxCombo = mapResult.getInt("max_combo");
        beatmapEntity.frozen = mapResult.getBoolean("frozen");
        beatmapEntity.plays = mapResult.getInt("plays");
        beatmapEntity.passes = mapResult.getInt("passes");
        beatmapEntity.mode = mapResult.getInt("mode");
        beatmapEntity.bpm = mapResult.getFloat("bpm");
        beatmapEntity.cs = mapResult.getFloat("cs");
        beatmapEntity.ar = mapResult.getFloat("ar");
        beatmapEntity.od = mapResult.getFloat("od");
        beatmapEntity.hp = mapResult.getFloat("hp");
        beatmapEntity.diff = mapResult.getFloat("diff");
        return beatmapEntity;
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
