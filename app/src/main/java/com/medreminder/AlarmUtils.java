package com.medreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;

public class AlarmUtils {
    private static final int REQ=2001;

    public static void scheduleDailyAlarm(Context ctx,int hour,int minute){
        AlarmManager am=(AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi=buildPI(ctx);
        Calendar c=Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,hour);
        c.set(Calendar.MINUTE,minute);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        if(c.getTimeInMillis()<=System.currentTimeMillis()) c.add(Calendar.DAY_OF_MONTH,1);
        if(Build.VERSION.SDK_INT>=23)
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pi);
        else
            am.setExact(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pi);
    }

    public static void cancelDailyAlarm(Context ctx){
        ((AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE)).cancel(buildPI(ctx));
    }

    private static PendingIntent buildPI(Context ctx){
        Intent i=new Intent(ctx,DailyAlarmReceiver.class);
        return PendingIntent.getBroadcast(ctx,REQ,i,
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
}
