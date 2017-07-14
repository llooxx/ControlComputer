package com.linorz.controlcomputer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;


/**
 * Created by linorz on 2017/7/14.
 */

public class TouchActivity2 extends Activity {
    float x, y;
    int screenWidth, screenHeight, width, height;
    double scale_size = 15.0 / 20;
    double temp = (1 - scale_size) / scale_size / 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_touch);

        View view = findViewById(R.id.touch_view);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        width = (int) (screenWidth * scale_size);
        height = (int) (screenHeight * scale_size);
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.width = width;
        lp.height = height;
        view.setLayoutParams(lp);

//        SocketUtils.post("appInfo", new SocketUtils.Params()
//                        .add("pixel_width", width)
//                        .add("pixel_height", height)
//                , new SocketUtils.Connect() {
//                    @Override
//                    public void onResponse(String response) {
//
//                    }
//                });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();
        SocketUtils.post("currentPoint", new SocketUtils.Params()
                        .add("scale_x", x / width - temp)
                        .add("scale_y", y / height - temp)
                , false
                , new SocketUtils.Connect() {
                    @Override
                    public void onResponse(String response) {

                    }
                });
        return super.onTouchEvent(event);
    }
}
