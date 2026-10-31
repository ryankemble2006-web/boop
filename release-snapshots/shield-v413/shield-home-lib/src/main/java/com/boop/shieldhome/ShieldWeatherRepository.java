package com.boop.shieldhome;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class ShieldWeatherRepository {
    private static final String PREFS="boop_shield_weather_v1";
    private static final String KEY_JSON="forecast_json";
    private static final String KEY_FETCHED="forecast_fetched_ms";
    private static final long FRESH_MS=30L*60L*1000L;
    private static final long STALE_MS=6L*60L*60L*1000L;
    private static final String LOCATION="Cardiff";
    private static final double LAT=51.4816, LON=-3.1791;
    private final SharedPreferences prefs;

    ShieldWeatherRepository(Context context) { prefs=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE); }

    WeatherSnapshot load(long nowMs) {
        String cached=prefs.getString(KEY_JSON,""); long fetched=prefs.getLong(KEY_FETCHED,0L);
        if (!cached.isEmpty() && nowMs-fetched>=0 && nowMs-fetched<FRESH_MS) {
            try { return parse(cached,fetched); } catch(Exception ignored) { }
        }
        try {
            String body=fetch(); WeatherSnapshot parsed=parse(body,nowMs);
            prefs.edit().putString(KEY_JSON,body).putLong(KEY_FETCHED,nowMs).apply();
            return parsed;
        } catch(Exception unavailable) {
            if (!cached.isEmpty() && nowMs-fetched>=0 && nowMs-fetched<STALE_MS) {
                try { return parse(cached,fetched); } catch(Exception ignored) { }
            }
            return null;
        }
    }

    private String fetch() throws Exception {
        String endpoint=String.format(Locale.US,
            "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current=temperature_2m,apparent_temperature,weather_code,wind_speed_10m,wind_direction_10m&hourly=temperature_2m,precipitation_probability,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,sunrise,sunset&timezone=auto&forecast_days=4",
            LAT,LON);
        HttpURLConnection c=(HttpURLConnection)new URL(endpoint).openConnection();
        c.setConnectTimeout(5000); c.setReadTimeout(5000); c.setRequestMethod("GET");
        c.setRequestProperty("Accept","application/json"); c.setRequestProperty("User-Agent","BOOP-Shield-Weather/1");
        int code=c.getResponseCode(); if(code<200||code>=300) throw new IllegalStateException("HTTP "+code);
        try(BufferedReader r=new BufferedReader(new InputStreamReader(c.getInputStream(),StandardCharsets.UTF_8))) {
            StringBuilder out=new StringBuilder(); String line; while((line=r.readLine())!=null) out.append(line); return out.toString();
        } finally { c.disconnect(); }
    }

    static WeatherSnapshot parse(String body,long fetchedAtMs) throws Exception {
        JSONObject root=new JSONObject(body), current=root.getJSONObject("current");
        JSONObject hourly=root.getJSONObject("hourly"), daily=root.getJSONObject("daily");
        String currentTime=current.getString("time");
        JSONArray hourTimes=hourly.getJSONArray("time"), hourTemps=hourly.getJSONArray("temperature_2m");
        JSONArray hourRain=hourly.getJSONArray("precipitation_probability"), hourCodes=hourly.getJSONArray("weather_code");
        List<WeatherSnapshot.Hour> hours=new ArrayList<>();
        for(int i=0;i<hourTimes.length()&&hours.size()<4;i++) {
            String time=hourTimes.getString(i); if(time.compareTo(currentTime)<0) continue;
            hours.add(new WeatherSnapshot.Hour(time,hourCodes.getInt(i),hourTemps.getDouble(i),hourRain.optInt(i,0)));
        }
        JSONArray dates=daily.getJSONArray("time"), dayCodes=daily.getJSONArray("weather_code");
        JSONArray highs=daily.getJSONArray("temperature_2m_max"), lows=daily.getJSONArray("temperature_2m_min");
        JSONArray rains=daily.getJSONArray("precipitation_probability_max");
        List<WeatherSnapshot.Day> days=new ArrayList<>();
        for(int i=0;i<dates.length()&&i<3;i++) days.add(new WeatherSnapshot.Day(
            dates.getString(i),dayCodes.getInt(i),highs.getDouble(i),lows.getDouble(i),rains.optInt(i,0)));
        String sunrise=daily.getJSONArray("sunrise").optString(0,"");
        String sunset=daily.getJSONArray("sunset").optString(0,"");
        return new WeatherSnapshot(LOCATION,currentTime,current.getInt("weather_code"),
            current.getDouble("temperature_2m"),current.getDouble("apparent_temperature"),
            current.getDouble("wind_speed_10m"),current.optInt("wind_direction_10m",0),
            sunrise,sunset,fetchedAtMs,hours,days);
    }
}
