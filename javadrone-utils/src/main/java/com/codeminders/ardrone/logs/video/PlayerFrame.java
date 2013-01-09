package com.codeminders.ardrone.logs.video;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class PlayerFrame extends JPanel {
    
    private static final long serialVersionUID = 5986493337737408886L;
    
    public Image lastFrame;
    
    public void paint(Graphics g) {
        Image img = lastFrame;
        if(img != null) {
            g.drawImage(lastFrame, 0, 0, img.getWidth(this), img.getHeight(this), this);
        }
    }
}
