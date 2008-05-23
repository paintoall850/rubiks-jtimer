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
    //String[] timeString;
    SmartButton[] smartButton;

    private boolean averageOfFiveMode; // experimental

    volatile Thread timerThread;
    AudioClip countdownClip = null;

    //int placeInAverage = 0;
    //float sessionTotalTime = 0, sessionFastest = 0, sessionSlowest = 0;
    //float bestAverage = 0, bestStandardDeviation = 0, bestFastest = 0, bestSlowest = 0, previousAverage = 0;
    //int countingDown = 0, cubesSolved = 0, sessionIndex = 0, acceptsSincePop = 12, numberOfPops = 0;
    //String[] bestAverageTimes = new String[12], bestAverageScrambles = new String[12];
    //String[] currentAverageScrambles = new String[12], currentAverageTimes = new String[12];
    //String[] sessionTimes = new String[100], sessionScrambles = new String[100];

    // expreimental
    //Hashtable<String, Vector<Solve>> recordTable = new Hashtable<String, Vector<Solve>>(10);
    SolveTable solveTable;
    private boolean sessionDetailsEnabled, averageDetailsEnabled;
    private int acceptsSincePop;
    private boolean runningCountdown;
    private int countingDown;
    private long startTime, stopTime;

    DecimalFormat ssxx, ss;
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
        } catch(Exception e){}

        // create this frame //and show it
        Standalone standalone = new Standalone();
        //standalone.setVisible(true);
        //startButton.requestFocus();
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
//        discardButton.setEnabled(false);
        popButton = new JButton();//"POP");
//        popButton.setEnabled(false);
        plusTwoButton = new JButton("+2");
//        plusTwoButton.setEnabled(false);
        averageModeButton = new JButton("Average of 5 Mode");

        averageLabels = new JLabel[12];
        for(int i=0; i<12; i++)
            averageLabels[i] = new JLabel();//"#" + (i+1));

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

        timerLabel = new JLabel();
        //timerLabel.setText(""); //"00.00"
        //timerLabel.setVisible(false);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setFont(new Font("Serif", Font.PLAIN, 94));

        timerArea = new TimerArea(this); // kinda dangerous but this is going to be how we invoke the timerStart() and stuff

        scramblePane = new ScramblePane(282, 216+20); // needs to be changed in two places
        scramblePane.setBorder(BorderFactory.createTitledBorder(theBorder, "Scramble View"));
        scramblePane.setLayout(null);


        sessionStatsLabel = new JLabel();//"<html>Recent Time: N/A<br>Previous Time: N/A<br>Progress: N/A<br><br>Cubes Solved: 0<br>Session Average: N/A</html>");
        sessionStatsLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Session Statistics"));
        rollingAverageLabel = new JLabel();//"<html>Current Average: <font size=\"5\">N/A</font><br>Progress: N/A<br><br>Fastest Time: N/A<br>Slowest Time: N/A<br>Standard Deviation: N/A</html>");
        rollingAverageLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Rolling Average"));
        bestAverageLabel = new JLabel();
        bestAverageLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Best Average"));

        bestAverageText = new JTextArea();//"Average: N/A\nIndividual Times: N/A");
        bestAverageText.setFont(regFont);
        bestAverageText.setBorder(blackLine);
        bestAverageText.setEditable(false);

        insertTimeButton = new JButton ("Insert Own Time");
        sessionDetailedViewButton = new JButton("Session Details");
//        sessionDetailedViewButton.setEnabled(false);
        averageDetailedViewButton = new JButton("Details");
//        averageDetailedViewButton.setEnabled(false);
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

        //timeString = new String[12];
        timeLabels = new JLabel[12];
        for(int i=0; i<12; i++){
        //    timeString[i] = "none";
            timeLabels[i] = new JLabel();//"<html><font size=\"5\">" + timeString[i] + "</font></html>");
        }


        optionsMenu.loadOptions(); // inital load of options
System.err.print("Before: " + "\n");
        solveTable = new SolveTable(optionsMenu.puzzleX); // experimental
System.err.print("After: " + solveTable.getPuzzle() + "\n");

        if(!optionsMenu.puzzleX.equals(puzzleCombo.getSelectedItem()+"")) // less glitchier
            puzzleCombo.setSelectedItem(optionsMenu.puzzleX);
