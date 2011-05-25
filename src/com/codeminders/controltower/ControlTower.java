/*
 * ControlTower.java
 *
 * Created on 17.05.2011, 13:41:27
 */
package com.codeminders.controltower;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.ARDrone.VideoChannel;
import com.codeminders.ardrone.DroneStatusChangeListener;
import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavDataListener;
import com.codeminders.ardroneui.controllers.AfterGlowController;
import com.codeminders.ardroneui.controllers.KeyboardController;
import com.codeminders.ardroneui.controllers.PS3Controller;
import com.codeminders.ardroneui.controllers.PS3ControllerState;
import com.codeminders.ardroneui.controllers.PS3ControllerStateChange;
import com.codeminders.ardroneui.controllers.SonyPS3Controller;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDDeviceNotFoundException;
import com.codeminders.hidapi.HIDManager;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.*;
import javax.swing.ImageIcon;

/**
 *
 * @author normenhansen
 */
@SuppressWarnings("serial")
public class ControlTower extends javax.swing.JFrame implements DroneStatusChangeListener, NavDataListener {

    private static final long READ_UPDATE_DELAY_MS = 5L;
    private static final long CONNECT_TIMEOUT = 8000L;
    private static float CONTROL_THRESHOLD = 0.5f;
    private int video_index = 0;
    private static final VideoChannel[] VIDEO_CYCLE = {VideoChannel.HORIZONTAL_ONLY,
        VideoChannel.VERTICAL_ONLY, VideoChannel.VERTICAL_IN_HORIZONTAL, VideoChannel.HORIZONTAL_IN_VERTICAL};
    private ImageIcon droneOn = new ImageIcon(getClass().getResource("/com/codeminders/controltower/images/drone_on.gif"));
    private ImageIcon droneOff = new ImageIcon(getClass().getResource("/com/codeminders/controltower/images/drone_off.gif"));
    private ImageIcon controllerOn = new ImageIcon(getClass().getResource("/com/codeminders/controltower/images/controller_on.png"));
    private ImageIcon controllerOff = new ImageIcon(getClass().getResource("/com/codeminders/controltower/images/controller_off.png"));
    private AtomicBoolean running = new AtomicBoolean(false);
    private AtomicBoolean flying = new AtomicBoolean(false);
    private ARDrone drone;
    private AtomicReference<PS3Controller> dev = new AtomicReference<PS3Controller>();
    private VideoPanel video = new VideoPanel();
    private DroneConfig configWindow;
    private BottomGaugePanel gauges = new BottomGaugePanel();

    static {
        System.loadLibrary("hidapi-jni");
    }

    /** Creates new form ControlTower */
    public ControlTower() {
        setupLog();
        setAlwaysOnTop(true);
        initComponents();
        configWindow = new DroneConfig(this, true);
        videoPanel.add(video);
        jPanel2.add(gauges);
        initController();
        initDrone();
    }

    private void initDrone() {
        try {
            drone = new ARDrone();
        } catch (UnknownHostException ex) {
            Logger.getLogger(ControlTower.class.getName()).log(Level.SEVERE, "Error creating drone object!", ex);
            return;
        }
        configWindow.setDrone(drone);
        gauges.setDrone(drone);
        video.setDrone(drone);
        drone.addStatusChangeListener(this);
        drone.addNavDataListener(this);
    }

