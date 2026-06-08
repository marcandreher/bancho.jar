package com.osuserverlist.bjar.modules.osu;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;

import com.osuserverlist.bjar.models.database.BeatmapEntity;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;

import me.skiincraft.api.ousu.OusuAPI;
import me.skiincraft.api.ousu.entity.beatmap.Beatmap;
import me.skiincraft.api.ousu.entity.beatmap.BeatmapSet;
import me.skiincraft.api.ousu.exceptions.BeatmapException;

public class OsuAPIHandler {

    private final OusuAPI osuAPI;
    private final static Logger logger = LoggerFactory.getLogger(OsuAPIHandler.class);

    public OsuAPIHandler(String apiKey) {
        this.osuAPI = new OusuAPI(apiKey);
    }

    public BeatmapEntity getBeatmapBySetId(MySQL mysql, long beatmapSetId) throws SQLException {
        ResultSet mapResult = mysql.query("SELECT * FROM `maps` WHERE `set_id` = ?", beatmapSetId).executeQuery();

        if (!mapResult.next()) {
            Beatmap osuBeatmap = osuAPI.getBeatmap(beatmapSetId).get();
            BeatmapEntity map = BeatmapEntity.fromBeatmap(osuBeatmap);

            insertInDb(mysql, map);
            return map;
        }

        BeatmapEntity map = BeatmapEntity.fromResultSet(mapResult);

        return map;
    }

    public BeatmapEntity getBeatmapByHash(MySQL mysql, String beatmapHash) throws SQLException {
        ResultSet mapResult = mysql.query("SELECT * FROM `maps` WHERE `md5` = ?", beatmapHash).executeQuery();

        if (!mapResult.next()) {
            Beatmap osuBeatmap;
            try {
                osuBeatmap = osuAPI.getBeatmapByChecksum(beatmapHash).get();
            }catch(BeatmapException e) {
                return null;
            }
            BeatmapEntity map = BeatmapEntity.fromBeatmap(osuBeatmap);

            ResultSet mapSetQuery = mysql.query("SELECT * FROM `mapsets` WHERE `id` = ?", map.getSetId()).executeQuery();

            if(!mapSetQuery.next()) {
                long queryStart = System.currentTimeMillis();
                int beatmapCount = 0;
                BeatmapSet beatmapSetRequest = osuAPI.getBeatmapSet(map.getSetId()).get();
                for(Beatmap beatmap : beatmapSetRequest.getAsList()) {
                    BeatmapEntity beatmapSetMap = BeatmapEntity.fromBeatmap(beatmap);
                    insertInDb(mysql, beatmapSetMap);
                    beatmapCount++;
                }

                mysql.exec("INSERT INTO `mapsets`(`id`, `last_osuapi_check`) VALUES (?,CURRENT_TIMESTAMP)", map.getSetId());

                logger.debug("Fetched beatmap set <{}> with <{}> beatmaps in <{}ms>", map.getSetId(), beatmapCount, System.currentTimeMillis() - queryStart);
            }

            return map;
        }

        BeatmapEntity map = BeatmapEntity.fromResultSet(mapResult);

        return map;
    }

    private void insertInDb(MySQL mysql, BeatmapEntity map) throws SQLException {
        mysql.exec(
                "INSERT INTO `maps` (`id`, `set_id`, `status`, `md5`, `artist`, `title`, `version`, `creator`, `filename`, `last_update`, `total_length`, `max_combo`, `frozen`, `plays`, `passes`, `mode`, `bpm`, `cs`, `ar`, `od`, `hp`, `diff`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                map.getId(), map.getSetId(), map.getStatus(), map.getMd5(), map.getArtist(), map.getTitle(),
                map.getVersion(), map.getCreator(), map.getFilename(), map.getLastUpdate(), map.getTotalLength(),
                map.getMaxCombo(), map.isFrozen(), map.getPlays(), map.getPasses(), map.getMode(), map.getBpm(),
                map.getCs(), map.getAr(), map.getOd(), map.getHp(), map.getDiff());

    }

}