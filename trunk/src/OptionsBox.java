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
import java.io.*;
import java.util.*;
import javax.swing.border.Border;

public class OptionsBox extends JFrame implements ActionListener, MouseListener, ScramblePanel.ColorListener, Constants{
    private static final String FILENAME = "rjt.properties";

    // Complete List of Save-able options
    public String puzzleX, countdownX;
    public boolean showResetConfirmX, showMinutesX;
    public Color countdownColorX, timerColorX, textBackgrColorX, currentColorX, fastestColorX, slowestColorX;
    public Color[] cubeColorsX = new Color[6];
    public Color[] pyraminxColorsX = new Color[4];
    public Color[] megaminxColorsX = new Color[12];
    public String averageViewFormatX, sessionViewFormatX;

    JTabbedPane tabs;
    JPanel generalTab, colorScheme1Tab, colorScheme2Tab, sessionTab, averageTab;
    JLayeredPane layeredPane1, layeredPane2;
    JButton saveButton, applyButton, resetButton, rejectButton;//, closeButton;
    JButton colorResetButton, cubeResetButton, pyraminxResetButton, megaminxResetButton, sessionResetButton, averageResetButton;
    JLabel puzzleLabel, countdownLabel, sessionSyntaxLabel, averageSyntaxLabel, startupLabel, colorLabel;
    JLabel countdownCLabel, timerCLabel, textBackgrCLabel, currentCLabel, fastestCLabel, slowestCLabel;
    ScramblePanel cubePanel, pyraminxPanel, megaminxPanel;

    JComboBox puzzleCombo, countdownCombo;
    JCheckBox confirmBox, showMinutesBox;
    JTextArea countdownColorText, timerColorText, textBackgrColorText, fastestColorText, slowestColorText, currentColorText;
    JTextArea sessionText, averageText;
    JScrollPane sessionScrollPane, averageScrollPane;

//**********************************************************************************************************************

