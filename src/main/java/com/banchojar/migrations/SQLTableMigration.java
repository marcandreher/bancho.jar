package com.banchojar.migrations;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import com.banchojar.Server;

public class SQLTableMigration implements Migration {

    @Override
    public void migrate(DSLContext dsl) {

        Integer userId = Server.dsl.select(DSL.field("id"))
                .from(DSL.table("users"))
                .where(DSL.field("id").eq(1))
                .fetchOne(DSL.field("id"), int.class);

        if (userId == null) {
            // Create bot user
            Server.dsl.insertInto(DSL.table("users"))
                    .columns(DSL.field("id"), DSL.field("username"), DSL.field("email"), DSL.field("password_hash"),
                            DSL.field("country"))
                    .values(1, "BanchoBot", "bot@bot.ppy.sh", "BOT", "US").execute();
        }

        Server.dsl.createTableIfNotExists("users_stats")
                .column("user_id", SQLDataType.INTEGER)
                .column("mode", SQLDataType.TINYINT)
                .column("ranked_score", SQLDataType.BIGINT)
                .column("accuracy", SQLDataType.DOUBLE)
                .column("play_count", SQLDataType.INTEGER)
                .column("total_score", SQLDataType.BIGINT)
                .column("global_rank", SQLDataType.INTEGER)
                .column("pp", SQLDataType.BIGINT)

                .constraints(
                        DSL.constraint("pk_users_stats").primaryKey("user_id", "mode"))
                .execute();

        Server.dsl.createTableIfNotExists("logins")
                .column("id", SQLDataType.INTEGER.identity(true))
                .column("user_id", SQLDataType.INTEGER)
                .column("ip", SQLDataType.VARCHAR(15))
                .column("timestamp", SQLDataType.TIMESTAMP)
                .column("ver", SQLDataType.VARCHAR(15))
                .constraints(
                        DSL.constraint("pk_logins").primaryKey("id"))
                .execute();

        Server.dsl.createTableIfNotExists("client_hashes")
                .column("user_id", SQLDataType.INTEGER)
                .column("executable_hash", SQLDataType.VARCHAR(255))
                .column("network_interface_hash", SQLDataType.VARCHAR(255))
                .column("registry_hash", SQLDataType.VARCHAR(255))
                .column("disk_drive_hash", SQLDataType.VARCHAR(255))
                .constraints(
                        DSL.constraint("pk_hashes").primaryKey(
                                "user_id", "executable_hash", "network_interface_hash", "registry_hash",
                                "disk_drive_hash"))
                .execute();

        Server.dsl.createTableIfNotExists("beatmaps")
                .column("beatmap_id", SQLDataType.BIGINT.nullable(false))
                .column("beatmap_set_id", SQLDataType.BIGINT.nullable(false))
                .column("status", SQLDataType.INTEGER.nullable(false))
                .column("artist", SQLDataType.VARCHAR(255).nullable(false))
                .column("title", SQLDataType.VARCHAR(255).nullable(false))
                .column("bpm", SQLDataType.FLOAT.nullable(false))
                .column("circles", SQLDataType.INTEGER.nullable(false))
                .column("sliders", SQLDataType.INTEGER.nullable(false))
                .column("creator", SQLDataType.VARCHAR(255).nullable(false))
                .column("creator_id", SQLDataType.BIGINT.nullable(false))
                .column("maxCombo", SQLDataType.INTEGER.nullable(false))
                .column("total_length", SQLDataType.INTEGER.nullable(false))
                .column("frozen", SQLDataType.BOOLEAN.nullable(false))
                .column("ar", SQLDataType.FLOAT.nullable(false))
                .column("od", SQLDataType.FLOAT.nullable(false))
                .column("cs", SQLDataType.FLOAT.nullable(false))
                .column("hp", SQLDataType.FLOAT.nullable(false))
                .column("mode", SQLDataType.TINYINT.nullable(false))
                .column("passes", SQLDataType.INTEGER.nullable(false))
                .column("plays", SQLDataType.INTEGER.nullable(false))
                .column("beatmap_diff", SQLDataType.FLOAT.nullable(false))
                .column("checksum", SQLDataType.VARCHAR(255).nullable(false))
                .column("approved_date", SQLDataType.TIMESTAMP.defaultValue((java.sql.Timestamp) null))
                .constraints(
                        DSL.constraint("pk_beatmap").primaryKey("beatmap_id"))
                .execute();

        Server.dsl.createTableIfNotExists("scores")
                .column("score_id", SQLDataType.INTEGER.identity(true))
                .column("map_md5", SQLDataType.VARCHAR(255))
                .column("user_id", SQLDataType.INTEGER)
                .column("score", SQLDataType.BIGINT)
                .column("max_combo", SQLDataType.INTEGER)
                .column("count_300", SQLDataType.INTEGER)
                .column("count_100", SQLDataType.INTEGER)
                .column("count_50", SQLDataType.INTEGER)
                .column("count_geki", SQLDataType.INTEGER)
                .column("count_katu", SQLDataType.INTEGER)
                .column("count_miss", SQLDataType.INTEGER)
                .column("perfect", SQLDataType.BOOLEAN)
                .column("mods", SQLDataType.INTEGER)
                .column("grade", SQLDataType.VARCHAR(2))
                .column("playtime", SQLDataType.TIMESTAMP)
                .column("mode", SQLDataType.TINYINT)
                .column("pp", SQLDataType.FLOAT)
                .column("acc", SQLDataType.FLOAT)
                .column("flags", SQLDataType.BIGINT)
                .column("diff", SQLDataType.FLOAT)
                .column("checksum", SQLDataType.VARCHAR(32))
                .constraints(
                        // Define score_id as the primary key
                        DSL.constraint("pk_scores").primaryKey("score_id"))
                .execute();
    }

    @Override
    public void rollback(DSLContext dsl) {
        dsl.dropTableIfExists("users_stats").execute();
        dsl.dropTableIfExists("logins").execute();
        dsl.dropTableIfExists("client_hashes").execute();
        dsl.dropTableIfExists("beatmaps").execute();
        dsl.dropTableIfExists("scores").execute();
    }

    @Override
    public boolean isNeeded() {
        return true;
    }

}
