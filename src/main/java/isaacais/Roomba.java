package isaacais;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Roomba extends TextureGameObject {

    public static final float STOP_TIME = 0.3f;
    public static final float SPEED = 2.0f;
    public static final float CHASE_SPEED = 4.0f;

    public ArrayList<Waypoint> waypoints = new ArrayList<>();
    public float stopTimer;
//    public Vector2i goal;
    public Vector2i destination;
    public Status status;
    public Vector2f direction = new Vector2f();
    private Random random;
    public float playerCollideCooldown = 0;
    private Vector2f start;
    private float moveTimer = 0;
    public int moveBadStreak = 0;

    public Roomba(TextureRenderer render, Texture texture, Vector2f position, InitMetadata data) {
        super(render, texture, new Box(
            position.x, position.y, 0.7f, 0.4f
        ), new Box(
            position.x, position.y - 0.1f, 1
        ), 0);
        if(data != null && data.waypoints != null) {
            this.waypoints = new ArrayList<>(data.waypoints);
        }
        stopTimer = 0;
        status = Status.STOPPED;
        start = new Vector2f(position);
        this.random = new Random((int) (position.x * position.y * 1000));
    }

    @Override
    public void update(double delta, Game game) {
        if(waypoints.size() == 0) return;
        if(status == Status.STOPPED) {
            if(moveTimer > 0) {
                if(moveTimer < 0.3f) {
                    ++moveBadStreak;
                    if(moveBadStreak > 5) {
                        moveBadStreak = 0;
                        collider.move(start.x - collider.getCenter().x, start.y - collider.getCenter().y);
                    }
                } else {
                    moveBadStreak = 0;
                }
                moveTimer = 0;
            }
            stopTimer -= (float) delta;
            if(stopTimer <= 0) {
                status = Status.MOVING;
                int i = random.nextInt(waypoints.size());
                if(destination == null) {
                    destination = Path.nearestFreeTile(
                        Path.nearestTile(
                            new Vector2f(waypoints.get(i).position)
                                .add((random.nextFloat() * 2 - 1) * waypoints.get(i).radius,
                                    (random.nextFloat() * 2 - 1) * waypoints.get(i).radius)),
                        game);
                }
                Vector2i next = Path.nextMove(getCollider().getCenter(), destination, getCollider(), game);
                direction = new Vector2f(next.x, next.y).sub(getCollider().getCenter());
                if(direction.lengthSquared() == 0) {
                    direction = new Vector2f(1, 0);
                }
                direction.normalize();
            }
        } else if(status == Status.MOVING) {
            moveTimer += delta;
            if(destination != null && collider.intersects(new Box(destination.x + 0.5f, destination.y + 0.5f, 1))) {
                destination = null;
            }
            Vector2f move = new Vector2f(direction).mul((float) delta * SPEED);
            this.collider.move(move.x, move.y);
            if(playerCollideCooldown <= 0 && game.player.getCollider().getCenter().distance(this.getCollider().getCenter()) < 2.5f) {
                this.status = Status.CHASING;
                this.destination = null;
                game.soundPlayer.play(Sound.SPOTTED);
            }
        } else {
            if(playerCollideCooldown <= 0) {
                Vector2f move = new Vector2f(game.player.getCollider().getCenter()).sub(this.getCollider().getCenter()).normalize((float) delta * CHASE_SPEED);
                this.collider.move(move.x, move.y);
            } else {
                this.status = Status.STOPPED;
                this.stopTimer = STOP_TIME;
            }
        }
        playerCollideCooldown -= delta;
    }

    public void draw(Matrix4f ortho) {
        super.draw(ortho);

//        if(destination != null)
//        this.render.draw(new Matrix4f(ortho).translate(destination.x, destination.y, 0).scale(0.1f, 0.1f, 0), new Vector4f(1));
    }

    @Override
    public void collide(GameObject other, Game game) {
        if(other instanceof Player) {
            Player player = (Player) other;
            if(playerCollideCooldown >= 0) {
                return;
            } else {
                playerCollideCooldown = 5;
            }
        } else if(other instanceof Toy) {
            return;
        }
        status = Status.STOPPED;
        stopTimer = STOP_TIME;
        destination = null;
    }

    @Override
    public void collide(Box wall, Game game) {
        status = Status.STOPPED;
        stopTimer = STOP_TIME;
        destination = null;
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
        return 3;
    }

    public enum Status {
        STOPPED, MOVING, CHASING
    }
}
