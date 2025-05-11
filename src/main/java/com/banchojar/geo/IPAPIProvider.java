package com.banchojar.geo;

import java.io.IOException;

import com.banchojar.App;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IPAPIProvider implements GeoLocProvider {

    public final String URL = "http://ip-api.com/json/%ip%?fields=status,message,countryCode,lat,lon";
    private final static OkHttpClient client = new OkHttpClient();

    @Override
    public GeoLocResponse getCountryCode(String ip) {
        String url = URL.replace("%ip%", ip);
        App.logger.debug("Fetching country code for IP: " + ip);
        Request request = new Request.Builder().url(url).build();
        GeoLocResponse geoLocResponse = new GeoLocResponse();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return geoLocResponse;
            }
            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            if ("success".equals(json.get("status").getAsString())) {
                geoLocResponse.setLatitude(json.get("lat").getAsFloat());
                geoLocResponse.setLongitude(json.get("lon").getAsFloat());
                geoLocResponse.setCountryId(Country.getIndexByCode(json.get("countryCode").getAsString()));
                return geoLocResponse;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch country code", e);
        }
        return geoLocResponse; 
    }

}
