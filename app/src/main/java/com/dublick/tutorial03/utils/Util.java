package com.dublick.tutorial03.utils;

import android.content.Context;

import com.dublick.tutorial03.myApplication;

/**
 * Created by 3dium on 07.07.2017.
 */

public class Util {
    public static float pxToDp(float px) {
        return px / getDensityScalar();
    }

    private static float getDensityScalar() {
        return myApplication.getInstance().getResources().getDisplayMetrics().density;
    }
}
