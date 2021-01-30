package isaacais;

import java.io.*;
import java.util.Arrays;

import static org.lwjgl.opengl.GL20.*;

public final class Shader {

    public static int createShader(String path, int type) {
        try {
            return createShader(path, getFile(path), type);
        } catch(IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating shader " + path);
        } catch (Exception e) {
            System.err.println(path);
            e.printStackTrace();
            throw new RuntimeException("Error creating shader " + path);
        }
    }

    public static String getFile(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Shader.class.getResourceAsStream(path)));
        StringBuilder b = new StringBuilder();
        while(reader.ready()) {
            b.append(reader.readLine()).append("\n");
        }
        return b.toString();
    }

    public static int createShader(String name, String src, int type) {
        int shader = glCreateShader(type);
        glShaderSource(shader, src);
        glCompileShader(shader);

        int[] p = new int[1];
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, p);
        if(p[0] != 0) {
            System.err.println(name + " shader info log");
            System.err.println(glGetShaderInfoLog(shader));
        }

        glGetShaderiv(shader, GL_COMPILE_STATUS, p);
        if(p[0] != GL_TRUE) {
            glDeleteShader(shader);
            throw new RuntimeException("Failed to compile shader " + name);
        }
        return shader;
    }

    public static void checkLinking(String name, int program) {
        int[] p = new int[1];
        glGetProgramiv(program, GL_INFO_LOG_LENGTH, p);
        if(p[0] != 0) {
            System.err.println(name + " program info log");
            System.err.println(glGetProgramInfoLog(program));
        }

        glGetProgramiv(program, GL_LINK_STATUS, p);
        if(p[0] != GL_TRUE) {
            glDeleteProgram(program);
            throw new RuntimeException("Failed to link program " + program);
        }
    }

    public enum Attribute {
        POSITION(0), TEXTURE(1);

        public int num;
        Attribute(int position) {
            this.num = position;
        }
    }

    public static int UNIFORM_COUNT = 3;
    public enum Uniform {
        MATRIX(0), COLOR(1), SAMPLER(2);

        public int num;
        Uniform(int position) {
            this.num = position;
        }
    }
    public static int[] initUniformList() {
        int[] uniforms = new int[UNIFORM_COUNT];
        Arrays.fill(uniforms, -1);
        return uniforms;
    }
}
