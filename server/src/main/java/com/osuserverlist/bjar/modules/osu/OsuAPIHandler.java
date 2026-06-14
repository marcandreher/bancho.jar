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

    private static final Logger logger = LoggerFactory.getLogger(OsuAPIHandler.class);

    private final OusuAPI osuAPI;

    public OsuAPIHandler(String apiKey) {
        this.osuAPI = new OusuAPI(apiKey);
    }

    public BeatmapEntity getBeatmapById(MySQL mysql, long beatmapId) throws SQLException {
        BeatmapEntity cachedMap = getMapById(mysql, beatmapId);

        if (cachedMap != null) {
            return cachedMap;
        }

        Beatmap osuBeatmap = osuAPI.getBeatmap(beatmapId).get();

        cacheMapset(mysql, osuBeatmap.getBeatmapSetId());

        BeatmapEntity map = getMapById(mysql, beatmapId);

        return map != null
                ? map
                : BeatmapEntity.fromBeatmap(osuBeatmap);
    }

    public BeatmapEntity getBeatmapByHash(MySQL mysql, String beatmapHash) throws SQLException {
        BeatmapEntity cachedMap = getMapByHash(mysql, beatmapHash);

        if (cachedMap != null) {
            return cachedMap;
        }

        Beatmap osuBeatmap;

        try {
            osuBeatmap = osuAPI.getBeatmapByChecksum(beatmapHash).get();
        } catch (BeatmapException e) {
            return null;
        }

        cacheMapset(mysql, osuBeatmap.getBeatmapSetId());

        BeatmapEntity map = getMapByHash(mysql, beatmapHash);

        return map != null
                ? map
                : BeatmapEntity.fromBeatmap(osuBeatmap);
    }

    private BeatmapEntity getMapById(MySQL mysql, long beatmapId) throws SQLException {
        ResultSet result = mysql.query(
                "SELECT * FROM `maps` WHERE `id` = ?",
                beatmapId
        ).executeQuery();

        return result.next()
                ? BeatmapEntity.fromResultSet(result)
                : null;
    }

    private BeatmapEntity getMapByHash(MySQL mysql, String md5) throws SQLException {
        ResultSet result = mysql.query(
                "SELECT * FROM `maps` WHERE `md5` = ?",
                md5
        ).executeQuery();

        return result.next()
                ? BeatmapEntity.fromResultSet(result)
                : null;
    }

    private boolean isMapsetCached(MySQL mysql, long setId) throws SQLException {
        ResultSet result = mysql.query(
                "SELECT 1 FROM `mapsets` WHERE `id` = ?",
                setId
        ).executeQuery();

        return result.next();
    }

    private void cacheMapset(MySQL mysql, long setId) throws SQLException {
        if (isMapsetCached(mysql, setId)) {
            return;
        }

        long startTime = System.currentTimeMillis();

        BeatmapSet beatmapSet = osuAPI.getBeatmapSet(setId).get();

        int beatmapCount = 0;

        for (Beatmap beatmap : beatmapSet.getAsList()) {
            insertMap(mysql, BeatmapEntity.fromBeatmap(beatmap));
            beatmapCount++;
        }

        mysql.exec(
                "INSERT INTO `mapsets` (`id`, `last_osuapi_check`) VALUES (?, CURRENT_TIMESTAMP)",
                setId
        );

        logger.debug(
                "Fetched beatmap set <{}> with <{}> beatmaps in <{}ms>",
                setId,
                beatmapCount,
                System.currentTimeMillis() - startTime
        );
    }

    private void insertMap(MySQL mysql, BeatmapEntity map) throws SQLException {
        mysql.exec(
                "INSERT INTO `maps` (`id`, `set_id`, `status`, `md5`, `artist`, `title`, `version`, `creator`, `filename`, `last_update`, `total_length`, `max_combo`, `frozen`, `plays`, `passes`, `mode`, `bpm`, `cs`, `ar`, `od`, `hp`, `diff`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                map.getId(),
                map.getSetId(),
                map.getStatus(),
                map.getMd5(),
                map.getArtist(),
                map.getTitle(),
                map.getVersion(),
                map.getCreator(),
                map.getFilename(),
                map.getLastUpdate(),
                map.getTotalLength(),
                map.getMaxCombo(),
                map.isFrozen(),
                map.getPlays(),
                map.getPasses(),
                map.getMode(),
                map.getBpm(),
                map.getCs(),
                map.getAr(),
                map.getOd(),
                map.getHp(),
                map.getDiff()
        );
    }
}