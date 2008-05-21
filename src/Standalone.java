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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.*;
import java.applet.*;
import java.util.*;
import java.io.*;
import javax.swing.plaf.metal.*;
import javax.swing.border.*;

public class Standalone extends JFrame implements ActionListener, Runnable, Constants{

    private OptionsMenu optionsMenu;
    private ScrambleGenerator scrambleGenerator;
    private InstructionScreen instructionScreen;
    private AboutScreen aboutScreen;

    private ScrambleAlg scrambleAlg;
    private ScramblePane scramblePane;
    private TimerArea timerArea;
    private String newAlg;

    JButton startButton, discardButton, popButton, plusTwoButton, averageModeButton;
    JButton sessionResetButton, sessionDetailedViewButton, averageDetailedViewButton, insertTimeButton;
    JLabel puzzleLabel, countdownLabel, useThisAlgLabel, timerLabel;
    JLabel sessionStatsLabel, rollingAverageLabel, bestAverageLabel;
    JMenuBar jMenuBar;
    JMenu fileMenu, toolsMenu, networkMenu, helpMenu;
    JMenuItem saveBestItem, saveSessionItem, optionsItem, exitItem, importItem, generatorItem, instItem, aboutItem, serverItem, clientItem;
    JComboBox puzzleCombo, countdownCombo;
    JTextArea scrambleText, bestAverageText;

    JLabel[] averageLabels, timeLabels;
    String[] timeString;
    SmartButton[] smartButton;

    private boolean averageOfFiveMode; // experimental

    volatile Thread timerThread;
    AudioClip countdownClip = null;

    private double[] timeQueue = new double[100]; // gowd this is bad

    boolean runningCountdown;
    int placeInAverage = 0;
    double startTime = 0, stopTime = 0;
    double sessionTotalTime = 0, sessionFastest = 0, sessionSlowest = 0;
    double bestAverage = 0, bestStandardDeviation = 0, bestFastest = 0, bestSlowest = 0, previousAverage = 0;
    int countingDown = 0, cubesSolved = 0, sessionIndex = 0, acceptsSincePop = 12, numberOfPops = 0;
    String[] bestAverageTimes = new String[12], bestAverageScrambles = new String[12];
    String[] currentAverageScrambles = new String[12], currentAverageTimes = new String[12];
    String[] sessionTimes = new String[100], sessionScrambles = new String[100];

    DecimalFormat ssxx, ss;
    JFileChooser fc = new JFileChooser();

    String[] importedAlgs;
    boolean hasImported;
    int importedIndex;

//**********************************************************************************************************************

    public static void main(String[] args){
        // set look and feel to match native OS
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_TITLE);
        String nativeLF = UIManager.getSystemLookAndFeelClassName();
        try{
            //UIManager.setLookAndFeel(nativeLF);
            //UIManager.setLookAndFeel(new com.sun.java.swing.plaf.mac.MacLookAndFeel());
            //UIManager.setLookAndFeel(new com.sun.java.swing.plaf.motif.MotifLookAndFeel());
            //UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel());
            //UIManager.setLookAndFeel(new com.sun.java.swing.plaf.gtk.GTKLooktAndFeel());
            //UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
            //MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme()); UIManager.setLookAndFeel(new MetalLookAndFeel());
            MetalLookAndFeel.setCurrentTheme(new OceanTheme()); UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch(Exception e){}

        // create this frame and show it
        Standalone standalone = new Standalone();
        standalone.setVisible(true);
    } // end main

//**********************************************************************************************************************

