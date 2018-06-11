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

    static String []heirarchyPerms;

    static void backupSystemPerms(){
        String path = "/data";
        heirarchyPerms = new String[4];
        byte[] buff = new byte[10];
        try {
            Process p0 = Runtime.getRuntime().exec("stat -c \"%a\" "+path);
            p0.waitFor();
            p0.getInputStream().read(buff);
            heirarchyPerms[0] = new String(buff);

            path += "/system_ce";
            Process p1 = Runtime.getRuntime().exec("stat -c \"%a\" "+path);
            p1.waitFor();
            p1.getInputStream().read(buff);
            heirarchyPerms[1] = new String(buff);

            path += "/0";
            Process p2 = Runtime.getRuntime().exec("stat -c \"%a\" "+path);
            p2.waitFor();
            p2.getInputStream().read(buff);
            heirarchyPerms[2] = new String(buff);

            path += "/snapshots";
            Process p3 = Runtime.getRuntime().exec("stat -c \"%a\" "+path);
            p3.waitFor();
            p3.getInputStream().read(buff);
            heirarchyPerms[3] = new String(buff);

            Log.e("Backup", path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void changePermsTemporarily() {
        String path = "/data";
        try {
            Process p0 = Runtime.getRuntime().exec("su -c chmod 775 " + path);
            p0.waitFor();

            path += "/system_ce";
            Process p1 = Runtime.getRuntime().exec("su -c chmod 755 " + path);
            p1.waitFor();

            path += "/0";
            Process p2 = Runtime.getRuntime().exec("su -c chmod 755 " + path);
            p2.waitFor();

            path += "/snapshots";
            Process p3 = Runtime.getRuntime().exec("su -c chmod 755 " + path);
            p3.waitFor();

            Process p4 = Runtime.getRuntime().exec("su -c chmod 666 " + path +"/*.jpg");
            p4.waitFor();

            Log.e("Change", path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void restorePerms(){
        String path = "/data";
        try {
            Process p0 = Runtime.getRuntime().exec("su -c chmod "+ heirarchyPerms[0] + path);
            p0.waitFor();

            path += "/system_ce";
            Process p1 = Runtime.getRuntime().exec("su -c chmod "+ heirarchyPerms[1] + path);
            p1.waitFor();

            path += "/0";
            Process p2 = Runtime.getRuntime().exec("su -c chmod "+ heirarchyPerms[2] + path);
            p2.waitFor();

            path += "/snapshots";
            Process p3 = Runtime.getRuntime().exec("su -c chmod "+ heirarchyPerms[3] + path);
            p3.waitFor();

            Log.e("Restore", path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("Path", THUMBNAIL_PATH);
                        getSupportFragmentManager().beginTransaction().replace(R.id.appLayout, new RecentsLayoutTypeOne()).commit();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    @Override
    protected void onStop() {
        super.onStop();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}

