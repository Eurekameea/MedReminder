package com.medreminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

public class DailyAlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context ctx,Intent intent){
        SharedPreferences p=ctx.getSharedPreferences("MedReminderPrefs",Context.MODE_PRIVATE);
        if(!p.getBoolean("isActive",false)) return;
        Intent svc=new Intent(ctx,MedNotificationService.class);
        svc.setAction(MedNotificationService.ACTION_SHOW);
        if(Build.VERSION.SDK_INT>=26) ctx.startForegroundService(svc);
        else ctx.startService(svc);
        AlarmUtils.scheduleDailyAlarm(ctx,p.getInt("alarmHour",8),p.getInt("alarmMinute",0));
    }
}
