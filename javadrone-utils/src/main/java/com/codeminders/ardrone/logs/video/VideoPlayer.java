package com.codeminders.ardrone.logs.video;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;

import javax.swing.JFrame;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.DroneVideoListener;
import com.codeminders.ardrone.VideoDataDecoder;
import com.codeminders.ardrone.decoder.FileDataReaderAndDecoder;

public class VideoPlayer implements Runnable, DroneVideoListener {

    private PlayerFrame displayPanel;
    private String fileName;
    public Image lastFrame;
    ARDrone drone;
    
    public VideoPlayer(String[] args) {
        if(args.length < 1) {
            return;
        } else {

            try {
                drone = new ARDrone();
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
                System.exit(-1);
            }
            drone.addImageListener(this);
            
            JFrame frame = new JFrame("Player");
            displayPanel = new PlayerFrame();

            frame.getContentPane().add(displayPanel, BorderLayout.CENTER);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            displayPanel.setVisible(true);
            frame.pack();
            frame.setVisible(true);
            frame.setSize(new Dimension(645, 380));
            
            fileName = args[0];

            new Thread(this).start();

        } 
    }

    public static void main(String[] args) {
        new VideoPlayer(args);
    }

    @Override
    public void run() {
        
        //TODO: play decoded content;

//        try {
//            File in = new File(fileName);
//
//            VideoDataDecoder video_decoder = new VideoDataDecoder(drone, 100 * 1024);
//
//            FileDataReaderAndDecoder video_reader = new FileDataReaderAndDecoder(in, video_decoder);
//
//            video_decoder.start();
//
//            Thread video_reader_thread = new Thread(video_reader);
//            video_reader_thread.setName("Video Reader");
//            video_reader_thread.start();
//            
//            video_reader_thread.join();
//
//            video_decoder.finish();

//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void frameReceived(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        image.setRGB(startX, startY, w, h, rgbArray, offset, scansize);
        displayPanel.lastFrame = image;
        displayPanel.invalidate();
        displayPanel.updateUI();        
    }
}
