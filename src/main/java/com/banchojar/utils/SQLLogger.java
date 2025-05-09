package com.banchojar.utils;

import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLLogger implements ExecuteListener {
    private static final Logger log = LoggerFactory.getLogger("packets");

    @Override
    public void executeStart(ExecuteContext ctx) {
        log.info("[SQL] " + ctx.sql());
    }

    @Override
    public void executeEnd(ExecuteContext ctx) {
    }

    @Override
    public void exception(ExecuteContext ctx) {
        log.error("Execution failed", ctx.sqlException());
    }
}
