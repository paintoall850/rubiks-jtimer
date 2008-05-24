/*
 * Rubik's JTimer - Copyright (C) 2008 Doug Li
 * JNetCube - Copyright (C) 2007 Chris Hunt
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

public class AboutScreen extends JFrame implements Constants{

    public AboutScreen(){
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        // configure JFrame
        setTitle("About " + APP_TITLE);
        setSize(660, 255);
        setIconImage((new ImageIcon(getClass().getResource("Cow.gif"))).getImage());
        setResizable(false);

        // center frame on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int appWidth = getSize().width, appHeight = getSize().height;
        setLocation((screenSize.width-appWidth)/2, (screenSize.height-appHeight)/2);

        JLabel cowPic = new JLabel(new ImageIcon(getClass().getResource("Cow.jpg")));
        cowPic.setBorder(BorderFactory.createLineBorder(Color.black));
        JLabel aboutLabel = new JLabel("<html><font size=\"7\">" + APP_TITLE + "</font><br>Package Updated: May 23, 2008<br><br>Derived from JNetCube which was designed and written by Chris Hunt (<u>huntca@plu.edu</u>) for the cubing community.<br><br>5x5 Scramble Viewer, \"re-skin\", various bug fixes, and minor changes contributed by Doug Li.<br>Contact him for feature requests and possible bugs at <u>DougCube@gmail.com</u>.</html>");
        aboutLabel.setVerticalAlignment(SwingConstants.TOP);

        cowPic.setBounds(10,10,215,200);
        aboutLabel.setBounds(235,10,420,200);

        contentPane.add(cowPic);
        contentPane.add(aboutLabel);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
    } // end AboutScreen
}
