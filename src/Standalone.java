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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.*;
import java.applet.*;
import java.util.*;
import java.io.*;
import javax.swing.plaf.metal.*;
import javax.swing.border.*;

public class Standalone extends JFrame implements ActionListener, Runnable, OptionsBox.OptionsListener, NetcubeMode.VisiblityListener, Constants{

    private OptionsBox optionsBox;
    private ScrambleGenerator scrambleGenerator;
    private InstructionScreen instructionScreen;
    private AboutScreen aboutScreen;
    private Server server;
    private Client client;

    private ScrambleAlg scrambleAlg;
    private ScramblePanel scramblePanel;
    private TimerArea timerArea;
    private String newAlg;

    JComboBox puzzleCombo, countdownCombo;
    JTextArea scrambleText, bestAverageText;
    JButton startButton, discardButton, dnfButton, plusTwoButton;
    JButton sessionResetButton, sessionDetailedViewButton, averageDetailedViewButton, insertTimeButton;
    JLabel puzzleLabel, countdownLabel, useThisAlgLabel, timerLabel;
    JLabel sessionStatsLabel, rollingAverageLabel, bestAverageLabel;

    // JMenu stuff
    JMenuBar jMenuBar;
    JMenu fileMenu, toolsMenu, networkMenu, helpMenu;
    JMenuItem saveBestItem, saveSessionItem, optionsItem, exitItem, importItem, generatorItem, instItem, aboutItem, serverItem, clientItem;

    JLabel[] averageLabels, timeLabels;
    SmartButton[] smartButton;

    volatile Thread timerThread;
    private boolean runningCountdown;
    AudioClip countdownClip = null;

    SolveTable solveTable;
    private boolean sessionDetailsEnabled, averageDetailsEnabled;
    private int countingDown;
    private long startTime, stopTime;

    JFileChooser fc = new JFileChooser();

    private String[] importedAlgs;
    private boolean hasImported;
    private int importedIndex;

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
        } catch(Exception ex){}


        Standalone standalone = new Standalone();
    } // end main

