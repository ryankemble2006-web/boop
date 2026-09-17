package com.boop.shieldhome;

final class WeatherCode {
    private WeatherCode() { }
    static String label(int code) {
        if (code == 0) return "Clear sky";
        if (code == 1) return "Mainly clear";
        if (code == 2) return "Partly cloudy";
        if (code == 3) return "Overcast";
        if (code == 45 || code == 48) return "Fog";
        if (code >= 51 && code <= 57) return "Drizzle";
        if (code >= 61 && code <= 67) return "Rain";
        if (code >= 71 && code <= 77) return "Snow";
        if (code >= 80 && code <= 82) return "Rain showers";
        if (code >= 85 && code <= 86) return "Snow showers";
        if (code >= 95) return "Thunderstorms";
        return "Weather";
    }
    static String glyph(int code) {
        if (code == 0) return "☀";
        if (code <= 2) return "⛅";
        if (code == 3) return "☁";
        if (code == 45 || code == 48) return "≋";
        if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82)) return "☂";
        if ((code >= 71 && code <= 77) || (code >= 85 && code <= 86)) return "❄";
        if (code >= 95) return "⚡";
        return "☁";
    }
    static String compass(int degrees) {
        String[] points={"N","NE","E","SE","S","SW","W","NW"};
        int index=(int)Math.round((((degrees%360)+360)%360)/45.0)%8;
        return points[index];
    }
}
