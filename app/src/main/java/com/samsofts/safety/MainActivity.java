package com.samsofts.safety;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity{

    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    SharedPreferences sharedPreferences;
    FusedLocationProviderClient fusedLocationProviderClient;
    EditText contactNumber,messageText,recorderDuration;
    Button save;
    ConstraintLayout background;
    private static String mFileName = null;
    MediaRecorder recorder;
    Context context;
    @Nullable



    int state;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        checkPermissions();
        contactNumber=(EditText) findViewById (R.id.contact_number);
        messageText=(EditText) findViewById (R.id.message_text);
        recorderDuration=(EditText)findViewById (R.id.recorder_duration) ;
        background=(ConstraintLayout) findViewById (R.id.bg);
        save=(Button) findViewById (R.id.button) ;
//        sharedPreferences= PreferenceManager.getDefaultSharedPreferences (this.getBaseContext ());
        SharedPreferences sharedPreferences= getApplicationContext ().getSharedPreferences ("data",MODE_MULTI_PROCESS);
        contactNumber.setText (sharedPreferences.getString ("number",""));
        messageText.setText (sharedPreferences.getString ("message",""));
        recorderDuration.setText (sharedPreferences.getString ("recordingDuration",""));
        context = getApplicationContext ();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient (this);
        mFileName = Environment.getExternalStorageDirectory ().getAbsolutePath ();
        mFileName += "/AudioRecording.3gp";
        state=0;



        save.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit ().putString ("number",contactNumber.getText ().toString ()).apply ();
                sharedPreferences.edit ().putString ("message",messageText.getText ().toString ()).apply ();
                sharedPreferences.edit ().putString ("recordingDuration",recorderDuration.getText ().toString ()).apply ();
//                lowBrightness ();
//                sendSMS ();
//                record ();
//                startService (new Intent (this));
                save.setText ("Saved");
            }
        });
        background.setOnTouchListener (new View.OnTouchListener () {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction ()==MotionEvent.ACTION_DOWN){
                }


                return true;
            }
        });

    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_UP) {
                    if (event.getEventTime() - event.getDownTime() > ViewConfiguration.getLongPressTimeout()) {
                        //TODO long click action
                        startService (new Intent (this,BgService.class));


                    } else {
                        //TODO click action
                    }
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_UP) {
                    if (event.getEventTime() - event.getDownTime() > ViewConfiguration.getLongPressTimeout()) {
                        //TODO long click action
//                        lowBrightness ();
//                        sendSMS ();
//                        record ();
                        startService (new Intent (this,BgService.class));

                    } else {
                        //TODO click action
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
    public void record() {
        recorder = new MediaRecorder ();
        recorder.setAudioSource (MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat (MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder (MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile (mFileName);
        try {
            recorder.prepare ();
            recorder.start ();   // Recording will start
            Toast.makeText (getApplicationContext (), "Playing recording", Toast.LENGTH_SHORT).show ();
        } catch (IOException e) {
            e.printStackTrace ();
            Toast.makeText (getApplicationContext (), e.getMessage ().toString (), Toast.LENGTH_LONG).show ();

        }


        new Timer ().schedule (new TimerTask () {

            @Override
            public void run() {
                runOnUiThread (new Runnable () {
                    @Override
                    public void run() {
                        recorder.stop ();
                        recorder.release ();
                        Toast.makeText (getApplicationContext (), "Playing stopped", Toast.LENGTH_SHORT).show ();

                    }
                });

            }

        }, Integer.parseInt (sharedPreferences.getString ("recordingDuration",""))*1000);
        //<-- Execute code after 15000 ms i.e after 15 Seconds.
        return;
    }

    public void lowBrightness() {
        try {
            android.provider.Settings.System.putInt (getContentResolver (),
                    Settings.System.SCREEN_BRIGHTNESS, 10);
        } catch (Exception e) {
            e.printStackTrace ();
            Toast.makeText (getApplicationContext (), e.getMessage ().toString (), Toast.LENGTH_LONG).show ();
        }

    }

    public void sendSMS() {
        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions (MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
            return;
        }
//        fusedLocationProviderClient.getLastLocation ().addOnCompleteListener (new OnCompleteListener<Location> () {
//            @Override
//            public void onComplete(@NonNull Task<Location> task) {
//                Location location=task.getResult ();
//                if(location!=null && state==0){
//                    state=1;
//                    SmsManager smsManager=SmsManager.getDefault ();
//                    smsManager.sendTextMessage ("00",null, "https://www.google.com/maps/place/"+String.valueOf (location.getLatitude ())+","+String.valueOf (location.getLongitude ()),null,null);
//                }
//            }
//        });
        fusedLocationProviderClient.getLastLocation ().addOnSuccessListener (new OnSuccessListener<Location> () {
            @Override
            public void onSuccess(Location location) {
                if(location!=null && state==0){
                    state=1;
                    SmsManager smsManager=SmsManager.getDefault ();
                    smsManager.sendTextMessage (sharedPreferences.getString ("number","").toString (),
                            null,
                            sharedPreferences.getString ("message","test").toString ()+"https://www.google.com/maps/place/"+String.valueOf (location.getLatitude ())+","+String.valueOf (location.getLongitude ()),
                            null,null);
                }
            }
        });

    }

    public void modifySystemSetting(Context c){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(Settings.System.canWrite (c)){

            }
            else {
                c.startActivity (new Intent (Settings.ACTION_MANAGE_WRITE_SETTINGS));
            }
        }
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    public void deniedAndStop(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    finish ();
                }
            }
        }
    }
    public void checkPermissions() {


        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        modifySystemSetting (this);
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        deniedAndStop(this,PERMISSIONS);

    }


}