//**********************************************************************************************************************

    public Standalone(){
        // configure Contentpane
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        // configure JFrame
        setTitle(APP_TITLE);
        RJT_Utils.centerJFrame(this, 860, 570);
        RJT_Utils.configureJFrame(this);

        fc.setFileFilter(new TextFileFilter());
        fc.setAcceptAllFileFilterUsed(false);

        scrambleAlg = new ScrambleAlg();
        newAlg = "";

        // configure countdownclip
        try {
            countdownClip = Applet.newAudioClip(getClass().getResource("count.mid"));
        } catch(NullPointerException ex){JOptionPane.showMessageDialog(this, "count.mid not found. There will be no countdown audio.");}

        // set up JMenuBar
        makeJMenuBar();
        setJMenuBar(jMenuBar);

        // inialize Popup Windows
        optionsBox = new OptionsBox();
        scrambleGenerator = new ScrambleGenerator(fc);
        instructionScreen = new InstructionScreen();
        aboutScreen = new AboutScreen();

        server = new Server(fc, optionsBox, scrambleGenerator, instructionScreen, aboutScreen);
        server.setVisible(false);
        server.addVisiblityListener(this);

        client = new Client(fc, optionsBox, scrambleGenerator, instructionScreen, aboutScreen);
        client.setVisible(false);
        client.addVisiblityListener(this);

        // initialize GUI objects
        puzzleLabel = new JLabel("Puzzle:");
        puzzleCombo = new JComboBox(puzzleChoices);
        countdownLabel = new JLabel("Countdown:");
        countdownCombo = new JComboBox(countdownChoices);

        startButton = new JButton("Start Timer");
        discardButton = new JButton("Discard Time");
        dnfButton = new JButton();
        plusTwoButton = new JButton("+2");

        averageLabels = new JLabel[12];
        for(int i=0; i<12; i++)
            averageLabels[i] = new JLabel();

        smartButton = new SmartButton[12];
        for(int i=0; i<12; i++)
            smartButton[i] = new SmartButton("#" + (i+1));

        useThisAlgLabel = new JLabel();//"Use this Scramble Algorithm:");

        scrambleText = new JTextArea("");
        scrambleText.setFocusable(true);
        scrambleText.setEditable(false);
        scrambleText.setLineWrap(true);
        scrambleText.setWrapStyleWord(true);
        scrambleText.setForeground(Color.black);
        scrambleText.setBorder(blackLine);
        scrambleText.setFont(lgAlgFont);

        timerLabel = new JLabel();
        //timerLabel.setText("");
        //timerLabel.setVisible(false);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setFont(timerFont);

        timerArea = new TimerArea(this); // kinda dangerous but this is going to be how we invoke the timerStart() and stuff

        scramblePanel = new ScramblePanel(282, 215+20); // needs to be changed in two places
        scramblePanel.setLayout(null);
        //scramblePanel.setBorder(BorderFactory.createTitledBorder(theBorder, "Scramble View"));


        sessionStatsLabel = new JLabel();
        //sessionStatsLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Session Statistics"));
        rollingAverageLabel = new JLabel();
        //rollingAverageLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Rolling Average"));
        bestAverageLabel = new JLabel();
        //bestAverageLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Best Average"));

        bestAverageText = new JTextArea();
        bestAverageText.setFont(regFont);
        bestAverageText.setBorder(blackLine);
        bestAverageText.setEditable(false);

        insertTimeButton = new JButton ("Insert Own Time");
        sessionDetailedViewButton = new JButton("Session Details");
        averageDetailedViewButton = new JButton("Details");
        sessionResetButton = new JButton("Full Session Reset");

        RJT_Utils.enterPressesWhenFocused(startButton);
        RJT_Utils.enterPressesWhenFocused(discardButton);
        RJT_Utils.enterPressesWhenFocused(dnfButton);
        RJT_Utils.enterPressesWhenFocused(plusTwoButton);
        RJT_Utils.enterPressesWhenFocused(sessionResetButton);
        RJT_Utils.enterPressesWhenFocused(sessionDetailedViewButton);
        RJT_Utils.enterPressesWhenFocused(averageDetailedViewButton);
        RJT_Utils.enterPressesWhenFocused(insertTimeButton);

        timeLabels = new JLabel[12];
        for(int i=0; i<12; i++)
            timeLabels[i] = new JLabel();


        runningCountdown = false;
        countingDown = 0;
        startTime = 0;
        stopTime = 0;


        optionsBox.addOptionsListener(this); // pass it this so that it can update GUI when needed
        optionsBox.loadOptions(); // inital load of options
        solveTable = new SolveTable(optionsBox.puzzleX);
        updateLabels(optionsBox.puzzleX);

        if(!optionsBox.puzzleX.equals(puzzleCombo.getSelectedItem()+"")) // less glitchier
            puzzleCombo.setSelectedItem(optionsBox.puzzleX);
        if(!optionsBox.countdownX.equals(countdownCombo.getSelectedItem()+"")) // less glitchier
            countdownCombo.setSelectedItem(optionsBox.countdownX);

        updateGUI();
        resetTheSession();

        // set bounds
        setTheBounds();
        // add to contentPane
        addTheContent(contentPane);
        // add ActionListeners
        addTheActionListeners();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
        returnFocus();

    } // end constructor

//**********************************************************************************************************************

    private void makeJMenuBar(){
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
        serverItem = new JMenuItem("Server Mode");
        serverItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        clientItem = new JMenuItem("Client Mode");
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
//        helpMenu.add(instItem);
        helpMenu.add(aboutItem);
        jMenuBar = new JMenuBar();
        jMenuBar.add(fileMenu);
        jMenuBar.add(toolsMenu);
        jMenuBar.add(networkMenu);
        jMenuBar.add(Box.createHorizontalGlue());
        jMenuBar.add(helpMenu);
    }

