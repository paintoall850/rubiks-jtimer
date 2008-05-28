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

public class CubeImage{

    private static final int NUM_FACES = 6;
    public Color[] myColors = new Color[NUM_FACES];
    private Polygon[] myFaces = new Polygon[NUM_FACES];
    private BufferedImage myImage;
    private int myWidth, myHeight;

//**********************************************************************************************************************

    public CubeImage(int width, int height){
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

    public final String scramble(int size, String scramble){
        StringTokenizer moves = new StringTokenizer(scramble);
        String move = "null";
        boolean failed = false;

        int[][][][] state = new int[NUM_FACES][size][size][1];
        for(int face=0; face<NUM_FACES; face++)
            for(int i=0; i<size; i++)
                for(int j=0; j<size; j++)
                    state[face][i][j][0] = face;

        while(moves.hasMoreTokens()){
            move = moves.nextToken();
            int dir = 1;
            if(move.endsWith("'")){move = move.substring(0, move.length()-1); dir = 3;}
            if(move.endsWith("2")){move = move.substring(0, move.length()-1); dir = 2;}

                 if(move.equals("F")){doTurn(size, state, 0, 0, dir);}
            else if(move.equals("B")){doTurn(size, state, 1, 0, dir);}
            else if(move.equals("L")){doTurn(size, state, 2, 0, dir);}
            else if(move.equals("R")){doTurn(size, state, 3, 0, dir);}
            else if(move.equals("D")){doTurn(size, state, 4, 0, dir);}
            else if(move.equals("U")){doTurn(size, state, 5, 0, dir);}
            else if(move.equals("x")){for(int slice=0; slice<size; slice++) doTurn(size, state, 3, slice, dir);}
            else if(move.equals("y")){for(int slice=0; slice<size; slice++) doTurn(size, state, 5, slice, dir);}
            else if(move.equals("z")){for(int slice=0; slice<size; slice++) doTurn(size, state, 0, slice, dir);}

            else if(size < 3){failed = true; break;}
            else if(move.equals("f")){doTurn(size, state, 0, 1, dir); if(size == 3) doTurn(size, state, 0, 0, dir);}
            else if(move.equals("b")){doTurn(size, state, 1, 1, dir); if(size == 3) doTurn(size, state, 1, 0, dir);}
            else if(move.equals("l")){doTurn(size, state, 2, 1, dir); if(size == 3) doTurn(size, state, 2, 0, dir);}
            else if(move.equals("r")){doTurn(size, state, 3, 1, dir); if(size == 3) doTurn(size, state, 3, 0, dir);}
            else if(move.equals("d")){doTurn(size, state, 4, 1, dir); if(size == 3) doTurn(size, state, 4, 0, dir);}
            else if(move.equals("u")){doTurn(size, state, 5, 1, dir); if(size == 3) doTurn(size, state, 5, 0, dir);}
            else if(move.equals("M")){for(int slice=1; slice<size-1; slice++) doTurn(size, state, 2, slice, dir);}
            else if(move.equals("E")){for(int slice=1; slice<size-1; slice++) doTurn(size, state, 4, slice, dir);}
            else if(move.equals("S")){for(int slice=1; slice<size-1; slice++) doTurn(size, state, 0, slice, dir);}
            else if(move.equals("Fw")){doTurn(size, state, 0, 0, dir); doTurn(size, state, 0, 1, dir);}
            else if(move.equals("Bw")){doTurn(size, state, 1, 0, dir); doTurn(size, state, 1, 1, dir);}
            else if(move.equals("Lw")){doTurn(size, state, 2, 0, dir); doTurn(size, state, 2, 1, dir);}
            else if(move.equals("Rw")){doTurn(size, state, 3, 0, dir); doTurn(size, state, 3, 1, dir);}
            else if(move.equals("Dw")){doTurn(size, state, 4, 0, dir); doTurn(size, state, 4, 1, dir);}
            else if(move.equals("Uw")){doTurn(size, state, 5, 0, dir); doTurn(size, state, 5, 1, dir);}

            else if((size < 5) || (size%2 == 0)){failed = true; break;}
            else if(move.equals("m")){doTurn(size, state, 2, (size-1)/2, dir);}
            else if(move.equals("e")){doTurn(size, state, 4, (size-1)/2, dir);}
            else if(move.equals("s")){doTurn(size, state, 0, (size-1)/2, dir);}
            else{failed = true; break;}
        }

        if(!failed){
            drawPuzzle(size, state);
            return "success";
        } else
            return move; // for generating error message
    } // end scramble

//**********************************************************************************************************************

    // dir = 1 for 90 deg, 2 for 180 deg, 3 for 270 deg
    private static final void doTurn(int size, int[][][][] state, int face, int slice, int dir){
        dir %= 4;
        if(slice > size-1) return;
        if(slice == size-1){ //far slice, mostly to help handle whole cube rotation
            doTurn(size, state, (face%2 == 1 ? face-1 : face+1), 0, 4-dir); //recursion for that far slice
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
                    cycle(state[5][size-tslice-1][size-i-1], state[3][size-i-1][tslice], state[4][tslice][i], state[2][i][size-tslice-1]);
                else if(tface == 2) // doing L
                    cycle(state[5][i][tslice], state[0][i][tslice], state[4][i][tslice], state[1][size-i-1][size-tslice-1]);
                else if(tface == 4) // doing D
                    cycle(state[0][size-tslice-1][i], state[3][size-tslice-1][i], state[1][size-tslice-1][i], state[2][size-tslice-1][i]);
            }
        }

        if(slice == 0) // this means we need to do some pure face rotation
            for(int d=0; d<dir; d++)
                for(int i=0; i<((size+1)/2); i++)
                    for(int j=0; j<(size/2); j++)
                        cycle(state[face][i][j], state[face][j][size-i-1], state[face][size-i-1][size-j-1], state[face][size-j-1][i]);
    } // end doTurn

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private final void drawPuzzle(int size, int[][][][] state){
        myImage = new BufferedImage(myWidth, myHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = myImage.createGraphics();
        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // turn on if angled lines

        int margin = 15;
        int face_gap = 7;
        //int face_pixels = 60;
//        int face_pixels = Math.min((myWidth - 3*face_gap - 2*margin)/4, ((myHeight-19) - 2*face_gap - 2*margin)/3);
int face_pixels = Math.min(2*(myWidth - 2*face_gap - 2*margin)/7, 2*((myHeight-19) - 1*face_gap - 2*margin)/5);
//        int n = face_pixels + face_gap;
int n = face_pixels;
        //int x = 15, y = 19; // nudge factors
//        int x = (myWidth - 4*face_pixels - 3*face_gap)/2, y = ((myHeight-19) - 3*face_pixels - 2*face_gap)/2;
int x = (myWidth - 7*face_pixels/2 - 2*face_gap)/2, y = ((myHeight-19) - 5*face_pixels/2 - 1*face_gap)/2;
//        y += 14; // nudge away from title
y += 14 - face_pixels/2;

        myFaces[0] = makeFace(face_pixels, 1*n + x, 1*n + y); // F face
        myFaces[1] = makeFace(face_pixels, 3*n + x, 1*n + y); // B face
        myFaces[2] = makeFace(face_pixels, 0*n + x, 1*n + y); // L face
        myFaces[3] = makeFace(face_pixels, 2*n + x, 1*n + y); // R face
        myFaces[4] = makeFace(face_pixels, 1*n + x, 2*n + y); // D face
        myFaces[5] = makeFace(face_pixels, 1*n + x, 0*n + y); // U face

        int x_shift = face_pixels/2, y_shift = face_pixels/2; // amount to translate top 2 points of U
        myFaces[5].xpoints[0] += x_shift;
        myFaces[5].ypoints[0] += y_shift;
        myFaces[5].xpoints[1] += x_shift;
        myFaces[5].ypoints[1] += y_shift;

        myFaces[3].xpoints[1] -= face_pixels - x_shift;
        myFaces[3].ypoints[1] -= face_pixels - y_shift;
        myFaces[3].xpoints[2] -= face_pixels - x_shift;
        myFaces[3].ypoints[2] -= face_pixels - y_shift;
        myFaces[1].translate(-(face_pixels - x_shift), -(face_pixels - y_shift));

        myFaces[0].translate(face_gap,0);
        myFaces[1].translate(2*face_gap,0);
        //myFaces[2].translate(0,0);
        myFaces[3].translate(face_gap,0);
        myFaces[4].translate(face_gap,face_gap);
        myFaces[5].translate(face_gap,0);

        for(int i=0; i<NUM_FACES; i++)
            drawFace(g2d, size, myFaces[i], state[i]);
    } // end drawPuzzle

//**********************************************************************************************************************

    private static final Polygon makeFace(int face_pixels, int x_offset, int y_offset){
        Polygon square = square_poly(face_pixels);
        square.translate(x_offset, y_offset);
        return square;
    }

//**********************************************************************************************************************

    private final void drawFace(Graphics2D g2d, int size, Polygon square, int[][][] state){

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
                            xs[1][i], ys[1][i], xs[3][i], ys[3][i],
                            xs[0][j], ys[0][j], xs[2][j], ys[2][j]);

        Polygon stickers[][] = new Polygon[size][size];
        for(int i=0; i<size; i++)
            for(int j=0; j<size; j++){
                stickers[i][j] = new Polygon();
                stickers[i][j].addPoint(lattice_points[i][j].x, lattice_points[i][j].y);
                stickers[i][j].addPoint(lattice_points[i][j+1].x, lattice_points[i][j+1].y);
                stickers[i][j].addPoint(lattice_points[i+1][j+1].x, lattice_points[i+1][j+1].y);
                stickers[i][j].addPoint(lattice_points[i+1][j].x, lattice_points[i+1][j].y);
                g2d.setColor(myColors[state[i][j][0]]);
                g2d.fillPolygon(stickers[i][j]); // fill each sticker
            }

        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(3F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawPolygon(square); // draw the outer square
        g2d.setStroke(new BasicStroke(1.5F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        for(int i=1; i<size; i++) // draw horizontal inside lines
            g2d.drawLine(xs[1][i], ys[1][i], xs[3][i], ys[3][i]);
        for(int j=1; j<size; j++) // draw vertical inside lines
            g2d.drawLine(xs[0][j], ys[0][j], xs[2][j], ys[2][j]);

    } // end drawFace

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
/*
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
*/
//**********************************************************************************************************************

    private static final void cycle(int n0[], int n1[], int n2[], int n3[]){
        int temp = n3[0];
        n3[0] = n2[0];
        n2[0] = n1[0];
        n1[0] = n0[0];
        n0[0] = temp;
    }

}
