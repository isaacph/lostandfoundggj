package isaacais;

import org.joml.Vector2f;

public class Player extends TextureGameObject {

    public Player(TextureRenderer texRender, Texture texture, Vector2f position) {
        super(texRender, texture,
            new Box(position.x, position.y, 0.4f, 0.2f),
            new Box(position.x, position.y - 0.45f, 1.0f));
    }

    @Override
    public int colliderPriority() {
        return 1;
    }
}
