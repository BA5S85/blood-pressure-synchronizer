package com.example.bassa.bloodpressuresynchronizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MeasureBPNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Intent service = new Intent(context, MeasureBPNotificationService.class);
        context.startService(service);
    }

}
