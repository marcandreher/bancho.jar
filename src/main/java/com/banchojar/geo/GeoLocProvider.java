package com.banchojar.geo;

public interface GeoLocProvider {
    public GeoLocResponse getCountryCode(String ip);
}
