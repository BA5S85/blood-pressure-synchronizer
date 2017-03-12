package com.example.bassa.bloodpressuresynchronizer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1RequestToken;

import java.io.IOException;

// http://stackoverflow.com/a/40258662/5572217
public class WithingsAuthenticationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withings_authentication);

        final WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient(webView));

        MainActivity.service = new ServiceBuilder()
                .apiKey(WithingsAPI.API_KEY)
                .apiSecret(WithingsAPI.API_SECRET)
                .build(WithingsAPI.instance());

        new Thread(new Runnable() {
            public void run() {
                try {
                    MainActivity.requestToken = MainActivity.service.getRequestToken();
                    final String authURL = MainActivity.service.getAuthorizationUrl(MainActivity.requestToken);
                    webView.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl(authURL);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class MyWebViewClient extends WebViewClient {

        WebView webView;

        MyWebViewClient(WebView webView){
            this.webView = webView;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            getVerifier(url);
        }

    }

    private void getVerifier(final String url) {
        try {
            String divStr = "oauth_verifier=";
            int i = url.indexOf(divStr);

            if (i != -1){
                Log.w("WITHINS_SUCCESS_LOG", "getVerifier(final String url)");

                final String oauth_verifier = url.substring(i + divStr.length());

                Intent intent = new Intent();
                intent.putExtra("VERIFIER", oauth_verifier);
                setResult(RESULT_OK, intent);

                finish();
            }

        } catch (Exception e) {
            Log.e("WITHINS_ERROR_LOG", e.getMessage());
        }
    }

}

class WithingsAPI extends DefaultApi10a {

    protected static final String API_KEY = "";
    protected static final String API_SECRET = "";

    protected WithingsAPI() {
    }

    private static class InstanceHolder {
        private static final WithingsAPI INSTANCE = new WithingsAPI();
    }

    public static WithingsAPI instance() {
        return InstanceHolder.INSTANCE;
    }

    // Step 1 : get a oAuth "request accessTokenKey"
    @Override
    public String getRequestTokenEndpoint() {
        return "https://oauth.withings.com/account/request_token";
    }

    // Step 2 : End-user authorization
    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        return String.format("https://oauth.withings.com/account/authorize?oauth_token=%s", requestToken.getToken());
    }

    // Step 3 : Generating access accessTokenKey
    @Override
    public String getAccessTokenEndpoint() {
        return "https://oauth.withings.com/account/access_token";
    }

}
