#version 330 core
in vec2 texCoord;
uniform sampler2D sampler;
uniform vec4 color;
out vec4 outColor;
void main() {
    float f = texture2D(sampler, texCoord).r;
    outColor = color * vec4(f);
}