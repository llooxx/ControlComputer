package com.linorz.controlcomputer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.linorz.controlcomputer.tools.SocketUtils;
import com.linorz.controlcomputer.tools.StaticMethod;
import com.linorz.controlcomputer.zxing.android.CaptureActivity;


public class MainActivity extends AppCompatActivity {
    private final int REQUEST_CODE_SCAN = 0x000;
    private final String DECODED_CONTENT_KEY = "codedContent";
    Button scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = StaticMethod.checkSelfPermissionArray(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            });
            if (permissions.length > 0) {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }

        scan = (Button) findViewById(R.id.btn_scan);
        Button storage = (Button) findViewById(R.id.btn_storage);
        Button touch1 = (Button) findViewById(R.id.btn_touch1);
        Button touch2 = (Button) findViewById(R.id.btn_touch2);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                MainActivity.this.startActivityForResult(intent, REQUEST_CODE_SCAN);
            }
        });
        storage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, StorageActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
        touch1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TouchAbsolutActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
        touch2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TouchPadActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SCAN:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        String content = data.getStringExtra(DECODED_CONTENT_KEY);
                        scan.setText(content);
                        SocketUtils.IP = content;
                    }
                }
                SocketUtils.post("appInfo", new SocketUtils.Params()
                                .add("ip", SocketUtils.getHostIP())
                        , true, new SocketUtils.Connect() {
                            @Override
                            public void onResponse(String response) {

                            }
                        });
                break;
        }
    }


}
