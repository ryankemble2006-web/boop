package com.boop.eyes;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.io.InputStream;

/** Local demo prop. Independent original hand layers and a vector sign, no notification access. */
public final class NotificationSignView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final Bitmap hands;
    private final Rect leftSource=new Rect(0,0,887,887),rightSource=new Rect(887,0,1774,887);
    private SignMotion.Pose pose=SignMotion.sample(0,0);
    private int style;
    private static final String[] NAMES={"WHATSAPP","GMAIL","FACEBOOK","X"};
    private static final String[] WORDS={"MESSAGE!","MAIL'S HERE!","OVER HERE!","SOMETHING NEW!"};
    private static final int[] COLOURS={0xff16a66c,0xffd84a40,0xff2875df,0xff323b52};
    public NotificationSignView(Context context){
        super(context);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        try(InputStream in=context.getAssets().open("boop-notification-hands.png")){
            BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;
            hands=BitmapFactory.decodeStream(in,null,options);
            if(hands==null)throw new IllegalStateException("Missing approved hands");
        }catch(Exception e){throw new IllegalStateException("Notification hand asset unavailable",e);}
    }
    public void show(SignMotion.Pose pose,int style){this.pose=pose;this.style=Math.floorMod(style,4);invalidate();}
    private void text(Canvas c,String value,float x,float y,float size,int colour){
        paint.setShader(null);paint.setColor(colour);paint.setTextSize(size);paint.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));paint.setTextAlign(Paint.Align.CENTER);c.drawText(value,x,y,paint);
    }
    private void hand(Canvas c,boolean left){
        c.save();c.translate(left?-290:290,22);c.rotate(left?-12-pose.wrist:12+pose.wrist);
        paint.setShader(null);paint.setColor(Color.WHITE);
        c.drawBitmap(hands,left?leftSource:rightSource,new RectF(-121,-121,121,121),paint);c.restore();
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);float scale=Math.min(getWidth()/1000f,getHeight()/680f);
        c.save();c.translate(getWidth()/2f,(getHeight()-680*scale)/2f);c.scale(scale,scale);
        c.translate(pose.sway,510+250*(1-pose.lift)+pose.bob);c.rotate(pose.angle);
        // The prop and wrists share the same parent transform: grips cannot drift.
        Path arrow=new Path();arrow.moveTo(-292,-86);arrow.lineTo(229,-86);arrow.lineTo(229,-124);arrow.lineTo(357,0);arrow.lineTo(229,124);arrow.lineTo(229,86);arrow.lineTo(-292,86);arrow.close();
        c.save();c.translate(0,9);paint.setColor(0xff071018);c.drawPath(arrow,paint);c.restore();
        paint.setShader(new LinearGradient(0,-100,0,110,0xffffffff,0xffdce9eb,Shader.TileMode.CLAMP));c.drawPath(arrow,paint);paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(7);paint.setColor(COLOURS[style]);c.drawPath(arrow,paint);paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOURS[style]);c.drawRoundRect(new RectF(-188,-57,-83,57),20,20,paint);
        text(c,style==0?"W":style==1?"M":style==2?"f":"X",-135,24,76,Color.WHITE);
        text(c,NAMES[style],70,-22,25,COLOURS[style]);
        text(c,WORDS[style],70,27,style==3?24:31,0xff13202d);
        // Both full five-digit source hands remain separate, unrepainted layers.
        hand(c,true);hand(c,false);
        c.restore();
    }
}
