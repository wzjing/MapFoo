#version 300 es

precision highp float;

out vec4 fragColor;

uniform vec2 iResolution;

void main() {
//    if (gl_FragCoord.x > iResolution.x/2.0){
//        fragColor = vec4(0, 1.0, 0, 1.0);
//    } else {
//        fragColor = vec4(0, 1.0, 1.0, 0);
//    }
    fragColor = vec4(0.0, 1.0, 1.0, 0);
}
