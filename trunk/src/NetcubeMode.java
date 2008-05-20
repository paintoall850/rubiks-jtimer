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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import javax.swing.text.*;
import java.applet.*;
import java.text.*;
import java.util.*;
import javax.swing.border.Border;

public abstract class NetcubeMode extends JFrame implements ActionListener, KeyListener, Runnable, Constants{
    protected Color myBackgrColor;

    JLabel usernameLabel, serverIpLabel, serverPortLabel, handicapLabel;
    JLabel useThisAlgLabel, timerLabel, localTimeLabel, remoteTimeLabel, localTimeUsernameLabel, remoteTimeUsernameLabel;
    JLabel puzzleLabel, countdownLabel, bigPicture, smallPicture, userIsTyping;
    JTextField usernameText, serverIpText, serverPortText, handicapText, chatText;
    JButton connectButton, sendMessageButton, localSessionDetailButton, localAverageDetailButton, remoteSessionDetailButton, remoteAverageDetailButton, startButton, popButton;
    JTextPane chatPane;
    StyledDocument chatDoc;
    Style redStyle, blueStyle, blackStyle;
    AudioClip chatSound, countdownClip, bing1, bing2, startupClip;
    JTextArea scrambleText;
    String remoteUsername;
    JComboBox puzzleCombo, countdownCombo;
    JScrollPane chatScrollPane;
    java.util.Timer timerThread;
    int countdown;
    double startTime, stopTime;
    DecimalFormat timeFormat;
    NumberFormat nf;
    String remoteTime;
    boolean isTyping, remoteIsTyping;
    ImageIcon typeOn, typeOff;

    //data storage
    String[] localSessionTimes, remoteSessionTimes, sessionScrambles, localCurrentAverage, remoteCurrentAverage, localCurrentScrambles, remoteCurrentScrambles;
    int localCubesSolved, remoteCubesSolved, sessionIndex, localCurrentPlaceInAverage, remoteCurrentPlaceInAverage, localNumOfPops, remoteNumOfPops, localScore, remoteScore, acceptsSincePop;
    double localTotalTime, remoteTotalTime, localSessionFastest, remoteSessionFastest, localSessionSlowest, remoteSessionSlowest, localCurrentFastest, remoteCurrentFastest, localCurrentSlowest, remoteCurrentSlowest, localCurrentRollingAverage, remoteCurrentRollingAverage, localCurrentSessionAverage, remoteCurrentSessionAverage;

    //network stuff
    Socket clientSocket;
    BufferedReader in;
    PrintWriter out;

    //listening thread
    Thread chatListener;

//**********************************************************************************************************************

