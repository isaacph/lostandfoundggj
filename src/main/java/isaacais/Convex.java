package isaacais;

import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Convex {

    public abstract Vector2f[] points();
    public abstract Vector2f[] axes();
    public abstract Convex add(Vector2f displacement);

    public Shadow shadow(Vector2f axis) {
        return project(axis, points());
    }

    public boolean intersects(Convex other) {
        ArrayList<Vector2f> axes = new ArrayList<>(Arrays.asList(axes()));
        axes.addAll(Arrays.asList(other.axes()));
        for(Vector2f axis : axes) {
            Shadow a = shadow(axis);
            Shadow b = other.shadow(axis);
            if(!a.intersects(b)) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<Vector2f> resolveOptions(Convex other) {
        ArrayList<Vector2f> axes = new ArrayList<>(Arrays.asList(axes()));
        axes.addAll(Arrays.asList(other.axes()));
        ArrayList<Vector2f> options = new ArrayList<>();
        for(Vector2f axis : axes) {
            Shadow a = shadow(axis);
            Shadow b = other.shadow(axis);
            options.addAll(Arrays.asList(Convex.mul(a.resolutions(b), axis)));
        }
        return options;
    }

    public <T extends Convex> Vector2f smallestResolution(List<T> convexes) {
        ArrayList<Vector2f> resolutions = new ArrayList<>();
        int bestResolutionCollisions = 0;
        for(Convex c : convexes) {
            if(intersects(c)) {
                resolutions.addAll(resolveOptions(c));
                ++bestResolutionCollisions;
            }
        }
        int step1Size = resolutions.size();
        for(int i = 0; i < step1Size; ++i) {
            Convex b = add(resolutions.get(i));
            for (Convex b2 : convexes) {
                if(b.intersects(b2)) {
                    ArrayList<Vector2f> options = b.resolveOptions(b2);
                    for(Vector2f v : options) {
                        v.x += resolutions.get(i).x;
                        v.y += resolutions.get(i).y;
                    }
                    resolutions.addAll(options);
                }
            }
        }
        Vector2f bestResolution = new Vector2f(0);
        float bestResolutionMagSq = 0;
        for(int i = 0; i < resolutions.size(); ++i) {
            int collisions = 0;
            Convex option = add(resolutions.get(i));
            for(Convex b : convexes) {
                if(option.intersects(b)) {
                    ++collisions;
                }
            }
            if(collisions < bestResolutionCollisions
                || (collisions == bestResolutionCollisions && resolutions.get(i).lengthSquared() < bestResolutionMagSq)) {
                bestResolution = resolutions.get(i);
                bestResolutionCollisions = collisions;
                bestResolutionMagSq = bestResolution.lengthSquared();
            }
        }
        return bestResolution;
    }

//    public static Vector2f project(Vector2f axis, Vector2f axisStart, Vector2f point) {
//        return new Vector2f(axisStart).add(new Vector2f(axis).mul(axis.dot(point)/axis.dot(axis)));
//    }
    public static Vector2f project(Vector2f axis, Vector2f axisStart, Vector2f point) {
        return new Vector2f(axis).mul(1.0f / axis.length()).mul(projMag(axis, point)).add(axisStart);
    }

    public static float projMag(Vector2f axis, Vector2f point) {
        if(axis.equals(0, 0)) {
            return 0;
        }
        return new Vector2f(axis.x, axis.y).normalize().dot(new Vector2f(point.x, point.y));
    }

    public static Shadow project(Vector2f axis, Vector2f[] points) {
        float[] shadow = new float[points.length];
        for(int i = 0; i < points.length; ++i) {
            shadow[i] = projMag(axis, points[i]);
        }
        return new Shadow(shadow);
    }

    public static Vector2f[] mul(float[] scalars, Vector2f axis) {
        Vector2f[] list = new Vector2f[scalars.length];
        for(int i = 0; i < scalars.length; ++i) {
            list[i] = new Vector2f(axis).normalize().mul(scalars[i]);
        }
        return list;
    }
}
