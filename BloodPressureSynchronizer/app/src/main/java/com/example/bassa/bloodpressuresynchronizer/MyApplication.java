package com.example.bassa.bloodpressuresynchronizer;

import android.app.Application;
import android.content.Context;

// http://stackoverflow.com/a/5114361/5572217
public class MyApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}