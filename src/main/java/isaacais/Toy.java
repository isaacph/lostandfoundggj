package isaacais;

import org.joml.Vector2f;

public class Toy extends TextureGameObject {

    private boolean delete = false;
    public int number;

    public Toy(TextureRenderer render, Texture texture, Vector2f position, Vector2f colliderSize, Vector2f renderOffset, float scale, int toyNumber) {
        super(render, texture, new Box(
            position.x, position.y, colliderSize.x, colliderSize.y
        ), new Box(
            position.x + renderOffset.x, position.y + renderOffset.y, scale
        ), 0);
        this.number = toyNumber;
    }

//    public void update(double delta, Game game) {
//
//    }

    @Override
    public void collide(GameObject other, Game game) {
        if(other instanceof Player) {
            this.delete = true;
        }
    }

    @Override
    public int colliderPriority() {
        return 0;
    }

    @Override
    public boolean shouldDelete() {
        return delete;
    }
}
