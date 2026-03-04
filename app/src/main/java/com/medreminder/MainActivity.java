package com.medreminder;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS="MedReminderPrefs",KEY_ACTIVE="isActive",
        KEY_HOUR="alarmHour",KEY_MINUTE="alarmMinute";
    private static final int REQ_NOTIF=1001;
    private Button btnToggle;
    private TextView tvStatus;
    private TimePicker timePicker;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);
        prefs=getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        btnToggle=findViewById(R.id.btnToggle);
        tvStatus=findViewById(R.id.tvStatus);
        timePicker=findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setHour(prefs.getInt(KEY_HOUR,8));
        timePicker.setMinute(prefs.getInt(KEY_MINUTE,0));
        updateUI();
        btnToggle.setOnClickListener(v->{
            if(prefs.getBoolean(KEY_ACTIVE,false)) stopReminder();
            else checkPerms();
        });
    }

    @Override protected void onResume(){ super.onResume(); updateUI(); }

    private void updateUI(){
        boolean on=prefs.getBoolean(KEY_ACTIVE,false);
        if(on){
            int h=prefs.getInt(KEY_HOUR,8),m=prefs.getInt(KEY_MINUTE,0);
            btnToggle.setText("停止提醒");
            btnToggle.setBackgroundColor(0xFFB71C1C);
            tvStatus.setText(String.format("已啟動  每天 %02d:%02d 藥丸圖示出現頂端",h,m));
            timePicker.setEnabled(false);
        } else {
            btnToggle.setText("開始計算（啟動提醒）");
            btnToggle.setBackgroundColor(0xFF1B5E20);
            tvStatus.setText("未啟動  設定時間後點擊啟動");
            timePicker.setEnabled(true);
        }
    }

    private void checkPerms(){
        if(Build.VERSION.SDK_INT>=33){
            if(ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    !=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},REQ_NOTIF);
                return;
            }
        }
        if(Build.VERSION.SDK_INT>=31){
            AlarmManager am=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
            if(!am.canScheduleExactAlarms()){
                new AlertDialog.Builder(this)
                    .setTitle("需要精確鬧鐘權限")
                    .setMessage("請開啟允許精確鬧鐘，確保準時提醒。")
                    .setPositiveButton("前往設定",(d,w)->{
                        Intent i=new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        i.setData(Uri.parse("package:"+getPackageName()));
                        startActivity(i);
                    }).setNegativeButton("取消",null).show();
                return;
            }
        }
        startReminder();
    }

    @Override
    public void onRequestPermissionsResult(int req,String[] p,int[] r){
        super.onRequestPermissionsResult(req,p,r);
        if(req==REQ_NOTIF&&r.length>0&&r[0]==PackageManager.PERMISSION_GRANTED) checkPerms();
    }

    private void startReminder(){
        int h=timePicker.getHour(),m=timePicker.getMinute();
        prefs.edit().putBoolean(KEY_ACTIVE,true).putInt(KEY_HOUR,h).putInt(KEY_MINUTE,m).apply();
        AlarmUtils.scheduleDailyAlarm(this,h,m);
        Intent svc=new Intent(this,MedNotificationService.class);
        svc.setAction(MedNotificationService.ACTION_SHOW);
        if(Build.VERSION.SDK_INT>=26) startForegroundService(svc);
        else startService(svc);
        updateUI();
        showOppoGuide();
    }

    private void stopReminder(){
        prefs.edit().putBoolean(KEY_ACTIVE,false).apply();
        AlarmUtils.cancelDailyAlarm(this);
        Intent svc=new Intent(this,MedNotificationService.class);
        svc.setAction(MedNotificationService.ACTION_HIDE);
        startService(svc);
        updateUI();
    }

    private void showOppoGuide(){
        String brand=Build.BRAND.toLowerCase();
        List<String> list=Arrays.asList("oppo","oneplus","realme");
        boolean isOppo=false;
        for(String b:list) if(brand.contains(b)){isOppo=true;break;}
        if(!isOppo) return;
        new AlertDialog.Builder(this)
            .setTitle("OPPO / realme 額外設定")
            .setMessage("ColorOS 省電機制可能導致提醒失效。\n\n"
                +"1. 設定 > 電池 > 省電管理 > 自定義省電\n"
                +"   找到「藥物提醒」> 設為「不限制」\n\n"
                +"2. 設定 > 應用程式 > 藥物提醒 > 電池\n"
                +"   關閉「允許自動管理」\n\n"
                +"3. 點下方按鈕 > 忽略電池優化")
            .setPositiveButton("前往電池設定",(d,w)->{
                try{
                    Intent i=new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    i.setData(Uri.parse("package:"+getPackageName()));
                    startActivity(i);
                }catch(Exception e){
                    startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                }
            })
            .setNegativeButton("稍後設定",null).show();
    }
}
