package isaacais;

import org.joml.Matrix4f;
import org.joml.Vector2f;

public abstract class GameObject implements Comparable<GameObject> {

    public abstract Convex getCollider();

    public abstract void draw(Matrix4f orthoProj);

    public abstract float depth();

    public int compareTo(GameObject other) {
        return Float.compare(depth(), other.depth());
    }

    public void update(double delta, Game game) {}

    public int colliderPriority() {
        return Integer.MAX_VALUE;
    }

    public abstract Vector2f getCenter();
}
