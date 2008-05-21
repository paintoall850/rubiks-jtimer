/**
  * Megaminx Scramble Generator by Daniel Hayes
  * Jan, 9 2008
  *
  * This generates scrambles for the megaminx using Stefan Pochmann's notation:
  *
  * http://www.stefan-pochmann.info/spocc/other_stuff/tools/scramble_megaminx/
  *
  * The main difference is that it will interspers "Y" turns randomly instead of
  * at set intervals.  It should avoid silly scrambles such as D++ Y- D- Y++.
  * The default length is 60 non-"Y" turns.
  *
  * Edit as you wish, but please don't sell it!
  */
import java.util.Vector;

public class ScrambleMegaminx{
    private final String[][] faces = {{"R"}, {"D", "Y"}};
    private final String[] directions = {"-", "--", "+", "++"};
    private final int defaultLength = 70;

    public ScrambleMegaminx(){}

    public String generateScramble(int length){
        String scramble = "";
        //StringBuffer scramble = new StringBuffer(); //output buffer
        Vector<Integer> facesFromLastPlane = new Vector<Integer>(0); //keeps track of which faces have been used for this round of this plane
        int lastPlane = -1;
        int plane = -1;
        int face = -1;
        int direction = -1;
        for(int i=0; i<length; i++){
            plane = (int)Math.floor(Math.random()*faces.length);
            if(plane==lastPlane){
                if(facesFromLastPlane.size() == faces[plane].length){
                    //if all the faces from this plane have been used, get a new plane
                    while(plane==lastPlane)
                        plane = (int)Math.floor(Math.random()*faces.length);
                    facesFromLastPlane = new Vector<Integer>(0);
                }
            }else{
                facesFromLastPlane = new Vector<Integer>(0);
            }
            face = (int)Math.floor(Math.random()*faces[plane].length);
            while(facesFromLastPlane.contains(face)){
                //select only unused faces from this plane
                face = (int)Math.floor(Math.random()*faces[plane].length);
            }
            facesFromLastPlane.add(face);
            direction = (int)Math.floor(Math.random()*directions.length);
            //puzzle rotations don't count as twists for scrambling
            if(plane==1 && face==1)
                i--;
            if(scramble == "")
                scramble = faces[plane][face] + directions[direction];
            else
                scramble = scramble + " " + faces[plane][face] + directions[direction];
            lastPlane = plane;
        }
        return scramble.toString();
    }

    public String generateScramble(){
//        return generateScramble(defaultLength);

        String scramble = "";
        int rand = 0;
        for(int line=0; line<7; line++){
            if(line != 0) scramble = scramble + ".";//"          ";// was "@" briefly too
            for(int i=0; i<10; i++){
                rand = (int)(Math.random()*2);
                scramble = scramble + (i%2==1 ? "D" : "R") + (rand==1 ? "++ " : "-- ");
            }
            //rand = (int)(Math.random()*2);
            scramble = scramble + "U" + (rand==1 ? "" : "'"); // force the U to be tied with last ++/--
        }
        return scramble;

    }
}
