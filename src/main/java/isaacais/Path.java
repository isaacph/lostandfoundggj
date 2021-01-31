package isaacais;

import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.*;

public class Path {

//    public ArrayList<Vector2f> step;
//
//    public Path() {
//        step = new ArrayList<>();
//    }
//
    public static Vector2i nearestTile(Vector2f position) {
        return new Vector2i((int) Math.floor(position.x), (int) Math.floor(position.y));
    }

    public static void rotateClockwise(Vector2i v) {
        int temp = -v.x;
        v.x = v.y;
        v.y = temp;
    }

    public static int mhMaxDist(Vector2i a, Vector2i b) {
        return Math.max(a.x - b.x, a.y - b.y);
    }

    public static boolean tileFree(Vector2i position, Game game) {
        if(game.floors.getTile(position.x, position.y) == Floor.Tile.EMPTY.val) return false;
        return game.getObjectsInTiles(position, position).size() == 0;
    }

    public static Vector2i nearestFreeTile(Vector2i position, Game game) {
        Vector2i current = new Vector2i(position);
        Vector2i direction = new Vector2i(1, 0);
        int num = 0, next = 1, nextAcc = 1;
        boolean nextFlag = true;
        while(!tileFree(current, game) && num < 10000) {
            if(num == next) {
                rotateClockwise(direction);
                next += nextAcc;
                if(nextFlag) {
                    nextAcc++;
                }
                nextFlag = !nextFlag;
            }
            current.add(direction);
            ++num;
        }
        if(num >= 10000) {
            current = new Vector2i(0);
        }
        return current;
    }

    public static Vector2i nextMove(Vector2f position, Vector2i destination, Convex mover, Game game) {
        return destination;/*
        Map<Vector2i, Float> shortestDist = new HashMap<>();
        Vector2f currentPos = new Vector2f(position);
        while(true) {
            List<Vector2i> inRange = floodFill(position, game);
        }*/
    }

    public static List<Vector2i> floodFill(Vector2f v, Game game) {
        return null;
    }
//
//    public static Path createPath(Vector2f start, Vector2f stop, Floor.Group floors) {
//        Path path = new Path();
//
//        Vector2i begin = nearestFreeTile(nearestTile(start), floors);
//        Vector2i end = nearestFreeTile(nearestTile(stop), floors);
//
//        Queue<Vector2i> queue = new ArrayDeque<>
//        ();
//        queue.add(end);
//
//        return path;
//    }
}
