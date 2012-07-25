
package com.codeminders.ardrone.util;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;


public abstract class ImageVideoRecorderBase extends BufferedImageVideoListener
{
    private DecimalFormat              format;

    protected File                     base_path;
    protected int                      seq;
    protected String                   prefix;
    protected RecordingSuccessCallback callback;

    public ImageVideoRecorderBase(File base_path, int starting_seq, String prefix, RecordingSuccessCallback callback)
    {
        this.base_path = base_path;
        this.seq = starting_seq;
        this.prefix = prefix;
        this.callback = callback;

        format = new java.text.DecimalFormat("0000");
    }

    public abstract String getExtension();

    protected File openFile() throws IOException
    {
        while(seq < 9999)
        {
            String fname = generateFileName();
            File f = new File(base_path, fname);
            if(f.createNewFile())
                return f;
            seq++;
            continue;
        }
        throw new IOException("Filename space is exhausted. Could not create file");
    }

    private String generateFileName()
    {
        return prefix + format.format(new Integer(seq)) + getExtension();
    }

}
