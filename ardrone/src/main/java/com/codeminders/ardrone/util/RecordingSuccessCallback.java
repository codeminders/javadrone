
package com.codeminders.ardrone.util;

public interface RecordingSuccessCallback
{

    /**
     * This method will be called if file was successfully recorded
     * 
     * @param filename - full path to recorded file
     */
    public void recordingSuccess(String filename);

    /**
     * This method will be called if file recording failed
     * 
     * @param filename - file name we attempted to record. Could be null.
     * @param err - error message. Could be null.
     * @param ex - throwable, which caused the error. Could be null.
     */
    public void recordingError(String filename, String err, Throwable ex);

}
