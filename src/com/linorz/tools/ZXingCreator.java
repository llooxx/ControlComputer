package com.linorz.tools;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by linorz on 2016/9/9.
 */
public class ZXingCreator {
	public static void main(String[] args) throws Exception {
		// 带logo
		// String text = "http://www.dans88.com.cn";
		// QRCodeUtil.encode(text, "d:/my180.jpg", "d:/", true);
		
		// 不带logo
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("restaurant_id", 2);
		jsonObject.put("seat_id", 2);
		String text = jsonObject.toString();
		QRCodeUtil.encode(text, "", "C:\\Users\\linorz\\Desktop", true);
	}
}
