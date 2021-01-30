package isaacais;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import static isaacais.Shader.Uniform.*;

public class BoxRenderer {

    private static final float[] TRIANGLES = {
        -0.5f, -0.5f,
        -0.5f, +0.5f,
        +0.5f, +0.5f,
        +0.5f, +0.5f,
        +0.5f, -0.5f,
        -0.5f, -0.5f
    };

    private final int program;
    private int[] uniforms;
    private int vao;
    private int vbo;

    public BoxRenderer() {
        int vert = Shader.createShader("../simplev.glsl", GL_VERTEX_SHADER);
        int frag = Shader.createShader("../simplef.glsl", GL_FRAGMENT_SHADER);
        program = glCreateProgram();
        glAttachShader(program, vert);
        glAttachShader(program, frag);
        glBindAttribLocation(program, Shader.Attribute.POSITION.num, "position");
        glLinkProgram(program);
        Shader.checkLinking("simple", program);
        glUseProgram(program);
        glDeleteShader(vert);
        glDeleteShader(frag);
        Main.checkGLError("simple shader init");
        uniforms = Shader.initUniformList();
        uniforms[MATRIX.num] = glGetUniformLocation(program, "matrix");
        uniforms[COLOR.num] = glGetUniformLocation(program, "color");

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, TRIANGLES, GL_STATIC_DRAW);
        glEnableVertexAttribArray(Shader.Attribute.POSITION.num);
        glVertexAttribPointer(Shader.Attribute.POSITION.num, 2, GL_FLOAT, false, 2 * 4, 0);
        Main.checkGLError("simple buffer init");
    }

    public void draw(Matrix4f matrix, Vector4f color) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            glBindVertexArray(vao);
            glUseProgram(program);
            glUniform4f(uniforms[COLOR.num], color.x, color.y, color.z, color.w);
            glUniformMatrix4fv(uniforms[MATRIX.num], false, matrix.get(buffer));
            glDrawArrays(GL_TRIANGLES, 0, 6);
        }
    }

    public void destroy() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        glDeleteProgram(program);
        Main.checkGLError("simple cleanup");
    }
}
