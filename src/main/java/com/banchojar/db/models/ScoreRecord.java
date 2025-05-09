package com.banchojar.db.models;

import java.sql.Timestamp;

public record ScoreRecord(
        Integer score_id,
        String map_md5,
        Integer user_id,
        Long score,
        Integer max_combo,
        Integer count_300,
        Integer count_100,
        Integer count_50,
        Integer count_geki,
        Integer count_katu,
        Integer count_miss,
        Boolean perfect,
        Integer mods,
        String grade,
        Timestamp playtime,
        String rank,
        Float pp,
        Float acc,
        Integer flags,
        Float diff) {
}