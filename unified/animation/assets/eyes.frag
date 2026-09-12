precision highp float;
uniform sampler2D uMaster;
uniform sampler2D uRig;
uniform vec4 uPose;
uniform float uHueRadians;
varying vec2 vUv;
const vec2 SIZE=vec2(1774.0,887.0);

vec3 straightColour(vec4 c){return c.rgb/max(c.a,0.00001);}
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
    if(dot(abs(uPose),vec4(1.0))<0.000001&&abs(uHueRadians)<0.000001){gl_FragColor=original;return;}
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
        rgb=mix(rgb,skin,coverage);
    }
    gl_FragColor=vec4(rgb*original.a,original.a);
}
