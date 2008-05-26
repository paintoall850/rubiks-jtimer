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
import javax.swing.border.Border;
import java.awt.image.BufferedImage;

public class ScramblePane extends JPanel implements MouseListener, Constants{

    private Color[] cubeColors = new Color[6];
    private Color[] pyraminxColors = new Color[4];
    private Color[] megaminxColors = new Color[12];

    private BufferedImage myImage;
    private Polygon[] cubeFacesX = new Polygon[6];
    private Polygon[] pyraminxFaces = new Polygon[4];
    private Polygon[] megaminxFaces = new Polygon[12];

    private String myPuzzle = "nothing";
    private String myScramble = "";
    private int myWidth, myHeight; // would prefer to get these with function calls, but they don't work

//**********************************************************************************************************************

    public ScramblePane(int width, int height){
        myWidth = width; myHeight = height; // needs gettin' rid of
//System.err.print("width:" + width + "\n");
//System.err.print("height:" + height + "\n");
//System.err.print("getWidth():" + this.getWidth() + "\n");
//System.err.print("getHeight():" + this.getHeight() + "\n");

        for(int face=0; face<6; face++) cubeColors[face] = Color.black; // just incase...
        for(int face=0; face<4; face++) pyraminxColors[face] = Color.black; // just incase...
        for(int face=0; face<12; face++) megaminxColors[face] = Color.black; // just incase...

        for(int face=0; face<6; face++) cubeFacesX[face] = new Polygon(); // just incase...
        for(int face=0; face<4; face++) pyraminxFaces[face] = new Polygon(); // just incase...
        for(int face=0; face<12; face++) megaminxFaces[face] = new Polygon(); // just incase...

        clearScreen();
    }

//**********************************************************************************************************************

    public void newScramble(String puzzle, String scrambleAlg){
        myPuzzle = puzzle;
        myScramble = scrambleAlg;
        updateScreen();
    }

//**********************************************************************************************************************

    private void clearImage(){
        myImage = new BufferedImage(myWidth, myHeight, BufferedImage.TYPE_INT_ARGB);
    }

    private void clearScreen(){
        myImage = new BufferedImage(myWidth, myHeight, BufferedImage.TYPE_INT_ARGB);
        repaint();
    }

//**********************************************************************************************************************

    public void updateScreen(){
        clearScreen();
             if(myPuzzle.equals("2x2x2")) scrambleCube(2);
        else if(myPuzzle.equals("3x3x3")) scrambleCube(3);
        else if(myPuzzle.equals("4x4x4")) scrambleCube(4);
        else if(myPuzzle.equals("5x5x5")) scrambleCube(5);
        else if(myPuzzle.equals("Pyraminx")) scramblePyraminx();
        else if(myPuzzle.equals("Megaminx")) scrambleMegaminx();
    }

//**********************************************************************************************************************

    public void setCubeColors(Color[] newColors){cubeColors = newColors;}
    public void setPyraminxColors(Color[] newColors){pyraminxColors = newColors;}
    public void setMegaminxColors(Color[] newColors){megaminxColors = newColors;}

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private ColorListener colorListener;

    public static interface ColorListener{
        public abstract void faceClicked(ScramblePane scramblePane, int face, Color[] puzzleColors, String s);
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

