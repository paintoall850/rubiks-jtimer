/*
 * JNetCube
 * Copyright (C) 2007 Chris Hunt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 *
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

public class TimerArea extends JButton implements FocusListener, KeyListener, Constants{
    public enum TimerState {RESTING, STARTING, RUNNING, STOPPED, ACCEPT_WAIT, ACCEPT};
    private static final Color
            RED = new Color(255,0,0),
            LT_GR = new Color(0,255,0),
            DK_GR = new Color(0,196,0),
            BLUE = new Color(0,0,255),
            GREY = new Color(80,80,80);
    //private Border myBorder;
    private Standalone myStandalone;
    private TimerState myState;
    //private int i, j;

    public TimerArea(Standalone standalone){
        myStandalone = standalone;
        myState = TimerState.RESTING;
        //i = 0; j = 0;

        addFocusListener(this);
        addKeyListener(this);
        setFocusPainted(false);
        setContentAreaFilled(false);
        updateBorder("Click to Focus", RED, GREY);
    }

    private void updateBorder(String title, Color textColor, Color borderColor){
        Font stdFont = new Font("SansSerif", Font.BOLD, 14);
        Border thickLine = BorderFactory.createLineBorder(borderColor, 3);
        Border myBorder = BorderFactory.createTitledBorder(thickLine, title, TitledBorder.CENTER, TitledBorder.TOP, stdFont, textColor);
        setBorder(myBorder);
    }

    public void setTimerState(TimerState state){
        myState = state; // need to do other stuff too!
    }

    public void focusLost(FocusEvent e){
        switch(myState){
            case RESTING:
                updateBorder("Start Timer", RED, GREY); break;
            case STARTING:
                updateBorder("Start Timer", RED, GREY); break;
            case RUNNING:
                updateBorder("Stop Timer", RED, GREY); break;
            case STOPPED:
                updateBorder("Stop Timer", RED, GREY); break;
            case ACCEPT_WAIT:
                updateBorder("Accept Time", RED, GREY); break;
            case ACCEPT:
                updateBorder("Accept Time", DK_GR, GREY); break;
        }

    }

    public void focusGained(FocusEvent e){
        switch(myState){
            case RESTING:
                updateBorder("Start Timer", DK_GR, RED); break;
            case STARTING:
                updateBorder("Start Timer", DK_GR, LT_GR); break;
            case RUNNING:
                updateBorder("Stop Timer", DK_GR, RED); break;
            case STOPPED:
                updateBorder("Stop Timer", DK_GR, LT_GR); break;
            case ACCEPT_WAIT:
                updateBorder("Accept Time", DK_GR, RED); break;
            case ACCEPT:
                updateBorder("Accept Time", DK_GR, LT_GR); break;
        }
    }

    public void keyReleased(final KeyEvent e){
        if(e.getKeyCode() == KeyEvent.VK_SPACE)
            switch(myState){
                case RESTING:       break;
                case STARTING:      /*myStandalone.timerStart();*/ myState = TimerState.RUNNING; focusGained(null); break;
                case RUNNING:       break;
                case STOPPED:       myState = TimerState.ACCEPT_WAIT; focusGained(null); break;
                case ACCEPT_WAIT:   break;
                case ACCEPT:        /*myStandalone.timerAccept();*/ myState = TimerState.RESTING; focusGained(null); break;
            }
        //int code = e.getKeyCode();
        //updateBorder("Key Released! (" + code + ", " + i + ")", new Color(0,0,255), new Color(255,0,0));
        //i++;
    }

    public void keyPressed(KeyEvent e){
        if(e.getKeyCode() == KeyEvent.VK_SPACE)
            switch(myState){
                case RESTING:       myState = TimerState.STARTING; focusGained(null); break;
                case STARTING:      break;
                case RUNNING:       /*myStandalone.timerStop();*/ myState = TimerState.STOPPED; focusGained(null); break;
                case STOPPED:       break;
                case ACCEPT_WAIT:   myState = TimerState.ACCEPT; focusGained(null); break;
                case ACCEPT:        break;
            }
        //int code = e.getKeyCode();
        //updateBorder("Key Pressed! (" + code + ", " + j + ")", new Color(0,0,255), new Color(0,255,0));
        //j++;
    }

    public void keyTyped(KeyEvent e){}
}
