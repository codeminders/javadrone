package com.codeminders.ardrone;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    private static final long CONNECT_TIMEOUT = 60000;
    ARDrone drone;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            drone = new ARDrone();
            drone.connect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        showButtons();
        
    }
    
    private void showButtons() {

        final Button takeOff = (Button) findViewById(R.id.button1);
        if (takeOff != null) {
            takeOff.setVisibility(View.VISIBLE);
            takeOff.setOnClickListener(new View.OnClickListener()  {
                public void onClick(View v) {
                    takeOff();
                }
            });
        }
        final Button land = (Button) findViewById(R.id.button2);
        if (land != null) {
            land.setVisibility(View.VISIBLE);
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
            // Wait until drone is ready
            drone.waitForReady(CONNECT_TIMEOUT);
            // do TRIM operation
            drone.trim();
            // Take off
            System.err.println("Taking off");
            drone.takeOff();
        } catch(Throwable e)
        {
            e.printStackTrace();
        }
    }
    
    private void land() {
        try
        {
            // Land
            System.err.println("Landing");
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
}