        if(myPuzzle.equals("2x2x2") || myPuzzle.equals("3x3x3") || myPuzzle.equals("4x4x4") || myPuzzle.equals("5x5x5")){
                 if(cubeFacesX[0].contains(x,y))  colorListener.faceClicked(this, 0, cubeColors, "Front Face of Cube");
            else if(cubeFacesX[1].contains(x,y))  colorListener.faceClicked(this, 1, cubeColors, "Back Face of Cube");
            else if(cubeFacesX[2].contains(x,y))  colorListener.faceClicked(this, 2, cubeColors, "Left Face of Cube");
            else if(cubeFacesX[3].contains(x,y))  colorListener.faceClicked(this, 3, cubeColors, "Right Face of Cube");
            else if(cubeFacesX[4].contains(x,y))  colorListener.faceClicked(this, 4, cubeColors, "Down Face of Cube");
            else if(cubeFacesX[5].contains(x,y))  colorListener.faceClicked(this, 5, cubeColors, "Up Face of Cube");
        }
        else if(myPuzzle.equals("Pyraminx")){
                 if(pyraminxFaces[0].contains(x,y)) colorListener.faceClicked(this, 0, pyraminxColors, "Front Face of Pyraminx");
            else if(pyraminxFaces[1].contains(x,y)) colorListener.faceClicked(this, 1, pyraminxColors, "Right Face of Pyraminx");
            else if(pyraminxFaces[2].contains(x,y)) colorListener.faceClicked(this, 2, pyraminxColors, "Down Face of Pyraminx");
            else if(pyraminxFaces[3].contains(x,y)) colorListener.faceClicked(this, 3, pyraminxColors, "Left Face of Pyraminx");
        }
        else if(myPuzzle.equals("Megaminx")){
                 if(megaminxFaces[ 0].contains(x,y)) colorListener.faceClicked(this,  0, megaminxColors, "Front Face (A) of Megaminx");
            else if(megaminxFaces[ 1].contains(x,y)) colorListener.faceClicked(this,  1, megaminxColors, "Up Face (B) of Megaminx");
            else if(megaminxFaces[ 2].contains(x,y)) colorListener.faceClicked(this,  2, megaminxColors, "Upper-Right Face (C) of Megaminx");
            else if(megaminxFaces[ 3].contains(x,y)) colorListener.faceClicked(this,  3, megaminxColors, "Lower-Right Face (D) of Megaminx");
            else if(megaminxFaces[ 4].contains(x,y)) colorListener.faceClicked(this,  4, megaminxColors, "Lower-Left Face (E) of Megaminx");
            else if(megaminxFaces[ 5].contains(x,y)) colorListener.faceClicked(this,  5, megaminxColors, "Upper-Left Face (F) of Megaminx");
            else if(megaminxFaces[ 6].contains(x,y)) colorListener.faceClicked(this,  6, megaminxColors, "Back Face (a) of Megaminx");
            else if(megaminxFaces[ 7].contains(x,y)) colorListener.faceClicked(this,  7, megaminxColors, "Down Face (b) of Megaminx");
            else if(megaminxFaces[ 8].contains(x,y)) colorListener.faceClicked(this,  8, megaminxColors, "Lower-Back-Right Face (f) of Megaminx");
            else if(megaminxFaces[ 9].contains(x,y)) colorListener.faceClicked(this,  9, megaminxColors, "Upper-Back-Right Face (e) of Megaminx");
            else if(megaminxFaces[10].contains(x,y)) colorListener.faceClicked(this, 10, megaminxColors, "Upper-Back-Left Face (d) of Megaminx");
            else if(megaminxFaces[11].contains(x,y)) colorListener.faceClicked(this, 11, megaminxColors, "Lower-Back-Left Face (c) of Megaminx");
        }
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private void drawCubeX(int size, int[][][][] state){
        clearImage(); Graphics2D g2d = myImage.createGraphics();
        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // turn on if angled lines

        int margin = 15;
        int face_gap = 7;
        //int face_pixels = 60;
        int face_pixels = Math.min((myWidth - 3*face_gap - 2*margin)/4, ((myHeight-19) - 2*face_gap - 2*margin)/3);
//System.err.print("myWidth:" + myWidth + "\n");
//System.err.print("myHeight:" + myHeight + "\n");
//System.err.print("margin:" + margin + "\n");
//System.err.print("face_pixels:" + face_pixels + "\n");
        int n = face_pixels + face_gap;
        //int x = 15, y = 19; // nudge factors
        int x = (myWidth - 4*face_pixels - 3*face_gap)/2, y = ((myHeight-19) - 3*face_pixels - 2*face_gap)/2;
        y += 14; // nudge away from title
//System.err.print("x:" + x + "\n");
//System.err.print("y:" + y + "\n");

        cubeFacesX[0] = drawCubeFace(g2d, size, face_pixels, 1*n + x, 1*n + y, state[0]);
        cubeFacesX[1] = drawCubeFace(g2d, size, face_pixels, 3*n + x, 1*n + y, state[1]);
        cubeFacesX[2] = drawCubeFace(g2d, size, face_pixels, 0*n + x, 1*n + y, state[2]);
        cubeFacesX[3] = drawCubeFace(g2d, size, face_pixels, 2*n + x, 1*n + y, state[3]);
        cubeFacesX[4] = drawCubeFace(g2d, size, face_pixels, 1*n + x, 2*n + y, state[4]);
        cubeFacesX[5] = drawCubeFace(g2d, size, face_pixels, 1*n + x, 0*n + y, state[5]);

        repaint();
    }

//**********************************************************************************************************************

    private Polygon drawCubeFace(Graphics2D g2d, int size, int face_pixels, int x_offset, int y_offset, int[][][] state){
        Polygon square = square_poly(face_pixels);
        square.translate(x_offset, y_offset);

        int xs[][] = new int[4][size+1], ys[][] = new int[4][size+1]; // the points that are on the edges, including outer
        for(int i=0; i<size+1; i++){
            float w = i/(float)size;
            xs[0][i] = (int)Math.round(w*square.xpoints[1] + (1F-w)*square.xpoints[0]);
            ys[0][i] = (int)Math.round(w*square.ypoints[1] + (1F-w)*square.ypoints[0]);
            xs[1][i] = (int)Math.round(w*square.xpoints[2] + (1F-w)*square.xpoints[1]);
            ys[1][i] = (int)Math.round(w*square.ypoints[2] + (1F-w)*square.ypoints[1]);
            xs[2][i] = (int)Math.round(w*square.xpoints[2] + (1F-w)*square.xpoints[3]);
            ys[2][i] = (int)Math.round(w*square.ypoints[2] + (1F-w)*square.ypoints[3]);
            xs[3][i] = (int)Math.round(w*square.xpoints[3] + (1F-w)*square.xpoints[0]);
            ys[3][i] = (int)Math.round(w*square.ypoints[3] + (1F-w)*square.ypoints[0]);
        }

        Point lattice_points[][] = new Point[size+1][size+1]; // for the internal points
        for(int i=0; i<size+1; i++)
            for(int j=0; j<size+1; j++)
                lattice_points[i][j] = RJT_Utils.intersectionPoint(
                            xs[1][i], ys[1][i],
                            xs[3][i], ys[3][i],
                            xs[0][j], ys[0][j],
                            xs[2][j], ys[2][j]);

        Polygon stickers[][] = new Polygon[size][size];
        for(int i=0; i<size; i++)
            for(int j=0; j<size; j++){
                stickers[i][j] = new Polygon();
                stickers[i][j].addPoint(lattice_points[i][j].x, lattice_points[i][j].y);
                stickers[i][j].addPoint(lattice_points[i][j+1].x, lattice_points[i][j+1].y);
                stickers[i][j].addPoint(lattice_points[i+1][j+1].x, lattice_points[i+1][j+1].y);
                stickers[i][j].addPoint(lattice_points[i+1][j].x, lattice_points[i+1][j].y);
                g2d.setColor(cubeColors[state[i][j][0]]);
                g2d.fillPolygon(stickers[i][j]); // fill each sticker
            }

        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(3F));
        g2d.drawPolygon(square); // draw the outer square
        g2d.setStroke(new BasicStroke(1.5F));
        for(int i=1; i<size; i++) // draw horizontal inside lines
            g2d.drawLine(xs[1][i], ys[1][i], xs[3][i], ys[3][i]);
        for(int j=1; j<size; j++) // draw vertical inside lines
            g2d.drawLine(xs[0][j], ys[0][j], xs[2][j], ys[2][j]);

