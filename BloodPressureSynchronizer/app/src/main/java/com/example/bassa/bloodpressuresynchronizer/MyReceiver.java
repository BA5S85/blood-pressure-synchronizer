package com.example.bassa.bloodpressuresynchronizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Intent service = new Intent(context, NotificationService.class);
        context.startService(service);
    }
}
