package isaacais;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static isaacais.Shader.Uniform.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class FloorRenderer {

    private static final float[] TRIANGLES = {
        -0.5f, -0.5f, 0.0f, 0.0f,
        -0.5f, +0.5f, 0.0f, 1.0f,
        +0.5f, +0.5f, 1.0f, 1.0f,
        +0.5f, +0.5f, 1.0f, 1.0f,
        +0.5f, -0.5f, 1.0f, 0.0f,
        -0.5f, -0.5f, 0.0f, 0.0f
    };

    private static class FloorRendering {
        int floorVao;
        int floorVbo;
        int floorVertCount;
        Floor floorRef;
        int wallVao;
        int wallVbo;
        int wallVertCount;
    }

    private final int programMask;
    private int[] uniformsMask;
    private FloorRendering[] floorRender;

    public Floor.Group floors;

    private TextureRenderer texRender;

    private Texture carpet, wall;

    public FloorRenderer() {
        carpet = new Texture(Util.PATH_PREFIX + "carpet.png", new Texture.Settings(GL_REPEAT, GL_NEAREST));
        wall = new Texture(Util.PATH_PREFIX + "wall.png", new Texture.Settings(GL_CLAMP_TO_EDGE, GL_NEAREST));
        this.texRender = new TextureRenderer(4);
        int vert = Shader.createShader(Util.PATH_PREFIX + "simplev.glsl", GL_VERTEX_SHADER);
        int frag = Shader.createShader(Util.PATH_PREFIX + "simplef.glsl", GL_FRAGMENT_SHADER);
        programMask = glCreateProgram();
        glAttachShader(programMask, vert);
        glAttachShader(programMask, frag);
        glBindAttribLocation(programMask, Shader.Attribute.POSITION.num, "position");
        glLinkProgram(programMask);
        Shader.checkLinking("floor", programMask);
        glUseProgram(programMask);
        glDeleteShader(vert);
        glDeleteShader(frag);
        Game.checkGLError("floor shader init");
        uniformsMask = Shader.initUniformList();
        uniformsMask[MATRIX.num] = glGetUniformLocation(programMask, "matrix");
        uniformsMask[COLOR.num] = glGetUniformLocation(programMask, "color");
        Game.checkGLError("floor shader init");
    }

    public void build(Floor.Group floors) {
        if(floorRender != null) {
            for(FloorRendering r : floorRender) {
                glDeleteBuffers(r.floorVbo);
                glDeleteVertexArrays(r.floorVao);
            }
        }
        this.floorRender = new FloorRendering[floors.floors.size()];
        this.floors = floors;

        int floorIndex = 0;
        for(Floor floor : this.floors.floors.values()) {
            FloorRendering render = new FloorRendering();
            { // floor
                int vao = glGenVertexArrays();
                glBindVertexArray(vao);

                int vbo = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                render.floorVertCount = this.buildFloor(floor);
                glEnableVertexAttribArray(Shader.Attribute.POSITION.num);
                glVertexAttribPointer(Shader.Attribute.POSITION.num, 2, GL_FLOAT, false, 2 * 4, 0);
                Game.checkGLError("floor buffer init");

                render.floorVao = vao;
                render.floorVbo = vbo;
            }
            {
                int vao = glGenVertexArrays();
                glBindVertexArray(vao);

                int vbo = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                render.wallVertCount = this.buildWall(floor);
                glEnableVertexAttribArray(Shader.Attribute.POSITION.num);
                glVertexAttribPointer(Shader.Attribute.POSITION.num, 2, GL_FLOAT, false, 4 * 4, 0);
                glEnableVertexAttribArray(Shader.Attribute.TEXTURE.num);
                glVertexAttribPointer(Shader.Attribute.TEXTURE.num, 2, GL_FLOAT, false, 4 * 4, 2 * 4);
                Game.checkGLError("wall buffer init");

                render.wallVao = vao;
                render.wallVbo = vbo;
            }

            render.floorRef = floor;
            this.floorRender[floorIndex] = render;
            floorIndex++;
        }
        Game.checkGLError("build loop");
    }

    private int buildWall(Floor floor) {
        int vertices = 0;
        for(int y = floor.y * Floor.FLOOR_SIZE; y < floor.y * Floor.FLOOR_SIZE + Floor.FLOOR_SIZE; ++y) {
            for(int x = floor.x * Floor.FLOOR_SIZE; x < floor.x * Floor.FLOOR_SIZE + Floor.FLOOR_SIZE; ++x) {
                if(floors.getTile(x, y) == Floor.Tile.FILLED.val && floors.getTile(x, y - 1) == Floor.Tile.EMPTY.val) {
                    vertices += 6;
                }
            }
        }
        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer data = stack.mallocFloat(vertices * 4);
            for(int y = floor.y * Floor.FLOOR_SIZE; y < floor.y * Floor.FLOOR_SIZE + Floor.FLOOR_SIZE; ++y) {
                for(int x = floor.x * Floor.FLOOR_SIZE; x < floor.x * Floor.FLOOR_SIZE + Floor.FLOOR_SIZE; ++x) {
                    if(floors.getTile(x, y) == Floor.Tile.FILLED.val && floors.getTile(x, y - 1) == Floor.Tile.EMPTY.val) {
                        int i = x - floor.x * Floor.FLOOR_SIZE;
                        int j = y - floor.y * Floor.FLOOR_SIZE - 2;
                        float so = (x % 2 == 0 ? 0.0f : 0.5f);
                        data.put(new float[] {
                            i, j, so, 0,
                            i, j+2, so, 1,
                            i+1, j+2, so+0.5f, 1,
                            i+1, j+2, so+0.5f, 1,
                            i+1, j, so+0.5f, 0,
                            i, j, so, 0
                        });
                    }
                }
            }
            data.flip();
            glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
        }
        return vertices;
    }

    public void draw(Matrix4f matrix) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);

            glEnable(GL_STENCIL_TEST);

            // Draw floor
            glStencilFunc(GL_ALWAYS, 1, 0xFF); // Set any stencil to 1
            glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
            glStencilMask(0xFF); // Write to stencil buffer
            glClear(GL_STENCIL_BUFFER_BIT); // Clear stencil buffer (0 by default)

            glUseProgram(programMask);
            glUniform4f(uniformsMask[COLOR.num], 1, 1, 1, 1);
            for(int i = 0; i < floorRender.length; ++i) {
                FloorRendering render = floorRender[i];
                glUniformMatrix4fv(uniformsMask[MATRIX.num], false, new Matrix4f(matrix).mul(render.floorRef.getMatrix()).get(buffer));
                glBindVertexArray(render.floorVao);
                glDrawArrays(GL_TRIANGLES, 0, render.floorVertCount);
                buffer.clear();
            }

            glStencilFunc(GL_EQUAL, 1, 0xFF); // Pass test if stencil value is 1
            glStencilMask(0x00); // Don't write anything to stencil buffer

            carpet.bind();
            for(int i = 0; i < floorRender.length; ++i) {
                FloorRendering render = floorRender[i];
                texRender.draw(new Matrix4f(matrix).translate((0.5f + render.floorRef.x) * Floor.FLOOR_SIZE, (0.5f + render.floorRef.y) * Floor.FLOOR_SIZE, 0).scale(Floor.FLOOR_SIZE, Floor.FLOOR_SIZE, 0), new Vector4f(1));
            }

            glDisable(GL_STENCIL_TEST);

            wall.bind();
            glUseProgram(texRender.program);
            glUniform4f(texRender.uniforms[COLOR.num], 1, 1, 1, 1);
            glUniform1i(texRender.uniforms[SAMPLER.num], 0);
            for(int i = 0; i < floorRender.length; ++i) {
                FloorRendering render = floorRender[i];
                glUniformMatrix4fv(texRender.uniforms[MATRIX.num], false, new Matrix4f(matrix).mul(render.floorRef.getMatrix()).scale(1.0001f).get(buffer));
                glBindVertexArray(render.wallVao);
                glDrawArrays(GL_TRIANGLES, 0, render.wallVertCount);
                buffer.clear();
            }
        }
        Game.checkGLError("draw loop");
    }

    public void destroy() {
        if(floorRender != null) {
            for(FloorRendering r : floorRender) {
                glDeleteBuffers(r.floorVbo);
                glDeleteVertexArrays(r.floorVao);
            }
        }
        glDeleteProgram(programMask);
        Game.checkGLError("texture cleanup");
    }

    private int buildFloor(Floor floor) {
        int vertices = 0;
        for(int y = 0; y < Floor.FLOOR_SIZE; ++y) {
            for(int x = 0; x < Floor.FLOOR_SIZE; ++x) {
                if(floor.get(x, y) == Floor.Tile.FILLED.val) {
                    vertices += 6;
                }
            }
        }
        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer data = stack.mallocFloat(vertices * 2);
            for(int y = 0; y < Floor.FLOOR_SIZE; ++y) {
                for(int x = 0; x < Floor.FLOOR_SIZE; ++x) {
                    if(floor.get(x, y) == Floor.Tile.FILLED.val) {
                        data.put(new float[] {
                            x - 0.001f, y - 0.001f,
                            x - 0.001f, y + 1.001f,
                            x + 1.001f, y + 1.001f,
                            x + 1.001f, y + 1.001f,
                            x + 1.001f, y - 0.001f,
                            x - 0.001f, y - 0.001f,
                        });
                    }
                }
            }
            data.flip();
            glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
        }
        return vertices;
    }
}
