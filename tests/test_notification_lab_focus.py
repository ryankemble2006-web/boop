"""Focus presentation and the sender's read-only accent-sharing boundary."""
from pathlib import Path
import subprocess
import xml.etree.ElementTree as ET
import pytest

ROOT=Path(__file__).resolve().parents[1]
ANDROID='{http://schemas.android.com/apk/res/android}'
PERMISSION='com.boop.shieldoverlay.permission.READ_HOME_ACCENT'
AUTHORITY='com.boop.shieldoverlay.home_accent'

def test_accent_provider_is_signature_protected_and_not_grantable():
    shield=ET.parse(ROOT/'split/shield/AndroidManifest.xml').getroot()
    permission=next((p for p in shield.findall('permission') if p.get(ANDROID+'name')==PERMISSION),None)
    assert permission is not None, 'Shield must declare its signature-only accent permission'
    assert permission.get(ANDROID+'protectionLevel')=='signature'
    provider=next(p for p in shield.findall('./application/provider') if p.get(ANDROID+'authorities')==AUTHORITY)
    assert provider.get(ANDROID+'permission')==PERMISSION
    assert provider.get(ANDROID+'grantUriPermissions')=='false'
    sender=ET.parse(ROOT/'notification-lab/src/main/AndroidManifest.xml').getroot()
    assert PERMISSION in [p.get(ANDROID+'name') for p in sender.findall('uses-permission')]
    assert AUTHORITY in [p.get(ANDROID+'authorities') for p in sender.findall('./queries/provider')]

STUBS={
'android/R.java':'package android; public class R {public static class attr {public static final int state_focused=1,state_pressed=2;}}',
'android/graphics/Color.java':'package android.graphics; public class Color {public static final int WHITE=-1;public static int rgb(int r,int g,int b){return 0xff000000|(r<<16)|(g<<8)|b;}}',
'android/graphics/drawable/GradientDrawable.java':'''package android.graphics.drawable; public class GradientDrawable {public int color,width,stroke; public void setColor(int c){color=c;}public void setCornerRadius(float r){}public void setStroke(int w,int c){width=w;stroke=c;}}''',
'android/graphics/drawable/StateListDrawable.java':'''package android.graphics.drawable; public class StateListDrawable {public final java.util.Map<Integer,GradientDrawable> states=new java.util.HashMap<>();public void addState(int[] s,GradientDrawable d){states.put(s.length==0?0:s[0],d);}}''',
'android/content/Context.java':'''package android.content; public class Context {public final ContentResolver resolver=new ContentResolver();public ContentResolver getContentResolver(){return resolver;}public android.content.res.Resources getResources(){return new android.content.res.Resources();}}''',
'android/content/res/Resources.java':'package android.content.res; public class Resources {public android.util.DisplayMetrics getDisplayMetrics(){return new android.util.DisplayMetrics();}}',
'android/util/DisplayMetrics.java':'package android.util; public class DisplayMetrics {public float density=2;}',
'android/content/ContentResolver.java':'''package android.content; public class ContentResolver {public android.database.Cursor response;public boolean denied;public android.database.Cursor query(android.net.Uri u,String[] p,String s,String[] a,String o){if(denied)throw new SecurityException();return response;}}''',
'android/net/Uri.java':'''package android.net; public class Uri {private String value;private Uri(String v){value=v;}public static Uri parse(String v){return new Uri(v);}public String getAuthority(){return java.net.URI.create(value).getAuthority();}public String getPath(){return java.net.URI.create(value).getPath();}public boolean equals(Object o){return o instanceof Uri&&value.equals(((Uri)o).value);}public String toString(){return value;}}''',
'android/content/ContentProvider.java':'''package android.content; public abstract class ContentProvider {public Context context=new Context();public Context getContext(){return context;}public abstract boolean onCreate();public abstract android.database.Cursor query(android.net.Uri u,String[] p,String s,String[] a,String o);public abstract String getType(android.net.Uri u);public abstract android.net.Uri insert(android.net.Uri u,ContentValues v);public abstract int delete(android.net.Uri u,String s,String[] a);public abstract int update(android.net.Uri u,ContentValues v,String s,String[] a);}''',
'android/content/ContentValues.java':'package android.content;public class ContentValues {}',
'android/database/Cursor.java':'package android.database;public interface Cursor extends AutoCloseable {boolean moveToFirst();int getColumnIndex(String s);int getInt(int i);void close();}',
'android/database/MatrixCursor.java':'''package android.database;public class MatrixCursor implements Cursor {public String[] columns;public Object[] row;public MatrixCursor(String[] c){columns=c;}public void addRow(Object[] r){row=r;}public boolean moveToFirst(){return row!=null;}public int getColumnIndex(String n){for(int i=0;i<columns.length;i++)if(columns[i].equals(n))return i;return -1;}public int getInt(int i){return (Integer)row[i];}public void close(){}}''',
'android/widget/TextView.java':'''package android.widget;public class TextView {public Object background,tint=new Object(),animator=new Object();public int left=11,top=12,right=13,bottom=14,color;public float size=19;public boolean highlight=true;public android.content.Context getContext(){return new android.content.Context();}public int getPaddingLeft(){return left;}public int getPaddingTop(){return top;}public int getPaddingRight(){return right;}public int getPaddingBottom(){return bottom;}public void setPadding(int l,int t,int r,int b){left=l;top=t;right=r;bottom=b;}public void setTextColor(int c){color=c;}public void setBackground(Object b){background=b;setPadding(0,0,0,0);}public void setBackgroundTintList(Object t){tint=t;}public void setStateListAnimator(Object a){animator=a;}public void setDefaultFocusHighlightEnabled(boolean b){highlight=b;}}''',
'com/boop/shieldhome/BoopTvChrome.java':'''package com.boop.shieldhome;public class BoopTvChrome {public static int accent=0xffff8844;public static int accentColor(android.content.Context c){return accent;}}''',
}

