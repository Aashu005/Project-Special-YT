package com.example.specialyt;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private FrameLayout fullScreenView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private static final String TAG = "SpecialYT";
    private int shortsCounter = 0;
    private boolean isWarningShown = false;
    private boolean isScrollBlocked = false;
    private View customView;
    private WebChromeClient webChromeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        fullScreenView = findViewById(R.id.full_screen_view);

        WebView.setWebContentsDebuggingEnabled(true);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; Pixel 4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.93 Mobile Safari/537.36");

        webChromeClient = new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                customView = view;
                customViewCallback = callback;

                fullScreenView.addView(customView);
                fullScreenView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }

            @Override
            public void onHideCustomView() {
                if (customView == null) {
                    return;
                }

                fullScreenView.removeView(customView);
                customView = null;
                fullScreenView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);

                if (customViewCallback != null) {
                    customViewCallback.onCustomViewHidden();
                }

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        };

        webView.setWebChromeClient(webChromeClient);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("youtube.com")) {
                    view.loadUrl(url);
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "Page finished loading: " + url);
                injectJavaScript(view);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                if (url.contains("shorts") && !isWarningShown && !isScrollBlocked) {
                    shortsCounter++;
                    if (shortsCounter == 5) {
                        showAlert();
                        isScrollBlocked = true;
                    }
                }
            }
        });

        webView.loadUrl("https://www.youtube.com");

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE
        }, 1);
    }

    private void injectJavaScript(WebView view) {
        String jsCode = "javascript:(function() { " +
                "function removeShorts() {" +
                "   var shortsButton = document.querySelector('ytm-pivot-bar-item-renderer .pivot-shorts');" +
                "   if (shortsButton) {" +
                "       shortsButton.parentElement.remove();" +
                "   }" +
                "   var homeFeedItems = document.querySelectorAll('ytm-reel-shelf-renderer, ytd-reel-shelf-renderer');" +
                "   homeFeedItems.forEach(function(item) {" +
                "       item.remove();" +
                "   });" +
                "}" +
                "function removeAds() {" +
                "   var adSelectors = [" +
                "       'ytd-display-ad-renderer', 'ytd-promoted-sparkles-text-search-renderer', " + // In-feed ads
                "       '.ytp-ad-module', '.video-ads', '.ytp-ad-overlay-container', " + // In-stream and overlay ads
                "       'ytm-promoted-sparkles-web-renderer', 'ytm-promoted-sparkles-text-search-renderer'," + // More ad selectors
                "       'ytd-promoted-video-renderer', 'ytd-sponsored-renderer', " + // Sponsored content
                "       '.ytp-ad-player-overlay', '.ytp-cued-ad', 'ytd-player-legacy-desktop-watch-ads-renderer'" + // Player ads
                "   ];" +
                "   adSelectors.forEach(function(selector) {" +
                "       var ads = document.querySelectorAll(selector);" +
                "       ads.forEach(function(ad) {" +
                "           ad.remove();" +
                "       });" +
                "   });" +
                "}" +
                "removeShorts();" +
                "removeAds();" +
                // Inject CSS to fix video position
                "var style = document.createElement('style');" +
                "style.innerHTML = 'ytd-app { position: relative; }';" +
                "document.head.appendChild(style);" +
                // End of CSS injection
                "setInterval(function() {" +
                "   removeShorts();" +
                "   removeAds();" +
                "}, 3000);" + // Continuously remove ads and shorts every 3 seconds
                "})()";
        view.loadUrl(jsCode);
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            webChromeClient.onHideCustomView();
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void showAlert() {
        isWarningShown = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("WARNING!!!")
                .setMessage("Aap loop me fasne jaa rhe ho")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        isWarningShown = false;
                        isScrollBlocked = false;
                        shortsCounter = 0;
                    }
                });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isWarningShown = false;
                isScrollBlocked = false;
                shortsCounter = 0;
            }
        });
        builder.show();
    }
}

