
package com.codeminders.ardrone;

import java.awt.image.BufferedImage;

public interface DroneVideoListener
{
    void frameReceived(BufferedImage image);
}