    public OptionsBox(){
        // configure Contentpane
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        // configure JFrame
        setTitle("Options for " + APP_TITLE + " (stored in " + FILENAME + ")");
        RJT_Utils.centerJFrame(this, 604, 325);
        RJT_Utils.configureJFrame(this);

        startupLabel = new JLabel();
        startupLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Start-up Options"));
        colorLabel = new JLabel();
        colorLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Color Options"));

        puzzleLabel = new JLabel("Puzzle:");
        puzzleCombo = new JComboBox(puzzleChoices);
        countdownLabel = new JLabel("Countdown:");
        countdownCombo = new JComboBox(countdownChoices);
        confirmBox = new JCheckBox("Session Reset Warning Window");
        showMinutesBox = new JCheckBox("Use mm:ss.xx Format");

        countdownCLabel = new JLabel("Countdown");
        countdownColorText = new JTextArea();
        countdownColorText.setEditable(false);
        countdownColorText.setBorder(blackLine);
        timerCLabel = new JLabel("Timer");
        timerColorText = new JTextArea();
        timerColorText.setEditable(false);
        timerColorText.setBorder(blackLine);
        textBackgrCLabel = new JLabel("Background");
        textBackgrColorText = new JTextArea();
        textBackgrColorText.setEditable(false);
        textBackgrColorText.setBorder(blackLine);
        currentCLabel = new JLabel("Current Time");
        currentColorText = new JTextArea();
        currentColorText.setEditable(false);
        currentColorText.setBorder(blackLine);
        fastestCLabel = new JLabel("Fastest Time");
        fastestColorText = new JTextArea();
        fastestColorText.setEditable(false);
        fastestColorText.setBorder(blackLine);
        slowestCLabel = new JLabel("Slowest Time");
        slowestColorText = new JTextArea();
        slowestColorText.setEditable(false);
        slowestColorText.setBorder(blackLine);

        cubePanel = new ScramblePanel(269+3,200);//2*(269+3)+10,200);
        cubePanel.setLayout(null);
        cubePanel.setBorder(BorderFactory.createTitledBorder(theBorder, "Cube Preview"));

        pyraminxPanel = new ScramblePanel(269+3,200);// was 282,235);
        pyraminxPanel.setLayout(null);
        pyraminxPanel.setBorder(BorderFactory.createTitledBorder(theBorder, "Pyraminx Preview"));

        megaminxPanel = new ScramblePanel(2*(269+3)+10,200);// was 282,235);
        megaminxPanel.setLayout(null);
        megaminxPanel.setBorder(BorderFactory.createTitledBorder(theBorder, "Megaminx Preview"));

        sessionSyntaxLabel = new JLabel("<html>%A - Average<br>%C - Number of Solves<br>%D - Standard Deviation<br>%F - Fastest Time<br>%I - Times and Scrambles<br>%O - Times Only<br>%P - Number of DNFs<br>%S - Slowest Time<br>%T - Date and Time<br>%Z - Puzzle Name</html>");
        sessionSyntaxLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Syntax"));
        sessionText = new JTextArea();
        sessionText.setFont(regFont);
        sessionText.setEditable(true);
        sessionScrollPane = new JScrollPane(sessionText);
        sessionScrollPane.setBorder(blackLine);

        averageSyntaxLabel = new JLabel("<html>%A - Average<br>%D - Standard Deviation<br>%F - Fastest Time<br>%I - Times and Scrambles<br>%O - Times Only<br>%T - Date and Time<br>%S - Slowest Time<br>%Z - Puzzle Name</html>");
        averageSyntaxLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Syntax"));
        averageText = new JTextArea();
        averageText.setFont(regFont);
        averageText.setEditable(true);
        averageScrollPane = new JScrollPane(averageText);
        averageScrollPane.setBorder(blackLine);

        colorResetButton = new JButton("Reset");
        cubeResetButton = new JButton("Reset");
        pyraminxResetButton = new JButton("Reset");
        megaminxResetButton = new JButton("Reset");
        sessionResetButton = new JButton("Reset");
        averageResetButton = new JButton("Reset");

        saveButton = new JButton("Save and Apply");
        applyButton = new JButton("Apply Only");
        resetButton = new JButton(RJT_Utils.makeRed("Reset All"));
        rejectButton = new JButton("Undo Changes");
        //closeButton = new JButton("Close");

        RJT_Utils.enterPressesWhenFocused(colorResetButton);
        RJT_Utils.enterPressesWhenFocused(cubeResetButton);
        RJT_Utils.enterPressesWhenFocused(pyraminxResetButton);
        RJT_Utils.enterPressesWhenFocused(megaminxResetButton);
        RJT_Utils.enterPressesWhenFocused(sessionResetButton);
        RJT_Utils.enterPressesWhenFocused(averageResetButton);

        RJT_Utils.enterPressesWhenFocused(saveButton);
        RJT_Utils.enterPressesWhenFocused(applyButton);
        RJT_Utils.enterPressesWhenFocused(resetButton);
        RJT_Utils.enterPressesWhenFocused(rejectButton);

        // call big add tabs and content function
        addStuffToTabs();

        // call big setBounds function
        setTheBounds();

        // add Content
        contentPane.add(tabs);
        contentPane.add(saveButton);
        contentPane.add(applyButton);
        contentPane.add(resetButton);
        contentPane.add(rejectButton);
        //contentPane.add(closeButton);

        // add ActionListeners
        addTheActionListeners();

        loadOptions();
    } // end constructor

//**********************************************************************************************************************

    private void setTheBounds(){

        tabs.setBounds(10,5,579,240);
        layeredPane1.setBounds(0,0,579,240);
        layeredPane2.setBounds(0,0,579,240);
        //saveButton.setBounds(10,255,186,30);
        //resetButton.setBounds(186+20,255,186,30);
        //cancelButton.setBounds(2*186+30,255,186,30);

        saveButton.setBounds(10,255,137,30);
        applyButton.setBounds(137+20,255,137,30);
        resetButton.setBounds(2*137+30,255,137,30);
        rejectButton.setBounds(3*137+40,255,137,30);
        //closeButton.setBounds(3*137+40,255,137,30);

        startupLabel.setBounds(10,5,269+3,200);
        colorLabel.setBounds(289+3,5,269+3,200);
        sessionScrollPane.setBounds(12,10,358+6-30,193);
        averageScrollPane.setBounds(12,10,358+6-30,193);
        sessionSyntaxLabel.setBounds(378+6-30,5,180+30,200);
        averageSyntaxLabel.setBounds(378+6-30,5,180+30,200);

        puzzleLabel.setBounds(30,25,90,20);
        puzzleCombo.setBounds(30,45,90,20);
        countdownLabel.setBounds(130,25,90,20);
        countdownCombo.setBounds(130,45,90,20);
        confirmBox.setBounds(30,75,230,20);
        showMinutesBox.setBounds(30,105,230,20);

        countdownColorText.setBounds(309,30,20,20);
        countdownCLabel.setBounds(334,30,100,20);
        timerColorText.setBounds(309,60,20,20);
        timerCLabel.setBounds(334,60,100,20);
        textBackgrColorText.setBounds(309,90,20,20);
        textBackgrCLabel.setBounds(334,90,100,20);
        currentColorText.setBounds(309+120,30,20,20);
        currentCLabel.setBounds(334+120,30,200,20);
        fastestColorText.setBounds(309+120,60,20,20);
        fastestCLabel.setBounds(334+120,60,200,20);
        slowestColorText.setBounds(309+120,90,20,20);
        slowestCLabel.setBounds(334+120,90,200,20);

        cubePanel.setBounds(10,5,269+3,200);//2*(269+3)+10,200);
        pyraminxPanel.setBounds((579-15)-(269+3),5,269+3,200); // was 282,235);
        megaminxPanel.setBounds(10,5,2*(269+3)+10,200); // was 282,235);

        colorResetButton.setBounds(564-85,200-30,70,20);
        cubeResetButton.setBounds(282-85,200-30,70,20);
        pyraminxResetButton.setBounds(564-85,200-30,70,20);
        megaminxResetButton.setBounds(564-85,200-30,70,20);
        sessionResetButton.setBounds(564-85,200-30,70,20);
        averageResetButton.setBounds(564-85,200-30,70,20);
    }

//**********************************************************************************************************************

