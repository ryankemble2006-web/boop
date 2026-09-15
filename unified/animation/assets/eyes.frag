precision highp float;
uniform sampler2D uMaster;
uniform sampler2D uCloth;
uniform sampler2D uRig;
uniform vec4 uPose;
uniform float uHueRadians;
uniform vec3 uFeltTint;
uniform float uFeltAmount;
varying vec2 vUv;
vec3 hueRotate(vec3 c,float a){
    float co=cos(a),si=sin(a);
    return clamp(vec3(
        c.r*(0.213+co*0.787-si*0.213)+c.g*(0.715-co*0.715-si*0.715)+c.b*(0.072-co*0.072+si*0.928),
        c.r*(0.213-co*0.213+si*0.143)+c.g*(0.715+co*0.285+si*0.140)+c.b*(0.072-co*0.072-si*0.283),
        c.r*(0.213-co*0.213-si*0.787)+c.g*(0.715-co*0.715+si*0.715)+c.b*(0.072+co*0.928+si*0.072)),0.0,1.0);
}

vec3 fabric(vec3 photo){
 float light=dot(photo,vec3(.213,.715,.072));
 float shade=clamp(1.9*pow(max(light,0.0),.45),.025,.95);
 return mix(photo,uFeltTint*shade,uFeltAmount);
}
void main(){
 // Preserve the hero's aspect with padding; never sample the smaller study variants.
 vec2 p=vUv*vec2(1536.0,768.0)-vec2(0.0,64.0);
 if(p.y<0.0||p.y>=640.0){gl_FragColor=vec4(0.0);return;}
 vec4 rig=texture2D(uRig,p/vec2(1536.0,640.0));
 float alpha=rig.a;
 if(alpha<.00001){gl_FragColor=vec4(0.0);return;}
 float edge=(rig.r*65280.0+rig.g*255.0)/32.0;
 bool left=p.x<768.0;
 vec2 centre=left?vec2(475.0,437.0):vec2(1070.0,437.0);
 float support=1.0-smoothstep(.60,1.0,length((p-centre)/vec2(265.0,230.0)));
 support*=smoothstep(edge+10.0,edge+65.0,p.y);
 vec2 source=p-uPose.zw*vec2(32.0,22.0)*support;
 vec3 rgb=texture2D(uMaster,source/vec2(1536.0,1024.0)).rgb;
 float iris=1.0-smoothstep(.91,1.04,length((source-centre)/vec2(181.0)));
 rgb=mix(rgb,hueRotate(rgb,uHueRadians),iris);
 float openLid=1.0-smoothstep(-2.0,2.0,p.y-edge);
 rgb=mix(rgb,fabric(texture2D(uMaster,p/vec2(1536.0,1024.0)).rgb),openLid);
 float closure=clamp(left?uPose.x:uPose.y,0.0,1.0);
 if(closure>0.000001){
  float end=mix(edge,642.0,closure);
  float cover=smoothstep(-2.0,2.0,end-p.y)*smoothstep(edge-18.0,edge-4.0,p.y)*smoothstep(0.0,.03,closure);
  // Sample only interior cloth, never the generated reference's backdrop.
  // Fixed destination coordinates keep individual wear marks stable while closing.
  float localX=clamp((p.x-(left?220.0:815.0))/510.0,0.0,1.0);
  float clothX=left?mix(.22,.40,localX):mix(.63,.80,localX);
  float clothY=mix(.48,.72,clamp((p.y-180.0)/460.0,0.0,1.0));
  vec3 cloth=texture2D(uCloth,vec2(clothX,clothY)).rgb;
  rgb=mix(rgb,fabric(cloth),cover);
 }
 gl_FragColor=vec4(rgb*alpha,alpha);
}
