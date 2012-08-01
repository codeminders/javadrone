
package com.codeminders.controltower.config;

import java.io.BufferedInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer
{

    private static final int EXTERNAL_BUFFER_SIZE = 128000;

	public static synchronized void playResource(Class resclass, String resname) throws Exception
    {
        play(AudioSystem.getAudioInputStream(new BufferedInputStream(resclass.getResourceAsStream(resname))));
    }

    public static synchronized void play(AudioInputStream audioInputStream) throws Exception
    {

        AudioFormat audioFormat = audioInputStream.getFormat();
        SourceDataLine line = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try
        {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
            line.start();

            int nBytesRead = 0;
            byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
            while((nBytesRead = audioInputStream.read(abData, 0, abData.length)) > 0)
            {
                line.write(abData, 0, nBytesRead);
            }
        } finally
        {
            if(line != null)
            {
                line.drain();
                line.close();
            }
        }
    }

}
