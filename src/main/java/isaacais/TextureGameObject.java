package isaacais;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class TextureGameObject extends GameObject {

    protected TextureRenderer render;
    protected Texture texture;
    protected Convex collider;
    protected Box drawObject;
    protected Vector2f offset = new Vector2f();
    protected float depthOffset = 0;

    public TextureGameObject(TextureRenderer render, Texture texture, Convex collider, Box drawObject, float depthOffset) {
        this.render = render;
        this.texture = texture;
        this.drawObject = drawObject;
        this.collider = collider;
        offset = new Vector2f(drawObject.x - collider.getCenter().x, drawObject.y - collider.getCenter().y);
        this.depthOffset = depthOffset;
    }

    public TextureGameObject(TextureRenderer render, Texture texture, Convex collider, Box drawObject) {
        this(render, texture, collider, drawObject, 0);
    }

    @Override
    public Convex getCollider() {
        return collider;
    }

    public void draw(Matrix4f orthoProj) {
        this.drawObject.x = collider.getCenter().x + offset.x;
        this.drawObject.y = collider.getCenter().y + offset.y;
        texture.bind();
        render.draw(new Matrix4f(orthoProj).mul(drawObject.getModelMatrix()), new Vector4f(1));
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
}