    public NetcubeMode(Color textBackgrColor){
        // configure JFrame
        setSize(727, 544);
        setIconImage((new ImageIcon(getClass().getResource("Cow.gif"))).getImage());
        setResizable(false);

        // center frame on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int appWidth = getSize().width, appHeight = getSize().height;
        setLocation((screenSize.width-appWidth)/2, (screenSize.height-appHeight)/2);

        try { //configure chatSound
            chatSound = Applet.newAudioClip(getClass().getResource("blip.wav"));
        } catch(NullPointerException e){JOptionPane.showMessageDialog(this, "blip.wav not found. There will be no audio when a message is recieved.");}

        try { //configure countdownClip
            countdownClip = Applet.newAudioClip(getClass().getResource("count.mid"));
        } catch(NullPointerException e){JOptionPane.showMessageDialog(this, "count.mid not found. There will be no audio during countdown.");}

        try { //configure bing1
            bing1 = Applet.newAudioClip(getClass().getResource("bing1.wav"));
        } catch(NullPointerException e){JOptionPane.showMessageDialog(this, "bing1.wav not found. There will be no 'ready' sound.");}

        try { //configure bing2
            bing2 = Applet.newAudioClip(getClass().getResource("bing2.wav"));
        } catch(NullPointerException e){JOptionPane.showMessageDialog(this, "bing2.wav not found. There will be no 'ready' sound.");}

        try { //configure startupClip
            startupClip = Applet.newAudioClip(getClass().getResource("startup.wav"));
        } catch(NullPointerException e){JOptionPane.showMessageDialog(this, "startup.wav not found. There will be no startup sound.");}

        nf = NumberFormat.getNumberInstance(new Locale("en", "US"));
        timeFormat = (DecimalFormat)nf;
        timeFormat.applyPattern("00.00");

        myBackgrColor = textBackgrColor;

        //GUI Object creation
        usernameLabel = new JLabel("Username:");
        serverIpLabel = new JLabel("Server IP:");
        serverPortLabel = new JLabel("Server Port:");
        handicapLabel = new JLabel("Handicap:");
        usernameText = new JTextField();
        serverIpText = new JTextField(); // override in sub-class
        serverPortText = new JTextField("52003");
        handicapText = new JTextField();
        connectButton = new JButton(); // override in sub-class

        typeOff = new ImageIcon(getClass().getResource("typeOff.gif"));
        typeOn = new ImageIcon(getClass().getResource("typeOn.gif"));
        userIsTyping = new JLabel(typeOff);

        chatPane = new JTextPane(chatDoc = new DefaultStyledDocument());
        chatPane.setEditable(false);
        chatScrollPane = new JScrollPane(chatPane);
        chatScrollPane.setBorder(blackLine);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        redStyle = chatPane.addStyle("red", null);
        blueStyle = chatPane.addStyle("blue", null);
        blackStyle = chatPane.addStyle("black", null);
        StyleConstants.setForeground(redStyle, Color.red);
        StyleConstants.setForeground(blueStyle, Color.blue);
        StyleConstants.setForeground(blackStyle, Color.black);

        chatText = new JTextField();
        chatText.addKeyListener(this);
        sendMessageButton = new JButton("Send");

        useThisAlgLabel = new JLabel("Use this Scramble Algorithm:");
        useThisAlgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scrambleText = new JTextArea("");
        scrambleText.setFocusable(true);
        scrambleText.setEditable(false);
        scrambleText.setLineWrap(true);
        scrambleText.setWrapStyleWord(true);
        scrambleText.setBackground(myBackgrColor);
        scrambleText.setForeground(Color.black);
        scrambleText.setBorder(blackLine);

        timerLabel = new JLabel("");
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setFont(new Font("Serif", Font.PLAIN, 94));

        localTimeUsernameLabel = new JLabel("<html>Rolling Average: <FONT SIZE=\"5\">N/A</FONT><br>Session Average: N/A<br><br>Score: 0<br>Session Fastest Time: N/A<br>Session Slowest Time: N/A</html>");
        remoteTimeUsernameLabel = new JLabel("<html>Rolling Average: <FONT SIZE=\"5\">N/A</FONT><br>Session Average: N/A<br><br>Score: 0<br>Session Fastest Time: N/A<br>Session Slowest Time: N/A</html>");
        localTimeLabel = new JLabel(""); // will override
        remoteTimeLabel = new JLabel(""); // will override
        localTimeUsernameLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        remoteTimeUsernameLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        localTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        remoteTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        localTimeLabel.setFont(new Font("Serif", Font.PLAIN, 60));
        remoteTimeLabel.setFont(new Font("Serif", Font.PLAIN, 60));

        localTimeUsernameLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Local Statistics"));
        remoteTimeUsernameLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Remote Statistics"));

        localAverageDetailButton = new JButton("View Rolling");
        localSessionDetailButton = new JButton("View Session");
        remoteAverageDetailButton = new JButton("View Rolling");
        remoteSessionDetailButton = new JButton("View Session");

        puzzleLabel = new JLabel("Puzzle:");
        puzzleCombo = new JComboBox(puzzleChoices);
        countdownLabel = new JLabel("Countdown:");
        countdownCombo = new JComboBox(countdownChoices);
        startButton = new JButton(); // will override
        popButton = new JButton("POP");

        bigPicture = new JLabel((new ImageIcon(getClass().getResource("bigPicture.jpg"))));
        smallPicture = new JLabel((new ImageIcon(getClass().getResource("smallPicture.jpg"))));
        bigPicture.setBorder(blackLine);
        smallPicture.setBorder(blackLine);

        //set everything to defaults
        reset();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

//**********************************************************************************************************************

    protected void setTheBounds(){

        usernameLabel.setBounds(10,10,80,20);
        serverIpLabel.setBounds(10,35,80,20);
        serverPortLabel.setBounds(10,60,80,20);
        handicapLabel.setBounds(10,85,80,20);
        usernameText.setBounds(90,10,80,20);
        serverIpText.setBounds(90,35,80,20);
        serverPortText.setBounds(90,60,80,20);
        handicapText.setBounds(90,85,80,20);
        connectButton.setBounds(10,110,160,20);
        chatScrollPane.setBounds(180,10,350,95);
        userIsTyping.setBounds(180,110,20,20);
        chatText.setBounds(205,110,235,20);
        sendMessageButton.setBounds(450,110,80,20);
        useThisAlgLabel.setBounds(10,140,700,20);
        scrambleText.setBounds(10,160,700,50);
        timerLabel.setBounds(10,220,700,75);
        localTimeUsernameLabel.setBounds(10,305,345,200);
        remoteTimeUsernameLabel.setBounds(365,305,345,200);
        localTimeLabel.setBounds(20,325,325,75);
        remoteTimeLabel.setBounds(375,325,325,75);
        localAverageDetailButton.setBounds(215,405,120,20);
        localSessionDetailButton.setBounds(215,430,120,20);
        remoteAverageDetailButton.setBounds(570,405,120,20);
        remoteSessionDetailButton.setBounds(570,430,120,20);
        puzzleLabel.setBounds(540,15,80,20);
        puzzleCombo.setBounds(540,35,80,20);
        countdownLabel.setBounds(540+90,15,80,20);
        countdownCombo.setBounds(540+90,35,80,20);
        startButton.setBounds(540,60,170,20);
        popButton.setBounds(540,85,170,20);
        smallPicture.setBounds(210,10,500,120);
        bigPicture.setBounds(10,150,700,355);
    }

//**********************************************************************************************************************

    protected void addTheContent(Container contentPane){

        contentPane.add(usernameLabel);
        contentPane.add(serverIpLabel);
        contentPane.add(serverPortLabel);
        contentPane.add(handicapLabel);
        contentPane.add(usernameText);
        contentPane.add(serverIpText);
        contentPane.add(serverPortText);
        contentPane.add(handicapText);
        contentPane.add(connectButton);
        contentPane.add(chatScrollPane);
        contentPane.add(userIsTyping);
        contentPane.add(chatText);
        contentPane.add(sendMessageButton);
        contentPane.add(useThisAlgLabel);
        contentPane.add(scrambleText);
        contentPane.add(timerLabel);
        contentPane.add(localTimeUsernameLabel);
        contentPane.add(remoteTimeUsernameLabel);
        contentPane.add(localTimeLabel);
        contentPane.add(remoteTimeLabel);
        contentPane.add(localAverageDetailButton);
        contentPane.add(localSessionDetailButton);
        contentPane.add(remoteAverageDetailButton);
        contentPane.add(remoteSessionDetailButton);
        contentPane.add(puzzleLabel);
        contentPane.add(puzzleCombo);
        contentPane.add(countdownLabel);
        contentPane.add(countdownCombo);
        contentPane.add(startButton);
        contentPane.add(popButton);
        //contentPane.add(smallPicture);
        //contentPane.add(bigPicture);
    }

//**********************************************************************************************************************

    protected void addActionListeners(){
        connectButton.addActionListener(this);
        sendMessageButton.addActionListener(this);
        chatText.addActionListener(this);
        startButton.addActionListener(this);
        popButton.addActionListener(this);
        localSessionDetailButton.addActionListener(this);
        localAverageDetailButton.addActionListener(this);
        remoteSessionDetailButton.addActionListener(this);
        remoteAverageDetailButton.addActionListener(this);
    }

//**********************************************************************************************************************

    protected void prepGUI(){
        chatText.setEnabled(false);
        sendMessageButton.setEnabled(false);
        startButton.setEnabled(false);
        popButton.setEnabled(false);
        localAverageDetailButton.setEnabled(false);
        localSessionDetailButton.setEnabled(false);
        remoteAverageDetailButton.setEnabled(false);
        remoteSessionDetailButton.setEnabled(false);
    }

//**********************************************************************************************************************

    protected void hideGUI(){
        //usernameLabel.setVisible(true);
        //serverIpLabel.setVisible(true);
        //serverPortLabel.setVisible(true);
        //handicapLabel.setVisible(true);
        //usernameText.setVisible(true);
        //serverIpText.setVisible(true);
        //serverPortText.setVisible(true);
        //handicapText.setVisible(true);
        //connectButton.setVisible(true);
        bigPicture.setVisible(true);
        smallPicture.setVisible(true);

        //hide everything
        chatScrollPane.setVisible(false);
        userIsTyping.setVisible(false);
        chatText.setVisible(false);
        sendMessageButton.setVisible(false);
        puzzleLabel.setVisible(false);
        puzzleCombo.setVisible(false);
        countdownLabel.setVisible(false);
        countdownCombo.setVisible(false);
        startButton.setVisible(false);
        popButton.setVisible(false);

        useThisAlgLabel.setVisible(false);
        scrambleText.setVisible(false);
        timerLabel.setVisible(false);
        localTimeLabel.setVisible(false);
        remoteTimeLabel.setVisible(false);
        localTimeUsernameLabel.setVisible(false);
        remoteTimeUsernameLabel.setVisible(false);
        localAverageDetailButton.setVisible(false);
        localSessionDetailButton.setVisible(false);
        remoteSessionDetailButton.setVisible(false);
        remoteAverageDetailButton.setVisible(false);

    } // end hideGUI

//**********************************************************************************************************************

    protected void showGUI(){
        //usernameLabel.setVisible(false);
        //serverIpLabel.setVisible(false);
        //serverPortLabel.setVisible(false);
        //handicapLabel.setVisible(false);
        //usernameText.setVisible(false);
        //serverIpText.setVisible(false);
        //serverPortText.setVisible(false);
        //handicapText.setVisible(false);
        //connectButton.setVisible(false);
        bigPicture.setVisible(false);
        smallPicture.setVisible(false);

        //show everything
        chatScrollPane.setVisible(true);
        userIsTyping.setVisible(true);
        chatText.setVisible(true);
        sendMessageButton.setVisible(true);
        puzzleLabel.setVisible(true);
        puzzleCombo.setVisible(true);
        countdownLabel.setVisible(true);
        countdownCombo.setVisible(true);
        startButton.setVisible(true);
        popButton.setVisible(true);

        useThisAlgLabel.setVisible(true);
        scrambleText.setVisible(true);
        timerLabel.setVisible(true);
        localTimeLabel.setVisible(true);
        remoteTimeLabel.setVisible(true);
        localTimeUsernameLabel.setVisible(true);
        remoteTimeUsernameLabel.setVisible(true);
        localAverageDetailButton.setVisible(true);
        localSessionDetailButton.setVisible(true);
        remoteSessionDetailButton.setVisible(true);
        remoteAverageDetailButton.setVisible(true);

    } // end showGUI

//**********************************************************************************************************************

    protected final class RunCountdown extends java.util.TimerTask{
        int readyTime = 2;
        public void run(){
            if(readyTime == 2){
                bing2.play();
                timerLabel.setFont(new Font("Serif", Font.PLAIN, 64));
                timerLabel.setText("Get Ready!...2");
                readyTime--;
            } else if(readyTime == 1){
                timerLabel.setText("Get Ready!...1");
                readyTime--;
            } else {
                timerLabel.setFont(new Font("Serif", Font.PLAIN, 94));
                if(readyTime == 0){
                    bing1.play();
                    readyTime--;
                }
                if(countdown == 0){
                    timerLabel.setForeground(Color.blue);
                    startButton.setEnabled(true);
                    if(acceptsSincePop >= 12){
                        popButton.setText("POP");
                        popButton.setEnabled(true);
                    }
                    timerThread = new java.util.Timer();
                    startButton.requestFocus();

                    startTime = System.currentTimeMillis();
                    timerThread.schedule(new RunTimer(), 0, 120);
                    this.cancel();
                } else if(countdown == 3){
                    try{
                        countdownClip.play();
                    } catch(NullPointerException e){}
                    timerLabel.setText(countdown+"");
                    countdown--;
                } else {
                    timerLabel.setText(countdown+"");
                    countdown--;
                }
            }
        }
    } // end RunCountdown class

//**********************************************************************************************************************

    protected final class RunTimer extends java.util.TimerTask{
        public void run(){
            double time = (System.currentTimeMillis() - startTime)/1000;
            timerLabel.setText(timeFormat.format(time));
        }
    } // end RunTimer class

//**********************************************************************************************************************

    protected final void updateStats(){
        remoteTimeLabel.setText(remoteTime);

        //**********show who won and adjust scores!**********
        double localTime, remoteTime;
        try{
            localTime = Double.parseDouble(localTimeLabel.getText());
        } catch(NumberFormatException e){localTime = 0;}
        try{
            remoteTime = Double.parseDouble(remoteTimeLabel.getText());
        } catch(NumberFormatException f){remoteTime = 0;}

        //increment remote pops if they popped
        if(remoteTime == 0)
            remoteNumOfPops++;
        else
            remoteCubesSolved++;

        //if local person won
        if(((localTime < remoteTime) || (remoteTime == 0)) && localTime != 0){
            localTimeLabel.setForeground(new Color(0,180,0)); // was 0,110,0
            remoteTimeLabel.setForeground(Color.red);
            localScore++;
        }
        //if remote person won
        else if(((remoteTime < localTime) || localTime == 0) && remoteTime != 0){
            localTimeLabel.setForeground(Color.red);
            remoteTimeLabel.setForeground(new Color(0,180,0)); // was 0,110,0
            remoteScore++;
        }
        // tie!
        else {
            localTimeLabel.setForeground(Color.blue);
            remoteTimeLabel.setForeground(Color.blue);
        }

        //**********add times and scrambles to rolling average and session times**********
        sessionScrambles[sessionIndex] = scrambleText.getText();
        localSessionTimes[sessionIndex] = localTimeLabel.getText();
        remoteSessionTimes[sessionIndex] = remoteTimeLabel.getText();

        //if the time is not a pop, add the time and scramble to the rolling average and move the place up one
        if(localTime != 0){
            localCurrentAverage[localCurrentPlaceInAverage] = localTimeLabel.getText();
            localCurrentScrambles[localCurrentPlaceInAverage] = scrambleText.getText();
            localCurrentPlaceInAverage++;
            if(localCurrentPlaceInAverage == 12)
                localCurrentPlaceInAverage = 0;
        }
        if(remoteTime != 0){
            remoteCurrentAverage[remoteCurrentPlaceInAverage] = remoteTimeLabel.getText();
            remoteCurrentScrambles[remoteCurrentPlaceInAverage] = scrambleText.getText();
            remoteCurrentPlaceInAverage++;
            if(remoteCurrentPlaceInAverage == 12)
                remoteCurrentPlaceInAverage = 0;
        }

        //**********update averages and find fastest and slowest times**********
        // LOCAL if this is the first time, or if this time is faster or slower than fastest and slowest, then store it
        if(localSessionSlowest == 0 && localTime != 0){
            localSessionSlowest = localTime;
            localSessionFastest = localTime;
        } else if(localTime > localSessionSlowest && localTime != 0){
            localSessionSlowest = localTime;
        } else if(localTime < localSessionFastest && localTime != 0){
            localSessionFastest = localTime;
        }

        // REMOTE if this is the first time, or if this time is faster or slower than fastest and slowest, then store it
        if(remoteSessionSlowest == 0 && remoteTime != 0){
            remoteSessionSlowest = remoteTime;
            remoteSessionFastest = remoteTime;
        } else if(remoteTime > remoteSessionSlowest && remoteTime != 0){
            remoteSessionSlowest = remoteTime;
        } else if(remoteTime < remoteSessionFastest && remoteTime != 0){
            remoteSessionFastest = remoteTime;
        }

        //update session average
        if(localTime != 0){
            localTotalTime = localTotalTime + localTime;
            localCurrentSessionAverage = localTotalTime/localCubesSolved;
        }
        if(remoteTime != 0){
            remoteTotalTime = remoteTotalTime + remoteTime;
            remoteCurrentSessionAverage = remoteTotalTime/remoteCubesSolved;
        }

        //update LOCAL rolling average if at least 12 cubes have been solved
        if(localCubesSolved >= 12){
            //find the fastest and slowest time in the current average
            int fastest = 0;
            int slowest = 0;

            for(int i=0; i<12; i++){
                double fastestTime = Double.parseDouble(localCurrentAverage[fastest]);
                double slowestTime = Double.parseDouble(localCurrentAverage[slowest]);
                double currentTime = Double.parseDouble(localCurrentAverage[i]);

                if(currentTime > slowestTime)
                    slowest = i;
                if(currentTime < fastestTime)
                    fastest = i;
            }

            localCurrentSlowest = Double.parseDouble(localCurrentAverage[slowest]);
            localCurrentFastest = Double.parseDouble(localCurrentAverage[fastest]);

            //calculate average of the middle ten times
            double sum = 0;
            for(int i=0; i<12; i++)
                if(i!=fastest && i!=slowest)
                    sum += Double.parseDouble(localCurrentAverage[i]);
            localCurrentRollingAverage = sum/10.0;
        }

        //update REMOTE rolling average if at least 12 cubes have been solved
        if(remoteCubesSolved >= 12){
            //find the fastest and slowest time in the current average
            int fastest = 0;
            int slowest = 0;

            for(int i=0; i<12; i++){
                double fastestTime = Double.parseDouble(remoteCurrentAverage[fastest]);
                double slowestTime = Double.parseDouble(remoteCurrentAverage[slowest]);
                double currentTime = Double.parseDouble(remoteCurrentAverage[i]);

                if(currentTime > slowestTime)
                    slowest = i;
                if(currentTime < fastestTime)
                    fastest = i;
            }

            remoteCurrentSlowest = Double.parseDouble(remoteCurrentAverage[slowest]);
            remoteCurrentFastest = Double.parseDouble(remoteCurrentAverage[fastest]);

            //calculate average of the middle ten times
            double sum = 0;
            for(int i=0; i<12; i++)
                if(i!=fastest && i!=slowest)
                    sum += Double.parseDouble(remoteCurrentAverage[i]);
            remoteCurrentRollingAverage = sum/10.0;
        }

        //**********increment session index**********
        sessionIndex++;

        //**********update statistics display to reflect new statistics**********
        String localRollingAverage, remoteRollingAverage, localSessionAverage, remoteSessionAverage, localSessionFastestTime, remoteSessionFastestTime, localSessionSlowestTime, remoteSessionSlowestTime;

        if(localCubesSolved >= 12)
            localRollingAverage = timeFormat.format(localCurrentRollingAverage);
        else
            localRollingAverage = "N/A";

        if(remoteCubesSolved >= 12)
            remoteRollingAverage = timeFormat.format(remoteCurrentRollingAverage);
        else
            remoteRollingAverage = "N/A";

        if(localCubesSolved >= 1){
            localSessionAverage = timeFormat.format(localCurrentSessionAverage);
            localSessionFastestTime = timeFormat.format(localSessionFastest);
            localSessionSlowestTime = timeFormat.format(localSessionSlowest);
        } else {
            localSessionAverage = "N/A";
            localSessionFastestTime = "N/A";
            localSessionSlowestTime = "N/A";
        }

        if(remoteCubesSolved >= 1){
            remoteSessionAverage = timeFormat.format(remoteCurrentSessionAverage);
            remoteSessionFastestTime = timeFormat.format(remoteSessionFastest);
            remoteSessionSlowestTime = timeFormat.format(remoteSessionSlowest);
        } else {
            remoteSessionAverage = "N/A";
            remoteSessionFastestTime = "N/A";
            remoteSessionSlowestTime = "N/A";
        }

        localTimeUsernameLabel.setText("<html>Rolling Average: <FONT SIZE=\"5\">"+localRollingAverage+"</FONT><br>Session Average: "+localSessionAverage+"<br><br>Score: "+localScore+"<br>Session Fastest Time: "+localSessionFastestTime+"<br>Session Slowest Time: "+localSessionSlowestTime+"</html>");
        remoteTimeUsernameLabel.setText("<html>Rolling Average: <FONT SIZE=\"5\">"+remoteRollingAverage+"</FONT><br>Session Average: "+remoteSessionAverage+"<br><br>Score: "+remoteScore+"<br>Session Fastest Time: "+remoteSessionFastestTime+"<br>Session Slowest Time: "+remoteSessionSlowestTime+"</html>");
    } // end updateStats

//**********************************************************************************************************************

    protected final void reset(){
        isTyping = false;
        remoteIsTyping = false;

        localSessionTimes = new String[100];
        remoteSessionTimes = new String[100];
        sessionScrambles = new String[100];

        localCurrentAverage = new String[12];
        remoteCurrentAverage = new String[12];
        localCurrentScrambles = new String[12];
        remoteCurrentScrambles = new String[12];

        localCubesSolved = 0;
        remoteCubesSolved = 0;
        localSessionFastest = 0;
        remoteSessionFastest = 0;
        localSessionSlowest = 0;
        remoteSessionSlowest = 0;
        localCurrentFastest = 0;
        remoteCurrentFastest = 0;
        localCurrentSlowest = 0;
        remoteCurrentSlowest = 0;
        localCurrentPlaceInAverage = 0;
        remoteCurrentPlaceInAverage = 0;
        localNumOfPops = 0;
        remoteNumOfPops = 0;
        localScore = 0;
        remoteScore = 0;
        sessionIndex = 0;
        localCurrentRollingAverage = 0;
        remoteCurrentRollingAverage = 0;
        localCurrentSessionAverage = 0;
        remoteCurrentSessionAverage = 0;
        localTotalTime = 0;
        remoteTotalTime = 0;
        acceptsSincePop = 13;

        timerLabel.setText("");
        localTimeUsernameLabel.setText("<html>Rolling Average: <FONT SIZE=\"5\">N/A</FONT><br>Session Average: N/A<br><br>Score: 0<br>Session Fastest Time: N/A<br>Session Slowest Time: N/A</html>");
        remoteTimeUsernameLabel.setText("<html>Rolling Average: <FONT SIZE=\"5\">N/A</FONT><br>Session Average: N/A<br><br>Score: 0<br>Session Fastest Time: N/A<br>Session Slowest Time: N/A</html>");
        localTimeLabel.setText("");
        remoteTimeLabel.setText("");
        chatText.setText("");
    } // end reset

//**********************************************************************************************************************

    protected final String getLocalSessionView(){
        String sessionViewFormat = "----- Rubik's JTimer Session Statistics for %T -----\r\n\r\nUsername: %U\r\n\r\nCubes Solved: %C\r\nTotal Pops: %P\r\nAverage: %A\r\n\r\nFastest Time: %F\r\nSlowest Time: %S\r\n\r\nIndividual Times:\r\n%I";
        String timesAndScrambles = "", timesOnly = "";

        for(int i=0; i<sessionIndex; i++){
            timesAndScrambles = timesAndScrambles + (i+1) + ")          " + localSessionTimes[i] + "          " + sessionScrambles[i] + "\r\n";
            timesOnly = timesOnly + localSessionTimes[i] + "\r\n";
        }

        String returnMe = sessionViewFormat;
        returnMe = findAndReplace(returnMe,"%T",new Date()+"");
        returnMe = findAndReplace(returnMe,"%U",usernameText.getText());
        returnMe = findAndReplace(returnMe,"%A",timeFormat.format(localCurrentSessionAverage));
        returnMe = findAndReplace(returnMe,"%I",timesAndScrambles);
        returnMe = findAndReplace(returnMe,"%O",timesOnly);
        returnMe = findAndReplace(returnMe,"%F",timeFormat.format(localSessionFastest));
        returnMe = findAndReplace(returnMe,"%S",timeFormat.format(localSessionSlowest));
        returnMe = findAndReplace(returnMe,"%C",localCubesSolved+"");
        returnMe = findAndReplace(returnMe,"%P",localNumOfPops+"");
        return returnMe;
    } // end getLocalSessionView

//**********************************************************************************************************************

    protected final String getLocalAverageView(){
        String averageViewFormat = "----- Rubik's JTimer Best Average for %T -----\r\n\r\nUsername: %U\r\n\r\nAverage: %A\r\n\r\nFastest Time: %F\r\nSlowest Time: %S\r\n\r\nIndividual Times:\r\n%I";
        String timesAndScrambles = "", timesOnly = "";

        for(int i=0; i<12; i++){
            String currentTime = localCurrentAverage[i];
            if(Double.parseDouble(currentTime) == localCurrentFastest || Double.parseDouble(currentTime) == localCurrentSlowest)
                currentTime = "(" + currentTime + ")";
            timesAndScrambles = timesAndScrambles + (i+1) + ")          " + currentTime + "          " + localCurrentScrambles[i] + "\r\n";
            timesOnly = timesOnly + currentTime + "\r\n";
        }

        String returnMe = averageViewFormat;
        returnMe = findAndReplace(returnMe,"%T",new Date()+"");
        returnMe = findAndReplace(returnMe,"%U",usernameText.getText());
        returnMe = findAndReplace(returnMe,"%A",timeFormat.format(localCurrentRollingAverage));
        returnMe = findAndReplace(returnMe,"%I",timesAndScrambles);
        returnMe = findAndReplace(returnMe,"%O",timesOnly);
        returnMe = findAndReplace(returnMe,"%F",timeFormat.format(localCurrentFastest));
        returnMe = findAndReplace(returnMe,"%S",timeFormat.format(localCurrentSlowest));
        return returnMe;
    } // end getLocalAverageView

//**********************************************************************************************************************

    protected final String getRemoteSessionView(){
        String sessionViewFormat = "----- Rubik's JTimer Session Statistics for %T -----\r\n\r\nUsername: %U\r\n\r\nCubes Solved: %C\r\nTotal Pops: %P\r\nAverage: %A\r\n\r\nFastest Time: %F\r\nSlowest Time: %S\r\n\r\nIndividual Times:\r\n%I";
        String timesAndScrambles = "", timesOnly = "";

        for(int i=0; i<sessionIndex; i++){
            timesAndScrambles = timesAndScrambles + (i+1) + ")          " + remoteSessionTimes[i] + "          " + sessionScrambles[i] + "\r\n";
            timesOnly = timesOnly + remoteSessionTimes[i] + "\r\n";
        }

        String returnMe = sessionViewFormat;
        returnMe = findAndReplace(returnMe,"%T",new Date()+"");
        returnMe = findAndReplace(returnMe,"%U",remoteUsername);
        returnMe = findAndReplace(returnMe,"%A",timeFormat.format(remoteCurrentSessionAverage));
        returnMe = findAndReplace(returnMe,"%I",timesAndScrambles);
        returnMe = findAndReplace(returnMe,"%O",timesOnly);
        returnMe = findAndReplace(returnMe,"%F",timeFormat.format(remoteSessionFastest));
        returnMe = findAndReplace(returnMe,"%S",timeFormat.format(remoteSessionSlowest));
        returnMe = findAndReplace(returnMe,"%C",remoteCubesSolved+"");
        returnMe = findAndReplace(returnMe,"%P",remoteNumOfPops+"");
        return returnMe;
    } // end getRemoteSessionView

//**********************************************************************************************************************

    protected final String getRemoteAverageView(){
        String averageViewFormat = "----- Rubik's JTimer Best Average for %T -----\r\n\r\nUsername: %U\r\n\r\nAverage: %A\r\n\r\nFastest Time: %F\r\nSlowest Time: %S\r\n\r\nIndividual Times:\r\n%I";
        String timesAndScrambles = "", timesOnly = "";

        for(int i=0; i<12; i++){
            String currentTime = remoteCurrentAverage[i];
            if(Double.parseDouble(currentTime) == remoteCurrentFastest || Double.parseDouble(currentTime) == remoteCurrentSlowest)
                currentTime = "(" + currentTime + ")";
            timesAndScrambles = timesAndScrambles + (i+1) + ")          " + currentTime + "          " + remoteCurrentScrambles[i] + "\r\n";
            timesOnly = timesOnly + currentTime + "\r\n";
        }

        String returnMe = averageViewFormat;
        returnMe = findAndReplace(returnMe,"%T",new Date()+"");
        returnMe = findAndReplace(returnMe,"%U",remoteUsername);
        returnMe = findAndReplace(returnMe,"%A",timeFormat.format(remoteCurrentRollingAverage));
        returnMe = findAndReplace(returnMe,"%I",timesAndScrambles);
        returnMe = findAndReplace(returnMe,"%O",timesOnly);
        returnMe = findAndReplace(returnMe,"%F",timeFormat.format(remoteCurrentFastest));
        returnMe = findAndReplace(returnMe,"%S",timeFormat.format(remoteCurrentSlowest));
        return returnMe;
    } // end getRemoteAverageView

//**********************************************************************************************************************

    protected final String findAndReplace(String original, String find, String replace){
        while(true){
            int index = original.indexOf(find);
            if(index >= 0)
                original = original.substring(0,index) + replace + original.substring((index+find.length()),(original.length()));
            else
                break;
        }
        return original;
    } // end findAndReplace

//**********************************************************************************************************************

    public final void keyTyped(KeyEvent e){}
    public final void keyPressed(KeyEvent e){}

//**********************************************************************************************************************

    public final void keyReleased(KeyEvent e){
        Object source = e.getSource();

        if(source == chatText){
            if(chatText.getText().equals("")){
                if(isTyping){
                    isTyping = false;
                    out.println("I");
                    out.flush();
                }
            } else {
                if(!isTyping){
                    isTyping = true;
                    out.println("I");
                    out.flush();
                }
            }
        }
    } // end keyReleased

}