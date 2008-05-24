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

public class OptionsMenu extends JFrame implements ActionListener, MouseListener, Constants{
    private static final String FACE_NAMES[] = {"Front", "Back", "Left", "Right", "Down", "Up"};
    private static final String FILENAME = "rjt.properties";

    // Complete List of Save-able options
    public String puzzleX, countdownX;
    public boolean showResetConfirmX, showMinutesX;
    public Color countdownColorX, timerColorX, textBackgrColorX, currentColorX, fastestColorX, slowestColorX;
    public Color[] cubeColorsX = new Color[6];
    public Color[] pyraminxColorsX = new Color[4];
    public Color[] megaminxColorsX = new Color[12];
    public String averageViewFormatX, sessionViewFormatX;

    private Standalone myStandalone;
    JTabbedPane tabs;
    JPanel generalTab, cubeSchemeTab, minxSchemeTab, sessionTab, bestTab;
    JButton saveButton, resetButton, cancelButton;
    JLabel puzzleLabel, countdownLabel, averageSyntaxLabel, sessionSyntaxLabel, startupLabel, colorLabel;
    JLabel countdownCLabel, timerCLabel, textBackgrCLabel, currentCLabel, fastestCLabel, slowestCLabel;
    JLabel[] faceCLabels = new JLabel[6];
    JLabel faceColorLabel, previewLabel;//, pyraminxViewLabel, megaminxViewLabel;
    ScramblePane pyraminxView, megaminxView;

    JComboBox puzzleCombo, countdownCombo;
    JCheckBox confirmBox, showMinutesBox;
    JTextArea countdownColorText, timerColorText, textBackgrColorText, fastestColorText, slowestColorText, currentColorText;
    JTextArea[] faceColorTexts = new JTextArea[6];
    JTextArea averageText, sessionText;
    JScrollPane averageScrollPane, sessionScrollPane;
    JTextArea[][][] ScramblePreview;

//**********************************************************************************************************************

