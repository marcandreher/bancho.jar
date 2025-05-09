package com.banchojar.db.provider;

import org.jooq.SQLDialect;

import com.banchojar.App.Config;

public interface Provider {
    public String getConnectionString(Config config);
    public Providers getProvider();
    public SQLDialect getDialect();
    public String getDriverClassName();
}
