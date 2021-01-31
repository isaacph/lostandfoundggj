package isaacais;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.text.DecimalFormat;
import java.util.*;

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
    public Floor.Group floors;

    private BoxRenderer boxRender;
    private TextureRenderer texRender;
    private FloorRenderer floorRender;
    private SpriteRenderer spriteRender;
    private Texture scrap;
    private Texture roomba;
    private Texture sofa;
    private Texture alien;
    private Texture bunny;
    private Texture duck;
    private Texture kid;
    private Texture sink;
    private Texture oven;
    private Texture fridge;
    private Font font;
    private Font fontBig;
    private Font fontSmall;

    public ArrayList<GameObject> gameObjects;
    public Player player;

    public boolean updateGameObjects = false;
    public boolean levelEdit = false;

    public GameObject selectedGameObject = null;
    public Map<GameObject, Floor.InitData> gameObjectDataMap;
    public Console console;
    public int toysRequired, toysPossessed;

    public Vector2f playerStart;

    public SoundPlayer soundPlayer;

    private OverlayMode currentOverlay = OverlayMode.NONE;
    private double gameTimer;

    private enum OverlayMode {
        NONE, DEFEAT, VICTORY
    }

    public List<GameObject> getObjectsInTiles(Vector2i min, Vector2i max) {
        Box box = new Box((min.x + max.x + 1) / 2.0f, (min.y + max.y + 1) / 2.0f, (max.x - min.x + 1) - 0.001f, (max.y - min.y + 1) - 0.001f);
        ArrayList<GameObject> list = new ArrayList<>();
        for(GameObject obj : gameObjects) {
            if(box.intersects(obj.getCollider())) {
                list.add(obj);
            }
        }
        return list;
    }

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

    public void initGameObjects() {
        gameTimer = 0;
        toysRequired = floors.toysRequired;
        toysPossessed = 0;
        selectedGameObject = null;
        gameObjects = new ArrayList<>();
        gameObjectDataMap = new HashMap<>();
        playerStart = null;
        Player oldPlayer = player;
        player = new Player(spriteRender, new Vector2f(0));
        if(oldPlayer != null) {
            player.getCollider().move(oldPlayer.getCollider().getCenter().x - player.getCollider().getCenter().x,
                oldPlayer.getCollider().getCenter().y - player.getCollider().getCenter().y);
        }
        gameObjects.add(player);
        for(Floor.InitData entity : floors.entityData) {
            if(entity.type == Floor.EntityType.SOFA.val) {
                TextureGameObject tgo = new TextureGameObject(texRender, sofa,
                    new Box(entity.position.x, entity.position.y, 3.5f, 0.8f),
                    new Box(entity.position.x, entity.position.y - 0.8f, 5),
                    0.25f);
                gameObjects.add(tgo);
                gameObjectDataMap.put(tgo, entity);
            } else if(entity.type == Floor.EntityType.ROOMBA.val) {
                Roomba.InitMetadata meta = null;
                if(entity.metadata != null) {
                    try {
                        meta = (Roomba.InitMetadata) entity.metadata;
                    }
                    catch (Exception e) {
                        System.err.println("Roomba had incorrect metadata!");
                        e.printStackTrace();
                    }
                }
                Roomba tgo = new Roomba(texRender, roomba, new Vector2f(entity.position), meta);
                gameObjects.add(tgo);
                gameObjectDataMap.put(tgo, entity);
            } else if(entity.type >= Floor.EntityType.TOY0.val && entity.type <= Floor.EntityType.TOY2.val) {
                int toyNumber = entity.type - Floor.EntityType.TOY0.val;
                Toy toy = null;
                if(toyNumber == 0) {
                    toy = new Toy(texRender, alien, new Vector2f(entity.position), new Vector2f(0.2f), new Vector2f(0), 0.5f, toyNumber);
                } else if(toyNumber == 1) {
                    toy = new Toy(texRender, bunny, new Vector2f(entity.position), new Vector2f(0.2f), new Vector2f(0), 0.5f, toyNumber);
                } else if(toyNumber == 2) {
                    toy = new Toy(texRender, duck, new Vector2f(entity.position), new Vector2f(0.2f), new Vector2f(0), 0.5f, toyNumber);
                }
                if(toy != null) {
                    gameObjects.add(toy);
                    gameObjectDataMap.put(toy, entity);
                } else {
                    System.err.println("Error: unidentified toy number " + toyNumber);
                }
            } else if(entity.type == Floor.EntityType.PLAYER.val) {
                playerStart = entity.position;
            } else if(entity.type == Floor.EntityType.SINK.val) {
                TextureGameObject tgo = new TextureGameObject(texRender, sink,
                    new Box(entity.position.x, entity.position.y, 3.2f, 0.8f),
                    new Box(entity.position.x - 0.2f, entity.position.y - 0.8f, 5),
                    0.25f);
                gameObjects.add(tgo);
                gameObjectDataMap.put(tgo, entity);
            } else if(entity.type == Floor.EntityType.OVEN.val) {
                TextureGameObject tgo = new TextureGameObject(texRender, oven,
                    new Box(entity.position.x, entity.position.y, 0.01f, 0.01f),
                    new Box(entity.position.x + 0.1f, entity.position.y, 2),
                    0.25f);
                gameObjects.add(tgo);
                gameObjectDataMap.put(tgo, entity);
            } else if(entity.type == Floor.EntityType.FRIDGE.val) {
                TextureGameObject tgo = new TextureGameObject(texRender, fridge,
                    new Box(entity.position.x, entity.position.y, 2.0f, 0.8f),
                    new Box(entity.position.x, entity.position.y - 1.4f, 5),
                    0.25f);
                gameObjects.add(tgo);
                gameObjectDataMap.put(tgo, entity);
            }
        }
    }

    public void initPlayer() {
        if(playerStart == null) playerStart = new Vector2f(0);
        gameObjects.remove(player);
        player = new Player(spriteRender, new Vector2f(playerStart));
        gameObjects.add(player);
    }

    public void loadLevel(String resourcePath) {
        updateGameObjects = true;
        levelEdit = false;
        {
            Floor.Group f = Floor.fromResource(Util.PATH_PREFIX + resourcePath);
            if (f != null) {
                floors = f;
                floorRender.build(floors);
                initGameObjects();
            }
        }
        initGameObjects();
        initPlayer();
    }

    public void run() {
        GLFWErrorCallback.createPrint(System.err).set();
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(800, 600, "Lost and Found!", NULL, NULL);
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
        soundPlayer = new SoundPlayer();

        boxRender = new BoxRenderer();
        texRender = new TextureRenderer();
        floorRender = new FloorRenderer();
        spriteRender = new SpriteRenderer();
        scrap = new Texture(Util.PATH_PREFIX + "scrap.png");
        roomba = new Texture(Util.PATH_PREFIX + "roomba.png");
        sofa = new Texture(Util.PATH_PREFIX + "sofa.png");
        alien = new Texture(Util.PATH_PREFIX + "alien.png");
        bunny = new Texture(Util.PATH_PREFIX + "bunny.png");
        duck = new Texture(Util.PATH_PREFIX + "duck.png");
        kid = new Texture(Util.PATH_PREFIX + "kid.png");
        sink = new Texture(Util.PATH_PREFIX + "sink.png");
        oven = new Texture(Util.PATH_PREFIX + "oven.png");
        fridge = new Texture(Util.PATH_PREFIX + "fridge.png");
        font = new Font(Util.PATH_PREFIX + "font.ttf", 48, 512, 512);
        fontBig = new Font(Util.PATH_PREFIX + "font.ttf", 80, 1024, 1024);
        fontSmall = new Font(Util.PATH_PREFIX + "font.ttf", 28, 512, 256);
        console = new Console(font, boxRender);

        floors = new Floor.Group();
        floors.setTile((byte) 1, 0, 0);
        floors.setTile((byte) 1, -1, 0);
        floors.setTile((byte) 1, 0, -1);
        floors.setTile((byte) 1, -1, -1);
        floorRender.build(floors);
        gameObjects = new ArrayList<>();
        gameObjectDataMap = new HashMap<>();

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
                if(levelEdit) {
                    if(action == GLFW_PRESS && key == GLFW_KEY_TAB) {
                        float closestDist = 1.5f;
                        GameObject closest = null;
                        for(GameObject entity : gameObjects) {
                            if(mouseWorld.distance(entity.getCenter()) < closestDist) {
                                closestDist = mouseWorld.distance(entity.getCenter());
                                closest = entity;
                            }
                        }
                        if(closest != null) {
                            selectedGameObject = closest;
                        } else {
                            selectedGameObject = null;
                        }
                    }
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
                            initGameObjects();
                        }
                    }
                    if (selectedGameObject != null) {
                        if (action == GLFW_PRESS && key >= GLFW_KEY_1 && key <= GLFW_KEY_9) {
                            if (selectedGameObject instanceof Roomba) {
                                Roomba roomba = (Roomba) selectedGameObject;
                                Roomba.Waypoint newWaypoint = new Roomba.Waypoint();
                                newWaypoint.position = mouseWorld;
                                newWaypoint.radius = key - GLFW_KEY_0;
                                roomba.waypoints.add(newWaypoint);
                                gameObjectDataMap.get(roomba).metadata = new Roomba.InitMetadata(roomba.waypoints);
                            }
                        } else if (action == GLFW_PRESS && key == GLFW_KEY_0) {
                            if (selectedGameObject instanceof Roomba) {
                                Roomba roomba = (Roomba) selectedGameObject;
                                if (roomba.waypoints.size() > 0) {
                                    roomba.waypoints.remove(roomba.waypoints.size() - 1);
                                    gameObjectDataMap.get(roomba).metadata = new Roomba.InitMetadata(roomba.waypoints);
                                }
                            }
                        }
                    } else {
                        if (action == GLFW_PRESS && key == GLFW_KEY_0) {
                            Floor.InitData entity = new Floor.InitData();
                            entity.type = Floor.EntityType.PLAYER.val;
                            entity.position = mouseWorld;
                            floors.entityData.add(entity);
                            initGameObjects();
                        }
                        if (action == GLFW_PRESS && key == GLFW_KEY_1) {
                            Floor.InitData entity = new Floor.InitData();
                            entity.type = Floor.EntityType.ROOMBA.val;
                            entity.position = mouseWorld;
                            floors.entityData.add(entity);
                            initGameObjects();
                        }
                        if (action == GLFW_PRESS && key == GLFW_KEY_2) {
                            Floor.InitData entity = new Floor.InitData();
                            entity.type = Floor.EntityType.SOFA.val;
                            entity.position = mouseWorld;
                            floors.entityData.add(entity);
                            initGameObjects();
                        }
                        if (action == GLFW_PRESS && key == GLFW_KEY_3) {
                            Floor.InitData entity = new Floor.InitData();
                            entity.type = Floor.EntityType.TOY0.val;
                            entity.position = mouseWorld;
                            floors.entityData.add(entity);
                            initGameObjects();
                        }
                        if (action == GLFW_PRESS && key == GLFW_KEY_4) {
                            Floor.InitData entity = new Floor.InitData();
                            entity.type = Floor.EntityType.TOY1.val;
                            entity.position = mouseWorld;
                            floors.entityData.add(entity);
                            initGameObjects();
                        }
                        if (action == GLFW_PRESS && key == GLFW_KEY_5) {
                            Floor.InitData entity = new Floor.InitData();
                            entity.type = Floor.EntityType.TOY2.val;
                            entity.position = mouseWorld;
                            floors.entityData.add(entity);
                            initGameObjects();
                        }
                        if (action == GLFW_PRESS && key == GLFW_KEY_6) {
                            Floor.InitData entity = new Floor.InitData();
                            entity.type = Floor.EntityType.SINK.val;
                            entity.position = mouseWorld;
                            floors.entityData.add(entity);
                            initGameObjects();
                        }
                        if (action == GLFW_PRESS && key == GLFW_KEY_7) {
                            Floor.InitData entity = new Floor.InitData();
                            entity.type = Floor.EntityType.OVEN.val;
                            entity.position = mouseWorld;
                            floors.entityData.add(entity);
                            initGameObjects();
                        }
                        if (action == GLFW_PRESS && key == GLFW_KEY_8) {
                            Floor.InitData entity = new Floor.InitData();
                            entity.type = Floor.EntityType.FRIDGE.val;
                            entity.position = mouseWorld;
                            floors.entityData.add(entity);
                            initGameObjects();
                        }
                    }
                } else if(!updateGameObjects) {
                    if(glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
                        updateGameObjects = true;
                        initGameObjects();
                        initPlayer();
                        currentOverlay = OverlayMode.NONE;
                    }
                } else if(updateGameObjects) {
                    if(glfwGetKey(window, GLFW_KEY_1) == GLFW_PRESS) {
                        loadLevel("level1.dat");
                    } else if(glfwGetKey(window, GLFW_KEY_2) == GLFW_PRESS) {
                        loadLevel("level2.dat");
                    } else if(glfwGetKey(window, GLFW_KEY_3) == GLFW_PRESS) {
                        loadLevel("level1.dat");
                    }
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

        loadLevel("level1.dat");

        double soundTimer = 3 * 60;

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

            soundPlayer.update((float) delta);

            if(updateGameObjects) {
                gameTimer += delta;
            }
            soundTimer += delta;
            if(soundTimer > 2 * 60 + 12) {
                soundTimer = 0;
                soundPlayer.play(Sound.MUSIC);
            }

            if(delta > 0.1f) delta = 0.1f;

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
                            initGameObjects();
                        }
                    } else if (args[0].equals("ugo")) {
                        this.updateGameObjects = !this.updateGameObjects;
                        console.println("Update game objects: " + this.updateGameObjects);
                        this.initGameObjects();
                        if(this.updateGameObjects) {
                            this.initPlayer();
                        }
                    } else if(args[0].equals("setreq") && args.length == 2) {
                        this.floors.toysRequired = Integer.parseInt(args[1]);
                        this.toysRequired = floors.toysRequired;
                    } else if(args[0].equals("edit")) {
                        this.levelEdit = !this.levelEdit;
                        console.println("Level editor: " + levelEdit);
                    } else {
                        console.println("Unknown command!");
                    }
                } else {
                    console.println(cmd);
                }
            }
            console.commands.clear();

            if(!console.focus && levelEdit) {
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

                if(updateGameObjects) {
                    List<GameObject> toDelete = new ArrayList<>();
                    for (GameObject gameObject : gameObjects) {
                        if(gameObject.shouldDelete()) {
                            toDelete.add(gameObject);
                        } else {
                            gameObject.update(timeStep, this);
                        }
                    }
                    for(GameObject gameObject : toDelete) {
                        gameObjects.remove(gameObject);
                    }

                    for (GameObject object : gameObjects) {
                        if (object.colliderPriority() < Integer.MAX_VALUE) {
                            ArrayList<Convex> wall = new ArrayList<>();
                            int minX = (int) Math.floor(object.getCollider().getCenter().x) - 1, maxX = (int) Math.ceil(object.getCollider().getCenter().x) + 1;
                            int minY = (int) Math.floor(object.getCollider().getCenter().y) - 1, maxY = (int) Math.ceil(object.getCollider().getCenter().y) + 1;
                            for (int y = minY; y <= maxY; ++y) {
                                for (int x = minX; x <= maxX; ++x) {
                                    if (floors.getTile(x, y) == Floor.Tile.EMPTY.val) {
                                        Box b = new Box(x + 0.5f, y + 0.5f, 1.0f, 1.0f);
                                        wall.add(b);
                                        if(object.getCollider().intersects(b)) {
                                            object.collide(b, this);
                                        }
                                    }
                                }
                            }
                            for (GameObject obj : gameObjects) {
                                if (obj != object && obj.colliderPriority() >= object.colliderPriority()) {
                                    wall.add(obj.getCollider());
                                }
                                if(obj != object && obj.getCollider().intersects(object.getCollider())) {
                                    object.collide(obj, this);
                                    obj.collide(object, this);
                                }
                            }
                            Vector2f resolve = object.getCollider().smallestResolution(wall);
                            object.getCollider().move(resolve.x, resolve.y);
                        }
                    }

                    if(toysRequired <= toysPossessed) {
                        updateGameObjects = false;
                        currentOverlay = OverlayMode.VICTORY;
                    } else if(player.fallTime <= 0 && player.fallen) {
                        updateGameObjects = false;
                        currentOverlay = OverlayMode.DEFEAT;
                    }
                } else {
                    if(levelEdit) {
                        player.update(timeStep, this);
                    }
                }

                screenX = player.getCenter().x;
                screenY = player.getCenter().y;
            } while(delta >= 0 && false);

            glClear(GL_COLOR_BUFFER_BIT);

            setViewMatrix();

            Game.checkGLError("Main start");

            floorRender.draw(new Matrix4f(ortho).mul(view));

