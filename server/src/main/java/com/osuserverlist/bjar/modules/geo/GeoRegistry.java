package com.osuserverlist.bjar.modules.geo;

public class GeoRegistry {
    private static GeoProvider provider = new IPAPIProvider();

    public static void upsertProvider(GeoProvider provider) {
        GeoRegistry.provider = provider;
    }

    public static GeoProvider getProvider() {
        return provider;
    }
}