        return square;
    }

//**********************************************************************************************************************

    private void scrambleCube(int size){
        StringTokenizer moves = new StringTokenizer(myScramble);
        String move = "null";
        boolean failed = false;

        int[][][][] state = new int[6][size][size][1];
        for(int face=0; face<6; face++)
            for(int i=0; i<size; i++)
                for(int j=0; j<size; j++)
                    state[face][i][j][0] = face;

        while(moves.hasMoreTokens()){
            move = moves.nextToken();
            int dir = 1;
            if(move.endsWith("'")){move = move.substring(0, move.length()-1); dir = 3;}
            if(move.endsWith("2")){move = move.substring(0, move.length()-1); dir = 2;}
            //JOptionPane.showMessageDialog(this, "For " + myPuzzle + ": <" + move + "> gives " + dir + ".");

                 if(move.equals("F")){doCubeTurn(size, state, 0, 0, dir);}
            else if(move.equals("B")){doCubeTurn(size, state, 1, 0, dir);}
            else if(move.equals("L")){doCubeTurn(size, state, 2, 0, dir);}
            else if(move.equals("R")){doCubeTurn(size, state, 3, 0, dir);}
            else if(move.equals("D")){doCubeTurn(size, state, 4, 0, dir);}
            else if(move.equals("U")){doCubeTurn(size, state, 5, 0, dir);}
            else if(move.equals("x")){for(int slice=0; slice<size; slice++) doCubeTurn(size, state, 3, slice, dir);}
            else if(move.equals("y")){for(int slice=0; slice<size; slice++) doCubeTurn(size, state, 5, slice, dir);}
            else if(move.equals("z")){for(int slice=0; slice<size; slice++) doCubeTurn(size, state, 0, slice, dir);}

            else if(size < 3){failed = true; break;}
            else if(move.equals("f")){doCubeTurn(size, state, 0, 1, dir); if(size == 3) doCubeTurn(size, state, 0, 0, dir);}
            else if(move.equals("b")){doCubeTurn(size, state, 1, 1, dir); if(size == 3) doCubeTurn(size, state, 1, 0, dir);}
            else if(move.equals("l")){doCubeTurn(size, state, 2, 1, dir); if(size == 3) doCubeTurn(size, state, 2, 0, dir);}
            else if(move.equals("r")){doCubeTurn(size, state, 3, 1, dir); if(size == 3) doCubeTurn(size, state, 3, 0, dir);}
            else if(move.equals("d")){doCubeTurn(size, state, 4, 1, dir); if(size == 3) doCubeTurn(size, state, 4, 0, dir);}
            else if(move.equals("u")){doCubeTurn(size, state, 5, 1, dir); if(size == 3) doCubeTurn(size, state, 5, 0, dir);}
            else if(move.equals("M")){for(int slice=1; slice<size-1; slice++) doCubeTurn(size, state, 2, slice, dir);}
            else if(move.equals("E")){for(int slice=1; slice<size-1; slice++) doCubeTurn(size, state, 4, slice, dir);}
            else if(move.equals("S")){for(int slice=1; slice<size-1; slice++) doCubeTurn(size, state, 0, slice, dir);}
            else if(move.equals("Fw")){doCubeTurn(size, state, 0, 0, dir); doCubeTurn(size, state, 0, 1, dir);}
            else if(move.equals("Bw")){doCubeTurn(size, state, 1, 0, dir); doCubeTurn(size, state, 1, 1, dir);}
            else if(move.equals("Lw")){doCubeTurn(size, state, 2, 0, dir); doCubeTurn(size, state, 2, 1, dir);}
            else if(move.equals("Rw")){doCubeTurn(size, state, 3, 0, dir); doCubeTurn(size, state, 3, 1, dir);}
            else if(move.equals("Dw")){doCubeTurn(size, state, 4, 0, dir); doCubeTurn(size, state, 4, 1, dir);}
            else if(move.equals("Uw")){doCubeTurn(size, state, 5, 0, dir); doCubeTurn(size, state, 5, 1, dir);}

            else if((size < 5) || (size%2 == 0)){failed = true; break;}
            else if(move.equals("m")){doCubeTurn(size, state, 2, (size-1)/2, dir);}
            else if(move.equals("e")){doCubeTurn(size, state, 4, (size-1)/2, dir);}
            else if(move.equals("s")){doCubeTurn(size, state, 0, (size-1)/2, dir);}
            else{failed = true; break;}
        }

        if(failed)
            JOptionPane.showMessageDialog(this, "Scramble View encountered bad token for " + myPuzzle + ": <"+ move + ">.");
        else
            drawCubeX(size, state);
    }

//**********************************************************************************************************************

