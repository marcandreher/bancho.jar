package com.banchojar.handlers.osu;

import java.sql.Timestamp;

import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banchojar.Server;
import com.banchojar.db.models.BeatmapRecord;

import me.skiincraft.api.ousu.OusuAPI;
import me.skiincraft.api.ousu.entity.beatmap.Beatmap;

public class OsuAPIHandler {

    static Logger logger = LoggerFactory.getLogger("packets");

    public static BeatmapRecord getBeatmapBySetId(String apiKey, long beatmapSetId) throws Exception {
        BeatmapRecord existingBeatmap = Server.dsl.selectFrom(DSL.table("beatmaps"))
                .where(DSL.field("beatmap_set_id").eq(beatmapSetId))
                .fetchOneInto(BeatmapRecord.class);

        if (existingBeatmap != null) {
            return existingBeatmap;
        }
        OusuAPI osuAPI = new OusuAPI(apiKey);
        Beatmap request = osuAPI.getBeatmap(beatmapSetId).get();

        return fillRecord(request);
    }

    public static BeatmapRecord getBeatmapByHash(String apiKey, String beatmapHash) throws Exception {
        BeatmapRecord existingBeatmap = Server.dsl.selectFrom(DSL.table("beatmaps"))
                .where(DSL.field("checksum").eq(beatmapHash))
                .fetchOneInto(BeatmapRecord.class);

        if (existingBeatmap != null) {
            return existingBeatmap;
        }
        OusuAPI osuAPI = new OusuAPI(apiKey);
        Beatmap request = osuAPI.getBeatmapByChecksum(beatmapHash).get();
        return fillRecord(request);
    }

    private static BeatmapRecord fillRecord(Beatmap request) throws Exception {

        Timestamp approvedDate =request.getApprovedDate() == null ? null : Timestamp.from(request.getApprovedDate().toInstant());

     
        BeatmapRecord beatmapRecord = new BeatmapRecord(
                request.getBeatmapId(),
                request.getBeatmapSetId(),
                request.getApprovated().getId(),
                request.getArtist(),
                request.getTitle(),
                request.getBPM(),
                request.getCircles(),
                request.getSliders(),
                request.getCreatorName(),
                request.getCreatorId(),
                request.getMaxCombo(),
                request.getTotalLength(),
                false,
                request.getDifficultApproach(),
                request.getDifficultOverall(),
                request.getDifficultSize(),
                request.getDifficultDrain(),
                request.getGameMode().getId(),
                0,
                0,
                request.getDifficultAim(),
                request.getFileMD5(),
                approvedDate

        );
        Server.dsl.insertInto(DSL.table("beatmaps"))
        .columns(
                DSL.field("beatmap_id"),
                DSL.field("beatmap_set_id"),
                DSL.field("status"),
                DSL.field("artist"),
                DSL.field("title"),
                DSL.field("bpm"),
                DSL.field("circles"),
                DSL.field("sliders"),
                DSL.field("creator"),
                DSL.field("creator_id"),
                DSL.field("maxCombo"),
                DSL.field("total_length"),
                DSL.field("frozen"),
                DSL.field("ar"),
                DSL.field("od"),
                DSL.field("cs"),
                DSL.field("hp"),
                DSL.field("mode"),
                DSL.field("passes"),
                DSL.field("plays"),
                DSL.field("beatmap_diff"),
                DSL.field("checksum"),
                DSL.field("approved_date"))
        .values(
                beatmapRecord.beatmap_id(),
                beatmapRecord.beatmap_set_id(),
                beatmapRecord.status(),
                beatmapRecord.artist(),
                beatmapRecord.title(),
                beatmapRecord.bpm(),
                beatmapRecord.circles(),
                beatmapRecord.sliders(),
                beatmapRecord.creator(),
                beatmapRecord.creator_id(),
                beatmapRecord.maxCombo(),
                beatmapRecord.total_length(),
                false,
                beatmapRecord.ar(),
                beatmapRecord.od(),
                beatmapRecord.cs(),
                beatmapRecord.hp(),
                beatmapRecord.mode(),
                0,
                0,
                beatmapRecord.beatmap_diff(),
                beatmapRecord.checksum(),
                beatmapRecord.approved_date())
        .execute();
        
        return beatmapRecord;
    }

}