//**********************************************************************************************************************

    private void setTheBounds(){

        puzzleLabel.setBounds(10,5,90,20);
        puzzleCombo.setBounds(10,25,90,20);
        countdownLabel.setBounds(110,5,90,20);
        countdownCombo.setBounds(110,25,90,20);
        startButton.setBounds(10,50,190,90);
        discardButton.setBounds(10,145,190,45);
        dnfButton.setBounds(10,195,90,45);
        plusTwoButton.setBounds(110,195,90,45);

        useThisAlgLabel.setBounds(215,5,333,20);
        scrambleText.setBounds(215,25,333,115);
        timerLabel.setBounds(215,145+12,333,75);
        timerArea.setBounds(215,145,333,75+21);

        scramblePanel.setBounds(563,5,282,235); // needs to be changed in two places

        // total width is 834 if there is a 10 margin on each side
        // so use formula: margin = (834-12*width-11*separation)/2 + 10
        // initial x value is left margine
        int x = 14;
        int width = 67;
        int seperation = 2;
        for(int i=0; i<12; i++){
            smartButton[i].setBounds(x, 250, width, 46);
            averageLabels[i].setBounds(x, 250, width, 20);
            averageLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            timeLabels[i].setBounds(x, 265, width, 20);
            timeLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            x += width + seperation;
        }

        sessionStatsLabel.setBounds(10,300,412,130);
        rollingAverageLabel.setBounds(432,300,412,130);
        bestAverageLabel.setBounds(10,435,834,72);
        bestAverageText.setBounds(20,452,724,45);
        averageDetailedViewButton.setBounds(754,452,80,45);
        sessionDetailedViewButton.setBounds(241,326,160,20);
        insertTimeButton.setBounds(241,351,160,20);
        sessionResetButton.setBounds(241,376,160,20);
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
        startButton.addActionListener(this);
        discardButton.addActionListener(this);
        dnfButton.addActionListener(this);
        plusTwoButton.addActionListener(this);
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
        contentPane.add(dnfButton);
        contentPane.add(plusTwoButton);
        contentPane.add(useThisAlgLabel);
        contentPane.add(scrambleText);
        contentPane.add(timerLabel);
        //contentPane.add(timerArea);
        contentPane.add(scramblePanel);
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
            if(startButton.getText().equals("Start Timer")) timerStart();
            else if(startButton.getText().equals("Stop Timer")) timerStop();
            else if(startButton.getText().equals("Accept Time")) timerAccept();
            returnFocus();
        } else if(source == discardButton){
            buttonsOn();
            updateScrambleAlgs();
            returnFocus();
        } else if(source == dnfButton){
            dnfButton.setText(RJT_Utils.makeRed("DNF"));
            acceptTime((stopTime-startTime)/1000D, true, false);
            returnFocus();
        } else if(source == plusTwoButton){
            acceptTime((stopTime-startTime)/1000D, false, true);
            returnFocus();
        } else if(source == sessionResetButton){
            if(optionsBox.showResetConfirmX){
                int choice = JOptionPane.showConfirmDialog(this,
                                                "Are you sure you want to reset this session and lose all times?",
                                                "Warning!",
                                                JOptionPane.YES_NO_OPTION,
                                                JOptionPane.QUESTION_MESSAGE);
                if(choice != JOptionPane.YES_OPTION){
                    returnFocus();
                    return;
                }
            }
            resetTheSession();
        } else if(source == puzzleCombo){
            String puzzle = puzzleCombo.getSelectedItem()+"";
            if(solveTable.getPuzzle().equals(puzzle)) return;

            solveTable.setPuzzle(puzzle);
            updateLabels(puzzle);

            if(solveTable.okayToDNF())
                dnfButton.setText("DNF");
            else
                dnfButton.setText(RJT_Utils.makeRed("DNF"));

            hasImported = false;
            importedIndex = 0;
            updateStats();
            buttonsOn();
            updateScrambleAlgs();
            returnFocus();
        } else if(source == countdownCombo){
            returnFocus();
        } else if(source == sessionDetailedViewButton){
            DetailedView win = new DetailedView(fc, "Session Times for " + solveTable.getPuzzle(), getSessionView(), optionsBox.textBackgrColorX);
            win.setVisible(true);
        } else if(source == averageDetailedViewButton){
            DetailedView win = new DetailedView(fc, "Best Average for " + solveTable.getPuzzle(), getAverageView(), optionsBox.textBackgrColorX);
            win.setVisible(true);
        } else if(source == insertTimeButton){
            String input = JOptionPane.showInputDialog(this, "Enter time to add in seconds or DNF:", "Insert New Time", JOptionPane.PLAIN_MESSAGE);
            if(input == null) return;
            acceptTime(input);
            insertTimeButton.requestFocus();
            return;
        } else if(source == saveBestItem){
            if(averageDetailsEnabled){
                int userChoice = fc.showSaveDialog(Standalone.this);
                if(userChoice == JFileChooser.APPROVE_OPTION)
                    RJT_Utils.saveToFile(this, getAverageView(), fc.getSelectedFile());
            } else {
                JOptionPane.showMessageDialog(this, "Not enough solves completed to calculate an average.");
            }
        } else if(source == saveSessionItem){
            if(sessionDetailsEnabled){
                int userChoice = fc.showSaveDialog(Standalone.this);
                if(userChoice == JFileChooser.APPROVE_OPTION)
                    RJT_Utils.saveToFile(this, getSessionView(), fc.getSelectedFile());
            } else {
                JOptionPane.showMessageDialog(this, "No times have been recorded for this session.");
            }
        } else if(source == optionsItem){
            optionsBox.setVisible(true);
        } else if(source == exitItem){
            System.exit(0);
        } else if(source == importItem){ // This section needs to be re-written, perhaps into RJT_Utils.java
            int userChoice = fc.showOpenDialog(Standalone.this);
            if(userChoice == JFileChooser.APPROVE_OPTION){
                String input = "";
                try{
                    FileReader fr = new FileReader(fc.getSelectedFile());
                    BufferedReader in = new BufferedReader(fr);
                    String read;
                    while((read = in.readLine()) != null)
                        input += read + "%";
                    in.close();
                } catch(IOException ex){
                    JOptionPane.showMessageDialog(this, "There was an error opening the file.");
                }
                StringTokenizer st = new StringTokenizer(input, "%");
                importedAlgs = new String[st.countTokens()];
                for(int i=0; i<importedAlgs.length; i++)
                    importedAlgs[i] = st.nextToken();
                hasImported = true;
                importedIndex = 0;
                updateScrambleAlgs();
            }
        } else if(source == generatorItem){
            scrambleGenerator.puzzleCombo.setSelectedItem(puzzleCombo.getSelectedItem()+"");
            scrambleGenerator.setVisible(true);
        } else if(source == serverItem){
            int choice = JOptionPane.showConfirmDialog(this, "Switching to Server Mode destroys main window session. Are you sure?", "Warning!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(choice != JOptionPane.YES_OPTION){
                returnFocus();
                return;
            }
            hideEverything();
            //Server server = new Server(/*puzzleCombo.getSelectedItem()+"", countdownCombo.getSelectedItem()+"",*/ optionsBox);
            server.puzzleCombo.setSelectedItem(puzzleCombo.getSelectedItem()+"");
            server.countdownCombo.setSelectedItem(countdownCombo.getSelectedItem()+"");
            //server.addVisiblityListener(this);
            optionsBox.addOptionsListener(server);
            server.setVisible(true);
            //disposeAll();
        } else if(source == clientItem){
            int choice = JOptionPane.showConfirmDialog(this, "Switching to Client Mode destroys main window session. Are you sure?", "Warning!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(choice != JOptionPane.YES_OPTION){
                returnFocus();
                return;
            }
            hideEverything();
            //Client client = new Client(optionsBox);
            //client.addVisiblityListener(this);
            optionsBox.addOptionsListener(client);
            client.setVisible(true);
            //disposeAll();
//        } else if(source == instItem){
//            instructionScreen.setVisible(true);
        } else if(source == aboutItem){
            aboutScreen.setVisible(true);
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
                    timerLabel.setForeground(optionsBox.timerColorX);
                    startTime = System.currentTimeMillis();
                } else if(countingDown == 3){
                    try{
                        countdownClip.play();
                    } catch(NullPointerException ex){}
                    timerLabel.setText(countingDown+"");
                    countingDown--;
                    try{
                        timerThread.sleep(1000);
                    } catch(InterruptedException ex){}
                } else {
                    timerLabel.setText(countingDown+"");
                    countingDown--;
                    try{
                        timerThread.sleep(1000);
                    } catch(InterruptedException ex){}
                }
            } else {
                double time = (System.currentTimeMillis() - startTime)/1000D;
                timerLabel.setText(RJT_Utils.timeToString(time, optionsBox.showMinutesX, true));
                try{
                    timerThread.sleep(120);
                } catch(InterruptedException ex){
                    System.out.println(ex.getMessage());
                }
            }
        }
    } // end run