    public OptionsMenu(Standalone standalone){
        // configure Contentpane
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        // configure JFrame
        setTitle("Options for " + APP_TITLE + " (stored in " + FILENAME + ")");
        centerFrameOnScreen(604, 325);
        setIconImage((new ImageIcon(getClass().getResource("Cow.gif"))).getImage());
        setResizable(false);

        myStandalone = standalone;

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

        faceColorLabel = new JLabel();
        faceColorLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Color Scheme for Cubes"));
        previewLabel = new JLabel();
        previewLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Preview for 3x3"));

        for(int face=0; face<6; face++){
            faceCLabels[face] = new JLabel(FACE_NAMES[face]);
            faceColorTexts[face] = new JTextArea();
            faceColorTexts[face].setEditable(false);
            faceColorTexts[face].setBorder(blackLine);
        }

        ScramblePreview = new JTextArea[6][3][3];
        for(int face=0; face<6; face++)
            for(int i=0; i<3; i++)
                for(int j=0; j<3; j++){
                    ScramblePreview[face][i][j] = new JTextArea();
                    ScramblePreview[face][i][j].setEditable(false);
                    ScramblePreview[face][i][j].setFocusable(false);
                    ScramblePreview[face][i][j].setBorder(blackLine);
                }

        //pyraminxViewLabel = new JLabel();
        //pyraminxViewLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Pyraminx Preview"));
        pyraminxView = new ScramblePane(269+3,200);// was 282,235);
        pyraminxView.setLayout(null);
        pyraminxView.setBorder(BorderFactory.createTitledBorder(theBorder, "Pyraminx Preview"));

        //megaminxViewLabel = new JLabel();
        //megaminxViewLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Megaminx Preview"));
        megaminxView = new ScramblePane(269+3,200);// was 282,235);
        megaminxView.setLayout(null);
        megaminxView.setBorder(BorderFactory.createTitledBorder(theBorder, "Megaminx Preview"));

        sessionSyntaxLabel = new JLabel("<html>%A - Average<br>%C - Number of Solves<br>%D - Standard Deviation<br>%F - Fastest Time<br>%I - Times and Scrambles<br>%O - Times Only<br>%P - Number of Pops<br>%S - Slowest Time<br>%T - Date and Time<br>%Z - Puzzle Name</html>");
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

        saveButton = new JButton("Save Options");
        resetButton = new JButton("Reset All Options");
        cancelButton = new JButton("Cancel");

        // call big add tabs and content function
        addStuffToTabs();

        // call big setBounds function
        setTheBounds();

        // add Content
        contentPane.add(tabs);
        contentPane.add(saveButton);
        contentPane.add(resetButton);
        contentPane.add(cancelButton);

        // add ActionListeners
        addTheActionListeners();

        loadOptions();
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

        tabs.setBounds(10,5,579,240);
        saveButton.setBounds(10,255,186,30);
        resetButton.setBounds(186+20,255,186,30);
        cancelButton.setBounds(2*186+30,255,186,30);

        startupLabel.setBounds(10,5,269+3,200);
        colorLabel.setBounds(289+3,5,269+3,200);
        faceColorLabel.setBounds(10,5,269+3,200);
        previewLabel.setBounds(289+3,5,269+3,200);
        sessionScrollPane.setBounds(12,10,358+6,193);
        averageScrollPane.setBounds(12,10,358+6,193);
        sessionSyntaxLabel.setBounds(378+6,5,180,200);
        averageSyntaxLabel.setBounds(378+6,5,180,200);

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

        faceColorTexts[0].setBounds(90,95,20,20);
        faceCLabels[0].setBounds(90+25,95,60,20);
        faceColorTexts[1].setBounds(210,95,20,20);
        faceCLabels[1].setBounds(210+25,95,60,20);
        faceColorTexts[2].setBounds(30,95,20,20);
        faceCLabels[2].setBounds(30+25,95,60,20);
        faceColorTexts[3].setBounds(150,95,20,20);
        faceCLabels[3].setBounds(150+25,95,60,20);
        faceColorTexts[4].setBounds(90,120,20,20);
        faceCLabels[4].setBounds(90+25,120,60,20);
        faceColorTexts[5].setBounds(90,70,20,20);
        faceCLabels[5].setBounds(90+25,70,60,20);

        int x = -71, y = 9;
        setFaceBounds(ScramblePreview[0], 3, 450+x, 75+y, 15);
        setFaceBounds(ScramblePreview[1], 3, 550+x, 75+y, 15);
        setFaceBounds(ScramblePreview[2], 3, 400+x, 75+y, 15);
        setFaceBounds(ScramblePreview[3], 3, 500+x, 75+y, 15);
        setFaceBounds(ScramblePreview[4], 3, 450+x, 125+y, 15);
        setFaceBounds(ScramblePreview[5], 3, 450+x, 25+y, 15);

        //pyraminxViewLabel.setBounds(10,5,269+3,200); // copy below
        pyraminxView.setBounds(10,5,269+3,200); // was 282,235);
        //megaminxViewLabel.setBounds((579-15)-(269+3),5,269+3,200); // copy below
        megaminxView.setBounds((579-15)-(269+3),5,269+3,200); // was 282,235);

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

        cubeSchemeTab = new JPanel();
        cubeSchemeTab.setLayout(null);
        cubeSchemeTab.add(faceColorLabel);
        cubeSchemeTab.add(previewLabel);
        for(int face=0; face<6; face++){
            cubeSchemeTab.add(faceColorTexts[face]);
            cubeSchemeTab.add(faceCLabels[face]);
            for(int i=0; i<3; i++)
                for(int j=0; j<3; j++)
                    cubeSchemeTab.add(ScramblePreview[face][i][j]);
        }

        minxSchemeTab = new JPanel();
        minxSchemeTab.setLayout(null);
        //minxSchemeTab.add(pyraminxViewLabel);
        minxSchemeTab.add(pyraminxView);
        //minxSchemeTab.add(megaminxViewLabel);
        minxSchemeTab.add(megaminxView);

        bestTab = new JPanel();
        bestTab.setLayout(null);
        bestTab.add(averageSyntaxLabel);
        bestTab.add(averageScrollPane);

        sessionTab = new JPanel();
        sessionTab.setLayout(null);
        sessionTab.add(sessionSyntaxLabel);
        sessionTab.add(sessionScrollPane);

        tabs = new JTabbedPane();
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.add(generalTab, "General");
        tabs.add(cubeSchemeTab, "Cube Colors");
        //tabs.add(minxSchemeTab, "Pyraminx & Megaminx Colors");
        tabs.add(sessionTab, "Session Times");
        tabs.add(bestTab, "Best Average");
    }

//**********************************************************************************************************************

    private void addTheActionListeners(){

        saveButton.addActionListener(this);
        resetButton.addActionListener(this);
        cancelButton.addActionListener(this);

        countdownColorText.addMouseListener(this);
        timerColorText.addMouseListener(this);
        textBackgrColorText.addMouseListener(this);
        currentColorText.addMouseListener(this);
        fastestColorText.addMouseListener(this);
        slowestColorText.addMouseListener(this);

        for(int face=0; face<6; face++)
            faceColorTexts[face].addMouseListener(this);
    }

//**********************************************************************************************************************

