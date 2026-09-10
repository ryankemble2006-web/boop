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
    private final Bitmap[] thumbs=new Bitmap[2];
    private final float[] mesh=new float[9*17*2];
    private SignMotion.Pose pose=SignMotion.sample(0,0);
    private int style;
    private boolean freddie;
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
        for(int side=0;side<2;side++)thumbs[side]=sampleFinger(new float[]{696,425,769,282,174},side==1);
    }
    public void show(SignMotion.Pose pose,int style){this.pose=pose;this.style=Math.floorMod(style,4);freddie=false;invalidate();}
    public void showFreddie(SignMotion.Pose pose){this.pose=pose;freddie=true;invalidate();}
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
        Canvas stripCanvas=new Canvas(strip);
        stripCanvas.drawBitmap(hands,map,paint);
        // Only the root fades: retain the original felt detail and fingertip silhouette.
        Paint feather=new Paint(Paint.ANTI_ALIAS_FLAG);
        feather.setShader(new LinearGradient(0,205,0,256,Color.WHITE,Color.TRANSPARENT,Shader.TileMode.CLAMP));
        feather.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        stripCanvas.drawRect(0,0,128,256,feather);
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
    private void handBridge(Canvas c,boolean left){
        // Original palm material wraps around the board edge, beneath the finger roots.
        // The inward feather joins the depths while the outer side hides the arrow tip.
        float centre=left?-291:326;
        int layer=c.saveLayer(centre-48,-79,centre+48,79,null);
        rearHand(c,left);
        paint.setShader(new LinearGradient(left?-308:327,0,left?-283:351,0,
                left?Color.WHITE:Color.TRANSPARENT,left?Color.TRANSPARENT:Color.WHITE,Shader.TileMode.CLAMP));
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        c.drawRect(centre-48,-79,centre+48,79,paint);
        paint.setXfermode(null);paint.setShader(null);c.restoreToCount(layer);
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
        if(freddie){drawFreddie(c);c.restore();return;}
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
        handBridge(c,true);handBridge(c,false);
        frontFingers(c,true);frontFingers(c,false);
        c.restore();
    }
    private void micHand(Canvas c,boolean left,float y,boolean front){
        c.save();c.translate(0,y);c.scale(.82f,.82f);
        if(!front){
            c.save();c.translate(left?292:-335,0);rearHand(c,left);c.restore();
            // The fifth digit opposes the four front fingers, behind the shaft.
            c.save();c.translate(left?26:-26,9);c.rotate(left?23:-23);
            paint.setShader(null);paint.setColor(Color.WHITE);
            c.drawBitmap(thumbs[left?0:1],null,new RectF(-16,-32,16,38),paint);c.restore();
            c.restore();return;
        }
        c.translate(left?292:-335,0);
        handBridge(c,left);frontFingers(c,left);
        c.restore();
    }
    private void moustache(Canvas c){
        c.save();c.translate(pose.sway*.3f,405+pose.bob*.25f);c.rotate(pose.angle*.12f);
        c.scale(1,pose.lift);
        for(int side=0;side<2;side++){
            c.save();if(side==1)c.scale(-1,1);
            Path p=new Path();p.moveTo(0,1);p.cubicTo(30,-25,56,-21,83,0);
            p.cubicTo(110,20,132,13,150,2);p.cubicTo(128,47,85,48,53,36);
            p.cubicTo(26,31,8,17,0,1);p.close();
            paint.setShader(new LinearGradient(0,-20,0,48,0xff51483f,0xff080808,Shader.TileMode.CLAMP));c.drawPath(p,paint);
            paint.setShader(null);c.save();c.clipPath(p);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(.85f);
            for(int i=0;i<52;i++){
                float x=i*3;Path hair=new Path();hair.moveTo(x,-12+(i%7));
                hair.cubicTo(x+11,3,x+7,25,x+32,40);
                paint.setColor(i%3==0?0xff605249:0xff292520);c.drawPath(hair,paint);
            }
            paint.setStyle(Paint.Style.FILL);c.restore();c.restore();
        }
        c.restore();
    }
    private void drawFreddie(Canvas c){
        moustache(c);
        c.save();c.translate(-160+pose.sway,455+120*(1-pose.lift)+pose.bob);
        c.rotate(-54+pose.angle);
        micHand(c,true,0,false);micHand(c,false,235,false);
        paint.setShader(new LinearGradient(-10,0,10,0,new int[]{0xff343d44,0xfff1f5f7,0xff77838c,0xff202a32},null,Shader.TileMode.CLAMP));
        c.drawRoundRect(new RectF(-10,-50,10,330),8,8,paint);paint.setShader(null);
        paint.setColor(0xff0b1015);c.drawRoundRect(new RectF(-17,-95,17,54),12,12,paint);
        paint.setColor(0xff76818b);c.drawRect(-16,-90,16,-80,paint);
        RectF head=new RectF(-35,-162,35,-87);
        paint.setShader(new RadialGradient(-12,-142,64,new int[]{0xfff0ede5,0xff9c9c97,0xff252c31},null,Shader.TileMode.CLAMP));
        c.drawOval(head,paint);paint.setShader(null);
        c.save();Path meshHead=new Path();meshHead.addOval(head,Path.Direction.CW);c.clipPath(meshHead);
        paint.setStrokeWidth(1.1f);paint.setColor(0xff202a31);
        for(int i=-72;i<90;i+=6){c.drawLine(-40,-162+i,40,-82+i,paint);c.drawLine(-40,-82-i,40,-162-i,paint);}
        c.restore();paint.setColor(0xffc0c4c3);c.drawRoundRect(new RectF(-32,-110,32,-103),3,3,paint);
        micHand(c,true,0,true);micHand(c,false,235,true);
        c.restore();
    }
}
