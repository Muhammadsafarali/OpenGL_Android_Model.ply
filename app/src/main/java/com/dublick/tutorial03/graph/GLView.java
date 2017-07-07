package com.dublick.tutorial03.graph;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by 3dium on 06.07.2017.
 */

public class GLView extends GLSurfaceView{

    private OpenGLRenderer renderer;
    ScaleGestureDetector SGD;
    private float mPreviousX;
    private float mPreviousY;

    private float density;

    public GLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);

        final DisplayMetrics displayMetrics = new DisplayMetrics();

        renderer = new OpenGLRenderer(context);
        setRenderer(renderer);
        SGD = new ScaleGestureDetector(context, new ScaleListener());
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event != null) {

            float x = event.getX();
            float y = event.getY();

            if (event.getAction() == MotionEvent.ACTION_MOVE)
            {
                if (renderer != null)
                {
                    float deltaX = (x - mPreviousX) / density / 2f;
                    float deltaY = (y - mPreviousY) / density / 2f;

                    renderer.deltaX += deltaX;
                    renderer.deltaY += deltaY;
                }
            }

            mPreviousX = x;
            mPreviousY = y;
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    public void setRenderer(float density)
    {
        this.density = density;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            renderer.scale *= detector.getScaleFactor();
            return true;
        }
    }
}