    // dir = 1 for 90 deg, 2 for 180 deg, 3 for 270 deg
    private void doCubeTurn(int size, int[][][][] state, int face, int slice, int dir){
        dir %= 4;
        if(slice > size-1) return;
        if(slice == size-1){ //far slice, mostly to help handle whole cube rotation
            doCubeTurn(size, state, (face%2 == 1 ? face-1 : face+1), 0, 4-dir); //recursion for that far slice
            return;
        }

        int tface = face, tslice = slice, tdir = dir;
        if(face%2 == 1){
            tface = face-1;
            tslice = size-slice-1;
            tdir = 4-dir;
        }

        for(int d=0; d<tdir; d++){
            for(int i=0; i<size; i++){
                if(tface == 0) //doing F
                    RJT_Utils.cycle(state[5][size-tslice-1][size-i-1], state[3][size-i-1][tslice], state[4][tslice][i], state[2][i][size-tslice-1]);
                else if(tface == 2) // doing L
                    RJT_Utils.cycle(state[5][i][tslice], state[0][i][tslice], state[4][i][tslice], state[1][size-i-1][size-tslice-1]);
                else if(tface == 4) // doing D
                    RJT_Utils.cycle(state[0][size-tslice-1][i], state[3][size-tslice-1][i], state[1][size-tslice-1][i], state[2][size-tslice-1][i]);
            }
        }

        if(slice == 0) // this means we need to do some pure face rotation
            for(int d=0; d<dir; d++)
                for(int i=0; i<((size+1)/2); i++)
                    for(int j=0; j<(size/2); j++)
                        RJT_Utils.cycle(state[face][i][j], state[face][j][size-i-1], state[face][size-i-1][size-j-1], state[face][size-j-1][i]);
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private void drawMegaminx(int[][][] state){
        clearImage(); Graphics2D g2d = myImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int xShift = 125, yShift = 48; // for the second/back cluster of 6 faces (was 141, 0)
        //int xCenter = 76, yCenter = 100; // 141, 120 worked for just 1 cluster
        int xCenter = (myWidth-xShift)/2, yCenter = (myHeight-yShift-20)/2 + 20;
        float radius = Math.min(myWidth, myHeight-20) * 0.112F;//24; // hard code for now
        float face_gap = 7;
        float big_radius = 2 * radius * (float)Math.cos(0.2D*Math.PI) + face_gap;
//System.err.print("xShift:" + xShift + "\n");
//System.err.print("yShift:" + yShift + "\n");
//System.err.print("xCenter:" + xCenter + "\n");
//System.err.print("yCenter:" + yCenter + "\n");
//System.err.print("radius:" + radius + "\n");

        Polygon big_pent = RJT_Utils.regular_poly(5, big_radius, true); // auxiliary: for drawing outer 5 faces of cluster
        big_pent.translate(xCenter, yCenter);

        megaminxFaces[ 0] = drawMinxFace(g2d, radius, xCenter, yCenter, false, state[0]);
        megaminxFaces[ 1] = drawMinxFace(g2d, radius, big_pent.xpoints[0], big_pent.ypoints[0], true, state[1]);
        megaminxFaces[ 2] = drawMinxFace(g2d, radius, big_pent.xpoints[1], big_pent.ypoints[1], true, state[2]);
        megaminxFaces[ 3] = drawMinxFace(g2d, radius, big_pent.xpoints[2], big_pent.ypoints[2], true, state[3]);
        megaminxFaces[ 4] = drawMinxFace(g2d, radius, big_pent.xpoints[3], big_pent.ypoints[3], true, state[4]);
        megaminxFaces[ 5] = drawMinxFace(g2d, radius, big_pent.xpoints[4], big_pent.ypoints[4], true, state[5]);

        megaminxFaces[ 6] = drawMinxFace(g2d, radius, xCenter + xShift, yCenter + yShift, false, state[6]);
        megaminxFaces[ 7] = drawMinxFace(g2d, radius, big_pent.xpoints[0] + xShift, big_pent.ypoints[0] + yShift, true, state[7]);
        megaminxFaces[ 8] = drawMinxFace(g2d, radius, big_pent.xpoints[1] + xShift, big_pent.ypoints[1] + yShift, true, state[8]);
        megaminxFaces[ 9] = drawMinxFace(g2d, radius, big_pent.xpoints[2] + xShift, big_pent.ypoints[2] + yShift, true, state[9]);
        megaminxFaces[10] = drawMinxFace(g2d, radius, big_pent.xpoints[3] + xShift, big_pent.ypoints[3] + yShift, true, state[10]);
        megaminxFaces[11] = drawMinxFace(g2d, radius, big_pent.xpoints[4] + xShift, big_pent.ypoints[4] + yShift, true, state[11]);

        repaint();
    }

//**********************************************************************************************************************

    private Polygon drawMinxFace(Graphics2D g2d, float r, int x_offset, int y_offset, boolean pointup, int[][] state){
        Polygon pent = RJT_Utils.regular_poly(5, r, pointup);
        pent.translate(x_offset, y_offset);

        int xs[][] = new int[5][2], ys[][] = new int[5][2]; // the 10 points that are on the edges
        for(int i=0; i<5; i++){
            xs[i][0] = (int)Math.round(0.45F*pent.xpoints[(i+1)%5] + 0.55F*pent.xpoints[i]);
            ys[i][0] = (int)Math.round(0.45F*pent.ypoints[(i+1)%5] + 0.55F*pent.ypoints[i]);
            xs[i][1] = (int)Math.round(0.55F*pent.xpoints[(i+1)%5] + 0.45F*pent.xpoints[i]);
            ys[i][1] = (int)Math.round(0.55F*pent.ypoints[(i+1)%5] + 0.45F*pent.ypoints[i]);
        }

        Point inside_pent[] = new Point[5]; // for internal pentagon, i.e. center
        for(int i=0; i<5; i++)
            inside_pent[i] = RJT_Utils.intersectionPoint(
                        xs[(i+0)%5][0], ys[(i+0)%5][0],
                        xs[(i+3)%5][1], ys[(i+3)%5][1],
                        xs[(i+1)%5][0], ys[(i+1)%5][0],
                        xs[(i+4)%5][1], ys[(i+4)%5][1]);

        Polygon stickers[] = new Polygon[11];
        for(int i=0; i<11; i++) stickers[i] = new Polygon();
        for(int i=0; i<5; i++){ // repeat for each set
            // corner sticker
            stickers[2*i].addPoint(pent.xpoints[i], pent.ypoints[i]);
            stickers[2*i].addPoint(xs[i][0], ys[i][0]);
            stickers[2*i].addPoint(inside_pent[i].x, inside_pent[i].y);
            stickers[2*i].addPoint(xs[(i+4)%5][1], ys[(i+4)%5][1]);
            // edge sticker
            stickers[2*i+1].addPoint(xs[i][0], ys[i][0]);
            stickers[2*i+1].addPoint(xs[i][1], ys[i][1]);
            stickers[2*i+1].addPoint(inside_pent[(i+1)%5].x, inside_pent[(i+1)%5].y);
            stickers[2*i+1].addPoint(inside_pent[i].x, inside_pent[i].y);
            // center sticker
            stickers[10].addPoint(inside_pent[i].x, inside_pent[i].y);
        }

        for(int i=0; i<11; i++){
            g2d.setColor(megaminxColors[state[i][0]]);
            g2d.fillPolygon(stickers[i]); // fill each sticker
        }
        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(3F));
        g2d.drawPolygon(pent); // draw the outer pentagon
        g2d.setStroke(new BasicStroke(1.5F));
        for(int i=0; i<5; i++) // now draw the 5 lines inside
            g2d.drawLine(xs[i][0], ys[i][0], xs[(i+3)%5][1], ys[(i+3)%5][1]);

        return pent;
    }

//**********************************************************************************************************************

