package com.boop.eyes;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/** Existing pose/clock surface, now driven by photographic PNG layers. */
public final class CanonicalEyeRenderer implements GLSurfaceView.Renderer {
 public interface Failure {void report(String detail);}
 private final AssetManager assets;private final Failure failure;
 private final FloatBuffer positions=buffer(8),uv=buffer(8);
 private int program,positionLocation,uvLocation,poseLocation,hueLocation,feltLocation,feltAmount;
 private final int[] textures=new int[2];
 private boolean ready;
 public volatile EyeMotion.Pose pose=EyeMotion.OPEN;
 private volatile float hueRotationRadians;
 private volatile float[] feltTint=FeltPalette.tint(0);
 public CanonicalEyeRenderer(AssetManager assets,Failure failure){
  this.assets=assets;this.failure=failure;uv.put(new float[]{0,1,1,1,0,0,1,0}).position(0);
 }
 public void setHueRotationDegrees(float degrees){hueRotationRadians=(float)Math.toRadians(degrees);}
 public void setFeltColour(int value){feltTint=FeltPalette.tint(value);}
 private static FloatBuffer buffer(int n){return ByteBuffer.allocateDirect(n*4).order(ByteOrder.nativeOrder()).asFloatBuffer();}
 private String read(String name)throws Exception{
  try(InputStream in=assets.open(name);ByteArrayOutputStream out=new ByteArrayOutputStream()){
   byte[] data=new byte[4096];int n;while((n=in.read(data))>=0)out.write(data,0,n);return out.toString("UTF-8");
  }
 }
 private int shader(int type,String code){
  int shader=GLES20.glCreateShader(type);GLES20.glShaderSource(shader,code);GLES20.glCompileShader(shader);
  int[] ok=new int[1];GLES20.glGetShaderiv(shader,GLES20.GL_COMPILE_STATUS,ok,0);
  if(ok[0]==0){String log=GLES20.glGetShaderInfoLog(shader);GLES20.glDeleteShader(shader);throw new IllegalStateException(log);}return shader;
 }
 private void bindTexture(int slot){
  GLES20.glActiveTexture(GLES20.GL_TEXTURE0+slot);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[slot]);
  GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
  GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
  GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
  GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
 }
 @Override public void onSurfaceCreated(GL10 unused,EGLConfig config){
  ready=false;
  try{
   int vs=shader(GLES20.GL_VERTEX_SHADER,read("eyes.vert")),fs=shader(GLES20.GL_FRAGMENT_SHADER,read("eyes.frag"));
   program=GLES20.glCreateProgram();GLES20.glAttachShader(program,vs);GLES20.glAttachShader(program,fs);GLES20.glLinkProgram(program);
   int[] ok=new int[1];GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,ok,0);
   GLES20.glDeleteShader(vs);GLES20.glDeleteShader(fs);
   if(ok[0]==0)throw new IllegalStateException(GLES20.glGetProgramInfoLog(program));
   GLES20.glUseProgram(program);
   positionLocation=GLES20.glGetAttribLocation(program,"aPosition");uvLocation=GLES20.glGetAttribLocation(program,"aUv");
   poseLocation=GLES20.glGetUniformLocation(program,"uPose");hueLocation=GLES20.glGetUniformLocation(program,"uHueRadians");
   feltLocation=GLES20.glGetUniformLocation(program,"uFeltTint");feltAmount=GLES20.glGetUniformLocation(program,"uFeltAmount");
   GLES20.glGenTextures(2,textures,0);
   byte[] rig=null;
   String[] names={"boop-png-study.png"};
   for(int i=0;i<names.length;i++){
    BitmapFactory.Options opts=new BitmapFactory.Options();opts.inScaled=false;
    Bitmap bitmap;try(InputStream in=assets.open(names[i])){bitmap=BitmapFactory.decodeStream(in,null,opts);}
    if(bitmap==null||bitmap.getWidth()!=1536||bitmap.getHeight()!=1024)throw new IllegalStateException("PNG dimensions: "+names[i]);
    if(i==0){
     int[] pixels=new int[1536*640];bitmap.getPixels(pixels,0,1536,0,0,1536,640);
     rig=PngPuppetRig.create(pixels,1536,640);
    }
    bindTexture(i);GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);bitmap.recycle();
   }
   bindTexture(1);
   ByteBuffer data=ByteBuffer.allocateDirect(rig.length);data.put(rig).position(0);
   // Encoded geometry must be uploaded raw; Bitmap premultiplication corrupts RG at fuzzy edges.
   GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_RGBA,1536,640,0,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,data);
   GLES20.glUniform1i(GLES20.glGetUniformLocation(program,"uMaster"),0);
   GLES20.glUniform1i(GLES20.glGetUniformLocation(program,"uRig"),1);
   GLES20.glEnable(GLES20.GL_BLEND);GLES20.glBlendFunc(GLES20.GL_ONE,GLES20.GL_ONE_MINUS_SRC_ALPHA);
   int error=GLES20.glGetError();if(error!=GLES20.GL_NO_ERROR)throw new IllegalStateException("GL setup "+error);
   ready=true;Log.i("BOOPEyes","renderer-ready PNG photographic puppet");
  }catch(Exception e){Log.e("BOOPEyes","renderer failed",e);failure.report(e.toString());}
 }
 @Override public void onSurfaceChanged(GL10 unused,int width,int height){
  GLES20.glViewport(0,0,width,height);
  float viewAspect=width/(float)Math.max(1,height);
  float x=Math.min(1f,2f/viewAspect)*.94f,y=Math.min(1f,viewAspect/2f)*.94f;
  positions.clear();positions.put(new float[]{-x,-y,x,-y,-x,y,x,y}).position(0);
 }
 @Override public void onDrawFrame(GL10 unused){
  GLES20.glClearColor(0,0,0,0);GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);if(!ready)return;
  EyeMotion.Pose current=pose;float[] tint=feltTint;
  GLES20.glUseProgram(program);
  GLES20.glUniform4f(poseLocation,current.left,current.right,current.x,current.y);
  GLES20.glUniform1f(hueLocation,hueRotationRadians);
  GLES20.glUniform3f(feltLocation,tint[0],tint[1],tint[2]);GLES20.glUniform1f(feltAmount,tint[3]);
  positions.position(0);uv.position(0);
  GLES20.glEnableVertexAttribArray(positionLocation);GLES20.glEnableVertexAttribArray(uvLocation);
  GLES20.glVertexAttribPointer(positionLocation,2,GLES20.GL_FLOAT,false,0,positions);
  GLES20.glVertexAttribPointer(uvLocation,2,GLES20.GL_FLOAT,false,0,uv);
  GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
 }
}
