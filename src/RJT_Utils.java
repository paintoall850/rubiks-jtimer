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
import java.util.Locale;

public abstract class RJT_Utils implements Constants{

//**********************************************************************************************************************

    public static final void centerJFrame(JFrame jFrame, int width, int height){
        jFrame.setSize(width, height);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int appWidth = jFrame.getSize().width, appHeight = jFrame.getSize().height;
        jFrame.setLocation((screenSize.width-appWidth)/2, (screenSize.height-appHeight)/2);
        jFrame.doLayout();
    } // end centerJFrame

//**********************************************************************************************************************

    public static final void configureJFrame(JFrame jFrame){
        jFrame.setIconImage((new ImageIcon(jFrame.getClass().getResource("Cow.gif"))).getImage());
        jFrame.setResizable(false);
    } // end configureJFrame

//**********************************************************************************************************************

    public static final void saveToFile(Component component, String text, File file){
        File outputFile;
        if(file.getName().toLowerCase().endsWith(".txt"))
            outputFile = new File(file+"");
        else
            outputFile = new File(file+".txt");

        if(outputFile.exists()){
            int choice = JOptionPane.showConfirmDialog(component,
                                            "Overwrite existing file?",
                                            "Warning!",
                                            JOptionPane.YES_NO_OPTION,
                                            JOptionPane.QUESTION_MESSAGE);
             if(choice != JOptionPane.YES_OPTION)
                 return;
        }
        try{
            FileWriter fw = new FileWriter(outputFile);
            BufferedWriter out = new BufferedWriter(fw);
            out.write(text);
            out.close();
        } catch(IOException ex){
            JOptionPane.showMessageDialog(component, "There was an error saving. You may not have write permissions.");
        }
    } // end saveToFile

//**********************************************************************************************************************

    public static void enterPressesWhenFocused(JButton button){

        button.registerKeyboardAction(
            button.getActionForKeyStroke(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false)),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
                JComponent.WHEN_FOCUSED);

        button.registerKeyboardAction(
            button.getActionForKeyStroke(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true)),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                JComponent.WHEN_FOCUSED);
    }

//**********************************************************************************************************************

    public static final void hideOnEsc(final JFrame jFrame, JRootPane rootPane){
        ActionListener escListener = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                jFrame.setVisible(false);
            }
        };
        rootPane.registerKeyboardAction(escListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), // on down-press of Esc
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    public static final Polygon regular_poly(int n, double r, boolean pointup){
        Polygon poly = new Polygon();
        double offset = (pointup ? -Math.PI/2 : Math.PI/2);
        for(int i=0; i<n; i++)
            poly.addPoint((int)Math.round(r*Math.cos(i*2*Math.PI/n + offset)),
                          (int)Math.round(r*Math.sin(i*2*Math.PI/n + offset)));
        return poly;
    }

//**********************************************************************************************************************

    public static final Point intersectionPoint(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4){
        double norm = DET(x1-x2, y1-y2, x3-x4, y3-y4);
        double x_inter = DET(DET(x1,y1,x2,y2), x1-x2, DET(x3,y3,x4,y4), x3-x4)/norm;
        double y_inter = DET(DET(x1,y1,x2,y2), y1-y2, DET(x3,y3,x4,y4), y3-y4)/norm;

        return new Point((int)Math.round(x_inter), (int)Math.round(y_inter));
    }

    private static final double DET(double a, double b, double c, double d){
        return (a*d - b*c);
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    public static final double roundTime(double x){
        return Math.round(100D*x)/100D;
    }

    public static final String ssxx_format(double x){
        x = roundTime(x);
        return String.format(Locale.US, "%05.2f", x); // new Locale("en", "US")
    }

    public static final String ss_format(double x){
        x = (double)Math.round(x);
        return String.format(Locale.US, "%02.0f", x); // new Locale("en", "US")
    }

//**********************************************************************************************************************

    public static final String timeToString(double time, boolean showMinutes, boolean truncate){
        return timeToString(time, showMinutes, truncate, false);
    }

    public static final String timeToString(double time, boolean showMinutes, boolean truncate, boolean verbose){
        String s;
        time = roundTime(time);
        if(time>=60 && showMinutes){
            int min = (int)(time/60);
            double sec = time-min*60;
            s = min + ":" + ((time < 600 || !truncate) ? ssxx_format(sec) : ss_format(sec));
        } else
            s = ssxx_format(time) + (verbose ? " sec." : "");
        return s;
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    public static final String makeRed(String s){
        return "<html><font color=\"#FF0000\">" + s + "</font></html>";
    }

    public static final String makeGreen(String s){
        return "<html><font color=\"#00FF00\">" + s + "</font></html>";
    }

    public static final String makeBlue(String s){
        return "<html><font color=\"#0000FF\">" + s + "</font></html>";
    }

}