    public Standalone(){
        // configure Contentpane
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        // configure JFrame
        setTitle(APP_TITLE);
        centerFrameOnScreen(860, 570);
        setIconImage((new ImageIcon(getClass().getResource("Cow.gif"))).getImage());
        setResizable(false);

        ssxx = (DecimalFormat)NumberFormat.getNumberInstance(new Locale("en", "US")); ssxx.applyPattern("00.00");
        ss = (DecimalFormat)NumberFormat.getNumberInstance(new Locale("en", "US")); ss.applyPattern("00");

        fc.setFileFilter(new TextFileFilter());
        fc.setAcceptAllFileFilterUsed(false);
        scrambleAlg = new ScrambleAlg();
        newAlg = "";

        // configure countdownclip
        try {
            countdownClip = Applet.newAudioClip(getClass().getResource("count.mid"));
        } catch(NullPointerException e){JOptionPane.showMessageDialog(this, "count.mid not found. There will be no countdown audio.");}

        // set up JMenuBar
        saveBestItem = new JMenuItem("Save Best Average As...");
        saveSessionItem = new JMenuItem("Save Session Average As...");
        optionsItem = new JMenuItem("Options");
        optionsItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        exitItem = new JMenuItem("Exit");
        importItem = new JMenuItem("Import Scrambles"); importItem.setMnemonic('I');
        importItem.setAccelerator(KeyStroke.getKeyStroke('I', 2));
        generatorItem = new JMenuItem("Generate Scrambles"); generatorItem.setMnemonic('G');
        generatorItem.setAccelerator(KeyStroke.getKeyStroke('G', 2));
        instItem = new JMenuItem("Instuctions");
        instItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        aboutItem = new JMenuItem("About " + APP_TITLE); aboutItem.setMnemonic('A');
        aboutItem.setAccelerator(KeyStroke.getKeyStroke('A', 2));
        serverItem = new JMenuItem("Start Server");
        serverItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        clientItem = new JMenuItem("Connect To Server");
        clientItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, 0));
        fileMenu = new JMenu("File"); fileMenu.setMnemonic('F');
        fileMenu.add(saveBestItem);
        fileMenu.add(saveSessionItem);
        fileMenu.addSeparator();
        fileMenu.add(optionsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        toolsMenu = new JMenu("Tools"); toolsMenu.setMnemonic('T');
        toolsMenu.add(importItem);
        toolsMenu.add(generatorItem);
        networkMenu = new JMenu("Network Timer"); networkMenu.setMnemonic('N');
        networkMenu.add(serverItem);
        networkMenu.add(clientItem);
        helpMenu = new JMenu("Help"); helpMenu.setMnemonic('H');
        helpMenu.add(instItem);
        helpMenu.add(aboutItem);
        jMenuBar = new JMenuBar();
        jMenuBar.add(fileMenu);
        jMenuBar.add(toolsMenu);
        jMenuBar.add(networkMenu);
        jMenuBar.add(Box.createHorizontalGlue());
        jMenuBar.add(helpMenu);
        setJMenuBar(jMenuBar);

        // inialize Popup Windows
        optionsMenu = new OptionsMenu(this); // pass it this so that it can update GUI when needed
        scrambleGenerator = new ScrambleGenerator();
        instructionScreen = new InstructionScreen();
        aboutScreen = new AboutScreen();

        // initialize GUI objects
        puzzleLabel = new JLabel("Puzzle:");
        puzzleCombo = new JComboBox(puzzleChoices);
        countdownLabel = new JLabel("Countdown:");
        countdownCombo = new JComboBox(countdownChoices);

        startButton = new JButton("Start Timer");
        discardButton = new JButton("Discard Time");
        discardButton.setEnabled(false);
        popButton = new JButton("POP");
        popButton.setEnabled(false);
        plusTwoButton = new JButton("+2");
        plusTwoButton.setEnabled(false);
        averageModeButton = new JButton("Average of 5 Mode");

        averageLabels = new JLabel[12];
        for(int i=0; i<12; i++)
            averageLabels[i] = new JLabel("#" + (i+1));

        smartButton = new SmartButton[12];
        for(int i=0; i<12; i++)
            smartButton[i] = new SmartButton("#" + (i+1));

        useThisAlgLabel = new JLabel("Use this Scramble Algorithm:");

        scrambleText = new JTextArea("");
        scrambleText.setFocusable(true);
        scrambleText.setEditable(false);
        scrambleText.setLineWrap(true);
        scrambleText.setWrapStyleWord(true);
        //scrambleText.setBackground(backColor);
        scrambleText.setForeground(Color.black);
        scrambleText.setBorder(blackLine);
        scrambleText.setFont(lgAlgFont);

        timerLabel = new JLabel("");
        timerLabel.setText(""); //"00.00"
        timerLabel.setVisible(false);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setFont(new Font("Serif", Font.PLAIN, 94));

        timerArea = new TimerArea(this); // kinda dangerous but this is going to be how we invoke the timerStart() and stuff

        scramblePane = new ScramblePane(282, 216+20); // needs to be changed in two places
        scramblePane.setBorder(BorderFactory.createTitledBorder(theBorder, "Scramble View"));
        scramblePane.setLayout(null);


        sessionStatsLabel = new JLabel("<html>Recent Time: N/A<br>Previous Time: N/A<br>Progress: N/A<br><br>Cubes Solved: 0<br>Session Average: N/A</html>");
        sessionStatsLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Session Statistics"));
        rollingAverageLabel = new JLabel("<html>Current Average: <font size=\"5\">N/A</font><br>Progress: N/A<br><br>Fastest Time: N/A<br>Slowest Time: N/A<br>Standard Deviation: N/A</html>");
        rollingAverageLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Rolling Average"));
        bestAverageLabel = new JLabel("");
        bestAverageLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Best Average"));

        bestAverageText = new JTextArea("Average: N/A\nIndividual Times: N/A");
        bestAverageText.setFont(regFont);
        bestAverageText.setBorder(blackLine);
        bestAverageText.setEditable(false);

        insertTimeButton = new JButton ("Insert Own Time");
        sessionDetailedViewButton = new JButton("Session Details");
        sessionDetailedViewButton.setEnabled(false);
        averageDetailedViewButton = new JButton("Details");
        averageDetailedViewButton.setEnabled(false);
        sessionResetButton = new JButton("Full Session Reset");

        enterPressesWhenFocused(startButton);
        enterPressesWhenFocused(discardButton);
        enterPressesWhenFocused(popButton);
        enterPressesWhenFocused(plusTwoButton);
        enterPressesWhenFocused(averageModeButton);
        enterPressesWhenFocused(sessionResetButton);
        enterPressesWhenFocused(sessionDetailedViewButton);
        enterPressesWhenFocused(averageDetailedViewButton);
        enterPressesWhenFocused(insertTimeButton);

        timeString = new String[12];
        timeLabels = new JLabel[12];
        for(int i=0; i<12; i++){
            timeString[i] = "none";
            timeLabels[i] = new JLabel("<html><font size=\"5\">" + timeString[i] + "</font></html>");
        }

        // set bounds
        setTheBounds();

        // add to contentPane
        addTheContent(contentPane);

        // add ActionListeners
        addTheActionListeners();

        // inital load of options
        optionsMenu.loadOptions();
        if(!optionsMenu.puzzleX.equals(puzzleCombo.getSelectedItem()+"")) // less glitchier
            puzzleCombo.setSelectedItem(optionsMenu.puzzleX);
        if(!optionsMenu.countdownX.equals(countdownCombo.getSelectedItem()+"")) // less glitchier
            countdownCombo.setSelectedItem(optionsMenu.countdownX);
        OptionsToGUI();

        // set some stuff up
        importedIndex = 0;
        hasImported = false;
        updateScrambleAlgs();
        timeLabels[0].setForeground(optionsMenu.currentColorX);
        returnFocus();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    } // end constructor

//**********************************************************************************************************************

    private void centerFrameOnScreen(int width, int height){
        setSize(width, height);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int appWidth = getSize().width, appHeight = getSize().height;
        setLocation((screenSize.width-appWidth)/2, (screenSize.height-appHeight)/2);
    }