    private void scrambleMegaminx(){
        StringTokenizer moves = new StringTokenizer(myScramble);
        String move = "null";
        boolean failed = false;

        int[][][] state = new int[12][11][1];
        for(int face=0; face<12; face++)
            for(int i=0; i<11; i++)
                state[face][i][0] = face;

        while(moves.hasMoreTokens()){
            move = moves.nextToken();
            int dir = 1;
            //     if(move.endsWith("++")){move = move.substring(0, move.length()-2); dir = 2;}
            //else if(move.endsWith("+")){move = move.substring(0, move.length()-1); dir = 1;}
            //else if(move.endsWith("--")){move = move.substring(0, move.length()-2); dir = 3;}
            //else if(move.endsWith("-")){move = move.substring(0, move.length()-1); dir = 4;}
                 if(move.endsWith("1")){move = move.substring(0, move.length()-1); dir = 1;}
            else if(move.endsWith("2")){move = move.substring(0, move.length()-1); dir = 2;}
            else if(move.endsWith("3")){move = move.substring(0, move.length()-1); dir = 3;}
            else if(move.endsWith("4")){move = move.substring(0, move.length()-1); dir = 4;}
            //else if(move.endsWith("'")){move = move.substring(0, move.length()-1); dir = 4;}

            if(move.equals("R+")){
                doMinxFaceTurn(state, 8, 1); doMinxSliceAssist(state, 8, 1); doMinxSliceAssist(state, 5, 4);
            }
            else if(move.equals("R++")){
                doMinxFaceTurn(state, 8, 2); doMinxSliceAssist(state, 8, 2); doMinxSliceAssist(state, 5, 3);
            }
            else if(move.equals("R--")){
                doMinxFaceTurn(state, 8, 3); doMinxSliceAssist(state, 8, 3); doMinxSliceAssist(state, 5, 2);
            }
            else if(move.equals("R-")){
                doMinxFaceTurn(state, 8, 4); doMinxSliceAssist(state, 8, 4); doMinxSliceAssist(state, 5, 1);
            }
            else if(move.equals("D+")){
                doMinxFaceTurn(state, 7, 1); doMinxSliceAssist(state, 7, 1); doMinxSliceAssist(state, 1, 4);
            }
            else if(move.equals("D++")){
                doMinxFaceTurn(state, 7, 2); doMinxSliceAssist(state, 7, 2); doMinxSliceAssist(state, 1, 3);
            }
            else if(move.equals("D--")){
                doMinxFaceTurn(state, 7, 3); doMinxSliceAssist(state, 7, 3); doMinxSliceAssist(state, 1, 2);
            }
            else if(move.equals("D-")){
                doMinxFaceTurn(state, 7, 4); doMinxSliceAssist(state, 7, 4); doMinxSliceAssist(state, 1, 1);
            }
            else if(move.equals("U") || move.equals("Y+")) doMinxFaceTurn(state, 1, 1);
            else if(move.equals("U'") || move.equals("Y-")) doMinxFaceTurn(state, 1, 4);
            else if(move.equals("Y++")) doMinxFaceTurn(state, 1, 2);
            else if(move.equals("Y--")) doMinxFaceTurn(state, 1, 3);
            else if(move.equals("A")) doMinxFaceTurn(state, 0, dir);
            else if(move.equals("B")) doMinxFaceTurn(state, 1, dir);
            else if(move.equals("C")) doMinxFaceTurn(state, 2, dir);
            else if(move.equals("D")) doMinxFaceTurn(state, 3, dir);
            else if(move.equals("E")) doMinxFaceTurn(state, 4, dir);
            else if(move.equals("F")) doMinxFaceTurn(state, 5, dir);
            else if(move.equals("a")) doMinxFaceTurn(state, 6, dir);
            else if(move.equals("b")) doMinxFaceTurn(state, 7, dir);
            else if(move.equals("f")) doMinxFaceTurn(state, 8, dir); // c,d,e,f reversed, this is correct
            else if(move.equals("e")) doMinxFaceTurn(state, 9, dir);
            else if(move.equals("d")) doMinxFaceTurn(state,10, dir);
            else if(move.equals("c")) doMinxFaceTurn(state,11, dir);
            else{failed = true; break;}
        }

        if(failed)
            JOptionPane.showMessageDialog(this, "Scramble View encountered bad token for " + myPuzzle + ": <"+ move + ">.");
        else
            drawMegaminx(state);
    }

//**********************************************************************************************************************

