#version 330 core
in vec2 position;
in vec2 texture;
uniform mat4 matrix;
out vec2 texCoord;

void main() {
    gl_Position = matrix * vec4(position, 0, 1);
    texCoord = texture;
}