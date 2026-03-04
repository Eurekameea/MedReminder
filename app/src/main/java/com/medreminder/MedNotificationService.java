package com.medreminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MedNotificationService extends Service {
    public static final String ACTION_SHOW="ACTION_SHOW_MED",ACTION_HIDE="ACTION_HIDE_MED";
    public static final String CHANNEL_ID="med_ch";
    public static final int NOTIF_ID=9999;

    @Override public void onCreate(){ super.onCreate(); createChannel(); }

    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent!=null&&ACTION_HIDE.equals(intent.getAction())){
            stopForeground(true); stopSelf(); return START_NOT_STICKY;
        }
        startForeground(NOTIF_ID,buildNotif());
        return START_STICKY;
    }

    private Notification buildNotif(){
        Intent open=new Intent(this,MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi=PendingIntent.getActivity(this,0,open,
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this,CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pill_status)
            .setContentTitle("今天記得吃藥！")
            .setContentText("點此查看提醒設定")
            .setContentIntent(pi)
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(false)
            .setSilent(true)
            .build();
    }

    private void createChannel(){
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel ch=new NotificationChannel(
                CHANNEL_ID,"藥物提醒",NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("每日服藥提醒圖示");
            ch.setShowBadge(false); ch.enableVibration(false); ch.enableLights(false);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }
    @Nullable @Override public IBinder onBind(Intent i){ return null; }
}
