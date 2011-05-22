/*
 * VideoPanel.java
 *
 * Created on 21.05.2011, 18:42:10
 */
package com.codeminders.controltower;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.DroneVideoListener;
import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavDataListener;
import eu.hansolo.steelseries.tools.ColorDef;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author normenhansen
 */
public class VideoPanel extends javax.swing.JPanel implements NavDataListener, DroneVideoListener {

    private ARDrone drone;
    private AtomicBoolean drawWarnings = new AtomicBoolean(true);
    private AtomicBoolean drawInstruments = new AtomicBoolean(false);
    private AtomicReference<BufferedImage> image = new AtomicReference<BufferedImage>();
    private AtomicReference<NavData> currentData = new AtomicReference<NavData>(new NavData());
    private BufferedImage batteryImage;
//    private BufferedImage sensorImage;
    private BufferedImage commImage;
    private BufferedImage windImage;
    private BufferedImage emergencyImage;
    private Color green = new Color(0, .7f, 0);
    private Color greenLight = new Color(.3f, 1f, .3f);
    private Color greenDark = new Color(0, .4f, 0);

    /** Creates new form VideoPanel */
    public VideoPanel() {
        initComponents();
        try {
            batteryImage = ImageIO.read(getClass().getResourceAsStream("/com/codeminders/controltower/images/battery_warning.png"));
//            sensorImage = ImageIO.read(getClass().getResourceAsStream("/com/codeminders/controltower/images/ultrasound_warning.png"));
            commImage = ImageIO.read(getClass().getResourceAsStream("/com/codeminders/controltower/images/comm_warning.png"));
            windImage = ImageIO.read(getClass().getResourceAsStream("/com/codeminders/controltower/images/wind_warning.png"));
            emergencyImage = ImageIO.read(getClass().getResourceAsStream("/com/codeminders/controltower/images/warning.png"));
        } catch (IOException ex) {
            Logger.getLogger(ControlTower.class.getName()).log(Level.SEVERE, "{0}", ex);
        }
    }

    public void setDrone(ARDrone drone){
        this.drone = drone;
        drone.addImageListener(this);
        drone.addNavDataListener(this);
    }
    