    private void updateDroneStatus(final boolean available) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (!available) {
                    droneStatus.setForeground(Color.RED);
                    droneStatus.setIcon(droneOff);
                } else {
                    droneStatus.setForeground(Color.GREEN);
                    droneStatus.setIcon(droneOn);
                }
            }
        });

    }

    private void updateControllerStatus(final boolean available) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (!available) {
                    controllerStatus.setForeground(Color.RED);
                    controllerStatus.setIcon(controllerOff);
                } else {
                    controllerStatus.setForeground(Color.GREEN);
                    controllerStatus.setIcon(controllerOn);
                }
            }
        });

    }

    private void updateBatteryStatus(final int value) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                batteryStatus.setText(value + "%");
                if (value < 15) {
                    batteryStatus.setForeground(Color.RED);
                } else if (value < 50) {
                    batteryStatus.setForeground(Color.ORANGE);
                } else {
                    batteryStatus.setForeground(Color.GREEN);
                }
            }
        });
    }

    private void resetStatus() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                droneStatus.setForeground(Color.RED);
                droneStatus.setIcon(droneOff);
                batteryStatus.setForeground(Color.RED);
                batteryStatus.setText("0%");
            }
        });

    }

    @Override
    public void ready() {
        updateDroneStatus(true);
    }

    @Override
    public void navDataReceived(NavData nd) {
        updateBatteryStatus(nd.getBattery());
        this.flying.set(nd.isFlying());
    }

    private void cycleVideoChannel(ARDrone drone) throws IOException {
        if (++video_index == VIDEO_CYCLE.length) {
            video_index = 0;
        }
        drone.selectVideoChannel(VIDEO_CYCLE[video_index]);
    }

    private void initController() {
        PS3Controller current = dev.get();
        if (current != null) {
            try {
                current.close();
            } catch (IOException ex) {
                Logger.getLogger(ControlTower.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            dev.set(findController());
        } catch (IOException ex) {
            Logger.getLogger(ControlTower.class.getName()).log(Level.SEVERE, "{0}", ex);
        }
        if (dev.get() == null) {
            System.err.println("No suitable controller found! Using keyboard");
            dev.set(new KeyboardController(this));
            updateControllerStatus(false);
        } else {
            System.err.println("Gamepad controller found");
            updateControllerStatus(true);
        }
    }

    private static PS3Controller findController() throws IOException {
        HIDDeviceInfo[] devs = HIDManager.listDevices();
        for (int i = 0; i < devs.length; i++) {
            if (AfterGlowController.isA(devs[i])) {
                return new AfterGlowController(devs[i]);
            }
            if (SonyPS3Controller.isA(devs[i])) {
                return new SonyPS3Controller(devs[i]);
            }
        }
        return null;
    }

    public void updateLoop() {
        if (running.get()) {
            return;
        }
        running.set(true);
        resetStatus();
        try {

            drone.addStatusChangeListener(new DroneStatusChangeListener() {

                @Override
                public void ready() {
                    try {
                        System.err.println("Configure");
                        configWindow.updateDrone();
                        drone.selectVideoChannel(VideoChannel.HORIZONTAL_ONLY);
                        drone.setCombinedYawMode(true);
                        drone.trim();
                    } catch (IOException e) {
                        drone.changeToErrorState(e);
                    }
                }
            });

            System.err.println("Connecting to the drone");
            drone.connect();
            drone.waitForReady(CONNECT_TIMEOUT);
            System.err.println("Connected to the drone");
            try {
                try {
                    PS3ControllerState oldpad = null;
                    while (running.get()) {
                        PS3ControllerState pad = dev.get().read();
                        PS3ControllerStateChange pad_change = new PS3ControllerStateChange(oldpad, pad);
                        oldpad = pad;

                        if (pad_change.isStartChanged() && pad_change.isStart()) {
                            System.err.println("Taking off");
                            drone.takeOff();
                        }
                        if (pad_change.isSelectChanged() && pad_change.isSelect()) {
                            System.err.println("Landing");
                            drone.land();
                        }
                        if (pad_change.isPSChanged() && pad_change.isPS()) {
                            System.err.println("Reseting");

                            drone.clearEmergencySignal();
                            drone.trim();
                        }
                        if (pad_change.isTriangleChanged() && pad_change.isTriangle()) {
                            System.err.println("Video cycle");
                            cycleVideoChannel(drone);
                        }
                        if (pad_change.isCrossChanged() && pad_change.isCross()) {
                            drone.playAnimation(8, 500);
                            drone.playLED(ARDrone.LED.FIRE, 10, 1);
                        }
                        if (pad_change.isSquareChanged() && pad_change.isSquare()) {
                            drone.playAnimation(5, 500);
                        }
                        if (pad_change.isCircleChanged() && pad_change.isCircle()) {
                            drone.playAnimation(4, 500);
                        }
                        if (pad_change.isL1Changed() && pad_change.isL1()) {
                            drone.playLED(1, 10, 2);
                        }
                        if (pad_change.isR1Changed() && pad_change.isR1()) {
                            drone.playLED(2, 10, 2);
                        }
                        if (pad_change.isL2Changed() && pad_change.isL2()) {
                            drone.playLED(3, 10, 2);
                        }
                        if (pad_change.isR2Changed() && pad_change.isR2()) {
                            drone.playLED(4, 10, 2);
                        }
                        if (flying.get()) {
                            // Detecting if we need to move the drone

                            int leftX = pad.getLeftJoystickX();
                            int leftY = pad.getLeftJoystickY();

                            int rightX = pad.getRightJoystickX();
                            int rightY = pad.getRightJoystickY();

                            float left_right_tilt = 0f;
                            float front_back_tilt = 0f;
                            float vertical_speed = 0f;
                            float angular_speed = 0f;

                            if (Math.abs(((float) leftX) / 128f) > CONTROL_THRESHOLD) {
                                left_right_tilt = ((float) leftX) / 128f;
                            }

                            if (Math.abs(((float) leftY) / 128f) > CONTROL_THRESHOLD) {
                                front_back_tilt = ((float) leftY) / 128f;
                            }

                            if (Math.abs(((float) rightX) / 128f) > CONTROL_THRESHOLD) {
                                angular_speed = ((float) rightX) / 128f;
                            }

                            if (Math.abs(-1 * ((float) rightY) / 128f) > CONTROL_THRESHOLD) {
                                vertical_speed = -1 * ((float) rightY) / 128f;
                            }

                            if (left_right_tilt != 0 || front_back_tilt != 0 || vertical_speed != 0 || angular_speed != 0) {
                                drone.move(left_right_tilt, front_back_tilt, vertical_speed, angular_speed);
                            } else {
                                drone.hover();
                            }
                        }

                        try {
                            Thread.sleep(READ_UPDATE_DELAY_MS);
                        } catch (InterruptedException e) {
                            // Ignore
                        }
                    }
                } finally {
                }
            } finally {
                drone.disconnect();
            }
        } catch (HIDDeviceNotFoundException hex) {
            hex.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        resetStatus();
        running.set(false);
    }

    private void startUpdateLoop() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                updateLoop();
            }
        });
        thread.setName("ARDrone Control Loop");
        thread.start();
    }

    /** This method is called from within the constructor to
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
        controllerStatus = new javax.swing.JLabel();
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
        droneStatus.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                droneStatusMouseReleased(evt);
            }
        });
        jToolBar1.add(droneStatus);

        batteryStatus.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        batteryStatus.setForeground(java.awt.Color.red);
        batteryStatus.setText("0%");
        jToolBar1.add(batteryStatus);
        jToolBar1.add(jSeparator1);

        controllerStatus.setForeground(java.awt.Color.red);
        controllerStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/codeminders/controltower/images/controller_off.png"))); // NOI18N
        controllerStatus.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                controllerStatusMouseReleased(evt);
            }
        });
        jToolBar1.add(controllerStatus);
        jToolBar1.add(jSeparator3);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 522, Short.MAX_VALUE)
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
        configureButton.setToolTipText("show tuning settings");
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

    private void configureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureButtonActionPerformed
        configWindow.setVisible(true);
    }//GEN-LAST:event_configureButtonActionPerformed

    private void droneStatusMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_droneStatusMouseReleased
        startUpdateLoop();
    }//GEN-LAST:event_droneStatusMouseReleased

    private void controllerStatusMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_controllerStatusMouseReleased
        initController();
    }//GEN-LAST:event_controllerStatusMouseReleased

    private void instrumentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_instrumentButtonActionPerformed
        Dimension dim = jPanel2.getSize();
        if (dim.getHeight() > 0) {
            dim.setSize(dim.getWidth(), 0);
            jPanel2.setPreferredSize(dim);
            jPanel2.setSize(dim);
            jPanel2.setVisible(false);
        } else {
            dim.setSize(dim.getWidth(), 210);
            jPanel2.setPreferredSize(dim);
            jPanel2.setSize(dim);
            jPanel2.setVisible(true);
        }
    }//GEN-LAST:event_instrumentButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        final ControlTower tower = new ControlTower();
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                tower.setLocationRelativeTo(null);
                tower.setVisible(true);
            }
        });
        tower.startUpdateLoop();
    }

    public void setControlThreshold(float sens) {
        CONTROL_THRESHOLD = sens;
    }

    private static void setupLog() {
        Logger topLogger = java.util.logging.Logger.getLogger("");
        Handler consoleHandler = null;
        for (Handler handler : topLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                consoleHandler = handler;
                break;
            }
        }

        if (consoleHandler == null) {
            consoleHandler = new ConsoleHandler();
            topLogger.addHandler(consoleHandler);
        }
        topLogger.setLevel(Level.ALL);
        consoleHandler.setLevel(java.util.logging.Level.ALL);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel batteryStatus;
    private javax.swing.JButton configureButton;
    private javax.swing.JLabel controllerStatus;
    private javax.swing.JLabel droneStatus;
    private javax.swing.JButton instrumentButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel videoPanel;
    // End of variables declaration//GEN-END:variables
}
