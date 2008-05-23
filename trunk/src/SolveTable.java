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

import java.text.*;
import java.util.*;

public class SolveTable implements Constants{

    private Hashtable<String, Vector<Solve>> myTable = new Hashtable<String, Vector<Solve>>(10);
    private String currentPuzzle;
    private DecimalFormat ssxx, ss;
    private boolean showMinutes, truncate, verbose;

//**********************************************************************************************************************

    public SolveTable(String puzzle){
        setPuzzle(puzzle);
        ssxx = (DecimalFormat)NumberFormat.getNumberInstance(new Locale("en", "US")); ssxx.applyPattern("00.00");
        ss = (DecimalFormat)NumberFormat.getNumberInstance(new Locale("en", "US")); ss.applyPattern("00");
        setTimeStyle(true, false, false);
    }

//**********************************************************************************************************************

    public void setPuzzle(String puzzle){
        currentPuzzle = puzzle;

        if(!myTable.containsKey(currentPuzzle))
            myTable.put(currentPuzzle, new Vector<Solve>(100, 100));
    }

//**********************************************************************************************************************

    public String getPuzzle(){
        return currentPuzzle;
    }

//**********************************************************************************************************************

    public void addSolve(int time100, String scramble, boolean isPop, boolean isPlus2){
        Solve solve = new Solve(time100, scramble, isPop, isPlus2);
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

    public float getTime(int i){
        Solve solve = myTable.get(currentPuzzle).get(i);
        if(solve.isPop)
            return INF;
        if(solve.isPlus2)
            return (solve.time100 + 200)/100F;
        return solve.time100/100F;
    }

//**********************************************************************************************************************

    public String getString(int i){
        Solve solve = myTable.get(currentPuzzle).get(i);
        if(solve.isPop) return "POP";
        return formatTime(solve);
    }

//**********************************************************************************************************************

    public boolean getPopQ(int i){
        return myTable.get(currentPuzzle).get(i).isPop;
    }

//**********************************************************************************************************************

    public class Solve{
        int time100;
        String scramble;
        boolean isPop, isPlus2;

        float rollingAverage = INF, rollingStdDev = INF;
        float rollingFastestTime = INF, rollingSlowestTime = 0;
        int rollingFastestIndex = 0, rollingSlowestIndex = 0;

        float sessionAverage = INF, sessionStdDev = INF;
        float sessionFastestTime = INF, sessionSlowestTime = 0;
        int sessionFastestIndex = 0, sessionSlowestIndex = 0;
        int numberSolves = 0, numberPops = 0;

        public Solve(int time100, String scramble, boolean isPop, boolean isPlus2){
            this.time100 = time100;
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
        float time = solve.time100/100F;
        if(solve.isPlus2) time += 2;
        if(time>=60 && showMinutes){
            int min = (int)(time/60);
            float sec = time-min*60;
            s = min + ":" + ((time < 600 || !truncate) ? ssxx.format(sec) : ss.format(sec))
                    + (solve.isPlus2 ? "+" : "");
        } else
            s = ssxx.format(time) + (solve.isPlus2 ? "+" : "") + (verbose ? " sec." : "");
        return s;
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    public void sessionReset(){
        myTable.get(currentPuzzle).clear();
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
                float time = getTime(index);
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
                solve.rollingStdDev = (float)Math.sqrt(solve.rollingStdDev/(12-3));
            }
        }

        solve.sessionAverage = 0;
        solve.sessionFastestTime = INF; solve.sessionSlowestTime = 0;
        solve.sessionFastestIndex = size-1; solve.sessionSlowestIndex = 0;
        solve.numberSolves = 0; solve.numberPops = 0;
        for(int index=0; index<size; index++){
            float time = getTime(index);
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
            solve.sessionStdDev = (float)Math.sqrt(solve.sessionStdDev/(solve.numberSolves-1));
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
        float temp = getSolve(0).rollingAverage;
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

}
