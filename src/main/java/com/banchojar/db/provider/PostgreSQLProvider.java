package com.banchojar.db.provider;

import org.jooq.SQLDialect;

import com.banchojar.App.Config;

public class PostgreSQLProvider implements Provider {

    @Override
    public String getConnectionString(Config config) {
        return "jdbc:postgresql://" + config.getDbHost() + ":" + config.getDbPort() + "/" + config.getDbName();
    }

    @Override
    public Providers getProvider() {
        return Providers.POSTGRESQL;
    }

    @Override
    public SQLDialect getDialect() {
        return SQLDialect.POSTGRES;
    }

    @Override
    public String getDriverClassName() {
        return "org.postgresql.Driver";
    }
    
}
