package com.dublick.tutorial03.graph;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.dublick.tutorial03.utils.Util;

/**
 * Created by 3dium on 06.07.2017.
 */

public class GLView extends GLSurfaceView{
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_ROTATE = 1;
    private static final int TOUCH_ZOOM = 2;

    private OpenGLRenderer renderer;

    private PointF pinchStartPoint = new PointF();
    private float pinchStartDistance = 0.0f;
    private int touchMode = TOUCH_NONE;

    private float mPreviousX;
    private float mPreviousY;

    private float density;

    public GLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);

        final DisplayMetrics displayMetrics = new DisplayMetrics();

        renderer = new OpenGLRenderer(context);
        setRenderer(renderer);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mPreviousX = event.getX();
                mPreviousY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    if (touchMode != TOUCH_ROTATE) {
                        mPreviousX = event.getX();
                        mPreviousY = event.getY();
                    }
                    touchMode = TOUCH_ROTATE;
                    float x = event.getX();
                    float y = event.getY();
                    float dx = x - mPreviousX;
                    float dy = y - mPreviousY;
                    mPreviousX = x;
                    mPreviousY = y;
                    renderer.rotate(Util.pxToDp(dy), Util.pxToDp(dx));
                } else if (event.getPointerCount() == 2) {
                    if (touchMode != TOUCH_ZOOM) {
                        pinchStartDistance = getPinchDistance(event);
                        getPinchCenterPoint(event, pinchStartPoint);
                        mPreviousX = pinchStartPoint.x;
                        mPreviousY = pinchStartPoint.y;
                        touchMode = TOUCH_ZOOM;
                    } else {
                        PointF pt = new PointF();
                        getPinchCenterPoint(event, pt);
                        float dx = pt.x - mPreviousX;
                        float dy = pt.y - mPreviousY;
                        mPreviousX = pt.x;
                        mPreviousY = pt.y;
                        float pinchScale = getPinchDistance(event) / pinchStartDistance;
                        pinchStartDistance = getPinchDistance(event);
                        renderer.translate(Util.pxToDp(dx), Util.pxToDp(dy), pinchScale);
                    }
                }
                requestRender();
                break;

            case MotionEvent.ACTION_UP:
                pinchStartPoint.x = 0.0f;
                pinchStartPoint.y = 0.0f;
                touchMode = TOUCH_NONE;
                break;
        }
        return true;
//        if (event != null) {
//
//            float x = event.getX();
//            float y = event.getY();
//
//            if (event.getAction() == MotionEvent.ACTION_MOVE)
//            {
//                if (renderer != null)
//                {
//                    float deltaX = (x - mPreviousX) / density / 2f;
//                    float deltaY = (y - mPreviousY) / density / 2f;
//
//                    renderer.deltaX += deltaX;
//                    renderer.deltaY += deltaY;
//                }
//            }
//
//            mPreviousX = x;
//            mPreviousY = y;
//            return true;
//        } else {
//            return super.onTouchEvent(event);
//        }
    }

    private float getPinchDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void getPinchCenterPoint(MotionEvent event, PointF pt) {
        pt.x = (event.getX(0) + event.getX(1)) * 0.5f;
        pt.y = (event.getY(0) + event.getY(1)) * 0.5f;
    }

    public void setRenderer(float density)
    {
        this.density = density;
    }

}
