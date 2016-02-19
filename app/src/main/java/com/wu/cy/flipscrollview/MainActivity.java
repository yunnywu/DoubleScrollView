package com.wu.cy.flipscrollview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class MainActivity extends AppCompatActivity {

    WebView mwebview;

    DoubleScrollView mPullUpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mwebview= (WebView) findViewById(R.id.webview);
        mPullUpView = (DoubleScrollView) findViewById(R.id.pullUpView);


        mwebview.loadUrl("https://github.com/");

    }
}
