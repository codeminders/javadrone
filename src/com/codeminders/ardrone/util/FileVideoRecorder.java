
package com.codeminders.ardrone.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import com.codeminders.ardrone.DroneVideoListener;

public class FileVideoRecorder implements DroneVideoListener
{

    private ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
    private boolean                  recording;
    private MJPEGGenerator           generator;

    public void startRecording()
    {
        if(!recording)
        {
            recording = true;
        }
    }

    public void stopRecording()
    {
        if(recording)
        {
            recording = false;
        }
    }

    public void clear()
    {
        stopRecording();
        images.clear();
    }

    public void saveVideo(String fileName)
    {
        stopRecording();
        int numFrames = images.size();

        if(numFrames == 0)
        {
            System.err.println("No frames recorded");
            return;
        }

        double framerate = 60;
        try
        {
            generator = new MJPEGGenerator(new File(fileName), images.get(0).getWidth(), images.get(0).getHeight(),
                    framerate, numFrames);
            for(BufferedImage image : images)
            {
                generator.addImage(image);
            }
            generator.finishAVI();
            images.clear();
        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void frameReceived(BufferedImage image)
    {
        if(recording)
        {
            images.add(image);
        }
    }

}
