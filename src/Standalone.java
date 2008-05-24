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

public class Standalone extends JFrame implements ActionListener, Runnable, Constants{

    private OptionsMenu optionsMenu;
    private ScrambleGenerator scrambleGenerator;
    private InstructionScreen instructionScreen;
    private AboutScreen aboutScreen;

    private ScrambleAlg scrambleAlg;
    private ScramblePane scramblePane;
    private TimerArea timerArea;
    private String newAlg;

    JButton startButton, discardButton, popButton, plusTwoButton;//, averageModeButton;
    JButton sessionResetButton, sessionDetailedViewButton, averageDetailedViewButton, insertTimeButton;
    JLabel puzzleLabel, countdownLabel, useThisAlgLabel, timerLabel;
    JLabel sessionStatsLabel, rollingAverageLabel, bestAverageLabel;
    JMenuBar jMenuBar;
    JMenu fileMenu, toolsMenu, networkMenu, helpMenu;
    JMenuItem saveBestItem, saveSessionItem, optionsItem, exitItem, importItem, generatorItem, instItem, aboutItem, serverItem, clientItem;
    JComboBox puzzleCombo, countdownCombo;
    JTextArea scrambleText, bestAverageText;

    JLabel[] averageLabels, timeLabels;
    SmartButton[] smartButton;

    //private boolean averageOfFiveMode;

    volatile Thread timerThread;
    AudioClip countdownClip = null;