    // dir = number of turns 1/5 turns clockwise
    private void doMinxFaceTurn(int[][][] state, int face, int dir){
        int plus6 = (face < 6 ? 0 : 6);
        dir %= 5;
        for(int d=0; d<dir; d++){
            switch(face%6){
                case 0:
                    helperMinxFaceTurn(state, plus6, 1, 2, 3, 4, 5,
                                                     4, 6, 8, 0, 2);
                    break;
                case 1:
                    helperMinxFaceTurn(state, plus6, 0, 5,10, 9, 2,
                                                     4, 0, 4, 4, 8);
                    break;
                case 2:
                    helperMinxFaceTurn(state, plus6, 0, 1, 9, 8, 3,
                                                     6, 2, 2, 2, 0);
                    break;
                case 3:
                    helperMinxFaceTurn(state, plus6, 0, 2, 8, 7, 4,
                                                     8, 4, 0, 0, 2);
                    break;
                case 4:
                    helperMinxFaceTurn(state, plus6, 0, 3, 7,11, 5,
                                                     0, 6, 8, 8, 4);
                    break;
                case 5:
                    helperMinxFaceTurn(state, plus6, 0, 4,11,10, 1,
                                                     2, 8, 6, 6, 6);
                    break;
            }

            //pure face rotation time
            RJT_Utils.cycle(state[face][0], state[face][2], state[face][4], state[face][6], state[face][8]); // corners
            RJT_Utils.cycle(state[face][1], state[face][3], state[face][5], state[face][7], state[face][9]); // edges
        }
    }

//**********************************************************************************************************************

    private void helperMinxFaceTurn(int[][][] state, int plus6,
                                        int f0, int f1, int f2, int f3, int f4,
                                        int x0, int x1, int x2, int x3, int x4){
        for(int i=0; i<3; i++) // the three stickers on the edge, so like c/e/c
            RJT_Utils.cycle(state[(f0+plus6)%12][(x0+i)%10],
                            state[(f1+plus6)%12][(x1+i)%10],
                            state[(f2+plus6)%12][(x2+i)%10],
                            state[(f3+plus6)%12][(x3+i)%10],
                            state[(f4+plus6)%12][(x4+i)%10]);
    }

//**********************************************************************************************************************

