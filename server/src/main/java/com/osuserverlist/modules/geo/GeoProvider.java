package com.osuserverlist.modules.geo;

public interface GeoProvider {
    public GeoResponse getCountryCode(String ip);

}
