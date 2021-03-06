package com.example.scbcchoi.eatemup;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class Settings extends AppCompatActivity {
    private Dialog timePickerDialog;
    private static SharedPreferences settingsLocalStorage;
    private static SharedPreferences settingsStringLocalStorage;
    private static int defaulthour = 17;
    private static int defaultmin = 30;
    private Dialog settingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        int hourOfDay = getInt("hour", this);
        int minute = getInt("minute", this);
        if(hourOfDay < 0) hourOfDay = defaulthour; //by default we set it to 17:30
        if(minute < 0) minute = defaultmin;

        TextView dh = findViewById(R.id.defaultHour);
        TextView dm = findViewById(R.id.defaultMinute);

        String tempHour = Integer.toString(hourOfDay);
        String tempMinute = Integer.toString(minute);

        if (hourOfDay < 10){
            tempHour = "0" + tempHour;
        }
        if (minute < 10){
            tempMinute = "0" + tempMinute;
        }


        dh.setText(tempHour);
        dm.setText(tempMinute);
    }

    public static void setInt(String key, int val, Context context){
        settingsLocalStorage = context.getSharedPreferences("settingsLocalStorage", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settingsLocalStorage.edit();
        editor.putInt(key, val)
                .apply();
    }

    //returns -1 if key doesn't exist, otherwise returns corresponding int value to the key
    public static int getInt(String key, Context context){
        settingsLocalStorage = context.getSharedPreferences("settingsLocalStorage", Context.MODE_PRIVATE);
        return settingsLocalStorage.getInt(key, -1);
    }

    //returns "" if key doesn't exist, otherwise returns corresponding int value to the key
    public static String getStr(String key, Context context){
        settingsStringLocalStorage = context.getSharedPreferences("settingsStringLocalStorage", Context.MODE_PRIVATE);
        return settingsStringLocalStorage.getString(key, "");
    }

    public static void setStr(String key, String val, Context context){
        settingsStringLocalStorage = context.getSharedPreferences("settingsStringLocalStorage", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settingsStringLocalStorage.edit();
        editor.putString(key, val)
                .apply();
    }



    //init background service
    public static void backgroundInit(Context context){
        //init receiver
        ComponentName receiver = new ComponentName(context, BootRec.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        //init background intent
        Intent intent = new Intent(context, BackgroundService.class);
        //Intent intent = new Intent(context, BootRec.class);
        PendingIntent alarmIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //try to get hour of day and minute from local storage
        int hourOfDay = getInt("hour", context);
        int minute = getInt("minute", context);
        if(hourOfDay < 0) hourOfDay = defaulthour; //by default we set it to 17:30
        if(minute < 0) minute = defaultmin;

        System.out.println("hour is " + hourOfDay + ", minute is " + minute);

        //Set alarm for each day.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY ,
                alarmIntent);
    }

    public void doneSetting(View v){
        //set notification times
        TextView edtHour = findViewById(R.id.defaultHour);
        TextView edtMinute = findViewById(R.id.defaultMinute);
        String hour = edtHour.getText().toString();
        String minute = edtMinute.getText().toString();
        if(hour.equals("")) hour = Integer.toString(defaulthour);
        if(minute.equals("")) minute = Integer.toString(defaultmin);
        int dhour = Integer.valueOf(hour);
        int dmin = Integer.valueOf(minute);
        //proceed only if dhour and dmin are valid
        if(!hour.equals("") && !minute.equals("") && dhour >= 0 && dhour < 24 && dmin >= 0 && dmin < 60){
            setInt("hour", dhour, this);
            setInt("minute", dmin, this);
            //Settings.setStr("todaysDate", "", this);
            System.out.println("Background Init!");
            backgroundInit(this);

            //everything is fine. we go to main
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
            finish();
        }
        //this means that dhour or dmin are not valid.
        else{
            Toast.makeText(this, "invalid hour or minutes!", Toast.LENGTH_SHORT).show();
        }
    }

    public void clearSettings(View v){
        settingDialog = new Dialog(v.getContext());
        settingDialog.setContentView(R.layout.confirm);
        settingDialog.show();
    }

    public void yes(View v){
        ListsModel lm = new ListsModel(this);
        lm.clearList("history");
        lm.clearList("inventory");
        lm.clearList("shopping");
        //Settings.setStr("todaysDate", "", this);
        settingDialog.dismiss();;
    }

    public void no(View v){
        settingDialog.dismiss();
    }

    public void setTimeClick(View view){
        timePickerDialog = new Dialog(view.getContext());
        timePickerDialog.setContentView(R.layout.dialog_setting_timepicker);

        TimePicker timePicker = timePickerDialog.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);

        TextView edtHour = findViewById(R.id.defaultHour);
        TextView edtMinute = findViewById(R.id.defaultMinute);

        String hour = edtHour.getText().toString();
        String minute = edtMinute.getText().toString();

        if(hour.equals("") || minute.equals("")){
            timePicker.setCurrentHour(17);
            timePicker.setCurrentMinute(30);
        } else {
            timePicker.setCurrentHour(Integer.parseInt(hour));
            timePicker.setCurrentMinute(Integer.parseInt(minute));
        }
        timePickerDialog.show();
    }

    public void dialogTimeCancel(View view){
        timePickerDialog.dismiss();

    }

    public void dialogTimeSelect(View view){
        TimePicker timePicker = timePickerDialog.findViewById(R.id.time_picker);

        int hours = timePicker.getCurrentHour();
        int minutes = timePicker.getCurrentMinute();

        TextView edtHour = findViewById(R.id.defaultHour);
        TextView edtMinute = findViewById(R.id.defaultMinute);

        String sHours =  Integer.toString(hours);
        String sMinutes = Integer.toString(minutes);

        if (hours < 10){
            sHours = "0" + sHours;
        }
        if (minutes < 10){
            sMinutes = "0" + sMinutes;
        }
        edtHour.setText(sHours);
        edtMinute.setText(sMinutes);

        timePickerDialog.dismiss();
    }
}
