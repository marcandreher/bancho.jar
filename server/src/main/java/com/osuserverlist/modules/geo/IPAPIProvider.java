package com.osuserverlist.modules.geo;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IPAPIProvider implements GeoProvider {

    public final String URL = "http://ip-api.com/json/%ip%?fields=status,message,countryCode,lat,lon";
    private final static OkHttpClient client = new OkHttpClient();

    @Override
    public GeoResponse getCountryCode(String ip) {
        String url = URL.replace("%ip%", ip);
        Request request = new Request.Builder().url(url).build();
        GeoResponse geoResponse = new GeoResponse();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return geoResponse;
            }
            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            if ("success".equals(json.get("status").getAsString())) {
                geoResponse.setLatitude(json.get("lat").getAsFloat());
                geoResponse.setLongitude(json.get("lon").getAsFloat());
                geoResponse.setCountryId(Country.getIndexByCode(json.get("countryCode").getAsString()));
                return geoResponse;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch country code", e);
        }
        return geoResponse; 
    }

}