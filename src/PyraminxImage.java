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

import java.awt.*;
import java.util.*;
import java.awt.image.BufferedImage;

public class PyraminxImage{

    private static final int NUM_FACES = 4;
    public Color[] myColors = new Color[NUM_FACES];
    private Polygon[] myFaces = new Polygon[NUM_FACES];
    private BufferedImage myImage;
    private int myWidth, myHeight;

//**********************************************************************************************************************

    public PyraminxImage(int width, int height){
        myWidth = width;
        myHeight = height;

        for(int face=0; face<NUM_FACES; face++) myColors[face] = Color.black; // just incase...
        for(int face=0; face<NUM_FACES; face++) myFaces[face] = new Polygon(); // just incase...
        myImage = new BufferedImage(myWidth, myHeight, BufferedImage.TYPE_INT_ARGB);
    }

//**********************************************************************************************************************

    public final void setColors(Color[] newColors){
        for(int i=0; i<NUM_FACES; i++)
            myColors[i] = new Color(newColors[i].getRGB());
    }

//**********************************************************************************************************************

    public final BufferedImage getImage(){return myImage;}

//**********************************************************************************************************************

    public final boolean inFace(int i, int x, int y){
        if(i >= 0 && i < NUM_FACES)
            return myFaces[i].contains(x,y);
        else
            return false;
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    public final String scramble(String scramble){
        StringTokenizer moves = new StringTokenizer(scramble);
        String move = "null";
        boolean failed = false;

        // 6,7,8 are tip stickers, other evens are face stickers, odds are edge stickers
        int[][][] state = new int[NUM_FACES][9][1];
        for(int face=0; face<NUM_FACES; face++)
            for(int i=0; i<9; i++)
                state[face][i][0] = face;

        while(moves.hasMoreTokens()){
            move = moves.nextToken();
            int dir = 1;
            if(move.endsWith("'")){move = move.substring(0, move.length()-1); dir = 2;}

                 if(move.equals("U")) doCoreTurn(state, 0, dir);
            else if(move.equals("L")) doCoreTurn(state, 1, dir);
            else if(move.equals("R")) doCoreTurn(state, 2, dir);
            else if(move.equals("B")) doCoreTurn(state, 3, dir);

            else if(move.equals("u")) doTipsTurn(state, 0, dir);
            else if(move.equals("l")) doTipsTurn(state, 1, dir);
            else if(move.equals("r")) doTipsTurn(state, 2, dir);
            else if(move.equals("b")) doTipsTurn(state, 3, dir);
            else{failed = true; break;}
        }

        if(!failed){
            drawPuzzle(state);
            return "success";
        } else
            return move; // for generating error message
    } // end scramble

//**********************************************************************************************************************

    // dir = number of turns 1/3 turns clockwise
    private static final void doCoreTurn(int[][][] state, int face, int dir){
        dir %= 3;
        for(int d=0; d<dir; d++)
            switch(face){
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

        doTipsTurn(state, face, dir);
    } // end doCoreTurn

//**********************************************************************************************************************

    // dir = number of turns 1/3 turns clockwise
    private static final void doTipsTurn(int[][][] state, int face, int dir){
        dir %= 3;
        for(int d=0; d<dir; d++)
            switch(face){
                case 0: cycle(state[0][6], state[3][8], state[1][7]); break;
                case 1: cycle(state[0][8], state[2][7], state[3][6]); break;
                case 2: cycle(state[0][7], state[1][6], state[2][8]); break;
                case 3: cycle(state[2][6], state[1][8], state[3][7]); break;
            }
    } // end doTipsTurn

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private final void drawPuzzle(int[][][] state){
        myImage = new BufferedImage(myWidth, myHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = myImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // turn on if angled lines

        int xCenter = myWidth/2;//141;
        int yCenter = myHeight/2-19;//98;
        double radius = Math.min(myWidth, myHeight-20) * 0.24D;//52;
        double face_gap = 7;
        double big_radius = radius + face_gap;//2 * radius * Math.cos(Math.PI/3) + face_gap;
//System.err.print("xCenter:" + xCenter + "\n");
//System.err.print("yCenter:" + yCenter + "\n");
//System.err.print("radius:" + radius + "\n");

        Polygon big_tri = RJT_Utils.regular_poly(3, big_radius, false); // auxiliary: for drawing the outer 3 faces
        big_tri.translate(xCenter, yCenter);

        myFaces[0] = makeFace(radius, xCenter, yCenter, true); // F face
        myFaces[1] = makeFace(radius, big_tri.xpoints[2], big_tri.ypoints[2], false); // R face
        myFaces[2] = makeFace(radius, big_tri.xpoints[0], big_tri.ypoints[0], false); // D face
        myFaces[3] = makeFace(radius, big_tri.xpoints[1], big_tri.ypoints[1], false); // L face

        // example of the adjustments that can be made, really shows off the power of the drawFace code
        //myFaces[1].xpoints[2] -= radius * 0.4D;
        //myFaces[2].ypoints[0] -= radius * 0.9D;
        //myFaces[3].xpoints[1] += radius * 0.4D;

        for(int i=0; i<NUM_FACES; i++)
            drawFace(g2d, myFaces[i], state[i]);
    } // end drawPuzzle

//**********************************************************************************************************************

    private static final Polygon makeFace(double r, int x_offset, int y_offset, boolean pointup){
        Polygon tri = RJT_Utils.regular_poly(3, r, pointup);
        tri.translate(x_offset, y_offset);
        return tri;
    }

//**********************************************************************************************************************

    private final void drawFace(Graphics2D g2d, Polygon tri, int[][] state){

        int xs[][] = new int[3][2], ys[][] = new int[3][2]; // the 6 points that are on the edges
        for(int i=0; i<3; i++){
            xs[i][0] = (int)Math.round(1D*tri.xpoints[(i+1)%3]/3D + 2D*tri.xpoints[i]/3D);
            ys[i][0] = (int)Math.round(1D*tri.ypoints[(i+1)%3]/3D + 2D*tri.ypoints[i]/3D);
            xs[i][1] = (int)Math.round(2D*tri.xpoints[(i+1)%3]/3D + 1D*tri.xpoints[i]/3D);
            ys[i][1] = (int)Math.round(2D*tri.ypoints[(i+1)%3]/3D + 1D*tri.ypoints[i]/3D);
        }

        Point mid_point = RJT_Utils.intersectionPoint(
                            xs[0][0], ys[0][0], xs[1][1], ys[1][1],
                            xs[1][0], ys[1][0], xs[2][1], ys[2][1]);

        Polygon stickers[] = new Polygon[9];
        for(int i=0; i<9; i++) stickers[i] = new Polygon();
        for(int i=0; i<3; i++){ // repeat for each set
            // tip sticker
            stickers[i+6].addPoint(tri.xpoints[i], tri.ypoints[i]);
            stickers[i+6].addPoint(xs[i][0], ys[i][0]);
            stickers[i+6].addPoint(xs[(i+2)%3][1], ys[(i+2)%3][1]);
            // center sticker
            stickers[2*i].addPoint(xs[i][0], ys[i][0]);
            stickers[2*i].addPoint(mid_point.x, mid_point.y);
            stickers[2*i].addPoint(xs[(i+2)%3][1], ys[(i+2)%3][1]);
            // edge sticker
            stickers[2*i+1].addPoint(xs[i][0], ys[i][0]);
            stickers[2*i+1].addPoint(xs[i][1], ys[i][1]);
            stickers[2*i+1].addPoint(mid_point.x, mid_point.y);
        }

        for(int i=0; i<9; i++){
            g2d.setColor(myColors[state[i][0]]);
            g2d.fillPolygon(stickers[i]); // fill each sticker
        }
        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(3F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawPolygon(tri); // draw the outer triangle
        g2d.setStroke(new BasicStroke(1.5F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        for(int i=0; i<3; i++) // draw 3 long lines inside
            g2d.drawLine(xs[i][0], ys[i][0], xs[(i+1)%3][1], ys[(i+1)%3][1]);
        for(int i=0; i<3; i++) // draw 3 short lines inside
            g2d.drawLine(xs[i][0], ys[i][0], xs[(i+2)%3][1], ys[(i+2)%3][1]);

    } // end drawFace

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private static final void cycle(int n0[], int n1[], int n2[]){
        int temp = n2[0];
        n2[0] = n1[0];
        n1[0] = n0[0];
        n0[0] = temp;
    }

}
