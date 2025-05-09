package com.banchojar.db.provider;

import org.jooq.SQLDialect;

import com.banchojar.App.Config;

public class MySQLProvider implements Provider {

    @Override
    public String getConnectionString(Config config) {
        return "jdbc:mysql://" + config.getDbHost() + ":" + config.getDbPort() + "/" + config.getDbName();
    }

    @Override
    public Providers getProvider() {
        return Providers.MYSQL;
    }

    @Override
    public SQLDialect getDialect() {
        return SQLDialect.MYSQL;
    }

    @Override
    public String getDriverClassName() {
        return "com.mysql.cj.jdbc.Driver";
    }
    
}
