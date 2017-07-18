package com.linorz.controlcomputer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.linorz.controlcomputer.tools.SocketUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.zip.ZipInputStream;

/**
 * Created by linorz on 2017/7/16.
 */

public class ScreenActivity extends Activity {
    private TextureView screen;
    private ServerSocket serverSocket;
    private int defaultBindPort = 20000;    //默认监听端口号为20000
    private int tryBindTimes = 0;
    private Rect rect;
    private Thread thread;
    private BitmapFactory.Options options;

    int screenWidth, screenHeight;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            byte[] image = (byte[]) message.obj;
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);
            Canvas canvas = screen.lockCanvas();
            canvas.drawBitmap(bitmap, null, rect, null);
            screen.unlockCanvasAndPost(canvas);
            bitmap.recycle();
            return false;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activty_screen);
        hideBottomUIMenu();

        screen = (TextureView) findViewById(R.id.screen_surface);
        options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        startServer();
    }

    double x, y, start_x, start_y, last_x, last_y;
    long current_time = 0;
    long last_time = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                SocketUtils.post("currentPoint", new SocketUtils.Params()
                        .add("scale_x", 1 - y / screenHeight)
                        .add("scale_y", x / screenWidth));
                break;
            case MotionEvent.ACTION_DOWN:
                current_time = System.currentTimeMillis();
                if (current_time - last_time < 200) {
                    SocketUtils.post("pressPoint", new SocketUtils.Params()
                            .add("scale_x", 0)
                            .add("scale_y", 0));
                }
                last_time = current_time;
                start_x = x;
                start_y = y;
                last_x = x;
                last_y = y;
                break;
            case MotionEvent.ACTION_UP:
                SocketUtils.post("upPoint", new SocketUtils.Params()
                        .add("scale_x", 0)
                        .add("scale_y", 0));
                break;
            case MotionEvent.ACTION_POINTER_UP:
                // 第二个手指抬起事件
                SocketUtils.post("doublePoint", new SocketUtils.Params());
                break;
        }

        return super.onTouchEvent(event);
    }

    private void bingToServerPort(int port) throws Exception {
        try {
            serverSocket = new ServerSocket(port);
            Log.e("EEE", port + "服务启动!");
            SocketUtils.post("appFilePort",
                    new SocketUtils.Params().add("port", port).add("ip", SocketUtils.getHostIP()));
        } catch (Exception e) {
            this.tryBindTimes = this.tryBindTimes + 1;
            port = port + this.tryBindTimes;
            if (this.tryBindTimes >= 20) {
                throw new Exception("您已经尝试很多次了，但是仍无法绑定到指定的端口!请重新选择绑定的默认端口号");
            }
            //递归绑定端口
            this.bingToServerPort(port);
        }
    }

    public void startServer() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bingToServerPort(defaultBindPort);
                } catch (Exception e) {
                    Log.e("EEE", "绑定端口不成功!");
                }
                Socket socket = null;
                SocketUtils.post("screenSend", new SocketUtils.Params());
                while (true) {
                    try {
                        socket = serverSocket.accept();
                        drawCanvas(socket).start();
                    } catch (IOException e) {
                        Log.e("EEE", "连接断开");
                        break;
                    }
                }
                try {
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public Thread drawCanvas(final Socket socket) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                DataInputStream dis = null;
                try {
                    dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    byte[] image = unZip(dis);

                    if (rect == null)
                        rect = new Rect(0, 0, screen.getMeasuredWidth(), screen.getMeasuredHeight());

                    Message message = new Message();
                    message.obj = image;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (dis != null) dis.close();
                        if (socket != null) socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static byte[] unZip(DataInputStream dis) {
        byte[] b = null;
        try {
            ZipInputStream zip = new ZipInputStream(dis);
            while (zip.getNextEntry() != null) {
                byte[] buf = new byte[1024];
                int num = -1;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((num = zip.read(buf, 0, buf.length)) != -1) {
                    baos.write(buf, 0, num);
                }
                b = baos.toByteArray();
                baos.flush();
                baos.close();
            }
            zip.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public void onBackPressed() {
        SocketUtils.post("stopScreenSend", new SocketUtils.Params());
        thread.stop();
        super.onBackPressed();
    }
}
