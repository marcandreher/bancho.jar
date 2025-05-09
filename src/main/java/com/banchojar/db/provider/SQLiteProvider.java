package com.banchojar.db.provider;

import org.jooq.SQLDialect;

import com.banchojar.App.Config;

public class SQLiteProvider implements Provider {

    @Override
    public String getConnectionString(Config config) {
        return "jdbc:sqlite:" + config.getDbName();
    }

    @Override
    public Providers getProvider() {
        return Providers.SQLITE;
    }

    @Override
    public SQLDialect getDialect() {
        return SQLDialect.SQLITE;
    }

    @Override
    public String getDriverClassName() {
        return "org.sqlite.JDBC";
    }
    
}