    private void addStuffToTabs(){

        generalTab = new JPanel();
        generalTab.setLayout(null);
        generalTab.add(startupLabel);
        generalTab.add(colorLabel);
        generalTab.add(puzzleLabel);
        generalTab.add(countdownLabel);
        generalTab.add(puzzleCombo);
        generalTab.add(countdownCombo);
        generalTab.add(confirmBox);
        generalTab.add(showMinutesBox);
        generalTab.add(timerColorText);
        generalTab.add(timerCLabel);
        generalTab.add(countdownColorText);
        generalTab.add(countdownCLabel);
        generalTab.add(textBackgrColorText);
        generalTab.add(textBackgrCLabel);
        generalTab.add(currentColorText);
        generalTab.add(currentCLabel);
        generalTab.add(fastestColorText);
        generalTab.add(fastestCLabel);
        generalTab.add(slowestColorText);
        generalTab.add(slowestCLabel);
        generalTab.add(colorResetButton);

        layeredPane1 = new JLayeredPane();
        layeredPane1.setLayout(null);
        layeredPane1.add(cubePanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane1.add(pyraminxPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane1.add(cubeResetButton, JLayeredPane.MODAL_LAYER);
        layeredPane1.add(pyraminxResetButton, JLayeredPane.MODAL_LAYER);
        colorScheme1Tab = new JPanel();
        colorScheme1Tab.setLayout(null);
        colorScheme1Tab.add(layeredPane1);

        layeredPane2 = new JLayeredPane();
        layeredPane2.setLayout(null);
        layeredPane2.add(megaminxPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane2.add(megaminxResetButton, JLayeredPane.MODAL_LAYER);
        colorScheme2Tab = new JPanel();
        colorScheme2Tab.setLayout(null);
        colorScheme2Tab.add(layeredPane2);

        sessionTab = new JPanel();
        sessionTab.setLayout(null);
        sessionTab.add(sessionSyntaxLabel);
        sessionTab.add(sessionScrollPane);
        sessionTab.add(sessionResetButton);

        averageTab = new JPanel();
        averageTab.setLayout(null);
        averageTab.add(averageSyntaxLabel);
        averageTab.add(averageScrollPane);
        averageTab.add(averageResetButton);

        tabs = new JTabbedPane();
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.add(generalTab, "General");
        tabs.add(colorScheme1Tab, "Cube & Pyraminx Scheme");
        tabs.add(colorScheme2Tab, "Megaminx Scheme");
        tabs.add(sessionTab, "Session Times");
        tabs.add(averageTab, "Best Average");
    }

//**********************************************************************************************************************

    private void addTheActionListeners(){

        saveButton.addActionListener(this);
        applyButton.addActionListener(this);
        resetButton.addActionListener(this);
        rejectButton.addActionListener(this);
        //closeButton.addActionListener(this);

        countdownColorText.addMouseListener(this);
        timerColorText.addMouseListener(this);
        textBackgrColorText.addMouseListener(this);
        currentColorText.addMouseListener(this);
        fastestColorText.addMouseListener(this);
        slowestColorText.addMouseListener(this);

        cubePanel.addColorListener(this);
        pyraminxPanel.addColorListener(this);
        megaminxPanel.addColorListener(this);

        colorResetButton.addActionListener(this);
        cubeResetButton.addActionListener(this);
        pyraminxResetButton.addActionListener(this);
        megaminxResetButton.addActionListener(this);
        sessionResetButton.addActionListener(this);
        averageResetButton.addActionListener(this);
    }

//**********************************************************************************************************************

    public void actionPerformed(ActionEvent e){
        Object source = e.getSource();

        if(source == saveButton){
            saveOptions();
            optionsListener.optionsCallback();
            this.setVisible(false);
        } else if(source == applyButton){
            captureOptions();
            optionsListener.optionsCallback();
        } else if(source == resetButton){
            resetOptions();
        } else if(source == rejectButton){
            loadOptions();
//        } else if(source == closeButton){
//            this.setVisible(false);
        } else if(source == colorResetButton){
            colorReset();
            OptionsToGUI();
        } else if(source == cubeResetButton){
            cubeReset();
            OptionsToGUI();
        } else if(source == pyraminxResetButton){
            pyraminxReset();
            OptionsToGUI();
        } else if(source == megaminxResetButton){
            megaminxReset();
            OptionsToGUI();
        } else if(source == sessionResetButton){
            sessionReset();
            OptionsToGUI();
        } else if(source == averageResetButton){
            averageReset();
            OptionsToGUI();
        }
    } // end actionPerformed

//**********************************************************************************************************************

    private void setFaceBounds(JTextArea[][] aFace, int order, int x, int y, int size){
        for(int i=0; i<order; i++)
            for(int j=0; j<order; j++)
                aFace[i][j].setBounds(j*size+x, i*size+y, size, size);
    }

//**********************************************************************************************************************

    private void saveOptions(){
        captureOptions();

        SortedProperties props = new SortedProperties();
        props.setProperty("01.puzzle", puzzleX);
        props.setProperty("02.countdown", countdownX);
        props.setProperty("03.showResetConfirm", showResetConfirmX+"");
        props.setProperty("04.showMinutes", showMinutesX+"");
        props.setProperty("05.countdownColor", colorToString(countdownColorX));
        props.setProperty("06.timerColor", colorToString(timerColorX));
        props.setProperty("07.textBackgrColor", colorToString(textBackgrColorX));
        props.setProperty("08.currentColor", colorToString(currentColorX));
        props.setProperty("09.fastestColor", colorToString(fastestColorX));
        props.setProperty("10.slowestColor", colorToString(slowestColorX));
        for(int face=0; face<6; face++)
            props.setProperty("11.cubeColors_" + padNum(face), colorToString(cubeColorsX[face]));
        for(int face=0; face<4; face++)
            props.setProperty("12.pyraminxColors_" + padNum(face), colorToString(pyraminxColorsX[face]));
        for(int face=0; face<12; face++)
            props.setProperty("13.megaminxColors_" + padNum(face), colorToString(megaminxColorsX[face]));
        props.setProperty("14.averageViewFormat", averageViewFormatX);
        props.setProperty("15.sessionViewFormat", sessionViewFormatX);

        try{
            FileOutputStream out = new FileOutputStream(FILENAME);
            props.store(out, APP_TITLE + " Configuration File");
            out.close();
        }
        catch(IOException ex){
            JOptionPane.showMessageDialog(this, "There was an error saving. You may not have write permissions.");
        }
    }

//**********************************************************************************************************************

    public void loadOptions(){
        try{
            SortedProperties props = new SortedProperties();
            FileInputStream in = new FileInputStream(FILENAME);
            props.load(in);
            in.close();

            puzzleX = props.getProperty("01.puzzle");
            countdownX = props.getProperty("02.countdown");
            showResetConfirmX = Boolean.parseBoolean(props.getProperty("03.showResetConfirm"));
            showMinutesX = Boolean.parseBoolean(props.getProperty("04.showMinutes"));
            countdownColorX = stringToColor(props.getProperty("05.countdownColor"));
            timerColorX = stringToColor(props.getProperty("06.timerColor"));
            textBackgrColorX = stringToColor(props.getProperty("07.textBackgrColor"));
            currentColorX = stringToColor(props.getProperty("08.currentColor"));
            fastestColorX = stringToColor(props.getProperty("09.fastestColor"));
            slowestColorX = stringToColor(props.getProperty("10.slowestColor"));
            for(int face=0; face<6; face++)
                cubeColorsX[face] = stringToColor(props.getProperty("11.cubeColors_" + padNum(face)));
            for(int face=0; face<4; face++)
                pyraminxColorsX[face] = stringToColor(props.getProperty("12.pyraminxColors_" + padNum(face)));
            for(int face=0; face<12; face++)
                megaminxColorsX[face] = stringToColor(props.getProperty("13.megaminxColors_" + padNum(face)));
            averageViewFormatX = props.getProperty("14.averageViewFormat");
            sessionViewFormatX = props.getProperty("15.sessionViewFormat");

            OptionsToGUI();
        } catch(IOException ex){
            resetOptions();
        }
    } // end loadOptions

//**********************************************************************************************************************

    private void resetOptions(){
        puzzleX = "3x3x3";
        countdownX = "15";
        showResetConfirmX = true;
        showMinutesX = true;

        colorReset();
        cubeReset();
        pyraminxReset();
        megaminxReset();
        sessionReset();
        averageReset();

        OptionsToGUI();
    } // end resetOptions

//**********************************************************************************************************************

    private void colorReset(){
        countdownColorX = Color.red;
        timerColorX = Color.blue;
        // yellow = (255,222,140), purple = (255,220,220), cyan = (140,255,222), Ocean = (200,221,242)
        textBackgrColorX = new Color(200,221,242);
        currentColorX = new Color(0,180,0);
        fastestColorX = Color.blue;
        slowestColorX = Color.red;
    }

    private void cubeReset(){
        cubeColorsX[0] = new Color(0,196,0); // green
        cubeColorsX[1] = new Color(0,0,255); // blue
        cubeColorsX[2] = new Color(255,128,0); // orange
        cubeColorsX[3] = new Color(255,0,0); // red
        cubeColorsX[4] = new Color(255,255,0); // yellow
        cubeColorsX[5] = new Color(255,255,255); // white
    }

    private void pyraminxReset(){
        pyraminxColorsX[0] = new Color(0,180,255); // powder blue
        pyraminxColorsX[1] = new Color(255,0,0); // red
        pyraminxColorsX[2] = new Color(255,255,0); // yellow
        pyraminxColorsX[3] = new Color(0,255,0); // bright green
    }

    private void megaminxReset(){
        megaminxColorsX[0] = new Color(255,255,255); // white
        megaminxColorsX[1] = new Color(0,180,255); // powder blue
        megaminxColorsX[2] = new Color(200,128,0); // brown
        megaminxColorsX[3] = new Color(255,0,0); // red
        megaminxColorsX[4] = new Color(255,255,0); // yellow
        megaminxColorsX[5] = new Color(0,255,0); // bright green
        megaminxColorsX[6] = new Color(160,0,255); // purple
        megaminxColorsX[7] = new Color(0,0,210); // dark blue
        megaminxColorsX[8] = new Color(0,128,0); // dark green
        megaminxColorsX[9] = new Color(255,80,80); // pink
        megaminxColorsX[10] = new Color(255,180,180); // light pink
        megaminxColorsX[11] = new Color(255,128,0); // orange
    }

    private void sessionReset(){
        sessionViewFormatX = "----- " + APP_TITLE + " Session Statistics for %T -----\n\nTotal Solves: %C\nTotal DNFs: %P\nAverage: %A\n\nFastest Time: %F\nSlowest Time: %S\nStandard Deviation: %D\n\nIndividual Times:\n%I";
    }

    private void averageReset(){
        averageViewFormatX = "----- " + APP_TITLE + " Best Average for %T -----\n\nAverage: %A\n\nFastest Time: %F\nSlowest Time: %S\nStandard Deviation: %D\n\nIndividual Times:\n%I";
    }

//**********************************************************************************************************************

    private void captureOptions(){

        puzzleX = puzzleCombo.getSelectedItem()+"";
        countdownX = countdownCombo.getSelectedItem()+"";
        showResetConfirmX = confirmBox.isSelected();
        showMinutesX = showMinutesBox.isSelected();
        countdownColorX = countdownColorText.getBackground();
        timerColorX = timerColorText.getBackground();
        textBackgrColorX = textBackgrColorText.getBackground();
        currentColorX = currentColorText.getBackground();
        fastestColorX = fastestColorText.getBackground();
        slowestColorX = slowestColorText.getBackground();
        averageViewFormatX = averageText.getText();
        sessionViewFormatX = sessionText.getText();

        cubeColorsX = cubePanel.getCubeColors();
        pyraminxColorsX = pyraminxPanel.getPyraminxColors();
        megaminxColorsX = megaminxPanel.getMegaminxColors();

    } // end captureOptions

//**********************************************************************************************************************

    private void OptionsToGUI(){

        puzzleCombo.setSelectedItem(puzzleX);
        countdownCombo.setSelectedItem(countdownX);
        confirmBox.setSelected(showResetConfirmX);
        showMinutesBox.setSelected(showMinutesX);
        countdownColorText.setBackground(countdownColorX);
        timerColorText.setBackground(timerColorX);
        textBackgrColorText.setBackground(textBackgrColorX);
            averageText.setBackground(textBackgrColorX);
            sessionText.setBackground(textBackgrColorX);
        currentColorText.setBackground(currentColorX);
        fastestColorText.setBackground(fastestColorX);
        slowestColorText.setBackground(slowestColorX);
        averageText.setText(averageViewFormatX); averageText.setCaretPosition(0);
        sessionText.setText(sessionViewFormatX); sessionText.setCaretPosition(0);

        cubePanel.setCubeColors(cubeColorsX);
        cubePanel.newScramble("3x3x3", "");
        pyraminxPanel.setPyraminxColors(pyraminxColorsX);
        pyraminxPanel.newScramble("Pyraminx", "");
        megaminxPanel.setMegaminxColors(megaminxColorsX);
        megaminxPanel.newScramble("Megaminx", "");

    } // end OptionsToGUI

//**********************************************************************************************************************

    private static Color stringToColor(String s){
        return new Color(Integer.parseInt(s, 16));
    }

//**********************************************************************************************************************

    private static String colorToString(Color c){
        String s = Integer.toHexString(c.getRGB() & 0xffffff);
        int pad = 6-s.length();
        if(pad>0)
            for(int i=0; i<pad; i++)
                s = "0"+s;
        return s;
    }

//**********************************************************************************************************************

    private static String padNum(int n){
        String s = n+"";
        int pad = 2-s.length();
        if(pad>0)
            for(int i=0; i<pad; i++)
                s = "0"+s;
        return s;
    }

//**********************************************************************************************************************

    public void mouseClicked(MouseEvent e){
        Object source = e.getSource();

        if(source == countdownColorText) makeColorChooser(countdownColorText, "Countdown Display");
        else if(source == timerColorText) makeColorChooser(timerColorText, "Timing Display");
        else if(source == textBackgrColorText) makeColorChooser(textBackgrColorText, "Alg Background");
        else if(source == currentColorText) makeColorChooser(currentColorText, "Current Place In Average");
        else if(source == fastestColorText) makeColorChooser(fastestColorText, "Fastest Time In Average");
        else if(source == slowestColorText) makeColorChooser(slowestColorText, "Slowest Time In Average");
    }

//**********************************************************************************************************************

    private boolean makeColorChooser(JTextArea area, String s){
        Color newColor = JColorChooser.showDialog(this, "Choose Color for "+s, area.getBackground());
        if(newColor != null){
            area.setBackground(newColor);
            return true;
        }
        else
            return false;
    }

//**********************************************************************************************************************

    // this is for the ColorListener interface
    public void faceClicked(ScramblePanel scramblePanel, int face, Color[] puzzleColors, String s){
        Color newColor = JColorChooser.showDialog(this, s, puzzleColors[face]);
        if(newColor != null){
            puzzleColors[face] = newColor;
            scramblePanel.updateScreen();
        }
    }

//**********************************************************************************************************************

    public void mouseReleased(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private OptionsListener optionsListener;

    public static interface OptionsListener{
        public abstract void optionsCallback();
    }

    public void addOptionsListener(OptionsListener optionsListener){
        this.optionsListener = optionsListener;
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    // to override key ordering, so that it's alphabetical on stores
    public static class SortedProperties extends Properties{
        public Enumeration<Object> keys(){
            Enumeration<Object> keysEnum = super.keys();
            Vector<String> keyStrings = new Vector<String>();

            while(keysEnum.hasMoreElements())
                keyStrings.add((String)keysEnum.nextElement());
            Collections.sort(keyStrings);

            Vector<Object> keyObjects = new Vector<Object>(keyStrings);
            return keyObjects.elements();
        }
    }
}