//System.err.print("fdsafsd: " + "" + "\n");
        if(!optionsMenu.countdownX.equals(countdownCombo.getSelectedItem()+"")) // less glitchier
            countdownCombo.setSelectedItem(optionsMenu.countdownX);
//System.err.print("waefawe: " + "" + "\n");
        updateGUI();
//System.err.print("awefawfewa: " + "" + "\n");
        resetTheSession();

        // set bounds
        setTheBounds();
        // add to contentPane
        addTheContent(contentPane);
        // add ActionListeners
        addTheActionListeners();

/*
        sessionDetailsEnabled = false; averageDetailsEnabled = false;
        acceptsSincePop = 12;
        updateStatsX();//@@@
        buttonsOn();
*/
        runningCountdown = false;
        countingDown = 0;
        startTime = 0;
        stopTime = 0;
System.err.print("we're here." + "\n");
/*
        // set some stuff up
        importedIndex = 0;
        hasImported = false;
        updateScrambleAlgs();
        //timeLabels[0].setForeground(optionsMenu.currentColorX);
//        returnFocus();
*/
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
        startButton.requestFocus();

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
        averageModeButton.setBounds(663,326,160,20);
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
            if(startButton.getText().equals("Start Timer")) timerStart();
            else if(startButton.getText().equals("Stop Timer")) timerStop();
            else if(startButton.getText().equals("Accept Time")) timerAccept();
        } else if(source == discardButton){
            buttonsOn();
            updateScrambleAlgs();
/*            if(sessionDetailsEnabled)
                sessionDetailedViewButton.setEnabled(true);
            if(averageDetailsEnabled)
                averageDetailedViewButton.setEnabled(true);
            insertTimeButton.setEnabled(true);
            sessionResetButton.setEnabled(true);
            discardButton.setEnabled(false);
            popButton.setEnabled(false);
            plusTwoButton.setEnabled(false);
            puzzleCombo.setEnabled(true);
            countdownCombo.setEnabled(true);
            startButton.setText("Start Timer");
            timerLabel.setText("");
            //timerLabel.setVisible(false);
            updateScrambleAlgs();
            returnFocus();*/
        } else if(source == popButton){
            acceptsSincePop = 0;
            //numberOfPops++;
            popButton.setText("Popped!");
            acceptTimeX(stopTime-startTime, true, false);
/*
            if(sessionIndex > sessionTimes.length-1){
                String[] temp = new String[sessionTimes.length*2];
                String[] temp2 = new String[sessionTimes.length*2];
                for(int i=0; i<sessionTimes.length; i++){
                    temp[i] = sessionTimes[i];
                    temp2[i] = sessionScrambles[i];
                }
                sessionTimes = temp;
                sessionScrambles = temp2;
            }*/
            //sessionTimes[sessionIndex] = "POP";
            //sessionScrambles[sessionIndex] = newAlg;//scrambleText.getText();
            //sessionIndex++;
/*
            if(cubesSolved >= 12)
                averageDetailedViewButton.setEnabled(true);
            insertTimeButton.setEnabled(true);
            sessionDetailedViewButton.setEnabled(true);
            sessionResetButton.setEnabled(true);

            puzzleCombo.setEnabled(true);
            countdownCombo.setEnabled(true);
            startButton.setText("Start Timer");
            discardButton.setEnabled(false);
            popButton.setEnabled(false);
            plusTwoButton.setEnabled(false);

            timerLabel.setText("");
            //timerLabel.setVisible(false);
            updateScrambleAlgs();
            returnFocus();
*/
        } else if(source == plusTwoButton){
            //stopTime += 1200000; // TEMP: was 2000
            //cubesSolved++;
            try{
                acceptTimeX(stopTime-startTime, false, true);//acceptTime();
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
            resetTheSession();
        } else if(source == puzzleCombo){
System.err.print("combo box: " + puzzleCombo.getSelectedItem() + "\n");
            if(solveTable.getPuzzle().equals(puzzleCombo.getSelectedItem()+"")){
System.err.print("not good: " + solveTable.getPuzzle() + "\n");
                return;
            }
            solveTable.setPuzzle(puzzleCombo.getSelectedItem()+"");
popButton.setText("POP");
//sessionDetailsEnabled = false;
//averageDetailsEnabled = false;
acceptsSincePop = 12;
hasImported = false;
importedIndex = 0;

            updateStatsX();
            buttonsOn();
            updateScrambleAlgs();
            //returnFocus();
        } else if(source == countdownCombo){
            returnFocus();
        } else if(source == sessionDetailedViewButton){
            //DetailedView win = new DetailedView("Session Times", getSessionView(), optionsMenu.textBackgrColorX);
            //win.setVisible(true);
        } else if(source == averageDetailedViewButton){
            //DetailedView win = new DetailedView("Best Average", getAverageView(), optionsMenu.textBackgrColorX);
            //win.setVisible(true);
        } else if(source == saveSessionItem){
/*            if(cubesSolved >= 1){
                int userChoice = fc.showSaveDialog(Standalone.this);
                if(userChoice == JFileChooser.APPROVE_OPTION)
                    saveToFile(getSessionView(), fc.getSelectedFile());
            } else {
                JOptionPane.showMessageDialog(this, "No times have been recorded for this session.");
            }*/
        } else if(source == saveBestItem){
/*            if(cubesSolved >= 12){
                int userChoice = fc.showSaveDialog(Standalone.this);
                if(userChoice == JFileChooser.APPROVE_OPTION)
                    saveToFile(getAverageView(), fc.getSelectedFile());
            } else {
                JOptionPane.showMessageDialog(this, "Not enough cubes have been solved to calculate an average.");
            }*/
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
                    while((read = in.readLine()) != null)
                        input += read + "%";
                    in.close();
                } catch(IOException g){JOptionPane.showMessageDialog(this, "There was an error opening the file.");}
                StringTokenizer st = new StringTokenizer(input, "%");
                importedAlgs = new String[st.countTokens()];
                for(int i=0; i<importedAlgs.length; i++)
                    importedAlgs[i] = st.nextToken();
                hasImported = true;
                importedIndex = 0;
                updateScrambleAlgs();
            }
        } else if(source == insertTimeButton){
            String input = JOptionPane.showInputDialog(this, "Enter time to add in seconds or POP:");
            if(input == null) return;
            if(input.equalsIgnoreCase("POP")){
                //numberOfPops++;
/*                if(sessionIndex > sessionTimes.length-1){
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
*/              
                acceptTimeX(0, true, false);
//                updateScrambleAlgs();
//                sessionDetailedViewButton.setEnabled(true);
                return;
            }
            float inputTime = 0;
            try{
                inputTime = Float.parseFloat(ssxx.format(Float.parseFloat(input)));
            } catch(NumberFormatException h){
                JOptionPane.showMessageDialog(this, "Invalid number entered. No time was added to the session.");
                return;
            }
            //startTime = 0;
            //stopTime = Math.round(inputTime * 1000);
            //cubesSolved++;
            try{
                acceptTimeX(Math.round(inputTime * 1000));//acceptTime();
            } catch(NumberFormatException v){
                JOptionPane.showMessageDialog(this, "There has been an error, please inform Chris that you saw this message.");
                System.out.println(v);
            }
            insertTimeButton.requestFocus();

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
                float time = (System.currentTimeMillis() - startTime)/1000F;
                timerLabel.setText(timeToString(time, true));
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
/*
    private void acceptTime() throws NumberFormatException{
        float time = (stopTime-startTime)/1000F;
        timeString[placeInAverage] = timeToString(time, true);
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

        sessionTotalTime += Float.parseFloat(ssxx.format(time));
        sessionTimes[sessionIndex] = ssxx.format(time);
        sessionScrambles[sessionIndex] = newAlg;//scrambleText.getText();
        currentAverageTimes[placeInAverage] = ssxx.format(time);
        currentAverageScrambles[placeInAverage] = newAlg;//scrambleText.getText();

        if(Float.parseFloat(sessionTimes[sessionIndex]) < sessionFastest || Float.parseFloat(sessionTimes[sessionIndex]) > sessionSlowest || sessionFastest == 0 || sessionSlowest == 0){
            if(sessionFastest == 0 || sessionSlowest == 0){
                sessionFastest = Float.parseFloat(sessionTimes[sessionIndex]);
                sessionSlowest = Float.parseFloat(sessionTimes[sessionIndex]);
            } else if(Float.parseFloat(sessionTimes[sessionIndex]) < sessionFastest){
                sessionFastest = Float.parseFloat(sessionTimes[sessionIndex]);
            } else if(Float.parseFloat(sessionTimes[sessionIndex]) > sessionSlowest){
                sessionSlowest = Float.parseFloat(sessionTimes[sessionIndex]);
            }
        }

        for(int i=0; i<12; i++)
            timeLabels[i].setForeground(Color.black);

        if(cubesSolved >= 12){
            int fastest = 0;
            int slowest = 0;

            for(int i=0; i<12; i++){
                if(Float.parseFloat(currentAverageTimes[i]) < Float.parseFloat(currentAverageTimes[fastest]))
                    fastest = i;
                else if(Float.parseFloat(currentAverageTimes[i]) > Float.parseFloat(currentAverageTimes[slowest]))
                    slowest = i;
            }

            timeLabels[fastest].setForeground(optionsMenu.fastestColorX);
            timeLabels[slowest].setForeground(optionsMenu.slowestColorX);

            float average = 0;
            for(int i=0; i<12; i++)
                if(i!=fastest && i!=slowest)
                    average += Float.parseFloat(currentAverageTimes[i]);
            average /= 10;

            float standardDeviation = 0;
            for(int i=0; i<12; i++)
                if(i!=fastest && i!=slowest)
                    standardDeviation += (average-Float.parseFloat(currentAverageTimes[i])) * (average-Float.parseFloat(currentAverageTimes[i]));
            standardDeviation = (float)Math.sqrt(standardDeviation/9);

            String progressColor, progress;
            progress = ssxx.format(average-previousAverage);
            try{
                if(Float.parseFloat(progress) > 0){
                    progressColor = "#FF0000";
                } else if(Float.parseFloat(progress) < 0){
                    progressColor = "#0000FF";
                } else {
                    progressColor = "#000000";
                    progress = "00.00";
                }
            } catch(NumberFormatException e){
                    progressColor = "#000000";
            }
            String printedAverage = timeToString(average, false, true);
            rollingAverageLabel.setText("<html>Current Average: <font size=\"5\">" + printedAverage + "</font><br>Progress: <font color=\"" + progressColor + "\">" + progress +" sec.</font><br><br>Fastest Time: " + timeString[fastest] + "<br>Slowest Time: " + timeString[slowest] + "<br>Standard Deviation: " + ssxx.format(standardDeviation) + "</html>");
            previousAverage = average;

            if(average<bestAverage || bestAverage==0){
                bestAverage = average;
                String bestAverageFormated = timeToString(bestAverage, false, true);
                bestFastest = Float.parseFloat(currentAverageTimes[fastest]);
                bestSlowest = Float.parseFloat(currentAverageTimes[slowest]);
                bestStandardDeviation = standardDeviation;

                //copy times and scrambles
                String[] temp = new String[12];
                String[] temp2 = new String[12];

                for(int i=0; i<12; i++){
                    if(Float.parseFloat(currentAverageTimes[i])>=60 && optionsMenu.showMinutesX){
                        int min = (int)(Float.parseFloat(currentAverageTimes[i])/60);
                        temp[i] = min + ":" + ssxx.format(Float.parseFloat(currentAverageTimes[i]) - min*60);
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
                progress = ssxx.format(Float.parseFloat(currentAverageTimes[placeInAverage]) - Float.parseFloat(currentAverageTimes[11]));
            } else {
                previousTime = timeString[placeInAverage-1];
                progress = ssxx.format(Float.parseFloat(currentAverageTimes[placeInAverage]) - Float.parseFloat(currentAverageTimes[placeInAverage-1]));
            }
        } else {
            previousTime = "N/A";
            progress = "N/A";
        }

        String sessionAverage = timeToString(sessionTotalTime/cubesSolved, false, true);

        String progressColor;
        try{
            if(Float.parseFloat(progress) > 0){
                progressColor = "#FF0000";
                progress = progress + " sec.";
            } else if(Float.parseFloat(progress) < 0){
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
*/
//**********************************************************************************************************************
/*
    private String getSessionView(){
        String timesAndScrambles = "", timesOnly = "";
        float deviation = 0, average = 0;
        if(cubesSolved >= 1)
            average = sessionTotalTime/cubesSolved;

        for(int i=0; i<sessionIndex; i++){
            String currentTime = "POP";
            if(!sessionTimes[i].equals("POP"))
                currentTime = timeToString(Float.parseFloat(sessionTimes[i]), false);
            timesAndScrambles = timesAndScrambles + (i+1) + ")          " + currentTime + "          " + sessionScrambles[i] + "\n";
            timesOnly += currentTime + "\n";
            if(cubesSolved >= 2){
                if(!(sessionTimes[i].equals("POP")))
                    deviation += (average-Float.parseFloat(sessionTimes[i])) * (average-Float.parseFloat(sessionTimes[i]));
            }
        }
        if(cubesSolved >= 2)
            deviation = (float)Math.sqrt(deviation/(cubesSolved-1));

        String formatedAverage = timeToString(average, false);
        String formatedFastest = timeToString(sessionFastest, false);
        String formatedSlowest = timeToString(sessionSlowest, false);

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
*/
//**********************************************************************************************************************
/*
    private String getAverageView(){
        String timesAndScrambles = "", timesOnly = "";

        for(int i=0; i<12; i++){
            timesAndScrambles += (i+1) + ")          " + bestAverageTimes[i] + "          " + bestAverageScrambles[i] + "\n";
            timesOnly += bestAverageTimes[i] + "\n";
        }

        String formatedAverage = timeToString(bestAverage, false);
        String formatedFastest = timeToString(bestFastest, false);
        String formatedSlowest = timeToString(bestSlowest, false);

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
*/
//**********************************************************************************************************************
    private final String timeToString(float time, boolean truncate){
        return timeToString(time, truncate, false);
    }

    private final String timeToString(float time, boolean truncate, boolean verbose){
        String s = ssxx.format(time); // consider the case time=59.999D
        time = Float.parseFloat(s);
        if(time>=60 && optionsMenu.showMinutesX){
            int min = (int)(time/60);
            float sec = time-min*60;
            s = min + ":" + ((time < 600 || !truncate) ? ssxx.format(sec) : ss.format(sec));
        } else
            s = ssxx.format(time) + (verbose ? " sec." : "");
        return s;
    }

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
            float time = (stopTime-startTime)/1000F;
            timerLabel.setText(timeToString(time, true));
            startButton.setText("Accept Time");
            if(acceptsSincePop >= 12){// || acceptsSincePop == -1){
                popButton.setText("POP");
                popButton.setEnabled(true);
            }
            discardButton.setEnabled(true);
            plusTwoButton.setEnabled(true);
        }
    }

