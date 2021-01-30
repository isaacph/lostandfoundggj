package isaacais;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    public long window;
    public Matrix4f ortho, view;

    public void onWindowSizeChange(int w, int h) {
        glViewport(0, 0, w, h);
        ortho = new Matrix4f().ortho(0.0f, (float) w, (float) h, 0, 0.0f, 1.0f);
        view = new Matrix4f();
        view.scale(100);
        view.translate(5, 5, 0);
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
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
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
        Box player = new Box(3, 3, 1);
        ArrayList<Box> wall = new ArrayList<>(Arrays.asList(
            new Box(4, 4, 1),
            new Box(5, 4, 1),
            new Box(5, 5, 1)
        ));

        System.out.println(Convex.project(new Vector2f(1, 0), new Vector2f(0, 0), new Vector2f(2, 1)));

        double currentTime = glfwGetTime(), lastTime = currentTime;
        double delta;
        while ( !glfwWindowShouldClose(window) ) {
            currentTime = glfwGetTime();
            delta = currentTime - lastTime;
            lastTime = currentTime;

            do {
                float timeStep = 1.0f / 60.0f;
                delta -= timeStep;

                if(glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
                    player.rotation += timeStep;
                }
                if(glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) {
                    player.rotation -= timeStep;
                }

                Vector2f move = new Vector2f(0);
                if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
                    --move.y;
                }
                if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
                    ++move.y;
                }
                if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
                    --move.x;
                }
                if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
                    ++move.x;
                }
                if(!move.equals(0, 0)) {
                    move.normalize(timeStep * 1);
                }
                player.x += move.x;
                player.y += move.y;

//                if(glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS) {
                    Vector2f resolve = player.smallestResolution(wall);
                    player.x += resolve.x;
                    player.y += resolve.y;
//                }
            } while(delta >= 1.0 / 60.0);

            glClear(GL_COLOR_BUFFER_BIT);

            Main.checkGLError("Main start");
            boxRender.draw(new Matrix4f(ortho).mul(view).mul(player.getModelMatrix()), new Vector4f(1, 1, 1, 1));

            texture.bind();
            for(Box w : wall) {
                texRender.draw(new Matrix4f(ortho).mul(view).mul(w.getModelMatrix()), new Vector4f(1, 1, 1, 1));
            }

