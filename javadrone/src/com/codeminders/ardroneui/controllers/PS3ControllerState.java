
package com.codeminders.ardroneui.controllers;

/**
 * Data structure describing state of generic PS-3 compatible game controller.
 * 
 * @author lord
 */
public class PS3ControllerState
{
    // buttons with pictures
    protected boolean square;
    protected boolean cross;
    protected boolean circle;
    protected boolean triangle;

    // Front-side buttons
    protected boolean L1;
    protected boolean R1;
    protected boolean L2;
    protected boolean R2;

    // square small "select" button
    protected boolean select;
    // triangular small "start" button
    protected boolean start;

    // Pressing on joysticks (button)
    protected boolean leftJoystickPress;
    protected boolean rightJoystickPress;

    // PS3 button (sometimes labeled as Home on 3rd party models)
    protected boolean PS;

    // Direction pad (hatswitch)
    protected int     hatSwitchLeftRight;
    protected int     hatSwitchUpDown;

    // Analog joysticks

    protected int     leftJoystickX;
    protected int     leftJoystickY;

    protected int     rightJoystickX;
    protected int     rightJoystickY;

    public PS3ControllerState(boolean square, boolean cross, boolean circle, boolean triangle, boolean l1, boolean r1,
            boolean l2, boolean r2, boolean select, boolean start, boolean leftJoystickPress,
            boolean rightJoystickPress, boolean pS, int hatSwitchLeftRight, int hatSwitchUpDown, int leftJoystickX,
            int leftJoystickY, int rightJoystickX, int rightJoystickY)
    {
        super();
        this.square = square;
        this.cross = cross;
        this.circle = circle;
        this.triangle = triangle;
        L1 = l1;
        R1 = r1;
        L2 = l2;
        R2 = r2;
        this.select = select;
        this.start = start;
        this.leftJoystickPress = leftJoystickPress;
        this.rightJoystickPress = rightJoystickPress;
        PS = pS;
        this.hatSwitchLeftRight = hatSwitchLeftRight;
        this.hatSwitchUpDown = hatSwitchUpDown;
        this.leftJoystickX = leftJoystickX;
        this.leftJoystickY = leftJoystickY;
        this.rightJoystickX = rightJoystickX;
        this.rightJoystickY = rightJoystickY;
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

    public int getLeftJoystickX()
    {
        return leftJoystickX;
    }

    public int getLeftJoystickY()
    {
        return leftJoystickY;
    }

    public int getRightJoystickX()
    {
        return rightJoystickX;
    }

    public int getRightJoystickY()
    {
        return rightJoystickY;
    }

    public int getHatSwitchLeftRight()
    {
        return hatSwitchLeftRight;
    }

    public int getHatSwitchUpDown()
    {
        return hatSwitchUpDown;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("PS3ControllerState [square=");
        builder.append(square);
        builder.append(", cross=");
        builder.append(cross);
        builder.append(", circle=");
        builder.append(circle);
        builder.append(", triangle=");
        builder.append(triangle);
        builder.append(", L1=");
        builder.append(L1);
        builder.append(", R1=");
        builder.append(R1);
        builder.append(", L2=");
        builder.append(L2);
        builder.append(", R2=");
        builder.append(R2);
        builder.append(", select=");
        builder.append(select);
        builder.append(", start=");
        builder.append(start);
        builder.append(", rightJoystickPress=");
        builder.append(rightJoystickPress);
        builder.append(", leftJoystickPress=");
        builder.append(leftJoystickPress);
        builder.append(", PS=");
        builder.append(PS);
        builder.append(", hatSwitchLeftRight=");
        builder.append(hatSwitchLeftRight);
        builder.append(", hatSwitchUpDown=");
        builder.append(hatSwitchUpDown);
        builder.append(", leftJoystickX=");
        builder.append(leftJoystickX);
        builder.append(", leftJoystickY=");
        builder.append(leftJoystickY);
        builder.append(", rightJoystickX=");
        builder.append(rightJoystickX);
        builder.append(", rightJoystickY=");
        builder.append(rightJoystickY);
        builder.append("]");
        return builder.toString();
    }

}