//**********************************************************************************************************************

    private void setTheBounds(){

        puzzleLabel.setBounds(10,5,90,20);
        puzzleCombo.setBounds(10,25,90,20);
        countdownLabel.setBounds(110,5,90,20);
        countdownCombo.setBounds(110,25,90,20);
        startButton.setBounds(10,50,190,70+20);
        discardButton.setBounds(10,125+20,190,45);
        popButton.setBounds(10,175+20,90,45);
        plusTwoButton.setBounds(110,175+20,90,45);

        useThisAlgLabel.setBounds(215,5,333,20);
        scrambleText.setBounds(215,25,333,115);
        timerLabel.setBounds(215,125+12+20,333,75);
        timerArea.setBounds(215,125+20,333,75+21);

        scramblePane.setBounds(563,5,282,216+20); // needs to be changed in two places

        // total width is 834 if there is a 10 margin on each side
        // so use formula: margin = (834-12*width-11*separation)/2 + 10
        // initial x value is left margine
        int x = 14;
        int width = 67;
        int seperation = 2;
        for(int i=0; i<12; i++){
            smartButton[i].setBounds(x, 230+20, width, 46);
            averageLabels[i].setBounds(x, 230+20, width, 20);
            averageLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            timeLabels[i].setBounds(x, 245+20, width, 20);
            timeLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            x = x + width + seperation;
        }

        sessionStatsLabel.setBounds(10,270+10+20,412,130);
        rollingAverageLabel.setBounds(432,270+10+20,412,130);
        bestAverageLabel.setBounds(10,410+5+20,834,72);
        bestAverageText.setBounds(20,427+5+20,724,45);
        averageDetailedViewButton.setBounds(754,426+5+20,80,45);
        sessionDetailedViewButton.setBounds(311-70,286+20+20,160,20);
        insertTimeButton.setBounds(311-70,311+20+20,160,20);
        sessionResetButton.setBounds(311-70,336+20+20,160,20);
        averageModeButton.setBounds(663,286+20+20,160,20);
    }

//**********************************************************************************************************************

    private void addTheActionListeners(){

        saveBestItem.addActionListener(this);
        saveSessionItem.addActionListener(this);
        optionsItem.addActionListener(this);
        exitItem.addActionListener(this);
        importItem.addActionListener(this);
        generatorItem.addActionListener(this);
        instItem.addActionListener(this);
        aboutItem.addActionListener(this);
        serverItem.addActionListener(this);
        clientItem.addActionListener(this);
        puzzleCombo.addActionListener(this);
        countdownCombo.addActionListener(this);
        startButton.addActionListener(this); //HERE
        discardButton.addActionListener(this);
        popButton.addActionListener(this);
        plusTwoButton.addActionListener(this);
        averageModeButton.addActionListener(this);
        sessionDetailedViewButton.addActionListener(this);
        averageDetailedViewButton.addActionListener(this);
        sessionResetButton.addActionListener(this);
        insertTimeButton.addActionListener(this);
    }

//**********************************************************************************************************************

    private void addTheContent(Container contentPane){

        contentPane.add(puzzleLabel);
        contentPane.add(puzzleCombo);
        contentPane.add(countdownLabel);
        contentPane.add(countdownCombo);
        contentPane.add(startButton);
        contentPane.add(discardButton);
        contentPane.add(popButton);
        contentPane.add(plusTwoButton);
        //contentPane.add(averageModeButton);
        contentPane.add(useThisAlgLabel);
        contentPane.add(scrambleText);
        contentPane.add(timerLabel);
        //contentPane.add(timerArea);
        contentPane.add(scramblePane);
        contentPane.add(sessionStatsLabel);
        contentPane.add(rollingAverageLabel);
        contentPane.add(bestAverageLabel);
        contentPane.add(bestAverageText);
        contentPane.add(sessionDetailedViewButton);
        contentPane.add(averageDetailedViewButton);
        contentPane.add(sessionResetButton);
        contentPane.add(insertTimeButton);

        for(int i=0; i<12; i++){
            //contentPane.add(smartButton[i]);
            contentPane.add(averageLabels[i]);
            contentPane.add(timeLabels[i]);
        }
    }

