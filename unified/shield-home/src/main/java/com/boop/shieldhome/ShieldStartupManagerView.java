package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** TV-first presentation only. Package authority stays in the controller and bridge. */
public final class ShieldStartupManagerView extends LinearLayout {
    public interface Callbacks {
        void onOpenPackages(StartupManagerUiModel.Mode mode);
        void onOpenRestore(); void onCheckLocalLink(); void onRunNow();
        void onFilter(StartupManagerUiModel.Filter filter);
        void onPackageInfo(String packageName); void onPrimary(String packageName);
        void onToggleBoot(String packageName,boolean enabled);
        void onToggleBackground(String packageName,boolean enabled);
        void onForceStop(String packageName); void onProtected(String packageName,String reason);
        void onPackageFocus(String packageName,int actionIndex);
        void onToggleRestoreSelection(String packageName,boolean selected);
        void onRestoreOne(String packageName); void onRestoreSelected(); void onBack();
        default void onOpenOverview() { onBack(); }
        default void onSetAuto(boolean enabled) { }
        default void onRefresh() { }
        default void onBoopDefaults(boolean undo) { }
    }
    private static final int MUTED=Color.rgb(174,184,193), PANEL=Color.rgb(25,27,29), CARD=Color.rgb(36,39,42);
    private LinearLayout body,rail;
    private TextView detail;
    private final java.util.Map<String,String> packageLabels = new java.util.HashMap<>();
    public void setPackageLabels(List<StartupPackageState> rows) { for (var row:rows) packageLabels.put(row.packageName(),row.label()); }
    private boolean busy;
    private String status="";
    private final List<TextView> navigation=new ArrayList<>();
    public ShieldStartupManagerView(Context context) {
        super(context); setBackgroundColor(Color.BLACK);
        setPadding(dp(26),dp(22),dp(26),dp(20));
    }
    public void setStatus(String text,boolean working) { status=text==null?"":text; busy=working; }

