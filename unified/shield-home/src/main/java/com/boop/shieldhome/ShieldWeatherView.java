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
    private static final int HEADER_DP=26;

    ShieldWeatherView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setPadding(dp(18),dp(10),dp(18),dp(8));
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.rgb(16,16,16));
        bg.setCornerRadius(dp(14));
        bg.setStroke(dp(1),Color.rgb(48,48,48));
        setBackground(bg);
        setFocusable(false);
        setClickable(false);
    }

    void bind(WeatherSnapshot s,long nowMs) {
        removeAllViews();
        if(s==null){
            LinearLayout empty=column();
            empty.setGravity(Gravity.CENTER_VERTICAL);
            empty.addView(text("Weather unavailable",24,true,Color.WHITE));
            empty.addView(text("Trying again automatically",15,false,Color.LTGRAY));
            addView(empty,new LayoutParams(LayoutParams.MATCH_PARENT,0,1f));
            return;
        }

        LinearLayout top=row();
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(current(s),new LayoutParams(0,LayoutParams.MATCH_PARENT,3f));
        top.addView(divider(),new LayoutParams(dp(1),LayoutParams.MATCH_PARENT));
        top.addView(hours(s),new LayoutParams(0,LayoutParams.MATCH_PARENT,4f));
        top.addView(divider(),new LayoutParams(dp(1),LayoutParams.MATCH_PARENT));
        top.addView(days(s),new LayoutParams(0,LayoutParams.MATCH_PARENT,3f));
        addView(top,new LayoutParams(LayoutParams.MATCH_PARENT,0,1f));

        LinearLayout foot=row();
        foot.setGravity(Gravity.CENTER_VERTICAL);
        TextView wind=text("↝  "+Math.round(s.windSpeed)+" km/h "+WeatherCode.compass(s.windDirection),12,false,CYAN);
        wind.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        foot.addView(wind,new LayoutParams(0,LayoutParams.MATCH_PARENT,1f));

        TextView sun=text("☀  "+clock(s.sunrise)+"    ◐  "+clock(s.sunset),12,false,Color.LTGRAY);
        sun.setGravity(Gravity.CENTER);
        foot.addView(sun,new LayoutParams(0,LayoutParams.MATCH_PARENT,1f));

        long mins=Math.max(0,(nowMs-s.fetchedAtMs)/60000L);
        String age=mins<1?"just now":mins+" min ago";
        TextView source=text("Updated "+age+"  ·  Open-Meteo",11,false,Color.GRAY);
        source.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
        foot.addView(source,new LayoutParams(0,LayoutParams.MATCH_PARENT,1f));
        addView(foot,new LayoutParams(LayoutParams.MATCH_PARENT,dp(24)));
    }

    private LinearLayout current(WeatherSnapshot s){
        LinearLayout box=column();
        box.setPadding(0,0,dp(14),0);
        addHeader(box,s.location);

        LinearLayout body=row();
        body.setGravity(Gravity.CENTER_VERTICAL);
        TextView icon=text(WeatherCode.glyph(s.code),40,false,CYAN);
        icon.setGravity(Gravity.CENTER);
        body.addView(icon,new LayoutParams(dp(58),LayoutParams.MATCH_PARENT));

        LinearLayout nums=column();
        nums.setGravity(Gravity.CENTER_VERTICAL);
        nums.addView(text(Math.round(s.temp)+"°",38,true,Color.WHITE));
        nums.addView(text(WeatherCode.label(s.code),12,false,Color.LTGRAY));
        nums.addView(text("Feels "+Math.round(s.feels)+"°",11,false,Color.LTGRAY));
        body.addView(nums,new LayoutParams(0,LayoutParams.MATCH_PARENT,1f));
        box.addView(body,new LayoutParams(LayoutParams.MATCH_PARENT,0,1f));
        return box;
    }

    private LinearLayout hours(WeatherSnapshot s){
        LinearLayout box=column();
        box.setPadding(dp(14),0,dp(14),0);
        addHeader(box,"Next 4 hours");

        LinearLayout strip=row();
        strip.setGravity(Gravity.CENTER_VERTICAL);
        for(WeatherSnapshot.Hour h:s.hours){
            LinearLayout cell=column();
            cell.setGravity(Gravity.CENTER);
            cell.addView(text(clock(h.time),11,false,Color.LTGRAY));
            cell.addView(text(WeatherCode.glyph(h.code),23,false,Color.WHITE));
            cell.addView(text(Math.round(h.temp)+"°",14,true,Color.WHITE));
            cell.addView(text("● "+h.rain+"%",10,false,CYAN));
            strip.addView(cell,new LayoutParams(0,LayoutParams.MATCH_PARENT,1f));
        }
        box.addView(strip,new LayoutParams(LayoutParams.MATCH_PARENT,0,1f));
        return box;
    }

    private LinearLayout days(WeatherSnapshot s){
        LinearLayout box=column();
        box.setPadding(dp(14),0,0,0);
        addHeader(box,"3 day forecast");

        LinearLayout strip=row();
        strip.setGravity(Gravity.CENTER_VERTICAL);
        for(int i=0;i<s.days.size();i++){
            WeatherSnapshot.Day d=s.days.get(i);
            LinearLayout cell=column();
            cell.setGravity(Gravity.CENTER);
            String label=i==0?"Today":i==1?"Tomorrow":weekday(d.date);
            cell.addView(text(label,11,false,Color.LTGRAY));
            cell.addView(text(WeatherCode.glyph(d.code),23,false,Color.WHITE));
            cell.addView(text(Math.round(d.high)+"° / "+Math.round(d.low)+"°",12,true,Color.WHITE));
            cell.addView(text("● "+d.rain+"%",10,false,CYAN));
            strip.addView(cell,new LayoutParams(0,LayoutParams.MATCH_PARENT,1f));
        }
        box.addView(strip,new LayoutParams(LayoutParams.MATCH_PARENT,0,1f));
        return box;
    }

    private void addHeader(LinearLayout box,String value){
        TextView title=text(value,13,true,Color.WHITE);
        title.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        title.setSingleLine(true);
        box.addView(title,new LayoutParams(LayoutParams.MATCH_PARENT,dp(HEADER_DP)));
    }

    private LinearLayout row(){ LinearLayout v=new LinearLayout(getContext()); v.setOrientation(HORIZONTAL); return v; }
    private LinearLayout column(){ LinearLayout v=new LinearLayout(getContext()); v.setOrientation(VERTICAL); return v; }
    private TextView text(String value,float size,boolean bold,int color){ TextView v=new TextView(getContext()); v.setText(value); v.setTextColor(color); v.setTextSize(TypedValue.COMPLEX_UNIT_SP,size); v.setIncludeFontPadding(false); if(bold)v.setTypeface(v.getTypeface(),1); return v; }
    private android.view.View divider(){ android.view.View v=new android.view.View(getContext()); v.setBackgroundColor(Color.rgb(56,74,86)); return v; }
    private String clock(String iso){ if(iso==null)return ""; int t=iso.indexOf('T'); return t>=0&&iso.length()>=t+6?iso.substring(t+1,t+6):iso; }
    private String weekday(String iso){ try{return LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("EEE",Locale.UK));}catch(Exception e){return iso;} }
    private int dp(int v){ return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,v,getResources().getDisplayMetrics())); }
}
