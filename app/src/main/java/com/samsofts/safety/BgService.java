package com.samsofts.safety;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class BgService extends Service {

    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * {@link IBinder} is usually for a complex interface
     * that has been <a href="{@docRoot}guide/components/aidl.html">described using
     * aidl</a>.
     *
     * <p><em>Note that unlike other application components, calls on to the
     * IBinder interface returned here may not happen on the main thread
     * of the process</em>.  More information about the main thread can be found in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">Processes and
     * Threads</a>.</p>
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        lowBrightness ();
        record ();
        sendSMS ();
        return super.onStartCommand (intent, flags, startId);
    }

    public void record() {
        SharedPreferences sharedPreferences= getApplicationContext ().getSharedPreferences ("data",MODE_MULTI_PROCESS);

        String mFileName;
        mFileName = Environment.getExternalStorageDirectory ().getAbsolutePath ();
        mFileName += "/AudioRecording.3gp";
        MediaRecorder recorder;
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

        SystemClock.sleep (Integer.parseInt (sharedPreferences.getString ("recordingDuration", "")) * 1000);
        recorder.stop ();
        recorder.release ();
        Toast.makeText (getApplicationContext (), "Playing stopped", Toast.LENGTH_SHORT).show ();

//        new Timer ().schedule (new TimerTask () {
//
//            @Override
//            public void run() {
//                runOnUiThread (new Runnable () {
//                    @Override
//                    public void run() {
//                        recorder.stop ();
//                        recorder.release ();
//                        Toast.makeText (getApplicationContext (), "Playing stopped", Toast.LENGTH_SHORT).show ();
//
//                    }
//                });
//
//            }
//
//        }, Integer.parseInt (sharedPreferences.getString ("recordingDuration", "")) * 1000);
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

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient (this);

        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
        fusedLocationProviderClient.getLastLocation ().addOnSuccessListener (new OnSuccessListener<Location> () {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    SharedPreferences sharedPreferences= getApplicationContext ().getSharedPreferences ("data",MODE_MULTI_PROCESS);
                    SmsManager smsManager = SmsManager.getDefault ();
                    smsManager.sendTextMessage (sharedPreferences.getString ("number", "").toString (),
                            null,
                            sharedPreferences.getString ("message", "test").toString () + "https://www.google.com/maps/place/" + String.valueOf (location.getLatitude ()) + "," + String.valueOf (location.getLongitude ()),
                            null, null);
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
    
}
