#version 300 es

in vec3 vertexPosition;
uniform mat4 mvp;

void main() {
    gl_Position = vec4(vertexPosition, 1);
}
