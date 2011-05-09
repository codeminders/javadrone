
package com.codeminders.ardroneui.controllers;

public abstract class PS3ControllerState
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
    
    protected float leftJoystickX;
    protected float leftJoystickY;

    protected float rightJoystickX;
    protected float rightJoystickY;
}
