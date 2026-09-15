precision highp float;
uniform sampler2D uMaster;
uniform vec3 uFeltTint;
uniform sampler2D uRig;
varying mediump vec3 vInk;
varying highp vec2 vUv;
void main(){
    float coverage=1.0-smoothstep(.2,1.0,abs(vInk.z));
    float alpha=vInk.y*coverage;
    // Surface wisps may cover felt, never the exposed eye whites or iris.
    vec2 encoded=texture2D(uRig,vec2(vUv.x,.25)).rg;
    float edge=(encoded.r*65280.0+encoded.g*255.0)/32.0;
    float above=1.0-smoothstep(-1.0,1.0,vUv.y*887.0-edge);
    float solid=step(.01,texture2D(uMaster,vUv).a);
    alpha*=mix(1.0,above,solid);
    gl_FragColor=vec4(vec3(vInk.x)*uFeltTint*alpha,alpha);
}
