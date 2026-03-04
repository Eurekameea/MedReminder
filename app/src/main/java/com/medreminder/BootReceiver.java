package com.medreminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context ctx,Intent intent){
        String a=intent.getAction();
        if(!Intent.ACTION_BOOT_COMPLETED.equals(a)
            &&!"android.intent.action.QUICKBOOT_POWERON".equals(a)) return;
        SharedPreferences p=ctx.getSharedPreferences("MedReminderPrefs",Context.MODE_PRIVATE);
        if(!p.getBoolean("isActive",false)) return;
        int h=p.getInt("alarmHour",8),m=p.getInt("alarmMinute",0);
        AlarmUtils.scheduleDailyAlarm(ctx,h,m);
        Calendar now=Calendar.getInstance();
        boolean passed=(now.get(Calendar.HOUR_OF_DAY)>h)
            ||(now.get(Calendar.HOUR_OF_DAY)==h&&now.get(Calendar.MINUTE)>=m);
        if(passed){
            Intent svc=new Intent(ctx,MedNotificationService.class);
            svc.setAction(MedNotificationService.ACTION_SHOW);
            if(Build.VERSION.SDK_INT>=26) ctx.startForegroundService(svc);
            else ctx.startService(svc);
        }
    }
}
