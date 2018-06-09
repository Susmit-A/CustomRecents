package com.susmit.customrecents;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    FrameLayout appLayout;
    static String THUMBNAIL_PATH = "/data/system_ce/0/snapshots";
    static String RECENT_TASKS_XML_PATH = "/data/system_ce/0/recent_tasks";
    static String TEMP_THUMBNAIL_DIR;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_main);
        TEMP_THUMBNAIL_DIR = getFilesDir().getPath() + "/temp";
        File thumbnails = new File(TEMP_THUMBNAIL_DIR);
        if (!thumbnails.exists())
            thumbnails.mkdir();
        appLayout = findViewById(R.id.appLayout);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process p = Runtime.getRuntime().exec("su -c cp " + THUMBNAIL_PATH + "/*.jpg " + TEMP_THUMBNAIL_DIR);
                    p.waitFor();
                    Process p2 = Runtime.getRuntime().exec("su -c chmod 666 " + TEMP_THUMBNAIL_DIR + "/*");
                    p2.waitFor();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getSupportFragmentManager().beginTransaction().replace(R.id.appLayout, new RecentsLayoutTypeOne()).commit();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process p = Runtime.getRuntime().exec("su -c rm -fR " + TEMP_THUMBNAIL_DIR);
                    p.waitFor();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        finishAndRemoveTask();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    @Override
    protected void onStop() {
        super.onStop();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process p = Runtime.getRuntime().exec("su -c rm -fR " + TEMP_THUMBNAIL_DIR);
                    p.waitFor();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        finishAndRemoveTask();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("Destroyed", "");
                    Process p = Runtime.getRuntime().exec("su -c rm -fR " + TEMP_THUMBNAIL_DIR);
                    p.waitFor();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        super.onDestroy();
    }

    @Override
    protected void finalize() {
        try {
            Process p = Runtime.getRuntime().exec("su -c rm -fR " + TEMP_THUMBNAIL_DIR);
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

