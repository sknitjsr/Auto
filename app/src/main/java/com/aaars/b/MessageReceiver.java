package com.aaars.b;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import static android.content.Context.LOCATION_SERVICE;
import static com.aaars.b.Root.USER_ID;

public class MessageReceiver extends BroadcastReceiver {

    private static MessageListener mListener;
    LocationManager locationManager;
    SmsManager smsManager;
    Module md2;
    DatabaseReference last;
    LastRun lr;
    Boolean flag;
    GoogleSignInAccount account;


    @Override
    public void onReceive(final Context context, final Intent intent) {
        try {
            account = GoogleSignIn.getLastSignedInAccount(context);
            if (account != null && USER_ID == null) {
                USER_ID = account.getId();
            }

            md2 = new Module();
            smsManager = SmsManager.getDefault();
            flag = true;

            //LASTRUN
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            last = database.getInstance().getReference().child("users").child(USER_ID).child("lastrun");
            ValueEventListener lastr = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        lr = dataSnapshot.getValue(LastRun.class);
                        if(flag) {
                            //call(context, intent);
                            flag = false;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(context, "Failed to load post.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            last.addValueEventListener(lastr);
            call(context, intent);
        }
        catch (Exception e) {
            Toast.makeText(context, "Module Ran, Restart app to sync with server", Toast.LENGTH_LONG).show();
        }
    }

    void call(final Context context, final Intent intent) {
        try {

            md2 = new Module();
            smsManager = SmsManager.getDefault();

            if (account != null && USER_ID == null) {
                USER_ID = account.getId();
            }

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference drmd2 = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("6");
            ValueEventListener wifiTimerListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        md2 = dataSnapshot.getValue(Module.class);
                        Bundle data = intent.getExtras();
                        Object[] pdus = (Object[]) data.get("pdus");
                        for (int i = 0; i < pdus.length; i++) {
                            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);

                            if (smsMessage.getDisplayMessageBody().equalsIgnoreCase("wifi on " + md2.parameters.get(0)) && md2.parameters.get(1).equals("true")) {
                                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                if (wifiManager != null) {
                                    wifiManager.setWifiEnabled(true);
                                    callNotification("A Module Ran - WiFi Turned On","WiFi was turned On!", context);
                                }

                                Calendar cc = Calendar.getInstance();
                                lr.lastrun.set(6,"" + cc.getTime());
                                last.setValue(lr);

                            } else if (smsMessage.getDisplayMessageBody().equalsIgnoreCase("wifi off " + md2.parameters.get(0)) && md2.parameters.get(2).equals("true")) {
                                Toast.makeText(context , "hii wiffi off", Toast.LENGTH_LONG).show();
                                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                if (wifiManager != null) {
                                    wifiManager.setWifiEnabled(false);
                                    //Toast.makeText(context , "wifi manager", Toast.LENGTH_LONG).show();
                                    callNotification("A Module Ran - WiFi Turned Off","WiFi was turned Off!", context);
                                }

                                Calendar cc = Calendar.getInstance();
                                lr.lastrun.set(7,"" + cc.getTime());
                                last.setValue(lr);

                            } else if (smsMessage.getDisplayMessageBody().equalsIgnoreCase("volume max " + md2.parameters.get(0)) && md2.parameters.get(3).equals("true")) {
                                AudioManager mobilemode = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                                mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                mobilemode.setStreamVolume(AudioManager.STREAM_MUSIC, mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                                mobilemode.setStreamVolume(AudioManager.STREAM_ALARM, mobilemode.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
                                mobilemode.setStreamVolume(AudioManager.STREAM_RING, mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                                callNotification("A Module Ran - Volume Turned to Max","Caution : Volume Max !!", context);

                                Calendar cc = Calendar.getInstance();
                                lr.lastrun.set(8,"" + cc.getTime());
                                last.setValue(lr);

                            } else if (smsMessage.getDisplayMessageBody().equalsIgnoreCase("volume min " + md2.parameters.get(0)) && md2.parameters.get(4).equals("true")) {
                                AudioManager mobilemode = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                                mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    mobilemode.setStreamVolume(AudioManager.STREAM_MUSIC, mobilemode.getStreamMinVolume(AudioManager.STREAM_MUSIC), 0);
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    mobilemode.setStreamVolume(AudioManager.STREAM_ALARM, mobilemode.getStreamMinVolume(AudioManager.STREAM_ALARM), 0);
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    mobilemode.setStreamVolume(AudioManager.STREAM_RING, mobilemode.getStreamMinVolume(AudioManager.STREAM_RING), 0);
                                }
                                callNotification("A Module Ran - Volume Turned to Min","Caution : Volume Min !!", context);

                                Calendar cc = Calendar.getInstance();
                                lr.lastrun.set(9,"" + cc.getTime());
                                last.setValue(lr);

                            } else if (smsMessage.getDisplayMessageBody().equalsIgnoreCase("location " + md2.parameters.get(0)) && md2.parameters.get(6).equals("true")) {
                                final LocationListener locationListener = new LocationListener() {
                                    @Override
                                    public void onLocationChanged(final Location location) {
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference ref = database.getReference("users").child(USER_ID).child("modules");
                                        ref.child("1").child("currentLocation").setValue(location);
                                        Calendar cc = Calendar.getInstance();
                                        lr.lastrun.set(12,"" + cc.getTime());
                                        last.setValue(lr);
                                    }

                                    @Override
                                    public void onStatusChanged(String s, int i, Bundle bundle) {
                                    }

                                    @Override
                                    public void onProviderEnabled(String s) {
                                    }

                                    @Override
                                    public void onProviderDisabled(String s) {
                                    }
                                };

                                locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
                                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION))
                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15,
                                            50, locationListener);

                                Calendar cc = Calendar.getInstance();
                                lr.lastrun.set(12,"" + cc.getTime());
                                last.setValue(lr);

                                Location lc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                String content = "UNABLE";
                                if (lc != null)
                                    content = "I am here: https://www.google.com/maps/search/?api=1&query=" + lc.getLatitude() + "," + lc.getLongitude() + " - Sent via Automator";
                                smsManager.sendTextMessage(smsMessage.getOriginatingAddress(), null, content, null, null);
                                callNotification("A Module Ran - Location ","Location Sent", context);
                            } else if (smsMessage.getDisplayMessageBody().equalsIgnoreCase("lostphone " + md2.parameters.get(0)) && md2.parameters.get(5).equals("true")) {

                                Calendar cc = Calendar.getInstance();
                                lr.lastrun.set(15,"" + cc.getTime());
                                last.setValue(lr);

                                //AUDIO MAX
                                AudioManager mobilemode = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                                mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                mobilemode.setStreamVolume(AudioManager.STREAM_MUSIC, mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                                mobilemode.setStreamVolume(AudioManager.STREAM_ALARM, mobilemode.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
                                mobilemode.setStreamVolume(AudioManager.STREAM_RING, mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                                callNotification("Device Lost ","Location Sent", context);

                                //WIFI ON
                                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                if (wifiManager != null) {
                                    wifiManager.setWifiEnabled(true);
                                }

                                final LocationListener locationListener = new LocationListener() {
                                    @Override
                                    public void onLocationChanged(final Location location) {
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference ref = database.getReference("users").child(USER_ID).child("modules");
                                        ref.child("1").child("currentLocation").setValue(location);
                                    }

                                    @Override
                                    public void onStatusChanged(String s, int i, Bundle bundle) {
                                    }

                                    @Override
                                    public void onProviderEnabled(String s) {
                                    }

                                    @Override
                                    public void onProviderDisabled(String s) {
                                    }
                                };

                                locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
                                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION))
                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15,
                                            50, locationListener);

                                Location lc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                String content = "I am here: https://www.google.com/maps/search/?api=1&query=" + lc.getLatitude() + "," + lc.getLongitude() + " - Sent via Automator";
                                smsManager.sendTextMessage(smsMessage.getOriginatingAddress(), null, content, null, null);

                                MediaPlayer mpintro = MediaPlayer.create(context, R.raw.alarm);
                                mpintro.setLooping(false);
                                mpintro.start();
                            }

                            //mListener.messageReceived(message);
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(context, "Failed to load post.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            drmd2.addValueEventListener(wifiTimerListener);
        }
        catch (Exception e) {
            Toast.makeText(context, "Module Ran, Restart app to sync with server", Toast.LENGTH_LONG).show();
        }
    }

    public static void bindListener(MessageListener listener){
        mListener = listener;
    }
    public void callNotification(String title, String text, Context context) {
        Intent intent = new Intent(context, Splash.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "default")
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setSmallIcon(R.drawable.alpha)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, mBuilder.build());
    }



}
