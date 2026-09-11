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

/** One reusable renderer: static placement, source-texture lid/gaze deformation. */
public final class CanonicalEyeRenderer implements GLSurfaceView.Renderer {
    public interface Failure {void report(String detail);}
    private final AssetManager assets;
    private final Failure failure;
    private final FloatBuffer positions=buffer(8), uv=buffer(8);
    private int program, positionLocation, uvLocation, poseLocation;
    private int[] textures=new int[2];
    private boolean ready;
    public volatile EyeMotion.Pose pose=EyeMotion.OPEN;
    public CanonicalEyeRenderer(AssetManager assets,Failure failure){
        this.assets=assets;this.failure=failure;
        uv.put(new float[]{0,1,1,1,0,0,1,0}).position(0);
    }
    private static FloatBuffer buffer(int n){return ByteBuffer.allocateDirect(n*4).order(ByteOrder.nativeOrder()).asFloatBuffer();}
    private String read(String name)throws Exception{
        try(InputStream input=assets.open(name);ByteArrayOutputStream bytes=new ByteArrayOutputStream()){
            byte[] block=new byte[4096];int n;while((n=input.read(block))>=0)bytes.write(block,0,n);
            return bytes.toString("UTF-8");
        }
    }
    private int shader(int type,String source){
        int shader=GLES20.glCreateShader(type);GLES20.glShaderSource(shader,source);GLES20.glCompileShader(shader);
        int[] result=new int[1];GLES20.glGetShaderiv(shader,GLES20.GL_COMPILE_STATUS,result,0);
        if(result[0]==0){String log=GLES20.glGetShaderInfoLog(shader);GLES20.glDeleteShader(shader);throw new IllegalStateException(log);}
        return shader;
    }
    @Override public void onSurfaceCreated(GL10 unused,EGLConfig config){
        ready=false;
        try{
            int vertex=shader(GLES20.GL_VERTEX_SHADER,read("eyes.vert"));
            int fragment=shader(GLES20.GL_FRAGMENT_SHADER,read("eyes.frag"));
            program=GLES20.glCreateProgram();GLES20.glAttachShader(program,vertex);GLES20.glAttachShader(program,fragment);GLES20.glLinkProgram(program);
            int[] result=new int[1];GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,result,0);
            GLES20.glDeleteShader(vertex);GLES20.glDeleteShader(fragment);
            if(result[0]==0)throw new IllegalStateException(GLES20.glGetProgramInfoLog(program));
            GLES20.glUseProgram(program);
            positionLocation=GLES20.glGetAttribLocation(program,"aPosition");uvLocation=GLES20.glGetAttribLocation(program,"aUv");poseLocation=GLES20.glGetUniformLocation(program,"uPose");
            GLES20.glGenTextures(2,textures,0);
            String[] names={"boopApprovedEyes.png","lid-rig.png"};
            for(int i=0;i<2;i++){
                BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;
                Bitmap bitmap;
                try(InputStream stream=assets.open(names[i])){bitmap=BitmapFactory.decodeStream(stream,null,options);}
                if(bitmap==null)throw new IllegalStateException("Missing texture "+names[i]);
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0+i);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[i]);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);bitmap.recycle();
            }
            GLES20.glUniform1i(GLES20.glGetUniformLocation(program,"uMaster"),0);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(program,"uRig"),1);
            GLES20.glEnable(GLES20.GL_BLEND);GLES20.glBlendFunc(GLES20.GL_ONE,GLES20.GL_ONE_MINUS_SRC_ALPHA);
            int error=GLES20.glGetError();if(error!=GLES20.GL_NO_ERROR)throw new IllegalStateException("GL setup "+error);
            ready=true;Log.i("BOOPEyes","renderer-ready GLES2 shared texture/state rig");
        }catch(Exception error){Log.e("BOOPEyes","renderer failed",error);failure.report(error.toString());}
    }
    @Override public void onSurfaceChanged(GL10 unused,int width,int height){
        GLES20.glViewport(0,0,width,height);
        float viewAspect=width/(float)Math.max(1,height);
        float x=Math.min(1f,2f/viewAspect)*0.94f,y=Math.min(1f,viewAspect/2f)*0.94f;
        positions.clear();positions.put(new float[]{-x,-y,x,-y,-x,y,x,y}).position(0);
    }
    @Override public void onDrawFrame(GL10 unused){
        GLES20.glClearColor(0,0,0,1);GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        if(!ready)return;
        EyeMotion.Pose current=pose;
        GLES20.glUseProgram(program);GLES20.glUniform4f(poseLocation,current.left,current.right,current.x,current.y);
        positions.position(0);uv.position(0);
        GLES20.glEnableVertexAttribArray(positionLocation);GLES20.glEnableVertexAttribArray(uvLocation);
        GLES20.glVertexAttribPointer(positionLocation,2,GLES20.GL_FLOAT,false,0,positions);
        GLES20.glVertexAttribPointer(uvLocation,2,GLES20.GL_FLOAT,false,0,uv);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
    }
}
