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
    protected OptionsBox optionsBox;
    //protected ScrambleGenerator scrambleGenerator;
    //protected InstructionScreen instructionScreen;
    //protected AboutScreen aboutScreen;

    protected ScramblePanel scramblePanel;
    protected String newAlg;

    protected static final String sessionViewFormat = "----- " + APP_TITLE + " Session Statistics for %T -----\n\nUsername: %U\n\nTotal Solves: %C\nTotal Pops: %P\nAverage: %A\n\nFastest Time: %F\nSlowest Time: %S\n\nIndividual Times:\n%I";
    protected static final String averageViewFormat = "----- " + APP_TITLE + " Best Average for %T -----\n\nUsername: %U\n\nAverage: %A\n\nFastest Time: %F\nSlowest Time: %S\n\nIndividual Times:\n%I";

    JLabel usernameLabel, serverIpLabel, serverPortLabel, bigPicture, smallPicture;
    JLabel puzzleLabel, countdownLabel, useThisAlgLabel, timerLabel, userIsTyping;
    JLabel localTimeLabel, remoteTimeLabel, localTimeUsernameLabel, remoteTimeUsernameLabel;
    JTextField usernameText, serverIpText, serverPortText, chatText;
    JButton connectButton, sendMessageButton, localSessionDetailButton, localAverageDetailButton, remoteSessionDetailButton, remoteAverageDetailButton, startButton, popButton;
    JTextPane chatPane;
    StyledDocument chatDoc;
    Style redStyle, blueStyle, blackStyle;
    AudioClip chatSound, countdownClip, bing1, bing2, startupClip;
    JTextArea scrambleText;
    String remoteUsername, remoteTime;
    JComboBox puzzleCombo, countdownCombo;
    JScrollPane chatScrollPane;
    java.util.Timer timerThread;
    int countdown;
    long startTime, stopTime;
    DecimalFormat ssxx, ss;
    boolean isTyping, remoteIsTyping;
    ImageIcon typeOn, typeOff;

    // mixed stuff
    JLabel remoteStatusLabel; // not used in Client
    JTextArea readyColor; // not used in Client
    JCheckBox localStatusLabel; // not used in Server

    // data storage
    String[] localSessionTimes, remoteSessionTimes, sessionScrambles, localCurrentAverage, remoteCurrentAverage, localCurrentScrambles, remoteCurrentScrambles;
    int localCubesSolved, remoteCubesSolved, sessionIndex, localCurrentPlaceInAverage, remoteCurrentPlaceInAverage, localNumOfPops, remoteNumOfPops, localScore, remoteScore, acceptsSincePop;
    float localTotalTime, remoteTotalTime, localSessionFastest, remoteSessionFastest, localSessionSlowest, remoteSessionSlowest, localCurrentFastest, remoteCurrentFastest, localCurrentSlowest, remoteCurrentSlowest, localCurrentRollingAverage, remoteCurrentRollingAverage, localCurrentSessionAverage, remoteCurrentSessionAverage;

    // JMenu stuff
    JMenuBar jMenuBar;
    JMenu fileMenu;//, toolsMenu, helpMenu;
    JMenuItem /*saveBestItem, saveSessionItem, optionsItem,*/ exitItem/*, importItem, generatorItem, instItem, aboutItem*/;

    // network stuff
    Socket clientSocket;
    BufferedReader in;
    PrintWriter out;

    // listening thread
    Thread chatListener;