//**********************************************************************************************************************
// Public Methods
//**********************************************************************************************************************

    public void actionPerformed(ActionEvent e){
        Object source = e.getSource();

        if(source == startButton){
            if(startButton.getText().equals("Start Timer")){
                timerStart();
            } else if(startButton.getText().equals("Stop Timer")){
                timerStop();
            } else if(startButton.getText().equals("Accept Time")){
                timerAccept();
            }
        } else if(source == discardButton){
            if(cubesSolved >= 1 || numberOfPops >= 1)
                sessionDetailedViewButton.setEnabled(true);
            if(cubesSolved >= 12)
                averageDetailedViewButton.setEnabled(true);
            insertTimeButton.setEnabled(true);
            sessionResetButton.setEnabled(true);
            discardButton.setEnabled(false);
            popButton.setEnabled(false);
            plusTwoButton.setEnabled(false);
            puzzleCombo.setEnabled(true);
            countdownCombo.setEnabled(true);
            startButton.setText("Start Timer");
            timerLabel.setText("");//timerLabel.setText("Ready2?");
            timerLabel.setVisible(false);
            updateScrambleAlgs();
            returnFocus();
        } else if(source == popButton){
            acceptsSincePop = 0;
            numberOfPops++;
            popButton.setText("Popped");
            if(sessionIndex > sessionTimes.length-1){
                String[] temp = new String[sessionTimes.length*2];
                String[] temp2 = new String[sessionTimes.length*2];
                for(int i=0; i<sessionTimes.length; i++){
                    temp[i] = sessionTimes[i];
                    temp2[i] = sessionScrambles[i];
                }
                sessionTimes = temp;
                sessionScrambles = temp2;
            }
            sessionTimes[sessionIndex] = "POP";
            sessionScrambles[sessionIndex] = newAlg;//scrambleText.getText();
            sessionIndex++;
            if(cubesSolved >= 12)
                averageDetailedViewButton.setEnabled(true);
            insertTimeButton.setEnabled(true);
            sessionDetailedViewButton.setEnabled(true);
            sessionResetButton.setEnabled(true);
            discardButton.setEnabled(false);
            popButton.setEnabled(false);
            plusTwoButton.setEnabled(false);
            puzzleCombo.setEnabled(true);
            countdownCombo.setEnabled(true);
            startButton.setText("Start Timer");
            timerLabel.setText("");//timerLabel.setText("Ready3?");
            timerLabel.setVisible(false);
            updateScrambleAlgs();
            returnFocus();
        } else if(source == plusTwoButton){
            stopTime = stopTime + 2000;//1200000; // TEMP: was 2000
            cubesSolved++;
            try{
                acceptTime();
            } catch(NumberFormatException l){
                JOptionPane.showMessageDialog(this, "There has been an error, please inform Chris that you saw this message.");
                System.out.println(l);
            }
        } else if(source == averageModeButton){
            if(averageModeButton.getText().equals("Average of 5 Mode")){
                averageModeButton.setText("Average of 10 Mode");
                averageOfFiveMode = true;
            } else{
                averageModeButton.setText("Average of 5 Mode");
                averageOfFiveMode = false;
            }
        } else if(source == sessionResetButton){
            if(optionsMenu.showResetConfirmX){
                int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to reset this session and lose all times?", "Warning!", 0);
                if(choice == 1){
                    returnFocus();
                    return;
                }
            }

            placeInAverage = 0;
            sessionIndex = 0;
            bestAverage = 0;
            cubesSolved = 0;
            sessionTotalTime = 0;
            sessionFastest = 0;
            sessionSlowest = 0;
            acceptsSincePop = 12;
            sessionTimes = new String[100];
            sessionScrambles = new String[100];
            bestAverageText.setText("Average: N/A\nIndividual Times: N/A");
            for(int i=0; i<12; i++){
                timeString[i] = "none";
                timeLabels[i].setText("<html><font size=\"5\">" + timeString[i] + "</font></html>");
                timeLabels[i].setForeground(Color.black);
            }
            timeLabels[placeInAverage].setForeground(optionsMenu.currentColorX);
            sessionStatsLabel.setText("<html>Recent Time: N/A<br>Previous Time: N/A<br>Progress: N/A<br><br>Cubes Solved: 0<br>Session Average: N/A</html>");
            rollingAverageLabel.setText("<html>Current Average: <font size=\"5\">N/A</font><br>Progress: N/A<br><br>Fastest Time: N/A<br>Slowest Time: N/A<br>Standard Deviation: N/A</html>");
            insertTimeButton.setEnabled(true);
            sessionDetailedViewButton.setEnabled(false);
            averageDetailedViewButton.setEnabled(false);
            discardButton.setEnabled(false);
            popButton.setEnabled(false);
            plusTwoButton.setEnabled(false);
            puzzleCombo.setEnabled(true);
            countdownCombo.setEnabled(true);
            startButton.setText("Start Timer");
            timerLabel.setText("");//timerLabel.setText("Ready4?");
            timerLabel.setVisible(false);
            hasImported = false;
            updateScrambleAlgs();
            returnFocus();
        } else if(source == puzzleCombo){
            updateScrambleAlgs();
            returnFocus();
        } else if(source == countdownCombo){
            returnFocus();
        } else if(source == sessionDetailedViewButton){
            DetailedView win = new DetailedView("Session Times", getSessionView(), optionsMenu.textBackgrColorX);
            win.setVisible(true);
        } else if(source == averageDetailedViewButton){
            DetailedView win = new DetailedView("Best Average", getAverageView(), optionsMenu.textBackgrColorX);
            win.setVisible(true);
        } else if(source == saveSessionItem){
            if(cubesSolved >= 1){
                int userChoice = fc.showSaveDialog(Standalone.this);
                if(userChoice == JFileChooser.APPROVE_OPTION)
                    saveToFile(getSessionView(), fc.getSelectedFile());
            } else {
                JOptionPane.showMessageDialog(this, "No times have been recorded for this session.");
            }
        } else if(source == saveBestItem){
            if(cubesSolved >= 12){
                int userChoice = fc.showSaveDialog(Standalone.this);
                if(userChoice == JFileChooser.APPROVE_OPTION)
                    saveToFile(getAverageView(), fc.getSelectedFile());
            } else {
                JOptionPane.showMessageDialog(this, "Not enough cubes have been solved to calculate an average.");
            }
        } else if(source == exitItem){
            System.exit(0);
        } else if(source == generatorItem){
            if(!scrambleGenerator.isVisible()){
                scrambleGenerator.puzzleCombo.setSelectedItem(puzzleCombo.getSelectedItem()+"");
                scrambleGenerator.setVisible(true);
            }
        } else if(source == instItem){
            if(!instructionScreen.isVisible())
                instructionScreen.setVisible(true);
        } else if(source == aboutItem){
            if(!aboutScreen.isVisible())
                aboutScreen.setVisible(true);
        } else if(source == importItem){
            int userChoice = fc.showOpenDialog(Standalone.this);
            if(userChoice == JFileChooser.APPROVE_OPTION){
                String input = "";
                try{
                    FileReader fr = new FileReader(fc.getSelectedFile());
                    BufferedReader in = new BufferedReader(fr);
                    String read;
                    while ((read = in.readLine()) != null){
                        input = input + read + "%";
                    }
                    in.close();
                } catch(IOException g){JOptionPane.showMessageDialog(this, "There was an error opening the file.");}
                StringTokenizer st = new StringTokenizer(input, "%");
                importedAlgs = new String[st.countTokens()];
                for(int i=0;i<importedAlgs.length;i++)
                    importedAlgs[i] = st.nextToken();
                hasImported = true;
                importedIndex = 0;
                updateScrambleAlgs();
            }
        } else if(source == insertTimeButton){
            String input = JOptionPane.showInputDialog(this, "Enter time to add in seconds or POP:");
            if(input == null){return;}
            if(input.equalsIgnoreCase("POP")){
                numberOfPops++;
                if(sessionIndex > sessionTimes.length-1){
                    String[] temp = new String[sessionTimes.length*2];
                    String[] temp2 = new String[sessionTimes.length*2];
                    for(int i=0; i<sessionTimes.length; i++){
                        temp[i] = sessionTimes[i];
                        temp2[i] = sessionScrambles[i];
                    }
                    sessionTimes = temp;
                    sessionScrambles = temp2;
                }
                sessionTimes[sessionIndex] = "POP";
                sessionScrambles[sessionIndex] = newAlg;//scrambleText.getText();
                sessionIndex++;
                updateScrambleAlgs();
                sessionDetailedViewButton.setEnabled(true);
                return;
            }
            double inputTime = 0;
            if(input != null){
                try{
                    inputTime = Double.parseDouble(ssxx.format(Double.parseDouble(input))) * 1000;
                } catch(NumberFormatException h){
                    JOptionPane.showMessageDialog(this, "Invalid number entered. No time was added to the session.");
                    return;
                }
                startTime = 0;
                stopTime = inputTime;
                cubesSolved++;
                try{
                    acceptTime();
                } catch(NumberFormatException v){
                    JOptionPane.showMessageDialog(this, "There has been an error, please inform Chris that you saw this message.");
                    System.out.println(v);
                }
                insertTimeButton.requestFocus();
            }
        } else if(source == optionsItem){
            optionsMenu.setVisible(true);
        } else if(source == serverItem){
            int choice = JOptionPane.showConfirmDialog(this, "Switching to Server Mode destroys main window session. Are you sure?", "Warning!", 0);
            if(choice == 1){
                returnFocus();
                return;
            }
            //this.setVisible(false);
            Server server = new Server(puzzleCombo.getSelectedItem()+"", countdownCombo.getSelectedItem()+"", optionsMenu);
            server.setVisible(true);
            disposeAll(); //this.dispose();
        } else if(source == clientItem){
            int choice = JOptionPane.showConfirmDialog(this, "Switching to Client Mode destroys main window session. Are you sure?", "Warning!", 0);
            if(choice == 1){
                returnFocus();
                return;
            }
            //this.setVisible(false);
            Client client = new Client(optionsMenu);
            client.setVisible(true);
            disposeAll(); //this.dispose();
        }
    } // end actionPerformed

