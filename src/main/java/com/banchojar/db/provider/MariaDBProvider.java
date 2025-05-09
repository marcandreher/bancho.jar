package com.banchojar.db.provider;

import org.jooq.SQLDialect;

import com.banchojar.App.Config;

public class MariaDBProvider implements Provider {

    @Override
    public String getConnectionString(Config config) {
        return "jdbc:mysql://" + config.getDbHost() + ":" + config.getDbPort() + "/" + config.getDbName();
    }

    @Override
    public Providers getProvider() {
        return Providers.MARIADB;
    }

    @Override
    public SQLDialect getDialect() {
        return SQLDialect.MARIADB;
    }

    @Override
    public String getDriverClassName() {
        return "com.mysql.cj.jdbc.Driver";
    }
    
}