//            for(Floor.InitData entity : floors.entityData) {
//                if(entity.type == Floor.EntityType.PLAYER.val) {
//                    kid.bind();
//                    texRender.draw(new Matrix4f(ortho).mul(view).translate(entity.position.x, entity.position.y - 0.3f, 0).scale(1.0f, 1.0f, 0), new Vector4f(1, 1, 1, 1));
//                }
////                else if(entity.type == Floor.EntityType.ROOMBA.val) {
////                    roomba.bind();
////                    texRender.draw(new Matrix4f(ortho).mul(view).translate(entity.position.x, entity.position.y, 0).scale(1.0f, 1.0f, 0), new Vector4f(1, 1, 1, 1));
////                }
//            }
            if(!updateGameObjects && playerStart != null && levelEdit) {
                kid.bind();
                texRender.draw(new Matrix4f(ortho).mul(view).translate(playerStart.x, playerStart.y - 0.45f, 0), new Vector4f(1));
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

            if(selectedGameObject != null) {
                Vector2f position = selectedGameObject.getCenter();
                scrap.bind();
                texRender.draw(new Matrix4f(ortho).mul(view).translate(position.x, position.y, 0).rotate((float) Math.PI / 4.0f, 0, 0, 1).scale(1, 0.05f, 0), new Vector4f(0, 1, 0, 1));
                texRender.draw(new Matrix4f(ortho).mul(view).translate(position.x, position.y, 0).rotate((float) -Math.PI / 4.0f, 0, 0, 1).scale(1, 0.05f, 0), new Vector4f(0, 1, 0, 1));

                if(selectedGameObject instanceof Roomba) {
                    Roomba roomba = (Roomba) selectedGameObject;
                    for(Roomba.Waypoint waypoint : roomba.waypoints) {
                        texRender.draw(new Matrix4f(ortho).mul(view).translate(waypoint.position.x, waypoint.position.y, 0).rotate((float) Math.PI / 2f, 0, 0, 1).scale(1, 0.05f, 0), new Vector4f(0, 1, 0, 1));
                        texRender.draw(new Matrix4f(ortho).mul(view).translate(waypoint.position.x, waypoint.position.y, 0).rotate((float) 0, 0, 0, 1).scale(1, 0.05f, 0), new Vector4f(0, 1, 0, 1));
                        boxRender.draw(new Matrix4f(ortho).mul(view).translate(waypoint.position.x, waypoint.position.y, 0).scale(waypoint.radius * 2), new Vector4f(0, 0.1f, 0, 0.1f));
                    }
                }
            }

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
                    texRender.draw(new Matrix4f(ortho).mul(view).translate(position.x, position.y, 0).rotate((float) Math.PI / 4.0f, 0, 0, 1).scale(1, 0.05f, 0), new Vector4f(1, 0, 0, 1));
                    texRender.draw(new Matrix4f(ortho).mul(view).translate(position.x, position.y, 0).rotate((float) -Math.PI / 4.0f, 0, 0, 1).scale(1, 0.05f, 0), new Vector4f(1, 0, 0, 1));
                }
            }

            if(currentOverlay == OverlayMode.VICTORY) {
                boxRender.draw(new Matrix4f(ortho).translate(pixelWidth / 2.0f, pixelHeight / 2.0f, 0)
                    .scale(400), new Vector4f(1));
                fontBig.draw("Victory!", pixelWidth / 2.0f - fontBig.textWidth("Victory!") / 2 + 16.0f, pixelHeight / 2.0f, new Matrix4f(ortho), new Vector4f(0, 0, 0, 1));
                font.draw("Time: " + new DecimalFormat("0.00").format(gameTimer), pixelWidth / 2.0f - 80, pixelHeight / 2.0f + 50.0f, new Matrix4f(ortho), new Vector4f(0, 0, 0, 1));
                fontSmall.draw("Press space to play again...", pixelWidth / 2.0f - fontSmall.textWidth("Press space to play again...") / 2.0f + 16.0f, pixelHeight / 2.0f + 160, new Matrix4f(ortho), new Vector4f(0, 0, 0, 1));
            }
            if(currentOverlay == OverlayMode.DEFEAT) {
                boxRender.draw(new Matrix4f(ortho).translate(pixelWidth / 2.0f, pixelHeight / 2.0f, 0)
                    .scale(400), new Vector4f(1));
                fontBig.draw("Defeat!", pixelWidth / 2.0f - fontBig.textWidth("Defeat!") / 2 + 16.0f, pixelHeight / 2.0f, new Matrix4f(ortho), new Vector4f(0,0 ,0, 1));
                font.draw("Time: " + new DecimalFormat("0.00").format(gameTimer), pixelWidth / 2.0f - 80, pixelHeight / 2.0f + 50.0f, new Matrix4f(ortho), new Vector4f(0, 0, 0, 1));font.draw("Time: " + new DecimalFormat("0.00").format(gameTimer), pixelWidth / 2.0f - 80, pixelHeight / 2.0f + 50.0f, new Matrix4f(ortho), new Vector4f(0, 0, 0, 1));
                fontSmall.draw("Press space to play again...", pixelWidth / 2.0f - fontSmall.textWidth("Press space to play again...") / 2.0f + 16.0f, pixelHeight / 2.0f + 160, new Matrix4f(ortho), new Vector4f(0, 0, 0, 1));
            }

            {
                String txt = "Toys: " + toysPossessed + " / " + toysRequired;
                font.draw(txt, pixelWidth - font.textWidth(txt) - 10.0f, 40, ortho, new Vector4f(1));
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
        soundPlayer.cleanUp();
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
