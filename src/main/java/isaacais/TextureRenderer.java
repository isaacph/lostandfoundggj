package isaacais;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static isaacais.Shader.Uniform.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class TextureRenderer {

    public final int program;
    public int[] uniforms;
    private int vao;
    private int vbo;

    public TextureRenderer(int duplicate) {
        float scale = duplicate;
        float[] triangles = {
            -0.5f, -0.5f, 0.0f, 0.0f,
            -0.5f, +0.5f, 0.0f, scale,
            +0.5f, +0.5f, scale, scale,
            +0.5f, +0.5f, scale, scale,
            +0.5f, -0.5f, scale, 0.0f,
            -0.5f, -0.5f, 0.0f, 0.0f
        };

        int vert = Shader.createShader("../texturev.glsl", GL_VERTEX_SHADER);
        int frag = Shader.createShader("../texturef.glsl", GL_FRAGMENT_SHADER);
        program = glCreateProgram();
        glAttachShader(program, vert);
        glAttachShader(program, frag);
        glBindAttribLocation(program, Shader.Attribute.POSITION.num, "position");
        glBindAttribLocation(program, Shader.Attribute.TEXTURE.num, "texture");
        glLinkProgram(program);
        Shader.checkLinking("texture", program);
        glUseProgram(program);
        glDeleteShader(vert);
        glDeleteShader(frag);
        Main.checkGLError("texture shader init");
        uniforms = Shader.initUniformList();
        uniforms[MATRIX.num] = glGetUniformLocation(program, "matrix");
        uniforms[COLOR.num] = glGetUniformLocation(program, "color");
        uniforms[SAMPLER.num] = glGetUniformLocation(program, "sampler");

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, triangles, GL_STATIC_DRAW);
        glEnableVertexAttribArray(Shader.Attribute.POSITION.num);
        glVertexAttribPointer(Shader.Attribute.POSITION.num, 2, GL_FLOAT, false, 4 * 4, 0);
        glEnableVertexAttribArray(Shader.Attribute.TEXTURE.num);
        glVertexAttribPointer(Shader.Attribute.TEXTURE.num, 2, GL_FLOAT, false, 4 * 4, 2 * 4);
        Main.checkGLError("texture buffer init");
    }
    public TextureRenderer() {
        this(1);
    }

    public void draw(Matrix4f matrix, Vector4f color) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            glBindVertexArray(vao);
            glUseProgram(program);
            glUniform4f(uniforms[COLOR.num], color.x, color.y, color.z, color.w);
            glUniformMatrix4fv(uniforms[MATRIX.num], false, matrix.get(buffer));
            glUniform1i(uniforms[SAMPLER.num], 0);
            glDrawArrays(GL_TRIANGLES, 0, 6);
        }
    }

    public void destroy() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        glDeleteProgram(program);
        Main.checkGLError("texture cleanup");
    }
}
