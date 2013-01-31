/*
 * ControlTower.java
 *
 * Created on 17.05.2011, 13:41:27
 */
package com.codeminders.controltower;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.ARDrone.VideoChannel;
import com.codeminders.ardrone.DroneStatusChangeListener;
import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavDataListener;
import com.codeminders.ardrone.controllers.Controller;
import com.codeminders.ardrone.controllers.GameControllerState;
import com.codeminders.ardrone.controllers.ControllerStateChange;
import com.codeminders.ardrone.controllers.KeyboardController;
import com.codeminders.ardrone.controllers.hid.manager.HIDControllerFinder;
import com.codeminders.controltower.config.AssignableControl.ControllerButton;
import com.codeminders.controltower.config.ControlMap;
import com.codeminders.hidapi.ClassPathLibraryLoader;
import com.codeminders.hidapi.HIDDeviceNotFoundException;

/**
 * The central class that represents the main window and also manages the
 * drone update loop.
 *
 * @author normenhansen
 */
@SuppressWarnings("serial")
public class ControlTower extends javax.swing.JFrame implements DroneStatusChangeListener, NavDataListener
{

    private static final long READ_UPDATE_DELAY_MS = 5L;
    private static final long CONNECT_TIMEOUT = 10000L;
    private static final float DEFAULT_CONTROL_THRESHOLD = 0.05f;

    private final ImageIcon droneOn = new ImageIcon(
        getClass().getResource("/com/codeminders/controltower/images/drone_on.gif"));
    private final ImageIcon droneOff = new ImageIcon(
        getClass().getResource("/com/codeminders/controltower/images/drone_off.gif"));
    private final ImageIcon controllerOn = new ImageIcon(
        getClass().getResource("/com/codeminders/controltower/images/controller_on.png"));
     private final ImageIcon keyboradControllerOn = new ImageIcon(
        getClass().getResource("/com/codeminders/controltower/images/keyboard_on.png"));
    private final ImageIcon controllerOff = new ImageIcon(
        getClass().getResource("/com/codeminders/controltower/images/controller_off.png"));
    private final ImageIcon keyboradControllerOff = new ImageIcon(
        getClass().getResource("/com/codeminders/controltower/images/keyboard_off.png"));
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean flying = new AtomicBoolean(false);
    private final AtomicBoolean flipSticks = new AtomicBoolean(false);
    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    private ARDrone drone;
    private final AtomicReference<Controller> dev = new AtomicReference<Controller>();
    private final VideoPanel video = new VideoPanel();
    private final DroneConfig droneConfigWindow;
    private final ControlConfig controlConfigWindow;
    private final KeyboardControlConfig keyboardControlConfigWindow;
    private final BottomGaugePanel gauges = new BottomGaugePanel();
    private final ControlMap controlMap = new ControlMap();

    private float controlThreshold = DEFAULT_CONTROL_THRESHOLD;

    private static boolean isHIDLibLoaded = false;

    static
    {
        isHIDLibLoaded = ClassPathLibraryLoader.loadNativeHIDLibrary();
    }

    /**
     * Creates new form ControlTower
     */
    public ControlTower()
    {
        setAlwaysOnTop(true);
        initComponents();
        droneConfigWindow = new DroneConfig(this, true);
        keyboardControlConfigWindow = new KeyboardControlConfig(this, true);
        controlConfigWindow = new ControlConfig(this, true, controlMap);
        videoPanel.add(video);
        jPanel2.add(gauges);
        initController();
        initDrone();
        flipSticks.set(prefs.getBoolean("FLIP_STICKS", false));
        flipSticksCheckbox.setSelected(flipSticks.get());
    }