//            {
//                boxRender.draw(new Matrix4f(ortho).mul(view).translate(0, 0, 0).rotate(player.rotation, 0, 0, 1).scale(40, 0.04f, 0), new Vector4f(1, 1, 0.5f, 1));
//                for(int i = 0; i < 4; ++i)
//                {
//                    Vector2f v = Convex.project(player.axes()[0], new Vector2f(0), player.points()[i]);
////                    v.y += player.y;
//                    boxRender.draw(new Matrix4f(ortho).mul(view).translate(
//                        v.x, v.y, 0).scale(0.08f), new Vector4f(0, 0, 1, 1));
//                }
//                for(Box b : wall) {
//                    for(int i = 0; i < 4; ++i) {
//                        Vector2f v = Convex.project(player.axes()[0], new Vector2f(0), b.points()[i]);
////                    v.y += player.y;
//                        boxRender.draw(new Matrix4f(ortho).mul(view).translate(
//                            v.x, v.y, 0).scale(0.08f), new Vector4f(0, 1, 1, 0.1f));
//                    }
//                }
//            }
//
//            {
//                boxRender.draw(new Matrix4f(ortho).mul(view).translate(0, 0, 0).rotate(0, 0, 0, 1).scale(40, 0.04f, 0), new Vector4f(1, 1, 0.5f, 1));
//                for(int i = 0; i < 4; ++i)
//                {
//                    Vector2f v = Convex.project(new Vector2f(1, 0), new Vector2f(0), player.points()[i]);
////                    v.y += player.y;
//                    boxRender.draw(new Matrix4f(ortho).mul(view).translate(
//                        v.x, v.y, 0).scale(0.08f), new Vector4f(0, 0, 1, 1));
//                }
//                for(Box b : wall) {
//                    for(int i = 0; i < 4; ++i) {
//                        Vector2f v = Convex.project(new Vector2f(1, 0), new Vector2f(0), b.points()[i]);
////                    v.y += player.y;
//                        boxRender.draw(new Matrix4f(ortho).mul(view).translate(
//                            v.x, v.y, 0).scale(0.08f), new Vector4f(0, 1, 1, 0.1f));
//                    }
//                }
//            }
//            {
//                boxRender.draw(new Matrix4f(ortho).mul(view).translate(0, 0, 0).rotate(player.rotation + (float) Math.PI / 2, 0, 0, 1).scale(40, 0.04f, 0), new Vector4f(1, 1, 0, 1));
////                boxRender.draw(new Matrix4f(ortho).mul(view).translate(player.x, player.y, 0).rotate(player.rotation + (float) Math.PI / 2, 0, 0, 1).scale(4, 0.04f, 0), new Vector4f(1, 1, 0, 1));
//                for(Vector2f v : player.points()) {
//                    boxRender.draw(new Matrix4f(ortho).mul(view).translate(v.x, v.y, 0).scale(0.1f), new Vector4f(0.1f, 1, 0, 1));
//                }
//                for(int i = 0; i < 4; ++i)
//                {
//                    Vector2f v = Convex.project(player.axes()[1], new Vector2f(0), player.points()[i]);
////                    v.y += player.y;
//                    boxRender.draw(new Matrix4f(ortho).mul(view).translate(
//                        v.x, v.y, 0).scale(0.08f), new Vector4f(0, 0, 1, 1));
//                }
//                for(Box b : wall) {
//                    for(int i = 0; i < 4; ++i) {
//                        Vector2f v = Convex.project(player.axes()[1], new Vector2f(0), b.points()[i]);
////                    v.y += player.y;
//                        boxRender.draw(new Matrix4f(ortho).mul(view).translate(
//                            v.x, v.y, 0).scale(0.08f), new Vector4f(0, 1, 1, 0.1f));
//                    }
//                }
//                ArrayList<Vector2f> resolutions = new ArrayList<>();
//                int bestResolutionCollisions = 0;
//                for(Box c : wall) {
//                    if(player.intersects(c)) {
//                        resolutions.addAll(player.resolveOptions(c));
////                        ++bestResolutionCollisions;
////                        ArrayList<Vector2f> axes = new ArrayList<>(Arrays.asList(player.axes()));
////                        for(Vector2f p : c.points()) {
////                            for(Vector2f a : axes) {
////                                Vector2f proj = Convex.project(a, p).add(0, player.y);
////                                boxRender.draw(new Matrix4f(ortho).translate(proj.x, proj.y,0).scale(10), new Vector4f(0, 1, 0, 1));
////                            }
////                        }
////                        axes = new ArrayList<>(Arrays.asList(c.axes()));
////                        for(Vector2f p : c.points()) {
////                            for(Vector2f a : axes) {
////                                Vector2f proj = Convex.project(a, p).add(c.x, c.y);
////                                boxRender.draw(new Matrix4f(ortho).translate(proj.x, proj.y,0).scale(10), new Vector4f(0, 1, 0, 1));
////                            }
////                        }
//                    }
//                }
//                for(Vector2f v : resolutions) {
//                    Box b = new Box(player);
//                    b.x += v.x;
//                    b.y += v.y;
//                    boxRender.draw(new Matrix4f(ortho).mul(view).mul(b.getModelMatrix()), new Vector4f(1, 0, 0, 0.1f));
//                }
////                int step1Size = resolutions.size();
////                for(int i = 0; i < step1Size; ++i) {
////                    Convex b = add(resolutions.get(i));
////                    for (Convex b2 : convexes) {
////                        if(b.intersects(b2)) {
////                            ArrayList<Vector2f> options = b.resolveOptions(b2);
////                            for(Vector2f v : options) {
////                                v.x += resolutions.get(i).x;
////                                v.y += resolutions.get(i).y;
////                            }
////                            resolutions.addAll(options);
////                        }
////                    }
////                }
////                Vector2f bestResolution = new Vector2f(0);
////                float bestResolutionMagSq = 0;
////                for(int i = 0; i < resolutions.size(); ++i) {
////                    int collisions = 0;
////                    Convex option = add(resolutions.get(i));
////                    for(Convex b : convexes) {
////                        if(option.intersects(b)) {
////                            ++collisions;
////                        }
////                    }
////                    if(collisions < bestResolutionCollisions
////                        || (collisions == bestResolutionCollisions && resolutions.get(i).lengthSquared() < bestResolutionMagSq)) {
////                        bestResolution = resolutions.get(i);
////                        bestResolutionCollisions = collisions;
////                        bestResolutionMagSq = bestResolution.lengthSquared();
////                    }
////                }
//            }

            double[] mx = new double[1], my = new double[1];
            glfwGetCursorPos(window, mx, my);
            font.draw(""+(mx[0] / 100 - 5) + ", " + (my[0] / 100 - 5), 100, 250, ortho);

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