    private void doMinxSliceAssist(int[][][] state, int face, int dir){
        int plus6 = (face < 6 ? 0 : 6);
        dir %= 5;
        for(int d=0; d<dir; d++){
            switch(face%6){
                case 0:
                    helperMinxSliceAssist(state, plus6, 1, 2, 3, 4, 5,
                                                        4, 6, 8, 0, 2);
                    break;
                case 1:
                    helperMinxSliceAssist(state, plus6, 0, 5,10, 9, 2,
                                                        4, 0, 4, 4, 8);
                    break;
                case 2:
                    helperMinxSliceAssist(state, plus6, 0, 1, 9, 8, 3,
                                                        6, 2, 2, 2, 0);
                    break;
                case 3:
                    helperMinxSliceAssist(state, plus6, 0, 2, 8, 7, 4,
                                                        8, 4, 0, 0, 2);
                    break;
                case 4:
                    helperMinxSliceAssist(state, plus6, 0, 3, 7,11, 5,
                                                        0, 6, 8, 8, 4);
                    break;
                case 5:
                    helperMinxSliceAssist(state, plus6, 0, 4,11,10, 1,
                                                        2, 8, 6, 6, 6);
                    break;
            }
        }
    }

//**********************************************************************************************************************

    private void helperMinxSliceAssist(int[][][] state, int plus6,
                                        int f0, int f1, int f2, int f3, int f4,
                                        int x0, int x1, int x2, int x3, int x4){
        RJT_Utils.cycle(state[(f0+plus6)%12][10],
                        state[(f1+plus6)%12][10],
                        state[(f2+plus6)%12][10],
                        state[(f3+plus6)%12][10],
                        state[(f4+plus6)%12][10]); // centers get cycled

        for(int i=0; i<7; i++) // for each of the 7 non-centers on the face
            RJT_Utils.cycle(state[(f0+plus6)%12][(x0+i+3)%10],
                            state[(f1+plus6)%12][(x1+i+3)%10],
                            state[(f2+plus6)%12][(x2+i+3)%10],
                            state[(f3+plus6)%12][(x3+i+3)%10],
                            state[(f4+plus6)%12][(x4+i+3)%10]);
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private void drawPyraminx(int[][][] state){
        clearImage(); Graphics2D g2d = myImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int xCenter = myWidth/2;//141;
        int yCenter = myHeight/2-19;//98;
        float radius = Math.min(myWidth, myHeight-20) * 0.24F;//52;
        float face_gap = 7;
        float big_radius = radius + face_gap;//2 * radius * Math.cos(Math.PI/3) + face_gap;
//System.err.print("xCenter:" + xCenter + "\n");
//System.err.print("yCenter:" + yCenter + "\n");
//System.err.print("radius:" + radius + "\n");

        Polygon big_tri = RJT_Utils.regular_poly(3, big_radius, false); // auxiliary: for drawing the outer 3 faces
        big_tri.translate(xCenter, yCenter);

        pyraminxFaces[0] = drawPyraFace(g2d, radius, xCenter, yCenter, true, state[0]);
        pyraminxFaces[1] = drawPyraFace(g2d, radius, big_tri.xpoints[2], big_tri.ypoints[2], false, state[1]);
        pyraminxFaces[2] = drawPyraFace(g2d, radius, big_tri.xpoints[0], big_tri.ypoints[0], false, state[2]);
        pyraminxFaces[3] = drawPyraFace(g2d, radius, big_tri.xpoints[1], big_tri.ypoints[1], false, state[3]);

        repaint();
    }

//**********************************************************************************************************************

    private Polygon drawPyraFace(Graphics2D g2d, float r, int x_offset, int y_offset, boolean pointup, int[][] state){
        Polygon tri = RJT_Utils.regular_poly(3, r, pointup);
        tri.translate(x_offset, y_offset);

        int xs[][] = new int[3][2], ys[][] = new int[3][2]; // the 6 points that are on the edges
        for(int i=0; i<3; i++){
            xs[i][0] = (int)Math.round(1F*tri.xpoints[(i+1)%3]/3F + 2F*tri.xpoints[i]/3F);
            ys[i][0] = (int)Math.round(1F*tri.ypoints[(i+1)%3]/3F + 2F*tri.ypoints[i]/3F);
            xs[i][1] = (int)Math.round(2F*tri.xpoints[(i+1)%3]/3F + 1F*tri.xpoints[i]/3F);
            ys[i][1] = (int)Math.round(2F*tri.ypoints[(i+1)%3]/3F + 1F*tri.ypoints[i]/3F);
        }

        Polygon stickers[] = new Polygon[9];
        for(int i=0; i<9; i++) stickers[i] = new Polygon();
        for(int i=0; i<3; i++){ // repeat for each set
            // tip sticker
            stickers[i+6].addPoint(tri.xpoints[i], tri.ypoints[i]);
            stickers[i+6].addPoint(xs[i][0], ys[i][0]);
            stickers[i+6].addPoint(xs[(i+2)%3][1], ys[(i+2)%3][1]);
            // center sticker
            stickers[2*i].addPoint(xs[i][0], ys[i][0]);
            stickers[2*i].addPoint(x_offset, y_offset); // not such a robust choice...
            stickers[2*i].addPoint(xs[(i+2)%3][1], ys[(i+2)%3][1]);
            // edge sticker
            stickers[2*i+1].addPoint(xs[i][0], ys[i][0]);
            stickers[2*i+1].addPoint(xs[i][1], ys[i][1]);
            stickers[2*i+1].addPoint(x_offset, y_offset); // not such a robust choice...
        }

        for(int i=0; i<9; i++){
            g2d.setColor(pyraminxColors[state[i][0]]);
            g2d.fillPolygon(stickers[i]); // fill each sticker
        }
        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(3F));
        g2d.drawPolygon(tri); // draw the outer triangle
        g2d.setStroke(new BasicStroke(1.5F));
        for(int i=0; i<3; i++) // draw 3 long lines inside
            g2d.drawLine(xs[i][0], ys[i][0], xs[(i+1)%3][1], ys[(i+1)%3][1]);
        for(int i=0; i<3; i++) // draw 3 short lines inside
            g2d.drawLine(xs[i][0], ys[i][0], xs[(i+2)%3][1], ys[(i+2)%3][1]);

        return tri;
    }

//**********************************************************************************************************************

