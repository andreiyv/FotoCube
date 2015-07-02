package com.easy.fotocubik;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static java.lang.Math.abs;

/**
 * This class implements our custom renderer. Note that the GL10 parameter passed in is unused for OpenGL ES 2.0
 * renderers -- the static class GLES20 is used instead.
 */
class LessonSixRenderer implements GLSurfaceView.Renderer {
    /**
     * Used for debug logs.
     */
    private static final String TAG = "fotocubik";

    private final Context mActivityContext;

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] mModelMatrix = new float[16];

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private float[] mViewMatrix = new float[16];

    /**
     * Store the projection matrix. This is used to project the scene onto a 2D viewport.
     */
    private float[] mProjectionMatrix = new float[16];

    /**
     * Allocate storage for the final combined matrix. This will be passed into the shader program.
     */
    private float[] mMVPMatrix = new float[16];

    /**
     * Store the accumulated rotation.
     */
    private final float[] mAccumulatedRotation = new float[16];

    /**
     * Store the current rotation.
     */
    private final float[] mCurrentRotation = new float[16];

    /**
     * A temporary matrix.
     */
    private float[] mTemporaryMatrix = new float[16];

    /**
     * Stores a copy of the model matrix specifically for the light position.
     */
    private float[] mLightModelMatrix = new float[16];
    private float[] mScaleMatrix = new float[16];   // scaling
    /**
     * Store our model data in a float buffer.
     */
    private final FloatBuffer mCubePositions;
    private final FloatBuffer mCubePositions1;
    private final FloatBuffer mCubePositions2;
    private final FloatBuffer mCubePositions3;
    private final FloatBuffer mCubePositions4;
    private final FloatBuffer mCubePositions5;
    private final FloatBuffer mCubeNormals;
    private final FloatBuffer mCubeNormals1;
    private final FloatBuffer mCubeNormals2;
    private final FloatBuffer mCubeNormals3;
    private final FloatBuffer mCubeNormals4;
    private final FloatBuffer mCubeNormals5;
    private final FloatBuffer mCubeTextureCoordinates;
    private final FloatBuffer mCubeTextureCoordinates1;
    private final FloatBuffer mCubeTextureCoordinates2;
    private final FloatBuffer mCubeTextureCoordinates3;
    private final FloatBuffer mCubeTextureCoordinates4;
    private final FloatBuffer mCubeTextureCoordinates5;
    private final FloatBuffer mCubeTextureCoordinatesForPlane;
    // scaling
    float scaleX = 1.0f;
    float scaleY = 1.0f;
    float scaleZ = 1.0f;

    float delta_zoom;
    float wp = -15.0f;

    int mode;

    int n=0;
    int s=0;

    /**
     * This will be used to pass in the transformation matrix.
     */
    private int mMVPMatrixHandle;

    /**
     * This will be used to pass in the modelview matrix.
     */
    private int mMVMatrixHandle;

    /**
     * This will be used to pass in the light position.
     */
    private int mLightPosHandle;

    /**
     * This will be used to pass in the texture.
     */
    private int mTextureUniformHandle;

    /**
     * This will be used to pass in model position information.
     */
    private int mPositionHandle;

    /**
     * This will be used to pass in model normal information.
     */
    private int mNormalHandle;

    /**
     * This will be used to pass in model texture coordinate information.
     */
    private int mTextureCoordinateHandle;

    /**
     * How many bytes per float.
     */
    private final int mBytesPerFloat = 4;

    /**
     * Size of the position data in elements.
     */
    private final int mPositionDataSize = 3;

    /**
     * Size of the normal data in elements.
     */
    private final int mNormalDataSize = 3;

    /**
     * Size of the texture coordinate data in elements.
     */
    private final int mTextureCoordinateDataSize = 2;

    /**
     * Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
     * we multiply this by our transformation matrices.
     */
    private final float[] mLightPosInModelSpace = new float[]{0.0f, 0.0f, 0.0f, 1.0f};

    /**
     * Used to hold the current position of the light in world space (after transformation via model matrix).
     */
    private final float[] mLightPosInWorldSpace = new float[4];

    /**
     * Used to hold the transformed position of the light in eye space (after transformation via modelview matrix)
     */
    private final float[] mLightPosInEyeSpace = new float[4];

    /**
     * This is a handle to our cube shading program.
     */
    private int mProgramHandle;

    /**
     * This is a handle to our light point program.
     */
    private int mPointProgramHandle;

    /**
     * These are handles to our texture data.
     */
    private int mBrickDataHandle;
    private int mGrassDataHandle;
    private int usb_android;
    private int cube_side_1;
    private int cube_side_2;
    private int cube_side_3;
    private int cube_side_4;
    private int cube_side_5;

    /**
     * Temporary place to save the min and mag filter, in case the activity was restarted.
     */
    private int mQueuedMinFilter;
    private int mQueuedMagFilter;

    // These still work without volatile, but refreshes are not guaranteed to happen.
    public volatile float mDeltaX;
    public volatile float ugolX;
    public volatile float mDeltaY;
    public volatile float ugolY;
    public volatile float dampX;
    public volatile float dampY;
    public volatile float slowdown;


    /**
     * Initialize the model data.
     */
    public LessonSixRenderer(final Context activityContext) {
        mActivityContext = activityContext;
        ugolX = 0.0f;
        ugolY = 0.0f;
        dampX = 1.0f;
        dampY = 1.0f;
        slowdown = 100.0f;
        // Define points for a cube.

        // X, Y, Z
        final float[] cubePositionData =
                {
                        // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle,
                        // if the points are counter-clockwise we are looking at the "front". If not we are looking at
                        // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
                        // usually represent the backside of an object and aren't visible anyways.

                        // Front face
                        -1.0f, 1.0f, 1.0f,
                        -1.0f, -1.0f, 1.0f,
                        1.0f, 1.0f, 1.0f,
                        -1.0f, -1.0f, 1.0f,
                        1.0f, -1.0f, 1.0f,
                        1.0f, 1.0f, 1.0f
/*
                        // Right face
                        1.0f, 1.0f, 1.0f,
                        1.0f, -1.0f, 1.0f,
                        1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, 1.0f,
                        1.0f, -1.0f, -1.0f,
                        1.0f, 1.0f, -1.0f,

                        // Back face
                        1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f,
                        -1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f,
                        -1.0f, -1.0f, -1.0f,
                        -1.0f, 1.0f, -1.0f,

                        // Left face
                        -1.0f, 1.0f, -1.0f,
                        -1.0f, -1.0f, -1.0f,
                        -1.0f, 1.0f, 1.0f,
                        -1.0f, -1.0f, -1.0f,
                        -1.0f, -1.0f, 1.0f,
                        -1.0f, 1.0f, 1.0f,

                        // Top face
                        -1.0f, 1.0f, -1.0f,
                        -1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, -1.0f,
                        -1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, -1.0f,

                        // Bottom face
                        1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f, 1.0f,
                        -1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f, 1.0f,
                        -1.0f, -1.0f, 1.0f,
                        -1.0f, -1.0f, -1.0f,
*/
                };

        final float[] cubePositionData1 =
                {
                        // Right face
                        1.0f, 1.0f, 1.0f,
                        1.0f, -1.0f, 1.0f,
                        1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, 1.0f,
                        1.0f, -1.0f, -1.0f,
                        1.0f, 1.0f, -1.0f
                };

        final float[] cubePositionData2 =
                {
                        // Back face
                        1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f,
                        -1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f,
                        -1.0f, -1.0f, -1.0f,
                        -1.0f, 1.0f, -1.0f
                };

        final float[] cubePositionData3 =
                {
                        // Left face
                        -1.0f, 1.0f, -1.0f,
                        -1.0f, -1.0f, -1.0f,
                        -1.0f, 1.0f, 1.0f,
                        -1.0f, -1.0f, -1.0f,
                        -1.0f, -1.0f, 1.0f,
                        -1.0f, 1.0f, 1.0f
                };

        final float[] cubePositionData4 =
                {
                        // Top face
                        -1.0f, 1.0f, -1.0f,
                        -1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, -1.0f,
                        -1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, -1.0f
                };

        final float[] cubePositionData5 =
                {
                        // Bottom face
                        1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f, 1.0f,
                        -1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f, 1.0f,
                        -1.0f, -1.0f, 1.0f,
                        -1.0f, -1.0f, -1.0f
                };

        // X, Y, Z
        // The normal is used in light calculations and is a vector which points
        // orthogonal to the plane of the surface. For a cube model, the normals
        // should be orthogonal to the points of each face.
        final float[] cubeNormalData =
                {
                        // Front face
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
/*
                        // Right face
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,

                        // Back face
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,

                        // Left face
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,

                        // Top face
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,

                        // Bottom face
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f
*/
                };

        final float[] cubeNormalData1 =
                {

                        // Right face
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f

                };

        final float[] cubeNormalData2 =
                {

                        // Back face
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f

                };

        final float[] cubeNormalData3 =
                {

                        // Left face
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f

                };

        final float[] cubeNormalData4 =
                {

                        // Top face
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f

                };

        final float[] cubeNormalData5 =
                {

                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f

                };

        // S, T (or X, Y)
        // Texture coordinate data.
        // Because images have a Y axis pointing downward (values increase as you move down the image) while
        // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
        // What's more is that the texture coordinates are the same for every face.
        final float[] cubeTextureCoordinateData =
                {
                        // Front face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f
/*
                        // Right face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Back face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Left face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Top face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Bottom face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f
*/
                };
        final float[] cubeTextureCoordinateData1 =
                {

                        // Right face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f
                };
        final float[] cubeTextureCoordinateData2 =
                {

                        // Back face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f
                };

        final float[] cubeTextureCoordinateData3 =
                {

                        // Left face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f
                };
        final float[] cubeTextureCoordinateData4 =
                {

                        // Top face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f
                };

        final float[] cubeTextureCoordinateData5 =
                {

                        // Bottom face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f
                };


        // S, T (or X, Y)
        // Texture coordinate data.
        // Because images have a Y axis pointing downward (values increase as you move down the image) while
        // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
        // What's more is that the texture coordinates are the same for every face.
        final float[] cubeTextureCoordinateDataForPlane =
                {
                        // Front face
                        0.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 25.0f,
                        25.0f, 0.0f,
/*
                        // Right face
                        0.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 25.0f,
                        25.0f, 0.0f,

                        // Back face
                        0.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 25.0f,
                        25.0f, 0.0f,

                        // Left face
                        0.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 25.0f,
                        25.0f, 0.0f,

                        // Top face
                        0.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 25.0f,
                        25.0f, 0.0f,

                        // Bottom face
                        0.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 25.0f,
                        25.0f, 0.0f
*/
                };



        // Initialize the buffers.
        mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions.put(cubePositionData).position(0);

        mCubePositions1 = ByteBuffer.allocateDirect(cubePositionData1.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions1.put(cubePositionData1).position(0);

        mCubePositions2 = ByteBuffer.allocateDirect(cubePositionData2.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions2.put(cubePositionData2).position(0);

        mCubePositions3 = ByteBuffer.allocateDirect(cubePositionData3.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions3.put(cubePositionData3).position(0);

        mCubePositions4 = ByteBuffer.allocateDirect(cubePositionData4.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions4.put(cubePositionData4).position(0);

        mCubePositions5 = ByteBuffer.allocateDirect(cubePositionData5.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions5.put(cubePositionData5).position(0);

        mCubeNormals = ByteBuffer.allocateDirect(cubeNormalData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeNormals.put(cubeNormalData).position(0);

        mCubeNormals1 = ByteBuffer.allocateDirect(cubeNormalData1.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeNormals1.put(cubeNormalData1).position(0);

        mCubeNormals2 = ByteBuffer.allocateDirect(cubeNormalData2.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeNormals2.put(cubeNormalData2).position(0);

        mCubeNormals3 = ByteBuffer.allocateDirect(cubeNormalData3.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeNormals3.put(cubeNormalData3).position(0);

        mCubeNormals4 = ByteBuffer.allocateDirect(cubeNormalData4.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeNormals4.put(cubeNormalData4).position(0);

        mCubeNormals5 = ByteBuffer.allocateDirect(cubeNormalData5.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeNormals5.put(cubeNormalData5).position(0);

        mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates1 = ByteBuffer.allocateDirect(cubeTextureCoordinateData1.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);
        mCubeTextureCoordinates1.put(cubeTextureCoordinateData1).position(0);

        mCubeTextureCoordinates2 = ByteBuffer.allocateDirect(cubeTextureCoordinateData2.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates2.put(cubeTextureCoordinateData2).position(0);

        mCubeTextureCoordinates3 = ByteBuffer.allocateDirect(cubeTextureCoordinateData3.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates3.put(cubeTextureCoordinateData3).position(0);

        mCubeTextureCoordinates4 = ByteBuffer.allocateDirect(cubeTextureCoordinateData4.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates4.put(cubeTextureCoordinateData4).position(0);

        mCubeTextureCoordinates5 = ByteBuffer.allocateDirect(cubeTextureCoordinateData5.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates5.put(cubeTextureCoordinateData5).position(0);

        mCubeTextureCoordinatesForPlane = ByteBuffer.allocateDirect(cubeTextureCoordinateDataForPlane.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinatesForPlane.put(cubeTextureCoordinateDataForPlane).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        // Set the background clear color to black.
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // The below glEnable() call is a holdover from OpenGL ES 1, and is not needed in OpenGL ES 2.
        // Enable texture mapping
        // GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        // Position the eye in front of the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = -0.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        //Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, -9.0f, 0, 0, -5, 0, 1, 0);

        final String vertexShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.per_pixel_vertex_shader_tex_and_light);
        final String fragmentShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.per_pixel_fragment_shader_tex_and_light);

        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                new String[]{"a_Position", "a_Normal", "a_TexCoordinate"});

        // Define a simple shader program for our point.
        final String pointVertexShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.point_vertex_shader);
        final String pointFragmentShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.point_fragment_shader);

        final int pointVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, pointVertexShader);
        final int pointFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
        mPointProgramHandle = ShaderHelper.createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle,
                new String[]{"a_Position"});

        // Load the texture
//        mBrickDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.stone_wall_public_domain);
        usb_android = TextureHelper.loadTexture(mActivityContext, R.drawable.magoniya);
        cube_side_1 = TextureHelper.loadTexture(mActivityContext, R.drawable.noisy_grass_public_domain);
        cube_side_2 = TextureHelper.loadTexture(mActivityContext, R.drawable.herbs);
        cube_side_3 = TextureHelper.loadTexture(mActivityContext, R.drawable.usb_android);
        cube_side_4 = TextureHelper.loadTexture(mActivityContext, R.drawable.usb_android);
        cube_side_5 = TextureHelper.loadTexture(mActivityContext, R.drawable.usb_android);

        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        mGrassDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.noisy_grass_public_domain);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        if (mQueuedMinFilter != 0) {
            setMinFilter(mQueuedMinFilter);
        }

        if (mQueuedMagFilter != 0) {
            setMagFilter(mQueuedMagFilter);
        }

        // Initialize the accumulated rotation matrix
        Matrix.setIdentityM(mAccumulatedRotation, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 1000.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
        // Matrix.frustumM(mProjectionMatrix, 0, -1.0f, 1.0f, -2.0f, 2.0f, 1.0f, 1000.0f);
        //  GLU.gluLookAt(glUnused, 9f, 9f, 9f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
   //     Log.d("SPACING: ", "mode: " + mode + "wp:" + wp);
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, wp, 0, 0, -5, 0, 1, 0);
       if ( mode == 1) {
           wp = wp + delta_zoom;
       }

        if ( wp > -5.0f ) { wp = -5.0f; }
        if ( wp < -25.0f ) { wp = -25.0f; }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        long slowTime = SystemClock.uptimeMillis() % 100000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        float slowAngleInDegrees = (360.0f / 100000.0f) * ((int) slowTime);

        // Set our per-vertex lighting program.
        GLES20.glUseProgram(mProgramHandle);

        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix");
        mLightPosHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_LightPos");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");


               // Calculate position of the light. Rotate and then push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -2.0f);
        Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 3.5f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);

        // Draw a cube.
        // Translate the cube into the screen.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.8f, -3.5f);

        // Set a matrix that contains the current rotation.

        Matrix.setIdentityM(mCurrentRotation, 0);


      //  Log.d("RENDER_before: ", "ugolX: " + ugolX + "ugolY:" + ugolY);
        if ( abs(mDeltaX) > 0.001f) { dampX = 1.5f; } else { dampX = 0.0f; }
        if ( abs(mDeltaY) > 0.001f) { dampY = 1.5f; } else { dampY = 0.0f; }









//        Matrix.rotateM(mCurrentRotation, 0, ugolX + mDeltaX/5000.0f, 0.0f, 1.0f, 0.0f);
//        Matrix.rotateM(mCurrentRotation, 0, ugolY + mDeltaY/5000.0f, 1.0f, 0.0f, 0.0f);


 //       Log.d("RENDER_after: ", "ugolX: " + ugolX + "ugolY:" + ugolY);









//        Matrix.rotateM(mCurrentRotation, 0, ugolX, 0.0f, 1.0f, 0.0f);
//        Matrix.rotateM(mCurrentRotation, 0, -1.0f*ugolY, 1.0f, 0.0f, 0.0f);


if (n == 0) {
 //   slowdown = 500.0f;
    ugolX = (mDeltaX / slowdown) - dampX * n;
    ugolY = (-1.0f * mDeltaY / slowdown) - dampY * n;
}

        if (ugolX > 0.001f ) {ugolX=(mDeltaX/slowdown)-0.01f*n;}
        if (ugolX < -0.001f ) {ugolX =(mDeltaX/slowdown)+0.01f*n;}

        if (ugolY > 0.001f ) {ugolY = (-1.0f * mDeltaY/slowdown)-0.01f*n;}
        if (ugolY < -0.001f ) {ugolY = (-1.0f * mDeltaY/slowdown)+0.01f*n;}

        if ( abs(ugolX) <= 0.05f) { ugolX = 0.0f; }
        if ( abs(ugolY) <= 0.05f) { ugolY = 0.0f; }

    //    Log.d("RENDER: ", "slowdown: " + slowdown + "ugolX: " + ugolX + "ugolY: " + ugolY);

        Matrix.rotateM(mCurrentRotation, 0, ugolX, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mCurrentRotation, 0,ugolY, 1.0f, 0.0f, 0.0f);


n = n+1;


  //      mDeltaX = 0.0f;
  //      mDeltaY = 0.0f;




        // Multiply the current rotation by the accumulated rotation, and then set the accumulated rotation to the result.
        Matrix.multiplyMM(mTemporaryMatrix, 0, mCurrentRotation, 0, mAccumulatedRotation, 0);
        System.arraycopy(mTemporaryMatrix, 0, mAccumulatedRotation, 0, 16);

        // Rotate the cube taking the overall rotation into account.
        Matrix.multiplyMM(mTemporaryMatrix, 0, mModelMatrix, 0, mAccumulatedRotation, 0);
        System.arraycopy(mTemporaryMatrix, 0, mModelMatrix, 0, 16);


// ---------------------------------------------------------------------




        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBrickDataHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usb_android);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinates);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);







        drawCube();




        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);

        // Bind the texture to this unit.
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBrickDataHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cube_side_1);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 1);

        // Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinates1);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);


        drawCube1();


        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);

        // Bind the texture to this unit.
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBrickDataHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cube_side_2);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 2);

        // Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinates2);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);


        drawCube2();


        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);

        // Bind the texture to this unit.
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBrickDataHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cube_side_3);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 3);

        // Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinates3);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);


        drawCube3();


        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE4);

        // Bind the texture to this unit.
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBrickDataHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cube_side_4);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 4);

        // Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinates4);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);


        drawCube4();

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE5);

        // Bind the texture to this unit.
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBrickDataHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cube_side_5);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 5);

        // Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinates5);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);


        drawCube5();

        /*

        // Draw a plane
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -2.0f, -5.0f);
        Matrix.scaleM(mModelMatrix, 0, 25.0f, 1.0f, 25.0f);
        Matrix.rotateM(mModelMatrix, 0, slowAngleInDegrees, 0.0f, 1.0f, 0.0f);

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGrassDataHandle);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Pass in the texture coordinate information
        mCubeTextureCoordinatesForPlane.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinatesForPlane);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        drawCube();

        // Draw a point to indicate the light.
        GLES20.glUseProgram(mPointProgramHandle);
        drawLight();

        */
    }

    public void setMinFilter(final int filter) {
        if (mBrickDataHandle != 0 && mGrassDataHandle != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBrickDataHandle);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, filter);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGrassDataHandle);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, filter);
        } else {
            mQueuedMinFilter = filter;
        }
    }

    public void setMagFilter(final int filter) {
        if (mBrickDataHandle != 0 && mGrassDataHandle != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBrickDataHandle);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, filter);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGrassDataHandle);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, filter);
        } else {
            mQueuedMagFilter = filter;
        }
    }

    /**
     * Draws a cube.
     */
    private void drawCube() {
        // Pass in the position information
        mCubePositions.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                0, mCubePositions);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the normal information
        mCubeNormals.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false,
                0, mCubeNormals);

        GLES20.glEnableVertexAttribArray(mNormalHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        // Draw the cube.
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }

    private void drawCube1() {
        // Pass in the position information
        mCubePositions1.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                0, mCubePositions1);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the normal information
        mCubeNormals1.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false,
                0, mCubeNormals1);

        GLES20.glEnableVertexAttribArray(mNormalHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        // Draw the cube.
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }

    private void drawCube2() {
        // Pass in the position information
        mCubePositions2.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                0, mCubePositions2);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the normal information
        mCubeNormals2.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false,
                0, mCubeNormals2);

        GLES20.glEnableVertexAttribArray(mNormalHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        // Draw the cube.
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }

    private void drawCube3() {
        // Pass in the position information
        mCubePositions3.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                0, mCubePositions3);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the normal information
        mCubeNormals3.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false,
                0, mCubeNormals3);

        GLES20.glEnableVertexAttribArray(mNormalHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        // Draw the cube.
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }

    private void drawCube4() {
        // Pass in the position information
        mCubePositions4.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                0, mCubePositions4);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the normal information
        mCubeNormals4.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false,
                0, mCubeNormals4);

        GLES20.glEnableVertexAttribArray(mNormalHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        // Draw the cube.
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }

    private void drawCube5() {
        // Pass in the position information
        mCubePositions5.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                0, mCubePositions5);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the normal information
        mCubeNormals5.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false,
                0, mCubeNormals5);

        GLES20.glEnableVertexAttribArray(mNormalHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        // Draw the cube.
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }


    /**
     * Draws a point representing the position of the light.
     */
    private void drawLight() {
        final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "a_Position");

        // Pass in the position.
        GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

        // Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES20.glDisableVertexAttribArray(pointPositionHandle);

        // Pass in the transformation matrix.
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);
        GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the point.
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
    }

    /**
     * Scaling
     */
    public void changeScale(float scale) {
        if (scaleX * scale > 1.4f)
            return;
        scaleX *= scale;scaleY *= scale;scaleZ *= scale;

     //   Log.d("SCALE: ", scaleX + "");
    }

}
