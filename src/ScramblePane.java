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
import java.util.*;
import javax.swing.border.Border;
import java.awt.image.BufferedImage;

public class ScramblePane extends JPanel{
    private static final int MIN_ORDER = 2, MAX_ORDER = 5; // constants
    private Color[] cubeColors = new Color[6];
    //private Color[] pyraminxColors = new Color[4];
    //private Color[] megaminxColors = new Color[12];
    private JTextArea[][][][] CubeFace, CubePrev;
    private BufferedImage myImage;
    private int myWidth, myHeight; // would prefer to get these with function calls, but they don't work

//**********************************************************************************************************************

    public ScramblePane(int width, int height){
        myWidth = width; myHeight = height; // needs gettin' rid of
        CubeFace = new JTextArea[MAX_ORDER+1][][][];
        CubePrev = new JTextArea[MAX_ORDER+1][][][]; // ignoring 0x0, and 1x1 case (although 1x1 would work)
        for(int side=0; side<6; side++) cubeColors[side] = Color.black; // just incase...
        //for(int side=0; side<12; side++) megaminxColors[side] = Color.black; // just incase...

        for(int order=MIN_ORDER; order<=MAX_ORDER; order++){
            prepareCube(order);
            setCubeBounds(order);
            //add to contentPane
            for(int side=0; side<6; side++)
                for(int i=0; i<order; i++)
                    for(int j=0; j<order; j++)
                       add(CubeFace[order][side][i][j]);
        }
        clearScreen();
    }

//**********************************************************************************************************************

    public void newScramble(String puzzle, String scrambleAlg){
        clearScreen();
             if(puzzle.equals("2x2x2")) scrambleCube(2, puzzle, scrambleAlg);
        else if(puzzle.equals("3x3x3")) scrambleCube(3, puzzle, scrambleAlg);
        else if(puzzle.equals("4x4x4")) scrambleCube(4, puzzle, scrambleAlg);
        else if(puzzle.equals("5x5x5")) scrambleCube(5, puzzle, scrambleAlg);
        else if(puzzle.equals("Pyraminx")) scramblePyraminx(puzzle, scrambleAlg);
        else if(puzzle.equals("Megaminx")) scrambleMegaminx(puzzle, scrambleAlg);
    }

//**********************************************************************************************************************

    private void clearScreen(){
        for(int order=MIN_ORDER; order<=MAX_ORDER; order++)
            setCubeVisible(order, false);
        myImage = new BufferedImage(myWidth, myHeight, BufferedImage.TYPE_INT_ARGB); // should clear it...
        repaint();
    }

//**********************************************************************************************************************

