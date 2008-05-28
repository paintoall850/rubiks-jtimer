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

public abstract class RJT_Utils implements Constants{

//**********************************************************************************************************************

    public static final void centerJFrame(JFrame jFrame, int width, int height){
        jFrame.setSize(width, height);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int appWidth = jFrame.getSize().width, appHeight = jFrame.getSize().height;
        jFrame.setLocation((screenSize.width-appWidth)/2, (screenSize.height-appHeight)/2);
    }

//**********************************************************************************************************************

    public static final void configureJFrame(JFrame jFrame){
        jFrame.setIconImage((new ImageIcon(jFrame.getClass().getResource("Cow.gif"))).getImage());
        jFrame.setResizable(false);
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    public static final Polygon regular_poly(int n, float r, boolean pointup){
        Polygon poly = new Polygon();
        float offset = (float)(pointup ? -Math.PI/2 : Math.PI/2);
        for(int i=0; i<n; i++)
            poly.addPoint((int)Math.round(r*Math.cos(i*2*Math.PI/n + offset)),
                          (int)Math.round(r*Math.sin(i*2*Math.PI/n + offset)));
        return poly;
    }

//**********************************************************************************************************************

    public static final Point intersectionPoint(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4){
        float norm = DET(x1-x2, y1-y2, x3-x4, y3-y4);
        float x_inter = DET(DET(x1,y1,x2,y2), x1-x2, DET(x3,y3,x4,y4), x3-x4)/norm;
        float y_inter = DET(DET(x1,y1,x2,y2), y1-y2, DET(x3,y3,x4,y4), y3-y4)/norm;

        return new Point((int)Math.round(x_inter), (int)Math.round(y_inter));
    }

    private static final float DET(float a, float b, float c, float d){
        return (a*d - b*c);
    }

//**********************************************************************************************************************

}