    private void initDrone()
    {
        try
        {
            drone = new ARDrone();
        }
        catch(UnknownHostException ex)
        {
            Logger.getLogger(ControlTower.class.getName()).error("Error creating drone object!", ex);
            return;
        }
        droneConfigWindow.setDrone(drone);
        gauges.setDrone(drone);
        video.setDrone(drone);
        drone.addStatusChangeListener(this);
        drone.addNavDataListener(this);
    }

    
    /**
     * Tries to find PS3 controller, else creates keyboard controller
     */
    private void initController()
    {
        Controller current = dev.get();
        if(current != null)
        {
            try
            {
                current.close();
            }
            catch(IOException ex)
            {
                Logger.getLogger(ControlTower.class.getName()).error("", ex);
            }
        }
        try
        {
            dev.set(findController());
        }
        catch(IOException ex)
        {
            Logger.getLogger(ControlTower.class.getName()).error("{0}", ex);
        }
        if(dev.get() == null)
        {
            System.err.println("No suitable controller found! Using keyboard");
            dev.set(new KeyboardController(this));
            updateControllerStatus(false);
        }
        else
        {
            System.err.println("Gamepad controller found");
            updateControllerStatus(true);
        }
    }

    private static Controller findController() throws IOException
    {
        if(!isHIDLibLoaded)
            return null;

        return HIDControllerFinder.findController();
    }

    private void updateLoop()
    {
        if(running.get())
        {
            return;
        }
        running.set(true);
        resetStatus();
        try
        {

            drone.addStatusChangeListener(new DroneStatusChangeListener()
            {

                @Override
                public void ready()
                {
                    try
                    {
                        Logger.getLogger(getClass().getName()).debug("updateLoop::ready()");
                        System.err.println("Configure");
                        droneConfigWindow.updateDrone();
                        drone.selectVideoChannel(VideoChannel.HORIZONTAL_ONLY);
                        drone.setCombinedYawMode(true);
                        drone.trim();
                    }
                    catch(IOException e)
                    {
                        drone.changeToErrorState(e);
                    }
                }
            });

            System.err.println("Connecting to the drone");
            drone.connect();
            drone.waitForReady(CONNECT_TIMEOUT);
            drone.clearEmergencySignal();
            System.err.println("Connected to the drone");
            try
            {
                GameControllerState oldpad = null;
                while(running.get())
                {
                    GameControllerState pad = dev.get().read();
                    ControllerStateChange pad_change = new ControllerStateChange(oldpad, pad);
                    oldpad = pad;

                    if(pad_change.isStartChanged() && pad_change.isStart())
                    {
                        controlMap.sendCommand(drone, ControllerButton.START);
                    }
                    if(pad_change.isSelectChanged() && pad_change.isSelect())
                    {
                        controlMap.sendCommand(drone, ControllerButton.SELECT);
                    }
                    if(pad_change.isPSChanged() && pad_change.isPS())
                    {
                        controlMap.sendCommand(drone, ControllerButton.PS);
                    }
                    if(pad_change.isTriangleChanged() && pad_change.isTriangle())
                    {
                        controlMap.sendCommand(drone, ControllerButton.TRIANGLE);
                    }
                    if(pad_change.isCrossChanged() && pad_change.isCross())
                    {
                        controlMap.sendCommand(drone, ControllerButton.CROSS);
                    }
                    if(pad_change.isSquareChanged() && pad_change.isSquare())
                    {
                        controlMap.sendCommand(drone, ControllerButton.SQUARE);
                    }
                    if(pad_change.isCircleChanged() && pad_change.isCircle())
                    {
                        controlMap.sendCommand(drone, ControllerButton.CIRCLE);
                    }
                    if(pad_change.isL1Changed() && pad_change.isL1())
                    {
                        controlMap.sendCommand(drone, ControllerButton.L1);
                    }
                    if(pad_change.isR1Changed() && pad_change.isR1())
                    {
                        controlMap.sendCommand(drone, ControllerButton.R1);
                    }
                    if(pad_change.isL2Changed() && pad_change.isL2())
                    {
                        controlMap.sendCommand(drone, ControllerButton.L2);
                    }
                    if(pad_change.isR2Changed() && pad_change.isR2())
                    {
                        controlMap.sendCommand(drone, ControllerButton.R2);
                    }
                    if(flying.get())
                    {
                        // Detecting if we need to move the drone

                        float leftX = pad.getLeftJoystickX() / 128f;
                        float leftY = pad.getLeftJoystickY() / 128f;

                        float rightX = pad.getRightJoystickX() / 128f;
                        float rightY = pad.getRightJoystickY() / 128f;

                        float leftRightTilt = 0f;
                        float frontBackTilt = 0f;
                        float verticalSpeed = 0f;
                        float angularSpeed = 0f;

                        float ct = 1 - controlThreshold;

                        if(Math.abs(leftX) > controlThreshold)
                        {
                            leftRightTilt = (leftX >= 0.0f ? (leftX - controlThreshold) / ct
                                                           : (leftX + controlThreshold) / ct);
                        }

                        if(Math.abs(leftY) > controlThreshold)
                        {
                            frontBackTilt = (leftY >= 0.0f ? (leftY - controlThreshold) / ct
                                                           : (leftY + controlThreshold) / ct);
                        }

                        if(Math.abs(rightX) > controlThreshold)
                        {
                            angularSpeed = (rightX >= 0.0f ? (rightX - controlThreshold) / ct
                                                           : (rightX + controlThreshold) / ct);
                        }

                        if(Math.abs(rightY) > controlThreshold)
                        {
                            verticalSpeed = -1 * (rightY >= 0.0f ? (rightY - controlThreshold) / ct
                                                                 : (rightY + controlThreshold) / ct);
                        }

                        if(leftRightTilt != 0 || frontBackTilt != 0 || verticalSpeed != 0 || angularSpeed != 0)
                        {
                            if(flipSticks.get())
                            {
                                drone.move(angularSpeed, -1 * verticalSpeed, -1 * frontBackTilt, leftRightTilt);
                            }
                            else
                            {
                                drone.move(leftRightTilt, frontBackTilt, verticalSpeed, angularSpeed);
                            }
                        }
                        else
                        {
                            drone.hover();
                        }
                    }

                    try
                    {
                        Thread.sleep(READ_UPDATE_DELAY_MS);
                    }
                    catch(InterruptedException e)
                    {
                        // Ignore
                    }
                }
            }
            finally
            {
                drone.disconnect();
            }
        }
        catch(HIDDeviceNotFoundException hex)
        {
            hex.printStackTrace();
        }
        catch(Throwable e)
        {
            e.printStackTrace();
        }
        resetStatus();
        running.set(false);
    }

