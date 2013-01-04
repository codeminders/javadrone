package com.codeminders.ardrone;



import java.io.IOException;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;

import com.codeminders.ardrone.ARDrone.State;
import com.codeminders.ardrone.controller.usbhost.AfterGlowUsbHostController;
import com.codeminders.ardrone.controller.usbhost.SonyPS3UsbHostController;
import com.codeminders.ardrone.controller.usbhost.UsbHostController;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint({ "NewApi", "NewApi", "NewApi", "NewApi" })
public class MainActivity extends Activity implements DroneVideoListener, OnSharedPreferenceChangeListener {
    
    private static final long CONNECTION_TIMEOUT = 10000;
  
    static ARDrone drone;
    
    ImageView display;
    ImageView ps3Image;
    TextView state;
    TextView joystick_state;
    Button connectButton;
    UsbHostController controller;
    Button btnConnectUsbControllerButton;
    Button btnTakeOffOrLand;
    
    private Builder usbNotSupportedDialog;
    
    UsbManager manager;
    ControllerThread ctrThread; 
    
    private static final String ACTION_USB_PERMISSION = "com.access.device.USB_PERMISSION";

    private static final String TAG = "AR.Drone";
    
    SharedPreferences prefs;
    
    boolean isVisible = true; 
            
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice deviceConnected = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        tryConnectPS3Controller(deviceConnected);
                    } else {
                        joystick_state.setText("Denied");                       
                    }
                }
            }
        }
    };

    private Builder turnOnWiFiDialog;

  
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        setContentView(R.layout.activity_main);

        java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");

        state = (TextView) findViewById(R.id.state);
        joystick_state = (TextView) findViewById(R.id.joystick_state);
        display =  (ImageView) findViewById(R.id.display);
        ps3Image =  (ImageView) findViewById(R.id.ps3image);
        connectButton = (Button) findViewById(R.id.connect);
        
        btnTakeOffOrLand = (Button) findViewById(R.id.takeOffOrland);
        btnTakeOffOrLand.setEnabled(false);
        
        turnOnWiFiDialog = new AlertDialog.Builder(this);
        turnOnWiFiDialog.setMessage("Please turn on WiFi and connect to AR.Drone wireless accsess point");
        turnOnWiFiDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
            dialog.dismiss();
        }});
        
        final Button btnConnect = (Button) findViewById(R.id.connect);
        btnConnect.setOnClickListener(new View.OnClickListener()  {
                public void onClick(View v) {
                    startARDroneConnection(btnConnect);
                }
        });
        
       
        btnConnectUsbControllerButton = (Button) findViewById(R.id.ps3Button);
        
        if (android.os.Build.VERSION.SDK_INT >= 12) {
            manager = (UsbManager) getSystemService(Context.USB_SERVICE); 
            final PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            
            btnConnectUsbControllerButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                    UsbDevice device = null;
                    while (deviceIterator.hasNext()) {
                        device = deviceIterator.next();
                        break;
                    }
             
                    if (null != device) {
                        if (!manager.hasPermission(device)) {
                            IntentFilter permissionFilter = new IntentFilter(ACTION_USB_PERMISSION);
                            registerReceiver(usbReceiver, permissionFilter);
                            manager.requestPermission(device, permissionIntent); 
                        } else {
                            tryConnectPS3Controller(device);
                        }
                    }
                }
            });
        } else {
            
            usbNotSupportedDialog = new AlertDialog.Builder(this);
            usbNotSupportedDialog.setMessage("Your phone does not support USB connections");
            usbNotSupportedDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }});
            
            btnConnectUsbControllerButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { 
                    usbNotSupportedDialog.show();
                }
            });
        }
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        
        if (android.os.Build.VERSION.SDK_INT >= 14 && !ViewConfiguration.get(this).hasPermanentMenuKey()) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.bootomButtons);
            Button btnSettings = new Button(this);
            btnSettings.setText(R.string.btn_settings);
            btnSettings.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.addView(btnSettings);
            btnSettings.setOnClickListener(new Button.OnClickListener(){
    
                @Override
                public void onClick(View arg0) {
                    startActivity(new Intent(arg0.getContext(), SettingsPrefs.class));
                }});
        }          
        
        if (prefs.getBoolean(PREF_AUTOCONNECT_DRONE, false)) {
            connectButton.performClick();
        }
        if (prefs.getBoolean(PREF_AUTOCONNECT_PS3, false)) {
            btnConnectUsbControllerButton.performClick();
        }
       
        findViewById(R.id.ps3imagesmall).setOnClickListener(new View.OnClickListener()  {
            public void onClick(View v) {
                ps3Image.setVisibility(ps3Image.getVisibility() != View.INVISIBLE ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }
    
    private void tryConnectPS3Controller(UsbDevice device) {
        if (device != null) {
            boolean joystickFound = false;
            
            if (SonyPS3UsbHostController.isA(device)) {
                try {
                    controller = new SonyPS3UsbHostController(device, manager);
                    joystickFound = true;
                } catch (Throwable e) {
                    joystick_state.setText("Error");                     
                }
            } else if (AfterGlowUsbHostController.isA(device)) {
                try {
                    controller = new AfterGlowUsbHostController(device, manager);
                    joystickFound = true;
                } catch (Throwable e) {
                    joystick_state.setText("Error");                     
                }
            }
            
            if (joystickFound) {
                btnConnectUsbControllerButton.setEnabled(false);
                joystick_state.setText("Connected");  
                joystick_state.setTextColor(Color.GREEN);
                // Start joystick reading thread
                ctrThread = new ControllerThread(drone, controller);
                ctrThread.setName("Controll Thread");
                loadControllerDeadZone();
                ctrThread.start();
            } else {
                joystick_state.setText("Not recognized"); 
            }

        }
    }
    
    private void startARDroneConnection(final Button btnConnect) {
        WifiManager connManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        
        if (connManager.isWifiEnabled()) {
            state.setTextColor(Color.RED);
            state.setText("Connecting..." +  connManager.getConnectionInfo().getSSID());
            btnConnect.setEnabled(false);
            (new DroneStarter()).execute(MainActivity.drone); 
        } else {
            turnOnWiFiDialog.show();
        }
    }       
    
    private void droneOnConnected() {
        
        state.setTextColor(Color.GREEN);
        state.setText("Connected");
        loadDroneSettingsFromPref();
        connectButton.setEnabled(false);
        drone.addImageListener(this);
        
        if (null != ctrThread) {
            ctrThread.setDrone(drone);
        }
        
        if (btnTakeOffOrLand != null) {
            btnTakeOffOrLand.setVisibility(View.VISIBLE);
            btnTakeOffOrLand.setClickable(true);
            btnTakeOffOrLand.setEnabled(true);
            btnTakeOffOrLand.setOnClickListener(new View.OnClickListener()  {
                public void onClick(View v) {
                    
                    if (null == drone || drone.getState() == State.DISCONNECTED) {
                        state.setText("Disconnected");
                        state.setTextColor(Color.RED);
                        connectButton.setEnabled(true);
                        return;
                    }
                    
                    if (btnTakeOffOrLand.getText().equals(getString(R.string.btn_land))) {
                        try
                        {
                            drone.land();
                        } catch(Throwable e)
                        {
                            Log.e(TAG, "Faliled to execute take off command" , e);
                        }
                        
                        btnTakeOffOrLand.setText(R.string.btn_take_off);
                    } else  {                        
                        try
                        {
                            drone.clearEmergencySignal();
                            drone.trim(); 
                            drone.takeOff();
                        } catch(Throwable e)
                        {
                            Log.e(TAG, "Faliled to execute take off command" , e);
                        }
                        btnTakeOffOrLand.setText(R.string.btn_land);
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {        
        switch (item.getItemId()) {
        case R.id.menu_settings:
            startActivity(new Intent(this, SettingsPrefs.class));
            break;
        case R.id.ps3_help:
            startActivity(new Intent(this, PS3ControllerHelp.class));
            break;
        case R.id.menu_exit:
            finish();
            break;
        }
        return true;
          
    }
    
    public static String PREF_AUTOCONNECT_DRONE = "pref_autoconnect_ardrone";
    public static String PREF_AUTOCONNECT_PS3 = "pref_autoconnect_ps3";
    
    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
        if (null != drone) {
            drone.resumeNavData();
            drone.resumeVideo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
        if (null != drone) {
            drone.pauseNavData();
            drone.pauseVideo();
        }
        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseResourses();        
    }

    private void releaseResourses() {
        if (null != ctrThread && ctrThread.isAlive()) { 
            ctrThread.finish();
        }
        if (null != drone) {
            try {
                drone.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "failed to stop ar. drone", e);
            }
        }
    }
    
    @Override
    public void frameReceived(int startX, int startY, int w, int h,
            int[] rgbArray, int offset, int scansize) {
        if (isVisible) {
            (new VideoDisplayer(startX, startY, w, h, rgbArray, offset, scansize)).execute(); 
        }
    }


    
private class VideoDisplayer extends AsyncTask<Void, Integer, Void> {
        
        public Bitmap b;
        public int[]rgbArray;
        public int offset;
        public int scansize;
        public int w;
        public int h;
        public VideoDisplayer(int x, int y, int width, int height, int[] arr, int off, int scan) {
            super();
            rgbArray = arr;
            offset = off;
            scansize = scan;
            w = width;
            h = height;
            
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            b =  Bitmap.createBitmap(rgbArray, offset, scansize, w, h, Bitmap.Config.RGB_565);
            b.setDensity(100);
            return null;
        }
        @Override
        protected void onPostExecute(Void param) {;
            ((BitmapDrawable)display.getDrawable()).getBitmap().recycle(); 
            display.setImageBitmap(b);
        }
    }

private class DroneStarter extends AsyncTask<ARDrone, Integer, Boolean> {
    
    @Override
    protected Boolean doInBackground(ARDrone... drones) {
        ARDrone drone = drones[0];
        try {
            drone = new ARDrone(InetAddress.getByAddress(ARDrone.DEFAULT_DRONE_IP), 10000, 60000);
            MainActivity.drone = drone;
            drone.connect();
            drone.clearEmergencySignal();
            drone.trim();
            drone.waitForReady(CONNECTION_TIMEOUT);
            drone.playLED(1, 10, 4);
            drone.selectVideoChannel(ARDrone.VideoChannel.HORIZONTAL_ONLY);
            drone.setCombinedYawMode(true);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to connect to drone", e);
            try {
                drone.clearEmergencySignal();
                drone.clearImageListeners();
                drone.clearNavDataListeners();
                drone.clearStatusChangeListeners();
                drone.disconnect();
            } catch (Exception ex) {
                Log.e(TAG, "Failed to clear drone state", ex);
            }
          
        }
        return false;
    }

    protected void onPostExecute(Boolean success) {
        if (success.booleanValue()) {
            droneOnConnected();
        } else {
            state.setTextColor(Color.RED);
            state.setText("Error");
            connectButton.setEnabled(true);
        }
    }
   }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
       if (key.equals(PREF_MAX_ALTITUDE)) {
           droneLoadMaxAltitude();
       } else if (key.equals(PREF_MAX_ANGLE)) {
           droneLoadMaxAngle();
       } else if (key.equals(PREF_MAX_VERICAL_SPEED)) {
           droneLoadMaxVerticalSpeed();
       } else if (key.equals(PREF_MAX_ROTATION_SPEED)) {
           drobeLoadMaxRotationSpeed();
       } else if (key.equals(PREF_MAX_CONTROLLER_DEDZONE)) {
           loadControllerDeadZone();
       }
    }
    
    public static String PREF_MAX_ALTITUDE = "pref_altitude_max";
    public static String PREF_MAX_ANGLE = "pref_angle_max";
    public static String PREF_MAX_VERICAL_SPEED = "pref_vertical_speed_max";
    public static String PREF_MAX_ROTATION_SPEED = "pref_rotation_speed_max";
    public static String PREF_MAX_CONTROLLER_DEDZONE = "pref_controller_deadzone";
    
    public static String DRONE_MAX_YAW_PARAM_NAME = "control:control_yaw";
    public static String DRONE_MAX_VERT_SPEED_PARAM_NAME = "control:control_vz_max";
    public static String DRONE_MAX_EULA_ANGLE = "control:euler_angle_max";
    public static String DRONE_MAX_ALTITUDE = "control:altitude_max";
    DecimalFormat twoDForm = new DecimalFormat("#.##");

    private void loadDroneSettingsFromPref() {
            droneLoadMaxAltitude();
            droneLoadMaxAngle();
            droneLoadMaxVerticalSpeed();
            drobeLoadMaxRotationSpeed();
            loadControllerDeadZone();
    }
            
    private void drobeLoadMaxRotationSpeed() {
        if (null != drone && prefs.contains(PREF_MAX_ROTATION_SPEED)) {
            setDroneParam(DRONE_MAX_YAW_PARAM_NAME, twoDForm.format(prefs.getFloat(PREF_MAX_ROTATION_SPEED, 50f) * Math.PI / 180f).replace(',', '.'));
        } 
    }

    private void loadControllerDeadZone() {
        if (null != ctrThread && prefs.contains(PREF_MAX_CONTROLLER_DEDZONE)) {
            ctrThread.setControlThreshhold(prefs.getFloat(PREF_MAX_ROTATION_SPEED, 30f) / 100f);
        }
        
    }

    private void droneLoadMaxVerticalSpeed() {
        if (null != drone && prefs.contains(PREF_MAX_VERICAL_SPEED)) {
            setDroneParam(DRONE_MAX_YAW_PARAM_NAME, String.valueOf(Math.round(prefs.getFloat(PREF_MAX_VERICAL_SPEED, 1f) * 1000)));
        } 
    }

    private void droneLoadMaxAngle() {
        if (null != drone && prefs.contains(PREF_MAX_ANGLE)) {
            setDroneParam(DRONE_MAX_EULA_ANGLE, twoDForm.format(prefs.getFloat(PREF_MAX_ANGLE, 6f) * Math.PI / 180f).replace(',', '.'));
        } 
    }

    private void droneLoadMaxAltitude() {
        if (null != drone && prefs.contains(PREF_MAX_ALTITUDE)) {
            setDroneParam(DRONE_MAX_ALTITUDE, String.valueOf(Math.round(prefs.getFloat(PREF_MAX_ALTITUDE, 1.5f) * 1000)));
        } 
    }
    
    private void setDroneParam(final String name, final String value) {
     new Thread(new Runnable() {  
            @Override
            public void run() {
                try {
                    drone.setConfigOption(name, value);
                    Log.d(TAG, "Drone parameter (" + name + ") is SET to value: " + value);
                } catch (IOException ex) {
                    Log.e(TAG, "Failed to set drone parameter (" + name + ") to value: " + value , ex);
                }
                
            }
        }).start();
    }

}
