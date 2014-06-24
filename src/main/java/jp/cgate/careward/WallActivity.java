package jp.cgate.careward;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.view.Window;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.os.Build;
import android.content.Context;
import android.util.Log;
import android.content.Intent;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import java.util.UUID;
import java.security.MessageDigest;

public class WallActivity extends Activity
{
    public interface Callback {
        void finish();
        void error(String msg);
    }

    private static final String TAG = WallActivity.class.getSimpleName();
    private static final String AD_URL = "ad.mobadme.jp";
    private static final String WALL_URL = "http://car.mobadme.jp/spg/spnew/%d/%d/index.php?user_id=%s&crypt=%s";
    private static String appKey = null;
    private static int mId = 0;
    private static int m_ownerID = 0;
    private static boolean debug = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        FrameLayout frame = new FrameLayout(this);
        setContentView(frame);

        WebView webView = new WebView(this);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri u = Uri.parse(url);
                debug("shouldOverrideUrlLoading:" + u.getHost());

                if(AD_URL.equals(u.getHost())) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, u);
                    view.getContext().startActivity(intent);
                    return true;
                }

                return false;
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setSupportZoom(false);
        webSettings.setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        String databasePath = webView.getContext().getDir("wall", Context.MODE_PRIVATE).getPath();
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabasePath(databasePath);


        frame.addView(webView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        try {
            webView.loadUrl(getWallUrl());
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    private String getUserId() {
        SharedPreferences prefs = getSharedPreferences("user_id", Context.MODE_PRIVATE);
        String user_id = prefs.getString("user_id", null);
        if(user_id == null) {
            user_id = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_id", user_id);
            editor.apply();
        }

        debug("user_id:" + user_id);

        return user_id;
    }

    private String getCrypt() {
        try {
            StringBuilder sb = new StringBuilder();
            for(byte b : MessageDigest.getInstance("SHA1").digest((getUserId() + appKey).getBytes())) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch(java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }
    private String getWallUrl() {
        return String.format(WALL_URL, m_ownerID, mId, getUserId(), getCrypt());
    }

    public static void init(final Activity a, final Callback callback) {
        if(appKey != null) {
            callback.finish();
            return;
        }
        a.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(a, jp.cgate.careward.WallActivity.class);
                PackageManager packageManager = a.getPackageManager();
                Bundle bundle = intent.resolveActivityInfo(packageManager, PackageManager.GET_META_DATA).metaData;

                appKey = bundle.getString("appKey", null);
                mId = bundle.getInt("mId", 0);
                m_ownerID = bundle.getInt("m_ownerID", 0);
                debug = bundle.getBoolean("debug", false);

                debug("api_key:" + appKey);
                debug("mId:" + mId);
                debug("m_ownerID:" + m_ownerID);

                if(appKey == null) {
                    callback.error("apikey not");
                    return;
                }
                if(mId == 0) {
                    callback.error("mId not");
                    return;
                }
                if(m_ownerID == 0) {
                    callback.error("m_ownerID not");
                    return;
                }
                callback.finish();

            }
        });
    }

    public static void show(final Activity a) {
        a.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                init(a, new Callback() {
                    @Override
                    public void finish() {
                        Intent intent = new Intent(a, jp.cgate.careward.WallActivity.class);
                        a.startActivity(intent);
                    }
                    @Override
                    public void error(String msg) {
                        debug(msg);
                    }
                });
            }
        });
    }


    private static void debug(String s) {
        if(debug) {
            Log.d(TAG, s);
        }
    }


}
