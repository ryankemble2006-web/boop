package com.boop.shieldhome;

import java.util.List;

final class WeatherSnapshot {
    static final class Hour {
        final String time; final int code; final double temp; final int rain;
        Hour(String time, int code, double temp, int rain) { this.time=time; this.code=code; this.temp=temp; this.rain=rain; }
    }
    static final class Day {
        final String date; final int code; final double high, low; final int rain;
        Day(String date, int code, double high, double low, int rain) { this.date=date; this.code=code; this.high=high; this.low=low; this.rain=rain; }
    }
    final String location, currentTime, sunrise, sunset;
    final int code; final double temp, feels, windSpeed; final int windDirection;
    final long fetchedAtMs; final List<Hour> hours; final List<Day> days;
    WeatherSnapshot(String location, String currentTime, int code, double temp, double feels,
            double windSpeed, int windDirection, String sunrise, String sunset, long fetchedAtMs,
            List<Hour> hours, List<Day> days) {
        this.location=location; this.currentTime=currentTime; this.code=code; this.temp=temp; this.feels=feels;
        this.windSpeed=windSpeed; this.windDirection=windDirection; this.sunrise=sunrise; this.sunset=sunset;
        this.fetchedAtMs=fetchedAtMs; this.hours=List.copyOf(hours); this.days=List.copyOf(days);
    }
}