    private void frame(String title,String subtitle,int selected,Callbacks callbacks) {
        removeAllViews(); setOrientation(HORIZONTAL); navigation.clear();
        rail=column(); rail.setPadding(dp(12),dp(14),dp(12),dp(12));
        rail.setBackground(FocusChrome.filled(getContext(),PANEL,16,false));
        addView(rail,new LayoutParams(dp(170),LayoutParams.MATCH_PARENT));
        TextView wordmark=text("BOOP",26,true); wordmark.setTextColor(FocusChrome.accentColor(getContext()));
        rail.addView(wordmark); rail.addView(caption("YOUR SHIELD. YOUR RULES.",10)); space(rail,22);
        String[] labels={"Overview","Packages","Boot cleanup","Background","Restore"};
        Runnable[] actions={callbacks::onOpenOverview,
            ()->callbacks.onOpenPackages(StartupManagerUiModel.Mode.DISABLE),
            ()->callbacks.onOpenPackages(StartupManagerUiModel.Mode.BOOT_CLEAN),
            ()->callbacks.onOpenPackages(StartupManagerUiModel.Mode.BACKGROUND),callbacks::onOpenRestore};
        for(int i=0;i<labels.length;i++) {
            TextView item=button(labels[i],"startup:nav:"+i,actions[i]); item.setTextSize(14);
            if(i==selected) item.setTextColor(FocusChrome.accentColor(getContext()));
            item.setSingleLine(true); item.setMinHeight(dp(46));
            rail.addView(item,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT)); space(rail,7);
            navigation.add(item);
        }
        rail.addView(new View(getContext()),new LayoutParams(1,0,1));
        rail.addView(caption("Core recovery stays protected.\nEverything else stays visible.",12));
        addView(new View(getContext()),new LayoutParams(dp(22),1));
        body=column(); addView(body,new LayoutParams(0,LayoutParams.MATCH_PARENT,1));
        body.addView(text(title,29,true)); body.addView(caption(subtitle,14)); space(body,14);
    }
    private void footer(Callbacks callbacks) {
        space(body,8);
        TextView result=caption((busy?"Working: ":"")+status,13);
        result.setMaxLines(2); result.setEllipsize(TextUtils.TruncateAt.END);
        result.setTextColor(busy?FocusChrome.accentColor(getContext()):MUTED);
        body.addView(result,new LayoutParams(LayoutParams.MATCH_PARENT,dp(36)));
        body.addView(caption("D-pad  Browse     OK  Choose     Back  Return     Menu  Restore",11));
        for(int i=0;i<navigation.size();i++) {
            final int index=i;
            navigation.get(i).setOnKeyListener((v,key,event)->{
                if(event.getAction()!=KeyEvent.ACTION_DOWN) return false;
                if(key==KeyEvent.KEYCODE_DPAD_UP){navigation.get(Math.max(0,index-1)).requestFocus();return true;}
                if(key==KeyEvent.KEYCODE_DPAD_DOWN){navigation.get(Math.min(navigation.size()-1,index+1)).requestFocus();return true;}
                return key==KeyEvent.KEYCODE_DPAD_LEFT;
            });
        }
    }
    public void renderOverview(boolean linkReady,boolean auto,int managedCount,String lastSummary,Callbacks callbacks) {
        frame("Startup Manager","Choose what starts, what stays, and what gets out of the way.",0,callbacks);
        // The new actions do not squeeze the existing dashboard at large text sizes.
        LinearLayout rootBody=body;
        ScrollView overviewScroll=new ScrollView(getContext());overviewScroll.setVerticalScrollBarEnabled(false);
        body=column();overviewScroll.addView(body);
        rootBody.addView(overviewScroll,new LayoutParams(LayoutParams.MATCH_PARENT,0,1));
        LinearLayout badges=row();
        badge(badges,"LOCAL CONTROL",linkReady?"Link saved":"Needs setup");
        badge(badges,"MANAGED PACKAGES",Integer.toString(managedCount));
        badge(badges,"RECOVERY","Core protected");
        body.addView(badges,new LayoutParams(LayoutParams.MATCH_PARENT,dp(56))); space(body,12);
        LinearLayout defaults=row();
        TextView useDefaults=button("Use BOOP defaults","startup:overview:defaults",()->callbacks.onBoopDefaults(false));
        TextView undoDefaults=button("Undo BOOP defaults","startup:overview:undo-defaults",()->callbacks.onBoopDefaults(true));
        useDefaults.setMinHeight(dp(52));undoDefaults.setMinHeight(dp(52));
        weightedContent(defaults,useDefaults);hspace(defaults,12);weightedContent(defaults,undoDefaults);
        body.addView(defaults,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));space(body,12);
        LinearLayout grid=column(); body.addView(grid,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        LinearLayout top=row(),bottom=row(); grid.addView(top,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        space(grid,12); grid.addView(bottom,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        TextView disable=hero("01   DISABLE APPS","Take back control","Turn off unwanted apps, launchers and companion packages.","startup:overview:disable",()->callbacks.onOpenPackages(StartupManagerUiModel.Mode.DISABLE));
        TextView boot=hero("02   CLEAN AFTER BOOT","A quieter startup","Close selected apps once. Later launches stay alone.","startup:overview:boot",()->callbacks.onOpenPackages(StartupManagerUiModel.Mode.BOOT_CLEAN));
        TextView background=hero("03   BACKGROUND START","Keep the extras quiet","Limit selected apps in the background. Open them when needed.","startup:overview:bg",()->callbacks.onOpenPackages(StartupManagerUiModel.Mode.BACKGROUND));
        TextView restore=hero("04   RESTORE CHANGES","Put it back","Return to the state saved before BOOP changed anything.","startup:overview:restore",callbacks::onOpenRestore);
        weightedContent(top,disable); hspace(top,12); weightedContent(top,boot);
        weightedContent(bottom,background); hspace(bottom,12); weightedContent(bottom,restore);
        space(body,12);
        LinearLayout utilities=row();
        TextView autoButton=button("After boot: "+(auto?"ON":"OFF"),"startup:overview:auto",()->callbacks.onSetAuto(!auto));
        TextView link=button("Check local link","startup:overview:link",callbacks::onCheckLocalLink);
        TextView back=button("Back","startup:overview:back",callbacks::onBack);
        weighted(utilities,autoButton); hspace(utilities,8); weighted(utilities,link); hspace(utilities,8); weighted(utilities,back);
        body.addView(utilities,new LayoutParams(LayoutParams.MATCH_PARENT,dp(44)));
        if(status.isEmpty()) status=lastSummary;
        body=rootBody;footer(callbacks);
        useDefaults.setNextFocusRightId(undoDefaults.getId());undoDefaults.setNextFocusLeftId(useDefaults.getId());
        useDefaults.setNextFocusDownId(disable.getId());undoDefaults.setNextFocusDownId(boot.getId());
        disable.setNextFocusUpId(useDefaults.getId());boot.setNextFocusUpId(undoDefaults.getId());
        useDefaults.setNextFocusUpId(useDefaults.getId());undoDefaults.setNextFocusUpId(undoDefaults.getId());
        disable.setNextFocusRightId(boot.getId()); disable.setNextFocusDownId(background.getId());
        boot.setNextFocusLeftId(disable.getId()); boot.setNextFocusDownId(restore.getId());
        background.setNextFocusRightId(restore.getId()); background.setNextFocusUpId(disable.getId());
        restore.setNextFocusLeftId(background.getId()); restore.setNextFocusUpId(boot.getId());
        disable.setNextFocusLeftId(navigation.get(0).getId());
        background.setNextFocusLeftId(navigation.get(0).getId());
        navigation.get(0).setNextFocusRightId(useDefaults.getId());
        useDefaults.setNextFocusLeftId(navigation.get(0).getId());
        post(useDefaults::requestFocus);
    }
    public void renderPackages(List<StartupPackageState> rows,StartupManagerUiModel.Filter filter,
            StartupManagerUiModel.Mode mode,StartupRecoveryPolicy.RecoveryCapabilities caps,
            String focusPackage,int focusAction,Callbacks callbacks) {
        int section=mode==StartupManagerUiModel.Mode.BOOT_CLEAN?2:mode==StartupManagerUiModel.Mode.BACKGROUND?3:1;
        frame("Package Control",modeSubtitle(mode)+"   \u2022   "+rows.size()+" shown",section,callbacks);
        LinearLayout chips=row(); List<TextView> filters=new ArrayList<>();
        for(StartupManagerUiModel.Filter option:StartupManagerUiModel.Filter.values()) {
            TextView chip=button(filterName(option),"startup:filter:"+option,()->callbacks.onFilter(option));
            chip.setGravity(Gravity.CENTER); chip.setTextSize(13);
            if(option==filter) chip.setTextColor(FocusChrome.accentColor(getContext()));
            weighted(chips,chip); hspace(chips,6); filters.add(chip);
        }
        body.addView(chips,new LayoutParams(LayoutParams.MATCH_PARENT,dp(42))); space(body,12);
        LinearLayout split=row(); split.setGravity(Gravity.TOP);
        ScrollView scroll=new ScrollView(getContext()); scroll.setFillViewport(true); scroll.setVerticalScrollBarEnabled(false);
        LinearLayout list=column(); scroll.addView(list);
        split.addView(scroll,new LayoutParams(0,LayoutParams.MATCH_PARENT,0.70f)); hspace(split,14);
        LinearLayout pane=column(); pane.setPadding(dp(16),dp(15),dp(16),dp(12));
        pane.setBackground(FocusChrome.filled(getContext(),PANEL,14,false));
        pane.addView(caption("SELECTED PACKAGE",11)); space(pane,12);
        detail=text("Choose a package to inspect its controls.",15,false);
        ScrollView detailsScroll=new ScrollView(getContext()); detailsScroll.setVerticalScrollBarEnabled(false);
        detailsScroll.addView(detail); pane.addView(detailsScroll,new LayoutParams(LayoutParams.MATCH_PARENT,0,1));
        space(pane,10); pane.addView(caption("App data is kept. Restore saves the original settings.",12));
        split.addView(pane,new LayoutParams(0,LayoutParams.MATCH_PARENT,0.30f));
        body.addView(split,new LayoutParams(LayoutParams.MATCH_PARENT,0,1));
        List<List<TextView>> matrix=new ArrayList<>(); List<String> ids=new ArrayList<>();
        for(StartupPackageState state:rows) {
            ids.add(state.packageName()); var risk=StartupRecoveryPolicy.assess(state,caps);
            boolean locked=risk.protectedPackage(); LinearLayout card=column();
            card.setPadding(dp(6),dp(4),dp(6),dp(6)); card.setBackground(FocusChrome.filled(getContext(),PANEL,12,false));
            TextView info=button(state.label()+"\n"+state.packageName(),tag(state,0),()->callbacks.onPackageInfo(state.packageName()));
            secondLine(info); info.setMaxLines(2); info.setEllipsize(TextUtils.TruncateAt.END);
            info.setMinHeight(dp(48));
            card.addView(info,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
            LinearLayout strip=row(); List<TextView> controls=new ArrayList<>(); controls.add(info);
            boolean boot=state.managedActions().contains(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN);
            boolean bg=state.managedActions().contains(StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK);
            String[] labels={StartupManagerUiModel.canUsePrimary(state,caps)?StartupManagerUiModel.primaryAction(state):"Protected","Boot: "+(boot?"ON":"OFF"),"Background", "Stop"};
            Runnable guard=()->callbacks.onProtected(state.packageName(),risk.protectionReason());
            Runnable[] actions={()->callbacks.onPrimary(state.packageName()),()->callbacks.onToggleBoot(state.packageName(),!boot),
                ()->callbacks.onToggleBackground(state.packageName(),!bg),()->callbacks.onForceStop(state.packageName())};
            for(int a=0;a<4;a++) {
                TextView control=button(labels[a],tag(state,a+1),(locked && (a!=0 || !StartupManagerUiModel.canUsePrimary(state,caps)))?guard:actions[a]);
                control.setGravity(Gravity.CENTER); control.setTextSize(13);
                if(a==2) control.setText("Background\n"+(bg?"ON":"OFF"));
                if(locked) control.setTextColor(MUTED);
                weighted(strip,control); if(a<3)hspace(strip,6); controls.add(control);
            }
            card.addView(strip,new LayoutParams(LayoutParams.MATCH_PARENT,Math.max(dp(48),textHeight(13,2,12))));
            for(int a=0;a<controls.size();a++) {
                final int action=a;
                controls.get(a).setOnFocusChangeListener((v,focused)->{
                    focus(v,focused);
                    if(focused) { callbacks.onPackageFocus(state.packageName(),action); showDetail(state,risk); }
                });
            }
            matrix.add(controls); list.addView(card,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT)); space(list,8);
        }
        if(rows.isEmpty()) list.addView(caption(busy?"Reading installed packages...":"No packages match this filter.",17));
        space(body,10); LinearLayout utilities=row();
        TextView back=button("Back","startup:packages:back",callbacks::onBack);
        TextView refresh=button("Refresh","startup:packages:refresh",callbacks::onRefresh);
        TextView run=button("Run boot cleanup","startup:packages:run",callbacks::onRunNow);
        weighted(utilities,back); hspace(utilities,8); weighted(utilities,refresh); hspace(utilities,8); weighted(utilities,run);
        body.addView(utilities,new LayoutParams(LayoutParams.MATCH_PARENT,dp(42)));
        footer(callbacks);
        wireMatrix(matrix,ids,filters,back,focusPackage,focusAction,StartupManagerNav.Screen.PACKAGES);
        for(int i=0;i<filters.size();i++) {
            TextView chip=filters.get(i);
            chip.setNextFocusLeftId(i==0?navigation.get(section).getId():filters.get(i-1).getId());
            chip.setNextFocusRightId(i==filters.size()-1?chip.getId():filters.get(i+1).getId());
            if(!matrix.isEmpty())chip.setNextFocusDownId(matrix.get(0).get(0).getId());
        }
        navigation.get(section).setNextFocusRightId(filters.get(filter.ordinal()).getId());
    }
    private void showDetail(StartupPackageState state,StartupRecoveryPolicy.Assessment risk) {
        detail.setText(state.label()+"\n\n"+state.packageName()+"\n\n"
                +(StartupManagerUiModel.isDisabled(state)?"DISABLED":"ENABLED")+"  \u2022  "+(state.systemApp()?"System app":"User app")
                +"\n"+(risk.protectedPackage()?"Protected for recovery":risk.impact()==StartupRecoveryPolicy.Impact.HIGH?"High impact":risk.impact()==StartupRecoveryPolicy.Impact.MEDIUM?"Changes a system feature":"User-controlled app")
                +"\n\n"+(risk.protectedPackage()?risk.protectionReason():state.launcher()?"Provides a Home screen. Disabling it changes the stock Home experience.":"Disable, close after boot, limit background start, or stop this app.")
                +"\n\nBOOP rules:\n"+actionSummary(state.managedActions()));
    }
    public void renderRestore(List<StartupRestoreRecord> records,Set<String> selected,String focusPackage,Callbacks callbacks) {
        frame("Restore changes","The original state is saved before BOOP changes anything.",4,callbacks);
        TextView batch=button("Restore selected  ("+selected.size()+")","startup:restore:batch",callbacks::onRestoreSelected);
        body.addView(batch,new LayoutParams(LayoutParams.MATCH_PARENT,dp(46))); space(body,12);
        ScrollView scroll=new ScrollView(getContext()); scroll.setVerticalScrollBarEnabled(false);
        LinearLayout list=column(); List<List<TextView>> matrix=new ArrayList<>(); List<String> ids=new ArrayList<>();
        for(StartupRestoreRecord record:records) {
            String pkg=record.packageName(); ids.add(pkg); LinearLayout card=row();
            String time=new SimpleDateFormat("dd MMM, HH:mm",Locale.getDefault()).format(new Date(record.firstChangedAtMillis()));
            TextView select=button((selected.contains(pkg)?"\u2713  ":"\u25cb  ")+packageLabels.getOrDefault(pkg,pkg)+"\n"+pkg+"  \u2022  "+time,
                    "startup:restore:"+pkg+":0",()->callbacks.onToggleRestoreSelection(pkg,!selected.contains(pkg)));
            secondLine(select); select.setMaxLines(2); select.setEllipsize(TextUtils.TruncateAt.END);
            TextView now=button("Restore","startup:restore:"+pkg+":1",()->callbacks.onRestoreOne(pkg));
            card.addView(select,new LayoutParams(0,dp(78),1)); hspace(card,10);
            card.addView(now,new LayoutParams(dp(114),dp(78))); matrix.add(List.of(select,now));
            list.addView(card,new LayoutParams(LayoutParams.MATCH_PARENT,dp(78))); space(list,10);
        }
        if(records.isEmpty())list.addView(caption("Nothing to undo. No BOOP package changes are saved.",18));
        scroll.addView(list); body.addView(scroll,new LayoutParams(LayoutParams.MATCH_PARENT,0,1)); space(body,10);
        TextView back=button("Back","startup:restore:back",callbacks::onBack);
        body.addView(back,new LayoutParams(LayoutParams.MATCH_PARENT,dp(42))); footer(callbacks);
        wireMatrix(matrix,ids,List.of(batch),back,focusPackage,0,StartupManagerNav.Screen.RESTORE);
        batch.setNextFocusLeftId(navigation.get(4).getId());
        if(!matrix.isEmpty())batch.setNextFocusDownId(matrix.get(0).get(0).getId());
        if(focusPackage==null)post(batch::requestFocus);
    }
    private void wireMatrix(List<List<TextView>> matrix,List<String> ids,List<TextView> header,TextView back,
            String focusPackage,int action,StartupManagerNav.Screen screen) {
        for(int r=0;r<matrix.size();r++)for(int c=0;c<matrix.get(r).size();c++) {
            final int rr=r,cc=c;
            matrix.get(r).get(c).setOnKeyListener((v,key,event)->{
                if(event.getAction()!=KeyEvent.ACTION_DOWN)return false;
                if((key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER)&&event.getRepeatCount()>0)return true;
                StartupManagerNav.Key direction=switch(key) {
                    case KeyEvent.KEYCODE_DPAD_UP->StartupManagerNav.Key.UP;
                    case KeyEvent.KEYCODE_DPAD_DOWN->StartupManagerNav.Key.DOWN;
                    case KeyEvent.KEYCODE_DPAD_LEFT->StartupManagerNav.Key.LEFT;
                    case KeyEvent.KEYCODE_DPAD_RIGHT->StartupManagerNav.Key.RIGHT;
                    default->null;
                };
                if(direction==null)return false;
                if(direction==StartupManagerNav.Key.UP&&rr==0){header.get(0).requestFocus();return true;}
                if(direction==StartupManagerNav.Key.DOWN&&rr==matrix.size()-1){back.requestFocus();return true;}
                var state=new StartupManagerNav.State(screen,0,ids.get(rr),cc,ids);
                var next=StartupManagerNav.key(state,direction,matrix.get(rr).size()).state();
                matrix.get(ids.indexOf(next.focusedPackageId())).get(next.actionIndex()).requestFocus(); return true;
            });
        }
        if(!matrix.isEmpty()) {
            int row=Math.max(0,ids.indexOf(focusPackage)); TextView target=matrix.get(row).get(Math.min(Math.max(0,action),matrix.get(row).size()-1));
            back.setNextFocusUpId(matrix.get(matrix.size()-1).get(0).getId()); post(target::requestFocus);
        } else post(back::requestFocus);
    }
    private TextView hero(String eyebrow,String title,String subtitle,String tag,Runnable action) {
        TextView v=button(eyebrow+"\n"+title+"\n\n"+subtitle,tag,action); v.setTextSize(17); v.setPadding(dp(18),dp(13),dp(18),dp(13));
        SpannableString text=new SpannableString(v.getText()); int split=text.toString().indexOf('\n');
        text.setSpan(new RelativeSizeSpan(0.67f),0,split,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(FocusChrome.accentColor(getContext())),0,split,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int sub=text.toString().indexOf("\n\n")+2;
        text.setSpan(new RelativeSizeSpan(0.78f),sub,text.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(MUTED),sub,text.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        v.setText(text); return v;
    }
    private void badge(LinearLayout row,String title,String value) {
        LinearLayout box=column(); box.setPadding(dp(13),dp(5),dp(8),dp(5));
        box.setBackground(FocusChrome.filled(getContext(),PANEL,10,false));
        box.addView(caption(title,10)); box.addView(text(value,18,true)); weighted(row,box); hspace(row,8);
    }
    private TextView button(String label,String tag,Runnable action) {
        TextView v=text(label,15,false); v.setGravity(Gravity.CENTER_VERTICAL); v.setPadding(dp(10),dp(5),dp(10),dp(5));
        v.setFocusable(true); v.setClickable(true); v.setId(View.generateViewId()); v.setTag(tag);
        v.setContentDescription(label); v.setBackground(FocusChrome.filled(getContext(),CARD,10,false));
        v.setOnFocusChangeListener(this::focus); v.setOnClickListener(ignored->action.run()); return v;
    }
    private void secondLine(TextView view) {
        SpannableString s=new SpannableString(view.getText()); int split=s.toString().indexOf('\n');
        if(split>=0){s.setSpan(new RelativeSizeSpan(0.72f),split+1,s.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new ForegroundColorSpan(MUTED),split+1,s.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);view.setText(s);}
    }
    private void focus(View view,boolean focused) { view.setBackground(FocusChrome.filled(getContext(),focused?Color.rgb(42,47,51):CARD,10,focused)); }
    private TextView text(String value,int size,boolean bold) {
        TextView v=new TextView(getContext()); v.setText(value); v.setTextSize(size); v.setTextColor(Color.WHITE);
        if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return v;
    }
    private TextView caption(String value,int size) { TextView v=text(value,size,false); v.setTextColor(MUTED); return v; }
    private String tag(StartupPackageState s,int a){return "startup:package:"+s.packageName()+":"+a;}
    private String filterName(StartupManagerUiModel.Filter f){return switch(f){case ALL->"All";case LAUNCHERS->"Launchers";case ADS->"Ads / recs";case SYSTEM->"System";case USER->"User";case DISABLED->"Disabled";};}
    private String modeSubtitle(StartupManagerUiModel.Mode mode){return switch(mode){case DISABLE->"Installed apps, including hidden system packages";case BOOT_CLEAN->"Close chosen packages once after startup";case BACKGROUND->"Limit background start, without disabling the app";};}
    private String actionSummary(Set<StartupRecoveryPolicy.ManagedAction> actions){
        if(actions==null||actions.isEmpty())return "No active BOOP rules";
        return actions.stream().map(a->switch(a){case DISABLED->"Disabled by BOOP";case BOOT_CLEAN->"Close after boot";case BACKGROUND_BLOCK->"Background limited";}).sorted().reduce((a,b)->a+" \u2022 "+b).orElse("");
    }
    private LinearLayout column(){LinearLayout v=new LinearLayout(getContext());v.setOrientation(VERTICAL);return v;}
    private LinearLayout row(){LinearLayout v=new LinearLayout(getContext());v.setOrientation(HORIZONTAL);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
    private void weighted(LinearLayout row,View v){row.addView(v,new LayoutParams(0,LayoutParams.MATCH_PARENT,1));}
    // Scrolling rows have no fixed height: measure their buttons/cards from content.
    private void weightedContent(LinearLayout row,View v){row.addView(v,new LayoutParams(0,LayoutParams.WRAP_CONTENT,1));}
    private void space(LinearLayout parent,int size){parent.addView(new View(getContext()),new LayoutParams(1,dp(size)));}
    private void hspace(LinearLayout parent,int size){parent.addView(new View(getContext()),new LayoutParams(dp(size),1));}
    private int textHeight(int sp,int lines,int paddingDp) {
        android.graphics.Paint paint=new android.graphics.Paint();
        paint.setTextSize(sp*getResources().getDisplayMetrics().scaledDensity);
        android.graphics.Paint.FontMetricsInt fm=paint.getFontMetricsInt();
        return (fm.bottom-fm.top)*lines+dp(paddingDp);
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
}
