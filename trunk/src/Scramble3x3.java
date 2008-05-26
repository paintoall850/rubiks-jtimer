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

public class Scramble3x3{
    private static final String[]   UandD = {"U", "U'", "U2", "D", "D'", "D2"},
                                    FandB = {"F", "F'", "F2", "B", "B'", "B2"},
                                    LandR = {"L", "L'", "L2", "R", "R'", "R2"};
    private int oldArray, oldFace, previousArray, previousFace, currentArray, currentFace;
    private String formatedMove;

    public Scramble3x3(){
    }

    private void generateMove(){
        currentArray = (int)(Math.random()*3);
        currentFace = (int)(Math.random()*6);

        String[] arrayChoice = null;
        switch (currentArray){
            case 0: arrayChoice = UandD; break;
            case 1: arrayChoice = FandB; break;
            case 2: arrayChoice = LandR; break;
        }

        formatedMove = arrayChoice[currentFace];
    }

    public String generateScramble(){
        String scramble = "";
        generateMove();
        scramble = formatedMove;
        previousArray = currentArray;
        previousFace = currentFace;

        do generateMove();
        while(isSameFace(currentArray, currentFace, previousArray, previousFace));

        scramble += " " + formatedMove;
        oldArray = previousArray;
        oldFace = previousFace;
        previousArray = currentArray;
        previousFace = currentFace;

        //we have two moves of the scramble. Now we need to generate the rest.
        for(int i=0; i<23; i++){
            generateMove();

            //If the first two moves are parallel
            if(isParallel(previousArray, oldArray)){
                //loop until third move is not parallel
                while(isParallel(currentArray, previousArray))
                    generateMove();
            } else {
                //loop until the next move is not the same face as the previous
                while(isSameFace(currentArray, currentFace, previousArray, previousFace))
                    generateMove();
            }

            scramble += " " + formatedMove;

            oldArray = previousArray;
            oldFace = previousFace;
            previousArray = currentArray;
            previousFace = currentFace;
        }

        return scramble;
    } // end Generate Scramble

    private boolean isSameFace(int thisArray, int thisFace, int thatArray, int thatFace){
        if(thisArray == thatArray)
            return (((thisFace == 0 || thisFace == 1 || thisFace == 2) && (thatFace == 0 || thatFace == 1 || thatFace == 2)) ||
                    ((thisFace == 3 || thisFace == 4 || thisFace == 5) && (thatFace == 3 || thatFace == 4 || thatFace == 5)));
        else
            return false;
    } // end isSameFace

    private boolean isParallel(int thisArray, int thatArray){
        return (thisArray == thatArray);
    } // end isParallel
}
