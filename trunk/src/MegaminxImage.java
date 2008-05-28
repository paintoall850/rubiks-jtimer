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

public class MegaminxImage{

    private static final int NUM_FACES = 12;
    public Color[] myColors = new Color[NUM_FACES];
    private Polygon[] myFaces = new Polygon[NUM_FACES];
    private BufferedImage myImage;
    private int myWidth, myHeight;

//**********************************************************************************************************************

    public MegaminxImage(int width, int height){
        myWidth = width;
        myHeight = height;

        for(int face=0; face<NUM_FACES; face++) myColors[face] = Color.black; // just incase...
        for(int face=0; face<NUM_FACES; face++) myFaces[face] = new Polygon(); // just incase...
        myImage = new BufferedImage(myWidth, myHeight, BufferedImage.TYPE_INT_ARGB);
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

        int[][][] state = new int[NUM_FACES][11][1];
        for(int face=0; face<NUM_FACES; face++)
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
                doFaceTurn(state, 8, 1); doSliceTurn(state, 8, 1); doSliceTurn(state, 5, 4);
            }
            else if(move.equals("R++")){
                doFaceTurn(state, 8, 2); doSliceTurn(state, 8, 2); doSliceTurn(state, 5, 3);
            }
            else if(move.equals("R--")){
                doFaceTurn(state, 8, 3); doSliceTurn(state, 8, 3); doSliceTurn(state, 5, 2);
            }
            else if(move.equals("R-")){
                doFaceTurn(state, 8, 4); doSliceTurn(state, 8, 4); doSliceTurn(state, 5, 1);
            }
            else if(move.equals("D+")){
                doFaceTurn(state, 7, 1); doSliceTurn(state, 7, 1); doSliceTurn(state, 1, 4);
            }
            else if(move.equals("D++")){
                doFaceTurn(state, 7, 2); doSliceTurn(state, 7, 2); doSliceTurn(state, 1, 3);
            }
            else if(move.equals("D--")){
                doFaceTurn(state, 7, 3); doSliceTurn(state, 7, 3); doSliceTurn(state, 1, 2);
            }
            else if(move.equals("D-")){
                doFaceTurn(state, 7, 4); doSliceTurn(state, 7, 4); doSliceTurn(state, 1, 1);
            }
            else if(move.equals("U") || move.equals("Y+")) doFaceTurn(state, 1, 1);
            else if(move.equals("U'") || move.equals("Y-")) doFaceTurn(state, 1, 4);
            else if(move.equals("Y++")) doFaceTurn(state, 1, 2);
            else if(move.equals("Y--")) doFaceTurn(state, 1, 3);
            else if(move.equals("A")) doFaceTurn(state, 0, dir);
            else if(move.equals("B")) doFaceTurn(state, 1, dir);
            else if(move.equals("C")) doFaceTurn(state, 2, dir);
            else if(move.equals("D")) doFaceTurn(state, 3, dir);
            else if(move.equals("E")) doFaceTurn(state, 4, dir);
            else if(move.equals("F")) doFaceTurn(state, 5, dir);
            else if(move.equals("a")) doFaceTurn(state, 6, dir);
            else if(move.equals("b")) doFaceTurn(state, 7, dir);
            else if(move.equals("f")) doFaceTurn(state, 8, dir); // c,d,e,f reversed, this is correct
            else if(move.equals("e")) doFaceTurn(state, 9, dir);
            else if(move.equals("d")) doFaceTurn(state,10, dir);
            else if(move.equals("c")) doFaceTurn(state,11, dir);
            else{failed = true; break;}
        }

        if(!failed){
            drawPuzzle(state);
            return "success";
        } else
            return move; // for generating error message
    } // end scramble

//**********************************************************************************************************************

    // dir = number of turns 1/5 turns clockwise
    private static final void doFaceTurn(int[][][] state, int face, int dir){
        int plus6 = (face < 6 ? 0 : 6);
        dir %= 5;
        for(int d=0; d<dir; d++){
            switch(face%6){
                case 0:
                    helperFaceTurn(state, plus6, 1, 2, 3, 4, 5,
                                                 4, 6, 8, 0, 2);
                    break;
                case 1:
                    helperFaceTurn(state, plus6, 0, 5,10, 9, 2,
                                                 4, 0, 4, 4, 8);
                    break;
                case 2:
                    helperFaceTurn(state, plus6, 0, 1, 9, 8, 3,
                                                 6, 2, 2, 2, 0);
                    break;
                case 3:
                    helperFaceTurn(state, plus6, 0, 2, 8, 7, 4,
                                                 8, 4, 0, 0, 2);
                    break;
                case 4:
                    helperFaceTurn(state, plus6, 0, 3, 7,11, 5,
                                                 0, 6, 8, 8, 4);
                    break;
                case 5:
                    helperFaceTurn(state, plus6, 0, 4,11,10, 1,
                                                 2, 8, 6, 6, 6);
                    break;
            }

            //pure face rotation time
            cycle(state[face][0], state[face][2], state[face][4], state[face][6], state[face][8]); // corners
            cycle(state[face][1], state[face][3], state[face][5], state[face][7], state[face][9]); // edges
        }
    } // end doFaceTurn