//**********************************************************************************************************************
// Private Methods
//**********************************************************************************************************************

    private String getSessionView(){

        String formatedAverage = "N/A", formatedStdDev = "N/A";
        String timesAndScrambles = "none", timesOnly = "none";
        String formatedFastest = "N/A", formatedSlowest = "N/A";
        int numberSolved = 0, numberDNFed = 0;

        int size = solveTable.getSize();
        if(size > 0){
            SolveTable.Solve currentSolve = solveTable.getSolve(size-1);

            numberSolved = currentSolve.numberSolves;
            numberDNFed = currentSolve.numberDNFs;

            if(currentSolve.sessionAverage != INF)
                formatedAverage = RJT_Utils.timeToString(currentSolve.sessionAverage, optionsBox.showMinutesX, false, true);
            else
                formatedAverage = "DNF";

            if(currentSolve.sessionStdDev != INF)
                formatedStdDev = RJT_Utils.timeToString(currentSolve.sessionStdDev, optionsBox.showMinutesX, false, false);
            else
                formatedStdDev = "???";

            solveTable.setTimeStyle(optionsBox.showMinutesX, false, false);
            timesAndScrambles = ""; timesOnly = "";
            for(int i=0; i<size; i++){
                String s = solveTable.getString(i);
                timesAndScrambles += (i+1) + ")          " + s + "          " + solveTable.getScramble(i) + "\n";
                timesOnly += s + "\n";
            }

            //solveTable.setTimeStyle(optionsBox.showMinutesX, false, false);
            formatedFastest = solveTable.getString(currentSolve.sessionFastestIndex);
            formatedSlowest = solveTable.getString(currentSolve.sessionSlowestIndex);
        }

        String returnMe = optionsBox.sessionViewFormatX;
        returnMe = returnMe.replaceAll("%T", new Date()+"");
        returnMe = returnMe.replaceAll("%C", numberSolved+"");
        returnMe = returnMe.replaceAll("%P", numberDNFed+"");
        returnMe = returnMe.replaceAll("%A", formatedAverage);
        returnMe = returnMe.replaceAll("%D", formatedStdDev);
        returnMe = returnMe.replaceAll("%I", timesAndScrambles.trim());
        returnMe = returnMe.replaceAll("%O", timesOnly.trim());
        returnMe = returnMe.replaceAll("%F", formatedFastest);
        returnMe = returnMe.replaceAll("%S", formatedSlowest);
        returnMe = returnMe.replaceAll("%Z", solveTable.getPuzzle());
        returnMe = returnMe.replaceAll("\n", System.getProperty("line.separator"));
        return returnMe;
    } // end getSessionView

