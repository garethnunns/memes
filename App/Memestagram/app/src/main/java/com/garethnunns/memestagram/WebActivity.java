package com.garethnunns.memestagram;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends AppCompatActivity {
    public static final String ARG_URL = "url";

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView webview = new WebView(this);
        webview.setWebViewClient(new WebViewClient());
        setContentView(webview);

        String def = getApplicationContext().getString(R.string.web);
        if(savedInstanceState != null) {
            url = savedInstanceState.getString(ARG_URL, def);
        }
        else {
            Intent intent = getIntent();
            if(intent != null)
                url = intent.getStringExtra(ARG_URL);
            else url = def;
        }

        webview.getSettings().setJavaScriptEnabled(true);

        webview.loadUrl(url);
    }

}
