package com.banchojar.db.models;

import java.time.OffsetDateTime;

public record LoginRecord(
        int id,
        String user_id,
        String ip,
        OffsetDateTime timestamp,
        String ver) {

}