//**********************************************************************************************************************

    public NetcubeMode(OptionsBox optionsBox){
        // configure JFrame
        RJT_Utils.configureJFrame(this);

        try { //configure chatSound
            chatSound = Applet.newAudioClip(getClass().getResource("blip.wav"));
        } catch(NullPointerException ex){JOptionPane.showMessageDialog(this, "blip.wav not found. There will be no audio when a message is recieved.");}

        try { //configure countdownClip
            countdownClip = Applet.newAudioClip(getClass().getResource("count.mid"));
        } catch(NullPointerException ex){JOptionPane.showMessageDialog(this, "count.mid not found. There will be no audio during countdown.");}

        try { //configure bing1
            bing1 = Applet.newAudioClip(getClass().getResource("bing1.wav"));
        } catch(NullPointerException ex){JOptionPane.showMessageDialog(this, "bing1.wav not found. There will be no 'ready' sound.");}

        try { //configure bing2
            bing2 = Applet.newAudioClip(getClass().getResource("bing2.wav"));
        } catch(NullPointerException ex){JOptionPane.showMessageDialog(this, "bing2.wav not found. There will be no 'ready' sound.");}

        try { //configure startupClip
            startupClip = Applet.newAudioClip(getClass().getResource("startup.wav"));
        } catch(NullPointerException ex){JOptionPane.showMessageDialog(this, "startup.wav not found. There will be no startup sound.");}

        ssxx = (DecimalFormat)NumberFormat.getNumberInstance(new Locale("en", "US")); ssxx.applyPattern("00.00");
        ss = (DecimalFormat)NumberFormat.getNumberInstance(new Locale("en", "US")); ss.applyPattern("00");

        this.optionsBox = optionsBox;
        newAlg = ""; // just in case...

        // set up JMenuBar
        makeJMenuBar();
        //setJMenuBar(jMenuBar);

        // GUI Object creation
        usernameLabel = new JLabel("Username:");
        serverIpLabel = new JLabel("Server IP:");
        serverPortLabel = new JLabel("Server Port:");
        usernameText = new JTextField();
        serverIpText = new JTextField(); // override in sub-class
        serverPortText = new JTextField("52003");
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
        scrambleText = new JTextArea("");
        scrambleText.setFocusable(true);
        scrambleText.setEditable(false);
        scrambleText.setLineWrap(true);
        scrambleText.setWrapStyleWord(true);
        scrambleText.setBackground(optionsBox.textBackgrColorX);
        scrambleText.setForeground(Color.black);
        scrambleText.setBorder(blackLine);

        timerLabel = new JLabel("");
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setFont(new Font("Serif", Font.PLAIN, 94));

        scramblePanel = new ScramblePanel(310+40, 215+20); // needs to be changed in two places
        scramblePanel.setLayout(null);
        scramblePanel.setBorder(BorderFactory.createTitledBorder(theBorder, "Scramble View"));
        scramblePanel.setCubeColors(optionsBox.cubeColorsX);
        scramblePanel.setPyraminxColors(optionsBox.pyraminxColorsX);
        scramblePanel.setMegaminxColors(optionsBox.megaminxColorsX);
        //updateScramblePanel(); // not here, comboBox might not be stable yet

        localTimeUsernameLabel = new JLabel("<html>Rolling Average: <font size=\"5\">N/A</font><br>Session Average: N/A<br><br>Score: 0<br>Session Fastest Time: N/A<br>Session Slowest Time: N/A</html>");
        remoteTimeUsernameLabel = new JLabel("<html>Rolling Average: <font size=\"5\">N/A</font><br>Session Average: N/A<br><br>Score: 0<br>Session Fastest Time: N/A<br>Session Slowest Time: N/A</html>");
        localTimeLabel = new JLabel(""); // override in sub-class
        remoteTimeLabel = new JLabel(""); // override in sub-class
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

        // mixed stuff
        readyColor = new JTextArea();
        readyColor.setEditable(false);
        readyColor.setBackground(Color.red);
        readyColor.setBorder(blackLine);
        remoteStatusLabel = new JLabel("Remote Status");
        localStatusLabel = new JCheckBox("I'm ready!");

        bigPicture = new JLabel((new ImageIcon(getClass().getResource("bigPicture.jpg"))));
        smallPicture = new JLabel((new ImageIcon(getClass().getResource("smallPicture.jpg"))));
        bigPicture.setBorder(blackLine);
        smallPicture.setBorder(blackLine);

        // set everything to defaults
        reset();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

//**********************************************************************************************************************

    private void makeJMenuBar(){
//        saveBestItem = new JMenuItem("Save Best Average As...");
//        saveSessionItem = new JMenuItem("Save Session Average As...");
//        optionsItem = new JMenuItem("Options");
//        optionsItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        exitItem = new JMenuItem("Exit");
//        importItem = new JMenuItem("Import Scrambles"); importItem.setMnemonic('I');
//        importItem.setAccelerator(KeyStroke.getKeyStroke('I', 2));
//        generatorItem = new JMenuItem("Generate Scrambles"); generatorItem.setMnemonic('G');
//        generatorItem.setAccelerator(KeyStroke.getKeyStroke('G', 2));
//        instItem = new JMenuItem("Instuctions");
//        instItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
//        aboutItem = new JMenuItem("About " + APP_TITLE); aboutItem.setMnemonic('A');
//        aboutItem.setAccelerator(KeyStroke.getKeyStroke('A', 2));
        fileMenu = new JMenu("File"); fileMenu.setMnemonic('F');
//        fileMenu.add(saveBestItem);
//        fileMenu.add(saveSessionItem);
//        fileMenu.addSeparator();
//        fileMenu.add(optionsItem);
//        fileMenu.addSeparator();
        fileMenu.add(exitItem);
//        toolsMenu = new JMenu("Tools"); toolsMenu.setMnemonic('T');
//        toolsMenu.add(importItem);
//        toolsMenu.add(generatorItem);
//        networkMenu = new JMenu("Network Timer"); networkMenu.setMnemonic('N');
//        helpMenu = new JMenu("Help"); helpMenu.setMnemonic('H');
//        helpMenu.add(instItem);
//        helpMenu.add(aboutItem);
        jMenuBar = new JMenuBar();
        jMenuBar.add(fileMenu);
//        jMenuBar.add(toolsMenu);
//        jMenuBar.add(networkMenu);
//        jMenuBar.add(Box.createHorizontalGlue());
//        jMenuBar.add(helpMenu);
    }

//**********************************************************************************************************************

    protected void setTheBounds(){

        usernameLabel.setBounds(10,10,80,20);
        serverIpLabel.setBounds(10,35,80,20);
        serverPortLabel.setBounds(10,60,80,20);
        usernameText.setBounds(90,10,80,20);
        serverIpText.setBounds(90,35,80,20);
        serverPortText.setBounds(90,60,80,20);
        connectButton.setBounds(10,85,160,45);

        puzzleLabel.setBounds(10,5,90,20);
        puzzleCombo.setBounds(10,25,90,20);
        countdownLabel.setBounds(110,5,90,20);
        countdownCombo.setBounds(110,25,90,20);
        startButton.setBounds(10,50,190,30);
        popButton.setBounds(10,85,190,30);
        readyColor.setBounds(10,120,20,20); // not used in Client
        remoteStatusLabel.setBounds(40,120,160,20); // not used in Client
        localStatusLabel.setBounds(10,120,190,20); // not used in Server

        useThisAlgLabel.setBounds(215,5,350,20);
        scrambleText.setBounds(215,25,350,115);
        timerLabel.setBounds(215,157,350,75);

        scramblePanel.setBounds(575,5,350,235); // needs to be changed in two places

        chatScrollPane.setBounds(10,150,190,245);
        userIsTyping.setBounds(10,400,20,20);
        chatText.setBounds(35,400,85,20);
        sendMessageButton.setBounds(130,400,70,20);

        localTimeUsernameLabel.setBounds(215,240,350,185);
        remoteTimeUsernameLabel.setBounds(575,240,350,185);
        localTimeLabel.setBounds(215,250,350,60);
        remoteTimeLabel.setBounds(575,250,350,60);
        localAverageDetailButton.setBounds(425,330,120,20);
        localSessionDetailButton.setBounds(425,355,120,20);
        remoteAverageDetailButton.setBounds(785,330,120,20);
        remoteSessionDetailButton.setBounds(785,355,120,20);

        smallPicture.setBounds(180,10,500,120);
        bigPicture.setBounds(10,150,700,355);
    }

//**********************************************************************************************************************

    protected void addTheContent(Container contentPane){

        contentPane.add(usernameLabel);
        contentPane.add(serverIpLabel);
        contentPane.add(serverPortLabel);
        contentPane.add(usernameText);
        contentPane.add(serverIpText);
        contentPane.add(serverPortText);
        contentPane.add(connectButton);

        contentPane.add(chatScrollPane);
        contentPane.add(userIsTyping);
        contentPane.add(chatText);
        contentPane.add(sendMessageButton);

        contentPane.add(useThisAlgLabel);
        contentPane.add(scrambleText);
        contentPane.add(timerLabel);

        contentPane.add(scramblePanel);

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

        contentPane.add(smallPicture);
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

        exitItem.addActionListener(this);
    }

//**********************************************************************************************************************

    protected void prepGUI(){
        startButton.setEnabled(false);
        popButton.setEnabled(false);
        chatText.setEnabled(false);
        sendMessageButton.setEnabled(false);
        localAverageDetailButton.setEnabled(false);
        localSessionDetailButton.setEnabled(false);
        remoteAverageDetailButton.setEnabled(false);
        remoteSessionDetailButton.setEnabled(false);

        RJT_Utils.enterPressesWhenFocused(startButton);
        RJT_Utils.enterPressesWhenFocused(popButton);
        //RJT_Utils.enterPressesWhenFocused(localStatusLabel);
        RJT_Utils.enterPressesWhenFocused(sendMessageButton);
        RJT_Utils.enterPressesWhenFocused(localAverageDetailButton);
        RJT_Utils.enterPressesWhenFocused(localSessionDetailButton);
        RJT_Utils.enterPressesWhenFocused(remoteAverageDetailButton);
        RJT_Utils.enterPressesWhenFocused(remoteSessionDetailButton);
    }

//**********************************************************************************************************************

    protected void hideGUI(){
        RJT_Utils.centerJFrame(this, 695, 170);
        setJMenuBar(null);

        usernameLabel.setVisible(true);
        serverIpLabel.setVisible(true);
        serverPortLabel.setVisible(true);
        usernameText.setVisible(true);
        serverIpText.setVisible(true);
        serverPortText.setVisible(true);
        connectButton.setVisible(true);
        bigPicture.setVisible(true);
        smallPicture.setVisible(true);

        //hide everything
        puzzleLabel.setVisible(false);
        puzzleCombo.setVisible(false);
        countdownLabel.setVisible(false);
        countdownCombo.setVisible(false);
        startButton.setVisible(false);
        popButton.setVisible(false);

        chatScrollPane.setVisible(false);
        userIsTyping.setVisible(false);
        chatText.setVisible(false);
        sendMessageButton.setVisible(false);

        useThisAlgLabel.setVisible(false);
        scrambleText.setVisible(false);
        timerLabel.setVisible(false);
        scramblePanel.setVisible(false);

        localTimeUsernameLabel.setVisible(false);
        remoteTimeUsernameLabel.setVisible(false);
        localTimeLabel.setVisible(false);
        remoteTimeLabel.setVisible(false);
        localAverageDetailButton.setVisible(false);
        localSessionDetailButton.setVisible(false);
        remoteSessionDetailButton.setVisible(false);
        remoteAverageDetailButton.setVisible(false);

    } // end hideGUI

//**********************************************************************************************************************

    protected void showGUI(){
        RJT_Utils.centerJFrame(this, 860+80, 465+20);
        setJMenuBar(jMenuBar);

        usernameLabel.setVisible(false);
        serverIpLabel.setVisible(false);
        serverPortLabel.setVisible(false);
        usernameText.setVisible(false);
        serverIpText.setVisible(false);
        serverPortText.setVisible(false);
        connectButton.setVisible(false);
        bigPicture.setVisible(false);
        smallPicture.setVisible(false);

        //show everything
        puzzleLabel.setVisible(true);
        puzzleCombo.setVisible(true);
        countdownLabel.setVisible(true);
        countdownCombo.setVisible(true);
        startButton.setVisible(true);
        popButton.setVisible(true);

        chatScrollPane.setVisible(true);
        userIsTyping.setVisible(true);
        chatText.setVisible(true);
        sendMessageButton.setVisible(true);

        useThisAlgLabel.setVisible(true);
        scrambleText.setVisible(true);
        timerLabel.setVisible(true);
        scramblePanel.setVisible(true);

        localTimeUsernameLabel.setVisible(true);
        remoteTimeUsernameLabel.setVisible(true);
        localTimeLabel.setVisible(true);
        remoteTimeLabel.setVisible(true);
        localAverageDetailButton.setVisible(true);
        localSessionDetailButton.setVisible(true);
        remoteSessionDetailButton.setVisible(true);
        remoteAverageDetailButton.setVisible(true);

    } // end showGUI

//**********************************************************************************************************************

    protected boolean commonAction(Object source){
        if(source == sendMessageButton || source == chatText){
            if(chatText.getText().equals("")) return true;
            try{
                chatDoc.insertString(chatDoc.getLength(), (usernameText.getText()+": "), redStyle);
                chatDoc.insertString(chatDoc.getLength(), (chatText.getText()+"\n"), blackStyle);
                safePrint("C" + chatText.getText());
                chatPane.setCaretPosition(chatDoc.getLength());
                chatText.setText("");
                if(isTyping){
                    isTyping = false;
                    safePrint("I");
                }
            } catch(BadLocationException ex){
                System.out.println(ex.getMessage());
            }
        } else if(source == localSessionDetailButton){
            DetailedView win = new DetailedView("Local Session Times", getLocalSessionView(), optionsBox.textBackgrColorX);
            win.setVisible(true);
        } else if(source == localAverageDetailButton){
            DetailedView win = new DetailedView("Local Rolling Average", getLocalAverageView(), optionsBox.textBackgrColorX);
            win.setVisible(true);
        } else if(source == remoteSessionDetailButton){
            DetailedView win = new DetailedView("Remote Session Times", getRemoteSessionView(), optionsBox.textBackgrColorX);
            win.setVisible(true);
        } else if(source == remoteAverageDetailButton){
            DetailedView win = new DetailedView("Remote Rolling Average", getRemoteAverageView(), optionsBox.textBackgrColorX);
            win.setVisible(true);
        } else if(source == exitItem){
            System.exit(0);
        } else
            return false;

        return true;
    }

//**********************************************************************************************************************

    protected void updateScramblePanel(){
        scramblePanel.newScramble(puzzleCombo.getSelectedItem()+"", newAlg.replaceAll(ALG_BREAK, " "));
    }

//**********************************************************************************************************************

    protected final class RunCountdown extends java.util.TimerTask{
        int readyTime = 2;
        public void run(){
            if(readyTime == 2){
                bing2.play();
                timerLabel.setFont(new Font("Serif", Font.PLAIN, 56)); // was 64
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
                    } catch(NullPointerException ex){}
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
            float time = (System.currentTimeMillis()-startTime)/1000F;
            timerLabel.setText(ssxx.format(time));
        }
    } // end RunTimer class

//**********************************************************************************************************************

    protected final void updateStats(){
        remoteTimeLabel.setText(remoteTime);

        //**********show who won and adjust scores!**********
        float localTime, remoteTime;
        try{
            localTime = Float.parseFloat(localTimeLabel.getText());
        } catch(NumberFormatException ex){localTime = 0;}
        try{
            remoteTime = Float.parseFloat(remoteTimeLabel.getText());
        } catch(NumberFormatException ex){remoteTime = 0;}

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
        sessionScrambles[sessionIndex] = newAlg; //scrambleText.getText();
        localSessionTimes[sessionIndex] = localTimeLabel.getText();
        remoteSessionTimes[sessionIndex] = remoteTimeLabel.getText();

        //if the time is not a pop, add the time and scramble to the rolling average and move the place up one
        if(localTime != 0){
            localCurrentAverage[localCurrentPlaceInAverage] = localTimeLabel.getText();
            localCurrentScrambles[localCurrentPlaceInAverage] = newAlg;
            localCurrentPlaceInAverage++;
            if(localCurrentPlaceInAverage == 12)
                localCurrentPlaceInAverage = 0;
        }
        if(remoteTime != 0){
            remoteCurrentAverage[remoteCurrentPlaceInAverage] = remoteTimeLabel.getText();
            remoteCurrentScrambles[remoteCurrentPlaceInAverage] = newAlg;
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
                float fastestTime = Float.parseFloat(localCurrentAverage[fastest]);
                float slowestTime = Float.parseFloat(localCurrentAverage[slowest]);
                float currentTime = Float.parseFloat(localCurrentAverage[i]);

                if(currentTime > slowestTime)
                    slowest = i;
                if(currentTime < fastestTime)
                    fastest = i;
            }

            localCurrentSlowest = Float.parseFloat(localCurrentAverage[slowest]);
            localCurrentFastest = Float.parseFloat(localCurrentAverage[fastest]);

            //calculate average of the middle ten times
            float sum = 0;
            for(int i=0; i<12; i++)
                if(i!=fastest && i!=slowest)
                    sum += Float.parseFloat(localCurrentAverage[i]);
            localCurrentRollingAverage = sum/10F;
        }

        //update REMOTE rolling average if at least 12 cubes have been solved
        if(remoteCubesSolved >= 12){
            //find the fastest and slowest time in the current average
            int fastest = 0;
            int slowest = 0;

            for(int i=0; i<12; i++){
                float fastestTime = Float.parseFloat(remoteCurrentAverage[fastest]);
                float slowestTime = Float.parseFloat(remoteCurrentAverage[slowest]);
                float currentTime = Float.parseFloat(remoteCurrentAverage[i]);

                if(currentTime > slowestTime)
                    slowest = i;
                if(currentTime < fastestTime)
                    fastest = i;
            }

            remoteCurrentSlowest = Float.parseFloat(remoteCurrentAverage[slowest]);
            remoteCurrentFastest = Float.parseFloat(remoteCurrentAverage[fastest]);

            //calculate average of the middle ten times
            float sum = 0;
            for(int i=0; i<12; i++)
                if(i!=fastest && i!=slowest)
                    sum += Float.parseFloat(remoteCurrentAverage[i]);
            remoteCurrentRollingAverage = sum/10F;
        }

        //**********increment session index**********
        sessionIndex++;

        //**********update statistics display to reflect new statistics**********
        String localRollingAverage, remoteRollingAverage, localSessionAverage, remoteSessionAverage, localSessionFastestTime, remoteSessionFastestTime, localSessionSlowestTime, remoteSessionSlowestTime;

        if(localCubesSolved >= 12)
            localRollingAverage = ssxx.format(localCurrentRollingAverage);
        else
            localRollingAverage = "N/A";

        if(remoteCubesSolved >= 12)
            remoteRollingAverage = ssxx.format(remoteCurrentRollingAverage);
        else
            remoteRollingAverage = "N/A";

        if(localCubesSolved >= 1){
            localSessionAverage = ssxx.format(localCurrentSessionAverage);
            localSessionFastestTime = ssxx.format(localSessionFastest);
            localSessionSlowestTime = ssxx.format(localSessionSlowest);
        } else {
            localSessionAverage = "N/A";
            localSessionFastestTime = "N/A";
            localSessionSlowestTime = "N/A";
        }

        if(remoteCubesSolved >= 1){
            remoteSessionAverage = ssxx.format(remoteCurrentSessionAverage);
            remoteSessionFastestTime = ssxx.format(remoteSessionFastest);
            remoteSessionSlowestTime = ssxx.format(remoteSessionSlowest);
        } else {
            remoteSessionAverage = "N/A";
            remoteSessionFastestTime = "N/A";
            remoteSessionSlowestTime = "N/A";
        }

        localTimeUsernameLabel.setText("<html>Rolling Average: <font size=\"5\">"+localRollingAverage+"</font><br>Session Average: "+localSessionAverage+"<br><br>Score: "+localScore+"<br>Session Fastest Time: "+localSessionFastestTime+"<br>Session Slowest Time: "+localSessionSlowestTime+"</html>");
        remoteTimeUsernameLabel.setText("<html>Rolling Average: <font size=\"5\">"+remoteRollingAverage+"</font><br>Session Average: "+remoteSessionAverage+"<br><br>Score: "+remoteScore+"<br>Session Fastest Time: "+remoteSessionFastestTime+"<br>Session Slowest Time: "+remoteSessionSlowestTime+"</html>");
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
        localTimeUsernameLabel.setText("<html>Rolling Average: <font size=\"5\">N/A</font><br>Session Average: N/A<br><br>Score: 0<br>Session Fastest Time: N/A<br>Session Slowest Time: N/A</html>");
        remoteTimeUsernameLabel.setText("<html>Rolling Average: <font size=\"5\">N/A</font><br>Session Average: N/A<br><br>Score: 0<br>Session Fastest Time: N/A<br>Session Slowest Time: N/A</html>");
        localTimeLabel.setText("");
        remoteTimeLabel.setText("");
        chatText.setText("");
    } // end reset

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    protected final String getLocalSessionView(){
        String timesAndScrambles = "", timesOnly = "";

        for(int i=0; i<sessionIndex; i++){
            timesAndScrambles += (i+1) + ")          " + localSessionTimes[i] + "          " + sessionScrambles[i] + "\n";
            timesOnly += localSessionTimes[i] + "\n";
        }

        String returnMe = sessionViewFormat;
        returnMe = returnMe.replaceAll("%T", new Date()+"");
        returnMe = returnMe.replaceAll("%U", usernameText.getText());
        returnMe = returnMe.replaceAll("%A", ssxx.format(localCurrentSessionAverage));
        returnMe = returnMe.replaceAll("%I", timesAndScrambles);
        returnMe = returnMe.replaceAll("%O", timesOnly);
        returnMe = returnMe.replaceAll("%F", ssxx.format(localSessionFastest));
        returnMe = returnMe.replaceAll("%S", ssxx.format(localSessionSlowest));
        returnMe = returnMe.replaceAll("%C", localCubesSolved+"");
        returnMe = returnMe.replaceAll("%P", localNumOfPops+"");
        returnMe = returnMe.replaceAll("\n", System.getProperty("line.separator"));
        return returnMe;
    } // end getLocalSessionView

