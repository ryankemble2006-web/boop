package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class ShieldWeatherView extends LinearLayout {
    private static final int HEADER_DP=24;
    private TextView lastDayTemperature;
    private int forecastLeft;

    ShieldWeatherView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setPadding(0,dp(6),0,dp(4));
        setBackground(null);
        setFocusable(false);
        setClickable(false);
    }

    void bind(WeatherSnapshot s,long nowMs) {
        removeAllViews();
        lastDayTemperature=null;
        if(s==null){
            LinearLayout empty=column();
            empty.setGravity(Gravity.CENTER_VERTICAL);
            empty.addView(text("Weather unavailable",24,true,Color.WHITE));
            empty.addView(text("Trying again automatically",15,false,Color.LTGRAY));
            addView(empty,new LayoutParams(LayoutParams.MATCH_PARENT,0,1f));
            return;
        }

        LinearLayout top=new AnchoredRow(false);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(current(s),new LayoutParams(dp(230),LayoutParams.MATCH_PARENT));
        top.addView(hours(s),new LayoutParams(dp(384),LayoutParams.MATCH_PARENT));
        LinearLayout forecastDays=days(s);
        top.addView(forecastDays,new LayoutParams(dp(288),LayoutParams.MATCH_PARENT));
        addView(top,new LayoutParams(LayoutParams.MATCH_PARENT,0,1f));

        LinearLayout foot=new AnchoredRow(true);
        foot.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout wind=iconLabel(R.drawable.boop_icon_wind,Math.round(s.windSpeed)+" km/h "+WeatherCode.compass(s.windDirection));
        foot.addView(wind,new LayoutParams(dp(230),LayoutParams.MATCH_PARENT));

        LinearLayout sun=row();sun.setGravity(Gravity.CENTER);
        sun.addView(iconLabel(R.drawable.boop_icon_sunrise,clock(s.sunrise)),new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT));
        LinearLayout sunset=iconLabel(R.drawable.boop_icon_sunset,clock(s.sunset));
        LayoutParams sunsetParams=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);sunsetParams.leftMargin=dp(12);
        sun.addView(sunset,sunsetParams);
        foot.addView(sun,new LayoutParams(dp(384),LayoutParams.MATCH_PARENT));

        long mins=Math.max(0,(nowMs-s.fetchedAtMs)/60000L);
        String age=mins<1?"just now":mins+" min ago";
        TextView source=text("Updated "+age+"  ·  Open-Meteo",11,false,Color.GRAY);
        source.setGravity(Gravity.CENTER);
        foot.addView(source,new LayoutParams(dp(288),LayoutParams.MATCH_PARENT));
        addView(foot,new LayoutParams(LayoutParams.MATCH_PARENT,dp(24)));
    }

    private LinearLayout current(WeatherSnapshot s){
        LinearLayout box=column();
        box.setPadding(0,0,0,0);
        addHeader(box,s.location);

        LinearLayout body=column();body.setGravity(Gravity.CENTER);
        LinearLayout hero=row();hero.setGravity(Gravity.CENTER);
        ImageView icon=weatherIcon(s.code);
        LayoutParams iconParams=new LayoutParams(dp(38),dp(44));iconParams.rightMargin=dp(8);
        hero.addView(icon,iconParams);
        TextView heroTemp=text(Math.round(s.temp)+"°C",38,false,BoopTvChrome.TEXT_PRIMARY);
        heroTemp.setTypeface(BoopTvChrome.FONT_LIGHT);
        heroTemp.setLetterSpacing(-0.02f);
        hero.addView(heroTemp,new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        body.addView(hero,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        addCenteredLine(body,text(WeatherCode.label(s.code),12.5f,true,BoopTvChrome.TEXT_PRIMARY),false);
        addCenteredLine(body,text("Feels like "+Math.round(s.feels)+"°C",11,false,BoopTvChrome.TEXT_SECONDARY),false);
        box.addView(body,new LayoutParams(LayoutParams.MATCH_PARENT,0,1f));
        return box;
    }

    private LinearLayout hours(WeatherSnapshot s){
        LinearLayout box=column();
        box.setPadding(0,0,0,0);
        addHeader(box,"Next 4 hours");

        LinearLayout strip=row();
        strip.setGravity(Gravity.CENTER);
        for(WeatherSnapshot.Hour h:s.hours){
            LinearLayout cell=column();
            cell.setGravity(Gravity.CENTER);
            addCenteredLine(cell,text(clock(h.time),11,false,BoopTvChrome.TEXT_SECONDARY),false);
            addWeatherIcon(cell,h.code);
            TextView temp=text(Math.round(h.temp)+"°",14.5f,true,BoopTvChrome.TEXT_PRIMARY);
            addCenteredLine(cell,temp,false);
            addRain(cell,h.rain);
            strip.addView(cell,new LayoutParams(0,LayoutParams.MATCH_PARENT,1f));
        }
        box.addView(strip,new LayoutParams(LayoutParams.MATCH_PARENT,0,1f));
        return box;
    }

    private LinearLayout days(WeatherSnapshot s){
        LinearLayout box=column();
        box.setPadding(0,0,0,0);
        addHeader(box,"3 day forecast");

        LinearLayout strip=row();
        strip.setGravity(Gravity.CENTER);
        for(int i=0;i<s.days.size();i++){
            WeatherSnapshot.Day d=s.days.get(i);
            LinearLayout cell=column();
            cell.setGravity(Gravity.CENTER);
            String label=i==0?"Today":i==1?"Tomorrow":weekday(d.date);
            addCenteredLine(cell,text(label,11,false,BoopTvChrome.TEXT_SECONDARY),false);
            addWeatherIcon(cell,d.code);
            TextView temperature=text(Math.round(d.high)+"° / "+Math.round(d.low)+"°",12.5f,true,BoopTvChrome.TEXT_PRIMARY);
            addCenteredLine(cell,temperature,false);
            lastDayTemperature=temperature;
            addRain(cell,d.rain);
            strip.addView(cell,new LayoutParams(0,LayoutParams.MATCH_PARENT,1f));
        }
        box.addView(strip,new LayoutParams(LayoutParams.MATCH_PARENT,0,1f));
        return box;
    }

    private void addHeader(LinearLayout box,String value){
        TextView title=text(value == null ? "" : value.toUpperCase(Locale.UK),11.5f,true,BoopTvChrome.TEXT_SECONDARY);
        title.setLetterSpacing(0.08f);
        title.setGravity(Gravity.CENTER);
        title.setSingleLine(true);
        box.addView(title,new LayoutParams(LayoutParams.MATCH_PARENT,dp(HEADER_DP)));
    }

    private void addCenteredLine(LinearLayout cell,TextView line,boolean weatherGlyph){
        line.setGravity(Gravity.CENTER);
        line.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        cell.addView(line,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
    }
    private ImageView weatherIcon(int code){
        ImageView image=new WeatherGlyph(WeatherCode.icon(code),34.32f);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setContentDescription(WeatherCode.label(code));
        return image;
    }
    private void addWeatherIcon(LinearLayout cell,int code){
        ImageView image=weatherIcon(code);
        LayoutParams params=new LayoutParams(dp(31),dp(31));params.gravity=Gravity.CENTER_HORIZONTAL;
        params.topMargin=dp(2);params.bottomMargin=dp(2);cell.addView(image,params);
    }
    private LinearLayout iconLabel(int resource,String value){
        LinearLayout group=row();group.setGravity(Gravity.CENTER);
        ImageView icon=new WeatherGlyph(resource,16);
        group.addView(icon,new LayoutParams(PrecisionIcons.dp(getContext(),14.4f),PrecisionIcons.dp(getContext(),14.4f)));
        TextView label=text(value,12,false,Color.LTGRAY);
        LayoutParams params=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);params.leftMargin=dp(5);
        group.addView(label,params);return group;
    }
    private void addRain(LinearLayout cell,int rain){
        LinearLayout group=iconLabel(R.drawable.boop_icon_droplet,rain+"%");
        TextView label=(TextView)group.getChildAt(1);label.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);label.setTextColor(accent());
        cell.addView(group,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
    }
    /** Independent compact groups anchored to the favourite, screen and final temperature ink. */
    private final class AnchoredRow extends LinearLayout {
        private final boolean footer;
        AnchoredRow(boolean footer) {
            super(ShieldWeatherView.this.getContext());this.footer=footer;
            setOrientation(HORIZONTAL);setClipChildren(false);setClipToPadding(false);
        }
        @Override protected void onLayout(boolean changed,int l,int t,int r,int b) {
            super.onLayout(changed,l,t,r,b);
            if(getChildCount()!=3)return;
            ViewPlacement.place(getChildAt(0),0);
            ViewPlacement.place(getChildAt(1),(getWidth()-getChildAt(1).getMeasuredWidth())/2);
            if(!footer) {
                android.view.View forecast=getChildAt(2);
                int right=forecast.getMeasuredWidth();
                if(lastDayTemperature!=null) {
                    Rect ink=new Rect();String value=lastDayTemperature.getText().toString();
                    lastDayTemperature.getPaint().getTextBounds(value,0,value.length(),ink);
                    int textLeft=Math.round((lastDayTemperature.getWidth()-lastDayTemperature.getPaint().measureText(value))/2f);
                    android.view.View node=lastDayTemperature;int offset=0;
                    while(node!=forecast){offset+=node.getLeft();node=(android.view.View)node.getParent();}
                    right=offset+textLeft+ink.right;
                }
                forecastLeft=getWidth()-right;
            }
            ViewPlacement.place(getChildAt(2),forecastLeft);
        }
    }
    private static final class ViewPlacement {
        static void place(android.view.View view,int left) {
            view.layout(left,view.getTop(),left+view.getMeasuredWidth(),view.getBottom());
        }
    }
    /** Centre the visible vector ink, including asymmetric clouds and wind trails. */
    private final class WeatherGlyph extends ImageView {
        private final int resource;
        private final float sizeDp;
        private final Drawable outline;
        WeatherGlyph(int resource,float sizeDp) {
            super(ShieldWeatherView.this.getContext());
            this.resource=resource;this.sizeDp=sizeDp;
            outline=getContext().getDrawable(resource).mutate();
            setImageDrawable(outline);
            setFocusable(false);
        }
        @Override protected void onDraw(Canvas canvas) {
            int size=Math.min(PrecisionIcons.dp(getContext(),sizeDp*PrecisionIcons.SCALE),Math.min(getWidth(),getHeight()));
            if(size<=0)return;
            Rect ink=PrecisionIcons.inkBounds(getContext(),resource,sizeDp,getWidth(),getHeight());
            int x=(getWidth()-size)/2+Math.round((getWidth()-ink.left-ink.right)/2f);
            int y=(getHeight()-size)/2+Math.round((getHeight()-ink.top-ink.bottom)/2f);
            outline.setTint(accent());
            outline.setBounds(x,y,x+size,y+size);
            outline.draw(canvas);
        }
    }
    private LinearLayout row(){ LinearLayout v=new LinearLayout(getContext()); v.setOrientation(HORIZONTAL); return v; }
    private LinearLayout column(){ LinearLayout v=new LinearLayout(getContext()); v.setOrientation(VERTICAL); return v; }
    private TextView text(String value,float size,boolean bold,int color){
        TextView v=new TextView(getContext());
        BoopTvChrome.markCustomStyled(v);
        v.setText(value);
        v.setTextColor(color);
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP,size);
        v.setIncludeFontPadding(false);
        v.setFontFeatureSettings("tnum");
        v.setTypeface(bold ? BoopTvChrome.FONT_MEDIUM : BoopTvChrome.FONT_REGULAR);
        v.setLetterSpacing(bold ? 0.015f : 0.01f);
        return v;
    }
    private String clock(String iso){ if(iso==null)return ""; int t=iso.indexOf('T'); return t>=0&&iso.length()>=t+6?iso.substring(t+1,t+6):iso; }
    private String weekday(String iso){ try{return LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("EEE",Locale.UK));}catch(Exception e){return iso;} }
    private int accent(){ return FocusChrome.accentColor(getContext()); }
    private int dp(int v){ return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,v,getResources().getDisplayMetrics())); }
}
