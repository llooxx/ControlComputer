package com.linorz.main;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by linorz on 2017/7/16.
 */
public class ScreenClient {
	private String ip;
	private int port;
	private Thread thread;

	public ScreenClient(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public void sendScreen() {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize(); // 获取主屏幕的大小
		Rectangle rect = new Rectangle(dimension); // 构造屏幕大小的矩形

		thread = new Thread(new Runnable() {
			Socket socket;

			@Override
			public void run() {
				Robot robot = null;
				try {
					robot = new Robot();
				} catch (AWTException e) {
					e.printStackTrace();
				}
				while (true) {
					createConnection();
					try {
						DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
						BufferedImage img = robot.createScreenCapture(rect);
						Point p = MouseInfo.getPointerInfo().getLocation();
						Graphics2D g2 = img.createGraphics();
						g2.setColor(new Color(255, 0, 0, 200));
						g2.fillRoundRect(p.x - 10, p.y - 10, 20, 20, 20, 20);

						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(baos);
						encoder.encode(img);

						byte[] data = baos.toByteArray();
						dos.write(zip(data));
						dos.flush();
						dos.close();
						baos.close();
						System.out.println("已发送");
					} catch (IOException ioe) {
						System.out.println("失败");
					}
					if (socket != null) {
						try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			}

			private boolean createConnection() {
				try {
					socket = new Socket(ip, port);
					System.out.println("连接成功！");
					return true;
				} catch (Exception e) {
					System.out.println("连接失败！");
					return false;
				}
			}
		});
		thread.start();
	}

	public void stop() {
		thread.stop();
	}

	public static byte[] zip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ZipOutputStream zip = new ZipOutputStream(bos);
			zip.setLevel(9);
			ZipEntry entry = new ZipEntry("zip");
			entry.setSize(data.length);
			zip.putNextEntry(entry);
			zip.write(data);
			zip.closeEntry();
			zip.close();
			b = bos.toByteArray();
			bos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

}