    public void setCubeColors(Color[] newColors){
        cubeColors = newColors;
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private void prepareCube(int order){
        CubeFace[order] = new JTextArea[6][order][order];
        CubePrev[order] = new JTextArea[6][order][order];

        for(int side=0; side<6; side++)
            for(int i=0; i<order; i++)
                for(int j=0; j<order; j++){
                    CubeFace[order][side][i][j] = new JTextArea();
                    CubeFace[order][side][i][j].setEditable(false);
                    CubeFace[order][side][i][j].setFocusable(false);
                    CubeFace[order][side][i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                    CubePrev[order][side][i][j] = new JTextArea();
                    CubePrev[order][side][i][j].setEditable(false);
                    CubePrev[order][side][i][j].setFocusable(false);
                    CubePrev[order][side][i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                }
    }

//**********************************************************************************************************************

    private void setCubeBounds(int order){
        int margin = 14;
        int face_gap = 4;
        //int face_pixels = 60;
        int face_pixels = Math.min((myWidth - 3*face_gap - 2*margin)/4, (myHeight - 2*face_gap - 2*margin)/3);
        int n = face_pixels + face_gap;
        //int x = 15, y = 19; // nudge factors
        int x = (myWidth - 4*face_pixels - 3*face_gap)/2, y = (myHeight - 3*face_pixels - 2*face_gap)/2;
        y += 5; // nudge away from title
//System.err.println("x=" + x);
//System.err.println("y=" + y);

        setCubeFaceBounds(CubeFace[order][0], order, 1*n + x ,1*n + y, face_pixels/order);
        setCubeFaceBounds(CubeFace[order][1], order, 3*n + x, 1*n + y, face_pixels/order);
        setCubeFaceBounds(CubeFace[order][2], order, 0*n + x, 1*n + y, face_pixels/order);
        setCubeFaceBounds(CubeFace[order][3], order, 2*n + x, 1*n + y, face_pixels/order);
        setCubeFaceBounds(CubeFace[order][4], order, 1*n + x, 2*n + y, face_pixels/order);
        setCubeFaceBounds(CubeFace[order][5], order, 1*n + x, 0*n + y, face_pixels/order);
    }

    private void setCubeFaceBounds(JTextArea[][] aFace, int order, int x, int y, int size){
        for(int i=0; i<order; i++)
            for(int j=0; j<order; j++)
                aFace[i][j].setBounds(j*size+x, i*size+y, size, size);
    }

//**********************************************************************************************************************

    private void resetCube(int order){
        for(int side=0; side<6; side++)
            for(int i=0; i<order; i++)
                for(int j=0; j<order; j++){
                    CubeFace[order][side][i][j].setBackground(cubeColors[side]);
                    CubePrev[order][side][i][j].setBackground(cubeColors[side]);
                }
    }

//**********************************************************************************************************************

    private void setCubeVisible(int order, boolean show){
        for(int side=0; side<6; side++)
            for(int i=0; i<order; i++)
                for(int j=0; j<order; j++)
                    CubeFace[order][side][i][j].setVisible(show);
    }

//**********************************************************************************************************************

    private void scrambleCube(int order, String puzzle, String scrambleAlg){
        StringTokenizer moves = new StringTokenizer(scrambleAlg);
        String currentMove = "null";
        boolean failed = false;
        resetCube(order);

        while(moves.hasMoreTokens()){
            currentMove = moves.nextToken();
            int dir = 1;
            if(currentMove.endsWith("'")){currentMove = currentMove.substring(0, currentMove.length()-1); dir = 3;}
            if(currentMove.endsWith("2")){currentMove = currentMove.substring(0, currentMove.length()-1); dir = 2;}
            //JOptionPane.showMessageDialog(this, "For " + order + "x" + order + ": <" + currentMove + "> gives " + dir + ".");

                 if(currentMove.equals("F")){doCubeTurn(order, 0, 0, dir);}
            else if(currentMove.equals("B")){doCubeTurn(order, 1, 0, dir);}
            else if(currentMove.equals("L")){doCubeTurn(order, 2, 0, dir);}
            else if(currentMove.equals("R")){doCubeTurn(order, 3, 0, dir);}
            else if(currentMove.equals("D")){doCubeTurn(order, 4, 0, dir);}
            else if(currentMove.equals("U")){doCubeTurn(order, 5, 0, dir);}
            else if(currentMove.equals("x")){for(int slice=0; slice<order; slice++) doCubeTurn(order, 3, slice, dir);}
            else if(currentMove.equals("y")){for(int slice=0; slice<order; slice++) doCubeTurn(order, 5, slice, dir);}
            else if(currentMove.equals("z")){for(int slice=0; slice<order; slice++) doCubeTurn(order, 0, slice, dir);}

            else if(order < 3){failed = true; break;}
            else if(currentMove.equals("f")){doCubeTurn(order, 0, 1, dir); if(order == 3) doCubeTurn(order, 0, 0, dir);}
            else if(currentMove.equals("b")){doCubeTurn(order, 1, 1, dir); if(order == 3) doCubeTurn(order, 1, 0, dir);}
            else if(currentMove.equals("l")){doCubeTurn(order, 2, 1, dir); if(order == 3) doCubeTurn(order, 2, 0, dir);}
            else if(currentMove.equals("r")){doCubeTurn(order, 3, 1, dir); if(order == 3) doCubeTurn(order, 3, 0, dir);}
            else if(currentMove.equals("d")){doCubeTurn(order, 4, 1, dir); if(order == 3) doCubeTurn(order, 4, 0, dir);}
            else if(currentMove.equals("u")){doCubeTurn(order, 5, 1, dir); if(order == 3) doCubeTurn(order, 5, 0, dir);}
            else if(currentMove.equals("M")){for(int slice=1; slice<order-1; slice++) doCubeTurn(order, 2, slice, dir);}
            else if(currentMove.equals("E")){for(int slice=1; slice<order-1; slice++) doCubeTurn(order, 4, slice, dir);}
            else if(currentMove.equals("S")){for(int slice=1; slice<order-1; slice++) doCubeTurn(order, 0, slice, dir);}
            else if(currentMove.equals("Fw")){doCubeTurn(order, 0, 0, dir); doCubeTurn(order, 0, 1, dir);}
            else if(currentMove.equals("Bw")){doCubeTurn(order, 1, 0, dir); doCubeTurn(order, 1, 1, dir);}
            else if(currentMove.equals("Lw")){doCubeTurn(order, 2, 0, dir); doCubeTurn(order, 2, 1, dir);}
            else if(currentMove.equals("Rw")){doCubeTurn(order, 3, 0, dir); doCubeTurn(order, 3, 1, dir);}
            else if(currentMove.equals("Dw")){doCubeTurn(order, 4, 0, dir); doCubeTurn(order, 4, 1, dir);}
            else if(currentMove.equals("Uw")){doCubeTurn(order, 5, 0, dir); doCubeTurn(order, 5, 1, dir);}

            else if((order < 5) || (order%2 == 0)){failed = true; break;}
            else if(currentMove.equals("m")){doCubeTurn(order, 2, (order-1)/2, dir);}
            else if(currentMove.equals("e")){doCubeTurn(order, 4, (order-1)/2, dir);}
            else if(currentMove.equals("s")){doCubeTurn(order, 0, (order-1)/2, dir);}
            else{failed = true; break;}
        }

        if(failed)
            JOptionPane.showMessageDialog(this, "Scramble View encountered bad token for " + puzzle + ": <"+ currentMove + ">.");
        setCubeVisible(order, !failed);
    }

//**********************************************************************************************************************

    // dir = 1 for 90 deg, 2 for 180 deg, 3 for 270 deg
    private void doCubeTurn(int order, int side, int slice, int dir){
        if(side<0 || side>5){
            JOptionPane.showMessageDialog(this, "Function doCubeTurn called with side=" + side + ".");
            return;
        }
        dir %= 4;
        if(dir == 0) return;
        if(slice > order-1) return;
        if(slice == order-1){ //far slice, mostly to help handle whole cube rotation
            doCubeTurn(order, (side%2 == 1 ? side-1 : side+1), 0, 4-dir); //recursion for that far slice
            return;
        }

        int tside = side, tslice = slice, tdir = dir;
        if(side%2 == 1){
            tside = side-1;
            tslice = order-slice-1;
            tdir = 4-dir;
        }

        JTextArea[][][] xFace = CubeFace[order];
        JTextArea[][][] xPrev = CubePrev[order];
        for(int d=0; d<tdir; d++){
            for(int i=0; i<order; i++)
                if(tside == 0){ //doing F
                    xFace[2][i][order-tslice-1].setBackground(xPrev[4][tslice][i].getBackground()); // L is what D was
                    xFace[3][order-i-1][tslice].setBackground(xPrev[5][order-tslice-1][order-i-1].getBackground()); // R is what U was
                    xFace[4][tslice][i].setBackground(xPrev[3][order-i-1][tslice].getBackground()); // D is what R was
                    xFace[5][order-tslice-1][order-i-1].setBackground(xPrev[2][i][order-tslice-1].getBackground()); // U is what L was
                } else if(tside == 2){ // doing L
                    xFace[0][i][tslice].setBackground(xPrev[5][i][tslice].getBackground()); // F is what U was
                    xFace[1][order-i-1][order-tslice-1].setBackground(xPrev[4][i][tslice].getBackground()); // B is what D was
                    xFace[4][i][tslice].setBackground(xPrev[0][i][tslice].getBackground()); // D is what F was
                    xFace[5][i][tslice].setBackground(xPrev[1][order-i-1][order-tslice-1].getBackground()); // U is what B was
                } else if(tside == 4){ // doing D
                    xFace[0][order-tslice-1][i].setBackground(xPrev[2][order-tslice-1][i].getBackground()); // F is what L was
                    xFace[1][order-tslice-1][i].setBackground(xPrev[3][order-tslice-1][i].getBackground()); // B is what R was
                    xFace[2][order-tslice-1][i].setBackground(xPrev[1][order-tslice-1][i].getBackground()); // L is what B was
                    xFace[3][order-tslice-1][i].setBackground(xPrev[0][order-tslice-1][i].getBackground()); // R is what F was
                }
            updatePreviousCube(order);
        }

        if(slice == 0) // this means we need to do some pure face rotation
            for(int d=0; d<dir; d++){
                for(int i=0; i<order; i++)
                    for(int j=0; j<order; j++)
                        xFace[side][i][j].setBackground(xPrev[side][order-j-1][i].getBackground());
                updatePreviousCube(order);
            }
    }

//**********************************************************************************************************************

    private void updatePreviousCube(int order){
        for(int side=0; side<6; side++)
            for(int i=0; i<order; i++)
                for(int j=0; j<order; j++)
                    CubePrev[order][side][i][j].setBackground(CubeFace[order][side][i][j].getBackground());
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private Color[] megaminxColors = {  new Color(255,255,255), // white
                                        new Color(0,180,255), // powder blue
                                        new Color(200,128,0), // brown
                                        new Color(255,0,0), // red
                                        new Color(255,255,0), // yellow
                                        new Color(0,255,0), // bright green
                                        new Color(160,0,255), // purple
                                        new Color(0,0,210), // dark blue
                                        new Color(0,128,0), // dark green
                                        new Color(255,80,80), // pink
                                        new Color(255,180,180), // light pink
                                        new Color(255,128,0)}; // orange

    private void drawMegaminx(int state[][][]){
        myImage = new BufferedImage(myWidth, myHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = myImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(1.5F));

        int xCenter = 76, yCenter = 90; // 141,120 worked for just 1 cluster
        int xShift = 125, yShift = 58; // for the second/back cluster of 6 faces (was 141,0)
        double radius = 25;
        double face_gap = 4;
        double big_radius = 2 * radius * Math.cos(0.2D*Math.PI) + face_gap;

        Polygon big_pent = regular_poly(5, big_radius, true); // auxiliary: for drawing outer 5 faces of cluster
        big_pent.translate(xCenter, yCenter);

        drawMinxFace(g2d, radius, xCenter, yCenter, false, state[0]);
        drawMinxFace(g2d, radius, big_pent.xpoints[0], big_pent.ypoints[0], true, state[1]);
        drawMinxFace(g2d, radius, big_pent.xpoints[1], big_pent.ypoints[1], true, state[2]);
        drawMinxFace(g2d, radius, big_pent.xpoints[2], big_pent.ypoints[2], true, state[3]);
        drawMinxFace(g2d, radius, big_pent.xpoints[3], big_pent.ypoints[3], true, state[4]);
        drawMinxFace(g2d, radius, big_pent.xpoints[4], big_pent.ypoints[4], true, state[5]);
        drawMinxFace(g2d, radius, xCenter + xShift, yCenter + yShift, false, state[6]);
        drawMinxFace(g2d, radius, big_pent.xpoints[0] + xShift, big_pent.ypoints[0] + yShift, true, state[7]);
        drawMinxFace(g2d, radius, big_pent.xpoints[1] + xShift, big_pent.ypoints[1] + yShift, true, state[8]);
        drawMinxFace(g2d, radius, big_pent.xpoints[2] + xShift, big_pent.ypoints[2] + yShift, true, state[9]);
        drawMinxFace(g2d, radius, big_pent.xpoints[3] + xShift, big_pent.ypoints[3] + yShift, true, state[10]);
        drawMinxFace(g2d, radius, big_pent.xpoints[4] + xShift, big_pent.ypoints[4] + yShift, true, state[11]);

        repaint();
    }

//**********************************************************************************************************************

    private void drawMinxFace(Graphics2D g2d, double r, int x_offset, int y_offset, boolean pointup, int state[][]){
        Polygon pent = regular_poly(5, r, pointup);
        pent.translate(x_offset, y_offset);

        int xs[] = new int[10], ys[] = new int[10]; // the 10 points that are on the edges
        for(int i=0; i<5; i++){
            xs[i] = (int)Math.round(0.45D*pent.xpoints[(i+1)%5] + 0.55D*pent.xpoints[i]);
            ys[i] = (int)Math.round(0.45D*pent.ypoints[(i+1)%5] + 0.55D*pent.ypoints[i]);
            xs[5+i] = (int)Math.round(0.55D*pent.xpoints[(i+1)%5] + 0.45D*pent.xpoints[i]);
            ys[5+i] = (int)Math.round(0.55D*pent.ypoints[(i+1)%5] + 0.45D*pent.ypoints[i]);
        }

        Point inside_pent[] = new Point[5]; // for internal pentagon, i.e. center
        for(int i=0; i<5; i++)
            inside_pent[i] = getLineIntersection(   xs[i], ys[i],
                                                    xs[5 + (3+i)%5], ys[5 + (3+i)%5],
                                                    xs[(i+1)%5], ys[(i+1)%5],
                                                    xs[5 + (4+i)%5], ys[5 + (4+i)%5]);

        Polygon stickers[] = new Polygon[11];
        for(int i=0; i<11; i++) stickers[i] = new Polygon();
        for(int i=0; i<5; i++){ // repeat for each set
            // corner sticker
            stickers[2*i].addPoint(pent.xpoints[i], pent.ypoints[i]);
            stickers[2*i].addPoint(xs[i], ys[i]);
            stickers[2*i].addPoint(inside_pent[i].x, inside_pent[i].y);
            stickers[2*i].addPoint(xs[5 + (i+4)%5], ys[5 + (i+4)%5]);
            // edge sticker
            stickers[2*i+1].addPoint(xs[i], ys[i]);
            stickers[2*i+1].addPoint(xs[5 + i], ys[5 + i]);
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
        g2d.drawPolygon(pent); // draw the outer pentagon
        for(int i=0; i<5; i++) // now draw the 5 lines inside
            g2d.drawLine(xs[i], ys[i], xs[5 + (i+3)%5], ys[5 + (i+3)%5]);
    }

//**********************************************************************************************************************

    private void scrambleMegaminx(String puzzle, String scrambleAlg){
        StringTokenizer moves = new StringTokenizer(scrambleAlg);
        String currentMove = "null";
        boolean failed = false;

        int state[][][] = new int[12][11][1];
        for(int side=0; side<12; side++)
            for(int i=0; i<11; i++)
                state[side][i][0] = side;

        while(moves.hasMoreTokens()){
            currentMove = moves.nextToken();
            int dir = 1;
            //     if(currentMove.endsWith("++")){currentMove = currentMove.substring(0, currentMove.length()-2); dir = 2;}
            //else if(currentMove.endsWith("+")){currentMove = currentMove.substring(0, currentMove.length()-1); dir = 1;}
            //else if(currentMove.endsWith("--")){currentMove = currentMove.substring(0, currentMove.length()-2); dir = 3;}
            //else if(currentMove.endsWith("-")){currentMove = currentMove.substring(0, currentMove.length()-1); dir = 4;}
                 if(currentMove.endsWith("1")){currentMove = currentMove.substring(0, currentMove.length()-1); dir = 1;}
            else if(currentMove.endsWith("2")){currentMove = currentMove.substring(0, currentMove.length()-1); dir = 2;}
            else if(currentMove.endsWith("3")){currentMove = currentMove.substring(0, currentMove.length()-1); dir = 3;}
            else if(currentMove.endsWith("4")){currentMove = currentMove.substring(0, currentMove.length()-1); dir = 4;}
            //else if(currentMove.endsWith("'")){currentMove = currentMove.substring(0, currentMove.length()-1); dir = 4;}

            if(currentMove.equals("R+")){
                doMinxFaceTurn(state, 8, 1); doMinxSliceAssist(state, 8, 1); doMinxSliceAssist(state, 5, 4);
            }
            else if(currentMove.equals("R++")){
                doMinxFaceTurn(state, 8, 2); doMinxSliceAssist(state, 8, 2); doMinxSliceAssist(state, 5, 3);
            }
            else if(currentMove.equals("R--")){
                doMinxFaceTurn(state, 8, 3); doMinxSliceAssist(state, 8, 3); doMinxSliceAssist(state, 5, 2);
            }
            else if(currentMove.equals("R-")){
                doMinxFaceTurn(state, 8, 4); doMinxSliceAssist(state, 8, 4); doMinxSliceAssist(state, 5, 1);
            }
            else if(currentMove.equals("D+")){
                doMinxFaceTurn(state, 7, 1); doMinxSliceAssist(state, 7, 1); doMinxSliceAssist(state, 1, 4);
            }
            else if(currentMove.equals("D++")){
                doMinxFaceTurn(state, 7, 2); doMinxSliceAssist(state, 7, 2); doMinxSliceAssist(state, 1, 3);
            }
            else if(currentMove.equals("D--")){
                doMinxFaceTurn(state, 7, 3); doMinxSliceAssist(state, 7, 3); doMinxSliceAssist(state, 1, 2);
            }
            else if(currentMove.equals("D-")){
                doMinxFaceTurn(state, 7, 4); doMinxSliceAssist(state, 7, 4); doMinxSliceAssist(state, 1, 1);
            }
            else if(currentMove.equals("U") || currentMove.equals("Y+")) doMinxFaceTurn(state, 1, 1);
            else if(currentMove.equals("U'") || currentMove.equals("Y-")) doMinxFaceTurn(state, 1, 4);
            else if(currentMove.equals("Y++")) doMinxFaceTurn(state, 1, 2);
            else if(currentMove.equals("Y--")) doMinxFaceTurn(state, 1, 3);
            else if(currentMove.equals("A")) doMinxFaceTurn(state, 0, dir);
            else if(currentMove.equals("B")) doMinxFaceTurn(state, 1, dir);
            else if(currentMove.equals("C")) doMinxFaceTurn(state, 2, dir);
            else if(currentMove.equals("D")) doMinxFaceTurn(state, 3, dir);
            else if(currentMove.equals("E")) doMinxFaceTurn(state, 4, dir);
            else if(currentMove.equals("F")) doMinxFaceTurn(state, 5, dir);
            else if(currentMove.equals("a")) doMinxFaceTurn(state, 6, dir);
            else if(currentMove.equals("b")) doMinxFaceTurn(state, 7, dir);
            else if(currentMove.equals("f")) doMinxFaceTurn(state, 8, dir); // c,d,e,f reversed on CCT, this is right
            else if(currentMove.equals("e")) doMinxFaceTurn(state, 9, dir);
            else if(currentMove.equals("d")) doMinxFaceTurn(state,10, dir);
            else if(currentMove.equals("c")) doMinxFaceTurn(state,11, dir);
            else{failed = true; break;}
        }

        if(failed)
            JOptionPane.showMessageDialog(this, "Scramble View encountered bad token for " + puzzle + ": <"+ currentMove + ">.");
        else
            drawMegaminx(state);
    }

//**********************************************************************************************************************

    // dir = number of turns 1/5 turns clockwise
    private void doMinxFaceTurn(int state[][][], int side, int dir){
        int plus6 = (side < 6 ? 0 : 6);
        dir %= 5;
        for(int d=0; d<dir; d++){
            switch(side%6){
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
            cycle(state[side][0], state[side][2], state[side][4], state[side][6], state[side][8]); // corners
            cycle(state[side][1], state[side][3], state[side][5], state[side][7], state[side][9]); // edges
        }
    }

//**********************************************************************************************************************

    private void helperMinxFaceTurn(int state[][][], int plus6,
                                        int f0, int f1, int f2, int f3, int f4,
                                        int x0, int x1, int x2, int x3, int x4){
        for(int i=0; i<3; i++) // the three stickers on the edge, so like c/e/c
            cycle(  state[(f0+plus6)%12][(x0+i)%10],
                    state[(f1+plus6)%12][(x1+i)%10],
                    state[(f2+plus6)%12][(x2+i)%10],
                    state[(f3+plus6)%12][(x3+i)%10],
                    state[(f4+plus6)%12][(x4+i)%10]);
    }

//**********************************************************************************************************************

    private void doMinxSliceAssist(int state[][][], int side, int dir){
        int plus6 = (side < 6 ? 0 : 6);
        dir %= 5;
        for(int d=0; d<dir; d++){
            switch(side%6){
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

    private void helperMinxSliceAssist(int state[][][], int plus6,
                                        int f0, int f1, int f2, int f3, int f4,
                                        int x0, int x1, int x2, int x3, int x4){
        cycle(  state[(f0+plus6)%12][10],
                state[(f1+plus6)%12][10],
                state[(f2+plus6)%12][10],
                state[(f3+plus6)%12][10],
                state[(f4+plus6)%12][10]); // centers get cycled

        for(int i=0; i<7; i++) // for each of the 7 non-centers on the face
            cycle(  state[(f0+plus6)%12][(x0+i+3)%10],
                    state[(f1+plus6)%12][(x1+i+3)%10],
                    state[(f2+plus6)%12][(x2+i+3)%10],
                    state[(f3+plus6)%12][(x3+i+3)%10],
                    state[(f4+plus6)%12][(x4+i+3)%10]);
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private Color[] pyraminxColors = {  new Color(0,180,255), // powder blue
                                        new Color(255,0,0), // red
                                        new Color(255,255,0), // yellow
                                        new Color(0,255,0)}; // bright green

    private void drawPyraminx(int state[][][]){
        myImage = new BufferedImage(myWidth, myHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = myImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(1.5F));

        int xCenter = 141;//myWidth/2;
        int yCenter = 88;//myHeight/2 - 20;
        double radius = 52;//Math.min(myWidth, myHeight) * 0.2;
//System.err.println("xCenter = " + xCenter);
//System.err.println("yCenter = " + yCenter);
//System.err.println("radius = " + radius);
        double face_gap = 6;
        double big_radius = radius + face_gap;//2 * radius * Math.cos(Math.PI/3) + face_gap;

        Polygon big_tri = regular_poly(3, big_radius, false); // auxiliary: for drawing the outer 3 faces
        big_tri.translate(xCenter, yCenter);

        drawPyraFace(g2d, radius, xCenter, yCenter, true, state[0]);
        drawPyraFace(g2d, radius, big_tri.xpoints[2], big_tri.ypoints[2], false, state[1]);
        drawPyraFace(g2d, radius, big_tri.xpoints[0], big_tri.ypoints[0], false, state[2]);
        drawPyraFace(g2d, radius, big_tri.xpoints[1], big_tri.ypoints[1], false, state[3]);

        repaint();
    }

//**********************************************************************************************************************

    private void drawPyraFace(Graphics2D g2d, double r, int x_offset, int y_offset, boolean pointup, int state[][]){
        Polygon tri = regular_poly(3, r, pointup);
        tri.translate(x_offset, y_offset);

        int xs[] = new int[6], ys[] = new int[6]; // the 6 points that are on the edges
        for(int i=0; i<3; i++){
            xs[i] = (int)Math.round(1.0D*tri.xpoints[(i+1)%3]/3.0D + 2.0D*tri.xpoints[i]/3.0D);
            ys[i] = (int)Math.round(1.0D*tri.ypoints[(i+1)%3]/3.0D + 2.0D*tri.ypoints[i]/3.0D);
            xs[3+i] = (int)Math.round(2.0D*tri.xpoints[(i+1)%3]/3.0D + 1.0D*tri.xpoints[i]/3.0D);
            ys[3+i] = (int)Math.round(2.0D*tri.ypoints[(i+1)%3]/3.0D + 1.0D*tri.ypoints[i]/3.0D);
        }

        Polygon stickers[] = new Polygon[9];
        for(int i=0; i<9; i++) stickers[i] = new Polygon();
        for(int i=0; i<3; i++){ // repeat for each set
            // tip sticker
            stickers[i+6].addPoint(tri.xpoints[i], tri.ypoints[i]);
            stickers[i+6].addPoint(xs[i], ys[i]);
            stickers[i+6].addPoint(xs[3 + (i+2)%3], ys[3 + (i+2)%3]);
            // center sticker
            stickers[2*i].addPoint(xs[i], ys[i]);
            stickers[2*i].addPoint(x_offset, y_offset);
            stickers[2*i].addPoint(xs[3 + (i+2)%3], ys[3 + (i+2)%3]);
            // edge sticker
            stickers[2*i+1].addPoint(xs[i], ys[i]);
            stickers[2*i+1].addPoint(xs[3 + i], ys[3 + i]);
            stickers[2*i+1].addPoint(x_offset, y_offset);
        }

        for(int i=0; i<9; i++){
            g2d.setColor(pyraminxColors[state[i][0]]);
            g2d.fillPolygon(stickers[i]); // fill each sticker
        }
        g2d.setColor(Color.black);
        g2d.drawPolygon(tri); // draw the outer triangle
        for(int i=0; i<3; i++) // draw 3 long lines inside
            g2d.drawLine(xs[i], ys[i], xs[3 + (i+1)%3], ys[3 + (i+1)%3]);
        for(int i=0; i<3; i++) // draw 3 short lines inside
            g2d.drawLine(xs[i], ys[i], xs[3 + (i+2)%3], ys[3 + (i+2)%3]);
    }

//**********************************************************************************************************************

    private void scramblePyraminx(String puzzle, String scrambleAlg){
        StringTokenizer moves = new StringTokenizer(scrambleAlg);
        String currentMove = "null";
        boolean failed = false;

        // 6,7,8 are tip stickers, other evens are face stickers, odds are edge stickers
        int state[][][] = new int[4][9][1];
        for(int side=0; side<4; side++)
            for(int i=0; i<9; i++)
                state[side][i][0] = side;

        while(moves.hasMoreTokens()){
            currentMove = moves.nextToken();
            int dir = 1;
            if(currentMove.endsWith("'")){currentMove = currentMove.substring(0, currentMove.length()-1); dir = 2;}

                 if(currentMove.equals("U")) doPyraCoreTurn(state, 0, dir);
            else if(currentMove.equals("L")) doPyraCoreTurn(state, 1, dir);
            else if(currentMove.equals("R")) doPyraCoreTurn(state, 2, dir);
            else if(currentMove.equals("B")) doPyraCoreTurn(state, 3, dir);

            else if(currentMove.equals("u")) doPyraTipsTurn(state, 0, dir);
            else if(currentMove.equals("l")) doPyraTipsTurn(state, 1, dir);
            else if(currentMove.equals("r")) doPyraTipsTurn(state, 2, dir);
            else if(currentMove.equals("b")) doPyraTipsTurn(state, 3, dir);
            else{failed = true; break;}
        }

        if(failed)
            JOptionPane.showMessageDialog(this, "Scramble View encountered bad token for " + puzzle + ": <"+ currentMove + ">.");
        else
            drawPyraminx(state);
    }

//**********************************************************************************************************************

    // dir = number of turns 1/3 turns clockwise
    private void doPyraCoreTurn(int state[][][], int side, int dir){
        dir %= 3;
        for(int d=0; d<dir; d++)
            switch(side){
                case 0:
                    for(int i=0; i<3; i++)
                        cycle(state[0][(5+i)%6], state[3][(3+i)%6], state[1][(1+i)%6]);
                    break;
                case 1:
                    for(int i=0; i<3; i++)
                        cycle(state[0][(3+i)%6], state[2][(1+i)%6], state[3][(5+i)%6]);
                    break;
                case 2:
                    for(int i=0; i<3; i++)
                        cycle(state[0][(1+i)%6], state[1][(5+i)%6], state[2][(3+i)%6]);
                    break;
                case 3:
                    for(int i=0; i<3; i++)
                        cycle(state[2][(5+i)%6], state[1][(3+i)%6], state[3][(1+i)%6]);
                    break;
            }

        doPyraTipsTurn(state, side, dir);
    }

//**********************************************************************************************************************

    // dir = number of turns 1/3 turns clockwise
    private void doPyraTipsTurn(int state[][][], int side, int dir){
        dir %= 3;
        for(int d=0; d<dir; d++)
            switch(side){
                case 0: cycle(state[0][6], state[3][8], state[1][7]); break;
                case 1: cycle(state[0][8], state[2][7], state[3][6]); break;
                case 2: cycle(state[0][7], state[1][6], state[2][8]); break;
                case 3: cycle(state[2][6], state[1][8], state[3][7]); break;
            }
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private Polygon regular_poly(int n, double r, boolean pointup){
        Polygon poly = new Polygon();
        double offset = (pointup ? -0.5D*Math.PI : 0.5D*Math.PI);
        for(int i=0; i<n; i++)
            poly.addPoint((int)Math.round(r*Math.cos(i*2*Math.PI/n + offset)),
                          (int)Math.round(r*Math.sin(i*2*Math.PI/n + offset)));
        return poly;
    }

//**********************************************************************************************************************

    private static Point getLineIntersection(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4){
        double norm = DET(x1-x2, y1-y2, x3-x4, y3-y4);
        double x_inter = DET(DET(x1,y1,x2,y2), x1-x2, DET(x3,y3,x4,y4), x3-x4)/norm;
        double y_inter = DET(DET(x1,y1,x2,y2), y1-y2, DET(x3,y3,x4,y4), y3-y4)/norm;

        return new Point((int)Math.round(x_inter), (int)Math.round(y_inter));
    }

//**********************************************************************************************************************

    private static double DET(double a, double b, double c, double d){
        return (a*d - b*c);
    }

//**********************************************************************************************************************

    private void cycle(int n0[], int n1[], int n2[]){
        int temp = n2[0];
        n2[0] = n1[0];
        n1[0] = n0[0];
        n0[0] = temp;
    }

    private void cycle(int n0[], int n1[], int n2[], int n3[], int n4[]){
        int temp = n4[0];
        n4[0] = n3[0];
        n3[0] = n2[0];
        n2[0] = n1[0];
        n1[0] = n0[0];
        n0[0] = temp;
    }

//**********************************************************************************************************************

    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(myImage, 0, 0, null);
    }

}
