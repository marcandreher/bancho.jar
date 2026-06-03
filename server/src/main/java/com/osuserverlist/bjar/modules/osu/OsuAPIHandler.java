package com.osuserverlist.bjar.modules.osu;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.osuserverlist.bjar.models.database.DbMap;
import com.osuserverlist.bjar.modules.database.MySQL;

import me.skiincraft.api.ousu.OusuAPI;
import me.skiincraft.api.ousu.entity.beatmap.Beatmap;

public class OsuAPIHandler {

    private final OusuAPI osuAPI;

    public OsuAPIHandler(String apiKey) {
        this.osuAPI = new OusuAPI(apiKey);
    }

    public DbMap getBeatmapBySetId(MySQL mysql, long beatmapSetId) throws SQLException {
        ResultSet mapResult = mysql.query("SELECT * FROM `maps` WHERE `set_id` = ?", beatmapSetId).executeQuery();

        if (!mapResult.next()) {
            Beatmap osuBeatmap = osuAPI.getBeatmap(beatmapSetId).get();
            DbMap map = new DbMap(osuBeatmap);

            insertInDb(mysql, map);
            return map;
        }

        DbMap map = new DbMap(mapResult);

        return map;
    }

    public DbMap getBeatmapByHash(MySQL mysql, String beatmapHash) throws Exception {
        ResultSet mapResult = mysql.query("SELECT * FROM `maps` WHERE `md5` = ?", beatmapHash).executeQuery();

        if (!mapResult.next()) {
            Beatmap osuBeatmap = osuAPI.getBeatmapByChecksum(beatmapHash).get();
            DbMap map = new DbMap(osuBeatmap);

            insertInDb(mysql, map);
            return map;
        }

        DbMap map = new DbMap(mapResult);

        return map;
    }

    private void insertInDb(MySQL mysql, DbMap map) throws SQLException {
        mysql.exec(
                "INSERT INTO `maps` (`id`, `set_id`, `status`, `md5`, `artist`, `title`, `version`, `creator`, `filename`, "
                        +
                        "`last_update`, `total_length`, `max_combo`, `frozen`, `plays`, `passes`, `mode`, `bpm`, `cs`, `ar`, "
                        +
                        "`od`, `hp`, `diff`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                map.getId(), map.getSetId(), map.getStatus(), map.getMd5(), map.getArtist(), map.getTitle(),
                map.getVersion(), map.getCreator(), map.getFilename(), map.getLastUpdate(), map.getTotalLength(),
                map.getMaxCombo(), map.isFrozen(), map.getPlays(), map.getPasses(), map.getMode(), map.getBpm(),
                map.getCs(), map.getAr(), map.getOd(), map.getHp(), map.getDiff());

    }

}