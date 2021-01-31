package isaacais;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;

public class SpriteGameObject extends GameObject {

    protected SpriteRenderer render;
    protected Texture texture;
    protected Convex collider;
    protected Box drawObject;
    protected Vector2f offset = new Vector2f();
    protected float depthOffset = 0;
    protected Frame currentFrame;

    public SpriteGameObject(SpriteRenderer render, Texture texture, Convex collider, Box drawObject, float depthOffset, Frame frame) {
        this.render = render;
        this.texture = texture;
        this.drawObject = drawObject;
        this.collider = collider;
        offset = new Vector2f(drawObject.x - collider.getCenter().x, drawObject.y - collider.getCenter().y);
        this.depthOffset = depthOffset;
        this.currentFrame = frame;
    }

    public SpriteGameObject(SpriteRenderer render, Texture texture, Convex collider, Box drawObject, Frame frame) {
        this(render, texture, collider, drawObject, 0, frame);
    }

    @Override
    public Convex getCollider() {
        return collider;
    }

    public void draw(Matrix4f orthoProj) {
        this.drawObject.x = collider.getCenter().x + offset.x;
        this.drawObject.y = collider.getCenter().y + offset.y;
        texture.bind();
        render.draw(new Matrix4f(orthoProj).mul(drawObject.getModelMatrix()), new Vector4f(1), currentFrame.pos, currentFrame.frame);
    }

    public float depth() {
        return collider.getCenter().y + depthOffset;
    }

    public Vector2f getCenter() {
        this.drawObject.x = collider.getCenter().x + offset.x;
        this.drawObject.y = collider.getCenter().y + offset.y;
        return new Vector2f(drawObject.x, drawObject.y);
    }

    public void move(float x, float y) {
        this.collider.move(x, y);
    }

    public static class Frame {
        public Vector2f pos;
        public Vector2f frame;

        public Frame(Vector2f pos, Vector2f frame) {
            this.pos = pos;
            this.frame = frame;
        }
        public Frame(float x, float y, float width, float height) {
            this(new Vector2f(x, y), new Vector2f(width, height));
        }
    }
}
