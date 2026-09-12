package com.boop.rally;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.io.*;

public final class MainActivity extends Activity {
    private GameSpec importing;
    private boolean busy;
    private TextView status;
    private final java.util.concurrent.ExecutorService files=java.util.concurrent.Executors.newSingleThreadExecutor();
    @Override public void onCreate(Bundle state){
        super.onCreate(state);TvUi.immersive(this);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER);root.setPadding(40,24,40,24);root.setBackgroundColor(TvUi.INK);
        root.addView(TvUi.text(this,"BOOP RALLY",34,TvUi.CYAN));
        TextView subtitle=TvUi.text(this,"THE NETWORK Q COLLECTION",17,0xffbccbd7);root.addView(subtitle);
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);root.addView(row,new LinearLayout.LayoutParams(-1,TvUi.dp(this,190)));
        Button first=null;
        for(GameSpec game:GameSpec.ALL){
            Button b=TvUi.button(this,game.year+"\n\n"+game.title,v->launch(game));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);lp.setMargins(12,20,12,20);row.addView(b,lp);if(first==null)first=b;
        }
        status=TvUi.text(this,"Choose your rally. A controller is recommended for racing.",16,Color.WHITE);root.addView(status);
        LinearLayout bottom=new LinearLayout(this);bottom.setGravity(Gravity.CENTER);root.addView(bottom);
        add(bottom,TvUi.button(this,"Controls",v->showControls()));
        add(bottom,TvUi.button(this,"Import game pack",v->chooseImport()));
        add(bottom,TvUi.button(this,"About",v->new AlertDialog.Builder(this).setTitle("BOOP Rally 1.0").setMessage("An independent offline launcher for your existing Network Q games.\n\nDOSBox Pure 1.0-preview6 and this host are GPL-2.0-or-later. Game files and trademarks remain with their owners and are not included in the APK.\n\nSource: github.com/ryankemble2006-web/boop, branch boop-rally-shield.\n\nBack returns to the collection without changing your Shield launcher.").setPositiveButton("Done",null).show()));
        setContentView(root);if(first!=null)first.requestFocus();
        if(state==null) handleLaunchIntent(getIntent());
    }
    private void add(LinearLayout row,Button b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(8,16,8,0);row.addView(b,p);}
    private File gameFile(GameSpec game){File dir=getExternalFilesDir("games");return dir==null?null:new File(dir,game.id+".zip");}
    @Override public void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);handleLaunchIntent(intent);}
    private void handleLaunchIntent(Intent intent){GameSpec game=GameSpec.find(intent.getStringExtra("game"));if(game!=null)launch(game);}
    private void launch(GameSpec game){
        if(busy)return; File pack=gameFile(game);
        if(pack==null || !pack.isFile()){new AlertDialog.Builder(this).setTitle("Game files are not here yet").setMessage("Import your prepared "+game.id+".zip pack, or use the supplied desktop installer. Your original PC folders stay untouched.").setPositiveButton("Import pack",(d,w)->beginImport(game)).setNegativeButton("Back",null).show();return;}
        busy=true;status.setText("Checking "+game.title+"...");
        files.execute(()->{try{GameBundle.validate(pack,game);runOnUiThread(()->{busy=false;status.setText("Choose your rally. A controller is recommended for racing.");if(!isFinishing())startActivity(new Intent(this,GameActivity.class).putExtra("game",game.id));});}
        catch(Exception e){runOnUiThread(()->{busy=false;status.setText("Game pack needs attention");problem(e.getMessage());});}});
    }
    private void problem(String text){if(!isFinishing())new AlertDialog.Builder(this).setTitle("Cannot open this game yet").setMessage(text).setPositiveButton("Back",null).show();}
    private void showControls(){new AlertDialog.Builder(this).setTitle("Rally controls").setMessage("MENUS\nD-pad or left stick: move\nA / remote OK: select\nB: go back inside the DOS game\n\nRACING\nLeft stick / D-pad: steering and arrow keys\nRight trigger: accelerate\nLeft trigger: brake\nX: Space     Y: F1\nL1: Z     R1: A\n\nBack / Start: pause and collection menu\nA USB/Bluetooth keyboard also works. The game's own controls/options remain available.").setPositiveButton("Ready",null).show();}
    private void chooseImport(){if(busy)return;new AlertDialog.Builder(this).setTitle("Which game pack?").setItems(new String[]{"Network Q RAC Rally (rac93.zip)","Rally Championship (rac96.zip)"},(d,n)->beginImport(GameSpec.ALL[n])).setNegativeButton("Back",null).show();}
    private void beginImport(GameSpec game){
        importing=game;
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE);
        try{startActivityForResult(i,10);}catch(ActivityNotFoundException e){problem("This Shield has no file picker. Use the supplied desktop installer to copy the game packs.");}
    }
    @Override public void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request!=10 || result!=RESULT_OK || data==null || data.getData()==null || importing==null)return;
        final GameSpec game=importing;final android.net.Uri uri=data.getData();busy=true;status.setText("Importing "+game.title+"...");
        files.execute(()->{try(InputStream in=getContentResolver().openInputStream(uri)){if(in==null)throw new IOException("This file cannot be opened.");File target=gameFile(game);if(target==null)throw new IOException("Game storage is not available.");GameBundle.importPack(in,target,game);runOnUiThread(()->{busy=false;status.setText(game.title+" is ready.");});}catch(Exception e){runOnUiThread(()->{busy=false;problem(e.getMessage());status.setText("Import did not finish. Your existing pack is unchanged.");});}});
    }
    @Override public void onResume(){super.onResume();TvUi.immersive(this);}
    @Override public void onDestroy(){files.shutdown();super.onDestroy();}
}
