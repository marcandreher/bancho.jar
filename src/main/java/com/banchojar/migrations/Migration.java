package com.banchojar.migrations;

import org.jooq.DSLContext;

public interface Migration {
    /**
     * This method is called when the migration is run.
     * 
     * @param dsl The DSLContext to use for the migration.
     */
    void migrate(DSLContext dsl);

    /**
     * This method is called when the migration is rolled back.
     * 
     * @param dsl The DSLContext to use for the rollback.
     */
    void rollback(DSLContext dsl);

    boolean isNeeded();
}
