package com.dublick.tutorial03.graph;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.dublick.tutorial03.R;
import com.dublick.tutorial03.utils.Constant;
import com.dublick.tutorial03.utils.ShaderUtils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glViewport;

/**
 * Created by 3dium on 06.07.2017.
 */

public class OpenGLRenderer implements GLSurfaceView.Renderer {

    private Context context;
    private int programId;

    public volatile float deltaX;
    public volatile float deltaY;
    public volatile float scale = 0.009f;

//    private HeightMap heightMap;
    private Mesh mesh;

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];

    private final float[] mvpMatrix = new float[16];

    private final float[] accumulatedRotation = new float[16];
    private final float[] currentRotation = new float[16];
    private final float[] lightModelMatrix = new float[16];
    private final float[] temporaryMatrix = new float[16];

    private final float[] lightPosInWorldSpace = new float[4];
    private final float[] lightPosInEyeSpace = new float[4];

    private int mvpMatrixUniform;
    private int mvMatrixUniform;
    private int lightPosUniform;

    private int positionAttribute;
    private int normalAttribute;
    private int colorAttribute;

    /** Additional constants. */
    private static final int POSITION_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int NORMAL_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int COLOR_DATA_SIZE_IN_ELEMENTS = 4;

    private static final int BYTES_PER_FLOAT = 4;
    private static final int BYTES_PER_SHORT = 2;

    private static final int STRIDE = (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS + COLOR_DATA_SIZE_IN_ELEMENTS)
            * BYTES_PER_FLOAT;


    public OpenGLRenderer(Context context) {
        this.context = context;
    }

    private void createViewMatrix() {
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = -0.5f;

        // We are looking toward the distance
        final float centerX = 0.0f;
        final float centerY = 0.0f;
        final float centerZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we
        // holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        heightMap = new HeightMap();
        mesh = new Mesh();

        glClearColor(0.2f, 0.2f, 0.2f, 0.0f);
        glEnable(GLES20.GL_DEPTH_TEST);

        createViewMatrix();

        int vertexShaderId = ShaderUtils.createShader(context, GL_VERTEX_SHADER, R.raw.vert);
        int fragmentShaderId = ShaderUtils.createShader(context, GL_FRAGMENT_SHADER, R.raw.frag);

        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId, new String[] {Constant.POSITION_ATTRIBUTE, Constant.NORMAL_ATTRIBUTE, Constant.COLOR_ATTRIBUTE});

        Matrix.setIdentityM(accumulatedRotation, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);

        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 1000.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(programId);

        mvpMatrixUniform = GLES20.glGetUniformLocation(programId, Constant.MVP_MATRIX_UNIFORM);
        mvMatrixUniform = GLES20.glGetUniformLocation(programId, Constant.MV_MATRIX_UNIFORM);
        lightPosUniform = GLES20.glGetUniformLocation(programId, Constant.LIGHT_POSITION_UNIFORM);
        positionAttribute = GLES20.glGetAttribLocation(programId, Constant.POSITION_ATTRIBUTE);
        normalAttribute = GLES20.glGetAttribLocation(programId, Constant.NORMAL_ATTRIBUTE);
        colorAttribute = GLES20.glGetAttribLocation(programId, Constant.COLOR_ATTRIBUTE);

        Matrix.setIdentityM(lightModelMatrix, 0);
        Matrix.translateM(lightModelMatrix, 0, 0.0f, 7.5f, -8.0f);

        Matrix.multiplyMV(lightPosInWorldSpace, 0, lightModelMatrix, 0, lightPosInWorldSpace, 0);
        Matrix.multiplyMV(lightPosInEyeSpace, 0, mViewMatrix, 0, lightPosInWorldSpace, 0);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -12f);

        Matrix.setIdentityM(currentRotation, 0);
        Matrix.rotateM(currentRotation, 0, deltaX, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(currentRotation, 0, deltaY, 1.0f, 0.0f, 0.0f);
        deltaX = 0.0f;
        deltaY = 0.0f;

        Matrix.multiplyMM(temporaryMatrix, 0, currentRotation, 0, accumulatedRotation, 0);
        System.arraycopy(temporaryMatrix, 0, accumulatedRotation, 0, 16);

        Matrix.multiplyMM(temporaryMatrix, 0, mModelMatrix, 0, accumulatedRotation, 0);
        System.arraycopy(temporaryMatrix, 0, mModelMatrix, 0, 16);

        Matrix.scaleM(mModelMatrix, 0, scale, scale, scale);
        Matrix.multiplyMM(mvpMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        glUniformMatrix4fv(mvMatrixUniform, 1, false, mvpMatrix, 0);

        Matrix.multiplyMM(temporaryMatrix, 0, mProjectionMatrix, 0, mvpMatrix, 0);
        System.arraycopy(temporaryMatrix, 0, mvpMatrix, 0, 16);

        glUniformMatrix4fv(mvpMatrixUniform, 1, false, mvpMatrix, 0);

        glUniform3f(lightPosUniform, lightPosInEyeSpace[0], lightPosInEyeSpace[1], lightPosInEyeSpace[2]);

//        heightMap.render();
        mesh.render();
    }

    class Mesh {
        final int[] vbo = new int[1];
        final int[] ibo = new int[1];

        int indexCount;

        Mesh() {
            PlyLoader plyLoader = new PlyLoader();
            try {
                final float[] vertexData = plyLoader.getVertex(context.getResources().openRawResource(R.raw.girl3));
                final short[] indexData = plyLoader.getIndexList();
                indexCount = indexData.length;

                final FloatBuffer heightMapVertexDataBuffer = ByteBuffer
                        .allocateDirect(vertexData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
                heightMapVertexDataBuffer.put(vertexData).position(0);

                final ShortBuffer heightMapIndexDataBuffer = ByteBuffer
                        .allocateDirect(indexData.length * BYTES_PER_SHORT).order(ByteOrder.nativeOrder())
                        .asShortBuffer();
                heightMapIndexDataBuffer.put(indexData).position(0);

                GLES20.glGenBuffers(1, vbo, 0);
                GLES20.glGenBuffers(1, ibo, 0);

                if (vbo[0] > 0 && ibo[0] > 0) {
                    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
                    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, heightMapVertexDataBuffer.capacity() * BYTES_PER_FLOAT,
                            heightMapVertexDataBuffer, GLES20.GL_STATIC_DRAW);

                    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
                    GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, heightMapIndexDataBuffer.capacity()
                            * BYTES_PER_SHORT, heightMapIndexDataBuffer, GLES20.GL_STATIC_DRAW);

                    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
                    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
                } else {
//                    errorHandler.handleError(ErrorType.BUFFER_CREATION_ERROR, "glGenBuffers");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        void render() {
            if (vbo[0] > 0 && ibo[0] > 0) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);

                // Bind Attributes
                GLES20.glVertexAttribPointer(positionAttribute, POSITION_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                        STRIDE, 0);
                GLES20.glEnableVertexAttribArray(positionAttribute);

                GLES20.glVertexAttribPointer(normalAttribute, NORMAL_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                        STRIDE, POSITION_DATA_SIZE_IN_ELEMENTS * BYTES_PER_FLOAT);
                GLES20.glEnableVertexAttribArray(normalAttribute);

                GLES20.glVertexAttribPointer(colorAttribute, COLOR_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                        STRIDE, (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS) * BYTES_PER_FLOAT);
                GLES20.glEnableVertexAttribArray(colorAttribute);

                // Draw
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, 0);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
            }
        }

        void release() {
            if (vbo[0] > 0) {
                GLES20.glDeleteBuffers(vbo.length, vbo, 0);
                vbo[0] = 0;
            }

            if (ibo[0] > 0) {
                GLES20.glDeleteBuffers(ibo.length, ibo, 0);
                ibo[0] = 0;
            }
        }
    }

    class HeightMap {
        static final int SIZE_PER_SIDE = 32;
        static final float MIN_POSITION = -5f;
        static final float POSITION_RANGE = 10f;

        final int[] vbo = new int[1];
        final int[] ibo = new int[1];

        int indexCount;

        HeightMap() {
            try {
                final int floatsPerVertex = POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS
                        + COLOR_DATA_SIZE_IN_ELEMENTS;
                final int xLength = SIZE_PER_SIDE;
                final int yLength = SIZE_PER_SIDE;

                final float[] heightMapVertexData = new float[xLength * yLength * floatsPerVertex];

                int offset = 0;

                // First, build the data for the vertex buffer
                for (int y = 0; y < yLength; y++) {
                    for (int x = 0; x < xLength; x++) {
                        final float xRatio = x / (float) (xLength - 1);

                        // Build our heightmap from the top down, so that our triangles are counter-clockwise.
                        final float yRatio = 1f - (y / (float) (yLength - 1));

                        final float xPosition = MIN_POSITION + (xRatio * POSITION_RANGE);
                        final float yPosition = MIN_POSITION + (yRatio * POSITION_RANGE);

                        // Position
                        heightMapVertexData[offset++] = xPosition;
                        heightMapVertexData[offset++] = yPosition;
                        heightMapVertexData[offset++] = ((xPosition * xPosition) + (yPosition * yPosition)) / 10f;

                        // Cheap normal using a derivative of the function.
                        // The slope for X will be 2X, for Y will be 2Y.
                        // Divide by 10 since the position's Z is also divided by 10.
                        final float xSlope = (2 * xPosition) / 10f;
                        final float ySlope = (2 * yPosition) / 10f;

                        // Calculate the normal using the cross product of the slopes.
                        final float[] planeVectorX = {1f, 0f, xSlope};
                        final float[] planeVectorY = {0f, 1f, ySlope};
                        final float[] normalVector = {
                                (planeVectorX[1] * planeVectorY[2]) - (planeVectorX[2] * planeVectorY[1]),
                                (planeVectorX[2] * planeVectorY[0]) - (planeVectorX[0] * planeVectorY[2]),
                                (planeVectorX[0] * planeVectorY[1]) - (planeVectorX[1] * planeVectorY[0])};

                        // Normalize the normal
                        final float length = Matrix.length(normalVector[0], normalVector[1], normalVector[2]);

                        heightMapVertexData[offset++] = normalVector[0] / length;
                        heightMapVertexData[offset++] = normalVector[1] / length;
                        heightMapVertexData[offset++] = normalVector[2] / length;

                        // Add some fancy colors.
                        heightMapVertexData[offset++] = xRatio;
                        heightMapVertexData[offset++] = yRatio;
                        heightMapVertexData[offset++] = 0.5f;
                        heightMapVertexData[offset++] = 1f;
                    }
                }

                // Now build the index data
                final int numStripsRequired = yLength - 1;
                final int numDegensRequired = 2 * (numStripsRequired - 1);
                final int verticesPerStrip = 2 * xLength;

                final short[] heightMapIndexData = new short[(verticesPerStrip * numStripsRequired) + numDegensRequired];

                offset = 0;

                for (int y = 0; y < yLength - 1; y++) {
                    if (y > 0) {
                        // Degenerate begin: repeat first vertex
                        heightMapIndexData[offset++] = (short) (y * yLength);
                    }

                    for (int x = 0; x < xLength; x++) {
                        // One part of the strip
                        heightMapIndexData[offset++] = (short) ((y * yLength) + x);
                        heightMapIndexData[offset++] = (short) (((y + 1) * yLength) + x);
                    }

                    if (y < yLength - 2) {
                        // Degenerate end: repeat last vertex
                        heightMapIndexData[offset++] = (short) (((y + 1) * yLength) + (xLength - 1));
                    }
                }

                indexCount = heightMapIndexData.length;

                final FloatBuffer heightMapVertexDataBuffer = ByteBuffer
                        .allocateDirect(heightMapVertexData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
                heightMapVertexDataBuffer.put(heightMapVertexData).position(0);

                final ShortBuffer heightMapIndexDataBuffer = ByteBuffer
                        .allocateDirect(heightMapIndexData.length * BYTES_PER_SHORT).order(ByteOrder.nativeOrder())
                        .asShortBuffer();
                heightMapIndexDataBuffer.put(heightMapIndexData).position(0);

                GLES20.glGenBuffers(1, vbo, 0);
                GLES20.glGenBuffers(1, ibo, 0);

                if (vbo[0] > 0 && ibo[0] > 0) {
                    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
                    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, heightMapVertexDataBuffer.capacity() * BYTES_PER_FLOAT,
                            heightMapVertexDataBuffer, GLES20.GL_STATIC_DRAW);

                    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
                    GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, heightMapIndexDataBuffer.capacity()
                            * BYTES_PER_SHORT, heightMapIndexDataBuffer, GLES20.GL_STATIC_DRAW);

                    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
                    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
                } else {
//                    errorHandler.handleError(ErrorType.BUFFER_CREATION_ERROR, "glGenBuffers");
                }
            } catch (Throwable t) {
                t.printStackTrace();
//                Log.w(TAG, t);
//                errorHandler.handleError(ErrorType.BUFFER_CREATION_ERROR, t.getLocalizedMessage());
            }
        }

        void render() {
            if (vbo[0] > 0 && ibo[0] > 0) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);

                // Bind Attributes
                GLES20.glVertexAttribPointer(positionAttribute, POSITION_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                        STRIDE, 0);
                GLES20.glEnableVertexAttribArray(positionAttribute);

                GLES20.glVertexAttribPointer(normalAttribute, NORMAL_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                        STRIDE, POSITION_DATA_SIZE_IN_ELEMENTS * BYTES_PER_FLOAT);
                GLES20.glEnableVertexAttribArray(normalAttribute);

                GLES20.glVertexAttribPointer(colorAttribute, COLOR_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                        STRIDE, (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS) * BYTES_PER_FLOAT);
                GLES20.glEnableVertexAttribArray(colorAttribute);

                // Draw
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
                GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, indexCount, GLES20.GL_UNSIGNED_SHORT, 0);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
            }
        }

        void release() {
            if (vbo[0] > 0) {
                GLES20.glDeleteBuffers(vbo.length, vbo, 0);
                vbo[0] = 0;
            }

            if (ibo[0] > 0) {
                GLES20.glDeleteBuffers(ibo.length, ibo, 0);
                ibo[0] = 0;
            }
        }
    }
}
