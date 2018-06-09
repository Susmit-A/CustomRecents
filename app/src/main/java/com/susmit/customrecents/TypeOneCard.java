package com.susmit.customrecents;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Layout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class TypeOneCard{

    private Context context;
    private ActivityManager.RecentTaskInfo current;
    private View.OnClickListener thumbnailClickListener;
    private View.OnClickListener closer;
    private LinearLayout RootLayout;
    private OnClearedListener onClearedListener;

    private RelativeLayout cardRoot;
    private ImageView thumbnailView;
    private ImageView closeButton;

    @SuppressLint("ClickableViewAccessibility")
    public TypeOneCard(Context c, LayoutInflater inflator, LinearLayout container, final Bitmap thumbNail, ActivityManager.RecentTaskInfo info){

        context = c;
        current = info;

        cardRoot = (RelativeLayout) inflator.inflate(R.layout.recents_card_type_one, null);
        thumbnailView = cardRoot.findViewById(R.id.appName);
        closeButton = cardRoot.findViewById(R.id.closeBtn);
        RootLayout = container;
        thumbnailView.setImageBitmap(thumbNail);
        thumbnailView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                Intent i = current.baseIntent;
                ActivityOptions opts = ActivityOptions.makeThumbnailScaleUpAnimation(cardRoot,thumbNail,(int)cardRoot.getX(),(int)cardRoot.getY());
                try {
                    context.startActivity(i,opts.toBundle());
                }catch (SecurityException e){
                    PackageManager pm = context.getPackageManager();
                    i = pm.getLaunchIntentForPackage(current.topActivity.getPackageName());
                    context.startActivity(i,opts.toBundle());
                }
            }
        });

        closer = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClearedListener.onOneCleared();
                closeButton.setVisibility(View.GONE);

                cardRoot.animate().setDuration(300).alpha(0).y(-cardRoot.getHeight()).start();
                cardRoot.postOnAnimationDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RootLayout.removeView(cardRoot);
                        if(RootLayout.getChildCount()==0) {
                            onClearedListener.onAllCleared();
                        }
                    }
                },300);
            }
        };

        closeButton.setOnClickListener(closer);
        cardRoot.setOnClickListener(closer);
    }

    public void setOnClearedListener(OnClearedListener listener){
        onClearedListener = listener;
    }

    public void insertCardIntoRecents(){
        RootLayout.addView(cardRoot);
    }
}