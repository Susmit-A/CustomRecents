package com.susmit.customrecents;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;
import static com.susmit.customrecents.MainActivity.TEMP_THUMBNAIL_DIR;

public class RecentsLayoutTypeOne extends Fragment{

    LinearLayout RootLayout;
    View hsv;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        hsv = inflater.inflate(R.layout.layout_type_1,null);
        RootLayout = hsv.findViewById(R.id.root_layout);

        final ActivityManager am =(ActivityManager) getContext().getSystemService(ACTIVITY_SERVICE);
        final List<ActivityManager.RecentTaskInfo> apps = am.getRecentTasks(1000,0);

        WallpaperManager wm = WallpaperManager.getInstance(getContext());
        hsv.setBackground(wm.getDrawable());


        RootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().moveTaskToBack(true);
                getActivity().finish();
            }
        });

        for(ActivityManager.RecentTaskInfo at : apps){
            final ActivityManager.RecentTaskInfo current = at;
            final ComponentName activityname = at.topActivity;

            try {
                if (activityname.getPackageName().equals("com.susmit.customrecents") || activityname.getPackageName().equals("com.google.android.apps.nexuslauncher"))
                    continue;
            }catch (NullPointerException e){
                continue;
            }

            Bitmap bmp = BitmapFactory.decodeFile(TEMP_THUMBNAIL_DIR+"/"+at.id+".jpg");
            if(bmp == null)
                continue;

            Log.e("Task "+at.id,activityname.getPackageName()+"\n");

            DisplayMetrics m = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(m);

            final Bitmap scaled;
            if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT)
                scaled = Bitmap.createScaledBitmap(bmp,(m.widthPixels*3)/5,(m.heightPixels*3)/5,false);
            else
                scaled = Bitmap.createScaledBitmap(bmp,(m.heightPixels*5)/9,(m.widthPixels*4)/9,false);

            TypeOneCard card = new TypeOneCard(getContext(),inflater,RootLayout,scaled,current);

            card.setOnClearedListener(new OnClearedListener() {
                @Override
                public void onAllCleared() {
                    getActivity().moveTaskToBack(true);
                    getActivity().finishAndRemoveTask();
                    getActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                }

                @Override
                public void onOneCleared() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Process p = Runtime.getRuntime().exec("su -c rm "+MainActivity.THUMBNAIL_PATH+"/"+current.id+"*");
                                p.waitFor();
                                Process p2 = Runtime.getRuntime().exec("su -c rm "+MainActivity.RECENT_TASKS_XML_PATH+"/"+current.id+"*");
                                p2.waitFor();
                                Process killer = Runtime.getRuntime().exec("su -c am force-stop "+current.topActivity.getPackageName());
                                killer.waitFor();
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            });

            card.insertCardIntoRecents();
        }

        if(RootLayout.getChildCount()==0){
            RootLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,Gravity.CENTER));
            View v = inflater.inflate(R.layout.layout_type_empty,null);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().moveTaskToBack(true);
                    getActivity().finishAndRemoveTask();
                    getActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                }
            });
            RootLayout.addView(v);
        }
        return hsv;
    }
}
