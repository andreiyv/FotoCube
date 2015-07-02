package com.easy.fotocubik;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;

public class LessonSixGLSurfaceView extends GLSurfaceView {
    private LessonSixRenderer mRenderer;

    // Offsets for touch events
    private float mPreviousX=0.0f;
    private float mPreviousY=0.0f;

    private float mDensity;

    // pinch to zoom
    public float oldDist = 100.0f;
    public float newDist;

    int mode = 0;

    // touch events
    private final int NONE = 0;
    private final int DRAG = 0;
    private final int ZOOM = 0;

    public LessonSixGLSurfaceView(Context context) {
        super(context);
    }

    public LessonSixGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
        mRenderer.n = 0;

            mRenderer.mode = 0;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:            // one touch: drag

                    Log.d("ACTION_DOWN", "mode=" + mode);

                    mRenderer.mDeltaX = 0.0f;
                    mRenderer.mDeltaY = 0.0f;

                    requestRender();

                    break;
/*
                case MotionEvent.ACTION_POINTER_DOWN:    // two touches: zoom

                    oldDist = spacing(event);
                    if (oldDist > 10.0f) {
                        mode = ZOOM; // zoom
                    }
                    Log.d("ACTION_POINTER_DOWN", "mode=" + mode);
                    break;
*/
                case MotionEvent.ACTION_UP:        // no mode
                    mode = NONE;
                    Log.d("ACTION_UP", "mode=NONE");

                    //oldDist = 100.0f;
                    break;
                case MotionEvent.ACTION_POINTER_UP:        // no mode
                    mode = NONE;
                    Log.d("ACTION_POINTER_UP", "mode=NONE");

    //`                oldDist = 100.0f;
                    break;

                case MotionEvent.ACTION_MOVE:                        // rotation
                    if (event.getPointerCount() > 1 && mode == ZOOM) {
                        newDist = spacing(event);
                //        Log.d("ACTION_MOVE_2_pnt: ", "OldDist: " + oldDist + ", NewDist: " + newDist);
                        mRenderer.mode = 1;
                        if (newDist > 10.0f) {
                            float scale = newDist / oldDist; // scale
                            // scale in the renderer
                            //mRenderer.changeScale(scale);
                            if ( newDist > oldDist) {
                                mRenderer.delta_zoom = newDist / 5000.0f;
                            } else {
                                mRenderer.delta_zoom = -1.0f * newDist / 5000.0f;
                            }
                            requestRender();
                            oldDist = newDist;
                        }
                    } else if (event.getPointerCount() == 1 && mode == DRAG) {

                        if ( mRenderer != null) {
                            float deltaX = (x - mPreviousX) / mDensity / 1.0f;
                            float deltaY = (y - mPreviousY) / mDensity / 1.0f;

                            mRenderer.mDeltaX += deltaX;
                            mRenderer.mDeltaY += deltaY;
                            requestRender();
                    //        Log.d("ACTION_MOVE_1_pointer: ", "deltaX: " + deltaX + ", deltaY: " + deltaY);
                        }


/*                    float dx = x - mPreviousX;
                    float dy = y - mPreviousY;
                    mRenderer.mAngleX += dx * TOUCH_SCALE_FACTOR;
                    mRenderer.mAngleY += dy * TOUCH_SCALE_FACTOR;
                    mGLSurfaceView.requestRender();
*/
                    }

                    break;

            }

        mPreviousX = x;
        mPreviousY = y;

        return true;


// ***************************************************************************
    }

    // finds spacing

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    // Hides superclass method.
    public void setRenderer(LessonSixRenderer renderer, float density) {
        mRenderer = renderer;
        mDensity = density;
        super.setRenderer(renderer);
    }
}