//**********************************************************************************************************************
    public void timerAccept(){
        //cubesSolved++;
        try{
            acceptTimeX(stopTime-startTime, false, false);
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

    public void updateGUI(){
        //int size = solveTable.getSize();
        //timeLabels[size%12].setForeground(optionsMenu.currentColorX);
        scramblePane.setCubeColors(optionsMenu.cubeColorsX);
        scramblePane.setPyraminxColors(optionsMenu.pyraminxColorsX);
        scramblePane.setMegaminxColors(optionsMenu.megaminxColorsX);
        updateScramblePane();
        scrambleText.setBackground(optionsMenu.textBackgrColorX);
        bestAverageText.setBackground(optionsMenu.textBackgrColorX);
    } //OptionsToGUI













//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private void acceptTimeX(long time) throws NumberFormatException{
        acceptTimeX(time, false, false);
    }

    private void acceptTimeX(long time, boolean isPop, boolean isPlus2) throws NumberFormatException{
        int time100 = Math.round(time/10F);
        solveTable.addSolve(time100, newAlg, isPop, isPlus2);
        acceptsSincePop++;
        updateStatsX();
        buttonsOn();
        updateScrambleAlgs();
    } // end acceptTimeX

//**********************************************************************************************************************

    private void buttonsOn(){
        puzzleCombo.setEnabled(true);
        countdownCombo.setEnabled(true);
        startButton.setText("Start Timer");
        discardButton.setEnabled(false);
        popButton.setEnabled(false);
        plusTwoButton.setEnabled(false);

//System.err.print("sessionDetailsEnabled: " + sessionDetailsEnabled + "\n");
        sessionDetailedViewButton.setEnabled(sessionDetailsEnabled);
        insertTimeButton.setEnabled(true);
        sessionResetButton.setEnabled(true);
//System.err.print("averageDetailsEnabled: " + averageDetailsEnabled + "\n");
        averageDetailedViewButton.setEnabled(averageDetailsEnabled);
        timerLabel.setText("");
        //timerLabel.setVisible(false);

        returnFocus();
    }

//**********************************************************************************************************************

    private void resetTheSession(){
System.err.print("start resetTheSession" + "\n");
        popButton.setText("POP");
        //sessionDetailsEnabled = false;
        //averageDetailsEnabled = false;
        acceptsSincePop = 12;
        hasImported = false;
        importedIndex = 0;

        solveTable.sessionReset();
        updateStatsX();
        buttonsOn();
        updateScrambleAlgs();
        
System.err.print("end resetTheSession" + "\n");
/*
        placeInAverage = 0;
        sessionIndex = 0;
        bestAverage = 0;
        cubesSolved = 0;
        sessionTotalTime = 0;
        sessionFastest = 0;
        sessionSlowest = 0;
        acceptsSincePop = 12;
        sessionTimes = new String[100];
        sessionScrambles = new String[100];*/
/*        bestAverageText.setText("Average: N/A\nIndividual Times: N/A");
        for(int i=0; i<12; i++){
            timeString[i] = "none";
            timeLabels[i].setText("<html><font size=\"5\">" + timeString[i] + "</font></html>");
            timeLabels[i].setForeground(Color.black);
        }
        timeLabels[placeInAverage].setForeground(optionsMenu.currentColorX);
        sessionStatsLabel.setText("<html>Recent Time: N/A<br>Previous Time: N/A<br>Progress: N/A<br><br>Cubes Solved: 0<br>Session Average: N/A</html>");
        rollingAverageLabel.setText("<html>Current Average: <font size=\"5\">N/A</font><br>Progress: N/A<br><br>Fastest Time: N/A<br>Slowest Time: N/A<br>Standard Deviation: N/A</html>");*/
/*
        puzzleCombo.setEnabled(true);
        countdownCombo.setEnabled(true);
        startButton.setText("Start Timer");
        discardButton.setEnabled(false);
        popButton.setEnabled(false);
        plusTwoButton.setEnabled(false);

        sessionDetailedViewButton.setEnabled(false);
        insertTimeButton.setEnabled(true);

        averageDetailedViewButton.setEnabled(false);

        timerLabel.setText("");//timerLabel.setText("Ready4?");
        //timerLabel.setVisible(false);
//        hasImported = false;
//        updateScrambleAlgs();
        returnFocus();
*/
    }

//**********************************************************************************************************************

    public void updateStatsX(){
        int size = solveTable.getSize();
        final String BLACK = "#000000", RED = "#FF0000", BLUE = "#0000FF", TIE = "FF8000";

//System.err.print("blah: " + size + "\n");

        String sRollingAverage = "N/A";
        String sRollingProgress = "N/A";
        String sRollingProgressColor = BLACK;
        String sRollingFastest = "N/A";
        String sRollingSlowest = "N/A";
        String sRollingStdDev = "N/A";

        String sBestAverage = "N/A";
        String sBestIndvTimes = "N/A";

        String sRecentTime = "N/A";
        String sPrevTime = "N/A";
        String sProgressColor = BLACK;
        String sProgress = "N/A";
        int numberSolved = 0;
        String sSessionAverage = "N/A";


        solveTable.setTimeStyle(optionsMenu.showMinutesX, true, false);
        if(size < 12){
            for(int i=0; i<12; i++){
                String s;
                averageLabels[i].setText("#" + (i+1));
                if(i<size){
                    s = solveTable.getString(i);

                    float fastestTime = solveTable.getSolve(size-1).sessionFastestTime;
                    float slowestTime = solveTable.getSolve(size-1).sessionSlowestTime;
                    if(fastestTime == slowestTime) timeLabels[i].setForeground(Color.black);
                    else if(solveTable.getTime(i) == fastestTime) timeLabels[i].setForeground(optionsMenu.fastestColorX);
                    else if(solveTable.getTime(i) == slowestTime) timeLabels[i].setForeground(optionsMenu.slowestColorX);
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

                float fastestTime = solveTable.getSolve(size-1).rollingFastestTime;
                float slowestTime = solveTable.getSolve(size-1).rollingSlowestTime;
                if(fastestTime == slowestTime) timeLabels[(size+i)%12].setForeground(Color.black);
                else if(solveTable.getTime(index) == fastestTime) timeLabels[(size+i)%12].setForeground(optionsMenu.fastestColorX);
                else if(solveTable.getTime(index) == slowestTime) timeLabels[(size+i)%12].setForeground(optionsMenu.slowestColorX);
                else timeLabels[(size+i)%12].setForeground(Color.black);
            }
        }
        timeLabels[size%12].setForeground(optionsMenu.currentColorX);


        if(size > 0){
            SolveTable.Solve currentSolve = solveTable.getSolve(size-1);

            if(size >= 12){
                if(currentSolve.rollingAverage != INF){
                    sRollingAverage = timeToString(currentSolve.rollingAverage, false, true);
                    sRollingStdDev = timeToString(currentSolve.rollingStdDev, false, false);
                }
                else{
                    sRollingAverage = "DNF";
                    sRollingStdDev = "???";
                }

                if(size > 12){
                    SolveTable.Solve prevSolve = solveTable.getSolve(size-2);
                    if(currentSolve.rollingAverage != INF && prevSolve.rollingAverage != INF){
                        sRollingProgress = timeToString(currentSolve.rollingAverage-prevSolve.rollingAverage, false, true);
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
                solveTable.setTimeStyle(optionsMenu.showMinutesX, false, false);
                sRollingFastest = solveTable.getString(currentSolve.rollingFastestIndex);
                sRollingSlowest = solveTable.getString(currentSolve.rollingSlowestIndex);
            }


            int bestIndex = solveTable.findBestRolling();
//System.err.print("bestIndex: " + bestIndex + "\n");
            if(bestIndex != -1){
                SolveTable.Solve bestSolve = solveTable.getSolve(bestIndex);

                sBestAverage = timeToString(bestSolve.rollingAverage, false, true);
                solveTable.setTimeStyle(optionsMenu.showMinutesX, false, false);
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


            sRecentTime = solveTable.getString(size-1);
            if(size > 1){
                sPrevTime = solveTable.getString(size-2);
                float recentTime = solveTable.getTime(size-1), prevTime = solveTable.getTime(size-2);
                if(recentTime != INF && prevTime != INF){
                    sProgress = timeToString(recentTime-prevTime, false, true);
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
//System.err.print("sessionAverage: " + currentSolve.sessionAverage + "\n");
            if(currentSolve.sessionAverage != INF)
                sSessionAverage = timeToString(currentSolve.sessionAverage, false, true);
            else
                sSessionAverage = "DNF";
        }


        rollingAverageLabel.setText("<html>Current Average: <font size=\"5\">" + sRollingAverage + "</font><br>Progress: <font color=\"" + sRollingProgressColor + "\">" + sRollingProgress +"</font><br><br>Fastest Time: " + sRollingFastest + "<br>Slowest Time: " + sRollingSlowest + "<br>Standard Deviation: " + sRollingStdDev + "</html>");

        bestAverageText.setText("Average: " + sBestAverage + "\nIndividual Times: " + sBestIndvTimes);

        sessionStatsLabel.setText("<html>Recent Time: " + sRecentTime + "<br>Previous Time: " + sPrevTime + "<br>Progress: <font color=\"" + sProgressColor + "\">" + sProgress + "</font><br><br>Cubes Solved: " + numberSolved + "<br>Session Average: " + sSessionAverage + "</html>");

        sessionDetailsEnabled = (size > 0);
        averageDetailsEnabled = (sBestAverage != "N/A");

    } // end updateStatsX

}