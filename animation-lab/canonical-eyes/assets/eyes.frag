precision highp float;
uniform sampler2D uMaster;
uniform sampler2D uRig;
uniform vec4 uPose;
varying vec2 vUv;
const vec2 SIZE=vec2(1774.0,887.0);

vec3 straightColour(vec4 c){return c.rgb/max(c.a,0.00001);}
void main(){
    vec4 original=texture2D(uMaster,vUv);
    if(original.a<0.00001){gl_FragColor=vec4(0.0);return;}
    if(dot(abs(uPose),vec4(1.0))<0.000001){gl_FragColor=original;return;}
    vec2 p=vUv*SIZE;
    bool left=p.x<887.0;
    float closure=left?uPose.x:uPose.y;
    vec2 encoded=texture2D(uRig,vec2(vUv.x,0.5)).rg;
    float edge=(encoded.r*65280.0+encoded.g*255.0)/32.0;
    float root=edge-64.0;

    // Sample the existing eye field with an anchored support region. No drawn iris.
    vec2 centre=left?vec2(535.0,550.0):vec2(1235.0,550.0);
    vec2 local=(p-centre)/vec2(310.0,300.0);
    float support=1.0-smoothstep(0.55,1.0,length(local));
    support*=smoothstep(edge+12.0,edge+85.0,p.y);
    vec2 source=p-uPose.zw*vec2(36.0,25.0)*support;
    vec3 rgb=straightColour(texture2D(uMaster,source/SIZE));

    if(closure>0.000001){
        // Stable destination and continuous coverage replace alpha-speck endpoints.
        float end=mix(edge,887.0,clamp(closure,0.0,1.0));
        float sy=root+(p.y-root)*(edge-root)/max(end-root,1.0);
        sy=clamp(sy,root,edge);
        vec3 skin=straightColour(texture2D(uMaster,vec2(p.x,sy)/SIZE));
        float coverage=smoothstep(-1.5,1.5,end-p.y)*smoothstep(root,root+3.0,p.y);
        rgb=mix(rgb,skin,coverage);
    }
    // Original silhouette/alpha stays anchored. Texture upload is premultiplied.
    gl_FragColor=vec4(rgb*original.a,original.a);
}
