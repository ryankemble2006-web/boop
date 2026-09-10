package com.boop.eyes;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.io.InputStream;

/** Local demo prop. Independent original hand layers and a vector sign, no notification access. */
public final class NotificationSignView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final Bitmap hands;
    private final Bitmap[][] fingers=new Bitmap[2][4];
    private final float[] mesh=new float[9*17*2];
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
        // Root, cap centre and width of the four non-thumb digits in the approved art.
        float[][] digits={{515,367,515,147,205},{401,414,234,259,188},
                {317,527,139,474,173},{313,637,169,682,143}};
        for(int side=0;side<2;side++)for(int digit=0;digit<4;digit++)
            fingers[side][digit]=sampleFinger(digits[digit],side==1);
    }
    public void show(SignMotion.Pose pose,int style){this.pose=pose;this.style=Math.floorMod(style,4);invalidate();}
    private void text(Canvas c,String value,float x,float y,float size,int colour){
        paint.setShader(null);paint.setColor(colour);paint.setTextSize(size);paint.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));paint.setTextAlign(Paint.Align.CENTER);c.drawText(value,x,y,paint);
    }
    private Bitmap sampleFinger(float[] digit,boolean mirror){
        float rx=digit[0],ry=digit[1],tx=digit[2],ty=digit[3],width=digit[4];
        float dx=tx-rx,dy=ty-ry,len=(float)Math.hypot(dx,dy);
        dx/=len;dy/=len;
        float nx=-dy*width/2,ny=dx*width/2;
        float ex=tx+dx*width*.53f,ey=ty+dy*width*.53f;
        float[] source={ex-nx,ey-ny,ex+nx,ey+ny,rx-nx,ry-ny};
        if(mirror)for(int i=0;i<source.length;i+=2)source[i]=1774-source[i];
        Matrix map=new Matrix();map.setPolyToPoly(source,0,new float[]{0,0,128,0,0,256},0,3);
        Bitmap strip=Bitmap.createBitmap(128,256,Bitmap.Config.ARGB_8888);
        new Canvas(strip).drawBitmap(hands,map,paint);
        return strip;
    }
    private void rearHand(Canvas c,boolean left){
        c.save();c.translate(left?-291:326,0);
        // Palm and thumb stay at the rear depth. The board occludes their inward half.
        Path silhouette=new Path();silhouette.addOval(new RectF(-47,-78,47,78),Path.Direction.CW);
        c.clipPath(silhouette);paint.setShader(null);paint.setColor(Color.WHITE);
        Rect source=left?new Rect(310,380,760,770):new Rect(1014,380,1464,770);
        c.drawBitmap(hands,source,new RectF(-47,-78,47,78),paint);c.restore();
    }
    private void frontFingers(Canvas c,boolean left){
        float[] lengths={69,80,76,61};
        float direction=left?1:-1;
        for(int digit=0;digit<4;digit++){
            // Closed grip: neighbouring fingers meet instead of fanning apart.
            float y=-42+digit*28;
            // One knuckle line keeps the right fingers closed over the sloping arrow.
            float edge=left?-292:335;
            float root=edge-direction*16;
            float curl=.85f+.15f*pose.lift;
            float reach=lengths[digit]*(1-.32f*curl);
            int at=0;
            for(int row=0;row<=16;row++){
                float t=1-row/16f; // Source cap -> fingertip; source root -> board edge.
                float bend=(float)Math.sin(t*Math.PI);
                float x=root+direction*(reach*t+7*bend);
                float centre=y+5*curl*bend;
                float thickness=(digit==3?35:38)*(1+.12f*bend);
                for(int col=0;col<=8;col++){
                    mesh[at++]=x;
                    mesh[at++]=centre+(col/8f-.5f)*thickness;
                }
            }
            paint.setShader(null);paint.setColor(Color.WHITE);
            c.drawBitmapMesh(fingers[left?0:1][digit],8,16,mesh,0,null,0,paint);
        }
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);float scale=Math.min(getWidth()/1000f,getHeight()/680f);
        c.save();c.translate(getWidth()/2f,(getHeight()-680*scale)/2f);c.scale(scale,scale);
        c.translate(pose.sway,510+250*(1-pose.lift)+pose.bob);c.rotate(pose.angle);
        // The prop and wrists share the same parent transform: grips cannot drift.
        rearHand(c,true);rearHand(c,false);
        Path arrow=new Path();arrow.moveTo(-292,-86);arrow.lineTo(229,-86);arrow.lineTo(229,-124);arrow.lineTo(357,0);arrow.lineTo(229,124);arrow.lineTo(229,86);arrow.lineTo(-292,86);arrow.close();
        c.save();c.translate(0,9);paint.setColor(0xff071018);c.drawPath(arrow,paint);c.restore();
        paint.setShader(new LinearGradient(0,-100,0,110,0xffffffff,0xffdce9eb,Shader.TileMode.CLAMP));c.drawPath(arrow,paint);paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(7);paint.setColor(COLOURS[style]);c.drawPath(arrow,paint);paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOURS[style]);c.drawRoundRect(new RectF(-188,-57,-83,57),20,20,paint);
        text(c,style==0?"W":style==1?"M":style==2?"f":"X",-135,24,76,Color.WHITE);
        text(c,NAMES[style],70,-22,25,COLOURS[style]);
        text(c,WORDS[style],70,27,style==3?24:31,0xff13202d);
        // Four individually sampled, foreshortened digits wrap across the front surface.
        frontFingers(c,true);frontFingers(c,false);
        c.restore();
    }
}
