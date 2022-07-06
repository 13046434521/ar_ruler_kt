#version 300 es

layout(location = 0) in vec4 a_Position;
uniform mat4 u_MvpMatrix;
uniform float u_Size;
void main() {
    gl_Position =  a_Position;
    gl_PointSize = u_Size;
}
