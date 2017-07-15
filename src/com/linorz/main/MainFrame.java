package com.linorz.main;

/**
 * Created by linorz on 2017/7/15.
 */

import com.linorz.tools.QRCodeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainFrame extends JFrame {
    JPanel jp1;
    JPanel jp2;

    public MainFrame(DropTargetAdapter dta) {
        jp1 = new JPanel();
        jp1.setBackground(Color.WHITE);
        jp2 = getIPQRPanel();
        jp1.add(jp2, BorderLayout.CENTER);


        getContentPane().add(jp1, BorderLayout.CENTER);
        setSize(500, 500);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocation(400, 200);
        setTitle("ControlComputer");
        drag(dta);
    }

    //IP的二维码
    public JPanel getIPQRPanel() {
        // 获得本机IP
        try {
            Main.COMPUTER_IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println(Main.COMPUTER_IP);
        try {
            BufferedImage bi = QRCodeUtil.createImage(Main.COMPUTER_IP, null, false);
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

    public void drag(DropTargetAdapter dta) {
        new DropTarget(jp1, DnDConstants.ACTION_COPY_OR_MOVE, dta);
    }

//    public static void main(String[] args) {
//        try {
//            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (UnsupportedLookAndFeelException e) {
//            e.printStackTrace();
//        }
//        new MainFrame().setVisible(true);
//    }

}