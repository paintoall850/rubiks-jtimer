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
import java.awt.image.BufferedImage; //

public class ScramblePane extends JPanel{
    private static final int MIN_ORDER = 2, MAX_ORDER = 5; // constants
    private Color[] xColor = new Color[6];
    private JTextArea[][][][] xFaceN, xPrevN;
    private int myWidth, myHeight;

//**********************************************************************************************************************

    public ScramblePane(int width, int height){
        myWidth = width; myHeight = height;
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
        generateImage();
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

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private BufferedImage minxImage;
    private Color[] megaminxColors = {  new Color(255,255,255), // white
                                        new Color(0,180,255), // powder blue
                                        new Color(200,128,0), // brown
                                        new Color(255,0,0), // red
                                        new Color(255,255,0), // yellow
                                        new Color(0,255,0), // bright green
                                        new Color(160,0,255), // purple
                                        new Color(0,0,170), // dark blue
                                        new Color(0,128,0), // dark green
                                        new Color(255,80,80), // pink
                                        new Color(255,180,180), // light pink
                                        new Color(255,128,0)}; // orange

    private void generateImage(){
        minxImage = new BufferedImage(myWidth, myHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = minxImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(1.6F));

        int minxState[][] = new int[12][11];
        for(int side=0; side<12; side++)
            for(int i=0; i<11; i++)
                minxState[side][i] = side;

        int xCenter = 141, yCenter = 120, big_shift = 141;
        double radius = 36;
        double face_gap = 4;
        double big_radius = 2 * radius * Math.cos(0.2D*Math.PI) + face_gap;

        Polygon big_pent = pentagon(big_radius, true);
        big_pent.translate(xCenter, yCenter);

        drawMinxFace(g2d, radius, xCenter, yCenter, false, minxState[0]);
        drawMinxFace(g2d, radius, big_pent.xpoints[0], big_pent.ypoints[0], true, minxState[1]);
        drawMinxFace(g2d, radius, big_pent.xpoints[1], big_pent.ypoints[1], true, minxState[2]);
        drawMinxFace(g2d, radius, big_pent.xpoints[2], big_pent.ypoints[2], true, minxState[3]);
        drawMinxFace(g2d, radius, big_pent.xpoints[3], big_pent.ypoints[3], true, minxState[4]);
        drawMinxFace(g2d, radius, big_pent.xpoints[4], big_pent.ypoints[4], true, minxState[5]);
        //drawMinxFace(g2d, radius, xCenter + big_shift, yCenter, false, minxState[6]);
        //drawMinxFace(g2d, radius, big_pent.xpoints[0] + big_shift, big_pent.ypoints[0], true, minxState[7]);
        //drawMinxFace(g2d, radius, big_pent.xpoints[1] + big_shift, big_pent.ypoints[1], true, minxState[8]);
        //drawMinxFace(g2d, radius, big_pent.xpoints[2] + big_shift, big_pent.ypoints[2], true, minxState[9]);
        //drawMinxFace(g2d, radius, big_pent.xpoints[3] + big_shift, big_pent.ypoints[3], true, minxState[10]);
        //drawMinxFace(g2d, radius, big_pent.xpoints[4] + big_shift, big_pent.ypoints[4], true, minxState[11]);
    }

//**********************************************************************************************************************

    private void drawMinxFace(Graphics2D g2d, double r, int x_offset, int y_offset, boolean pointup, int minxState[]){
        Polygon pent = pentagon(r, pointup);
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
        for(int i=0; i<5; i++){
            // corner sticker
            stickers[2*i].addPoint(pent.xpoints[i], pent.ypoints[i]);
            stickers[2*i].addPoint(xs[i], ys[i]);
            stickers[2*i].addPoint(inside_pent[i].x, inside_pent[i].y);
            stickers[2*i].addPoint(xs[5 + (4+i)%5], ys[5 + (4+i)%5]);
            //edge sticker
            stickers[2*i+1].addPoint(xs[i], ys[i]);
            stickers[2*i+1].addPoint(xs[5 + i], ys[5 + i]);
            stickers[2*i+1].addPoint(inside_pent[(i+1)%5].x, inside_pent[(i+1)%5].y);
            stickers[2*i+1].addPoint(inside_pent[i].x, inside_pent[i].y);
            //center sticker
            stickers[10].addPoint(inside_pent[i].x, inside_pent[i].y);
        }

        for(int i=0; i<11; i++){
            g2d.setColor(megaminxColors[minxState[i]]);
            g2d.fillPolygon(stickers[i]); // fill each sticker
        }
        g2d.setColor(Color.black);
        g2d.drawPolygon(pent); // draw the outer pentagon
        for(int i=0; i<5; i++) // now draw the 5 lines inside
            g2d.drawLine(xs[i], ys[i], xs[5 + (3+i)%5], ys[5 + (3+i)%5]);
    }

//**********************************************************************************************************************

    private Polygon pentagon(double r, boolean pointup){
        Polygon pent = new Polygon();
        double offset = (pointup ? -0.5D*Math.PI : 0.5D*Math.PI);
        for(int i=0; i<5; i++)
            pent.addPoint((int)Math.round(r*Math.cos(i*0.4D*Math.PI + offset)),
                          (int)Math.round(r*Math.sin(i*0.4D*Math.PI + offset)));
        return pent;
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
/*
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(minxImage, 0, 0, null);
    }
*/
}
