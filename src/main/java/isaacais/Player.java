package isaacais;

import org.joml.Vector2f;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class Player extends SpriteGameObject {


    public static final float FALL_DURATION = 37.0f / 20.0f;
    public boolean fallen = false;
    public float fallTime = 0;
    public Set<Object> toysCollected = new HashSet<>();
    public Texture side = new Texture(Util.PATH_PREFIX + "kid1.png");
    public Texture front = new Texture(Util.PATH_PREFIX + "kid2.png");
    public Texture back = new Texture(Util.PATH_PREFIX + "kid3.png");
    public Texture fall = new Texture(Util.PATH_PREFIX + "kidfall.png");
    public Direction direction = Direction.DOWN;

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public List<Frame> frontWalk = new ArrayList<>(Arrays.asList(
        new Frame(0/512.0f, 0/512.0f, 96/512.0f, 96/512.0f),
        new Frame(96/512.0f, 0/512.0f, 96/512.0f, 96/512.0f),
        new Frame(192/512.0f, 0/512.0f, 96/512.0f, 96/512.0f),
        new Frame(0/512.0f, 96/512.0f, 96/512.0f, 96/512.0f),
        new Frame(96/512.0f, 96/512.0f, 96/512.0f, 96/512.0f),
        new Frame(192/512.0f, 96/512.0f, 96/512.0f, 96/512.0f),
        new Frame(0/512.0f, 192/512.0f, 96/512.0f, 96/512.0f),
        new Frame(96/512.0f, 192/512.0f, 96/512.0f, 96/512.0f),
        new Frame(192/512.0f, 192/512.0f, 96/512.0f, 96/512.0f),
        new Frame(0/512.0f, 288/512.0f, 96/512.0f, 96/512.0f),
        new Frame(96/512.0f, 288/512.0f, 96/512.0f, 96/512.0f)
    ));

    public List<Frame> backWalk = new ArrayList<>(Arrays.asList(
        new Frame(0/512.0f, 0/512.0f, 96/512.0f, 96/512.0f),
        new Frame(96/512.0f, 0/512.0f, 96/512.0f, 96/512.0f),
        new Frame(192/512.0f, 0/512.0f, 96/512.0f, 96/512.0f),
        new Frame(0/512.0f, 96/512.0f, 96/512.0f, 96/512.0f),
        new Frame(96/512.0f, 96/512.0f, 96/512.0f, 96/512.0f),
        new Frame(192/512.0f, 96/512.0f, 96/512.0f, 96/512.0f),
        new Frame(0/512.0f, 192/512.0f, 96/512.0f, 96/512.0f),
        new Frame(96/512.0f, 192/512.0f, 96/512.0f, 96/512.0f),
        new Frame(192/512.0f, 192/512.0f, 96/512.0f, 96/512.0f),
        new Frame(0/512.0f, 288/512.0f, 96/512.0f, 96/512.0f),
        new Frame(96/512.0f, 288/512.0f, 96/512.0f, 96/512.0f)
    ));

    public List<Frame> sideWalk = new ArrayList<>(Arrays.asList(
        new Frame(0/512.0f, 0/512.0f, 96/512.0f, 96/512.0f),
        new Frame(96/512.0f, 0/512.0f, 96/512.0f, 96/512.0f),
        new Frame(192/512.0f, 0/512.0f, 96/512.0f, 96/512.0f),
        new Frame(288/512.0f, 0/512.0f, 96/512.0f, 96/512.0f),
        new Frame(0/512.0f, 96/512.0f, 96/512.0f, 96/512.0f),
        new Frame(96/512.0f, 96/512.0f, 96/512.0f, 96/512.0f),
        new Frame(192/512.0f, 96/512.0f, 96/512.0f, 96/512.0f),
        new Frame(288/512.0f, 96/512.0f, 96/512.0f, 96/512.0f),
        new Frame(0/512.0f, 192/512.0f, 96/512.0f, 96/512.0f),
        new Frame(96/512.0f, 192/512.0f, 96/512.0f, 96/512.0f),
        new Frame(192/512.0f, 192/512.0f, 96/512.0f, 96/512.0f),
        new Frame(288/512.0f, 192/512.0f, 96/512.0f, 96/512.0f),
        new Frame(0/512.0f, 288/512.0f, 96/512.0f, 96/512.0f),
        new Frame(96/512.0f, 288/512.0f, 96/512.0f, 96/512.0f),
        new Frame(192/512.0f, 288/512.0f, 96/512.0f, 96/512.0f),
        new Frame(288/512.0f, 288/512.0f, 96/512.0f, 96/512.0f),
        new Frame(0/512.0f, 384/512.0f, 96/512.0f, 96/512.0f)
    ));

    public List<Frame> fallAnimation = new ArrayList<>(Arrays.asList(
        new Frame(0/512.0f, 0/512.0f, 96/512.0f, 96/512.0f),
        new Frame(96/512.0f, 0/512.0f, 96/512.0f, 96/512.0f),
        new Frame(192/512.0f, 0/512.0f, 96/512.0f, 96/512.0f),
        new Frame(288/512.0f, 0/512.0f, 96/512.0f, 96/512.0f),
        new Frame(0/512.0f, 96/512.0f, 96/512.0f, 96/512.0f),
        new Frame(96/512.0f, 96/512.0f, 96/512.0f, 96/512.0f),
        new Frame(192/512.0f, 96/512.0f, 96/512.0f, 96/512.0f),
        new Frame(288/512.0f, 96/512.0f, 96/512.0f, 96/512.0f),
        new Frame(0/512.0f, 192/512.0f, 96/512.0f, 96/512.0f),
        new Frame(96/512.0f, 192/512.0f, 96/512.0f, 96/512.0f),
        new Frame(192/512.0f, 192/512.0f, 96/512.0f, 96/512.0f),
        new Frame(288/512.0f, 192/512.0f, 96/512.0f, 96/512.0f),
        new Frame(0/512.0f, 288/512.0f, 96/512.0f, 96/512.0f),
        new Frame(96/512.0f, 288/512.0f, 96/512.0f, 96/512.0f),
        new Frame(192/512.0f, 288/512.0f, 96/512.0f, 96/512.0f),
        new Frame(288/512.0f, 288/512.0f, 96/512.0f, 96/512.0f),
        new Frame(0/512.0f, 384/512.0f, 96/512.0f, 96/512.0f)
    ));

    public float walkTimer = 0;

//    public boolean[] hasToy = new boolean[3];

    public Player(SpriteRenderer texRender, Vector2f position) {
        super(texRender, null,
            new Box(position.x, position.y, 0.5f, 0.3f),
            new Box(position.x, position.y - 0.3f, 1.4f),
            new Frame(new Vector2f(0), new Vector2f(96.0f/512)));
        this.texture = front;
    }

    @Override
    public void update(double delta, Game game) {
        Vector2f move = new Vector2f(0);
        if(!game.console.focus) {
            if(glfwGetKey(game.window, GLFW_KEY_W) == GLFW_PRESS) {
                --move.y;
            }
            if(glfwGetKey(game.window, GLFW_KEY_S) == GLFW_PRESS) {
                ++move.y;
            }
            if(glfwGetKey(game.window, GLFW_KEY_A) == GLFW_PRESS) {
                --move.x;
            }
            if(glfwGetKey(game.window, GLFW_KEY_D) == GLFW_PRESS) {
                ++move.x;
            }
        }

        if(!move.equals(0, 0)) {
            move.normalize((float) delta * 3.0f);
            if(move.x > 0) {
                if(direction == Direction.DOWN || direction == Direction.UP) {
                    walkTimer = 0;
                }
                direction = Direction.RIGHT;
            }
            else if(move.x < 0) {
                if(direction == Direction.DOWN || direction == Direction.UP) {
                    walkTimer = 0;
                }
                direction = Direction.LEFT;
            }
            else if(move.y > 0) {
                if(direction == Direction.LEFT || direction == Direction.RIGHT) {
                    walkTimer = 0;
                }
                direction = Direction.DOWN;
            }
            else if(move.y < 0) {
                if(direction == Direction.LEFT || direction == Direction.RIGHT) {
                    walkTimer = 0;
                }
                direction = Direction.UP;
            }
        } else {
            walkTimer = 0;
        }

        if(fallTime <= 0) {
            move(move.x, move.y);
            walkTimer += delta;
        } else {
            fallTime -= (float) delta;
            walkTimer = 0;
        }

        if(direction == Direction.LEFT) {
            drawObject.width = -Math.abs(drawObject.width);
        } else {
            drawObject.width = Math.abs(drawObject.width);
        }
        if(direction == Direction.LEFT || direction == Direction.RIGHT) {
            texture = side;
            int frame = (int) (walkTimer * 20) % sideWalk.size();
            currentFrame = sideWalk.get(frame);
        } else if(direction == Direction.DOWN) {
            texture = front;
            int frame = (int) (walkTimer * 20) % frontWalk.size();
            currentFrame = frontWalk.get(frame);
        } else if(direction == Direction.UP) {
            texture = back;
            int frame = (int) (walkTimer * 20) % backWalk.size();
            currentFrame = backWalk.get(frame);
        }

        if(fallTime > 0) {
            texture = fall;
            int frame = (int) ((FALL_DURATION - fallTime) * 20) % fallAnimation.size();
            currentFrame = fallAnimation.get(frame);
        }
    }

    @Override
    public void collide(GameObject other, Game game) {
        if(other instanceof Roomba && fallTime <= 0) {
            fallTime = FALL_DURATION;
            fallen = true;
            game.soundPlayer.play(Sound.DIE);
        } else if(other instanceof Toy && fallTime <= 0) {
            if(!toysCollected.contains(other)) {
                toysCollected.add(other);
                game.toysPossessed++;
                game.soundPlayer.play(Sound.COLLECT);
            }
        }
    }

    @Override
    public int colliderPriority() {
        return 1;
    }
//
//    public int countToys() {
//        int i = 0;
//        for(boolean b : hasToy) {
//            if(b) ++i;
//        }
//        return i;
//    }
}
