package isaacais;

import org.joml.Vector2f;

public class Roomba extends TextureGameObject {

    public Roomba(TextureRenderer render, Texture texture, Vector2f position) {
        super(render, texture, new Box(
            position.x, position.y, 0.6f
        ), new Box(
            position.x, position.y, 1
        ), 0);
    }

    @Override
    public void update(double delta, Game game) {

    }
}