//**********************************************************************************************************************

    public void run(){
        Thread thisThread = Thread.currentThread();
        while(timerThread == thisThread){
            if(runningCountdown){
                if(countingDown == 0){
                    runningCountdown = false;
                    startButton.setEnabled(true);
                    returnFocus();
                    timerLabel.setForeground(optionsMenu.timerColorX);
                    startTime = System.currentTimeMillis();
                } else if(countingDown == 3){
                    try{
                        countdownClip.play();
                    } catch(NullPointerException e){}
                    timerLabel.setText(countingDown+"");
                    countingDown--;
                    try{
                        timerThread.sleep(1000);
                    } catch(InterruptedException e){}
                } else {
                    timerLabel.setText(countingDown+"");
                    countingDown--;
                    try{
                        timerThread.sleep(1000);
                    } catch(InterruptedException e){}
                }
            } else {
                double time = (System.currentTimeMillis()-startTime)/1000;
                if(time>=60 && optionsMenu.showMinutesX){
                    int min = (int)(time/60);
                    double sec = time-(min*60);
                    timerLabel.setText(min + ":" + ((time < 600) ? ssxx.format(sec) : ss.format(sec)));
                } else {
                    timerLabel.setText(ssxx.format(time));
                }
                try{
                    timerThread.sleep(120);
                } catch(InterruptedException e){
                    System.out.println(e);
                }
            }
        }
    } // end run

//**********************************************************************************************************************

    public static void enterPressesWhenFocused(JButton button){

        button.registerKeyboardAction(
            button.getActionForKeyStroke(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false)),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
                JComponent.WHEN_FOCUSED);

        button.registerKeyboardAction(
            button.getActionForKeyStroke(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true)),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                JComponent.WHEN_FOCUSED);

    }

