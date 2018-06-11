package com.susmit.customrecents;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

public class RecentsLayoutTypeOne extends Fragment{

    LinearLayout RootLayout;
    View hsv;

    Bitmap thumbNail,scaled;

    TypeOneCard card;

    LayoutInflater fragmentLayoutInflator;

    ActivityManager mActivityManager;
    List<ActivityManager.RecentTaskInfo> recentAppsList;

    DisplayMetrics mDisplayMetrics;

    int thumbnailBackground = Color.WHITE;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        fragmentLayoutInflator = inflater;

        mDisplayMetrics= new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

        hsv = inflater.inflate(R.layout.layout_type_1,null);
        RootLayout = hsv.findViewById(R.id.root_layout);

        mActivityManager =(ActivityManager) getContext().getSystemService(ACTIVITY_SERVICE);
        recentAppsList = mActivityManager.getRecentTasks(50,0);

        WallpaperManager wm = WallpaperManager.getInstance(getContext());
        hsv.setBackground(wm.getDrawable());

        RootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().moveTaskToBack(true);
                getActivity().finish();
            }
        });
        new TaskCardLoader().execute();
        return hsv;
    }


    private class TaskCardLoader extends AsyncTask<Void,Void,Void>{
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected Void doInBackground(Void... voids) {

            MainActivity.backupSystemPerms();
            MainActivity.changePermsTemporarily();

            for(ActivityManager.RecentTaskInfo at : recentAppsList) {

                if(at.numActivities==0)
                    continue;
                final ActivityManager.RecentTaskInfo current = at;
                final ComponentName activityname = at.topActivity;
                try {
                    if (activityname.getPackageName().equals("com.susmit.customrecents") || activityname.getPackageName().equals("com.google.android.apps.nexuslauncher"))
                        continue;
                } catch (NullPointerException e) {
                    continue;
                }

                thumbNail = BitmapFactory.decodeFile(MainActivity.THUMBNAIL_PATH + "/" + current.id + ".jpg");

                try {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                        scaled = Bitmap.createScaledBitmap(thumbNail, (mDisplayMetrics.widthPixels * 3) / 5, (mDisplayMetrics.heightPixels * 3) / 5, false);
                    else
                        scaled = Bitmap.createScaledBitmap(thumbNail, (mDisplayMetrics.heightPixels * 5) / 9, (mDisplayMetrics.widthPixels * 4) / 9, false);
                } catch (NullPointerException e) {
                    try {

                        Drawable d = getActivity().getPackageManager().getApplicationIcon(at.topActivity.getPackageName());
                        Bitmap bmp = Bitmap.createBitmap(d.getIntrinsicWidth(),d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                        Canvas c = new Canvas(bmp);
                        d.setBounds(0,0,c.getWidth(),c.getHeight());
                        d.draw(c);

                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                            scaled = Bitmap.createBitmap((mDisplayMetrics.widthPixels * 3) / 5, (mDisplayMetrics.heightPixels * 3) / 5, Bitmap.Config.ARGB_8888);
                        else
                            scaled = Bitmap.createBitmap((mDisplayMetrics.heightPixels * 5) / 9, (mDisplayMetrics.widthPixels * 4) / 9, Bitmap.Config.ARGB_8888);

                        Bitmap overlay = Bitmap.createBitmap(scaled.getWidth(),scaled.getHeight(),scaled.getConfig());
                        c = new Canvas(overlay);
                        c.drawBitmap(scaled,new Matrix(),null);
                        c.drawBitmap(bmp,scaled.getWidth()/2 - bmp.getWidth()/2,scaled.getHeight()/2 - bmp.getHeight()/2,null);

                        scaled = overlay;
                    } catch (PackageManager.NameNotFoundException e1) {
                        e1.printStackTrace();
                    }
                }


                card = new TypeOneCard(getContext(), fragmentLayoutInflator, RootLayout, scaled, current);

                card.setOnClearedListener(new OnClearedListener() {
                    @Override
                    public void onAllCleared() {
                        getActivity().moveTaskToBack(true);
                        getActivity().finishAndRemoveTask();
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }

                    @Override
                    public void onOneCleared() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    recentAppsList.remove(current);
                                    Process p0 = Runtime.getRuntime().exec("su -c rm " + MainActivity.THUMBNAIL_PATH + "/" + current.id + "*");
                                    p0.waitFor();
                                    Process p1 = Runtime.getRuntime().exec("su -c rm " + MainActivity.RECENT_TASKS_XML_PATH + "/" + current.id + "*");
                                    p1.waitFor();
                                    Process killer = Runtime.getRuntime().exec("su -c am force-stop " + current.topActivity.getPackageName());
                                    killer.waitFor();
                                } catch (IOException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                });

                publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            card.setThumbnailBackground(thumbnailBackground);
            card.insertCardIntoRecents();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(RootLayout.getChildCount()==0){
                RootLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,Gravity.CENTER));
                View v = fragmentLayoutInflator.inflate(R.layout.layout_type_empty,null);
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
            MainActivity.restorePerms();
        }
    }
}
