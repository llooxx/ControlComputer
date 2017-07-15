package com.linorz.controlcomputer.tools;

import android.os.Environment;

import java.io.File;

public class DealFile {// 将来static
    private static final String BASE_SD_PATH = "ControlComputer";

    /**
     * 获取本软件在存储卡的基本路径
     */
    public static File getBaseSDCardPath() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            try {
                File root_path = new File(
                        Environment.getExternalStorageDirectory(), BASE_SD_PATH);// 存在无卡、无权风险
                if (!root_path.exists())
                    root_path.mkdir();
                return root_path;
            } catch (Exception e) {
                return null;// 禁止权限在此处捕获
            }
        }
        return null;
    }

    /**
     * 清除缓存
     */
    public static void clearCatch() {
        final File root_path = new File(
                Environment.getExternalStorageDirectory(), BASE_SD_PATH);
        if (root_path.exists())
            try {
                deleteFile(root_path);
            } catch (Exception e) {
            }
    }

    public static String getFilePath() {
        return new File(Environment.getExternalStorageDirectory(), BASE_SD_PATH).getAbsolutePath();
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param file 要删除的根目录
     */
    private static void deleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            if (file.getName().equals("Message"))
                return;//暂时不删除消息界面
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                deleteFile(f);
            }
            file.delete();
        }
    }

}
