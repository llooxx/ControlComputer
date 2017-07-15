package com.linorz.main;

/**
 * Created by linorz on 2017/7/15.
 */

import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TransferClient {

    private String[] fileList;
    private String ip;
    private int port;

    public TransferClient(String ip, int port, String[] fileList) {
        this.ip = ip;
        this.port = port;
        this.fileList = fileList;
    }

    public void service() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        Vector<Integer> vector = getRandom(fileList.length);
        for (Integer integer : vector) {
            String filePath = fileList[integer];
            executorService.execute(sendFile(filePath));
        }
    }


    private Vector<Integer> getRandom(int size) {
        Vector<Integer> v = new Vector<Integer>();
        Random r = new Random();
        boolean b = true;
        while (b) {
            int i = r.nextInt(size);
            if (!v.contains(i))
                v.add(i);
            if (v.size() == size)
                b = false;
        }
        return v;
    }

    private Runnable sendFile(final String filePath) {
        return new Runnable() {

            private Socket socket = null;

            public void run() {
                System.out.println("开始发送文件:" + filePath);
                File file = new File(filePath);
                if (createConnection()) {
                    int bufferSize = 8192;
                    byte[] buf = new byte[bufferSize];
                    try {
                        DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                        dos.writeUTF(file.getName());
                        dos.flush();
                        dos.writeLong(file.length());
                        dos.flush();

                        int read = 0;
                        int passedlen = 0;
                        long length = file.length();    //获得要发送文件的长度
                        while ((read = fis.read(buf)) != -1) {
                            passedlen += read;
                            System.out.println("已经完成文件 [" + file.getName() + "]百分比: " + passedlen * 100L / length + "%");
                            dos.write(buf, 0, read);
                        }

                        dos.flush();
                        fis.close();
                        dos.close();
                        socket.close();
                        System.out.println("文件 " + filePath + "传输完成!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            private boolean createConnection() {
                try {
                    socket = new Socket(ip, port);
                    System.out.println("连接服务器成功！");
                    return true;
                } catch (Exception e) {
                    System.out.println("连接服务器失败！");
                    return false;
                }
            }

        };
    }

//    public static void main(String[] args) {
//        new TransferClient("", 0, new String[]{}).service();
//    }
}
