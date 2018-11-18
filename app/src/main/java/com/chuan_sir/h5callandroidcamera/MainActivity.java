package com.chuan_sir.h5callandroidcamera;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private WebView webView;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final static int PHOTO_REQUEST = 100;
    private final static int ALBUM_REQUEST = 101;
    private final static int TAKE_PHOTO_REQUEST_CODE = 102;
    private final static String url = "https://cmf.cmpay.com/khejb/index.html#/Certification2?wantGo=back";
    private CustomDiaLog mCustomDiaLog;
    private File fileUri;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.main_webview);
        fileUri = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + SystemClock.currentThreadTimeMillis() + ".jpg");
        initWebViewSetting();

    }

    private void initWebViewSetting() {
        WebSettings settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDomStorageEnabled(true);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setAllowContentAccess(true); // 是否可访问Content Provider的资源，默认值 true
        settings.setAllowFileAccess(true);    // 是否可访问本地文件，默认值 true
        // 是否允许通过file url加载的Javascript读取本地文件，默认值 false
        settings.setAllowFileAccessFromFileURLs(false);
        // 是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
        settings.setAllowUniversalAccessFromFileURLs(false);
        //开启JavaScript支持
        settings.setJavaScriptEnabled(true);
        // 支持缩放
        settings.setSupportZoom(true);
        //辅助WebView设置处理关于页面跳转，页面请求等操作
        webView.setWebViewClient(new MyWebViewClient());
        //辅助WebView处理图片上传操作
        webView.setWebChromeClient(new MyChromeWebClient());
        //加载地址
        webView.loadUrl(url);

    }

    //自定义 WebViewClient 辅助WebView设置处理关于页面跳转，页面请求等操作【处理tel协议和视频通讯请求url的拦截转发】
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e(TAG, url);
            view.loadUrl(url);
            return true;
        }
    }



    //自定义 WebChromeClient 辅助WebView处理图片上传操作【<input type=file> 文件上传标签】
    public class MyChromeWebClient extends WebChromeClient {
        // For Android 3.0-
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            showDialog();

        }

        // For Android 3.0+
        public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            showDialog();
        }

        //For Android 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            showDialog();
        }

        // For Android 5.0+
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            mUploadCallbackAboveL = filePathCallback;
            showDialog();
            return true;
        }
    }

    private void showDialog() {
        CustomDiaLog.Builder builder = new CustomDiaLog.Builder(this);
        builder.setItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int site, long id) {
                if (mCustomDiaLog != null)
                    mCustomDiaLog.dismiss();
                if (site == 0) {
                    checkPhotoPermission();
                } else if (site == 1) {
                    getPicFromAlbm();
                } else {
                    cancelFilePathCallback();
                }
            }
        });
        ArrayList<String> strs = new ArrayList<String>();
        strs.add("相机");
        strs.add("相册");
        strs.add("取消");
        mCustomDiaLog = builder.build(strs);
        mCustomDiaLog.setCanceledOnTouchOutside(false);
        mCustomDiaLog.show();
    }

    /**
     * 申请相机权限
     */
    private void checkPhotoPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    TAKE_PHOTO_REQUEST_CODE);
        } else {
            takePhoto();
        }
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        imageUri = Uri.fromFile(fileUri);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", fileUri);//通过FileProvider创建一个content类型的Uri

        }
        PhotoUtils.takePicture(this, imageUri, PHOTO_REQUEST);
    }

    /**
     * 从相册获取图片
     */
    private void getPicFromAlbm() {
        PhotoUtils.openPic(this, ALBUM_REQUEST);
    }

    /**
     * 取消数据回传回调，处理取消后不能重复调用onShowFileChooser或者openFileChooser的问题
     */
    private void cancelFilePathCallback() {
        if (mUploadMessage != null) {
            mUploadMessage.onReceiveValue(null);
            mUploadMessage = null;
        } else if (mUploadCallbackAboveL != null) {
            mUploadCallbackAboveL.onReceiveValue(null);
            mUploadCallbackAboveL = null;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //如果按下的是回退键且历史记录里确实还有页面
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST) {
            if (null == mUploadMessage && null == mUploadCallbackAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (mUploadCallbackAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        } else if (requestCode == ALBUM_REQUEST) {
            if (null == mUploadMessage && null == mUploadCallbackAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (mUploadCallbackAboveL != null) {
                if (resultCode == RESULT_OK) {
                    mUploadCallbackAboveL.onReceiveValue(new Uri[]{result});
                    mUploadCallbackAboveL = null;
                } else {
                    mUploadCallbackAboveL.onReceiveValue(new Uri[]{});
                    mUploadCallbackAboveL = null;
                }

            } else if (mUploadMessage != null) {
                if (resultCode == RESULT_OK) {
                    mUploadMessage.onReceiveValue(result);
                    mUploadMessage = null;
                } else {
                    mUploadMessage.onReceiveValue(Uri.EMPTY);
                    mUploadMessage = null;
                }

            }
        }
    }


    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        if (requestCode != PHOTO_REQUEST || mUploadCallbackAboveL == null) {
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                results = new Uri[]{imageUri};
            } else {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }

                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        mUploadCallbackAboveL.onReceiveValue(results);
        mUploadCallbackAboveL = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==TAKE_PHOTO_REQUEST_CODE){
            if(PackageManager.PERMISSION_GRANTED==grantResults[0]){
                takePhoto();
            }else {
                cancelFilePathCallback();
                Toast.makeText(this,"上传身份证照片需要开启相机权限",Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.setWebViewClient(null);
            webView.setWebChromeClient(null);
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();
//            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.destroy();
            webView = null;
        }
    }

}