//**********************************************************************************************************************
// Private Methods
//**********************************************************************************************************************

    private void acceptTime() throws NumberFormatException{
        double time = (stopTime-startTime)/1000;
        for(int i=11; i>0; i--) timeQueue[i] = timeQueue[i-1];
        timeQueue[0] = Double.parseDouble(ssxx.format(time));
        if(time>=60 && optionsMenu.showMinutesX){
            int min = (int)(time/60);
            double sec = time-(min*60);
            timeString[placeInAverage] = min + ":" + ((time < 600) ? ssxx.format(sec) : ss.format(sec));
        } else {
            timeString[placeInAverage] = ssxx.format(time);
        }
        timeLabels[placeInAverage].setText("<html><font size=\"5\">" + timeString[placeInAverage] + "</font></html>");

        // grow the two session lists by a factor of 2 when near full
        if(sessionIndex > sessionTimes.length-1){
            String[] temp = new String[sessionTimes.length*2];
            String[] temp2 = new String[sessionTimes.length*2];
            for(int i=0; i<sessionTimes.length; i++){
                temp[i] = sessionTimes[i];
                temp2[i] = sessionScrambles[i];
            }
            sessionTimes = temp;
            sessionScrambles = temp2;
        }

        sessionTotalTime = sessionTotalTime + Double.parseDouble(ssxx.format(time));
        sessionTimes[sessionIndex] = ssxx.format(time);
        sessionScrambles[sessionIndex] = newAlg;//scrambleText.getText();
        currentAverageScrambles[placeInAverage] = newAlg;//scrambleText.getText();
        currentAverageTimes[placeInAverage] = ssxx.format(time);

        if(Double.parseDouble(sessionTimes[sessionIndex]) < sessionFastest || Double.parseDouble(sessionTimes[sessionIndex]) > sessionSlowest || sessionFastest == 0 || sessionSlowest == 0){
            if(sessionFastest == 0 || sessionSlowest == 0){
                sessionFastest = Double.parseDouble(sessionTimes[sessionIndex]);
                sessionSlowest = Double.parseDouble(sessionTimes[sessionIndex]);
            } else if(Double.parseDouble(sessionTimes[sessionIndex]) < sessionFastest){
                sessionFastest = Double.parseDouble(sessionTimes[sessionIndex]);
            } else if(Double.parseDouble(sessionTimes[sessionIndex]) > sessionSlowest){
                sessionSlowest = Double.parseDouble(sessionTimes[sessionIndex]);
            }
        }

        for(int i=0; i<12; i++)
            timeLabels[i].setForeground(Color.black);

        if(cubesSolved >= 12){
            int fastest = 0;
            int slowest = 0;

            for(int i=0; i<12; i++){
                if(Double.parseDouble(currentAverageTimes[i]) < Double.parseDouble(currentAverageTimes[fastest]))
                    fastest = i;
                else if(Double.parseDouble(currentAverageTimes[i]) > Double.parseDouble(currentAverageTimes[slowest]))
                    slowest = i;
            }

            timeLabels[fastest].setForeground(optionsMenu.fastestColorX);
            timeLabels[slowest].setForeground(optionsMenu.slowestColorX);

            double average = 0;

            for(int i=0; i<12; i++)
                if(i!=fastest && i!=slowest)
                    average = average + Double.parseDouble(currentAverageTimes[i]);

            average /= 10.0;

            double standardDeviation = 0;
            for(int i=0; i<12; i++){
                if(i!=fastest && i!=slowest)
                    standardDeviation = standardDeviation + ((average - Double.parseDouble(currentAverageTimes[i])) * (average - Double.parseDouble(currentAverageTimes[i])));
            }
            standardDeviation = Math.sqrt(standardDeviation/9);

            String progressColor, progress;
            progress = ssxx.format(average-previousAverage);
            try{
                if(Double.parseDouble(progress) > 0){
                    progressColor = "#FF0000";
                } else if(Double.parseDouble(progress) < 0){
                    progressColor = "#0000FF";
                } else {
                    progressColor = "#000000";
                    progress = "00.00";
                }
            } catch(NumberFormatException e){
                    progressColor = "#000000";
            }
            String printedAverage;
            if(average>=60 && optionsMenu.showMinutesX){
                int minutes = (int)(average/60);
                printedAverage = minutes + ":" + ssxx.format(average-(minutes*60));
            } else {
                printedAverage = ssxx.format(average) + " sec.";
            }
            rollingAverageLabel.setText("<html>Current Average: <font size=\"5\">" + printedAverage + "</font><br>Progress: <font color=\"" + progressColor + "\">" + progress +" sec.</font><br><br>Fastest Time: " + timeString[fastest] + "<br>Slowest Time: " + timeString[slowest] + "<br>Standard Deviation: " + ssxx.format(standardDeviation) + "</html>");
            previousAverage = average;

            if(average<bestAverage || bestAverage==0){
                bestAverage = average;
                String bestAverageFormated;
                if(bestAverage>=60 && optionsMenu.showMinutesX){
                    int minutes = (int)(bestAverage/60);
                    bestAverageFormated = minutes + ":" + ssxx.format(bestAverage-(minutes*60));
                } else {
                    bestAverageFormated = ssxx.format(bestAverage) + " sec.";
                }
                bestFastest = Double.parseDouble(currentAverageTimes[fastest]);
                bestSlowest = Double.parseDouble(currentAverageTimes[slowest]);
                bestStandardDeviation = standardDeviation;

                //copy times and scrambles
                String[] temp = new String[12];
                String[] temp2 = new String[12];

                for(int i=0; i<12; i++){
                    if(Double.parseDouble(currentAverageTimes[i])>=60 && optionsMenu.showMinutesX){
                        int minutes = (int)(Double.parseDouble(currentAverageTimes[i])/60);
                        temp[i] = minutes + ":" + ssxx.format(Double.parseDouble(currentAverageTimes[i])-(minutes*60));
                    } else {
                        temp[i] = currentAverageTimes[i];
                    }
                    if(i==fastest || i==slowest)
                        temp[i] = "(" + temp[i] + ")";
                    temp2[i] = currentAverageScrambles[i];
                }

                //put times in order
                int p = 0;
                for(int i=placeInAverage+1; i<12; i++){
                    bestAverageTimes[p] = temp[i];
                    bestAverageScrambles[p] = temp2[i];
                    p++;
                }
                for(int i=0; i<placeInAverage+1; i++){
                    bestAverageTimes[p] = temp[i];
                    bestAverageScrambles[p] = temp2[i];
                    p++;
                }

                String invTimes = "";
                for(int i=0; i<12; i++)
                    invTimes = invTimes + bestAverageTimes[i] + ", ";
                invTimes = invTimes.substring(0, invTimes.length()-2);

                bestAverageText.setText("Average: " + bestAverageFormated + "\nIndividual Times: " + invTimes);
            }
        }

        String previousTime, progress;
        if(cubesSolved > 1){
            if(placeInAverage == 0){
                previousTime = timeString[11];
                progress = ssxx.format(Double.parseDouble(currentAverageTimes[placeInAverage]) - Double.parseDouble(currentAverageTimes[11]));
            } else {
                previousTime = timeString[placeInAverage-1];
                progress = ssxx.format(Double.parseDouble(currentAverageTimes[placeInAverage]) - Double.parseDouble(currentAverageTimes[placeInAverage-1]));
            }
        } else {
            previousTime = "N/A";
            progress = "N/A";
        }
        String sessionAverage = (ssxx.format(sessionTotalTime/cubesSolved));
        if(Double.parseDouble(sessionAverage)>=60 && optionsMenu.showMinutesX){
            int min = (int)(Double.parseDouble(sessionAverage)/60);
            sessionAverage = min + ":" + ssxx.format(Double.parseDouble(sessionAverage)-(min*60));
        } else {
            sessionAverage = sessionAverage + " sec.";
        }

        String progressColor;
        try{
            if(Double.parseDouble(progress) > 0){
                progressColor = "#FF0000";
                progress = progress + " sec.";
            } else if(Double.parseDouble(progress) < 0){
                progressColor = "#0000FF";
                progress = progress + " sec.";
            } else {
                progressColor = "#000000";
                progress = "00.00 sec.";
            }
        } catch(NumberFormatException e){
                progressColor = "#000000";
        }

        sessionStatsLabel.setText("<html>Recent Time: " + currentAverageTimes[placeInAverage] + "<br>Previous Time: " + previousTime + "<br>Progress: <font color=\"" + progressColor + "\">" + progress + "</font><br><br>Cubes Solved: " + cubesSolved + "<br>Session Average: " + sessionAverage + "</html>");

        placeInAverage++;
        sessionIndex++;
        acceptsSincePop++;
        if(placeInAverage == 12)
            placeInAverage = 0;
        timeLabels[placeInAverage].setForeground(optionsMenu.currentColorX);

        if(cubesSolved >= 12)
            averageDetailedViewButton.setEnabled(true);
        insertTimeButton.setEnabled(true);
        sessionDetailedViewButton.setEnabled(true);
        sessionResetButton.setEnabled(true);
        discardButton.setEnabled(false);
        popButton.setEnabled(false);
        plusTwoButton.setEnabled(false);
        puzzleCombo.setEnabled(true);
        countdownCombo.setEnabled(true);
        startButton.setText("Start Timer");
        timerLabel.setText("");//timerLabel.setText("Ready5?");
        timerLabel.setVisible(false);
        updateScrambleAlgs();
        returnFocus();
    } // end acceptTime

