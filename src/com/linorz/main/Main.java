package com.linorz.main;

import com.alibaba.fastjson.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;

/**
 * Created by linorz on 2017/7/14.
 */
public class Main {
    public static String COMPUTER_IP, APP_IP;
    public static int COMPUTER_PORT = 2333, APP_PORT = 9000,APP_FILE_PORT=10000;
    private DatagramSocket ds;
    public static double ratio_width, ratio_height;
    public static int COMPUTER_HEIGHT, COMPUTER_WIDTH;
    public Class<Actions> actionsClass = Actions.class;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                byte[] buf = new byte[1024];
                //服务端在2333端口监听接收到的数据
                ds = new DatagramSocket(COMPUTER_PORT);
                //接收从客户端发送过来的数据
                DatagramPacket dp_receive = new DatagramPacket(buf, 1024);
                System.out.println("server is on，waiting for client to send data......");

                boolean f = true;
                while (f) {
                    //服务器端接收来自客户端的数据
                    ds.receive(dp_receive);

                    String str_receive = new String(dp_receive.getData(), 0, dp_receive.getLength());
                    System.out.println(str_receive);

                    analyzeRequest(str_receive, dp_receive);

                    //由于dp_receive在接收了数据之后，其内部消息长度值会变为实际接收的消息的字节数，所以这里要将dp_receive的内部消息长度重新置为1024
                    dp_receive.setLength(1024);
                }
                ds.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    DropTargetAdapter dta = new DropTargetAdapter() {
        @Override
        public void drop(DropTargetDropEvent dtde) {
            try {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    List<File> list = (List<File>) (dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
                    String[] fileList = new String[list.size()];
                    for (int i = 0; i < fileList.length; i++) {
                        fileList[i]=list.get(i).getAbsolutePath();
                        dtde.dropComplete(true);
                    }
                    new TransferClient(APP_IP, APP_FILE_PORT, fileList).service();
                } else {
                    dtde.rejectDrop();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void analyzeRequest(String request, DatagramPacket dp_receive) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(request);
        boolean isNeedResponse = jsonObject.getBoolean("needresponse");
        Method method = actionsClass.getMethod(jsonObject.getString("action"), JSONObject.class);
        String result = (String) method.invoke(null, jsonObject);
        if (isNeedResponse && result != null) {
            //数据发动到客户端的端口
            DatagramPacket dp_send = new DatagramPacket(result.getBytes(), result.length(), dp_receive.getAddress(), dp_receive.getPort());
            ds.send(dp_send);
        }
    }

    public Main() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        Main.COMPUTER_HEIGHT = d.height;
        Main.COMPUTER_WIDTH = d.width;
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        new MainFrame(dta).setVisible(true);
        new Thread(runnable).start();
    }

    public static void main(String[] args) {
        new Main();
    }

}
