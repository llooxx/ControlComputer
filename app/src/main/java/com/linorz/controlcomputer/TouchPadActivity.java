package com.linorz.controlcomputer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.linorz.controlcomputer.tools.SocketUtils;

/**
 * Created by linorz on 2017/7/14.
 */

public class TouchPadActivity extends Activity {
    double x, y, start_x, start_y, last_x, last_y;
    int screenWidth, screenHeight, width, height;
    double scale_size = 15.0 / 20;
    double scale = 1.1;

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
    }

    long current_time = 0;
    long last_time = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                if (y < (screenHeight - height) / 2) {
                    if (Math.abs(x - last_x) > screenWidth * 0.05) {
                        SocketUtils.post((x - last_x < 0) ? "wheelUp" : "wheelDown", new SocketUtils.Params());
                        last_x = x;
                    }
                } else
                    SocketUtils.post("movePoint", new SocketUtils.Params()
                            .add("scale_x", scale * (x - start_x) / screenWidth)
                            .add("scale_y", scale * (y - start_y) / screenHeight));
                break;
            case MotionEvent.ACTION_DOWN:
                current_time = System.currentTimeMillis();
                if (current_time - last_time < 200) {
                    SocketUtils.post("pressPoint", new SocketUtils.Params()
                            .add("scale_x", scale * (x - start_x) / screenWidth)
                            .add("scale_y", scale * (y - start_y) / screenHeight));
                }
                last_time = current_time;
                start_x = x;
                start_y = y;
                last_x = x;
                last_y = y;
                break;
            case MotionEvent.ACTION_UP:
                SocketUtils.post("upPoint", new SocketUtils.Params()
                        .add("scale_x", scale * (x - start_x) / screenWidth)
                        .add("scale_y", scale * (y - start_y) / screenHeight));
                break;
            case MotionEvent.ACTION_POINTER_UP:
                // 第二个手指抬起事件
                SocketUtils.post("doublePoint", new SocketUtils.Params());
                break;
        }


        return super.onTouchEvent(event);
    }
}
