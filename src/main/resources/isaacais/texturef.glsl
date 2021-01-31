#version 330 core
in vec2 texCoord;
uniform vec4 color;
uniform sampler2D sampler;
out vec4 outColor;

void main() {
    outColor = color * texture2D(sampler, texCoord);
}