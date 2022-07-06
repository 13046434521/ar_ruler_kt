#version 300 es

precision mediump float;
layout(location = 0) out vec4 o_FragColor;

void main() {
    if (length(gl_PointCoord - vec2(0.5)) > 0.5){
        discard;
    }
    o_FragColor = vec4(1f, 0f, 0f, 1f);
}