    SolveTable solveTable;
    private boolean sessionDetailsEnabled, averageDetailsEnabled;
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
        } catch(NullPointerException ex){JOptionPane.showMessageDialog(this, "count.mid not found. There will be no countdown audio.");}

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
        popButton = new JButton();
        plusTwoButton = new JButton("+2");
        //averageModeButton = new JButton("Average of 5 Mode");

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
        timerLabel.setFont(new Font("Serif", Font.PLAIN, 94));

        timerArea = new TimerArea(this); // kinda dangerous but this is going to be how we invoke the timerStart() and stuff

        scramblePane = new ScramblePane(282, 215+20); // needs to be changed in two places
        //scramblePane.setBorder(BorderFactory.createTitledBorder(theBorder, "Scramble View"));
        scramblePane.setLayout(null);


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

        enterPressesWhenFocused(startButton);
        enterPressesWhenFocused(discardButton);
        enterPressesWhenFocused(popButton);
        enterPressesWhenFocused(plusTwoButton);
        //enterPressesWhenFocused(averageModeButton);
        enterPressesWhenFocused(sessionResetButton);
        enterPressesWhenFocused(sessionDetailedViewButton);
        enterPressesWhenFocused(averageDetailedViewButton);
        enterPressesWhenFocused(insertTimeButton);

        timeLabels = new JLabel[12];
        for(int i=0; i<12; i++)
            timeLabels[i] = new JLabel();


        runningCountdown = false;
        countingDown = 0;
        startTime = 0;
        stopTime = 0;


        optionsMenu.loadOptions(); // inital load of options
        solveTable = new SolveTable(optionsMenu.puzzleX);
        updateLabels(optionsMenu.puzzleX);

        if(!optionsMenu.puzzleX.equals(puzzleCombo.getSelectedItem()+"")) // less glitchier
            puzzleCombo.setSelectedItem(optionsMenu.puzzleX);
        if(!optionsMenu.countdownX.equals(countdownCombo.getSelectedItem()+"")) // less glitchier
            countdownCombo.setSelectedItem(optionsMenu.countdownX);

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

        scramblePane.setBounds(563,5,282,215+20); // needs to be changed in two places

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
        //averageModeButton.setBounds(663,326,160,20);
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
        //averageModeButton.addActionListener(this);
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
        } else if(source == popButton){
            //acceptsSincePop = 0;
            popButton.setText("Popped!");
            try{
                acceptTimeX(stopTime-startTime, true, false);
            } catch(NumberFormatException ex){
                JOptionPane.showMessageDialog(this, ERROR_MESS);
                System.out.println(ex.getMessage());
            }
        } else if(source == plusTwoButton){
            try{
                acceptTimeX(stopTime-startTime, false, true);
            } catch(NumberFormatException ex){
                JOptionPane.showMessageDialog(this, ERROR_MESS);
                System.out.println(ex.getMessage());
            }
/*        } else if(source == averageModeButton){
            if(averageModeButton.getText().equals("Average of 5 Mode")){
                averageModeButton.setText("Average of 10 Mode");
                averageOfFiveMode = true;
            } else{
                averageModeButton.setText("Average of 5 Mode");
                averageOfFiveMode = false;
            }*/
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
            String puzzle = puzzleCombo.getSelectedItem()+"";
            if(solveTable.getPuzzle().equals(puzzle)) return;

            solveTable.setPuzzle(puzzle);
            updateLabels(puzzle);

//popButton.setText("POP");
//acceptsSincePop = 12;
            if(solveTable.okayToPop())
                popButton.setText("POP");
            else
                popButton.setText("Popped!");

            hasImported = false;
            importedIndex = 0;
            updateStatsX();
            buttonsOn();
            updateScrambleAlgs();
        } else if(source == countdownCombo){
            returnFocus();
        } else if(source == sessionDetailedViewButton){
            DetailedView win = new DetailedView("Session Times for " + solveTable.getPuzzle(), getSessionView(), optionsMenu.textBackgrColorX);
            win.setVisible(true);
        } else if(source == averageDetailedViewButton){
            DetailedView win = new DetailedView("Best Average for " + solveTable.getPuzzle(), getAverageView(), optionsMenu.textBackgrColorX);
            win.setVisible(true);
        } else if(source == saveSessionItem){
            if(sessionDetailsEnabled){
                int userChoice = fc.showSaveDialog(Standalone.this);
                if(userChoice == JFileChooser.APPROVE_OPTION)
                    saveToFile(getSessionView(), fc.getSelectedFile());
            } else {
                JOptionPane.showMessageDialog(this, "No times have been recorded for this session.");
            }
        } else if(source == saveBestItem){
            if(averageDetailsEnabled){
                int userChoice = fc.showSaveDialog(Standalone.this);
                if(userChoice == JFileChooser.APPROVE_OPTION)
                    saveToFile(getAverageView(), fc.getSelectedFile());
            } else {
                JOptionPane.showMessageDialog(this, "Not enough solves completed to calculate an average.");
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
        } else if(source == insertTimeButton){
            String input = JOptionPane.showInputDialog(this, "Enter time to add in seconds or POP:");
            if(input == null) return;
            if(input.equalsIgnoreCase("POP")){
                try{
                    acceptTimeX(0, true, false);
                } catch(NumberFormatException ex){
                    JOptionPane.showMessageDialog(this, "Invalid number entered. No time was added to the session.");
                }
                return;
            }
            float inputTime = 0;
            try{
                inputTime = Float.parseFloat(ssxx.format(Float.parseFloat(input)));
            } catch(NumberFormatException ex){
                JOptionPane.showMessageDialog(this, "Invalid number entered. No time was added to the session.");
                return;
            }
            try{
                acceptTimeX(Math.round(inputTime * 1000));
            } catch(NumberFormatException ex){
                JOptionPane.showMessageDialog(this, ERROR_MESS);
                System.out.println(ex.getMessage());
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
            disposeAll();
        } else if(source == clientItem){
            int choice = JOptionPane.showConfirmDialog(this, "Switching to Client Mode destroys main window session. Are you sure?", "Warning!", 0);
            if(choice == 1){
                returnFocus();
                return;
            }
            //this.setVisible(false);
            Client client = new Client(optionsMenu);
            client.setVisible(true);
            disposeAll();
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
                float time = (System.currentTimeMillis() - startTime)/1000F;
                timerLabel.setText(timeToString(time, true));
                try{
                    timerThread.sleep(120);
                } catch(InterruptedException ex){
                    System.out.println(ex.getMessage());
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

    private String getSessionView(){

        String formatedAverage = "N/A", formatedStdDev = "N/A";
        String timesAndScrambles = "none", timesOnly = "none";
        String formatedFastest = "N/A", formatedSlowest = "N/A";
        int numberSolved = 0, numberPopped = 0;

        int size = solveTable.getSize();
        if(size > 0){
            SolveTable.Solve currentSolve = solveTable.getSolve(size-1);

            numberSolved = currentSolve.numberSolves;
            numberPopped = currentSolve.numberPops;

            if(currentSolve.sessionAverage != INF)
                formatedAverage = timeToString(currentSolve.sessionAverage, false, true);
            else
                formatedAverage = "DNF";

            if(currentSolve.sessionStdDev != INF)
                formatedStdDev = timeToString(currentSolve.sessionStdDev, false, false);
            else
                formatedStdDev = "???";

            solveTable.setTimeStyle(optionsMenu.showMinutesX, false, false);
            timesAndScrambles = ""; timesOnly = "";
            for(int i=0; i<size; i++){
                String s = solveTable.getString(i);
                timesAndScrambles += (i+1) + ")          " + s + "          " + solveTable.getScramble(i) + "\n";
                timesOnly += s + "\n";
            }

            //solveTable.setTimeStyle(optionsMenu.showMinutesX, false, false);
            formatedFastest = solveTable.getString(currentSolve.sessionFastestIndex);
            formatedSlowest = solveTable.getString(currentSolve.sessionSlowestIndex);
        }

        String returnMe = optionsMenu.sessionViewFormatX;
        returnMe = returnMe.replaceAll("%T", new Date()+"");
        returnMe = returnMe.replaceAll("%C", numberSolved+"");
        returnMe = returnMe.replaceAll("%P", numberPopped+"");
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

            formatedAverage = timeToString(bestSolve.rollingAverage, false, true);
            formatedStdDev = timeToString(bestSolve.rollingStdDev, false, false);

            solveTable.setTimeStyle(optionsMenu.showMinutesX, false, false);
            timesAndScrambles = ""; timesOnly = "";
            for(int i=1; i<=12; i++){
                int index = i+bestIndex-12;
                String s = solveTable.getString(index);
                if(index == bestSolve.rollingFastestIndex || index == bestSolve.rollingSlowestIndex)
                    s = "(" + s + ")";
                timesAndScrambles += (index+1) + ")          " + s + "          " + solveTable.getScramble(index) + "\n";
                timesOnly += s + "\n";
            }

            //solveTable.setTimeStyle(optionsMenu.showMinutesX, false, false);
            formatedFastest = solveTable.getString(bestSolve.rollingFastestIndex);
            formatedSlowest = solveTable.getString(bestSolve.rollingSlowestIndex);
        }

        String returnMe = optionsMenu.averageViewFormatX;
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

    private void saveToFile(String text, File file){
        File outputFile = new File(file+".txt");
        try{
            FileWriter fr = new FileWriter(outputFile);
            BufferedWriter out = new BufferedWriter(fr);
            out.write(text);
            out.close();
        } catch(IOException ex){
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
        scramblePane.newScramble(puzzleCombo.getSelectedItem()+"", newAlg.replaceAll(ALG_BREAK, " "));
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
            if(solveTable.okayToPop()){//if(acceptsSincePop >= 12){
                popButton.setText("POP");
                popButton.setEnabled(true);
            } else{
                popButton.setText("Popped!");
                popButton.setEnabled(false);
            }
            discardButton.setEnabled(true);
            plusTwoButton.setEnabled(true);
        }
    }

//**********************************************************************************************************************
    public void timerAccept(){
        try{
            acceptTimeX(stopTime-startTime);
        } catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this, ERROR_MESS);
            System.out.println(ex.getMessage());
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
        //acceptsSincePop++;
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

        sessionDetailedViewButton.setEnabled(sessionDetailsEnabled);
        insertTimeButton.setEnabled(true);
        sessionResetButton.setEnabled(true);

        averageDetailedViewButton.setEnabled(averageDetailsEnabled);
        timerLabel.setText("");
        //timerLabel.setVisible(false);

        returnFocus();
    }

//**********************************************************************************************************************

    private void resetTheSession(){
        popButton.setText("POP");
        //acceptsSincePop = 12;
        hasImported = false;
        importedIndex = 0;

        solveTable.sessionReset();
        updateStatsX();
        buttonsOn();
        updateScrambleAlgs();
    }

//**********************************************************************************************************************

    public void updateStatsX(){
        int size = solveTable.getSize();
        final String BLACK = "#000000", RED = "#FF0000", BLUE = "#0000FF", TIE = "FF8000";

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
            if(currentSolve.sessionAverage != INF)
                sSessionAverage = timeToString(currentSolve.sessionAverage, false, true);
            else
                sSessionAverage = "DNF";
        }


        rollingAverageLabel.setText("<html>Current Average: <font size=\"5\">" + sRollingAverage + "</font><br>Progress: <font color=\"" + sRollingProgressColor + "\">" + sRollingProgress +"</font><br><br>Fastest Time: " + sRollingFastest + "<br>Slowest Time: " + sRollingSlowest + "<br>Standard Deviation: " + sRollingStdDev + "</html>");

        bestAverageText.setText("Average: " + sBestAverage + "\nIndividual Times: " + sBestIndvTimes);

        sessionStatsLabel.setText("<html>Recent Time: " + sRecentTime + "<br>Previous Time: " + sPrevTime + "<br>Progress: <font color=\"" + sProgressColor + "\">" + sProgress + "</font><br><br>Total Solves: " + numberSolved + "<br>Session Average: " + sSessionAverage + "</html>");

        sessionDetailsEnabled = (size > 0);
        averageDetailsEnabled = (sBestAverage != "N/A");

    } // end updateStatsX

//**********************************************************************************************************************

    private void updateLabels(String puzzle){
        useThisAlgLabel.setText("Use this " + puzzle + " Scramble Algorithm:");
        scramblePane.setBorder(BorderFactory.createTitledBorder(theBorder, puzzle + " Scramble View"));
        sessionStatsLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Session Statistics for " + puzzle));
        rollingAverageLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Rolling Average for " + puzzle));
        bestAverageLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Best Average for " + puzzle));
    }

}
