package com.linorz.controlcomputer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.linorz.controlcomputer.tools.DealFile;
import com.linorz.controlcomputer.tools.SocketUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by linorz on 2017/7/15.
 */

public class StorageActivity extends Activity {
    private int defaultBindPort = 10000;    //默认监听端口号为10000
    private int tryBindTimes = 0;           //初始的绑定端口的次数设定为0
    private String savePath = DealFile.getBaseSDCardPath().getPath() + "/";
    private ServerSocket serverSocket;      //服务套接字等待对方的连接和文件发送

    private ExecutorService executorService;    //线程池
    private final int POOL_SIZE = 4;            //单个CPU的线程池大小
    private boolean next = true;
    private StorageAdapter adapter;
    protected RecyclerView recyclerView;
    protected List<Map<String, Object>> items;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            int n = message.what;
            int k = (int) message.obj;

            Map<String, Object> map = items.get(n);
            map.put("progress", k);
            if (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE
                    || (!recyclerView.isComputingLayout())) {
                adapter.notifyItemChanged(n);
            }

            return false;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        items = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StorageAdapter(this.getBaseContext(), items);
        recyclerView.setAdapter(adapter);
        startServer();
    }


    private void startServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bingToServerPort(defaultBindPort);
                    executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
                    Log.e("EEE", "开辟线程数 ： " + Runtime.getRuntime().availableProcessors() * POOL_SIZE);
                } catch (Exception e) {
                    Log.e("EEE", "绑定端口不成功!");
                }
                Socket socket = null;
                while (next) {
                    try {
                        socket = serverSocket.accept();
                        executorService.execute(new FileRunnable(socket));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                executorService.shutdown();
            }
        }).start();
    }

    private void bingToServerPort(int port) throws Exception {
        try {
            serverSocket = new ServerSocket(port);
            Log.e("EEE", port + "服务启动!");
            SocketUtils.post("appFilePort", new SocketUtils.Params().add("port", port));
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

    class FileRunnable implements Runnable {
        private Socket socket;
        private int num = -10;

        public FileRunnable(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            Log.e("EEE", "New connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
            DataInputStream dis = null;
            DataOutputStream dos = null;

            int bufferSize = 8192;
            byte[] buf = new byte[bufferSize];

            String name = null;
            try {
                dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                name = dis.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (name == null) return;

            String filePath = savePath + name;
            num = adapter.add(name,filePath);
            if (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE
                    || (!recyclerView.isComputingLayout())) {
                adapter.notifyItemInserted(num);
            }
            try {
                recyclerView.smoothScrollToPosition(num);
            } catch (Exception e) {

            }

            try {
                long length = dis.readLong();
                dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filePath)));

                int read = 0;
                long passedlen = 0;
                while ((read = dis.read(buf)) != -1) {
                    passedlen += read;
                    dos.write(buf, 0, read);
                    int k = (int) (passedlen * 100L / length);
                    Log.e("EEE", "文件[" + filePath + "]已经接收: " + k + "%");

                    Message message = new Message();
                    message.what = num;
                    message.obj = k;

                    handler.sendMessageDelayed(message, 0);

                }
                Log.e("EEE", "文件: " + filePath + "接收完成!");

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("EEE", "接收文件失败!");

                Message message = new Message();
                message.what = num;
                message.obj = -1;
                handler.sendMessageDelayed(message, 0);

            } finally {
                try {
                    if (dos != null) dos.close();
                    if (dis != null) dis.close();
                    if (socket != null) socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        next = false;
        finish();
    }
}