//**********************************************************************************************************************

    private String getAverageView(){

        String formatedAverage = "N/A", formatedStdDev = "N/A";
        String timesAndScrambles = "none", timesOnly = "none";
        String formatedFastest = "N/A", formatedSlowest = "N/A";

        int bestIndex = solveTable.findBestRolling();
        if(bestIndex != -1){
            SolveTable.Solve bestSolve = solveTable.getSolve(bestIndex);

            formatedAverage = RJT_Utils.timeToString(bestSolve.rollingAverage, optionsBox.showMinutesX, false, true);
            formatedStdDev = RJT_Utils.timeToString(bestSolve.rollingStdDev, optionsBox.showMinutesX, false, false);

            solveTable.setTimeStyle(optionsBox.showMinutesX, false, false);
            timesAndScrambles = ""; timesOnly = "";
            for(int i=1; i<=12; i++){
                int index = i+bestIndex-12;
                String s = solveTable.getString(index);
                if(index == bestSolve.rollingFastestIndex || index == bestSolve.rollingSlowestIndex)
                    s = "(" + s + ")";
                timesAndScrambles += (index+1) + ")          " + s + "          " + solveTable.getScramble(index) + "\n";
                timesOnly += s + "\n";
            }

            //solveTable.setTimeStyle(optionsBox.showMinutesX, false, false);
            formatedFastest = solveTable.getString(bestSolve.rollingFastestIndex);
            formatedSlowest = solveTable.getString(bestSolve.rollingSlowestIndex);
        }

        String returnMe = optionsBox.averageViewFormatX;
        returnMe = returnMe.replaceAll("%T", new Date()+"");
        returnMe = returnMe.replaceAll("%A", formatedAverage);
        returnMe = returnMe.replaceAll("%D", formatedStdDev);
        returnMe = returnMe.replaceAll("%I", timesAndScrambles.trim());
        returnMe = returnMe.replaceAll("%O", timesOnly.trim());
        returnMe = returnMe.replaceAll("%F", formatedFastest);
        returnMe = returnMe.replaceAll("%S", formatedSlowest);
        returnMe = returnMe.replaceAll("%Z", solveTable.getPuzzle());
        returnMe = returnMe.replaceAll("\n", System.getProperty("line.separator"));
        return returnMe;
    } // end getAverageView

//**********************************************************************************************************************

    private void updateScrambleAlgs(){
        String puzzle = puzzleCombo.getSelectedItem()+"";
        scrambleText.setFont(puzzle.equals("Megaminx") ? smAlgFont : lgAlgFont);
        if(hasImported && (importedIndex < importedAlgs.length)){
            newAlg = importedAlgs[importedIndex];
            importedIndex++;
        }
        else{
            if(hasImported){
                JOptionPane.showMessageDialog(this, "All imported scrambles have been used. Random scrambles will now be displayed.");
                hasImported = false;
            }
            newAlg = scrambleAlg.generateAlg(puzzle);
        }
        scrambleText.setText(newAlg.replaceAll(ALG_BREAK, "\n"));
        updateScramblePanel();
    } // end updateScrambleAlgs

