package com.autolink.lightshowcontrolpanel.ui.iview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ChartWebView extends WebView {
    public ChartWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccess(true);
        settings.setDisplayZoomControls(false);
        settings.setBuiltInZoomControls(false);
        loadUrl("file:///android_asset/chart_view.html");
    }

    public void loadChart(String dataJson){
        String call = String.format("javascript:load('%s')", dataJson);
        loadUrl(call);
    }
}
