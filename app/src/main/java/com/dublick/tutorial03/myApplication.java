package com.dublick.tutorial03;

import android.app.Application;

/**
 * Created by 3dium on 07.07.2017.
 */

public class myApplication extends Application {

    private static myApplication instance;

    public static myApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
