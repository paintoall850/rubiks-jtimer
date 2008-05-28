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
import java.util.*;
import java.awt.image.BufferedImage;

public class ScramblePanel extends JPanel implements MouseListener, Constants{
    private String myPuzzle = "nothing";
    private String myScramble = "";

    private CubeImage cubeImage;
    private PyraminxImage pyraminxImage;
    private MegaminxImage megaminxImage;

//**********************************************************************************************************************

    // would prefer to get (width, height) with function calls, but they don't work
    public ScramblePanel(int width, int height){
        cubeImage = new CubeImage(width, height);
        pyraminxImage = new PyraminxImage(width, height);
        megaminxImage = new MegaminxImage(width, height);
    }

//**********************************************************************************************************************

    public void newScramble(String puzzle, String scrambleAlg){
        myPuzzle = puzzle;
        myScramble = scrambleAlg;
        updateScreen();
    }

//**********************************************************************************************************************

    public final void updateScreen(){
        boolean failed = true;
        String s = "null";

             if(myPuzzle.equals("2x2x2")) s = cubeImage.scramble(2, myScramble);
        else if(myPuzzle.equals("3x3x3")) s = cubeImage.scramble(3, myScramble);
        else if(myPuzzle.equals("4x4x4")) s = cubeImage.scramble(4, myScramble);
        else if(myPuzzle.equals("5x5x5")) s = cubeImage.scramble(5, myScramble);
        else if(myPuzzle.equals("Pyraminx")) s = pyraminxImage.scramble(myScramble);
        else if(myPuzzle.equals("Megaminx")) s = megaminxImage.scramble(myScramble);

        if(s.equals("success"))
            repaint();
        else
            JOptionPane.showMessageDialog(this, "Scramble View encountered bad token for " + myPuzzle + ": <" + s + ">.");
    }

//**********************************************************************************************************************

    public Color[] getCubeColors(){return cubeImage.myColors;}
    public Color[] getPyraminxColors(){return pyraminxImage.myColors;}
    public Color[] getMegaminxColors(){return megaminxImage.myColors;}

    public void setCubeColors(Color[] newColors){cubeImage.setColors(newColors);}
    public void setPyraminxColors(Color[] newColors){pyraminxImage.setColors(newColors);}
    public void setMegaminxColors(Color[] newColors){megaminxImage.setColors(newColors);}

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private ColorListener colorListener;

    public static interface ColorListener{
        public abstract void faceClicked(ScramblePanel scramblePanel, int face, Color[] puzzleColors, String s);
    }

    public void addColorListener(ColorListener colorListener){
        addMouseListener(this);
        this.colorListener = colorListener;
    }

//**********************************************************************************************************************

    public void mouseReleased(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}

//**********************************************************************************************************************

    public void mouseClicked(MouseEvent e){
        int x = e.getX(), y = e.getY();
        String s;
        Color[] c;

        if(myPuzzle.equals("2x2x2") || myPuzzle.equals("3x3x3") || myPuzzle.equals("4x4x4") || myPuzzle.equals("5x5x5")){
            s = " of Cube";
            c = cubeImage.myColors;
                 if(cubeImage.inFace(0,x,y)) colorListener.faceClicked(this, 0, c, "Front Face"+s);
            else if(cubeImage.inFace(1,x,y)) colorListener.faceClicked(this, 1, c, "Back Face"+s);
            else if(cubeImage.inFace(2,x,y)) colorListener.faceClicked(this, 2, c, "Left Face"+s);
            else if(cubeImage.inFace(3,x,y)) colorListener.faceClicked(this, 3, c, "Right Face"+s);
            else if(cubeImage.inFace(4,x,y)) colorListener.faceClicked(this, 4, c, "Down Face"+s);
            else if(cubeImage.inFace(5,x,y)) colorListener.faceClicked(this, 5, c, "Up Face"+s);
        }
        else if(myPuzzle.equals("Pyraminx")){
            s = " of Pyraminx";
            c = pyraminxImage.myColors;
                 if(pyraminxImage.inFace(0,x,y)) colorListener.faceClicked(this, 0, c, "Front Face"+s);
            else if(pyraminxImage.inFace(1,x,y)) colorListener.faceClicked(this, 1, c, "Right Face"+s);
            else if(pyraminxImage.inFace(2,x,y)) colorListener.faceClicked(this, 2, c, "Down Face"+s);
            else if(pyraminxImage.inFace(3,x,y)) colorListener.faceClicked(this, 3, c, "Left Face"+s);
        }
        else if(myPuzzle.equals("Megaminx")){
            s = " of Megaminx";
            c = megaminxImage.myColors;
                 if(megaminxImage.inFace( 0,x,y)) colorListener.faceClicked(this,  0, c, "Front Face (A)"+s);
            else if(megaminxImage.inFace( 1,x,y)) colorListener.faceClicked(this,  1, c, "Up Face (B)"+s);
            else if(megaminxImage.inFace( 2,x,y)) colorListener.faceClicked(this,  2, c, "Upper-Right Face (C)"+s);
            else if(megaminxImage.inFace( 3,x,y)) colorListener.faceClicked(this,  3, c, "Lower-Right Face (D)"+s);
            else if(megaminxImage.inFace( 4,x,y)) colorListener.faceClicked(this,  4, c, "Lower-Left Face (E)"+s);
            else if(megaminxImage.inFace( 5,x,y)) colorListener.faceClicked(this,  5, c, "Upper-Left Face (F)"+s);
            else if(megaminxImage.inFace( 6,x,y)) colorListener.faceClicked(this,  6, c, "Back Face (a)"+s);
            else if(megaminxImage.inFace( 7,x,y)) colorListener.faceClicked(this,  7, c, "Down Face (b)"+s);
            else if(megaminxImage.inFace( 8,x,y)) colorListener.faceClicked(this,  8, c, "Lower-Back-Right Face (f)"+s);
            else if(megaminxImage.inFace( 9,x,y)) colorListener.faceClicked(this,  9, c, "Upper-Back-Right Face (e)"+s);
            else if(megaminxImage.inFace(10,x,y)) colorListener.faceClicked(this, 10, c, "Upper-Back-Left Face (d)"+s);
            else if(megaminxImage.inFace(11,x,y)) colorListener.faceClicked(this, 11, c, "Lower-Back-Left Face (c)"+s);
        }
    } // end mouseClicked

//**********************************************************************************************************************

    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        BufferedImage myImage;
        if(myPuzzle.equals("2x2x2") || myPuzzle.equals("3x3x3") || myPuzzle.equals("4x4x4") || myPuzzle.equals("5x5x5"))
            myImage = cubeImage.getImage();
        else if(myPuzzle.equals("Pyraminx"))
            myImage = pyraminxImage.getImage();
        else if(myPuzzle.equals("Megaminx"))
            myImage = megaminxImage.getImage();
        else
            return;

        g.drawImage(myImage, 0, 0, null);
    }
}
