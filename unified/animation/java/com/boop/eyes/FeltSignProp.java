package com.boop.eyes;
import android.content.Context;
import android.graphics.*;
import java.io.InputStream;
/** One connected photographed prop. Text changes; hand anatomy and grip never drift. */
final class FeltSignProp {
 private Bitmap sign;
 private final Bitmap[] thumbs;
 private final BitmapShader letterFelt;
 private final Paint art=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
 private final Paint lettering=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
 private final RectF destination=new RectF(-400,-200,400,200);
 private String notificationLabel;
 private static final int[] COLOURS={0xff13754e,0xffae322d,0xff2057a9,0xff323b52};
 FeltSignProp(Context context,Bitmap[] thumbs){
  this.thumbs=thumbs.clone();
  try(InputStream in=context.getAssets().open("boop-felt-sign-blank.png")){
   BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;
   sign=BitmapFactory.decodeStream(in,null,options);
   if(sign==null||sign.getWidth()!=1774||sign.getHeight()!=887)throw new IllegalStateException("Felt sign geometry");
  }catch(Exception e){throw new IllegalStateException("Felt sign unavailable",e);}
  // The blank board supplies actual felt texture inside the runtime letter shapes.
  Bitmap cloth=Bitmap.createBitmap(sign,650,300,400,250);
  letterFelt=new BitmapShader(cloth,Shader.TileMode.MIRROR,Shader.TileMode.MIRROR);
  Matrix materialScale=new Matrix();materialScale.setScale(.45f,.45f);letterFelt.setLocalMatrix(materialScale);
  lettering.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));
  lettering.setTextAlign(Paint.Align.CENTER);
 }
 Bitmap artwork(){return sign;}
 void setNotificationLabel(String label){notificationLabel=label;}
 void setArtwork(Bitmap image,Bitmap[] colouredThumbs){
  sign=image;
  System.arraycopy(colouredThumbs,0,thumbs,0,2);
 }
 void draw(Canvas canvas,int style){
  // A thumb still exists on each hand: its original felt bitmap is rendered at
  // rear depth and is occluded by the board. The four front fingers and palm
  // share one photographic silhouette instead of being stretched as loose strips.
  for(int side=0;side<2;side++){
   for(FeltSignRig.Digit digit:FeltSignRig.Digit.values()){
    if(!FeltSignRig.behindSign(digit))continue;
    float[] b=FeltSignRig.thumbBounds(side==0);
    canvas.drawBitmap(thumbs[side],null,new RectF(b[0],b[1],b[2],b[3]),art);
   }
  }
  canvas.drawBitmap(sign,null,destination,art);
  int colour=COLOURS[Math.floorMod(style,4)];
  lettering.setShader(null);lettering.setColorFilter(null);lettering.setColor(colour);
  String heading=notificationLabel==null?FeltSignRig.name(style):notificationLabel.toUpperCase(java.util.Locale.ROOT);
  lettering.setTextSize(26);
  float headingWidth=lettering.measureText(heading);
  if(headingWidth>460)lettering.setTextSize(26*460/headingWidth);
  canvas.drawText(heading,0,-30,lettering);
  String words=notificationLabel==null?FeltSignRig.words(style):"NOTIFICATION";
  lettering.setTextSize(49);
  float width=lettering.measureText(words);
  if(width>460)lettering.setTextSize(49*460/width);
  // Slightly raised dark felt letters retain the board's real material variation.
  lettering.setColor(0x66432f1e);canvas.drawText(words,1,32,lettering);
  lettering.setColor(Color.WHITE);lettering.setShader(letterFelt);
  float r=.8f*.213f,g=.8f*.715f,b=.8f*.072f;
  lettering.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{
   r,g,b,0,-164, r,g,b,0,-164, r,g,b,0,-164, 0,0,0,1,0})));
  canvas.drawText(words,0,30,lettering);
  lettering.setShader(null);lettering.setColorFilter(null);
 }
}
