package com.codeminders.ardrone;

import java.awt.image.BufferedImage;

public interface DroneImageReciveListner {
	void onCreate();
	void draw(BufferedImage image);
}
