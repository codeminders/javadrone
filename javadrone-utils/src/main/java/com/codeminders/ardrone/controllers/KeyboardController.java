
package com.codeminders.ardrone.controllers;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.JFrame;


public class KeyboardController extends Controller implements KeyListener
{
    GameControllerState state = new GameControllerState();

    public KeyboardController(JFrame frame)
    {
        frame.addKeyListener(this);
    }

    @Override
    public GameControllerState read() throws IOException
    {
        GameControllerState s;
        synchronized(state)
        {
            s = new GameControllerState(state);
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
        synchronized(state)
        {
            mapBooleanKey(e, false);
        }
    }

    private void mapBooleanKey(KeyEvent e, boolean value)
    {
        int c = e.getKeyCode();
        switch(c)
        {
        case KeyEvent.VK_ENTER:
            state.start = value;
            break;
        case KeyEvent.VK_SPACE:
            state.select = value;
            break;
        case KeyEvent.VK_R:
            state.PS = value;
            break;
        case KeyEvent.VK_C:
            state.triangle = value;
            break;
        case KeyEvent.VK_F:
            state.square = value;
            break;
        case KeyEvent.VK_E:
            state.circle = value;
            break;
        case KeyEvent.VK_W:
            state.leftJoystickY = value ? -127 : 0;
            break;
        case KeyEvent.VK_A:
            state.leftJoystickX = value ? -127 : 0;
            break;
        case KeyEvent.VK_S:
            state.leftJoystickY = value ? 127 : 0;
            break;
        case KeyEvent.VK_D:
            state.leftJoystickX = value ? 127 : 0;
            break;
        case KeyEvent.VK_UP:
            state.rightJoystickY = value ? -127 : 0;
            break;
        case KeyEvent.VK_LEFT:
            state.rightJoystickX = value ? -127 : 0;
            break;
        case KeyEvent.VK_DOWN:
            state.rightJoystickY = value ? 127 : 0;
            break;
        case KeyEvent.VK_RIGHT:
            state.rightJoystickX = value ? 127 : 0;
            break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void close() throws IOException {
     // nothing to do;        
    }

    @Override
    public String getName() {
        return "Standart KeyboardController";
    }

    @Override
    public String getManufacturerString() {
        return "not loading implemented";
    }

    @Override
    public String getProductString() {
        return "not loading implemented";
    }

}
