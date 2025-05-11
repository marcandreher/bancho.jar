package com.banchojar.geo;

public enum GeoProviders {
    IPAPI(new IPAPIProvider());

    private final GeoLocProvider provider;

    GeoProviders(GeoLocProvider provider) {
        this.provider = provider;
    }

    public GeoLocProvider getProvider() {
        return provider;
    }

    public static GeoLocProvider fromString(String provider) {
        for (GeoProviders geoProvider : values()) {
            if (geoProvider.name().equalsIgnoreCase(provider)) {
                return geoProvider.provider;
            }
        }
        return null;
    }
}
