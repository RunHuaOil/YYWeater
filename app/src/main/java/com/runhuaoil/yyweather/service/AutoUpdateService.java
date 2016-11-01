package com.runhuaoil.yyweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.runhuaoil.yyweather.receiver.AutoUpdateReceiver;
import com.runhuaoil.yyweather.util.HttpCallBack;
import com.runhuaoil.yyweather.util.HttpUtil;
import com.runhuaoil.yyweather.util.MySharedPreferences;
import com.runhuaoil.yyweather.util.ResponseHandle;


/**
 * Created by RunHua on 2016/10/31.
 */

public class AutoUpdateService extends Service {

    private AlarmManager alarmMgr;
    private PendingIntent pi;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();

        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        long lifeTime = SystemClock.elapsedRealtime() + 6 * 60 * 60 * 1000;

        Intent i = new Intent(this, AutoUpdateReceiver.class);
        pi = PendingIntent.getBroadcast(this, 0, i,0);

        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, lifeTime, pi);

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather(){
        SharedPreferences pre = MySharedPreferences.getInstance(this);
        String countyName = pre.getString("countyName", "");

        if (!TextUtils.isEmpty(countyName)){
            String address1 = "http://wthrcdn.etouch.cn/weather_mini?city=" + countyName;
            HttpUtil.sendHttpRequest(address1, new HttpCallBack() {
                @Override
                public void onFinish(String responseData) {
                    ResponseHandle.handleWeatherData(responseData, AutoUpdateService.this);
                }

                @Override
                public void onError(Exception e) {
                    //Toast.makeText(AutoUpdateService.this, "后台更新天气失败", Toast.LENGTH_SHORT).show();
                }
            });

            String address2 = "http://wthrcdn.etouch.cn/WeatherApi?city=" + countyName;
            HttpUtil.sendHttpRequest(address2, new HttpCallBack() {
                @Override
                public void onFinish(String responseData) {
                    ResponseHandle.handlePulishTime(responseData, AutoUpdateService.this);
                }

                @Override
                public void onError(Exception e) {
                    //Toast.makeText(AutoUpdateService.this, "后台更新天气失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        if (alarmMgr != null){
            alarmMgr.cancel(pi);
        }
        super.onDestroy();
    }
}