//**********************************************************************************************************************

    private static final void helperFaceTurn(int[][][] state, int plus6,
                                        int f0, int f1, int f2, int f3, int f4,
                                        int x0, int x1, int x2, int x3, int x4){
        for(int i=0; i<3; i++) // the three stickers on the edge, so like c/e/c
            cycle(  state[(f0+plus6)%12][(x0+i)%10],
                    state[(f1+plus6)%12][(x1+i)%10],
                    state[(f2+plus6)%12][(x2+i)%10],
                    state[(f3+plus6)%12][(x3+i)%10],
                    state[(f4+plus6)%12][(x4+i)%10]);
    } // end helperFaceTurn

//**********************************************************************************************************************

    private static final void doSliceTurn(int[][][] state, int face, int dir){
        int plus6 = (face < 6 ? 0 : 6);
        dir %= 5;
        for(int d=0; d<dir; d++){
            switch(face%6){
                case 0:
                    helperSliceTurn(state, plus6,   1, 2, 3, 4, 5,
                                                    4, 6, 8, 0, 2);
                    break;
                case 1:
                    helperSliceTurn(state, plus6,   0, 5,10, 9, 2,
                                                    4, 0, 4, 4, 8);
                    break;
                case 2:
                    helperSliceTurn(state, plus6,   0, 1, 9, 8, 3,
                                                    6, 2, 2, 2, 0);
                    break;
                case 3:
                    helperSliceTurn(state, plus6,   0, 2, 8, 7, 4,
                                                    8, 4, 0, 0, 2);
                    break;
                case 4:
                    helperSliceTurn(state, plus6,   0, 3, 7,11, 5,
                                                    0, 6, 8, 8, 4);
                    break;
                case 5:
                    helperSliceTurn(state, plus6,   0, 4,11,10, 1,
                                                    2, 8, 6, 6, 6);
                    break;
            }
        }
    } // end doSliceTurn

//**********************************************************************************************************************

    private static final void helperSliceTurn(int[][][] state, int plus6,
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
    } // end helperSliceTurn

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private final void drawPuzzle(int[][][] state){
        myImage = new BufferedImage(myWidth, myHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = myImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // turn on if angled lines

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

        myFaces[ 0] = makeFace(radius, xCenter, yCenter, false);
        myFaces[ 1] = makeFace(radius, big_pent.xpoints[0], big_pent.ypoints[0], true);
        myFaces[ 2] = makeFace(radius, big_pent.xpoints[1], big_pent.ypoints[1], true);
        myFaces[ 3] = makeFace(radius, big_pent.xpoints[2], big_pent.ypoints[2], true);
        myFaces[ 4] = makeFace(radius, big_pent.xpoints[3], big_pent.ypoints[3], true);
        myFaces[ 5] = makeFace(radius, big_pent.xpoints[4], big_pent.ypoints[4], true);

        myFaces[ 6] = makeFace(radius, xCenter + xShift, yCenter + yShift, false);
        myFaces[ 7] = makeFace(radius, big_pent.xpoints[0] + xShift, big_pent.ypoints[0] + yShift, true);
        myFaces[ 8] = makeFace(radius, big_pent.xpoints[1] + xShift, big_pent.ypoints[1] + yShift, true);
        myFaces[ 9] = makeFace(radius, big_pent.xpoints[2] + xShift, big_pent.ypoints[2] + yShift, true);
        myFaces[10] = makeFace(radius, big_pent.xpoints[3] + xShift, big_pent.ypoints[3] + yShift, true);
        myFaces[11] = makeFace(radius, big_pent.xpoints[4] + xShift, big_pent.ypoints[4] + yShift, true);

        for(int i=0; i<NUM_FACES; i++)
            drawFace(g2d, myFaces[i], state[i]);
    } // end drawPuzzle

//**********************************************************************************************************************

    private static final Polygon makeFace(float r, int x_offset, int y_offset, boolean pointup){
        Polygon pent = RJT_Utils.regular_poly(5, r, pointup);
        pent.translate(x_offset, y_offset);
        return pent;
    }

//**********************************************************************************************************************

    private final void drawFace(Graphics2D g2d, Polygon pent, int[][] state){

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
            g2d.setColor(myColors[state[i][0]]);
            g2d.fillPolygon(stickers[i]); // fill each sticker
        }
        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(3F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawPolygon(pent); // draw the outer pentagon
        g2d.setStroke(new BasicStroke(1.5F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for(int i=0; i<5; i++) // now draw the 5 lines inside
            g2d.drawLine(xs[i][0], ys[i][0], xs[(i+3)%5][1], ys[(i+3)%5][1]);

    } // end drawFace

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    public static final void cycle(int n0[], int n1[], int n2[], int n3[], int n4[]){
        int temp = n4[0];
        n4[0] = n3[0];
        n3[0] = n2[0];
        n2[0] = n1[0];
        n1[0] = n0[0];
        n0[0] = temp;
    }

}
