
package com.codeminders.ardroneui.controllers;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.JFrame;

public class KeyboardController extends PS3Controller implements KeyListener
{
    PS3ControllerState state = new PS3ControllerState();

    public KeyboardController(JFrame frame)
    {
        frame.addKeyListener(this);
    }

    @Override
    public PS3ControllerState read() throws IOException
    {
        PS3ControllerState s;
        synchronized(state)
        {
            s = new PS3ControllerState(state);
        }
        return s;
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        synchronized(state)
        {
            mapBooleanKey(e, true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        mapBooleanKey(e, false);
    }

    private void mapBooleanKey(KeyEvent e, boolean value)
    {
        char c = e.getKeyChar();
        switch(c)
        {
        case '\n':
            state.start = value;
            break;
        case ' ':
            state.select = value;
            break;
        case 'r':
            state.PS = value;
            break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

}
