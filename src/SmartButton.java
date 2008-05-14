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

public class SmartButton extends JButton implements FocusListener, MouseListener, ActionListener{
    private String myTitle;
    private Border myBorder;

    public SmartButton(String title){
        addFocusListener(this);
        addMouseListener(this);
        setFocusPainted(false);
        setContentAreaFilled(false);
        myTitle = title;
        updateBorder(title, Color.black);
    }

    public void updateBorder(String title, Color borderColor){
        Font stdFont = new Font("SansSerif", Font.BOLD, 12);
        Border thickLine = BorderFactory.createLineBorder(borderColor, 1);
        Color textColor = Color.black;
        myBorder = BorderFactory.createTitledBorder(thickLine, title, TitledBorder.CENTER, TitledBorder.TOP, stdFont, textColor);
        setBorder(myBorder);
    }

    public void focusLost(FocusEvent e){
        updateBorder(myTitle, Color.black);
    }

    public void focusGained(FocusEvent e){
        updateBorder(myTitle, new Color(255,0,0));
    }

    public void mouseExited(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mousePressed(MouseEvent e){}

    public void mouseClicked(MouseEvent e){
        if(SwingUtilities.isRightMouseButton(e))
            updateBorder("rClick", new Color(0,0,255));
        //((JComponent)thingToListenTo).requestFocus();
    }

    public void actionPerformed(ActionEvent e){}
}
