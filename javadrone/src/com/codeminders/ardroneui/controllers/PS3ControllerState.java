
package com.codeminders.ardroneui.controllers;

import java.util.BitSet;

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
    protected boolean dirLeft;

    // Analog joysticks

    protected int   leftJoystickX;
    protected int   leftJoystickY;

    protected int   rightJoystickX;
    protected int   rightJoystickY;

    public PS3ControllerState(byte[] hid_data, int hid_data_len)
    {
        //TODO: decode hid_data and set instance fields
        
        // 13 bit fields (buttons)
        // X,Y,Z,Rz - 4 8bit fields
        
        for(int i=0; i<hid_data_len; i++)
        {
            int v = hid_data[i];
            if (v<0) v = v+256;
            String hs = Integer.toHexString(v);
            if (v<16)
                System.err.print("0");
            System.err.print(hs + " ");
        }
        System.err.println("");
                
        BitSet bs = new BitSet(13);
        for(int i=0;i<8;i++)
        {
            if((1 & (hid_data[0] >> i)) == 1)
                bs.set(i);
        }
        for(int i=0;i<5;i++)
        {
            if((1 & (hid_data[1] >> i)) == 1)
                bs.set(8+i);
        }
        
        int i = 0;
        square = bs.get(i++);
        cross  = bs.get(i++);
        circle  = bs.get(i++);
        triangle  = bs.get(i++);
        L1  = bs.get(i++);
        R1  = bs.get(i++);
        L2  = bs.get(i++);
        R2  = bs.get(i++);
        select = bs.get(i++);
        start  = bs.get(i++);
        leftJoystickPress  = bs.get(i++);
        rightJoystickPress  = bs.get(i++);
        PS  = bs.get(i++);
        
        
        System.err.println(toString());

            
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
        builder.append(", dirLeft=");
        builder.append(dirLeft);
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
