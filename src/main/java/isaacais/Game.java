package isaacais;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.Collections;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game {
    public long window;
    public Matrix4f ortho, view;
    public float screenX, screenY;
    public float scale = 128.0f;
    public int pixelWidth = 800, pixelHeight = 600;
    private Vector2f mouseWorld;
    private Floor.Group floors;

    private BoxRenderer boxRender;
    private TextureRenderer texRender;
    private FloorRenderer floorRender;
    private Texture scrap;
    private Texture kid;
    private Texture roomba;
    private Texture sofa;
    private Font font;

    public ArrayList<GameObject> gameObjects;
    public ArrayList<GameObject> furniture;
    public ArrayList<GameObject> roombas;

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

    public void initFurniture() {
        if(furniture != null) {
            for(GameObject g : furniture) {
                gameObjects.remove(g);
            }
        }
        furniture = new ArrayList<>();
        for(Floor.InitData entity : floors.entityData) {
            if(entity.type == Floor.EntityType.SOFA.val) {
                TextureGameObject tgo = new TextureGameObject(texRender, sofa,
                    new Box(entity.position.x, entity.position.y, 4, 1),
                    new Box(entity.position.x, entity.position.y - 1, 5),
                    0.25f);
                furniture.add(tgo);
                gameObjects.add(tgo);
            }
        }
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

        furniture = new ArrayList<>();

        boxRender = new BoxRenderer();
        texRender = new TextureRenderer();
        floorRender = new FloorRenderer();
        scrap = new Texture("../scrap.png");
        kid = new Texture("../kid.png");
        roomba = new Texture("../roomba.png");
        sofa = new Texture("../sofa.png");
        font = new Font("../font.ttf", 48, 512, 512);
        Console console = new Console(font, boxRender);

        floors = new Floor.Group();
        floors.setTile((byte) 1, 0, 0);
        floors.setTile((byte) 1, -1, 0);
        floors.setTile((byte) 1, 0, -1);
        floors.setTile((byte) 1, -1, -1);
        floorRender.build(floors);
        gameObjects = new ArrayList<>();
        TextureGameObject player =
            new TextureGameObject(texRender, kid,
                new Box(0, 0, 0.4f, 0.2f),
                new Box(0, -0.45f, 1.0f));
        gameObjects.add(player);

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
            if(!console.focus) {
                if(action == GLFW_RELEASE && key == GLFW_KEY_GRAVE_ACCENT) {
                    float closestDist = 1.5f;
                    Floor.InitData closest = null;
                    for(Floor.InitData entity : floors.entityData) {
                        if(mouseWorld.distance(entity.position) < closestDist) {
                            closestDist = mouseWorld.distance(entity.position);
                            closest = entity;
                        }
                    }
                    if(closest != null) {
                        floors.entityData.remove(closest);
                        initFurniture();
                    }
                }
                if (action == GLFW_PRESS && key == GLFW_KEY_0) {
                    Floor.InitData entity = new Floor.InitData();
                    entity.type = Floor.EntityType.PLAYER.val;
                    entity.position = mouseWorld;
                    floors.entityData.add(entity);
                }
                if (action == GLFW_PRESS && key == GLFW_KEY_1) {
                    Floor.InitData entity = new Floor.InitData();
                    entity.type = Floor.EntityType.ROOMBA.val;
                    entity.position = mouseWorld;
                    floors.entityData.add(entity);
                }
                if (action == GLFW_PRESS && key == GLFW_KEY_2) {
                    Floor.InitData entity = new Floor.InitData();
                    entity.type = Floor.EntityType.SOFA.val;
                    entity.position = mouseWorld;
                    floors.entityData.add(entity);
                    initFurniture();
                }
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
            screenX = player.getCenter().x;
            screenY = player.getCenter().y;
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

            if(!console.focus) {
                if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
                    if (floors.getTile((int) Math.floor(mouseWorld.x), (int) Math.floor(mouseWorld.y)) != (byte) 1) {
                        floors.setTile((byte) 1, (int) Math.floor(mouseWorld.x), (int) Math.floor(mouseWorld.y));
                        floorRender.build(floors);
                    }
                } else if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS) {
                    if (floors.getTile((int) Math.floor(mouseWorld.x), (int) Math.floor(mouseWorld.y)) != (byte) 0) {
                        floors.setTile((byte) 0, (int) Math.floor(mouseWorld.x), (int) Math.floor(mouseWorld.y));
                        floorRender.build(floors);
//                    System.out.printf("%f, %f\n", b.x, b.y);
                    }
                }
            }

            do {
                double timeStep = delta;

                Vector2f move = new Vector2f(0);
                if(!console.focus) {
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
                    move.normalize((float) timeStep * 5.0f);
                }
                player.move(move.x, move.y);

                ArrayList<Convex> wall = new ArrayList<>();
                int minX = (int) Math.floor(player.getCollider().getCenter().x) - 1, maxX = (int) Math.ceil(player.getCollider().getCenter().x) + 1;
                int minY = (int) Math.floor(player.getCollider().getCenter().y) - 1, maxY = (int) Math.ceil(player.getCollider().getCenter().y) + 1;
                for(int y = minY; y <= maxY; ++y) {
                    for(int x = minX; x <= maxX; ++x) {
                        if(floors.getTile(x, y) == Floor.Tile.EMPTY.val) {
                            wall.add(new Box(x + 0.5f, y + 0.5f, 1.0f, 1.0f));
                        }
                    }
                }

                for(GameObject obj : gameObjects) {
                    if(obj != player) {
                        wall.add(obj.getCollider());
                    }
                }

                Vector2f resolve = player.getCollider().smallestResolution(wall);
                player.move(resolve.x, resolve.y);
                screenX = player.getCenter().x;
                screenY = player.getCenter().y;

                for(GameObject gameObject : gameObjects) {
                    gameObject.update(timeStep, this);
                }
            } while(delta >= 0 && false);

            glClear(GL_COLOR_BUFFER_BIT);

            setViewMatrix();

            Game.checkGLError("Main start");

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

            Collections.sort(gameObjects);
            for(GameObject obj : gameObjects) {
                obj.draw(new Matrix4f(ortho).mul(view));
            }

//            kid.bind();
//            boxRender.draw(new Matrix4f(ortho).mul(view).mul(player.getModelMatrix()), new Vector4f(1, 0, 1, 1));
//            texRender.draw(new Matrix4f(ortho).mul(view).translate(player.x, player.y - 0.45f, 0).scale(1.0f, 1.0f, 0), new Vector4f(1, 1, 1, 1));
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

            if(glfwGetKey(window, GLFW_KEY_GRAVE_ACCENT) == GLFW_PRESS) {
                float closestDist = 1.5f;
                Vector2f position = null;
                for(Floor.InitData entity : floors.entityData) {
                    if(mouseWorld.distance(entity.position) < closestDist) {
                        closestDist = mouseWorld.distance(entity.position);
                        position = entity.position;
                    }
                }
                if(position != null) {
                    scrap.bind();
                    texRender.draw(new Matrix4f(ortho).mul(view).translate(position.x, position.y, 0).rotate((float) Math.PI / 4.0f, 0, 0, 1).scale(1, 0.05f, 0), new Vector4f(1, 1, 1, 1));
                    texRender.draw(new Matrix4f(ortho).mul(view).translate(position.x, position.y, 0).rotate((float) -Math.PI / 4.0f, 0, 0, 1).scale(1, 0.05f, 0), new Vector4f(1, 1, 1, 1));
                }
            }

            console.draw(ortho);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        Game.checkGLError("Main loop");

        boxRender.destroy();
        texRender.destroy();
        font.cleanUp();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public static void main(String... args) {
        new Game().run();
    }

    public static void checkGLError(String text) {
        int code = glGetError();
        if(code != 0) {
            System.err.println("OpenGL Error " + code + ": " + text);
        }
    }
}
