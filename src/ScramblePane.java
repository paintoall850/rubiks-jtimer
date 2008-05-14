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

public class ScramblePane extends JPanel{
    private static final int MIN_ORDER = 2, MAX_ORDER = 5; // constants
    private Color[] xColor = new Color[6];
    private JTextArea[][][][] xFaceN, xPrevN;

//**********************************************************************************************************************

    public ScramblePane(){
        xFaceN = new JTextArea[MAX_ORDER+1][][][];
        xPrevN = new JTextArea[MAX_ORDER+1][][][]; // ignoring 0x0, and 1x1 case (although 1x1 would work)
        for(int side=0; side<6; side++) xColor[side] = Color.black; // just incase...

        for(int order=MIN_ORDER; order<=MAX_ORDER; order++){
            prepareNxN(order);
            setCubeBounds(order);
            //add to contentPane
            for(int side=0; side<6; side++)
                for(int i=0; i<order; i++)
                    for(int j=0; j<order; j++)
                       add(xFaceN[order][side][i][j]);
            visibleNxN(order, false);
        }
    }

//**********************************************************************************************************************

    public void newScramble(String puzzle, String scrambleAlg){
        int order;
        for(order=MIN_ORDER; order<=MAX_ORDER; order++)
            visibleNxN(order, false);

        if(puzzle.equals("2x2x2")) order = 2;
        else if(puzzle.equals("3x3x3")) order = 3;
        else if(puzzle.equals("4x4x4")) order = 4;
        else if(puzzle.equals("5x5x5")) order = 5;
        else return;

        resetNxN(order);
        scrambleNxN(order, scrambleAlg);
        visibleNxN(order, true);
    }

//**********************************************************************************************************************

