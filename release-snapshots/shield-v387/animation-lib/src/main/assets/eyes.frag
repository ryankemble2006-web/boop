precision highp float;
uniform sampler2D uMaster;
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
// Soft oval lower edge stays above the hero crop and outside the photographed
// eye. The existing cap and its transparency both stretch to this same contour.
float lidClosedY(float x,float centre){
 float dx=(x-centre)/320.0;
 return 410.0+226.0*sqrt(max(0.0,1.0-dx*dx));
}
// Stretch the existing cap from its crown to its moving lower lip.
float lidSampleY(float y,float top,float edge,float end){
 return min(edge-2.0, top+(y-top)*(edge-top)/max(end-top,1.0));
}
// RGB and silhouette use the same moving photographic sample. Blend their
// premultiplied contributions so neither the old cutout nor dark backdrop leaks.
vec4 puppetComposite(vec3 baseRgb,float baseAlpha,vec3 lidRgb,float lidAlpha,float cover){
 return vec4(mix(baseRgb*baseAlpha,lidRgb*lidAlpha,cover),mix(baseAlpha,lidAlpha,cover));
}
void main(){
 // Preserve the hero's aspect with padding; never sample the smaller study variants.
 vec2 p=vUv*vec2(1536.0,768.0)-vec2(0.0,64.0);
 if(p.y<0.0||p.y>=640.0){gl_FragColor=vec4(0.0);return;}
 vec4 rig=texture2D(uRig,p/vec2(1536.0,640.0));
 float alpha=rig.a;
 float edge=(rig.r*65280.0+rig.g*255.0)/32.0;
 bool left=p.x<768.0;
 vec2 centre=left?vec2(503.0,415.0):vec2(1030.0,415.0);
 float support=1.0-smoothstep(.60,1.0,length((p-centre)/vec2(265.0,230.0)));
 support*=smoothstep(edge+10.0,edge+65.0,p.y);
 vec2 source=p-uPose.zw*vec2(32.0,22.0)*support;
 vec3 rgb=texture2D(uMaster,source/vec2(1536.0,1024.0)).rgb;
 float iris=1.0-smoothstep(1.0,1.08,length((source-centre)/vec2(174.0)));
 rgb=mix(rgb,hueRotate(rgb,uHueRadians),iris);
 float closure=clamp(left?uPose.x:uPose.y,0.0,1.0);
 float lidCentre=left?470.0:1066.0;
 float end=mix(edge,edge<640.0?lidClosedY(p.x,lidCentre):642.0,closure);
 float top=min(rig.b*640.0,edge-3.0);
 float sampleY=closure>0.000001?lidSampleY(p.y,top,edge,end):p.y;
 vec3 lid=texture2D(uMaster,vec2(p.x,sampleY)/vec2(1536.0,1024.0)).rgb;
 float cover=1.0-smoothstep(-2.0,0.0,p.y-end);
 if(closure<=0.000001){
  // Keep the approved open frame's arithmetic exactly unchanged.
  rgb=mix(rgb,fabric(lid),cover);
  gl_FragColor=vec4(rgb*alpha,alpha);
 }else{
  float lidAlpha=texture2D(uRig,vec2(p.x,sampleY)/vec2(1536.0,640.0)).a;
  gl_FragColor=puppetComposite(rgb,alpha,fabric(lid),lidAlpha,cover);
 }
}
