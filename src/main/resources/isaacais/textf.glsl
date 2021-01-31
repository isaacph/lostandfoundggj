#version 330 core
in vec2 texCoord;
uniform sampler2D sampler;
uniform vec4 color;
out vec4 outColor;
void main() {
    float f = texture2D(sampler, texCoord).r;
    outColor = vec4(vec3(color.w), 1) * color * vec4(f);
}