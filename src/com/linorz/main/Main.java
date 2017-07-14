package com.linorz.main;

import com.alibaba.fastjson.JSONObject;
import com.linorz.tools.QRCodeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by linorz on 2017/7/14.
 */
public class Main {
    private String COMPUTER_IP;
    private int INPORT = 2333;
    private DatagramSocket ds;
    public static double ratio_width, ratio_height;
    public static int COMPUTER_HEIGHT;
    public static int COMPUTER_WIDTH;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                byte[] buf = new byte[1024];
                //服务端在2333端口监听接收到的数据
                ds = new DatagramSocket(INPORT);
                //接收从客户端发送过来的数据
                DatagramPacket dp_receive = new DatagramPacket(buf, 1024);
                System.out.println("server is on，waiting for client to send data......");

                boolean f = true;
                while (f) {
                    //服务器端接收来自客户端的数据
                    ds.receive(dp_receive);

                    String str_receive =
                            new String(dp_receive.getData(), 0, dp_receive.getLength());
                    System.out.println(str_receive);

                    analyzeRequest(str_receive, dp_receive);

                    //由于dp_receive在接收了数据之后，其内部消息长度值会变为实际接收的消息的字节数，
                    //所以这里要将dp_receive的内部消息长度重新置为1024
                    dp_receive.setLength(1024);
                }
                ds.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void analyzeRequest(String request, DatagramPacket dp_receive) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(request);
        boolean isNeedResponse = jsonObject.getBoolean("needresponse");
        String result = "";
        switch (jsonObject.getString("action")) {
            case "appInfo":
                result = Actions.appInfo(jsonObject);
                break;
            case "currentPoint":
                result = Actions.currentPoint(jsonObject);
                break;
            case "movePoint":
                result = Actions.movePoint(jsonObject);
                break;
            case "upPoint":
                result = Actions.upPoint(jsonObject);
                break;
            case "doublePoint":
                result = Actions.doublePoint(jsonObject);
                break;
            case "pressPoint":
                result = Actions.pressPoint(jsonObject);
                break;
            case "wheelUp":
                result = Actions.wheelUp(jsonObject);
                break;
            case "wheelDown":
                result = Actions.wheelDown(jsonObject);
                break;
            case "test":
                result = Actions.test(jsonObject);
                break;
            default:
                break;
        }
        if (isNeedResponse) {
            //数据发动到客户端的端口
            DatagramPacket dp_send = new DatagramPacket(result.getBytes(), result.length(), dp_receive.getAddress(), dp_receive.getPort());
            ds.send(dp_send);
        }
    }


    public Main() {
        initFrame();
        new Thread(runnable).start();
    }


    //IP的二维码
    public JPanel getIPQRPanel() {
        // 获得本机IP
        try {
            COMPUTER_IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println(COMPUTER_IP);
        try {
            BufferedImage bi = QRCodeUtil.createImage(COMPUTER_IP,
                    null, false);
            // 利用JPanel添加背景图片
            JPanel jp = new JPanel() {
                private static final long serialVersionUID = 1L;

                protected void paintComponent(Graphics g) {
                    g.drawImage(bi, 0, 0, bi.getWidth(), bi.getHeight(), this);
                }

            };
            jp.setPreferredSize(new Dimension(bi.getWidth() * 21 / 20, bi.getHeight() * 22 / 20));
            return jp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 初始化窗口
    public void initFrame() {
        JFrame jframe = new JFrame();
        JPanel jp = getIPQRPanel();

        jframe.setTitle("ip");
        jframe.setLocation(200, 200);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setPreferredSize(jp.getPreferredSize());
        jframe.setContentPane(jp);
        jframe.pack();
        jframe.setVisible(true);

    }

    public static void main(String[] args) {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        Main.COMPUTER_HEIGHT = d.height;
        Main.COMPUTER_WIDTH = d.width;
        new Main();
    }

}
