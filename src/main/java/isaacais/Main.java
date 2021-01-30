package isaacais;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    public long window;
    public Matrix4f ortho;

    public void onWindowSizeChange(int w, int h) {
        glViewport(0, 0, w, h);
        ortho = new Matrix4f().ortho(0.0f, (float) w, (float) h, 0, 0.0f, 1.0f);
    }

    public void run() {
        GLFWErrorCallback.createPrint(System.err).set();
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(800, 600, "Hello World!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        GL.createCapabilities();
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glfwSetWindowSizeCallback(window, (long win, int w, int h) -> {
            if(w > 0 && h > 0) {
                this.onWindowSizeChange(w, h);
            }
        });
        this.onWindowSizeChange(800, 600);

        BoxRenderer boxRender = new BoxRenderer();
        TextureRenderer texRender = new TextureRenderer();
        Texture texture = new Texture("../scrap.png");
        Font font = new Font("../font.ttf", 48, 512, 512);

        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT);

            Main.checkGLError("Main start");
            boxRender.draw(new Matrix4f(ortho).translate(100, 100, 0).scale(100, 100, 0), new Vector4f(1, 1, 1, 1));

            texture.bind();
            texRender.draw(new Matrix4f(ortho).translate(220, 100, 0).scale(100, 100, 0), new Vector4f(1, 1, 1, 1));

            font.draw("tessssssssst", 100, 250, ortho);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        Main.checkGLError("Main loop");

        boxRender.destroy();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public static void main(String... args) {
        new Main().run();
    }

    public static void checkGLError(String text) {
        int code = glGetError();
        if(code != 0) {
            System.err.println("OpenGL Error " + code + ": " + text);
        }
    }
}