PROBE='''package com.boop.alpha1;import android.content.*;import android.database.*;import android.net.*;import android.widget.*;import android.graphics.drawable.*;
public class LabFocusProbe {static void check(boolean v,String why){if(!v)throw new AssertionError(why);}public static void main(String[] args){
 TextView view=new TextView();BoopLabFocus.apply(view,0xffbc55ee);StateListDrawable bg=(StateListDrawable)view.background;
 check(bg.states.get(android.R.attr.state_focused).stroke==0xffbc55ee,"custom focus hue lost");check(bg.states.get(android.R.attr.state_focused).width==8,"4dp border must scale with density");
 check(bg.states.get(android.R.attr.state_focused).color==0xff222222,"focused fill must stay dark");check(view.color==-1&&!view.highlight&&view.tint==null,"native focus styling survived");
 check(view.left==11&&view.top==12&&view.right==13&&view.bottom==14&&view.size==19,"focus styling changed geometry or text size");
 Context context=new Context();check(BoopLabAccent.fromShieldHome(context)==0xff4db8ff,"missing provider must use Home cyan");
 BoopHomeAccentProvider provider=new BoopHomeAccentProvider();Uri uri=Uri.parse("content://com.boop.shieldoverlay.home_accent/accent");
 MatrixCursor result=(MatrixCursor)provider.query(uri,null,null,null,null);check(result.columns.length==1&&result.columns[0].equals("accent"),"provider exposes more than accent");
 context.resolver.response=result;check(BoopLabAccent.fromShieldHome(context)==0xffff8844,"sender did not read actual Home selection");
 context.resolver.denied=true;check(BoopLabAccent.fromShieldHome(context)==0xff4db8ff,"signature denial must fall back");
 boolean rejected=false;try{provider.query(Uri.parse("content://com.boop.shieldoverlay.home_accent/preferences"),null,null,null,null);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"unexpected provider path accepted");
 int refused=0;try{provider.insert(uri,new ContentValues());}catch(UnsupportedOperationException expected){refused++;}try{provider.update(uri,new ContentValues(),null,null);}catch(UnsupportedOperationException expected){refused++;}try{provider.delete(uri,null,null);}catch(UnsupportedOperationException expected){refused++;}check(refused==3,"provider permits mutation");
}}'''

def test_focus_geometry_and_accent_provider(tmp_path):
    for name,code in STUBS.items():
        p=tmp_path/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(code,encoding='utf-8')
    probe=tmp_path/'LabFocusProbe.java';probe.write_text(PROBE,encoding='utf-8')
    sources=[ROOT/'source'/name for name in ('BoopLabFocus.java','BoopLabAccent.java','BoopHomeAccentProvider.java')]
    subprocess.run(['javac','-encoding','UTF-8','-d',str(tmp_path),*map(str,tmp_path.rglob('*.java')),*map(str,sources)],check=True)
    subprocess.run(['java','-cp',str(tmp_path),'com.boop.alpha1.LabFocusProbe'],check=True)
