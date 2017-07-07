package com.dublick.tutorial03;

import android.app.FragmentManager;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.dublick.tutorial03.graph.GLView;

// Источник: http://www.learnopengles.com/android-lesson-eight-an-introduction-to-index-buffer-objects-ibos/


public class MainActivity extends AppCompatActivity {

    GLView glView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        glView = (GLView) findViewById(R.id.gl_view);

//        final DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//
//        glView.setRenderer(displayMetrics.density);
        Fragment_PlyViewer fragment_plyViewer = new Fragment_PlyViewer();

        FragmentManager fragmentManager = this.getFragmentManager();
        fragmentManager.beginTransaction().add(R.id.flContent, fragment_plyViewer).commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        glView.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration conf) {
        super.onConfigurationChanged(conf);
    }

}
