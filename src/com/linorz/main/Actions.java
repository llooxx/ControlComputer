package com.linorz.main;

import com.alibaba.fastjson.JSONObject;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 * Created by linorz on 2017/7/14.
 */
public class Actions {
    static double start_x = 0, start_y = 0;
    static int xx, yy;
    static boolean isPress = false;

    public static String test(JSONObject request) {
        int test = request.getInteger("test");
        return Constant.SUCCESS;
    }

    public static String appInfo(JSONObject request) {
        String ip = request.getString("ip");
        Main.APP_IP = ip;

        return Constant.SUCCESS;
    }

    // 当前屏幕上的点位置
    public static String currentPoint(JSONObject request) {
        double scale_x = request.getFloat("scale_x");
        double scale_y = request.getFloat("scale_y");
        try {
            Robot robot = new Robot();
            robot.mouseMove((int) (scale_y * Main.COMPUTER_WIDTH), (int) ((1 - scale_x) * Main.COMPUTER_HEIGHT));
        } catch (AWTException e) {
            e.printStackTrace();
        }

        return Constant.SUCCESS;
    }

    public static String movePoint(JSONObject request) {
        double scale_x = request.getFloat("scale_x");
        double scale_y = request.getFloat("scale_y");
        try {
            Robot robot = new Robot();
            xx = (int) (start_x + scale_y * Main.COMPUTER_WIDTH);
            yy = (int) (start_y - scale_x * Main.COMPUTER_HEIGHT);
            if (xx > Main.COMPUTER_WIDTH)
                xx = Main.COMPUTER_WIDTH;
            if (xx < 0)
                xx = 0;
            if (yy > Main.COMPUTER_HEIGHT)
                yy = Main.COMPUTER_HEIGHT;
            if (yy < 0)
                yy = 0;
            robot.mouseMove(xx, yy);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        return Constant.SUCCESS;
    }

    public static String upPoint(JSONObject request) {
        double scale_x = request.getFloat("scale_x");
        double scale_y = request.getFloat("scale_y");
        if (isPress)
            try {
                isPress = false;
                Robot robot = new Robot();
                robot.mouseRelease(InputEvent.BUTTON1_MASK);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        if (scale_x == 0) {
            try {
                Robot robot = new Robot();
                robot.mousePress(InputEvent.BUTTON1_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_MASK);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
        start_x = xx;
        start_y = yy;
        return Constant.SUCCESS;
    }

    public static String doublePoint(JSONObject request) {
        try {
            Robot robot = new Robot();
            robot.mousePress(InputEvent.BUTTON3_MASK);
            robot.mouseRelease(InputEvent.BUTTON3_MASK);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return Constant.SUCCESS;
    }

    public static String pressPoint(JSONObject request) {
        isPress = true;
        try {
            Robot robot = new Robot();
            robot.mousePress(InputEvent.BUTTON1_MASK);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return Constant.SUCCESS;
    }

    public static String wheelUp(JSONObject request) {
        try {
            Robot robot = new Robot();
            robot.mouseWheel(-1);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return Constant.SUCCESS;
    }

    public static String wheelDown(JSONObject request) {
        try {
            Robot robot = new Robot();
            robot.mouseWheel(1);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return Constant.SUCCESS;
    }
}
