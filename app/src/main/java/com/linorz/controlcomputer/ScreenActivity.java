package com.linorz.controlcomputer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.linorz.controlcomputer.tools.SocketUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipInputStream;

/**
 * Created by linorz on 2017/7/16.
 */

public class ScreenActivity extends Activity {
    private SurfaceView screen;
    private SurfaceHolder holder; //控制对象
    private ServerSocket serverSocket;
    private ExecutorService executorService;    //线程池
    private int defaultBindPort = 20000;    //默认监听端口号为20000
    private int tryBindTimes = 0;
    private Paint paint;
    private Rect rect;
    private Thread thread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activty_screen);

        screen = (SurfaceView) findViewById(R.id.screen_surface);
        holder = screen.getHolder();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        startServer();
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
                    executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                            .availableProcessors() * 2);
                } catch (Exception e) {
                    Log.e("EEE", "绑定端口不成功!");
                }
                Socket socket = null;
                SocketUtils.post("screenSend", new SocketUtils.Params());
                while (true) {
                    try {
                        socket = serverSocket.accept();
                        executorService.execute(drawCanvas(socket));
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
                executorService.shutdown();
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

                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                    Canvas canvas = holder.lockCanvas();
                    canvas.drawBitmap(bitmap, null, rect, paint);
                    holder.unlockCanvasAndPost(canvas);

                    TimeUnit.MILLISECONDS.sleep(10);// 接收图片间隔时间
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


    @Override
    public void onBackPressed() {
        SocketUtils.post("stopScreenSend", new SocketUtils.Params());
        thread.stop();
        super.onBackPressed();
    }
}
