
package com.codeminders.ardrone.util;

import java.awt.image.BufferedImage;

import com.codeminders.ardrone.DroneVideoListener;

public class FileImageRecorder implements DroneVideoListener
{
    private String                   base_path;
    private int                      starting_seq;
    private String                   prefix;
    private boolean                  activated;
    private RecordingSuccessCallback callback;

    /**
     * Creates image recorder. It will record files in given directory with
     * names like IMAGE-0010.png
     * 
     * @param base_path - directory where image files will be stored
     * @param starting_seq - starting sequence number
     * @param prefix - filename prefix
     * @param callback - callback object which will be notified on each
     *            success/failure. Could be null.
     */
    public FileImageRecorder(String base_path, int starting_seq, String prefix, RecordingSuccessCallback callback)
    {
        this.base_path = base_path;
        this.starting_seq = starting_seq;
        this.prefix = prefix;
        this.callback = callback;

        this.activated = false;
    }

    /**
     * Active recorder. Next image will be saved, after which it will be
     * automatically deactivated.
     */
    public synchronized void activate()
    {
        activated = true;
    }

    /**
     * Active recorder.
     */
    public synchronized void deActivate()
    {
        activated = false;
    }

    @Override
    public void frameReceived(BufferedImage image)
    {
        // TODO Auto-generated method stub

    }

}
