package com.linorz.controlcomputer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import java.util.Map;

/**
 * Created by linorz on 2017/7/14.
 */

public class TouchActivity  extends Activity {
    int x, y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_touch);

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        final int screenWidth  = display.getWidth();
        final int screenHeight  = display.getHeight();

        HttpUtils.post(HttpUtils.APP_INFO, new HttpUtils.Connect() {
            @Override
            public Map<String, String> setParams(Map<String, String> params) {
                params.put("pixel_width", screenWidth+"");
                params.put("pixel_height", screenHeight+"");
                return params;
            }

            @Override
            public void onResponse(String response) {

            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = (int) event.getX();
        y = (int) event.getY();
        HttpUtils.post(HttpUtils.POINT, new HttpUtils.Connect() {
            @Override
            public Map<String, String> setParams(Map<String, String> params) {
                params.put("app_x", x+"");
                params.put("app_y", y+"");
                return params;
            }

            @Override
            public void onResponse(String response) {

            }
        });
        return super.onTouchEvent(event);
    }
}
