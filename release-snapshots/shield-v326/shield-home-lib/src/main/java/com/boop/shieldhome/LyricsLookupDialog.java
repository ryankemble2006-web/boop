package com.boop.shieldhome;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.function.Consumer;

/** Edits search metadata only. Does not own or control the media session. */
final class LyricsLookupDialog extends Dialog {
    private final Activity activity;
    private final NowPlayingSnapshot original;
    private final String message;
    private final Consumer<NowPlayingSnapshot> search;
    private EditText title, artist;

    LyricsLookupDialog(Activity activity, NowPlayingSnapshot track, String message,
            Consumer<NowPlayingSnapshot> search) {
        super(activity); this.activity=activity; this.original=track;
        this.message=message; this.search=search;
    }
    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved); requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout panel=new LinearLayout(getContext()); panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(24),dp(18),dp(24),dp(18));
        panel.addView(label("Lyrics lookup",25),wrap());
        TextView help=label(message.isEmpty() ? "Edit the title or artist, then Search." : message+" Edit the title or artist and try again.",16);
        help.setTextColor(Color.LTGRAY); panel.addView(help,wrap());
        panel.addView(label("Track title",16),wrap()); title=field(original.title()); panel.addView(title,wrap());
        panel.addView(label("Artist",16),wrap()); artist=field(original.subtitle()); panel.addView(artist,wrap());
        title.setImeOptions(EditorInfo.IME_ACTION_NEXT); artist.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        title.setNextFocusDownId(artist.getId()); artist.setNextFocusUpId(title.getId());
        artist.setOnEditorActionListener((v,action,event)->{
            if(action==EditorInfo.IME_ACTION_SEARCH){submit();return true;} return false;
        });
        LinearLayout actions=new LinearLayout(getContext()); actions.setGravity(Gravity.END);
        TextView cancel=button("Cancel",v->dismiss()); TextView find=button("Search",v->submit());
        actions.addView(cancel,new LinearLayout.LayoutParams(dp(112),dp(44)));
        LinearLayout.LayoutParams searchParams=new LinearLayout.LayoutParams(dp(112),dp(44));searchParams.leftMargin=dp(12);
        actions.addView(find,searchParams); LinearLayout.LayoutParams actionParams=wrap();actionParams.topMargin=dp(16);
        panel.addView(actions,actionParams);
        artist.setNextFocusDownId(find.getId());find.setNextFocusUpId(artist.getId());cancel.setNextFocusUpId(artist.getId());
        cancel.setNextFocusRightId(find.getId());find.setNextFocusLeftId(cancel.getId());
        title.setOnKeyListener((v,key,event)->{
            if(event.getAction()==android.view.KeyEvent.ACTION_DOWN && key==android.view.KeyEvent.KEYCODE_DPAD_DOWN){
                hideKeyboard();artist.requestFocus();return true;
            }
            return false;
        });
        artist.setOnKeyListener((v,key,event)->{
            if(event.getAction()!=android.view.KeyEvent.ACTION_DOWN)return false;
            if(key==android.view.KeyEvent.KEYCODE_DPAD_DOWN){hideKeyboard();find.requestFocus();return true;}
            if(key==android.view.KeyEvent.KEYCODE_DPAD_UP){hideKeyboard();title.requestFocus();return true;}
            return false;
        });
        ScrollView scroll=new ScrollView(getContext());scroll.setFillViewport(true);scroll.addView(panel);setContentView(scroll);
        setCanceledOnTouchOutside(true);
        Window window=getWindow();
        if(window!=null){
            GradientDrawable surface=new GradientDrawable();surface.setColor(Color.rgb(24,24,24));surface.setCornerRadius(dp(14));
            window.setBackgroundDrawable(surface);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);window.setDimAmount(.65f);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
        title.requestFocus();title.setSelection(title.length());
    }
    @Override protected void onStart(){
        super.onStart(); Window window=getWindow();
        if(window!=null){window.setLayout(Math.min(dp(620),activity.getResources().getDisplayMetrics().widthPixels-dp(48)),
                Math.min(dp(390),activity.getResources().getDisplayMetrics().heightPixels-dp(48)));}
    }
    private void submit(){
        String t=title.getText().toString().trim(),a=artist.getText().toString().trim();
        if(t.isEmpty()){title.setError("Enter a track title");title.requestFocus();return;}
        if(a.isEmpty()){artist.setError("Enter an artist");artist.requestFocus();return;}
        NowPlayingSnapshot query=LyricsLookupQuery.edit(original,t,a);
        hideKeyboard();dismiss(); search.accept(query);
    }
    private void hideKeyboard(){
        android.view.inputmethod.InputMethodManager keyboard=(android.view.inputmethod.InputMethodManager)
                getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if(keyboard!=null)keyboard.hideSoftInputFromWindow(title.getWindowToken(),0);
    }
    private EditText field(String value){
        EditText field=new EditText(getContext());field.setId(View.generateViewId());
        field.setSingleLine(true);field.setTextColor(Color.WHITE);field.setTextSize(19);
        field.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        field.setFilters(new InputFilter[]{new InputFilter.LengthFilter(256)});field.setText(value);
        field.setPadding(dp(10),dp(6),dp(10),dp(6));return field;
    }
    private TextView button(String text,View.OnClickListener action){
        TextView view=label(text,18);view.setId(View.generateViewId());view.setGravity(Gravity.CENTER);
        view.setFocusable(true);view.setClickable(true);view.setOnClickListener(action);
        view.setBackground(FocusChrome.filled(getContext(),Color.rgb(38,38,38),8,false));
        view.setOnFocusChangeListener((v,focused)->v.setBackground(FocusChrome.filled(getContext(),Color.rgb(38,38,38),8,focused)));
        return view;
    }
    private TextView label(String text,int size){TextView v=new TextView(getContext());v.setText(text);v.setTextSize(size);v.setTextColor(Color.WHITE);return v;}
    private LinearLayout.LayoutParams wrap(){return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);}
    private int dp(int value){return Math.round(value*getContext().getResources().getDisplayMetrics().density);}
}