//**********************************************************************************************************************

    private String getSessionView(){
        String timesAndScrambles = "", timesOnly = "";
        double deviation = 0.0, average = 0.0;
        if(cubesSolved >= 1)
            average = sessionTotalTime/cubesSolved;

        String formatedAverage;
        if(average>=60 && optionsMenu.showMinutesX){
            int minutes = (int)(average/60);
            formatedAverage = minutes + ":" + ssxx.format(average - minutes*60);
        } else
            formatedAverage = ssxx.format(average);

        for(int i=0; i<sessionIndex; i++){
            String currentTime = "";
            if((!sessionTimes[i].equals("POP")) && Double.parseDouble(sessionTimes[i])>=60 && optionsMenu.showMinutesX){
                int minutes = (int)(Double.parseDouble(sessionTimes[i])/60);
                currentTime = minutes + ":" + ssxx.format(Double.parseDouble(sessionTimes[i])-(minutes*60));
            } else
                currentTime = sessionTimes[i];
            timesAndScrambles = timesAndScrambles + (i+1) + ")          " + currentTime + "          " + sessionScrambles[i] + "\n";
            timesOnly = timesOnly + currentTime + "\n";
            if(cubesSolved >= 2){
                if(!(sessionTimes[i].equals("POP")))
                    deviation = deviation + ((average-Double.parseDouble(sessionTimes[i]))*(average-Double.parseDouble(sessionTimes[i])));
            }
        }
        if(cubesSolved >= 2)
            deviation = Math.sqrt(deviation/(cubesSolved-1));

        String formatedFastest;
        String formatedSlowest;
        if(sessionFastest>=60 && optionsMenu.showMinutesX){
            int minutes = (int)(sessionFastest/60);
            formatedFastest = minutes + ":" + ssxx.format(sessionFastest - minutes*60);
        } else {
            formatedFastest = ssxx.format(sessionFastest);
        }
        if(sessionSlowest>=60 && optionsMenu.showMinutesX){
            int minutes = (int)(sessionSlowest/60);
            formatedSlowest = minutes + ":" + ssxx.format(sessionSlowest - minutes*60);
        } else {
            formatedSlowest = ssxx.format(sessionSlowest);
        }

        String returnMe = optionsMenu.sessionViewFormatX;
        returnMe = findAndReplace(returnMe, "%T", new Date()+"");
        returnMe = findAndReplace(returnMe, "%A", formatedAverage);
        returnMe = findAndReplace(returnMe, "%I", timesAndScrambles);
        returnMe = findAndReplace(returnMe, "%O", timesOnly);
        returnMe = findAndReplace(returnMe, "%F", formatedFastest);
        returnMe = findAndReplace(returnMe, "%S", formatedSlowest);
        returnMe = findAndReplace(returnMe, "%D", ssxx.format(deviation));
        returnMe = findAndReplace(returnMe, "%C", cubesSolved+"");
        returnMe = findAndReplace(returnMe, "%P", numberOfPops+"");
        returnMe = returnMe.replaceAll("\n", System.getProperty("line.separator"));
        return returnMe;
    } // end getSessionView

//**********************************************************************************************************************

    private String getAverageView(){
        String timesAndScrambles = "";
        String timesOnly = "";

        for(int i=0; i<12; i++){
            timesAndScrambles = timesAndScrambles + (i+1) + ")          " + bestAverageTimes[i] + "          " + bestAverageScrambles[i] + "\n";
            timesOnly = timesOnly + bestAverageTimes[i] + "\n";
        }

        String formatedAverage;
        if(bestAverage>=60 && optionsMenu.showMinutesX){
            int minutes = (int)(bestAverage/60);
            formatedAverage = minutes + ":" + ssxx.format(bestAverage - minutes*60);
        } else {
            formatedAverage = ssxx.format(bestAverage);
        }

        String formatedFastest;
        if(bestFastest>=60 && optionsMenu.showMinutesX){
            int minutes = (int)(bestFastest/60);
            formatedFastest = minutes + ":" + ssxx.format(bestFastest - minutes*60);
        } else {
            formatedFastest = ssxx.format(bestFastest);
        }

        String formatedSlowest;
        if(bestSlowest>=60 && optionsMenu.showMinutesX){
            int minutes = (int)(bestSlowest/60);
            formatedSlowest = minutes + ":" + ssxx.format(bestSlowest - minutes*60);
        } else {
            formatedSlowest = ssxx.format(bestSlowest);
        }

        String returnMe = optionsMenu.averageViewFormatX;
        returnMe = findAndReplace(returnMe, "%T", new Date()+"");
        returnMe = findAndReplace(returnMe, "%A", formatedAverage);
        returnMe = findAndReplace(returnMe, "%I", timesAndScrambles);
        returnMe = findAndReplace(returnMe, "%O", timesOnly);
        returnMe = findAndReplace(returnMe, "%D", ssxx.format(bestStandardDeviation));
        returnMe = findAndReplace(returnMe, "%F", formatedFastest);
        returnMe = findAndReplace(returnMe, "%S", formatedSlowest);
        returnMe = returnMe.replaceAll("\n", System.getProperty("line.separator"));
        return returnMe;
    } // end getAverageView

