package isaacais;

import org.joml.Matrix4f;

public abstract class GameObject implements Comparable<GameObject> {

    public abstract Convex getCollider();

    public abstract void draw(Matrix4f orthoProj);

    public abstract float depth();

    public int compareTo(GameObject other) {
        return Float.compare(depth(), other.depth());
    }

    public void update(double delta, Game game) {}
}
