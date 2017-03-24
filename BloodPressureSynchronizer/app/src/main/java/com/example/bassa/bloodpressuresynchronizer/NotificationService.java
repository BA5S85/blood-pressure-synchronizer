package com.example.bassa.bloodpressuresynchronizer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

// http://stackoverflow.com/a/22279317/5572217
public class NotificationService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        Intent mainIntent = new Intent(this, MainActivity.class);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentTitle("Tere! Kell on " + System.currentTimeMillis() + ".")
                .setContentText("See tähendab, et paras aeg on mõõta vererõhku!")
                .setSmallIcon(R.mipmap.icon)
                .setDefaults(Notification.DEFAULT_ALL)
                .setTicker("Tere! Kell on " + System.currentTimeMillis() + ", mis tähendab et paras aeg on mõõta vererõhku!")
                .setWhen(System.currentTimeMillis())
                .build();
        notificationManager.notify(0, notification);

        Log.i("NOTIFICATION_CREATED", "Notification created");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
