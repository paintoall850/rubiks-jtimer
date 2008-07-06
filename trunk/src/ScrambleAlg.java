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

public class ScrambleAlg{
    Scramble2x2 cube2x2;
    Scramble3x3 cube3x3;
    Scramble4x4 cube4x4;
    Scramble5x5 cube5x5;
    ScramblePyraminx pyraminx;
    ScrambleMegaminx megaminx;
    ScrambleSquareOne squareOne;

//**********************************************************************************************************************

    public ScrambleAlg(){
        cube2x2 = new Scramble2x2();
        cube3x3 = new Scramble3x3();
        cube4x4 = new Scramble4x4();
        cube5x5 = new Scramble5x5();
        pyraminx  = new ScramblePyraminx();
        megaminx  = new ScrambleMegaminx();
        squareOne = new ScrambleSquareOne();
    }

//**********************************************************************************************************************

    public String generateAlg(String puzzle){
        if(puzzle.equals("2x2x2"))
            return cube2x2.generateScramble();
        if(puzzle.equals("3x3x3"))
            return cube3x3.generateScramble();
        if(puzzle.equals("4x4x4"))
            return cube4x4.generateScramble();
        if(puzzle.equals("5x5x5"))
            return cube5x5.generateScramble();
        if(puzzle.equals("Pyraminx"))
            return pyraminx.generateScramble();
        if(puzzle.equals("Megaminx"))
            return megaminx.generateScramble();
        if(puzzle.equals("Square One"))
        	return squareOne.generateScramble();
        return null;
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

// incorporate Scramble_.java code as sub-clsses here... eventually?

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

}
