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

public class Scramble4x4{
    private String[][] UandD, FandB, LandR;
    private int deadArray, deadGroup, deadFace, oldArray, oldGroup, oldFace, previousArray, previousGroup, previousFace, currentArray, currentGroup, currentFace;
    private String formatedMove;

    public Scramble4x4(){
        UandD = new String[3][4];
        UandD[0][0] = "U";
        UandD[0][1] = "u";
        UandD[0][2] = "d'";
        UandD[0][3] = "D'";
        UandD[1][0] = "U'";
        UandD[1][1] = "u'";
        UandD[1][2] = "d";
        UandD[1][3] = "D";
        UandD[2][0] = "U2";
        UandD[2][1] = "u2";
        UandD[2][2] = "d2";
        UandD[2][3] = "D2";

        FandB = new String[3][4];
        FandB[0][0] = "F";
        FandB[0][1] = "f";
        FandB[0][2] = "b";
        FandB[0][3] = "B'";
        FandB[1][0] = "F'";
        FandB[1][1] = "f'";
        FandB[1][2] = "b";
        FandB[1][3] = "B";
        FandB[2][0] = "F2";
        FandB[2][1] = "f2";
        FandB[2][2] = "b2";
        FandB[2][3] = "B2";

        LandR = new String[3][4];
        LandR[0][0] = "L";
        LandR[0][1] = "l";
        LandR[0][2] = "r'";
        LandR[0][3] = "R'";
        LandR[1][0] = "L'";
        LandR[1][1] = "l'";
        LandR[1][2] = "r";
        LandR[1][3] = "R";
        LandR[2][0] = "L2";
        LandR[2][1] = "l2";
        LandR[2][2] = "r2";
        LandR[2][3] = "R2";
    }

    private void generateMove(){
        currentArray = (int)(Math.random()*3);
        currentGroup = (int)(Math.random()*3);
        currentFace = (int)(Math.random()*4);

        String[][] arrayChoice = null;
        switch (currentArray){
            case 0: arrayChoice = UandD; break;
            case 1: arrayChoice = FandB; break;
            case 2: arrayChoice = LandR; break;
        }

        formatedMove = arrayChoice[currentGroup][currentFace];
    }

    public String generateScramble(){
        String scramble = "";
        generateMove();
        scramble = formatedMove;

        previousArray = currentArray;
        previousGroup = currentGroup;
        previousFace = currentFace;

        do generateMove();
        while(isSameFace(currentArray, currentFace, previousArray, previousFace));

        scramble += " " + formatedMove;

        oldArray = previousArray;
        oldGroup = previousGroup;
        oldFace = previousFace;
        previousArray = currentArray;
        previousGroup = currentGroup;
        previousFace = currentFace;

        generateMove();

        //If the first two moves are parallel
        if(isParallel(previousArray, oldArray)){
            //If the first two moves are in the same direction
            if(movesSameDirection(previousArray, previousGroup, oldArray, oldGroup)){
                //loop until the next move is an intersecting face
                while(isParallel(currentArray, previousArray))
                    generateMove();
            } else {
                //loop until the next move is not in the same group as either of the first two moves AND is not the same face
                while((movesSameDirection(currentArray, currentGroup, previousArray, previousGroup) ||
                                    movesSameDirection(currentArray, currentGroup, oldArray, oldGroup)) ||
                                    isSameFace(currentArray, currentFace, previousArray, previousFace))
                    generateMove();
            }
        } else {
            //loop until the next move is not the same face as the previous
            while(isSameFace(currentArray, currentFace, previousArray, previousFace))
                generateMove();
        }

        //add third move to scramble
        scramble += " " + formatedMove;

        deadArray = oldArray;
        deadGroup = oldGroup;
        deadFace = oldFace;

        oldArray = previousArray;
        oldGroup = previousGroup;
        oldFace = previousFace;

        previousArray = currentArray;
        previousGroup = currentGroup;
        previousFace = currentFace;

        //we have three moves of the scramble. Now we need to generate the rest.
        for(int i=0; i<37; i++){
            generateMove();

            //if three moves before this one are all parellel, then make this move on an intersecting face
            if(isParallel(previousArray, oldArray) && isParallel(previousArray, deadArray)){
                while(isParallel(currentArray, previousArray))
                    generateMove();
            } else {
                //If the first two moves are parallel
                if(isParallel(previousArray, oldArray)){
                    //If the first two moves are in the same direction
                    if(movesSameDirection(previousArray, previousGroup, oldArray, oldGroup)){
                        //loop until the next move is an intersecting face
                        while(isParallel(currentArray, previousArray))
                            generateMove();
                    } else
                        //loop until the next move is not in the same group as either of the first two moves AND is not the same face
                        while(movesSameDirection(currentArray, currentGroup, previousArray, previousGroup) || movesSameDirection(currentArray, currentGroup, oldArray, oldGroup) || isSameFace(currentArray, currentFace, previousArray, previousFace)){
                            generateMove();
                    }
                } else {
                    //loop until the next move is not the same face as the previous
                    while(isSameFace(currentArray, currentFace, previousArray, previousFace))
                        generateMove();
                }
            }

            scramble += " " + formatedMove;

            deadArray = oldArray;
            deadGroup = oldGroup;
            deadFace = oldFace;

            oldArray = previousArray;
            oldGroup = previousGroup;
            oldFace = previousFace;

            previousArray = currentArray;
            previousGroup = currentGroup;
            previousFace = currentFace;
        }

        return scramble;
    } // end Generate Scramble

    private boolean isSameFace(int thisArray, int thisFace, int thatArray, int thatFace){
        return ((thisArray == thatArray) && (thisFace == thatFace));
    } // end isSameFace

    private boolean isParallel(int thisArray, int thatArray){
        return (thisArray == thatArray);
    } // end isParallel

    private boolean movesSameDirection(int thisArray, int thisGroup, int thatArray, int thatGroup){
        return (((thisArray == thatArray) && (thisGroup == thatGroup)) ||
                ((thisArray == thatArray) && (thisGroup == 2)) ||
                ((thisArray == thatArray) && (thatGroup == 2)));
    } // end movesSameDirection
}