    public void setDrawInstruments(final boolean draw) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                horizon1.setVisible(draw);
                radialBargraph1.setVisible(draw);
                airCompass1.setVisible(draw);
                altimeter1.setVisible(draw);
            }
        });
    }

    public void setDrawWarnings(boolean draw) {
        drawWarnings.set(draw);
    }

    @Override
    public void navDataReceived(NavData nd) {
        currentData.set(nd);
        updateGauges(nd);
    }

    @Override
    public void frameReceived(BufferedImage im) {
        image.set(im);
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        NavData data = currentData.get();
        int width = getWidth();
        int height = getHeight();
        int imageWidth = width / 10;
        int imageHeight = height / 10;
        drawDroneImage(g2d, width, height);
        drawWarnings(g2d, width, height, data, imageWidth, imageHeight);
        drawInstruments(g2d, height, width, data);
    }

    private void drawDroneImage(Graphics2D g2d, int width, int height) {
        BufferedImage im = image.get();
        if (im != null) {
            g2d.drawImage(im, 0, 0, width, height, null);
        }
    }

    private void drawWarnings(Graphics2D g2d, int width, int height, NavData data, int imageWidth, int imageHeight) {
        if (!drawWarnings.get()) {
            return;
        }
        if (data.isBatteryTooLow()) {
            g2d.drawImage(batteryImage, 0, 0, imageWidth, imageHeight, null);
        }
        if (data.isCommunicationProblemOccurred()) {
            g2d.drawImage(commImage, 0, height - imageHeight, imageWidth, imageHeight, null);
        }
        if (data.isTooMuchWind()) {
            g2d.drawImage(windImage, width - imageWidth, height - imageHeight, imageWidth, imageHeight, null);
        }
        if (data.isEmergency()) {
            g2d.drawImage(emergencyImage, width - imageWidth, 0, imageWidth, imageHeight, null);
        }
    }

    private void drawInstruments(Graphics2D g2d, int height, int width, NavData data) {
        if (!drawInstruments.get()) {
            return;
        }

        int instSize = height / 5;
        int instRadius = instSize / 2;
        int centerX = width / 2;
        int centerY = height - instRadius;
        int lineHeight = instSize / 10;
        int fontSize = instSize / 5;

        BasicStroke thick = new BasicStroke(instSize / 10);
        BasicStroke thin = new BasicStroke(instSize / 15);

        // Horizon
        Ellipse2D.Float circle = new Ellipse2D.Float(centerX - instRadius, height - instSize, instSize, instSize);
        g2d.setClip(circle);
        int pitchYA = (int) (Math.cos(Math.toRadians(data.getPitch() - 90)) * instRadius);
        int yawXA = (int) (Math.sin(Math.toRadians(data.getYaw())) * instRadius);
        int rollYA = (int) (Math.cos(Math.toRadians(data.getRoll() - 90)) * instRadius);
        int rollYB = (int) (Math.cos(Math.toRadians(data.getRoll() - 90 + 180)) * instRadius);
//        int rollXA = (int) (Math.sin(Math.toRadians(data.getRoll() - 90)) * instRadius);
//        int rollXB = (int) (Math.sin(Math.toRadians(data.getRoll() - 90 + 180)) * instRadius);
//        g2d.setColor(green);
//        g2d.setStroke(thin);
//        g2d.drawLine(centerX + rollXA, centerY + rollYA + pitchYA, centerX + rollXB, centerY + rollYB + pitchYA);
        Polygon sky = new Polygon();
        Polygon earth = new Polygon();
        sky.addPoint(centerX - instRadius, centerY + rollYA + pitchYA);
        sky.addPoint(centerX + instRadius, centerY + rollYB + pitchYA);
        sky.addPoint(centerX + instRadius, height-instSize);
        sky.addPoint(centerX - instRadius, height-instSize);
        earth.addPoint(centerX - instRadius, centerY + rollYA + pitchYA);
        earth.addPoint(centerX + instRadius, centerY + rollYB + pitchYA);
        earth.addPoint(centerX + instRadius, height);
        earth.addPoint(centerX - instRadius, height);
        g2d.setColor(Color.BLUE);
        g2d.fill(sky);
        g2d.setColor(Color.ORANGE);
        g2d.fill(earth);
        
        // Main Instrument Yaw
        g2d.setStroke(thick);
        if (data.getYaw() > 90 || data.getYaw() < -90) {
            g2d.setColor(greenDark);
        } else {
            g2d.setColor(greenLight);
        }
        g2d.drawLine(centerX - yawXA, centerY, centerX - yawXA, centerY + lineHeight);
        g2d.drawLine(centerX - yawXA, centerY, centerX - yawXA, centerY - lineHeight);
        // Main Instrument Circle
        g2d.setClip(null);
        g2d.setColor(green);
        g2d.setStroke(thin);
        g2d.drawOval(centerX - instRadius, height - instSize, instSize, instSize);

        // Height Display
        int heightYA = (int) (instSize * (data.getAltitude() / 5));
        if (data.isUltrasonicSensorDeaf()) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(greenLight);
        }
        g2d.setStroke(thick);
        g2d.setFont(g2d.getFont().deriveFont(0, fontSize));
        g2d.drawString("0", centerX - instSize, height);
        g2d.drawString("2.5", centerX - instSize, height - (instSize / 2));
        g2d.drawString("5", centerX - instSize, height - (instSize));
        if (data.isUltrasonicSensorDeaf()) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(green);
        }
        g2d.drawLine(centerX - instRadius - (lineHeight * 2), height - heightYA, centerX - instRadius - lineHeight, height - heightYA);

        // Battery
        g2d.setColor(greenLight);
        if (data.getBattery() < 15) {
            g2d.setColor(Color.RED);
        } else if (data.getBattery() < 50) {
            g2d.setColor(Color.ORANGE);
        }
        g2d.drawString(data.getBattery() + "%", centerX + instRadius, height - instSize);

        // Navigation Data
        g2d.setColor(greenLight);
        g2d.drawString(data.getLatitude() + "/" + data.getLongitude() + "/" + data.getHeading(), centerX + instRadius, height);
    }

    private void updateGauges(final NavData data) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                horizon1.setPitch(data.getPitch()*-1);
                horizon1.setRoll(data.getRoll());
                altimeter1.setValue(data.getAltitude()*1000);
                airCompass1.setValue(data.getYaw());
                radialBargraph1.setValue(data.getBattery());
                if(data.getBattery()<15){
                    radialBargraph1.setBarGraphColor(ColorDef.RED);
                    radialBargraph1.setLedBlinking(true);
                }else if(data.getBattery()<50){
                    radialBargraph1.setBarGraphColor(ColorDef.ORANGE);
                    radialBargraph1.setLedBlinking(false);
                }else{
                    radialBargraph1.setBarGraphColor(ColorDef.GREEN);
                    radialBargraph1.setLedBlinking(false);
                }
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        radialBargraph1 = new eu.hansolo.steelseries.gauges.RadialBargraph();
        jPanel21 = new javax.swing.JPanel();
        horizon1 = new eu.hansolo.steelseries.extras.Horizon();
        jPanel22 = new javax.swing.JPanel();
        airCompass1 = new eu.hansolo.steelseries.extras.AirCompass();
        jPanel23 = new javax.swing.JPanel();
        altimeter1 = new eu.hansolo.steelseries.extras.Altimeter();
        jPanel24 = new javax.swing.JPanel();

        setLayout(new java.awt.GridLayout(4, 6));

        jPanel1.setOpaque(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel1);

        jPanel2.setOpaque(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel2);

        jPanel3.setOpaque(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel3);

        jPanel4.setOpaque(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel4);

        jPanel5.setOpaque(false);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel5);

        jPanel6.setOpaque(false);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel6);

        jPanel7.setOpaque(false);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel7);

        jPanel8.setOpaque(false);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel8);

        jPanel9.setOpaque(false);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel9);

        jPanel10.setOpaque(false);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel10);

        jPanel11.setOpaque(false);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel11);

        jPanel12.setOpaque(false);

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel12);

        jPanel13.setOpaque(false);

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel13);

        jPanel14.setOpaque(false);

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel14);

        jPanel15.setOpaque(false);

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel15);

        jPanel16.setOpaque(false);

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel16);

        jPanel17.setOpaque(false);

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel17);

        jPanel18.setOpaque(false);

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel18);

        jPanel19.setOpaque(false);

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel19);

        jPanel20.setOpaque(false);

        radialBargraph1.setFrame3dEffectVisible(true);

        javax.swing.GroupLayout radialBargraph1Layout = new javax.swing.GroupLayout(radialBargraph1);
        radialBargraph1.setLayout(radialBargraph1Layout);
        radialBargraph1Layout.setHorizontalGroup(
            radialBargraph1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        radialBargraph1Layout.setVerticalGroup(
            radialBargraph1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(radialBargraph1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, Short.MAX_VALUE)
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(radialBargraph1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, Short.MAX_VALUE)
        );

        add(jPanel20);

        jPanel21.setOpaque(false);

        horizon1.setFrame3dEffectVisible(true);

        javax.swing.GroupLayout horizon1Layout = new javax.swing.GroupLayout(horizon1);
        horizon1.setLayout(horizon1Layout);
        horizon1Layout.setHorizontalGroup(
            horizon1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        horizon1Layout.setVerticalGroup(
            horizon1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(horizon1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, Short.MAX_VALUE)
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(horizon1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, Short.MAX_VALUE)
        );

        add(jPanel21);

        jPanel22.setOpaque(false);

        airCompass1.setFrame3dEffectVisible(true);

        javax.swing.GroupLayout airCompass1Layout = new javax.swing.GroupLayout(airCompass1);
        airCompass1.setLayout(airCompass1Layout);
        airCompass1Layout.setHorizontalGroup(
            airCompass1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        airCompass1Layout.setVerticalGroup(
            airCompass1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(airCompass1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, Short.MAX_VALUE)
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(airCompass1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, Short.MAX_VALUE)
        );

        add(jPanel22);

        jPanel23.setOpaque(false);

        altimeter1.setFrame3dEffectVisible(true);

        javax.swing.GroupLayout altimeter1Layout = new javax.swing.GroupLayout(altimeter1);
        altimeter1.setLayout(altimeter1Layout);
        altimeter1Layout.setHorizontalGroup(
            altimeter1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        altimeter1Layout.setVerticalGroup(
            altimeter1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(altimeter1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, Short.MAX_VALUE)
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(altimeter1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, Short.MAX_VALUE)
        );

        add(jPanel23);

        jPanel24.setOpaque(false);

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 59, Short.MAX_VALUE)
        );

        add(jPanel24);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private eu.hansolo.steelseries.extras.AirCompass airCompass1;
    private eu.hansolo.steelseries.extras.Altimeter altimeter1;
    private eu.hansolo.steelseries.extras.Horizon horizon1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private eu.hansolo.steelseries.gauges.RadialBargraph radialBargraph1;
    // End of variables declaration//GEN-END:variables
}
