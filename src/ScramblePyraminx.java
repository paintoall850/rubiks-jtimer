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

public class ScramblePyraminx{
    private static final String[]   U = {"U", "U'"},
                                    L = {"L", "L'"},
                                    R = {"R", "R'"},
                                    B = {"B", "B'"};
    private int previousArray, previousFace, currentArray, currentFace;
    private String formatedMove;

//**********************************************************************************************************************

    private void generateCoreMove(){
        currentArray = (int)(Math.random()*4);
        currentFace = (int)(Math.random()*2);

        String[] arrayChoice = null;
        switch(currentArray){
            case 0: arrayChoice = U; break;
            case 1: arrayChoice = L; break;
            case 2: arrayChoice = R; break;
            case 3: arrayChoice = B; break;
        }

        formatedMove = arrayChoice[currentFace];
    }

//**********************************************************************************************************************

    public String generateScramble(){
        String scramble = "";

        int tipChoice, tipsTurned = 4;
        tipChoice = (int)(Math.random()*3);
        switch (tipChoice){
            case 0: tipsTurned--; break;
            case 1: scramble += " u"; break;
            case 2: scramble += " u'"; break;
        }
        tipChoice = (int)(Math.random()*3);
        switch (tipChoice){
            case 0: tipsTurned--; break;
            case 1: scramble += " l"; break;
            case 2: scramble += " l'"; break;
        }
        tipChoice = (int)(Math.random()*3);
        switch (tipChoice){
            case 0: tipsTurned--; break;
            case 1: scramble += " r"; break;
            case 2: scramble += " r'"; break;
        }
        tipChoice = (int)(Math.random()*3);
        switch(tipChoice){
            case 0: tipsTurned--; break;
            case 1: scramble += " b"; break;
            case 2: scramble += " b'"; break;
        }

        generateCoreMove();
        scramble += " " + formatedMove;
        previousArray = currentArray;
        previousFace = currentFace;

        //we have one move of the scramble. Now we need to generate the rest.
        for(int i=0; i<(24-tipsTurned); i++){
            generateCoreMove();
            while(previousArray == currentArray)
                generateCoreMove();
            scramble += " " + formatedMove;
            previousArray = currentArray;
            previousFace = currentFace;
        }

//        if(scramble.startsWith(" ")) // truncate leading space (if there is one, but there should be)
//            scramble = scramble.substring(1, scramble.length());
        return scramble.trim();
    } // end Generate Scramble
}
