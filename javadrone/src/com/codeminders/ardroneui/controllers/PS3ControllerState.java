
package com.codeminders.ardroneui.controllers;

public class PS3ControllerState
{
    // buttons with pictures
    protected boolean triangle;
    protected boolean circle;
    protected boolean cross;
    protected boolean square;

    // PS3 button (sometimes labeled as Home on 3rd party models)
    protected boolean PS;

    // square small "select" button
    protected boolean select;
    // triangular small "start" button
    protected boolean start;

    protected boolean leftJoystickPress;
    protected boolean rightJoystickPress;

    // Front-side buttons
    protected boolean L1;
    protected boolean R1;
    protected boolean L2;
    protected boolean R2;

    // Direction pad
    protected boolean dirLeft;

    // Analog joysticks

    protected float   leftJoystickX;
    protected float   leftJoystickY;

    protected float   rightJoystickX;
    protected float   rightJoystickY;

    public PS3ControllerState(byte[] hid_data)
    {
        //TODO: decode hid_data and set instance fields
    }
    
    public boolean isTriangle()
    {
        return triangle;
    }

    public boolean isCircle()
    {
        return circle;
    }

    public boolean isCross()
    {
        return cross;
    }

    public boolean isSquare()
    {
        return square;
    }

    public boolean isPS()
    {
        return PS;
    }

    public boolean isSelect()
    {
        return select;
    }

    public boolean isStart()
    {
        return start;
    }

    public boolean isLeftJoystickPress()
    {
        return leftJoystickPress;
    }

    public boolean isRightJoystickPress()
    {
        return rightJoystickPress;
    }

    public boolean isL1()
    {
        return L1;
    }

    public boolean isR1()
    {
        return R1;
    }

    public boolean isL2()
    {
        return L2;
    }

    public boolean isR2()
    {
        return R2;
    }

    public boolean isDirLeft()
    {
        return dirLeft;
    }

    public float getLeftJoystickX()
    {
        return leftJoystickX;
    }

    public float getLeftJoystickY()
    {
        return leftJoystickY;
    }

    public float getRightJoystickX()
    {
        return rightJoystickX;
    }

    public float getRightJoystickY()
    {
        return rightJoystickY;
    }

}