    public void actionPerformed(ActionEvent e){
        Object source = e.getSource();

        if(source == saveButton){
            saveOptions();
            myStandalone.updateGUI();
            myStandalone.updateStatsX();
            this.setVisible(false); //this.dispose();
        } else if(source == cancelButton){
            this.setVisible(false); //this.dispose();
        } else if(source == resetButton){
            resetOptions();
            averageText.setCaretPosition(0);
            sessionText.setCaretPosition(0);
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
        for(int face=0; face<6; face++)
            cubeColorsX[face] = faceColorTexts[face].getBackground();
//        for(int face=0; face<4; face++)
//            pyraminxColorsX[face] = ???[face].getBackground();
//        for(int face=0; face<12; face++)
//            megaminxColorsX[face] = ???[face].getBackground();
        averageViewFormatX = averageText.getText();
        sessionViewFormatX = sessionText.getText();

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
        countdownColorX = Color.red;
        timerColorX = Color.blue;
        // yellow = (255,222,140), purple = (255,220,220), cyan = (140,255,222), Ocean = (200,221,242)
        textBackgrColorX = new Color(200,221,242);
        currentColorX = new Color(0,180,0);
        fastestColorX = Color.blue;
        slowestColorX = Color.red;

        cubeColorsX[0] = new Color(0,196,0); // green
        cubeColorsX[1] = new Color(0,0,255); // blue
        cubeColorsX[2] = new Color(255,128,0); // orange
        cubeColorsX[3] = new Color(255,0,0); // red
        cubeColorsX[4] = new Color(255,255,0); // yellow
        cubeColorsX[5] = new Color(255,255,255); // white

        pyraminxColorsX[0] = new Color(0,180,255); // powder blue
        pyraminxColorsX[1] = new Color(255,0,0); // red
        pyraminxColorsX[2] = new Color(255,255,0); // yellow
        pyraminxColorsX[3] = new Color(0,255,0); // bright green

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

        averageViewFormatX = "----- " + APP_TITLE + " Best Average for %T -----\n\nAverage: %A\n\nFastest Time: %F\nSlowest Time: %S\nStandard Deviation: %D\n\nIndividual Times:\n%I";
        sessionViewFormatX = "----- " + APP_TITLE + " Session Statistics for %T -----\n\nTotal Solves: %C\nTotal Pops: %P\nAverage: %A\n\nFastest Time: %F\nSlowest Time: %S\nStandard Deviation: %D\n\nIndividual Times:\n%I";

        OptionsToGUI();
    } // end resetOptions

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
        for(int face=0; face<6; face++)
            faceColorTexts[face].setBackground(cubeColorsX[face]);
        updateScramblePreview();
        averageText.setText(averageViewFormatX); averageText.setCaretPosition(0);
        sessionText.setText(sessionViewFormatX); sessionText.setCaretPosition(0);

        pyraminxView.setPyraminxColors(pyraminxColorsX);
        pyraminxView.newScramble("Pyraminx", "");
        megaminxView.setMegaminxColors(megaminxColorsX);
        megaminxView.newScramble("Megaminx", "");
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
        else if(source == faceColorTexts[0])
            {if(makeColorChooser(faceColorTexts[0], "Front Face")) updateScramblePreview();}
        else if(source == faceColorTexts[1])
            {if(makeColorChooser(faceColorTexts[1],  "Back Face")) updateScramblePreview();}
        else if(source == faceColorTexts[2])
            {if(makeColorChooser(faceColorTexts[2],  "Left Face")) updateScramblePreview();}
        else if(source == faceColorTexts[3])
            {if(makeColorChooser(faceColorTexts[3], "Right Face")) updateScramblePreview();}
        else if(source == faceColorTexts[4])
            {if(makeColorChooser(faceColorTexts[4],  "Down Face")) updateScramblePreview();}
        else if(source == faceColorTexts[5])
            {if(makeColorChooser(faceColorTexts[5],    "Up Face")) updateScramblePreview();}
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

    public void mouseReleased(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}

//**********************************************************************************************************************

    private void updateScramblePreview(){
        for(int face=0; face<6; face++)
            for(int i=0; i<3; i++)
                for(int j=0; j<3; j++)
                    ScramblePreview[face][i][j].setBackground(faceColorTexts[face].getBackground());
    }

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
