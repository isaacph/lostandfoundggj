package isaacais;

public class Shadow {

    public float min, max;

    public Shadow(float a, float b) {
        this.min = Math.min(a, b);
        this.max = Math.max(a, b);
    }

    public Shadow(float... points) {
        min = Float.MAX_VALUE;
        max = Float.MIN_VALUE;
        for(float p : points) {
            min = Math.min(min, p);
            max = Math.max(max, p);
        }
    }

    public boolean intersects(Shadow other) {
        return min <= other.min && other.min <= max ||
            min <= other.max && other.max <= max ||
            other.min <= min && min <= other.max ||
            other.min <= max && max <= other.max;
    }

    public float[] resolutions(Shadow other) {
        return new float[] {
            other.min - max - 0.001f,
            other.max - min + 0.001f,
        };
    }
}