//**********************************************************************************************************************

    protected final String getLocalAverageView(){
        String timesAndScrambles = "", timesOnly = "";

        for(int i=0; i<12; i++){
            String currentTime = localCurrentAverage[i];
            if(Float.parseFloat(currentTime) == localCurrentFastest || Float.parseFloat(currentTime) == localCurrentSlowest)
                currentTime = "(" + currentTime + ")";
            timesAndScrambles += (i+1) + ")          " + currentTime + "          " + localCurrentScrambles[i] + "\n";
            timesOnly += currentTime + "\n";
        }

        String returnMe = averageViewFormat;
        returnMe = returnMe.replaceAll("%T", new Date()+"");
        returnMe = returnMe.replaceAll("%U", usernameText.getText());
        returnMe = returnMe.replaceAll("%A", ssxx.format(localCurrentRollingAverage));
        returnMe = returnMe.replaceAll("%I", timesAndScrambles);
        returnMe = returnMe.replaceAll("%O", timesOnly);
        returnMe = returnMe.replaceAll("%F", ssxx.format(localCurrentFastest));
        returnMe = returnMe.replaceAll("%S", ssxx.format(localCurrentSlowest));
        returnMe = returnMe.replaceAll("\n", System.getProperty("line.separator"));
        return returnMe;
    } // end getLocalAverageView

