precision mediump float;
varying mediump vec2 vInk;
void main(){
    // Match the canonical GL_ONE / GL_ONE_MINUS_SRC_ALPHA blend mode.
    gl_FragColor=vec4(vec3(vInk.x)*vInk.y,vInk.y);
}
