package isaacais;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.io.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Floor implements Serializable {

    public static final int FLOOR_SIZE = 16;

    public int x, y;
    public byte[] data;

    public Floor(int x, int y) {
        this.x = x;
        this.y = y;
        data = new byte[FLOOR_SIZE * FLOOR_SIZE];
    }

    public byte get(int x, int y) {
        return data[y * FLOOR_SIZE + x];
    }
    public void set(byte b, int x, int y) {
        data[y * FLOOR_SIZE + x] = b;
    }

    public Matrix4f getMatrix() {
        return new Matrix4f().translate(x * FLOOR_SIZE, y * FLOOR_SIZE, 0).scale(1, 1, 0);
    }

    public static Floor.Group fromFile(String path) {
        try {
            ObjectInputStream stream = new ObjectInputStream(Util.readFile(path));
            return (Floor.Group) stream.readObject();
        } catch (Exception e) {
            System.err.println("Unable to read floor " + path);
            e.printStackTrace();
            return null;
        }
    }

    public static Floor.Group fromResource(String path) {
        try {
            InputStream stream = Texture.class.getResourceAsStream(path);
            ObjectInputStream s = new ObjectInputStream(stream);
            return (Floor.Group) s.readObject();
        } catch (Exception e) {
            System.err.println("Unable to read floor " + path);
            e.printStackTrace();
            return null;
        }
    }
    public static void toFile(Floor.Group floors, String path) {
        try {
            ObjectOutputStream stream = new ObjectOutputStream(Util.writeFile(path));
            stream.writeObject(floors);
            stream.close();
            System.out.println("Saved floors");
        } catch (Exception e) {
            System.err.println("Unable to write floor " + path);
            e.printStackTrace();
        }
    }

    public static class Group implements Serializable {
        public HashMap<Vector2i, Floor> floors;
        public List<InitData> entityData;

        public int toysRequired;
        public Group() {
            floors = new HashMap<>();
            entityData = new ArrayList<>();
            toysRequired = 0;
        }
        public void setTile(byte b, int x, int y) {
            Vector2i p = getFloorIndex(x, y);
            Floor f = floors.get(p);
            if(f == null) {
                f = new Floor(p.x, p.y);
                floors.put(p, f);
            }
            f.set(b, ((x % FLOOR_SIZE) + FLOOR_SIZE) % FLOOR_SIZE, ((y % FLOOR_SIZE) + FLOOR_SIZE) % FLOOR_SIZE);
        }
        public byte getTile(int x, int y) {
            Floor f = floors.get(getFloorIndex(x, y));
            if(f == null) return 0;
            return f.get(((x % FLOOR_SIZE) + FLOOR_SIZE) % FLOOR_SIZE, ((y % FLOOR_SIZE) + FLOOR_SIZE) % FLOOR_SIZE);
        }
        public Vector2i getFloorIndex(int x, int y) {
            return new Vector2i((int) Math.floor((double) x / FLOOR_SIZE), (int) Math.floor((double) y / FLOOR_SIZE));
        }
    }

    public enum Tile {
        EMPTY(0), FILLED(1);

        public byte val;
        Tile(int value) {
            this.val = (byte) value;
        }
    }

    public enum EntityType {
        NULL(0), PLAYER(1), ROOMBA(2), SOFA(3), TOY0(4), TOY1(5), TOY2(6);

        public byte val;
        EntityType(int val) {
            this.val = (byte) val;
        }
    }

    public static class InitData implements Serializable {
        public byte type = EntityType.NULL.val;
        public Vector2f position = new Vector2f(0);
        public Object metadata = null;
    }
}
