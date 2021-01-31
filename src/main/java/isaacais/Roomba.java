package isaacais;

import org.joml.Vector2f;

import java.io.Serializable;
import java.util.ArrayList;

public class Roomba extends TextureGameObject {

    public ArrayList<Waypoint> waypoints = new ArrayList<>();

    public Roomba(TextureRenderer render, Texture texture, Vector2f position, InitMetadata data) {
        super(render, texture, new Box(
            position.x, position.y, 0.6f
        ), new Box(
            position.x, position.y, 1
        ), 0);
        if(data != null && data.waypoints != null) {
            this.waypoints = new ArrayList<>(data.waypoints);
        }
    }

    @Override
    public void update(double delta, Game game) {

    }

    public static class InitMetadata implements Serializable {
        public ArrayList<Waypoint> waypoints;
        public InitMetadata(ArrayList<Waypoint> waypoints) {
            this.waypoints = new ArrayList<>(waypoints);
        }
    }

    public static class Waypoint implements Serializable {
        public Vector2f position;
        public float radius;
    }

    @Override
    public int colliderPriority() {
        return 2;
    }
}