    private void prepareNxN(int order){
        xFaceN[order] = new JTextArea[6][order][order];
        xPrevN[order] = new JTextArea[6][order][order];

        for(int side=0; side<6; side++)
            for(int i=0; i<order; i++)
                for(int j=0; j<order; j++){
                    xFaceN[order][side][i][j] = new JTextArea();
                    xFaceN[order][side][i][j].setEditable(false);
                    xFaceN[order][side][i][j].setFocusable(false);
                    xFaceN[order][side][i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                    xPrevN[order][side][i][j] = new JTextArea();
                    xPrevN[order][side][i][j].setEditable(false);
                    xPrevN[order][side][i][j].setFocusable(false);
                    xPrevN[order][side][i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                }
    }

//**********************************************************************************************************************

    private void resetNxN(int order){
        for(int side=0; side<6; side++)
            for(int i=0; i<order; i++)
                for(int j=0; j<order; j++){
                    xFaceN[order][side][i][j].setBackground(xColor[side]);
                    xPrevN[order][side][i][j].setBackground(xColor[side]);
                }
    }

//**********************************************************************************************************************

    private void scrambleNxN(int order, String scrambleAlg){
        StringTokenizer moves = new StringTokenizer(scrambleAlg);
        String currentMove = "null";
        boolean failed = false;

        while(moves.hasMoreTokens()){
            currentMove = moves.nextToken();
            int dir = 1;
            if(currentMove.endsWith("'")){currentMove = currentMove.substring(0, currentMove.length()-1); dir = 3;}
            if(currentMove.endsWith("2")){currentMove = currentMove.substring(0, currentMove.length()-1); dir = 2;}
            //JOptionPane.showMessageDialog(this, "For " + order + "x" + order + ": <" + currentMove + "> gives " + dir + ".");

                 if(currentMove.equals("F")){doTurn(order, 0, 0, dir);}
            else if(currentMove.equals("B")){doTurn(order, 1, 0, dir);}
            else if(currentMove.equals("L")){doTurn(order, 2, 0, dir);}
            else if(currentMove.equals("R")){doTurn(order, 3, 0, dir);}
            else if(currentMove.equals("D")){doTurn(order, 4, 0, dir);}
            else if(currentMove.equals("U")){doTurn(order, 5, 0, dir);}
            else if(currentMove.equals("x")){for(int slice=0; slice<order; slice++) doTurn(order, 3, slice, dir);}
            else if(currentMove.equals("y")){for(int slice=0; slice<order; slice++) doTurn(order, 5, slice, dir);}
            else if(currentMove.equals("z")){for(int slice=0; slice<order; slice++) doTurn(order, 0, slice, dir);}

            else if(order < 3){failed = true; break;}
            else if(currentMove.equals("f")){doTurn(order, 0, 1, dir); if(order == 3) doTurn(order, 0, 0, dir);}
            else if(currentMove.equals("b")){doTurn(order, 1, 1, dir); if(order == 3) doTurn(order, 1, 0, dir);}
            else if(currentMove.equals("l")){doTurn(order, 2, 1, dir); if(order == 3) doTurn(order, 2, 0, dir);}
            else if(currentMove.equals("r")){doTurn(order, 3, 1, dir); if(order == 3) doTurn(order, 3, 0, dir);}
            else if(currentMove.equals("d")){doTurn(order, 4, 1, dir); if(order == 3) doTurn(order, 4, 0, dir);}
            else if(currentMove.equals("u")){doTurn(order, 5, 1, dir); if(order == 3) doTurn(order, 5, 0, dir);}
            else if(currentMove.equals("M")){for(int slice=1; slice<order-1; slice++) doTurn(order, 2, slice, dir);}
            else if(currentMove.equals("E")){for(int slice=1; slice<order-1; slice++) doTurn(order, 4, slice, dir);}
            else if(currentMove.equals("S")){for(int slice=1; slice<order-1; slice++) doTurn(order, 0, slice, dir);}
            else if(currentMove.equals("Fw")){doTurn(order, 0, 0, dir); doTurn(order, 0, 1, dir);}
            else if(currentMove.equals("Bw")){doTurn(order, 1, 0, dir); doTurn(order, 1, 1, dir);}
            else if(currentMove.equals("Lw")){doTurn(order, 2, 0, dir); doTurn(order, 2, 1, dir);}
            else if(currentMove.equals("Rw")){doTurn(order, 3, 0, dir); doTurn(order, 3, 1, dir);}
            else if(currentMove.equals("Dw")){doTurn(order, 4, 0, dir); doTurn(order, 4, 1, dir);}
            else if(currentMove.equals("Uw")){doTurn(order, 5, 0, dir); doTurn(order, 5, 1, dir);}

            else if((order < 5) || (order%2 == 0)){failed = true; break;}
            else if(currentMove.equals("m")){doTurn(order, 2, (order-1)/2, dir);}
            else if(currentMove.equals("e")){doTurn(order, 4, (order-1)/2, dir);}
            else if(currentMove.equals("s")){doTurn(order, 0, (order-1)/2, dir);}
            else{failed = true; break;}
        }

        if(failed){
            JOptionPane.showMessageDialog(this, "Scramble View encountered bad token for " + order + "x" + order + ": <"+ currentMove + ">.");
            visibleNxN(order, false);
        }
    }

//**********************************************************************************************************************

    private void updatePreviousNxN(int order){
        for(int side=0; side<6; side++)
            for(int i=0; i<order; i++)
                for(int j=0; j<order; j++)
                    xPrevN[order][side][i][j].setBackground(xFaceN[order][side][i][j].getBackground());
    }

//**********************************************************************************************************************

    private void setCubeBounds(int order){
        int x = 15, y = 19; // nudge factors
        //int margin = 14;
        //int appWidth = getWidth(), appHeight = getHeight();
        int face_gap = 4;
        int face_pixels = 60;
        //setCubeBoundsMath.min((appWidth - 3*face_gap - 2*margin)/4, (appHeight - 2*face_gap - 2*margin)/3);
        int n = face_pixels + face_gap;
        //int x = (appWidth - 4*face_pixels - 3*face_gap)/2, y = (appHeight - 3*face_pixels - 2*face_gap)/2;

        setFaceBounds(xFaceN[order][0], order, 1*n + x ,1*n + y, face_pixels/order);
        setFaceBounds(xFaceN[order][1], order, 3*n + x, 1*n + y, face_pixels/order);
        setFaceBounds(xFaceN[order][2], order, 0*n + x, 1*n + y, face_pixels/order);
        setFaceBounds(xFaceN[order][3], order, 2*n + x, 1*n + y, face_pixels/order);
        setFaceBounds(xFaceN[order][4], order, 1*n + x, 2*n + y, face_pixels/order);
        setFaceBounds(xFaceN[order][5], order, 1*n + x, 0*n + y, face_pixels/order);
    }

//**********************************************************************************************************************

    private void visibleNxN(int order, boolean show){
        for(int side=0; side<6; side++)
            for(int i=0; i<order; i++)
                for(int j=0; j<order; j++)
                    xFaceN[order][side][i][j].setVisible(show);
    }

//**********************************************************************************************************************

    public void setColors(Color[] x){
        for(int side=0; side<6; side++)
            xColor[side] = x[side];
    }

//**********************************************************************************************************************

    private void setFaceBounds(JTextArea[][] aFace, int order, int x, int y, int size){
        for(int i=0; i<order; i++)
            for(int j=0; j<order; j++)
                aFace[i][j].setBounds(j*size+x, i*size+y, size, size);
    }

//**********************************************************************************************************************

    // dir: 1 for 90 deg, 2 for 180 deg, 3 for 270 deg
    private void doTurn(int order, int side, int slice, int dir){
        if(side<0 || side>5){
            JOptionPane.showMessageDialog(this, "Function doTurn called with side=" + side + ".");
            return;
        }
        dir %= 4;
        if(dir == 0) return;
        if(slice > order-1) return;
        if(slice == order-1){ //far slice, mostly to help handle whole cube rotation
            doTurn(order, (side%2 == 1 ? side-1 : side+1), 0, 4-dir); //recursion for that far slice
            return;
        }

        int tside = side, tslice = slice, tdir = dir;
        if(side%2 == 1){
            tside = side-1;
            tslice = order-slice-1;
            tdir = 4-dir;
        }

        JTextArea[][][] xFace = xFaceN[order];
        JTextArea[][][] xPrev = xPrevN[order];
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
            updatePreviousNxN(order);
        }

        if(slice == 0) // this means we need to do some pure face rotation
            for(int d=0; d<dir; d++){
                for(int i=0; i<order; i++)
                    for(int j=0; j<order; j++)
                        xFace[side][i][j].setBackground(xPrev[side][order-j-1][i].getBackground());
                updatePreviousNxN(order);
            }
    }

}
