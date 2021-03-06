package isaacais;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;

public class Console {

    public ArrayList<String> lines;
    public StringBuffer typing;
    private Font font;
    private BoxRenderer boxRender;

    public int displayLines = 7;
    public float x = 0, y = 40;
    public float jump = 40;
    public float width = 500;
    public boolean visible = false;
    public boolean focus = false;
    public static final float FOCUS_TIME = 5;
    public static final int CAPACITY = 40;
    public float focusTimer = 0;
    public float lineTimer = 0;

    public ArrayList<String> commands = new ArrayList<>();

    public Console(Font f, BoxRenderer b) {
        lines = new ArrayList<>();
        typing = new StringBuffer();
        font = f;
        boxRender = b;
    }

    public void update(double delta) {
        if(visible) {
            if(!focus) {
                focusTimer -= delta;
                if(focusTimer < 0) {
                    visible = false;
                }
            } else {
                focusTimer = FOCUS_TIME;
                lineTimer += delta;
            }
        }
    }

    public void draw(Matrix4f ortho) {
        if(visible) {
            boxRender.draw(new Matrix4f(ortho).translate(x + width / 2, y + displayLines * jump / 2, 0)
                .scale(width, displayLines * jump, 0), new Vector4f(0, 0, 0, 0.3f * focusTimer / FOCUS_TIME));
            float pos = y;
            for (int i = 0;
                 i < displayLines - 1;
                 ++i) {
                int next = lines.size() - (displayLines - 1) + i;
                if (next >= 0 && next < lines.size()) {
                    font.draw(lines.get(lines.size() - (displayLines - 1) + i), x, pos, ortho, new Vector4f(1, 1, 1, focusTimer / FOCUS_TIME));
                }
                pos += jump;
            }
            if((int) (lineTimer * 4) % 2 == 0) {
                font.draw(typing.toString(), x, pos, ortho, new Vector4f(1, 1, 1, focusTimer / FOCUS_TIME));
            } else {
                font.draw(typing.toString() + "|", x, pos, ortho, new Vector4f(1, 1, 1, focusTimer / FOCUS_TIME));
            }
        }
    }

    public void add(String s) {
        if(lines.size() + 1 > CAPACITY) {
            lines.remove(0);
        }
        lines.add(s);
    }

    public void println(String s) {
        focusTimer = FOCUS_TIME;
        int index = 0, start = 0;
        while(index < s.length()) {
            float width = 0;
            while (width < this.width && index < s.length()) {
                ++index;
                width = font.textWidth(s.substring(start, index));
            }
            add(s.substring(start, index));
            start = index;
        }
    }

    public void enable() {
        visible = true;
        focus = true;
        focusTimer = FOCUS_TIME;
        lineTimer = 0;
    }
    public void disable() {
        focus = false;
        visible = true;
        focusTimer = FOCUS_TIME;
        lineTimer = 0;
    }
    public boolean send() {
        if(typing.length() == 0) return false;
        commands.add(typing.toString());
        typing.delete(0, typing.length());
        return true;
    }
}