    private void startUpdateLoop()
    {
        Thread thread = new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                updateLoop();
            }
        });
        thread.setName("ARDrone Control Loop");
        thread.start();
    }

    /**
     * Updates the drone status in the UI, queues command to AWT event dispatch thread
     *
     * @param available
     */
    private void updateDroneStatus(final boolean available)
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {
                if(!available)
                {
                    droneStatus.setForeground(Color.RED);
                    droneStatus.setIcon(droneOff);
                }
                else
                {
                    droneStatus.setForeground(Color.GREEN);
                    droneStatus.setIcon(droneOn);
                }
            }
        });

    }

    /**
     * Updates the controller status in the UI, queues command to AWT event dispatch thread
     *
     * @param available
     */
    private void updateControllerStatus(final boolean available)
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {
                if(!available)
                {
                    joystickControllerStatus.setForeground(Color.RED);
                    joystickControllerStatus.setIcon(controllerOff);
                    keyboardControllerStatus.setForeground(Color.GREEN);
                    keyboardControllerStatus.setIcon(keyboradControllerOn);                    
                }
                else
                {
                    joystickControllerStatus.setForeground(Color.GREEN);
                    joystickControllerStatus.setIcon(controllerOn);
                    keyboardControllerStatus.setForeground(Color.RED);
                    keyboardControllerStatus.setIcon(keyboradControllerOff);  
                }
            }
        });

    }

    /**
     * Updates the battery status in the UI, queues command to AWT event dispatch thread
     *
     * @param value
     */
    private void updateBatteryStatus(final int value)
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {
                batteryStatus.setText(value + "%");
                if(value < 15)
                {
                    batteryStatus.setForeground(Color.RED);
                }
                else if(value < 50)
                {
                    batteryStatus.setForeground(Color.ORANGE);
                }
                else
                {
                    batteryStatus.setForeground(Color.GREEN);
                }
            }
        });
    }

    /**
     * Resets the UI, queues command to AWT event dispatch thread
     *
     */
    private void resetStatus()
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {
                droneStatus.setForeground(Color.RED);
                droneStatus.setIcon(droneOff);
                batteryStatus.setForeground(Color.RED);
                batteryStatus.setText("0%");
            }
        });

    }

    @Override
    public void ready()
    {
        Logger.getLogger(getClass().getName()).debug("ready()");
        updateDroneStatus(true);
    }

    @Override
    public void navDataReceived(NavData nd)
    {
        //Logger.getLogger(getClass().getName()).debug("navDataReceived()");
        updateBatteryStatus(nd.getBattery());
        this.flying.set(nd.isFlying());
    }

    public void setControlThreshold(float sens)
    {
        controlThreshold = sens;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        droneStatus = new javax.swing.JLabel();
        batteryStatus = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        joystickControllerStatus = new javax.swing.JLabel();
        keyboardControllerStatus = new javax.swing.JLabel();
        flipSticksCheckbox = new javax.swing.JCheckBox();
        mappingButton = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jPanel1 = new javax.swing.JPanel();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        instrumentButton = new javax.swing.JButton();
        configureButton = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        videoPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Control Tower");

        jToolBar1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.add(jSeparator5);

        droneStatus.setForeground(java.awt.Color.red);
        droneStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/codeminders/controltower/images/drone_off.gif"))); // NOI18N
        droneStatus.setToolTipText("drone status (lit = connected)");
        droneStatus.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                droneStatusMouseReleased(evt);
            }
        });
        jToolBar1.add(droneStatus);

        batteryStatus.setFont(new java.awt.Font("Lucida Grande", 1, 10)); // NOI18N
        batteryStatus.setForeground(java.awt.Color.red);
        batteryStatus.setText("0%");
        jToolBar1.add(batteryStatus);
        jToolBar1.add(jSeparator1);

        joystickControllerStatus.setForeground(java.awt.Color.red);
        joystickControllerStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/codeminders/controltower/images/controller_off.png"))); // NOI18N
        joystickControllerStatus.setToolTipText("controller status (green = available)");
        joystickControllerStatus.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                joystickControllerStatusMouseReleased(evt);
            }
        });
        jToolBar1.add(joystickControllerStatus);

        keyboardControllerStatus.setForeground(java.awt.Color.red);
        keyboardControllerStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/codeminders/controltower/images/keyboard_off.png"))); // NOI18N
        keyboardControllerStatus.setToolTipText("keyboard status (green = available)");
        keyboardControllerStatus.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                keyboardControllerStatusMouseReleased(evt);
            }
        });
        jToolBar1.add(keyboardControllerStatus);

        flipSticksCheckbox.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        flipSticksCheckbox.setText("flip sticks");
        flipSticksCheckbox.setFocusable(false);
        flipSticksCheckbox.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        flipSticksCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flipSticksCheckboxActionPerformed(evt);
            }
        });
        jToolBar1.add(flipSticksCheckbox);

        mappingButton.setText("mapping");
        mappingButton.setToolTipText("map controller buttons");
        mappingButton.setFocusable(false);
        mappingButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        mappingButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mappingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mappingButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(mappingButton);
        jToolBar1.add(jSeparator3);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 384, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jToolBar1.add(jPanel1);
        jToolBar1.add(jSeparator7);

        instrumentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/codeminders/controltower/images/instruments.gif"))); // NOI18N
        instrumentButton.setText("instruments");
        instrumentButton.setToolTipText("toggle instruments");
        instrumentButton.setFocusable(false);
        instrumentButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        instrumentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                instrumentButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(instrumentButton);

        configureButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/codeminders/controltower/images/objects_039.gif"))); // NOI18N
        configureButton.setText("tuning");
        configureButton.setToolTipText("show drone tuning settings");
        configureButton.setFocusable(false);
        configureButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        configureButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        configureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(configureButton);
        jToolBar1.add(jSeparator6);

        videoPanel.setBackground(new java.awt.Color(102, 102, 102));
        videoPanel.setPreferredSize(new java.awt.Dimension(320, 240));
        videoPanel.setLayout(new javax.swing.BoxLayout(videoPanel, javax.swing.BoxLayout.LINE_AXIS));

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
            .addComponent(videoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(videoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void configureButtonActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_configureButtonActionPerformed
        droneConfigWindow.setLocationRelativeTo(this);
        droneConfigWindow.setVisible(true);
    }//GEN-LAST:event_configureButtonActionPerformed

    private void droneStatusMouseReleased(java.awt.event.MouseEvent evt)
    {//GEN-FIRST:event_droneStatusMouseReleased
        startUpdateLoop();
    }//GEN-LAST:event_droneStatusMouseReleased

    private void controllerStatusMouseReleased(java.awt.event.MouseEvent evt)
    {//GEN-FIRST:event_controllerStatusMouseReleased
        initController();
    }//GEN-LAST:event_controllerStatusMouseReleased

    private void instrumentButtonActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_instrumentButtonActionPerformed
        Dimension dim = jPanel2.getSize();
        if(dim.getHeight() > 0)
        {
            dim.setSize(dim.getWidth(), 0);
            jPanel2.setPreferredSize(dim);
            jPanel2.setSize(dim);
            jPanel2.setVisible(false);
        }
        else
        {
            dim.setSize(dim.getWidth(), 210);
            jPanel2.setPreferredSize(dim);
            jPanel2.setSize(dim);
            jPanel2.setVisible(true);
        }
    }//GEN-LAST:event_instrumentButtonActionPerformed

    private void mappingButtonActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_mappingButtonActionPerformed
        if (keyboardControllerStatus.getForeground() != Color.GREEN) {
            controlConfigWindow.setLocationRelativeTo(this);
            controlConfigWindow.setVisible(true);
        } else {
            keyboardControlConfigWindow.setLocationRelativeTo(this);
            keyboardControlConfigWindow.setVisible(true);
        }
    }//GEN-LAST:event_mappingButtonActionPerformed

    private void flipSticksCheckboxActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_flipSticksCheckboxActionPerformed
        flipSticks.set(flipSticksCheckbox.isSelected());
        prefs.putBoolean("FLIP_STICKS", flipSticks.get());
    }//GEN-LAST:event_flipSticksCheckboxActionPerformed

    private void keyboardControllerStatusMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_keyboardControllerStatusMouseReleased
        initController();
    }//GEN-LAST:event_keyboardControllerStatusMouseReleased

    private void joystickControllerStatusMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_joystickControllerStatusMouseReleased
        initController();
    }//GEN-LAST:event_joystickControllerStatusMouseReleased

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        if (!SLF4JBridgeHandler.isInstalled()) {
            SLF4JBridgeHandler.install();
        }
        
        final ControlTower tower = new ControlTower();
        java.awt.EventQueue.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {
                tower.setLocationRelativeTo(null);
                tower.setVisible(true);
            }
        });
        tower.startUpdateLoop();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel batteryStatus;
    private javax.swing.JButton configureButton;
    private javax.swing.JLabel droneStatus;
    private javax.swing.JCheckBox flipSticksCheckbox;
    private javax.swing.JButton instrumentButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel joystickControllerStatus;
    private javax.swing.JLabel keyboardControllerStatus;
    private javax.swing.JButton mappingButton;
    private javax.swing.JPanel videoPanel;
    // End of variables declaration//GEN-END:variables
}