//**********************************************************************************************************************

    protected final String getRemoteSessionView(){
        String timesAndScrambles = "", timesOnly = "";

        for(int i=0; i<sessionIndex; i++){
            timesAndScrambles += (i+1) + ")          " + remoteSessionTimes[i] + "          " + sessionScrambles[i] + "\n";
            timesOnly += remoteSessionTimes[i] + "\n";
        }

        String returnMe = sessionViewFormat;
        returnMe = returnMe.replaceAll("%T", new Date()+"");
        returnMe = returnMe.replaceAll("%U", remoteUsername);
        returnMe = returnMe.replaceAll("%A", ssxx.format(remoteCurrentSessionAverage));
        returnMe = returnMe.replaceAll("%I", timesAndScrambles);
        returnMe = returnMe.replaceAll("%O", timesOnly);
        returnMe = returnMe.replaceAll("%F", ssxx.format(remoteSessionFastest));
        returnMe = returnMe.replaceAll("%S", ssxx.format(remoteSessionSlowest));
        returnMe = returnMe.replaceAll("%C", remoteCubesSolved+"");
        returnMe = returnMe.replaceAll("%P", remoteNumOfPops+"");
        returnMe = returnMe.replaceAll("\n", System.getProperty("line.separator"));
        return returnMe;
    } // end getRemoteSessionView

