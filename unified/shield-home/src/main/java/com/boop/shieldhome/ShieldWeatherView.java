package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class ShieldWeatherView extends LinearLayout {
    private static final int CYAN=Color.rgb(77,184,255);
    ShieldWeatherView(Context context) {
        super(context); setOrientation(VERTICAL); setPadding(dp(18),dp(12),dp(18),dp(9));
        GradientDrawable bg=new GradientDrawable(); bg.setColor(Color.rgb(16,16,16)); bg.setCornerRadius(dp(14));
        bg.setStroke(dp(1),Color.rgb(48,48,48)); setBackground(bg); setFocusable(false); setClickable(false);
    }
    void bind(WeatherSnapshot s,long nowMs) {
        removeAllViews();
        if(s==null){ addView(text("Weather unavailable",24,true,Color.WHITE)); addView(text("Trying again automatically",15,false,Color.LTGRAY)); return; }
        LinearLayout top=row();
        top.addView(current(s),new LayoutParams(0,0,3f)); top.addView(divider(),new LayoutParams(dp(1),LayoutParams.MATCH_PARENT));
        top.addView(hours(s),new LayoutParams(0,0,4f)); top.addView(divider(),new LayoutParams(dp(1),LayoutParams.MATCH_PARENT));
        top.addView(days(s),new LayoutParams(0,0,3f)); addView(top,new LayoutParams(LayoutParams.MATCH_PARENT,0,1f));
        LinearLayout foot=row(); foot.setGravity(Gravity.CENTER_VERTICAL);
        foot.addView(text("↝  "+Math.round(s.windSpeed)+" km/h "+WeatherCode.compass(s.windDirection),12,false,CYAN),new LayoutParams(0,LayoutParams.WRAP_CONTENT,1f));
        foot.addView(text("☀  "+clock(s.sunrise)+"    ◐  "+clock(s.sunset),12,false,Color.LTGRAY),new LayoutParams(0,LayoutParams.WRAP_CONTENT,1f));
        long mins=Math.max(0,(nowMs-s.fetchedAtMs)/60000L); String age=mins<1?"just now":mins+" min ago";
        TextView source=text("Updated "+age+"  ·  Open-Meteo",11,false,Color.GRAY); source.setGravity(Gravity.END);
        foot.addView(source,new LayoutParams(0,LayoutParams.WRAP_CONTENT,1f)); addView(foot,new LayoutParams(LayoutParams.MATCH_PARENT,dp(22)));
    }
    private LinearLayout current(WeatherSnapshot s){
        LinearLayout box=column(); box.setPadding(0,0,dp(14),0);
        box.addView(text(s.location,20,true,Color.WHITE)); box.addView(text(WeatherCode.label(s.code),13,false,Color.LTGRAY));
        LinearLayout temp=row(); TextView icon=text(WeatherCode.glyph(s.code),42,false,CYAN); icon.setGravity(Gravity.CENTER_VERTICAL);
        temp.addView(icon,new LayoutParams(dp(62),LayoutParams.MATCH_PARENT));
        LinearLayout nums=column(); nums.setGravity(Gravity.CENTER_VERTICAL); nums.addView(text(Math.round(s.temp)+"°",40,true,Color.WHITE)); nums.addView(text("Feels "+Math.round(s.feels)+"°",12,false,Color.LTGRAY));
        temp.addView(nums,new LayoutParams(0,LayoutParams.MATCH_PARENT,1f)); box.addView(temp,new LayoutParams(LayoutParams.MATCH_PARENT,0,1f)); return box;
    }
    private LinearLayout hours(WeatherSnapshot s){
        LinearLayout box=column(); box.setPadding(dp(14),0,dp(14),0); box.addView(text("Next 4 hours",13,true,Color.WHITE));
        LinearLayout strip=row(); strip.setGravity(Gravity.CENTER_VERTICAL); for(WeatherSnapshot.Hour h:s.hours){
            LinearLayout cell=column(); cell.setGravity(Gravity.CENTER); cell.addView(text(clock(h.time),11,false,Color.LTGRAY)); cell.addView(text(WeatherCode.glyph(h.code),23,false,Color.WHITE));
            cell.addView(text(Math.round(h.temp)+"°",14,true,Color.WHITE)); cell.addView(text("● "+h.rain+"%",10,false,CYAN)); strip.addView(cell,new LayoutParams(0,LayoutParams.MATCH_PARENT,1f));
        } box.addView(strip,new LayoutParams(LayoutParams.MATCH_PARENT,0,1f)); return box;
    }
    private LinearLayout days(WeatherSnapshot s){
        LinearLayout box=column(); box.setPadding(dp(14),0,0,0); box.addView(text("3 day forecast",13,true,Color.WHITE)); LinearLayout strip=row();
        for(int i=0;i<s.days.size();i++){ WeatherSnapshot.Day d=s.days.get(i); LinearLayout cell=column(); cell.setGravity(Gravity.CENTER); String label=i==0?"Today":i==1?"Tomorrow":weekday(d.date);
            cell.addView(text(label,11,false,Color.LTGRAY)); cell.addView(text(WeatherCode.glyph(d.code),23,false,Color.WHITE)); cell.addView(text(Math.round(d.high)+"° / "+Math.round(d.low)+"°",12,true,Color.WHITE)); cell.addView(text("● "+d.rain+"%",10,false,CYAN)); strip.addView(cell,new LayoutParams(0,LayoutParams.MATCH_PARENT,1f));
        } box.addView(strip,new LayoutParams(LayoutParams.MATCH_PARENT,0,1f)); return box;
    }
    private LinearLayout row(){ LinearLayout v=new LinearLayout(getContext()); v.setOrientation(HORIZONTAL); return v; }
    private LinearLayout column(){ LinearLayout v=new LinearLayout(getContext()); v.setOrientation(VERTICAL); return v; }
    private TextView text(String value,float size,boolean bold,int color){ TextView v=new TextView(getContext()); v.setText(value); v.setTextColor(color); v.setTextSize(TypedValue.COMPLEX_UNIT_SP,size); v.setIncludeFontPadding(false); if(bold)v.setTypeface(v.getTypeface(),1); return v; }
    private android.view.View divider(){ android.view.View v=new android.view.View(getContext()); v.setBackgroundColor(Color.rgb(56,74,86)); return v; }
    private String clock(String iso){ if(iso==null)return ""; int t=iso.indexOf('T'); return t>=0&&iso.length()>=t+6?iso.substring(t+1,t+6):iso; }
    private String weekday(String iso){ try{return LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("EEE",Locale.UK));}catch(Exception e){return iso;} }
    private int dp(int v){ return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,v,getResources().getDisplayMetrics())); }
}
