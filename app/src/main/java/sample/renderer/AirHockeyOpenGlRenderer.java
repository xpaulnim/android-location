package sample.renderer;

import android.content.Context;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import sample.R;
import sample.util.ShaderHelperKt;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_LINE_LOOP;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static sample.util.ShaderHelperKt.*;
import static sample.util.TextResourceReaderKt.readTextFileFromResource;

public class AirHockeyOpenGlRenderer implements GLSurfaceView.Renderer {
    // We're using 2 floating point values per vertex
    private static final int POSITION_COMPONENT_COUNT = 2;

    private static final int BYTES_PER_FLOAT = 4; // 4 bytes in a float

    // Required for transferring data from Dalvik Heap to Native Heap where it can be read by OpenGL
    private final FloatBuffer vertexData;

    private final Context context;

    // The OpenGL program
    private int program;

    // vertexShader variables
    private static final String A_POSITION = "a_Position";
    private int aPositionLocation;

    // fragmentShader variables
    private static final String U_COLOR = "u_Color";
    private int uColorLocation;

    public AirHockeyOpenGlRenderer(Context context) {
        this.context = context;

        // Two triangles needed to construct a rectangle
        float[] tableVerticesWithTriangles = {
                // Triangle 1
                0f, 0f,
                9f, 14f,
                0f, 14f,

                // Triangle 2
                0f, 0f,
                9f, 0f,
                9f, 14f,

                // Middle line
                0f, 7f,
                9f, 7f,

                // Mallets
                4.5f, 2f,
                4.5f, 12f
        };

        float[] tableVerticesWithTrianglesOpenGlTranslate = {
                // BgTriangle 1
                -0.5f, -0.5f,
                0.5f, 0.5f,
                -0.5f, 0.5f,

                // BgTriangle 2
                -0.5f, -0.5f,
                0.5f, -0.5f,
                0.5f, 0.5f,

                // Triangle 1
                -0.45f, -0.45f,
                0.45f, 0.45f,
                -0.45f, 0.45f,

                // Triangle 2
                -0.45f, -0.45f,
                0.45f, -0.45f,
                0.45f, 0.45f,

                // Middle line
                -0.5f, 0f,
                0.5f, 0f,

                // Mallets
                0f, -0.25f,
                0f, 0.25f,

                // Puck
                0.0f, 0.0f
        };

        vertexData = ByteBuffer
                .allocateDirect(tableVerticesWithTrianglesOpenGlTranslate.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        // Copy from dalvik to native memory
        vertexData.put(tableVerticesWithTrianglesOpenGlTranslate);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0F, 0.0F, 0.0F, 0.0F);

        String vertexShaderSource = readTextFileFromResource(context, R.raw.simple_vertex_shader);
        String fragmentShaderSource = readTextFileFromResource(context, R.raw.simple_fragment_shader);

        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);

        // Link the shaders into an OpenGL program
        program = linkProgram(vertexShader, fragmentShader);

        // Only really needed when debugging
        validateProgram(program);
        glUseProgram(program);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        uColorLocation = glGetUniformLocation(program, U_COLOR);

        vertexData.position(0); // Make sure vertexData is read from the start
        glVertexAttribPointer(
                aPositionLocation,
                POSITION_COMPONENT_COUNT,
                GL_FLOAT,
                false,
                0,
                vertexData
        );

        glEnableVertexAttribArray(aPositionLocation);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);

        // BgTriangle 1, 2
        glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        // Triangle 1, 2
        glUniform4f(uColorLocation, 1.0f, 1.0f, 0.0f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 6, 6);

        // Middle line
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_LINES, 12, 2);

        // Mallet
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_POINTS, 14, 1);

        // Mallet
        glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
        glDrawArrays(GL_POINTS, 15, 1);

        // Puck
        glUniform4f(uColorLocation, 0.0f, 1.0f, 0.0f, 1.0f);
        glDrawArrays(GL_POINTS, 16, 1);
    }
}
