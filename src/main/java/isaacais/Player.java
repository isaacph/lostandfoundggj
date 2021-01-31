package isaacais;

import org.joml.Vector2f;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class Player extends TextureGameObject {

    public static final float FALL_DURATION = 2.0f;
    private float fallTime = 0;
    public Set<Object> toysCollected = new HashSet<>();

//    public boolean[] hasToy = new boolean[3];

    public Player(TextureRenderer texRender, Texture texture, Vector2f position) {
        super(texRender, texture,
            new Box(position.x, position.y, 0.4f, 0.2f),
            new Box(position.x, position.y - 0.45f, 1.0f));
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
        }

        if(fallTime <= 0) {
            move(move.x, move.y);
        } else {
            fallTime -= (float) delta;
        }
    }

    @Override
    public void collide(GameObject other, Game game) {
        if(other instanceof Roomba && fallTime <= 0) {
            fallTime = FALL_DURATION;
        } else if(other instanceof Toy && fallTime <= 0) {
            if(!toysCollected.contains(other)) {
                toysCollected.add(other);
                game.toysPossessed++;
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
