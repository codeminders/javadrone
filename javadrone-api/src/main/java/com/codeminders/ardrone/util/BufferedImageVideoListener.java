
package com.codeminders.ardrone.util;

import java.awt.image.BufferedImage;

import com.codeminders.ardrone.DroneVideoListener;

/**
 * This is convenience base class for DroneVideoListener implementors, which
 * presents video frames to user as instances of java.awt.image.BufferedImag
 * 
 * @author lord
 */
public abstract class BufferedImageVideoListener implements DroneVideoListener
{
    @Override
    public void frameReceived(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize)
    {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        image.setRGB(startX, startY, w, h, rgbArray, offset, scansize);
        imageReceived(image);
    }

    public abstract void imageReceived(BufferedImage image);

}
