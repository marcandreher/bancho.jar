package com.banchojar.db.provider;

public enum Providers {
    MYSQL(new MariaDBProvider()),
    POSTGRESQL(new PostgreSQLProvider()),
    SQLITE(new SQLiteProvider()),
    MARIADB(new MariaDBProvider()),;

    private final Provider provider;

    Providers(Provider provider) {
        this.provider = provider;
    }

    Providers(String name) {
        Provider matchedProvider = null;
        for (Providers provider : Providers.values()) {
            if (provider.name().equalsIgnoreCase(name)) {
                matchedProvider = provider.provider;
                break;
            }
        }
        if (matchedProvider == null) {
            throw new IllegalArgumentException("No provider found for name: " + name);
        }
        this.provider = matchedProvider;
    }

    public static Provider fromString(String name) {
        for (Providers provider : Providers.values()) {
            if (provider.name().equalsIgnoreCase(name)) {
                return provider.getProvider();
            }
        }
        return null;
    }

    public Provider getProvider() {
        return provider;
    }    
}
