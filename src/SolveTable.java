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

import java.text.*;
import java.util.*;

public class SolveTable implements Constants{

    private Hashtable<String, Vector<Solve>> myTable = new Hashtable<String, Vector<Solve>>(10);
    private String currentPuzzle;
    private boolean showMinutes, truncate, verbose;

//**********************************************************************************************************************

    public SolveTable(String puzzle){
        setPuzzle(puzzle);
        setTimeStyle(true, false, false);
    }

//**********************************************************************************************************************

    public void setPuzzle(String puzzle){
        currentPuzzle = puzzle;

        if(!myTable.containsKey(currentPuzzle))
            myTable.put(currentPuzzle, new Vector<Solve>(40, 20));
    }

//**********************************************************************************************************************

    public String getPuzzle(){
        return currentPuzzle;
    }

//**********************************************************************************************************************

    public void addSolve(double time, String scramble, boolean isPop, boolean isPlus2){
        Solve solve = new Solve(RJT_Utils.roundTime(time), scramble, isPop, isPlus2);
        myTable.get(currentPuzzle).add(solve);
        computeStats(getSize()-1);
    }

//**********************************************************************************************************************

    public int getSize(){
        return myTable.get(currentPuzzle).size();
    }

//**********************************************************************************************************************

    public Solve getSolve(int i){
        return myTable.get(currentPuzzle).get(i);
    }

//**********************************************************************************************************************

    public double getTime(int i){
        Solve solve = myTable.get(currentPuzzle).get(i);
        if(solve.isPop)
            return INF;
        if(solve.isPlus2)
            return solve.time + 2;
        return solve.time;
    }

//**********************************************************************************************************************

    public String getString(int i){
        Solve solve = myTable.get(currentPuzzle).get(i);
        if(solve.isPop) return "POP";
        return formatTime(solve);
    }

//**********************************************************************************************************************

    public String getScramble(int i){
        Solve solve = myTable.get(currentPuzzle).get(i);
        return solve.scramble;
    }

//**********************************************************************************************************************

    public boolean getPopQ(int i){
        return myTable.get(currentPuzzle).get(i).isPop;
    }

//**********************************************************************************************************************

    public class Solve{
        double time;
        String scramble;
        boolean isPop, isPlus2;

        double rollingAverage = INF, rollingStdDev = INF;
        double rollingFastestTime = INF, rollingSlowestTime = 0;
        int rollingFastestIndex = 0, rollingSlowestIndex = 0;

        double sessionAverage = INF, sessionStdDev = INF;
        double sessionFastestTime = INF, sessionSlowestTime = 0;
        int sessionFastestIndex = 0, sessionSlowestIndex = 0;
        int numberSolves = 0, numberPops = 0;

        public Solve(double time, String scramble, boolean isPop, boolean isPlus2){
            this.time = time;
            this.scramble = scramble;
            this.isPop = isPop;
            this.isPlus2 = isPlus2;
        }
    }

//**********************************************************************************************************************

    public void setTimeStyle(boolean showMinutes, boolean truncate, boolean verbose){
        this.showMinutes = showMinutes;
        this.truncate = truncate;
        this.verbose = verbose;
    }

//**********************************************************************************************************************