//**********************************************************************************************************************

    private void updateScramblePanel(){
        scramblePanel.newScramble(puzzleCombo.getSelectedItem()+"", newAlg.replaceAll(ALG_BREAK, " "));
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    public void timerStart(){
        startButton.setText("Stop Timer");
        runningCountdown = true;
        countingDown = Integer.parseInt(countdownCombo.getSelectedItem()+"");
        timerLabel.setForeground(optionsBox.countdownColorX);
        //timerLabel.setVisible(true);

        puzzleCombo.setEnabled(false);
        countdownCombo.setEnabled(false);
        sessionDetailedViewButton.setEnabled(false);
        insertTimeButton.setEnabled(false);
        sessionResetButton.setEnabled(false);
        averageDetailedViewButton.setEnabled(false);

        timerThread = new Thread(this);
        timerThread.start();
    }

//**********************************************************************************************************************

    public void timerStop(){
        if(runningCountdown){
            timerThread = null;
            countdownClip.stop();
            buttonsOn();
        } else {
            stopTime = System.currentTimeMillis();
            timerThread = null;
            double time = (stopTime-startTime)/1000D;
            timerLabel.setText(RJT_Utils.timeToString(time, optionsBox.showMinutesX, true));
            startButton.setText("Accept Time");
            if(solveTable.okayToDNF())
                dnfButton.setText("DNF");
            else
                dnfButton.setText(RJT_Utils.makeRed("DNF"));
            dnfButton.setEnabled(true);
            discardButton.setEnabled(true);
            plusTwoButton.setEnabled(true);
        }
    }

//**********************************************************************************************************************
    public void timerAccept(){
        acceptTime((stopTime-startTime)/1000D);
    }

//**********************************************************************************************************************

    private void returnFocus(){ // debug: just to have it change one place while we try new stuff
        startButton.requestFocus();
        //timerArea.requestFocus();
    }

//**********************************************************************************************************************
/*
    private void disposeAll(){
        optionsBox.dispose();
        scrambleGenerator.dispose();
        instructionScreen.dispose();
        aboutScreen.dispose();
        this.dispose();
    } // end disposeAll
*/
//**********************************************************************************************************************

    private void hideEverything(){
        optionsBox.setVisible(false);
        scrambleGenerator.setVisible(false);
        instructionScreen.setVisible(false);
        aboutScreen.setVisible(false);
        this.setVisible(false);
    } // end hideEverything

//**********************************************************************************************************************

    private void updateGUI(){
        scramblePanel.setCubeColors(optionsBox.cubeColorsX);
        scramblePanel.setPyraminxColors(optionsBox.pyraminxColorsX);
        scramblePanel.setMegaminxColors(optionsBox.megaminxColorsX);
        scramblePanel.updateScreen();
        scrambleText.setBackground(optionsBox.textBackgrColorX);
        bestAverageText.setBackground(optionsBox.textBackgrColorX);
    } // end OptionsToGUI

//**********************************************************************************************************************

    // for OptionsListener interface
    public void optionsCallback(){
        updateGUI();
        updateStats();
    }

//**********************************************************************************************************************

    // for VisiblityListener interface
    public void netmodeCallback(){
        optionsBox.addOptionsListener(this); // pass it this so that it can update GUI when needed
        this.setVisible(true);
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private void acceptTime(double time){
        acceptTime(time, false, false);
    } // end acceptTime

    private void acceptTime(double time, boolean isDNF, boolean isPlus2){// throws NumberFormatException{
        if(time < 0){
            JOptionPane.showMessageDialog(this, "Negative time entered." + NOT_ADDED);
            return;
        }
        solveTable.addSolve(time, newAlg, isDNF, isPlus2);
        updateStats();
        buttonsOn();
        updateScrambleAlgs();
    } // end acceptTime

    private void acceptTime(String input){
        input = input.trim();
        if(input.equals("")){
            JOptionPane.showMessageDialog(this, "Nothing entered." + NOT_ADDED);
            return;
        }

        int min = 0;
        double sec = 0;
        boolean isDNF = false;
        boolean isPlus2 = false;

        if(input.equalsIgnoreCase("DNF")){
            isDNF = true;
        }
        else{
            if(input.endsWith("+")){
                input = input.substring(0, input.length()-1);
                isPlus2 = true;
            }
            String parts[] = input.split(":");
            if(parts.length > 2){
                JOptionPane.showMessageDialog(this, "Too many colons." + NOT_ADDED);
                return;
            }
            else{
                try{
                    if(parts.length == 1){
                        sec = Double.parseDouble(parts[0]);
                    }
                    else if(parts.length == 2){
                        min = Integer.valueOf(parts[0]);
                        sec = Double.parseDouble(parts[1]);
                    }
                } catch(NumberFormatException ex){
                    JOptionPane.showMessageDialog(this, "Invalid number entered." + NOT_ADDED);
                    return;
                }
            }
        }
        if(isPlus2)
            acceptTime(60*min + sec - 2, isDNF, isPlus2);
        else
            acceptTime(60*min + sec, isDNF, isPlus2);

    } // end acceptTime

//**********************************************************************************************************************

    private void buttonsOn(){
        puzzleCombo.setEnabled(true);
        countdownCombo.setEnabled(true);
        startButton.setText("Start Timer");
        discardButton.setEnabled(false);
        dnfButton.setEnabled(false);
        plusTwoButton.setEnabled(false);

        sessionDetailedViewButton.setEnabled(sessionDetailsEnabled);
        insertTimeButton.setEnabled(true);
        sessionResetButton.setEnabled(true);

        averageDetailedViewButton.setEnabled(averageDetailsEnabled);
        timerLabel.setText("");
        //timerLabel.setVisible(false);

    } // end buttonsOn

//**********************************************************************************************************************

    private void resetTheSession(){
        dnfButton.setText("DNF");
        hasImported = false;
        importedIndex = 0;

        solveTable.sessionReset();
        updateStats();
        buttonsOn();
        updateScrambleAlgs();
        returnFocus();
    } // end resetTheSession

//**********************************************************************************************************************

    private void updateStats(){
        int size = solveTable.getSize();
        final String BLACK = "#000000", RED = "#FF0000", BLUE = "#0000FF", TIE = "FF8000";

        String sRollingAverage = "N/A";
        String sRollingAverageColor = BLACK;
        String sRollingProgress = "N/A";
        String sRollingProgressColor = BLACK;
        String sRollingFastest = "N/A";
        String sRollingSlowest = "N/A";
        String sRollingStdDev = "N/A";
        String sRollingStdDevColor = BLACK;

        String sBestAverage = "N/A";
        String sBestIndvTimes = "N/A";

        String sRecentTime = "N/A";
        String sPrevTime = "N/A";
        String sProgressColor = BLACK;
        String sProgress = "N/A";
        int numberSolved = 0;
        String sSessionAverage = "N/A";


        boolean showMin = optionsBox.showMinutesX;
        solveTable.setTimeStyle(showMin, true, false);
        if(size < 12){
            for(int i=0; i<12; i++){
                String s;
                averageLabels[i].setText("#" + (i+1));
                if(i<size){
                    s = solveTable.getString(i);

                    double fastestTime = solveTable.getSolve(size-1).sessionFastestTime;
                    double slowestTime = solveTable.getSolve(size-1).sessionSlowestTime;
                    if(fastestTime == slowestTime) timeLabels[i].setForeground(Color.black);
                    else if(solveTable.getTime(i) == fastestTime) timeLabels[i].setForeground(optionsBox.fastestColorX);
                    else if(solveTable.getTime(i) == slowestTime) timeLabels[i].setForeground(optionsBox.slowestColorX);
                    else timeLabels[i].setForeground(Color.black);
                }
                else{
                    s = "none";
                    timeLabels[i].setForeground(Color.black);
                }
                timeLabels[i].setText("<html><font size=\"5\">" + s + "</font></html>");
            }
        }
        else{
            for(int i=0; i<12; i++){
                int index = size+i-12;
                averageLabels[(size+i)%12].setText("#" + (index+1));
                String s = solveTable.getString(index);
                timeLabels[(size+i)%12].setText("<html><font size=\"5\">" + s + "</font></html>");

                double fastestTime = solveTable.getSolve(size-1).rollingFastestTime;
                double slowestTime = solveTable.getSolve(size-1).rollingSlowestTime;
                if(fastestTime == slowestTime) timeLabels[(size+i)%12].setForeground(Color.black);
                else if(solveTable.getTime(index) == fastestTime) timeLabels[(size+i)%12].setForeground(optionsBox.fastestColorX);
                else if(solveTable.getTime(index) == slowestTime) timeLabels[(size+i)%12].setForeground(optionsBox.slowestColorX);
                else timeLabels[(size+i)%12].setForeground(Color.black);
            }
        }
        timeLabels[size%12].setForeground(optionsBox.currentColorX);


        if(size > 0){
            SolveTable.Solve currentSolve = solveTable.getSolve(size-1);

            if(size >= 12){
                if(currentSolve.rollingAverage != INF){
                    sRollingAverage = RJT_Utils.timeToString(currentSolve.rollingAverage, showMin, false, true);
                    sRollingStdDev = RJT_Utils.timeToString(currentSolve.rollingStdDev, showMin, false, false);
                }
                else{
                    sRollingAverage = "DNF"; sRollingAverageColor = RED;
                    sRollingStdDev = "???"; sRollingStdDevColor = RED;
                }

                if(size > 12){
                    SolveTable.Solve prevSolve = solveTable.getSolve(size-2);
                    if(currentSolve.rollingAverage != INF && prevSolve.rollingAverage != INF){
                        double time = currentSolve.rollingAverage - prevSolve.rollingAverage;
                        sRollingProgress = RJT_Utils.timeToString(time , showMin, false, true);
                        if(currentSolve.rollingAverage > prevSolve.rollingAverage) sRollingProgressColor = RED;
                        if(currentSolve.rollingAverage < prevSolve.rollingAverage) sRollingProgressColor = BLUE;
                        if(currentSolve.rollingAverage == prevSolve.rollingAverage) sRollingProgressColor = TIE;
                    }
                    if(currentSolve.rollingAverage != INF && prevSolve.rollingAverage == INF){
                        sRollingProgress = "+inf.";
                        sRollingProgressColor = RED;
                    }
                    if(currentSolve.rollingAverage == INF && prevSolve.rollingAverage != INF){
                        sRollingProgress = "-inf.";
                        sRollingProgressColor = BLUE;
                    }
                    if(currentSolve.rollingAverage == INF && prevSolve.rollingAverage == INF){
                        sRollingProgress = "???";
                        sRollingProgressColor = TIE;
                    }
                }
                solveTable.setTimeStyle(showMin, false, false);
                sRollingFastest = solveTable.getString(currentSolve.rollingFastestIndex);
                sRollingSlowest = solveTable.getString(currentSolve.rollingSlowestIndex);
            }


            int bestIndex = solveTable.findBestRolling();
            if(bestIndex != -1){
                SolveTable.Solve bestSolve = solveTable.getSolve(bestIndex);

                sBestAverage = RJT_Utils.timeToString(bestSolve.rollingAverage, showMin, false, true);
                solveTable.setTimeStyle(showMin, false, false);
                sBestIndvTimes = "";
                for(int i=1; i<=12; i++){
                    int index = i+bestIndex-12;
                    String s = solveTable.getString(index);
                    if(index == bestSolve.rollingFastestIndex || index == bestSolve.rollingSlowestIndex)
                        s = "(" + s + ")";
                    sBestIndvTimes += s + ", ";
                }
                sBestIndvTimes = sBestIndvTimes.substring(0, sBestIndvTimes.length()-2);
            }

            solveTable.setTimeStyle(showMin, false, false);
            sRecentTime = solveTable.getString(size-1);
            if(size > 1){
                sPrevTime = solveTable.getString(size-2);
                double recentTime = solveTable.getTime(size-1), prevTime = solveTable.getTime(size-2);
                if(recentTime != INF && prevTime != INF){
                    sProgress = RJT_Utils.timeToString(recentTime-prevTime, showMin, false, true);
                    if(recentTime > prevTime) sProgressColor = RED;
                    if(recentTime < prevTime) sProgressColor = BLUE;
                    if(recentTime == prevTime) sProgressColor = TIE;
                }
                if(recentTime != INF && prevTime == INF){
                    sProgress = "+inf.";
                    sProgressColor = RED;
                }
                if(recentTime == INF && prevTime != INF){
                    sProgress = "-inf.";
                    sProgressColor = BLUE;
                }
                if(recentTime == INF && prevTime == INF){
                    sProgress = "???";
                    sProgressColor = TIE;
                }
            }
            numberSolved = currentSolve.numberSolves;
            if(currentSolve.sessionAverage != INF)
                sSessionAverage = RJT_Utils.timeToString(currentSolve.sessionAverage, showMin, false, true);
            else
                sSessionAverage = "DNF";
        }


        rollingAverageLabel.setText("<html>Current Average: <font size=\"5\" color=\"" + sRollingAverageColor + "\">" + sRollingAverage + "</font><br>Progress: <font color=\"" + sRollingProgressColor + "\">" + sRollingProgress +"</font><br><br>Fastest Time: " + sRollingFastest + "<br>Slowest Time: " + sRollingSlowest + "<br>Standard Deviation: <font color=\"" + sRollingStdDevColor + "\">" + sRollingStdDev + "</font></html>");

        bestAverageText.setText("Average: " + sBestAverage + "\nIndividual Times: " + sBestIndvTimes);

        sessionStatsLabel.setText("<html>Recent Time: " + sRecentTime + "<br>Previous Time: " + sPrevTime + "<br>Progress: <font color=\"" + sProgressColor + "\">" + sProgress + "</font><br><br>Total Solves: " + numberSolved + "<br>Session Average: " + sSessionAverage + "</html>");

        sessionDetailsEnabled = (size > 0);
        averageDetailsEnabled = (sBestAverage != "N/A");

    } // end updateStats

//**********************************************************************************************************************

    private void updateLabels(String puzzle){
        useThisAlgLabel.setText("Use this " + puzzle + " Scramble Algorithm:");
        scramblePanel.setBorder(BorderFactory.createTitledBorder(theBorder, puzzle + " Scramble View"));
        sessionStatsLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Session Statistics for " + puzzle));
        rollingAverageLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Rolling Average for " + puzzle));
        bestAverageLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Best Average for " + puzzle));
    }

}
