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
    public float screenX, screenY;
    public float scale = 128.0f;
    public int pixelWidth = 800, pixelHeight = 600;
    private Vector2f mouseWorld;

    public void onWindowSizeChange(int w, int h) {
        if(w > 0 && h > 0) {
            glViewport(0, 0, w, h);
            ortho = new Matrix4f().ortho(0.0f, (float) w, (float) h, 0, 0.0f, 1.0f);
            pixelWidth = w;
            pixelHeight = h;
            scale = w / 20.0f;
        }
    }

    public Vector2f toWorldSpace(Vector2f screenSpace) {
        return new Vector2f(((screenSpace.x - pixelWidth / 2.0f) / scale) + screenX, ((screenSpace.y - pixelHeight / 2.0f) / scale) + screenY);
    }

    public void setViewMatrix() {
        view = new Matrix4f();
        view.translate(pixelWidth / 2.0f, pixelHeight / 2.0f, 0);
        view.scale(scale);
        view.translate(-screenX, -screenY, 0);
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
        FloorRenderer floorRender = new FloorRenderer();
        Texture texture = new Texture("../scrap.png");
        Font font = new Font("../font.ttf", 48, 512, 512);
        Console console = new Console(font, boxRender);

        Floor.Group floors = new Floor.Group();
        floors.setTile((byte) 1, 0, 0);
        floors.setTile((byte) 1, -1, 0);
        floors.setTile((byte) 1, 0, -1);
        floors.setTile((byte) 1, -1, -1);
        floorRender.build(floors);

        Texture kid = new Texture("../kid.png");
        Texture roomba = new Texture("../roomba.png");
        Texture sof = new Texture("../sofa.png");
        Box player = new Box(0, 0, 0.4f);

        double[] mx = new double[1], my = new double[1];

        glfwSetKeyCallback(window, ((w, key, scancode, action, mods) -> {
            if(key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
                if(console.focus) {
                    if (!console.send()) {
                        console.disable();
                    }
                } else {
                    console.enable();
                }
            }
            if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                if(console.focus) {
                    console.disable();
                }
            } else if(key == GLFW_KEY_BACKSPACE && action > 0) {
                if(console.typing.length() > 0) {
                    console.typing.deleteCharAt(console.typing.length() - 1);
                }
            }
            if(action == GLFW_PRESS && key == GLFW_KEY_0) {
                Floor.InitData entity = new Floor.InitData();
                entity.type = Floor.EntityType.PLAYER.val;
                entity.position = mouseWorld;
            }
        }));
        glfwSetCharCallback(window, ((w, c) -> {
            if(console.focus) {
                console.typing.append((char) c);
            }
        }));

        console.println("hello world!");
        console.println("hello world!");
        console.println("hello world!");
        console.println("hello world!");

        double currentTime = glfwGetTime(), lastTime = currentTime;
        double delta;
        while ( !glfwWindowShouldClose(window) ) {
            currentTime = glfwGetTime();
            delta = currentTime - lastTime;
            lastTime = currentTime;
            screenX = player.x;
            screenY = player.y;
            glfwGetCursorPos(window, mx, my);
            mouseWorld = toWorldSpace(new Vector2f((float) mx[0], (float) my[0]));

            console.update(delta);
            for(String cmd : console.commands) {
                if(cmd.startsWith("/")) {
                    String[] args = cmd.substring(1).split("\\s");
                    if(args[0].equals("test")) {
                        console.println("Testing!");
                    } else if(args[0].equals("set") && args.length == 4) {
                        floors.setTile(Byte.parseByte(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                        floorRender.build(floors);
                    } else if(args[0].equals("save") && args.length == 2) {
                        Floor.toFile(floors, args[1]);
                    } else if(args[0].equals("load") && args.length == 2) {
                        Floor.Group f = Floor.fromFile(args[1]);
                        if(f != null) {
                            floors = f;
                            floorRender.build(floors);
                        }
                    } else {
                        console.println("Unknown command!");
                    }
                } else {
                    console.println(cmd);
                }
            }
            console.commands.clear();

            if(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
                if (floors.getTile((int) Math.floor(mouseWorld.x), (int) Math.floor(mouseWorld.y)) != (byte) 1) {
                    floors.setTile((byte) 1, (int) Math.floor(mouseWorld.x), (int) Math.floor(mouseWorld.y));
                    floorRender.build(floors);
                }
            } else if(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS) {
                if (floors.getTile((int) Math.floor(mouseWorld.x), (int) Math.floor(mouseWorld.y)) != (byte) 0) {
                    floors.setTile((byte) 0, (int) Math.floor(mouseWorld.x), (int) Math.floor(mouseWorld.y));
                    floorRender.build(floors);
//                    System.out.printf("%f, %f\n", b.x, b.y);
                }
            }

            do {
                float timeStep = 1.0f / 60.0f;
                delta -= timeStep;

                Vector2f move = new Vector2f(0);
                if(!console.focus) {
                    if(glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
                        player.rotation += timeStep;
                    }
                    if(glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) {
                        player.rotation -= timeStep;
                    }

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
                }

                if(!move.equals(0, 0)) {
                    move.normalize(timeStep * 5);
                }
                player.x += move.x;
                player.y += move.y;

                ArrayList<Box> wall = new ArrayList<>();
                int minX = (int) Math.floor(player.x) - 1, maxX = (int) Math.ceil(player.x) + 1;
                int minY = (int) Math.floor(player.y) - 1, maxY = (int) Math.ceil(player.y) + 1;
                for(int y = minY; y <= maxY; ++y) {
                    for(int x = minX; x <= maxX; ++x) {
                        if(floors.getTile(x, y) == Floor.Tile.EMPTY.val) {
                            wall.add(new Box(x + 0.5f, y + 0.5f, 1.0f, 1.0f));
                        }
                    }
                }

                Vector2f resolve = player.smallestResolution(wall);
                player.x += resolve.x;
                player.y += resolve.y;
                screenX = player.x;
                screenY = player.y;
            } while(delta >= 1.0 / 60.0);

            glClear(GL_COLOR_BUFFER_BIT);

            setViewMatrix();

            Main.checkGLError("Main start");

            texture.bind();
            floorRender.draw(new Matrix4f(ortho).mul(view));

            for(Floor.InitData entity : floors.entityData) {
                if(entity.type == Floor.EntityType.PLAYER.val) {
                    kid.bind();
                    texRender.draw(new Matrix4f(ortho).mul(view).translate(entity.position.x, entity.position.y - 0.3f, 0).scale(1.0f, 1.0f, 0), new Vector4f(1, 1, 1, 1));
                } else if(entity.type == Floor.EntityType.ROOMBA.val) {
                    roomba.bind();
                    texRender.draw(new Matrix4f(ortho).mul(view).translate(entity.position.x, entity.position.y, 0).scale(1.0f, 1.0f, 0), new Vector4f(1, 1, 1, 1));
                }
            }

            kid.bind();
            texRender.draw(new Matrix4f(ortho).mul(view).translate(player.x, player.y - 0.3f, 0).scale(1.0f, 1.0f, 0), new Vector4f(1, 1, 1, 1));
//            for(Floor floor : floors.floors.values()) {
//                for(int y = 0; y < Floor.FLOOR_SIZE; ++y) {
//                    for(int x = 0; x < Floor.FLOOR_SIZE; ++x) {
//                        if(floor.get(x, y) != (byte) 0) {
//                            boxRender.draw(new Matrix4f(ortho).mul(view)
//                                .translate(floor.x * Floor.FLOOR_SIZE + x + 0.5f, floor.y * Floor.FLOOR_SIZE + y + 0.5f, 0),
//                                new Vector4f(0, 1, 0, 0.5f));
//                        }
//                    }
//                }
//            }
//            font.draw(""+(mx[0] / 100 - 5) + ", " + (my[0] / 100 - 5), 100, 250, ortho, new Vector4f(1, 1, 1, 1));
            console.draw(ortho);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        Main.checkGLError("Main loop");

        boxRender.destroy();
        texRender.destroy();
        font.cleanUp();

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