    private final String formatTime(Solve solve){
        String s;
        double time = solve.time;
        if(solve.isPlus2) time += 2;
        if(time>=60 && showMinutes){
            int min = (int)(time/60);
            double sec = time-min*60;
            s = min + ":" + (((time < 600 && !solve.isPlus2) || !truncate) ? RJT_Utils.ssxx_format(sec) : RJT_Utils.ss_format(sec))
                    + (solve.isPlus2 ? "+" : "");
        } else
            s = RJT_Utils.ssxx_format(time) + (solve.isPlus2 ? "+" : "") + (verbose ? " sec." : "");
        return s;
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    public void sessionReset(){
        myTable.get(currentPuzzle).clear();
    }

//**********************************************************************************************************************

    // wrapper function, not yet used
    // very useful when we want to change the window of RAs from 10:12 to say 3:5
    public void updateAllStats(){
        updateStatsBeginningAt(0);
    }

//**********************************************************************************************************************

    // not yet used, for updating computed stats down the list starting at point-of-change
    // updates all if called with index=0
    public void updateStatsBeginningAt(int index){
        int size = getSize();
        for(int i=index; i<size; i++)
            computeStats(i);
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private void computeStats(int solve_index){
        Solve solve = myTable.get(currentPuzzle).get(solve_index);
        final int size = solve_index + 1;

        if(size >= 12){
            solve.rollingFastestTime = INF; solve.rollingSlowestTime = 0;
            solve.rollingFastestIndex = size-1; solve.rollingSlowestIndex = size-12;
            for(int i=0; i<12; i++){
                int index = size+i-12;
                double time = getTime(index);
                if(time <= solve.rollingFastestTime){
                    solve.rollingFastestTime = time;
                    solve.rollingFastestIndex = index;
                }
                if(time > solve.rollingSlowestTime){
                    solve.rollingSlowestTime = time;
                    solve.rollingSlowestIndex = index;
                }
            }
            solve.rollingAverage = 0;
            for(int i=0; i<12; i++){
                int index = size+i-12;
                if(index!=solve.rollingFastestIndex && index!=solve.rollingSlowestIndex)
                    solve.rollingAverage += getTime(index);
            }
            solve.rollingAverage /= 10;

            if(solve.rollingAverage != INF){
                solve.rollingStdDev = 0;
                for(int i=0; i<12; i++){
                    int index = size+i-12;
                    if(index!=solve.rollingFastestIndex && index!=solve.rollingSlowestIndex)
                        solve.rollingStdDev += (solve.rollingAverage-getTime(index)) * (solve.rollingAverage-getTime(index));
                }
                solve.rollingStdDev = Math.sqrt(solve.rollingStdDev/(12-3));
            }
        }

        solve.sessionAverage = 0;
        solve.sessionFastestTime = INF; solve.sessionSlowestTime = 0;
        solve.sessionFastestIndex = size-1; solve.sessionSlowestIndex = 0;
        solve.numberSolves = 0; solve.numberPops = 0;
        for(int index=0; index<size; index++){
            double time = getTime(index);
            if(time <= solve.sessionFastestTime){
                solve.sessionFastestTime = time;
                solve.sessionFastestIndex = index;
            }
            if(time > solve.sessionSlowestTime){
                solve.sessionSlowestTime = time;
                solve.sessionSlowestIndex = index;
            }
            if(!getPopQ(index)){
                solve.sessionAverage += time;
                solve.numberSolves++;
            } else{
                solve.numberPops++;
            }
        }
        if(solve.numberSolves > 0)
            solve.sessionAverage /= solve.numberSolves;
        else
            solve.sessionAverage = INF;

        solve.sessionStdDev = 0;
        for(int index=0; index<size; index++)
            if(!getPopQ(index))
                solve.sessionStdDev += (solve.sessionAverage-getTime(index)) * (solve.sessionAverage-getTime(index));
        if(solve.numberSolves > 1)
            solve.sessionStdDev = Math.sqrt(solve.sessionStdDev/(solve.numberSolves-1));
        else
            solve.sessionStdDev = INF;
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    public int findBestRolling(){
        int size = getSize();
        if(size < 12) return -1;

        Solve solve = getSolve(0);
        int index = 0;
        double temp = getSolve(0).rollingAverage;
        for(int i=1; i<size; i++)
            if(getSolve(i).rollingAverage <= temp){
                temp = getSolve(i).rollingAverage;
                index = i;
            }

        if(temp == INF)
            return -1;
        else
            return index;
    }

//**********************************************************************************************************************

    public boolean okayToPop(){
        int size = getSize();

        int index;
        for(index = size-1; index >= 0; index--)
            if(getPopQ(index)) break;

        if(index == -1) return true;
        return (size-index) >= 12;
    }

}
