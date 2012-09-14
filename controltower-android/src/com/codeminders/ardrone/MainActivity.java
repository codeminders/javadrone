package com.codeminders.ardrone;



import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;

import com.codeminders.ardrone.controller.PS3Controller;
import com.codeminders.ardrone.controller.SonyPS3Controller;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements DroneVideoListener {
    
    private static final long CONNECTION_TIMEOUT = 100000;
  
    static ARDrone drone;
    ImageView display;
    TextView state;
    Button connectButton;
    PS3Controller controller;
    Button connectPs3Button;
    
    UsbManager manager;
    
    ControllerThread ctrThread; 
    
    private static final String ACTION_USB_PERMISSION = "com.access.device.USB_PERMISSION";
    
    static MainActivity mainActivity;
    
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice deviceConnected = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        if (deviceConnected != null) {
                           
                            if (SonyPS3Controller.isA(deviceConnected)) {
                                try {
                                    controller = new SonyPS3Controller(deviceConnected, manager);
                                    connectPs3Button.setEnabled(false);
                                    connectPs3Button.setText("Connected");                     
                                    // Start joystic reading thread
                                    ctrThread = new ControllerThread(drone, controller);
                                    ctrThread.setName("Controll Thread");
                                    ctrThread.start();
                                    
                                } catch (Throwable e) {
                                    connectPs3Button.setText("Error");                     
                                }
                            }

                        }
                    } else {
                        connectPs3Button.setText("Denied");
                        connectPs3Button.setEnabled(false);
                    }
                }
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.activity_main);

        java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");

        state = (TextView) findViewById(R.id.state);
        display =  (ImageView) findViewById(R.id.display);
        connectButton = (Button) findViewById(R.id.connect);
        Button btnLand = (Button) findViewById(R.id.land);
        btnLand.setEnabled(false);
        Button btnTakeOff = (Button) findViewById(R.id.takeOff);
        btnTakeOff.setEnabled(false);
        
        final Button btnConnect = (Button) findViewById(R.id.connect);
        btnConnect.setOnClickListener(new View.OnClickListener()  {
                public void onClick(View v) {
                    state.setTextColor(Color.RED);
                    state.setText("Connecting...");
                    (new DroneStarter()).execute(MainActivity.drone); 
                    }
        });
        
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        connectPs3Button = (Button) findViewById(R.id.ps3Button);

        connectPs3Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                UsbDevice device = null;
                while (deviceIterator.hasNext()) {
                    device = deviceIterator.next();
                    break;
                }

                if (null != device) {
                    PendingIntent permissionIntent = PendingIntent.getBroadcast(mainActivity, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    IntentFilter permissionFilter = new IntentFilter(ACTION_USB_PERMISSION);
                    mainActivity.registerReceiver(usbReceiver, permissionFilter);
                    manager.requestPermission(device, permissionIntent);
                }
            }
        });
    }
    

    public void showButtons() {

        final Button takeOff = (Button) findViewById(R.id.takeOff);
        if (takeOff != null) {
            takeOff.setVisibility(View.VISIBLE);
            takeOff.setClickable(true);
            takeOff.setEnabled(true);
            takeOff.setOnClickListener(new View.OnClickListener()  {
                public void onClick(View v) {
                    takeOff();
                }
            });
        }
        final Button land = (Button) findViewById(R.id.land);
        if (land != null) {
            land.setVisibility(View.VISIBLE);
            land.setClickable(true);
            land.setEnabled(true);
            land.setOnClickListener(new View.OnClickListener()  {
                public void onClick(View v) {
                    land();
                }
            });
        }
    }
    
    private void takeOff() {
        
        try
        {
            drone.clearEmergencySignal();
            drone.trim();
            drone.takeOff();
        } catch(Throwable e)
        {
            e.printStackTrace();
        }
    }
    
    private void land() {
        try
        {
            drone.land();
        } catch(Throwable e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        menu.add(0, 1, 1, R.string.str_exit);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {        
        switch (item.getItemId()) {
        case 0:
            break;
        case 1:
            exitOptionsDialog();
            break;
        }
        return true;
          
    }
    
    private void exitOptionsDialog()
    {
        // Disconnect from the done
        try {
            drone.disconnect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finish();
    }
    
    @Override
    public void frameReceived(int startX, int startY, int w, int h,
            int[] rgbArray, int offset, int scansize) {
        (new VideoDisplayer(startX, startY, w, h, rgbArray, offset, scansize)).execute();
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
            // do stuff
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
            MainActivity.drone = drone; // passing in null objects will not pass object refs
            drone.connect();
            drone.clearEmergencySignal();
            drone.waitForReady(CONNECTION_TIMEOUT);
            drone.playLED(1, 10, 4);
            drone.addImageListener(MainActivity.mainActivity);
            drone.selectVideoChannel(ARDrone.VideoChannel.HORIZONTAL_ONLY);
            drone.setCombinedYawMode(true);
            return true;
        } catch (Exception e) {
            try {
                drone.clearEmergencySignal();
                drone.clearImageListeners();
                drone.clearNavDataListeners();
                drone.clearStatusChangeListeners();
                drone.disconnect();
            } catch (Exception e1) {
            }
          
        }
        return false;
    }

    protected void onPostExecute(Boolean success) {
        if (success.booleanValue()) {
            state.setTextColor(Color.GREEN);
            state.setText("Connected");
            connectButton.setEnabled(false);
            mainActivity.showButtons();
        } else {
            state.setTextColor(Color.RED);
            state.setText("Error");
        }
    }
}

}
