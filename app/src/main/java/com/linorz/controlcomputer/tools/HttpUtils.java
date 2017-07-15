package com.linorz.controlcomputer.tools;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils extends AsyncTask<String, Void, String> {
    public static String IP = "192.168.1.106";
    public final static String POINT = "currentPoint";
    public final static String APP_INFO = "appInfo";
    private Connect connect;

    public HttpUtils(Connect connect) {
        this.connect = connect;
    }

    public static String getUrl(String url) {
        return IP + "/controlcomputer/" + url;
    }

    public static String sendPostMessage(String path, Map<String, String> params,
                                         String encode) {
        StringBuffer buffer = new StringBuffer();
        try {
            if (params != null && !params.isEmpty()) {
//                for (Map.Entry<String, String> entry : params.entrySet()) {
//                    buffer.append(entry.getKey()).append("=").append(
//                            URLEncoder.encode(entry.getValue(), encode))
//                            .append("&");
//                }
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    buffer.append(entry.getKey()).append("=").append(
                            entry.getValue())
                            .append("&");
                }
                buffer.deleteCharAt(buffer.length() - 1);
            }

            System.out.println("-->>" + buffer.toString());
            URL url = new URL(path);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(3000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            byte[] mydata = buffer.toString().getBytes();
            urlConnection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("Content-Length",
                    String.valueOf(mydata.length));
            OutputStream outputStream = urlConnection.getOutputStream();
            outputStream.write(mydata, 0, mydata.length);
            outputStream.close();
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 200) {
                return changeInputStream(urlConnection.getInputStream(), encode);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String changeInputStream(InputStream inputStream,
                                            String encode) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        String result = "";
        if (inputStream != null) {
            try {
                while ((len = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, len);
                }
                result = new String(outputStream.toByteArray(), encode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    public static void post(String url, Connect connect) {
        new HttpUtils(connect).execute(url);
    }

    @Override
    protected String doInBackground(String... strings) {
        if (connect == null) return "";
        Map<String, String> params = connect.setParams(new HashMap<String, String>());
        String url2 = getUrl(strings[0]);
        Log.e("EEE", url2);
        String result = HttpUtils.sendPostMessage(url2, params, "utf-8");
        Log.e("EEE", result);
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        connect.onResponse(result);
    }

    public interface Connect {
        public Map<String, String> setParams(Map<String, String> params);

        public void onResponse(String response);
    }
}
