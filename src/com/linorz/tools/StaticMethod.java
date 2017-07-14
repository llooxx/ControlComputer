package com.linorz.tools;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;



public class StaticMethod {

	public static void linorzPrint(Object... o) {
		if (o.length == 1) {
			System.out.println("****************************");
			System.out.println(o[0].toString());
			System.out.println("****************************");
		} else {
			System.out.println("****************************");
			for (int i = 0; i < o.length - 1; i++) {
				System.out.print(o[i].toString() + " , ");
			}
			System.out.println(o[o.length - 1].toString());
			System.out.println("****************************");
		}

	}

	public static String getHostIP() {
		String hostIp = null;
		try {
			Enumeration<?> nis = NetworkInterface.getNetworkInterfaces();
			InetAddress ia = null;
			while (nis.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) nis.nextElement();
				Enumeration<InetAddress> ias = ni.getInetAddresses();
				while (ias.hasMoreElements()) {
					ia = ias.nextElement();
					if (ia instanceof Inet6Address) {
						continue;// skip ipv6
					}
					String ip_str = ia.getHostAddress();
					if (!"127.0.0.1".equals(ip_str)) {
						hostIp = ia.getHostAddress();
						break;
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return hostIp;
	}
}
