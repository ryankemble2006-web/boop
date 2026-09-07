package com.boop.launcher;

import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.util.*;

public final class AllAppsView extends FrameLayout {
 public interface Listener{void launch(AppEntry app);void pin(AppEntry app);void searchOpened();void searchClosed();}
 private final GridView grid;private final ImageButton searchButton;private final EditText search;private final Listener listener;private List<AppEntry> all=new ArrayList<>();private List<AppEntry> shown=new ArrayList<>();
 public AllAppsView(Context c,Listener listener){super(c);this.listener=listener;setBackgroundColor(Color.BLACK);setContentDescription("All apps");
  grid=new GridView(c);grid.setNumColumns(4);grid.setVerticalSpacing(dp(14));grid.setHorizontalSpacing(dp(8));grid.setPadding(dp(12),dp(52),dp(12),dp(24));grid.setSelector(new ColorDrawable(Color.TRANSPARENT));addView(grid,new LayoutParams(-1,-1));
  searchButton=new ImageButton(c);searchButton.setImageResource(android.R.drawable.ic_menu_search);searchButton.setBackgroundColor(Color.TRANSPARENT);searchButton.setColorFilter(Color.WHITE);searchButton.setContentDescription("Search apps");LayoutParams sp=new LayoutParams(dp(48),dp(48),Gravity.TOP|Gravity.END);sp.topMargin=dp(8);sp.rightMargin=dp(10);addView(searchButton,sp);searchButton.setOnClickListener(v->openSearch());
  search=new EditText(c);search.setSingleLine();search.setTextColor(Color.WHITE);search.setHintTextColor(0xffaaaaaa);search.setTextSize(20);search.setHint("Search apps");search.setBackgroundColor(0xff111111);search.setPadding(dp(18),0,dp(18),0);search.setVisibility(GONE);LayoutParams ep=new LayoutParams(-1,dp(52),Gravity.TOP);ep.leftMargin=dp(16);ep.rightMargin=dp(16);ep.topMargin=dp(8);addView(search,ep);
  search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){shown=AppSearch.filter(all,s.toString());refresh();}public void afterTextChanged(Editable e){}});
  grid.setOnItemClickListener((p,v,pos,id)->listener.launch(shown.get(pos)));grid.setOnItemLongClickListener((p,v,pos,id)->{listener.pin(shown.get(pos));v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);return true;});
 }
 private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
 public void submit(List<AppEntry> apps){all=new ArrayList<>(apps);shown=new ArrayList<>(apps);refresh();}
 public boolean isSearching(){return search.getVisibility()==VISIBLE;}
 public void closeSearch(){if(!isSearching())return;search.setText("");search.setVisibility(GONE);searchButton.setVisibility(VISIBLE);((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(),0);listener.searchClosed();}
 private void openSearch(){searchButton.setVisibility(GONE);search.setVisibility(VISIBLE);search.requestFocus();((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(search,InputMethodManager.SHOW_IMPLICIT);listener.searchOpened();}
 private void refresh(){grid.setAdapter(new BaseAdapter(){public int getCount(){return shown.size();}public Object getItem(int p){return shown.get(p);}public long getItemId(int p){return p;}public View getView(int p,View old,android.view.ViewGroup parent){AppEntry a=shown.get(p);LinearLayout box=old instanceof LinearLayout?(LinearLayout)old:new LinearLayout(getContext());box.removeAllViews();box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);box.setPadding(dp(4),dp(10),dp(4),dp(10));ImageView iv=new ImageView(getContext());iv.setImageDrawable(a.icon);box.addView(iv,new LinearLayout.LayoutParams(dp(58),dp(58)));TextView tv=new TextView(getContext());tv.setText(a.label);tv.setTextColor(Color.WHITE);tv.setTextSize(12);tv.setGravity(Gravity.CENTER);tv.setMaxLines(2);box.addView(tv,new LinearLayout.LayoutParams(-1,dp(38)));box.setContentDescription(a.label+". Hold to add to home");return box;}});}
}
