package com.nexoai.ultra;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class BrowserBridge {
    public interface Callback { void ok(String text); void fail(String error); }

    private final Activity activity;
    private final WebView webView;
    private Callback callback;
    private String expectedHost;

    public BrowserBridge(Activity activity) {
        this.activity = activity;
        this.webView = new WebView(activity);
        configure();
    }

    private void configure() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setUserAgentString(s.getUserAgentString() + " NexoAI/2.0");
        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);
        }
        webView.addJavascriptInterface(new JsApi(), "NexoAndroid");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    java.net.URI u = new java.net.URI(url);
                    if (expectedHost != null && expectedHost.equalsIgnoreCase(u.getHost())) return false;
                } catch (Exception ignored) {}
                return true;
            }
        });
    }

    public WebView view(){ return webView; }

    public void health(Context c, Callback cb){
        this.callback = cb;
        String base = normalize(Prefs.site(c));
        expectedHost = host(base);
        webView.loadUrl(base + "/app/health.php?bridge=1");
    }

    public void generate(Context c, String mode, String text, String context, Callback cb){
        this.callback = cb;
        String base = normalize(Prefs.site(c));
        expectedHost = host(base);
        String body = form("mode", mode) + "&" + form("text", text) + "&" + form("context", context == null ? "" : context)
                + "&" + form("style", Prefs.style(c)) + "&" + form("pin", Prefs.pin(c)) + "&bridge=1";
        webView.postUrl(base + "/app/bridge.php", body.getBytes(StandardCharsets.UTF_8));
    }

    public void destroy(){ webView.removeJavascriptInterface("NexoAndroid"); webView.destroy(); }

    private static String normalize(String s){ s = s == null ? "" : s.trim(); while(s.endsWith("/")) s=s.substring(0,s.length()-1); return s; }
    private static String form(String k,String v){ return enc(k)+"="+enc(v==null?"":v); }
    private static String enc(String s){ try{return URLEncoder.encode(s,"UTF-8");}catch(Exception e){return "";} }
    private static String host(String s){ try{return new java.net.URI(s).getHost();}catch(Exception e){return "";} }

    public final class JsApi {
        @JavascriptInterface public void onResult(String text){ activity.runOnUiThread(() -> { if(callback!=null) callback.ok(text); }); }
        @JavascriptInterface public void onError(String error){ activity.runOnUiThread(() -> { if(callback!=null) callback.fail(error); }); }
        @JavascriptInterface public void onHealth(String text){ activity.runOnUiThread(() -> { if(callback!=null) callback.ok(text); }); }
    }
}