    private void scramblePyraminx(){
        StringTokenizer moves = new StringTokenizer(myScramble);
        String move = "null";
        boolean failed = false;

        // 6,7,8 are tip stickers, other evens are face stickers, odds are edge stickers
        int[][][] state = new int[4][9][1];
        for(int face=0; face<4; face++)
            for(int i=0; i<9; i++)
                state[face][i][0] = face;

        while(moves.hasMoreTokens()){
            move = moves.nextToken();
            int dir = 1;
            if(move.endsWith("'")){move = move.substring(0, move.length()-1); dir = 2;}

                 if(move.equals("U")) doPyraCoreTurn(state, 0, dir);
            else if(move.equals("L")) doPyraCoreTurn(state, 1, dir);
            else if(move.equals("R")) doPyraCoreTurn(state, 2, dir);
            else if(move.equals("B")) doPyraCoreTurn(state, 3, dir);

            else if(move.equals("u")) doPyraTipsTurn(state, 0, dir);
            else if(move.equals("l")) doPyraTipsTurn(state, 1, dir);
            else if(move.equals("r")) doPyraTipsTurn(state, 2, dir);
            else if(move.equals("b")) doPyraTipsTurn(state, 3, dir);
            else{failed = true; break;}
        }

        if(failed)
            JOptionPane.showMessageDialog(this, "Scramble View encountered bad token for " + myPuzzle + ": <"+ move + ">.");
        else
            drawPyraminx(state);
    }

//**********************************************************************************************************************

    // dir = number of turns 1/3 turns clockwise
    private void doPyraCoreTurn(int[][][] state, int face, int dir){
        dir %= 3;
        for(int d=0; d<dir; d++)
            switch(face){
                case 0:
                    for(int i=0; i<3; i++)
                        RJT_Utils.cycle(state[0][(5+i)%6], state[3][(3+i)%6], state[1][(1+i)%6]);
                    break;
                case 1:
                    for(int i=0; i<3; i++)
                        RJT_Utils.cycle(state[0][(3+i)%6], state[2][(1+i)%6], state[3][(5+i)%6]);
                    break;
                case 2:
                    for(int i=0; i<3; i++)
                        RJT_Utils.cycle(state[0][(1+i)%6], state[1][(5+i)%6], state[2][(3+i)%6]);
                    break;
                case 3:
                    for(int i=0; i<3; i++)
                        RJT_Utils.cycle(state[2][(5+i)%6], state[1][(3+i)%6], state[3][(1+i)%6]);
                    break;
            }

        doPyraTipsTurn(state, face, dir);
    }

//**********************************************************************************************************************

    // dir = number of turns 1/3 turns clockwise
    private void doPyraTipsTurn(int[][][] state, int face, int dir){
        dir %= 3;
        for(int d=0; d<dir; d++)
            switch(face){
                case 0: RJT_Utils.cycle(state[0][6], state[3][8], state[1][7]); break;
                case 1: RJT_Utils.cycle(state[0][8], state[2][7], state[3][6]); break;
                case 2: RJT_Utils.cycle(state[0][7], state[1][6], state[2][8]); break;
                case 3: RJT_Utils.cycle(state[2][6], state[1][8], state[3][7]); break;
            }
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private static final Polygon square_poly(int n){
        Polygon square = new Polygon();
        square.addPoint(0, 0);
        square.addPoint(n, 0);
        square.addPoint(n, n);
        square.addPoint(0, n);
        return square;
    }

//**********************************************************************************************************************

    private static final Polygon square_polyX(int n){
        Polygon square = new Polygon();
        float r = n*(float)Math.sqrt(0.5);
        float offset = (float)(-13*Math.PI/16);
        for(int i=0; i<4; i++)
            square.addPoint((int)Math.round(r*Math.cos(i*2*Math.PI/4 + offset)),
                          (int)Math.round(r*Math.sin(i*2*Math.PI/4 + offset)));
        square.translate(n/2, n/2);
        return square;
    }

//**********************************************************************************************************************

    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(myImage, 0, 0, null);
    }
}
