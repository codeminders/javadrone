
package com.codeminders.ardrone.util;

import java.io.File;
import java.io.IOException;

public abstract class FileRecorder
{
    public FileRecorder(File base_path, int starting_seq, String prefix, RecordingSuccessCallback callback)
    {
        this.base_path = base_path;
        this.starting_seq = starting_seq;
        this.prefix = prefix;
        this.callback = callback;
    }

    protected File                     base_path;
    protected int                      starting_seq;
    protected String                   prefix;
    protected RecordingSuccessCallback callback;

    public abstract String getExtension();

    protected File openFile() throws IOException
    {
        // TODO: sequence number is ignored for now
        return File.createTempFile(prefix, getExtension(), base_path);
    }

}
