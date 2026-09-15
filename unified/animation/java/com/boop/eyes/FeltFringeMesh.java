package com.boop.eyes;

import java.util.Arrays;

/** Sparse, stable ribbons rooted in the approved upper-lid alpha contour.
 * Built once per GL context; no bitmap edits, per-frame allocation or random clock.
 * Vertices contain centre x/y, pixel-width offset x/y, neutral light, alpha and edge.
 */
public final class FeltFringeMesh {
    private FeltFringeMesh() {}
    public static float[] create(int[] pixels,int width,int height,int[] rig,int rigWidth,int rigHeight){
        if(width<3||height<3||pixels==null||pixels.length!=width*height
                ||rigWidth<1||rigHeight<1||rig==null||rig.length!=rigWidth*rigHeight)
            throw new IllegalArgumentException("Complete master and rig required");
        int[] top=new int[width];
        Arrays.fill(top,-1);
        for(int x=0;x<width;x++){
            int rx=Math.min(rigWidth-1,(int)((x+0.5)*rigWidth/width));
            int encoded=rig[rx]; // First rig row is the approved open-lid lower edge.
            float edge=(((encoded>>>16)&255)*256+((encoded>>>8)&255))/32f;
            for(int y=0;y<height&&y<edge-8;y++){
                if((pixels[y*width+x]>>>24)>=128){top[x]=y;break;}
            }
        }
        Builder out=new Builder();
        float travelled=0,spacing=6;
        for(int x=3;x<width-3;x++){
            if(top[x]<0||top[x-1]<0||top[x-3]<0||top[x+3]<0){
                travelled=0;continue;
            }
            float step=top[x]-top[x-1];
            if(Math.abs(step)>32){travelled=0;continue;}
            travelled+=(float)Math.sqrt(1+step*step);
            if(travelled<spacing)continue;
            travelled=0;
            spacing=5+5*random(x,19);
            float slope=(top[x+3]-top[x-3])/6f;
            float inv=1f/(float)Math.sqrt(1+slope*slope);
            float tx=inv,ty=slope*inv,nx=ty,ny=-tx;
            float length=10+13*random(x,41);
            float bend=(random(x,73)-.5f)*7;
            float curl=(random(x,101)-.5f)*3;
            float thickness=2.2f+random(x,131); // Soft ribbon width in screen pixels.
            // Neutral stage light catches upward-facing fibres most strongly.
            float light=(.42f+.16f*random(x,157))*(.68f+.32f*Math.max(0,-ny));
            float opacity=.60f+.18f*random(x,179);
            float[] cx=new float[5],cy=new float[5],sx=new float[5],sy=new float[5],alpha=new float[5];
            for(int k=0;k<=4;k++){
                float t=k/4f;
                float tangent=bend*t*t+curl*(float)Math.sin(Math.PI*t);
                cx[k]=x-nx*5f+nx*length*t+tx*tangent;
                cy[k]=top[x]-ny*5f+ny*length*t+ty*tangent;
                alpha[k]=opacity*(float)Math.sqrt(1-t);
            }
            ribbon(out,cx,cy,sx,sy,alpha,thickness,light,width,height);
            // Loose curved wisps lie on the lit cloth, not just beyond its edge.
            if(random(x,211)>.25f){
                float depth=10+60*random(x,227);
                float ax=x-nx*depth,ay=top[x]-ny*depth;
                float span=12+22*random(x,241),bow=(random(x,257)-.5f)*12;
                float wispLight=.30f+.18f*random(x,271);
                float wispAlpha=.44f+.18f*random(x,283);
                for(int k=0;k<=4;k++){
                    float t=k/4f,along=(t-.5f)*span;
                    float arc=bow*(float)Math.sin(Math.PI*t);
                    cx[k]=ax+tx*along+nx*arc;
                    cy[k]=ay+ty*along+ny*arc;
                    alpha[k]=wispAlpha*(float)Math.sin(Math.PI*t);
                }
                ribbon(out,cx,cy,sx,sy,alpha,1.9f+.6f*random(x,307),wispLight,width,height);
            }
        }
        return Arrays.copyOf(out.data,out.size);
    }
    private static void ribbon(Builder out,float[] cx,float[] cy,float[] sx,float[] sy,float[] alpha,
                               float thickness,float light,int width,int height){
        for(int k=0;k<=4;k++){
            int before=Math.max(0,k-1),after=Math.min(4,k+1);
            float dx=cx[after]-cx[before],dy=cy[after]-cy[before];
            float scale=thickness*(1-.6f*k/4f)*.5f/(float)Math.sqrt(dx*dx+dy*dy);
            sx[k]=-dy*scale;sy[k]=dx*scale;
        }
        for(int k=0;k<4;k++){
            vertex(out,cx,cy,sx,sy,alpha,k,-1,light,width,height);
            vertex(out,cx,cy,sx,sy,alpha,k,1,light,width,height);
            vertex(out,cx,cy,sx,sy,alpha,k+1,-1,light,width,height);
            vertex(out,cx,cy,sx,sy,alpha,k+1,-1,light,width,height);
            vertex(out,cx,cy,sx,sy,alpha,k,1,light,width,height);
            vertex(out,cx,cy,sx,sy,alpha,k+1,1,light,width,height);
        }
    }
    private static void vertex(Builder out,float[] x,float[] y,float[] sx,float[] sy,float[] alpha,
                               int k,int side,float light,int width,int height){
        out.add(2*x[k]/width-1);
        out.add(1-2*y[k]/height);
        out.add(2*side*sx[k]/width);out.add(-2*side*sy[k]/height);
        out.add(light);out.add(alpha[k]);out.add(side);
    }
    private static float random(int x,int salt){
        int value=x*0x45d9f3b+salt*0x119de1f3;
        value^=value>>>16;value*=0x45d9f3b;value^=value>>>16;
        return (value&0xffff)/65535f;
    }
    private static final class Builder {
        float[] data=new float[8192];int size;
        void add(float value){
            if(size==data.length)data=Arrays.copyOf(data,data.length*2);
            data[size++]=value;
        }
    }
}