//**********************************************************************************************************************

    protected final String getRemoteAverageView(){
        String timesAndScrambles = "", timesOnly = "";

        for(int i=0; i<12; i++){
            String currentTime = remoteCurrentAverage[i];
            if(Float.parseFloat(currentTime) == remoteCurrentFastest || Float.parseFloat(currentTime) == remoteCurrentSlowest)
                currentTime = "(" + currentTime + ")";
            timesAndScrambles += (i+1) + ")          " + currentTime + "          " + remoteCurrentScrambles[i] + "\n";
            timesOnly += currentTime + "\n";
        }

        String returnMe = averageViewFormat;
        returnMe = returnMe.replaceAll("%T", new Date()+"");
        returnMe = returnMe.replaceAll("%U", remoteUsername);
        returnMe = returnMe.replaceAll("%A", ssxx.format(remoteCurrentRollingAverage));
        returnMe = returnMe.replaceAll("%I", timesAndScrambles);
        returnMe = returnMe.replaceAll("%O", timesOnly);
        returnMe = returnMe.replaceAll("%F", ssxx.format(remoteCurrentFastest));
        returnMe = returnMe.replaceAll("%S", ssxx.format(remoteCurrentSlowest));
        returnMe = returnMe.replaceAll("\n", System.getProperty("line.separator"));
        return returnMe;
    } // end getRemoteAverageView

//**********************************************************************************************************************
/*
    protected static final String findAndReplace(String original, String find, String replace){
        while(true){
            int index = original.indexOf(find);
            if(index >= 0)
                original = original.substring(0, index) + replace + original.substring(index+find.length(), original.length());
            else
                break;
        }
        return original;
    } // end findAndReplace
*/
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
                    safePrint("I");
                }
            } else {
                if(!isTyping){
                    isTyping = true;
                    safePrint("I");
                }
            }
        }
    } // end keyReleased

//**********************************************************************************************************************

    protected void safePrint(String s){
        if(out != null){
            out.println(s);
            out.flush();
        }
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private VisiblityListener visiblityListener;

    public static interface VisiblityListener{
        public abstract void netmodeCallback();
    }

    public void addVisiblityListener(VisiblityListener visiblityListener){
        this.visiblityListener = visiblityListener;
    }

}
