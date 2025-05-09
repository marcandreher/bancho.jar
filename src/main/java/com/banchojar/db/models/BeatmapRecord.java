package com.banchojar.db.models;

import java.sql.Timestamp;

public record BeatmapRecord(
        long beatmap_id,
        long beatmap_set_id,
        int status,
        String artist,
        String title,
        float bpm,
        int circles,
        int sliders,
        String creator,
        long creator_id,
        int maxCombo,
        int total_length,
        boolean frozen,
        float ar,
        float od,
        float cs,
        float hp,
        int mode,
        int passes,
        int plays,
        float beatmap_diff,
        String checksum,
        Timestamp approved_date

) {
}