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
import java.io.*;
import java.util.*;
import javax.swing.border.Border;

//area for all Program Constants
public interface Constants{
    static final Double INF = Double.POSITIVE_INFINITY;

    static final Font lgAlgFont = new Font("SansSerif", Font.BOLD, 18);
    static final Font smAlgFont = new Font("Courier", Font.BOLD, 11);
    static final Font regFont = new Font("Serif", Font.BOLD, 14);
    static final Font timerFont = new Font("Serif", Font.PLAIN, 90);

    static final Border blackLine = BorderFactory.createLineBorder(Color.black);
    static final Border theBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createRaisedBevelBorder());

    static final String[] puzzleChoices = {"2x2x2", "3x3x3", "4x4x4", "5x5x5", "Pyraminx", "Megaminx"/*, "Square-1"*/};
    static final String[] countdownChoices = {"0", "3", "5", "10", "15"};

    // needs to be changed in ScrambleMegaminx.java too
    static final String ALG_BREAK = "[.]"; // was "@" (note it's treated as regular expression)
    static final String APP_TITLE = "Rubik's JTimer";
    static final String ERROR_MESS = "There has been an error, please inform Doug that you saw this message.";
    static final String NOT_ADDED = " No time was added to the session.";
}