//**********************************************************************************************************************

    private static final String findAndReplace(String original, String find, String replace){
        while(true){
            int index = original.indexOf(find);
            if(index >= 0)
                original = original.substring(0, index) + replace + original.substring(index+find.length(), original.length());
            else
                break;
        }
        return original;
    } // end findAndReplace

//**********************************************************************************************************************

    private void saveToFile(String text, File file){
        File outputFile = new File(file+".txt");
        try{
            FileWriter fr = new FileWriter(outputFile);
            BufferedWriter out = new BufferedWriter(fr);
            out.write(text);
            out.close();
        } catch(IOException e){
            JOptionPane.showMessageDialog(this, "There was an error saving. You may not have write permissions.");
        }
    } // end saveToFile

//**********************************************************************************************************************

    private void updateScrambleAlgs(){
        scrambleText.setFont(puzzleCombo.getSelectedItem() == "Megaminx" ? smAlgFont : lgAlgFont);
        if(hasImported && (importedIndex < importedAlgs.length)){
            newAlg = importedAlgs[importedIndex];
            scrambleText.setText(newAlg.replaceAll(ALG_BREAK, "\n"));
            importedIndex++;
        }
        else{
            if(hasImported){
                JOptionPane.showMessageDialog(this, "All imported scrambles have been used. Random scrambles will now be displayed.");
                hasImported = false;
            }
            newAlg = scrambleAlg.generateAlg(puzzleCombo.getSelectedItem()+"");
            scrambleText.setText(newAlg.replaceAll(ALG_BREAK, "\n"));
        }
        updateScramblePane();
    } // end updateScrambleAlgs

//**********************************************************************************************************************

    private void updateScramblePane(){
        scramblePane.newScramble(puzzleCombo.getSelectedItem()+"", newAlg.replaceAll(ALG_BREAK, " "));//scrambleText.getText());
    }

//**********************************************************************************************************************

    public void timerStart(){
        startButton.setText("Stop Timer");
        runningCountdown = true;
        countingDown = Integer.parseInt(countdownCombo.getSelectedItem()+"");
        timerLabel.setForeground(optionsMenu.countdownColorX);
        timerLabel.setVisible(true);
        insertTimeButton.setEnabled(false);
        sessionResetButton.setEnabled(false);
        sessionDetailedViewButton.setEnabled(false);
        averageDetailedViewButton.setEnabled(false);
        puzzleCombo.setEnabled(false);
        countdownCombo.setEnabled(false);
        timerThread = new Thread(this);
        timerThread.start();
    }

//**********************************************************************************************************************

    public void timerStop(){
        if(runningCountdown){
            timerThread = null;
            if(cubesSolved>=1 || numberOfPops>=1)
                sessionDetailedViewButton.setEnabled(true);
            insertTimeButton.setEnabled(true);
            averageDetailedViewButton.setEnabled(true);
            sessionResetButton.setEnabled(true);
            discardButton.setEnabled(false);
            popButton.setEnabled(false);
            plusTwoButton.setEnabled(false);
            puzzleCombo.setEnabled(true);
            countdownCombo.setEnabled(true);
            startButton.setText("Start Timer");
            timerLabel.setText("");//timerLabel.setText("Ready1?");
            timerLabel.setVisible(false);
            countdownClip.stop();
            returnFocus();
        } else {
            stopTime = System.currentTimeMillis();
            timerThread = null;
            double time = (stopTime-startTime)/1000;
            if(time>=60 && optionsMenu.showMinutesX){
                int min = (int)(time/60);
                double sec = time - min*60;
                timerLabel.setText(min + ":" + ((time < 600) ? ssxx.format(sec) : ss.format(sec)));
            } else {
                timerLabel.setText(ssxx.format(time));
            }
            startButton.setText("Accept Time");
            if(acceptsSincePop >= 12 || acceptsSincePop == -1){
                popButton.setText("POP");
                popButton.setEnabled(true);
            }
            discardButton.setEnabled(true);
            plusTwoButton.setEnabled(true);
        }
    }

//**********************************************************************************************************************
    public void timerAccept(){
        cubesSolved++;
        try{
            acceptTime();
        } catch(NumberFormatException j){
            JOptionPane.showMessageDialog(this, "There has been an error, please inform Chris that you saw this message.");
            System.out.println(j);
        }
    }

//**********************************************************************************************************************

    private void returnFocus(){ // debug: just to have it change one place while we try new stuff
        startButton.requestFocus();
        //timerArea.requestFocus();
    }

//**********************************************************************************************************************

    private void disposeAll(){
        optionsMenu.dispose();
        scrambleGenerator.dispose();
        instructionScreen.dispose();
        aboutScreen.dispose();
        this.dispose();
    }

//**********************************************************************************************************************

    public void OptionsToGUI(){
        //if(!optionsMenu.puzzleX.equals(puzzleCombo.getSelectedItem()+""))
        //    puzzleCombo.setSelectedItem(optionsMenu.puzzleX);
        //if(!optionsMenu.countdownX.equals(countdownCombo.getSelectedItem()+""))
        //    countdownCombo.setSelectedItem(optionsMenu.countdownX);

        timeLabels[placeInAverage].setForeground(optionsMenu.currentColorX);
        scramblePane.setCubeColors(optionsMenu.cubeColorsX);
        scramblePane.setPyraminxColors(optionsMenu.pyraminxColorsX);
        scramblePane.setMegaminxColors(optionsMenu.megaminxColorsX);
        updateScramblePane();
        scrambleText.setBackground(optionsMenu.textBackgrColorX);
        bestAverageText.setBackground(optionsMenu.textBackgrColorX);
    } //OptionsToGUI

}