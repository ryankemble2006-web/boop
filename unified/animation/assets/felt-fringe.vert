attribute vec2 aPosition;
attribute vec2 aOffset;
attribute vec3 aInk;
uniform vec2 uScale;
uniform float uPixelScale;
varying mediump vec3 vInk;
varying highp vec2 vUv;
void main(){
    // Centre follows BOOP; ribbon width stays readable at preview/phone/TV sizes.
    vec2 point=aPosition+aOffset/uPixelScale;
    gl_Position=vec4(point*uScale,0.0,1.0);
    vUv=vec2(point.x*.5+.5,.5-point.y*.5);
    vInk=aInk;
}
