package isaacais;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Box extends Convex{

    public float x, y;
    public float width, height;
    public float rotation;

    public Box(float x, float y, float width, float height, float rotation) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
    }

    public Box(float x, float y, float width, float height) {
        this(x, y, width, height, 0);
    }

    public Box(float x, float y, float scale) {
        this(x, y, scale, scale);
    }

    public Box(Box other) {
        this(other.x, other.y, other.width, other.height, other.rotation);
    }

    public Matrix4f getModelMatrix() {
        return new Matrix4f().translate(x, y, 0).rotate(rotation, 0, 0, 1).scale(width, height, 0);
    }

    @Override
    public Vector2f[] points() {
        Vector2f p = new Vector2f(x, y);
        Vector2f w = new Vector2f((float) Math.cos(rotation) * width / 2, (float) Math.sin(rotation) * width / 2);
        Vector2f h = new Vector2f((float) Math.cos(rotation + Math.PI / 2) * height / 2, (float) Math.sin(rotation + Math.PI / 2) * height / 2);
        return new Vector2f[] {
            new Vector2f(p).add(w).add(h),
            new Vector2f(p).add(w).sub(h),
            new Vector2f(p).sub(w).sub(h),
            new Vector2f(p).sub(w).add(h)
        };
    }
    @Override
    public Vector2f[] axes() {
        return new Vector2f[] {
            new Vector2f((float) Math.cos(rotation), (float) Math.sin(rotation)),
            new Vector2f((float) Math.cos(rotation + Math.PI / 2), (float) Math.sin(rotation + Math.PI / 2)),
            new Vector2f((float) Math.cos(rotation + Math.PI), (float) Math.sin(rotation + Math.PI)),
            new Vector2f((float) Math.cos(rotation + 3 * Math.PI / 2), (float) Math.sin(rotation + Math.PI / 2 * 3)),
        };
    }

    public Vector2f getCenter() {
        return new Vector2f(x, y);
    }

    public void move(float x, float y) {
        this.x += x;
        this.y += y;
    }

    @Override
    public Convex add(Vector2f displacement) {
        Box b = new Box(this);
        b.x += displacement.x;
        b.y += displacement.y;
        return b;
    }

    @Override
    public Convex withCenter(Vector2f position) {
        Box b = new Box(this);
        b.x = position.x;
        b.y = position.y;
        return b;
    }
}
