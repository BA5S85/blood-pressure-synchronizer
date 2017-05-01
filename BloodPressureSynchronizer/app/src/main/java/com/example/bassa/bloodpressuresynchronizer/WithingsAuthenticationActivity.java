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
import com.github.scribejava.core.model.SignatureType;

import java.io.IOException;

// http://stackoverflow.com/a/40258662/5572217
public class WithingsAuthenticationActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withings_authentication);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient(webView));

        MainActivity.service = new ServiceBuilder()
                .apiKey(WithingsAPI.API_KEY)
                .apiSecret(WithingsAPI.API_SECRET)
                .signatureType(SignatureType.QueryString)
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

    @Override
    // http://stackoverflow.com/a/9743841/5572217
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void getVerifier(final String url) {
        try {
            if (url.equals("https://oauth.withings.com/account/This%20is%20a%20mobile%20application")) { // access denied
                finish();
                return;
            }

            String divStrUID = "userid=";
            String divStrVerifier = "oauth_verifier=";

            int i = url.indexOf(divStrUID);
            int j = url.indexOf(divStrVerifier);

            if (i != -1 && j != -1){
                final String userid = url.substring(i + divStrUID.length(), url.indexOf('&'));
                final String oauth_verifier = url.substring(j + divStrVerifier.length());

                Intent intent = new Intent();
                intent.putExtra("USER_ID", userid);
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

    protected static final String API_KEY = BuildConfig.API_KEY;
    protected static final String API_SECRET = BuildConfig.API_SECRET;

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
