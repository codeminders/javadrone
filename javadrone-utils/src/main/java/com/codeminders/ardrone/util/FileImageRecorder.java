
package com.codeminders.ardrone.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

public class FileImageRecorder extends ImageVideoRecorderBase
{
    private static final int    MAX_SAVING_THREADS = 4;
    private static final String EXT                = ".png";

    private boolean             activated;
    private ExecutorService     executor;

    public String getExtension()
    {
        return EXT;
    }

    private class ImageSaver implements Runnable
    {
        private BufferedImage     image;
        private FileImageRecorder recorder;

        public ImageSaver(BufferedImage image, FileImageRecorder recorder)
        {
            this.image = image;
            this.recorder = recorder;
        }

        @Override
        public void run()
        {
            recorder.record(image);
        }
    }

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
    public FileImageRecorder(File base_path, int starting_seq, String prefix, RecordingSuccessCallback callback)
    {
        super(base_path, starting_seq, prefix, callback);

        this.activated = false;
        executor = Executors.newFixedThreadPool(MAX_SAVING_THREADS);
    }

    void record(BufferedImage image)
    {
        File f;
        try
        {
            f = openFile();
        } catch(IOException e)
        {
            callback.recordingError(null, "error opening file", e);
            return;
        }
        try
        {
            ImageIO.write(image, "png", f);
        } catch(IOException e)
        {
            callback.recordingError(f.getPath(), "error writing file", e);
            f.delete();
            return;
        }

        callback.recordingSuccess(f.getPath());
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
    public synchronized void imageReceived(BufferedImage image)
    {
        if(!activated)
            return;

        executor.execute(new ImageSaver(image, this));
        activated = false;
    }
}
