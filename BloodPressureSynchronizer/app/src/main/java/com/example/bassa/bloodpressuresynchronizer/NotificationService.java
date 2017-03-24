package com.example.bassa.bloodpressuresynchronizer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

// http://stackoverflow.com/a/22279317/5572217
public class NotificationService extends Service {

    @Override
    public void onStart(Intent intent, int startId) {
        super.onCreate();

        Intent mainIntent = new Intent(this, MainActivity.class);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentTitle("Paras aeg on mõõta vererõhku!")
                .setContentText("Ole tubli ja soorita mõõtmine.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setDefaults(Notification.DEFAULT_ALL)
                .setTicker("Paras aeg on mõõta vererõhku!")
                .setWhen(System.currentTimeMillis())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        notificationManager.notify(0, notification);

        Log.i("NOTIFICATION_CREATED", "Notification created");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
