package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** One review screen. Selection changes only a local set; Apply is always explicit. */
final class StartupDefaultsScreen extends LinearLayout {
    StartupDefaultsScreen(Context context,List<StartupDefaultsCoordinator.Row> rows,Set<String> selected,
            boolean undo,boolean busy,String status,boolean autoOwned,Runnable apply,Runnable back,Runnable keep) {
        super(context);setOrientation(VERTICAL);setBackgroundColor(Color.BLACK);
        setPadding(dp(30),dp(22),dp(30),dp(20));
        TextView title=text(undo?"Undo BOOP defaults":"Use BOOP defaults",27);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);addView(title);
        TextView explanation=text(undo
                ?"Restore only the changes made by this preset. Newer conflicting settings are left alone. Interrupted changes keep their saved originals."
                :"Review these exact apps. Untick anything you use. This can disable Prime Video, Live Channels and vendor features; it is not a universal safe list.",14);
        explanation.setTextColor(Color.LTGRAY);addView(explanation);gap(10);
        if(autoOwned){TextView auto=text(undo?"Also undo the automatic-cleanup switch only if later selections still match.":"Automatic boot cleanup will be enabled for the selected boot rules. Existing selections are kept.",13);auto.setTextColor(Color.LTGRAY);addView(auto);gap(8);}
        ScrollView scroll=new ScrollView(context);scroll.setVerticalScrollBarEnabled(false);
        LinearLayout list=new LinearLayout(context);list.setOrientation(VERTICAL);scroll.addView(list);
        addView(scroll,new LayoutParams(LayoutParams.MATCH_PARENT,0,1));
        List<TextView> focusRows=new ArrayList<>();
        TextView primary=button("",apply);primary.setTag("startup:defaults:apply");
        Runnable updateCount=()->{primary.setText(busy?"Working...":undo?"Undo BOOP defaults":"Apply selected ("+selected.size()+")");};
        updateCount.run();
        for(var row:rows) {
            String pkg=row.profile().packageName();boolean selectable=row.available()&&!undo;
            TextView item=button("",()->{});item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(dp(14),dp(10),dp(14),dp(10));item.setMinHeight(dp(78));
            Runnable update=()->item.setText((undo?"":row.available()?(selected.contains(pkg)?"\u2611  ":"\u2610  "):"\u2013  ")
                    +row.profile().label()+"\n"+pkg+"\n"
                    +(row.available()?(undo?"Restore the pre-defaults changes for this app.":row.profile().actions()):"Kept unchanged: "+row.reason())
                    +(undo?"":"\n"+row.profile().consequence()));
            update.run();item.setTag("startup:defaults:"+pkg);
            item.setOnClickListener(v->{if(!busy&&selectable){if(!selected.remove(pkg))selected.add(pkg);update.run();updateCount.run();}});
            if(!row.available())item.setTextColor(Color.LTGRAY);
            LayoutParams lp=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);lp.bottomMargin=dp(9);list.addView(item,lp);focusRows.add(item);
        }
        if(rows.isEmpty())list.addView(text(undo?"No package changes are pending. Check the automatic-cleanup note above.":"No matching apps were found.",17));
        gap(9);TextView progress=text(status==null?"":status,13);progress.setTextColor(Color.LTGRAY);progress.setMaxLines(2);addView(progress);
        gap(9);LinearLayout actions=new LinearLayout(context);actions.setOrientation(HORIZONTAL);
        TextView backButton=button(busy?"Cancel":"Back",back);
        primary.setOnClickListener(v->{if(!busy&&(undo||!selected.isEmpty()))apply.run();});
        actions.addView(backButton,new LayoutParams(0,LayoutParams.WRAP_CONTENT,1));
        LayoutParams primaryLp=new LayoutParams(0,LayoutParams.WRAP_CONTENT,1);primaryLp.leftMargin=dp(12);actions.addView(primary,primaryLp);
        if(keep!=null){TextView keepButton=button("Keep current settings",()->{if(!busy)keep.run();});LayoutParams lp=new LayoutParams(0,LayoutParams.WRAP_CONTENT,1);lp.leftMargin=dp(12);actions.addView(keepButton,lp);keepButton.setNextFocusLeftId(primary.getId());primary.setNextFocusRightId(keepButton.getId());keepButton.setNextFocusRightId(keepButton.getId());}
        else primary.setNextFocusRightId(primary.getId());
        backButton.setNextFocusLeftId(backButton.getId());backButton.setNextFocusRightId(primary.getId());primary.setNextFocusLeftId(backButton.getId());
        addView(actions);gap(7);addView(text("Up/Down: review apps     OK: select     Back: return without applying",11));
        for(int i=0;i<focusRows.size();i++) {
            TextView row=focusRows.get(i);row.setNextFocusUpId(i==0?row.getId():focusRows.get(i-1).getId());
            row.setNextFocusDownId(i+1<focusRows.size()?focusRows.get(i+1).getId():backButton.getId());
            row.setNextFocusLeftId(row.getId());row.setNextFocusRightId(row.getId());
        }
        if(!focusRows.isEmpty()){int last=focusRows.get(focusRows.size()-1).getId();primary.setNextFocusUpId(last);backButton.setNextFocusUpId(last);post(focusRows.get(0)::requestFocus);}
        else post(backButton::requestFocus);
    }
    private TextView text(String value,int sp){TextView v=new TextView(getContext());v.setText(value);v.setTextSize(sp);v.setTextColor(Color.WHITE);return v;}
    private TextView button(String label,Runnable click) {
        TextView v=text(label,16);v.setFocusable(true);v.setClickable(true);v.setId(View.generateViewId());v.setMinHeight(dp(52));
        v.setGravity(Gravity.CENTER);v.setPadding(dp(10),dp(8),dp(10),dp(8));
        v.setBackground(FocusChrome.filled(getContext(),Color.rgb(36,39,42),12,false));
        v.setOnFocusChangeListener((view,on)->view.setBackground(FocusChrome.filled(getContext(),Color.rgb(36,39,42),12,on)));
        v.setOnClickListener(view->click.run());
        v.setOnKeyListener((view,key,event)->event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()>0&&(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER));
        return v;
    }
    private void gap(int n){addView(new View(getContext()),new LayoutParams(1,dp(n)));}
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
}
