precision highp float;
uniform sampler2D uMaster;
uniform sampler2D uRig;
uniform vec4 uPose;
uniform float uHueRadians;
uniform vec3 uFeltTint;
varying vec2 vUv;
const vec2 SIZE=vec2(1774.0,887.0);

vec3 straightColour(vec4 c){return c.rgb/max(c.a,0.00001);}
// Static material coordinates: head movement carries the cloth with the eye,
// while the closing lid reveals more pile without stretching fibres vertically.
float feltHash(vec2 p){
    vec3 q=fract(vec3(p.xyx)*0.1031);
    q+=dot(q,q.yzx+33.33);
    return fract((q.x+q.y)*q.z);
}
float feltNoise(vec2 p){
    vec2 i=floor(p),f=fract(p);
    f=f*f*(3.0-2.0*f);
    return mix(mix(feltHash(i),feltHash(i+vec2(1.0,0.0)),f.x),
               mix(feltHash(i+vec2(0.0,1.0)),feltHash(i+vec2(1.0,1.0)),f.x),f.y);
}
vec3 feltMaterial(vec3 skin,vec2 p){
    float light=clamp(dot(skin,vec3(0.213,0.715,0.072)),0.0,1.0);
    float pile=feltNoise(p/5.5);
    // Two soft, crossed and bent fibre fields avoid a regular woven pattern.
    vec2 a=vec2(p.x+0.37*p.y,p.y-0.21*p.x);
    vec2 b=vec2(p.x-0.51*p.y,p.y+0.43*p.x);
    float strandA=1.0-smoothstep(0.07,0.26,abs(feltNoise(a/vec2(13.0,2.5)+pile)-0.5));
    float strandB=1.0-smoothstep(0.06,0.24,abs(feltNoise(b/vec2(10.0,3.0)-pile)-0.5));
    // Compress the old plastic specular into a broad charcoal-felt crown light.
    // Tint changes cloth colour while preserving the original material luminance.
    float crown=sqrt(light);
    float charcoal=0.025+0.25*crown;
    float fibres=(0.65*strandA+0.35*strandB-0.43)*(0.045+0.10*crown);
    float nap=(pile-0.5)*0.035;
    return vec3(clamp(charcoal+fibres+nap,0.012,0.42))*uFeltTint;
}
vec3 hueRotate(vec3 c,float a){
    float co=cos(a),si=sin(a);
    return clamp(vec3(
        c.r*(0.213+co*0.787-si*0.213)+c.g*(0.715-co*0.715-si*0.715)+c.b*(0.072-co*0.072+si*0.928),
        c.r*(0.213-co*0.213+si*0.143)+c.g*(0.715+co*0.285+si*0.140)+c.b*(0.072-co*0.072-si*0.283),
        c.r*(0.213-co*0.213-si*0.787)+c.g*(0.715-co*0.715+si*0.715)+c.b*(0.072+co*0.928+si*0.072)),0.0,1.0);
}
float irisMask(vec2 source){
    vec2 centre=source.x<887.0?vec2(535.0,543.0):vec2(1233.0,543.0);
    float d=length((source-centre)/vec2(225.0,225.0));
    return 1.0-smoothstep(0.82,1.02,d);
}
void main(){
    vec4 original=texture2D(uMaster,vUv);
    if(original.a<0.00001){gl_FragColor=vec4(0.0);return;}
    vec2 p=vUv*SIZE;
    bool left=p.x<887.0;
    float closure=left?uPose.x:uPose.y;
    vec2 encoded=texture2D(uRig,vec2(vUv.x,0.25)).rg;
    float edge=(encoded.r*65280.0+encoded.g*255.0)/32.0;
    vec2 continued=texture2D(uRig,vec2(vUv.x,0.75)).rg;
    float contour=(continued.r*65280.0+continued.g*255.0)/32.0;
    float root=edge-64.0;

    // Sample the existing eye field with an anchored support region. No drawn iris.
    vec2 centre=left?vec2(535.0,550.0):vec2(1235.0,550.0);
    vec2 local=(p-centre)/vec2(310.0,300.0);
    float support=1.0-smoothstep(0.55,1.0,length(local));
    support*=smoothstep(edge+12.0,edge+85.0,p.y);
    vec2 source=p-uPose.zw*vec2(36.0,25.0)*support;
    vec3 rgb=straightColour(texture2D(uMaster,source/SIZE));
    rgb=mix(rgb,hueRotate(rgb,uHueRadians),irisMask(source));

    // Apply the same cloth to the resting upper lid, including neutral cyan.
    // The approved rig's lower edge is the material boundary, not iris colour.
    float openLid=1.0-smoothstep(-1.5,1.5,p.y-edge);
    if(openLid>0.0){
        rgb=mix(rgb,feltMaterial(straightColour(original),p),openLid);
    }
    if(closure>0.000001){
        // Stable destination and continuous coverage replace alpha-speck endpoints.
        float end=mix(edge,887.0,clamp(closure,0.0,1.0));
        // Continue the inner arc instead of inheriting the artwork end-cap's
        // horizontal shelf. Ease out of the exact open pose, converge at shut.
        end+=(contour-edge)*smoothstep(0.0,0.12,closure)*(1.0-closure);
        float sy=root+(p.y-root)*(edge-root)/max(end-root,1.0);
        sy=clamp(sy,root,edge);
        vec3 skin=straightColour(texture2D(uMaster,vec2(p.x,sy)/SIZE));
        float coverage=smoothstep(-1.5,1.5,end-p.y)*smoothstep(root,root+3.0,p.y);
        if(coverage>0.0){rgb=mix(rgb,feltMaterial(skin,p),coverage);}
    }
    gl_FragColor=vec4(rgb*original.a,original.a);
}
