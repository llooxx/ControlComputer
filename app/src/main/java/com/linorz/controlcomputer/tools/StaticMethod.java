package com.linorz.controlcomputer.tools;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.linorz.controlcomputer.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by linorz on 2017/7/16.
 */

public class StaticMethod {
    public static boolean checkEndsWithInStringArray(String checkItsEnd, String[] fileEndings) {
        for (String aEnd : fileEndings) {
            if (checkItsEnd.endsWith(aEnd))
                return true;
        }
        return false;
    }

    private static Uri getUriForFile(Context context, File file) {
        if (context == null || file == null) {
            throw new NullPointerException();
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context.getApplicationContext(),
                    "com.linorz.controlcomputer.fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    public static void openFile(Context context, File file, String fileName) {
        Uri uri = getUriForFile(context, file);
        Intent intent;
        if (checkEndsWithInStringArray(fileName, context.getResources().getStringArray(R.array.fileEndingImage))) {
            intent = OpenFiles.getImageFileIntent(uri);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources().
                getStringArray(R.array.fileEndingWebText))) {
            intent = OpenFiles.getHtmlFileIntent(uri);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources().
                getStringArray(R.array.fileEndingPackage))) {
            intent = OpenFiles.getApkFileIntent(uri);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources().
                getStringArray(R.array.fileEndingAudio))) {
            intent = OpenFiles.getAudioFileIntent(uri);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources().
                getStringArray(R.array.fileEndingVideo))) {
            intent = OpenFiles.getVideoFileIntent(uri);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources().
                getStringArray(R.array.fileEndingText))) {
            intent = OpenFiles.getTextFileIntent(uri);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources().
                getStringArray(R.array.fileEndingPdf))) {
            intent = OpenFiles.getPdfFileIntent(uri);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources().
                getStringArray(R.array.fileEndingWord))) {
            intent = OpenFiles.getWordFileIntent(uri);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources().
                getStringArray(R.array.fileEndingExcel))) {
            intent = OpenFiles.getExcelFileIntent(uri);
            context.startActivity(intent);
        } else if (checkEndsWithInStringArray(fileName, context.getResources().
                getStringArray(R.array.fileEndingPPT))) {
            intent = OpenFiles.getPPTFileIntent(uri);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "无法打开，请安装相应的软件！", Toast.LENGTH_SHORT);
        }

    }

    //批量申请权限
    public static String[] checkSelfPermissionArray(Object cxt, String[] permission) {
        ArrayList<String> permiList = new ArrayList<>();
        for (String p : permission) {
            if (!checkSelfPermissionWrapper(cxt, p)) {
                permiList.add(p);
            }
        }
        if (permiList.size() > 0) {
            return permiList.toArray(new String[permiList.size()]);
        } else {
            return new String[]{};
        }
    }

    //权限检查
    @TargetApi(Build.VERSION_CODES.M)
    private static boolean checkSelfPermissionWrapper(Object cxt, String permission) {
        if (cxt instanceof Activity) {
            Activity activity = (Activity) cxt;
            return ActivityCompat.checkSelfPermission(activity,
                    permission) == PackageManager.PERMISSION_GRANTED;
        } else if (cxt instanceof Fragment) {
            Fragment fragment = (Fragment) cxt;
            return fragment.getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            throw new RuntimeException("cxt is net a activity or fragment");
        }
    }
}
