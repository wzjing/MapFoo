#version 300 es

precision highp float;
uniform sampler2D iChannel0;
uniform vec2 iResolution;

out vec4 fragColor;
void main()
{
    vec2 uv = (gl_FragCoord.xy+0.5)/iResolution.xy;
    fragColor = texture(iChannel0, uv);
}