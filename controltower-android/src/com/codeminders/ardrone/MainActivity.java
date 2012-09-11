package com.codeminders.ardrone;



import java.net.InetAddress;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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
    
    static MainActivity mainActivity;
    
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
            e.printStackTrace();
            try {
                drone.clearEmergencySignal();
                drone.clearImageListeners();
                drone.clearNavDataListeners();
                drone.clearStatusChangeListeners();
                drone.disconnect();
            } catch (Exception e1) {
                e1.printStackTrace();
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
