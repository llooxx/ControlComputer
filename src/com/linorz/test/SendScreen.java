package com.linorz.test;

/**
 * Created by linorz on 2017/7/16.
 */
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
/**
 *
 * @author Knuth
 */
public class SendScreen extends javax.swing.JFrame {

    public static int SERVERPORT=12122;
    private ServerSocket serverSocket;
    private Robot robot;
    public  Dimension screen;
    public Rectangle rect ;
    private Socket socket;
    DefaultListModel dlmsend = new DefaultListModel();

    public SendScreen(int SERVERPORT) {
        initComponents();
        jList1.setModel(dlmsend);
        try {
            serverSocket = new ServerSocket(SERVERPORT);
            //设置超时时间
            serverSocket.setSoTimeout(864000);
            robot = new Robot();
        } catch (Exception e) {
            e.printStackTrace();
        }

        screen = Toolkit.getDefaultToolkit().getScreenSize();  //获取主屏幕的大小

        rect = new Rectangle(screen);                          //构造屏幕大小的矩形

    }
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setViewportView(jList1);

        jButton1.setText("开始广播");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("结束广播");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 389, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(24, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jButton1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jButton2)
                                                .addGap(40, 40, 40))))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(62, 62, 62)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton1)
                                        .addComponent(jButton2))
                                .addContainerGap(61, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        dlmsend.addElement("教师端开始广播！");

        Thread t = new  reThread();
        t.start();
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {

        try {
            // TODO add your handling code here:
            socket.close();
            dlmsend.addElement("广播结束");
            Thread t = new  reThread();
            t.stop();
        } catch (IOException ex) {
            Logger.getLogger(SendScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }



    class reThread extends Thread {

        public void run(){

            while(true)
            {
                try{
                    socket = serverSocket.accept();
                    dlmsend.addElement("学生端口已经连接");
                    ZipOutputStream zip = new ZipOutputStream(new DataOutputStream(socket.getOutputStream()));
                    zip.setLevel(0);     //设置压缩级别,java共8个还是11个压缩级别？



                    BufferedImage img = robot.createScreenCapture(rect);
                    zip.putNextEntry(new ZipEntry("test.jpg"));
                    ImageIO.write(img,"jpg",zip);
                    if(zip!=null)zip.close();
                    dlmsend.addElement("Client正在实时连接");
                    dlmsend.addElement("连接第"+(System.currentTimeMillis()/1000)%24%60+"秒");
                } catch (IOException ioe) {
                    dlmsend.addElement("连接断开");

                } finally {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {e.printStackTrace();}
                    }
                }
            }
        }
    }

    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SendScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SendScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SendScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SendScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

    /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SendScreen(SERVERPORT).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
// End of variables declaration

}