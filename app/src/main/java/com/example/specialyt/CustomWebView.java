package com.example.specialyt;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.webkit.WebView;

public class CustomWebView extends WebView {

    private boolean isFullScreen = false;

    public CustomWebView(Context context) {
        super(context);
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isFullScreen) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            } else {
                super.onMeasure(height, width);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
        if (isFullScreen) {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    private void setOrientation(int orientation) {
        Context context = getContext();
        if (context instanceof Activity) {
            ((Activity) context).setRequestedOrientation(orientation);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!isFullScreen) {
            return;
        }
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }
}
