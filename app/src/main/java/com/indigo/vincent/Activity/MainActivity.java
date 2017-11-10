package com.indigo.vincent.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

import com.indigo.vincent.Bean.Article;
import com.indigo.vincent.UI.MyMenu;
import com.indigo.vincent.Util.WebViewHelper;

/**
 * Created by Miracle on 2017/11/3.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    WebView webView;
    WebViewHelper webViewHelper = WebViewHelper.getInstance();
    Article article;
    EditText edt;
    Button btn_getData;
    String path = "http://www.gushiwen.org/";
    long mTime = 0;
    ProgressDialog pd;

    MyMenu myMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_main);

        myMenu = new MyMenu(this);

        webView = (WebView) findViewById(R.id.webView);
        edt = (EditText) findViewById(R.id.edt);
        btn_getData = (Button) findViewById(R.id.btn_getData);
        pd = new ProgressDialog(this);
        pd.setMessage("努力加载中，请等待~...");
        edt.setText(path);
        pd.show();

        initWebView();
    }


    /**
     * 初始化WebView
     */
    private void initWebView() {
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    if (pd.isShowing())
                        pd.dismiss();
                }
            }
        });
        webView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                btn_getData.setVisibility(View.VISIBLE);
            }
        });
        webViewHelper.setUpWebView(webView, new WebViewHelper.OnGetDataListener() {
            @Override
            public void getDataListener(String text) {
                Intent intent = new Intent(MainActivity.this, Activity_Capture.class);
                article = new Article(text, TextUtils.isEmpty(webViewHelper.getTitle()) ? "" : "《" + webViewHelper.getTitle() + "》");
                intent.putExtra("data", article);
                startActivity(intent);
            }
        });
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mTime = SystemClock.uptimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (SystemClock.uptimeMillis() - mTime < 300) {
                            btn_getData.setVisibility(View.GONE);
                        }
                        break;
                }
                return false;
            }
        });
        webView.loadUrl(path);//加载页面
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        btn_getData.setVisibility(View.GONE);
        super.onActionModeFinished(mode);
    }

    /**
     * 生成图片分享按钮
     *
     * @param v
     */
    public void ClickOnSelect(View v) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webViewHelper.getSelectedData(webView);
            }
        });
        btn_getData.setVisibility(View.GONE);
    }

    /**
     * 加载页面
     *
     * @param v
     */
    public void Load(View v) {
        pd.show();
        webView.removeAllViews();
        edt.clearFocus();
        webView.loadUrl(edt.getText().toString().trim());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webViewHelper.clear();
        webView.removeAllViews();
        webView.setVisibility(View.GONE);
        webView = null;
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        myMenu.menuClick(this, v);
    }
}
