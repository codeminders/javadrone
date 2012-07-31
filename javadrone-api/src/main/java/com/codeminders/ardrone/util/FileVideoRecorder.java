
package com.codeminders.ardrone.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Video recorder. It records single video file. You can start/stop recording
 * many times.
 * 
 * @author lord
 * 
 */
public class FileVideoRecorder extends ImageVideoRecorderBase implements Runnable
{
    private static final String  EXT          = ".avi";

    private Queue<BufferedImage> frames_queue = new LinkedList<BufferedImage>();
    private boolean              recording;
    private boolean              done;
    private MJPEGGenerator       generator;

    double                       frame_rate;

    private int                  frame_width;
    private int                  frame_height;
    File                         current_file;

    public FileVideoRecorder(File base_path, int starting_seq, String prefix, RecordingSuccessCallback callback,
            double frame_rate)
    {
        super(base_path, starting_seq, prefix, callback);

        this.frame_rate = frame_rate;

        this.recording = false;
        this.done = false;
        this.generator = null;

        Thread thr = new Thread(this);
        thr.start();
    }

    public synchronized void startRecording()
    {
        recording = true;
        notify();
    }

    public synchronized void pauseRecording()
    {
        recording = false;
        notify();
    }

    public synchronized void finishRecording()
    {
        recording = false;
        done = true;
        notify();
    }

    @Override
    public synchronized void imageReceived(BufferedImage image)
    {
        if(recording)
        {
            frames_queue.add(image);
            notify();
        }
    }

    @Override
    public synchronized void run()
    {
        while(true)
        {
            try
            {
                wait();
            } catch(InterruptedException e)
            {
                // Ignore
            }

            if(done)
            {
                if(generator != null)
                {
                    try
                    {
                        generator.finishAVI();
                        callback.recordingSuccess(current_file.getAbsolutePath());
                    } catch(Exception e)
                    {
                        callback.recordingError(current_file.getAbsolutePath(), "Error closing stream", e);
                    }
                } else
                {
                    callback.recordingError(null, "Recording have not started yet", null);
                }
                return;
            }

            BufferedImage frame = frames_queue.poll();
            if(frame == null)
                continue;

            if(generator == null)
            {
                // Lazy init. Using first frame size as default
                frame_width = frame.getWidth();
                frame_height = frame.getHeight();
                try
                {
                    current_file = openFile();
                } catch(IOException e1)
                {
                    callback.recordingError(null, "Error opening file", e1);
                    return;
                }
                try
                {
                    generator = new MJPEGGenerator(current_file, frame_width, frame_height, frame_rate, 0);
                } catch(Exception e)
                {
                    callback.recordingError(current_file.getAbsolutePath(), "Error video stream", e);
                    return;
                }
            }

            try
            {
                if(frame.getWidth() != frame_width || frame.getHeight() != frame_height)
                {
                    // Needs to be resized
                    Image i = frame.getScaledInstance(frame_width, frame_height, Image.SCALE_FAST);
                    generator.addImage(i);
                } else
                {
                    generator.addImage(frame);
                }
            } catch(Exception e)
            {
                callback.recordingError(current_file.getAbsolutePath(), "Error adding frame", e);
                return;
            }
        }
    }

    public String getExtension()
    {
        return EXT;
    }

}
