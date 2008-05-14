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

public class Scramble2x2{
    private String[]    UandD = {"U","U'","U2",
                                 "D","D'","D2"},
                        FandB = {"F","F'","F2",
                                 "B","B'","B2"},
                        LandR = {"L","L'","L2",
                                 "R","R'","R2"};
    private int previousArray, previousFace, currentArray, currentFace;
    private String formatedMove;

    public Scramble2x2(){}

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

        for(int i=0; i<24; i++){
            do generateMove();
            while(isSameFace(currentArray, currentFace, previousArray, previousFace) || isParallel(currentArray, previousArray));
            scramble = scramble + " " + formatedMove;
            previousArray = currentArray;
            previousFace = currentFace;
        }

        return scramble;
    } // end Generate Scramble

    private boolean isSameFace(int thisArray, int thisFace, int thatArray, int thatFace){
        if(thisArray == thatArray){
            if(((thisFace == 0 || thisFace == 1 || thisFace == 2) && (thatFace == 0 || thatFace == 1 || thatFace == 2)) ||
               ((thisFace == 3 || thisFace == 4 || thisFace == 5) && (thatFace == 3 || thatFace == 4 || thatFace == 5)))
                return true;
            else
                return false;
        } else
            return false;
    } // end isSameFace

    private boolean isParallel(int thisArray, int thatArray){
        return (thisArray == thatArray);
    } // end isParallel
}
