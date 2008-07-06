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

/*
 * Algorithm shamelessly ripped from Jaap Scherphuis at:
 *  http://www.geocities.com/jaapsch/scramblesq1.htm
 *  
 *  In the interest of full disclosure, I do not fully understand the algorithm.
 *  
 *  A few minor scrambleString formatting tweaks were made, but
 *  all the rest is Jaap's
 */
public class ScrambleSquareOne {
    private int scrambleLength = 40;
    
    int[] seq;
    int[] posit;
    
    public ScrambleSquareOne(){
    }

    public String generateScramble(){
        seq = new int[scrambleLength];
        posit = new int[] {0,0,1,2,2,3,4,4,5,6,6,7,8,9,9,10,11,11,12,13,13,14,15,15};
        this.scramble();
        int l = -1, k;
        StringBuffer s = new StringBuffer();
      
        for(int i=0; i<scrambleLength; i++){
            if(i==0)
                s.append("(");
            k=seq[i];
            if(k==0){
                if(l==1)
                    s.append("0");
                s.append(") / (");
                l=0;
            }else if(k>0){
                s.append((k>6?k-12:k)+",");
                l=1;
            }else if(k<0){
                if(l<=0)
                    s.append("0,");
                s.append((k<=-6?k+12:k));
                l=2;
            }
        }
        if(l==1)
            s.append("0");
        s.append(")");
        
        int index = s.indexOf("()");
        while(index != -1){
            s.replace(index, index+2, "");
            index = s.indexOf("()");
        }
        return s.toString();
    }
    
    public void scramble(){
        int ls = -1, f = 0, j;
        for(int i = 0; i<scrambleLength; i++){
            do{
                if(ls == 0){
                    j = (int) Math.floor(Math.random()*22)-11;
                    if(j >= 0)
                        j++;
                }else if (ls == 1){
                    j = (int) Math.floor(Math.random()*12)-11;
                }else if (ls == 2){
                    j = 0;
                }else{
                    j = (int) Math.floor(Math.random()*12)-11;
                }
            }while( (f > 1 && j >= -6 && j < 0) || doMove(j));
            if(j>0)
                ls = 1;
            else if(j<0)
                ls = 2;
            else{
                ls = 0;
                f++;
            }
            seq[i] = j;
        }
    }
    
    public boolean doMove(int m){
        int temp,f=m;
        int[] t;
        if(f == 0){
            for(int i = 0; i<6; i++){
                temp = posit[i+12];
                posit[i+12] = posit[i+6];
                posit[i+6] = temp;
            }
        }else if(f > 0){
            f = 12 - f;
            if( posit[f] == posit[f-1] )
                return true;
            if( f < 6 && posit[f+6] == posit[f+5] )
                return true;
            if( f > 6 && posit[f-6] == posit[f-7] )
                return true;
            if( f == 6 && posit[0] == posit[11] )
                return true;
            t = new int[12];
            for(int i = 0; i<12; i++){
                t[i] = posit[i];
            }
            temp = f;
            for(int i = 0; i<12; i++){
                posit[i] = t[temp];
                if(temp == 11)
                    temp = 0;
                else
                    temp ++;
            }
        }else if (f < 0){
            f = -f;
            if(posit[f+12] == posit[f+11])
                return true;
            if( f < 6 && posit[f+18] == posit[f+17])
                return true;
            if( f > 6 && posit[f+6] == posit[f+5])
                return true;
            if( f == 6 && posit[12] == posit[23])
                return true;
            t = new int[12];
            for(int i = 0; i<12; i++){
                t[i] = posit[i+12];
            }
            temp = f;
            for(int i = 0; i<12; i++){
                posit[i+12]=t[temp];
                if(temp == 11)
                    temp = 0;
                else
                    temp++;
            }
        }
        return false;
    }
}
