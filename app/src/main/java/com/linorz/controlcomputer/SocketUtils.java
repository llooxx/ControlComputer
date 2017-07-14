package com.linorz.controlcomputer;

import android.os.AsyncTask;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by linorz on 2017/7/14.
 */

public class SocketUtils extends AsyncTask<String, Void, String> {
    public int SERVER_PORT = 2333;
    public int CLIENT_PORT = 9000;
    private Connect connect;
    private int TIMEOUT = 5000;  //设置接收数据的超时时间
    private int MAXNUM = 1;      //设置重发数据的最多次数
    private static boolean isNeedResponse = true;

    public SocketUtils(Connect connect, boolean isNeedResponse) {
        this.isNeedResponse = isNeedResponse;
        this.connect = connect;
    }

    public SocketUtils(Connect connect) {
        this.connect = connect;
    }


    public static void post(String action, Params params, Connect connect) {
        params.add("action", action);
        params.add("needresponse",isNeedResponse);
        new SocketUtils(connect).execute(params.toJson());
    }

    public static void post(String action, Params params, boolean isNeedResponse, Connect connect) {
        params.add("action", action);
        params.add("needresponse",isNeedResponse);
        new SocketUtils(connect, isNeedResponse).execute(params.toJson());
    }

    @Override
    protected String doInBackground(String... strings) {
        String result = "";
        String str_send = strings[0];
        byte[] buf = new byte[1024];
        //客户端在9000端口监听接收到的数据
        DatagramSocket ds = null;
        InetAddress loc = null;
        try {
            ds = new DatagramSocket(CLIENT_PORT);
            loc = InetAddress.getByName(HttpUtils.IP);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
        //定义用来发送数据的DatagramPacket实例
        DatagramPacket dp_send = new DatagramPacket(str_send.getBytes(), str_send.length(), loc, SERVER_PORT);
        //定义用来接收数据的DatagramPacket实例
        DatagramPacket dp_receive = new DatagramPacket(buf, 1024);
        //数据发向2333端口
        try {
            ds.setSoTimeout(TIMEOUT);              //设置接收数据时阻塞的最长时间
        } catch (SocketException e) {
            e.printStackTrace();
        }

        if (isNeedResponse) {
            int tries = 0;                         //重发数据的次数
            boolean receivedResponse = false;     //是否接收到数据的标志位
            //直到接收到数据，或者重发次数达到预定值，则退出循环
            while (!receivedResponse && tries < MAXNUM) {
                try {
                    //发送数据
                    ds.send(dp_send);
                    //接收从服务端发送回来的数据
                    ds.receive(dp_receive);
                    //如果接收到的数据不是来自目标地址，则抛出异常
                    if (!dp_receive.getAddress().equals(loc)) {
                        throw new IOException("Received packet from an umknown source");
                    }
                    //如果接收到数据。则将receivedResponse标志位改为true，从而退出循环
                    receivedResponse = true;
                } catch (InterruptedIOException e) {
                    //如果接收数据时阻塞超时，重发并减少一次重发的次数
                    tries += 1;
                    Log.e("EEE", "Time out," + (MAXNUM - tries) + " more tries...");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (receivedResponse) {
                //如果收到数据，则打印出来
                Log.e("EEE", "client received data from server：");
                result = new String(dp_receive.getData(), 0, dp_receive.getLength());
                Log.e("EEE", result);
                //由于dp_receive在接收了数据之后，其内部消息长度值会变为实际接收的消息的字节数，
                //所以这里要将dp_receive的内部消息长度重新置为1024
                dp_receive.setLength(1024);
            } else {
                //如果重发MAXNUM次数据后，仍未获得服务器发送回来的数据，则打印如下信息
                Log.e("EEE", "No response -- give up.");
            }
        } else {
            //发送数据
            try {
                ds.send(dp_send);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ds.close();
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        connect.onResponse(result);
    }

    public interface Connect {
        void onResponse(String response);
    }

    public static class Params {
        JSONObject jo = new JSONObject();

        Params add(String key, Object value) {
            jo.put(key, value);
            return this;
        }

        public String toJson() {
            return jo.toJSONString();
        }

    }

}
