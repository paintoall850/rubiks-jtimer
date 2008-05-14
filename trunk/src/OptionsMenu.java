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
import java.io.*;
import java.util.*;
import javax.swing.border.Border;

public class OptionsMenu extends JFrame implements ActionListener, MouseListener, Constants{
    private static final String FACE_NAMES[] = {"Front","Back","Left","Right","Down","Up"};

    public String puzzleX, countdownX;
    public boolean showResetConfirmX, showMinutesX;
    public Color countdownColorX, timerColorX, textBackgrColorX, currentColorX, fastestColorX, slowestColorX;
    public Color[] faceColorsX = new Color[6];
    public String averageViewFormatX, sessionViewFormatX;
/*
    class OptionsObj{
        public String puzzleX, countdownX;
        public boolean showResetConfirmX, showMinutesX;
        public Color countdownColorX, timerColorX, textBackgrColorX, currentColorX, fastestColorX, slowestColorX;
        public Color[] faceColorsX = new Color[6];
        public String averageViewFormatX, sessionViewFormatX;
    }
*/
    private Standalone myStandalone;
    JTabbedPane tabs;
    JPanel generalTab, colorTab, bestTab, sessionTab;
    JButton saveButton, resetButton, cancelButton;
    JLabel puzzleLabel, countdownLabel, averageSyntaxLabel, sessionSyntaxLabel, startupLabel, colorLabel;
    JLabel countdownCLabel, timerCLabel, textBackgrCLabel, currentCLabel, fastestCLabel, slowestCLabel;
    JLabel[] faceCLabels = new JLabel[6];
    JLabel faceColorLabel, previewLabel;

    JComboBox puzzleCombo, countdownCombo;
    JCheckBox confirmBox, showMinutesBox;
    JTextArea countdownColorText, timerColorText, textBackgrColorText, fastestColorText, slowestColorText, currentColorText;
    JTextArea[] faceColorTexts = new JTextArea[6];
    JTextArea averageText, sessionText;
    JTextArea[][][] ScramblePreview;

//**********************************************************************************************************************

    public OptionsMenu(Standalone standalone){
        // configure Contentpane
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        // configure JFrame
        setTitle("Options for Rubik's JTimer (stored in rjt.conf)");
        setSize(604, 325);
        setIconImage((new ImageIcon(getClass().getResource("Cow.gif"))).getImage());
        setResizable(false);

        // center frame on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int appWidth = getSize().width, appHeight = getSize().height;
        setLocation((screenSize.width-appWidth)/2, (screenSize.height-appHeight)/2);

        myStandalone = standalone;

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

        for(int side=0; side<6; side++){
            faceCLabels[side] = new JLabel(FACE_NAMES[side]);
            faceColorTexts[side] = new JTextArea();
            faceColorTexts[side].setEditable(false);
            faceColorTexts[side].setBorder(blackLine);
        }

        faceColorLabel = new JLabel();
        faceColorLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Scramble View Face Colors"));
        previewLabel = new JLabel();
        previewLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Preview"));

        ScramblePreview = new JTextArea[6][3][3];

        for(int side=0; side<6; side++)
            for(int i=0; i<3; i++)
                for(int j=0; j<3; j++){
                    ScramblePreview[side][i][j] = new JTextArea();
                    ScramblePreview[side][i][j].setEditable(false);
                    ScramblePreview[side][i][j].setBorder(blackLine);
                }

        startupLabel = new JLabel();
        startupLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Start-up Options"));
        colorLabel = new JLabel();
        colorLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Color Options"));
        saveButton = new JButton("Save Options");
        resetButton = new JButton("Reset All Options");
        cancelButton = new JButton("Cancel");
        puzzleLabel = new JLabel("Puzzle:");
        puzzleCombo = new JComboBox(puzzleChoices);
        countdownLabel = new JLabel("Countdown:");
        countdownCombo = new JComboBox(countdownChoices);

        averageSyntaxLabel = new JLabel("<HTML>%A - Average<br>%D - Standard Deviation<br>%F - Fastest Time<br>%I - Times And Scrambles<br>%O - Times Only<br>%T - Date And Time<br>%S - Slowest Time</HTML>");
        averageSyntaxLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Syntax"));
        sessionSyntaxLabel = new JLabel("<HTML>%A - Average<br>%C - Cubes Solved<br>%D - Standard Deviation<br>%F - Fastest Time<br>%I - Times And Scrambles<br>%O - Times Only<br>%P - Number Of Pops<br>%S - Slowest Time<br>%T - Date And Time</HTML>");
        sessionSyntaxLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Syntax"));
        confirmBox = new JCheckBox("Session Reset Warning Window");
        showMinutesBox = new JCheckBox("Use mm:ss.xx Format");
        averageText = new JTextArea();
        JScrollPane averageScrollPane = new JScrollPane(averageText);
        averageScrollPane.setBorder(blackLine);
        sessionText = new JTextArea();
        JScrollPane sessionScrollPane = new JScrollPane(sessionText);
        sessionScrollPane.setBorder(blackLine);

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
        colorTab = new JPanel();
        colorTab.setLayout(null);
        colorTab.add(faceColorLabel);
        colorTab.add(previewLabel);
        for(int side=0; side<6; side++){
            colorTab.add(faceColorTexts[side]);
            colorTab.add(faceCLabels[side]);
            for(int i=0; i<3; i++)
                for(int j=0; j<3; j++)
                    colorTab.add(ScramblePreview[side][i][j]);
        }
        bestTab = new JPanel();
        bestTab.setLayout(null);
        bestTab.add(averageSyntaxLabel);
        bestTab.add(averageScrollPane);
        sessionTab = new JPanel();
        sessionTab.setLayout(null);
        sessionTab.add(sessionSyntaxLabel);
        sessionTab.add(sessionScrollPane);
        tabs = new JTabbedPane();
        tabs.add(generalTab, "General Options");
        tabs.add(colorTab, "Color Scheme");
        tabs.add(bestTab, "Best Average Output");
        tabs.add(sessionTab, "Session Times Output");

        tabs.setBounds(10,5,679-100,240);
        saveButton.setBounds(10,255,186,30);
        resetButton.setBounds(186+20,255,186,30);
        cancelButton.setBounds(2*186+30,255,186,30);

        puzzleLabel.setBounds(30,25,80,20);
        puzzleCombo.setBounds(30,45,80,20);
        countdownLabel.setBounds(120,25,80,20);
        countdownCombo.setBounds(120,45,80,20);
        confirmBox.setBounds(30,75,230,20);
        showMinutesBox.setBounds(30,105,230,20);

        averageScrollPane.setBounds(12,10,358,183);
        sessionScrollPane.setBounds(12,10,358,183);
        averageSyntaxLabel.setBounds(378,5,180,190);
        sessionSyntaxLabel.setBounds(378,5,180,190);
        startupLabel.setBounds(10,5,319-50,190);
        colorLabel.setBounds(339-50,5,319-50,190);

        countdownColorText.setBounds(359-50,30,20,20);
        countdownCLabel.setBounds(384-50,30,100,20);
        timerColorText.setBounds(359-50,60,20,20);
        timerCLabel.setBounds(384-50,60,100,20);
        textBackgrColorText.setBounds(359-50,90,20,20);
        textBackgrCLabel.setBounds(384-50,90,100,20);
        currentColorText.setBounds(359+120-50,30,20,20);
        currentCLabel.setBounds(384+120-50,30,200,20);
        fastestColorText.setBounds(359+120-50,60,20,20);
        fastestCLabel.setBounds(384+120-50,60,200,20);
        slowestColorText.setBounds(359+120-50,90,20,20);
        slowestCLabel.setBounds(384+120-50,90,200,20);

        faceColorLabel.setBounds(10,5,319-50,190);
        previewLabel.setBounds(339-50,5,319-50,190);

        faceColorTexts[0].setBounds(110-20,90,20,20);
        faceCLabels[0].setBounds(110+25-20,90,60,20);
        faceColorTexts[1].setBounds(230-20,90,20,20);
        faceCLabels[1].setBounds(230+25-20,90,60,20);
        faceColorTexts[2].setBounds(50-20,90,20,20);
        faceCLabels[2].setBounds(50+25-20,90,60,20);
        faceColorTexts[3].setBounds(170-20,90,20,20);
        faceCLabels[3].setBounds(170+25-20,90,60,20);
        faceColorTexts[4].setBounds(110-20,115,20,20);
        faceCLabels[4].setBounds(110+25-20,115,60,20);
        faceColorTexts[5].setBounds(110-20,65,20,20);
        faceCLabels[5].setBounds(110+25-20,65,60,20);

        int x = -75, y = 5;
        setFaceBounds(ScramblePreview[0], 3, 450+x, 75+y, 15);
        setFaceBounds(ScramblePreview[1], 3, 550+x, 75+y, 15);
        setFaceBounds(ScramblePreview[2], 3, 400+x, 75+y, 15);
        setFaceBounds(ScramblePreview[3], 3, 500+x, 75+y, 15);
        setFaceBounds(ScramblePreview[4], 3, 450+x, 125+y, 15);
        setFaceBounds(ScramblePreview[5], 3, 450+x, 25+y, 15);

        contentPane.add(tabs);
        contentPane.add(saveButton);
        contentPane.add(resetButton);
        contentPane.add(cancelButton);

        saveButton.addActionListener(this);
        resetButton.addActionListener(this);
        cancelButton.addActionListener(this);

        countdownColorText.addMouseListener(this);
        timerColorText.addMouseListener(this);
        textBackgrColorText.addMouseListener(this);
        currentColorText.addMouseListener(this);
        fastestColorText.addMouseListener(this);
        slowestColorText.addMouseListener(this);
        for(int side=0; side<6; side++)
            faceColorTexts[side].addMouseListener(this);

        loadOptions();
    } // end constructor

//**********************************************************************************************************************

    public void actionPerformed(ActionEvent e){
        Object source = e.getSource();

        if(source == saveButton){
            saveOptions();
            myStandalone.OptionsToGUI();//Standalone.loadOptions();
            setVisible(false);//this.dispose();
        } else if(source == cancelButton){
            setVisible(false);//this.dispose();
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
        for(int side=0; side<6; side++)
            faceColorsX[side] = faceColorTexts[side].getBackground();
        averageViewFormatX = averageText.getText();
        sessionViewFormatX = sessionText.getText();

        String printToFile = "";
        printToFile = printToFile + puzzleX + "~";
        printToFile = printToFile + countdownX + "~";
        printToFile = printToFile + showResetConfirmX + "~";
        printToFile = printToFile + showMinutesX + "~";
        printToFile = printToFile + countdownColorX.getRGB() + "~";
        printToFile = printToFile + timerColorX.getRGB() + "~";
        printToFile = printToFile + textBackgrColorX.getRGB() + "~";
        printToFile = printToFile + currentColorX.getRGB() + "~";
        printToFile = printToFile + fastestColorX.getRGB() + "~";
        printToFile = printToFile + slowestColorX.getRGB() + "~";
        for(int side=0; side<6; side++)
            printToFile = printToFile + faceColorsX[side].getRGB() + "~";
        printToFile = printToFile + averageViewFormatX + "~";
        printToFile = printToFile + sessionViewFormatX;

        try{
            FileWriter fw = new FileWriter("rjt.conf");
            BufferedWriter out = new BufferedWriter(fw);
            out.write(printToFile);
            out.close();
        } catch(IOException g){
            JOptionPane.showMessageDialog(this,"There was an error saving. You may not have write permissions.");
        }
    }

//**********************************************************************************************************************

    public void loadOptions(){
        String input = "";
        try{
            FileReader fr = new FileReader("rjt.conf");
            BufferedReader in = new BufferedReader(fr);
            String read;
            while((read = in.readLine()) != null)
                input = input + read + "\n";
            in.close();
            input = input.substring(0, input.length()-1);

            StringTokenizer st = new StringTokenizer(input, "~");
            puzzleX = st.nextToken();
            countdownX = st.nextToken();
            showResetConfirmX = st.nextToken().equalsIgnoreCase("true");
            showMinutesX = st.nextToken().equalsIgnoreCase("true");
            countdownColorX = new Color(Integer.parseInt(st.nextToken()));
            timerColorX = new Color(Integer.parseInt(st.nextToken()));
            textBackgrColorX = new Color(Integer.parseInt(st.nextToken()));
            currentColorX = new Color(Integer.parseInt(st.nextToken()));
            fastestColorX = new Color(Integer.parseInt(st.nextToken()));
            slowestColorX = new Color(Integer.parseInt(st.nextToken()));
            for(int side=0; side<6; side++)
                faceColorsX[side] = new Color(Integer.parseInt(st.nextToken()));
            averageViewFormatX = st.nextToken();
            sessionViewFormatX = st.nextToken();

            OptionsToGUI();
        } catch(IOException g){
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
        faceColorsX[0] = new Color(255,255,255);
        faceColorsX[1] = new Color(255,255,0);
        faceColorsX[2] = new Color(255,0,0);
        faceColorsX[3] = new Color(255,128,0);
        faceColorsX[4] = new Color(0,0,255);
        faceColorsX[5] = new Color(0,196,0);
        averageViewFormatX = "----- Rubik's JTimer Best Average for %T -----\r\n\r\nAverage: %A\r\n\r\nFastest Time: %F\r\nSlowest Time: %S\r\nStandard Deviation: %D\r\n\r\nIndividual Times:\r\n%I";
        sessionViewFormatX = "----- Rubik's JTimer Session Statistics for %T -----\r\n\r\nCubes Solved: %C\r\nTotal Pops: %P\r\nAverage: %A\r\n\r\nFastest Time: %F\r\nSlowest Time: %S\r\nStandard Deviation: %D\r\n\r\nIndividual Times:\r\n%I";

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
        for(int side=0; side<6; side++)
            faceColorTexts[side].setBackground(faceColorsX[side]);
        updateScramblePreview();
        averageText.setText(averageViewFormatX); averageText.setCaretPosition(0);
        sessionText.setText(sessionViewFormatX); sessionText.setCaretPosition(0);
    } // end OptionsToGUI

//**********************************************************************************************************************

    public void mouseClicked(MouseEvent e){
        Object source = e.getSource();

        if(source == countdownColorText){
            Color newColor = JColorChooser.showDialog(this,"Choose Color for Countdown Display",countdownColorText.getBackground());
            if(newColor != null) countdownColorText.setBackground(newColor);
        } else if(source == timerColorText){
            Color newColor = JColorChooser.showDialog(this,"Choose Color for Timing Display",timerColorText.getBackground());
            if(newColor != null) timerColorText.setBackground(newColor);
        } else if(source == textBackgrColorText){
            Color newColor = JColorChooser.showDialog(this,"Choose Color for Alg Background",textBackgrColorText.getBackground());
            if(newColor != null) textBackgrColorText.setBackground(newColor);
        } else if(source == currentColorText){
            Color newColor = JColorChooser.showDialog(this,"Choose Color for Current Place In Average",currentColorText.getBackground());
            if(newColor != null) currentColorText.setBackground(newColor);
        } else if(source == fastestColorText){
            Color newColor = JColorChooser.showDialog(this,"Choose Color for Fastest Time In Average",fastestColorText.getBackground());
            if(newColor != null) fastestColorText.setBackground(newColor);
        } else if(source == slowestColorText){
            Color newColor = JColorChooser.showDialog(this,"Choose Color for Slowest Time In Average",slowestColorText.getBackground());
            if(newColor != null) slowestColorText.setBackground(newColor);
        } else if(source == faceColorTexts[0]){
            Color newColor = JColorChooser.showDialog(this,"Choose Color for Front Face",faceColorTexts[0].getBackground());
            if(newColor != null){faceColorTexts[0].setBackground(newColor); updateScramblePreview();}
        } else if(source == faceColorTexts[1]){
            Color newColor = JColorChooser.showDialog(this,"Choose Color for Back Face",faceColorTexts[1].getBackground());
            if(newColor != null){faceColorTexts[1].setBackground(newColor); updateScramblePreview();}
        } else if(source == faceColorTexts[2]){
            Color newColor = JColorChooser.showDialog(this,"Choose Color for Left Face",faceColorTexts[2].getBackground());
            if(newColor != null){faceColorTexts[2].setBackground(newColor); updateScramblePreview();}
        } else if(source == faceColorTexts[3]){
            Color newColor = JColorChooser.showDialog(this,"Choose Color for Right Face",faceColorTexts[3].getBackground());
            if(newColor != null){faceColorTexts[3].setBackground(newColor); updateScramblePreview();}
        } else if(source == faceColorTexts[4]){
            Color newColor = JColorChooser.showDialog(this,"Choose Color for Down Face",faceColorTexts[4].getBackground());
            if(newColor != null){faceColorTexts[4].setBackground(newColor); updateScramblePreview();}
        } else if(source == faceColorTexts[5]){
            Color newColor = JColorChooser.showDialog(this,"Choose Color for Up Face",faceColorTexts[5].getBackground());
            if(newColor != null){faceColorTexts[5].setBackground(newColor); updateScramblePreview();}
        }
    }

//**********************************************************************************************************************

    public void mouseReleased(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}

//**********************************************************************************************************************

    private void updateScramblePreview(){
        for(int side=0; side<6; side++)
            for(int i=0; i<3; i++)
                for(int j=0; j<3; j++)
                    ScramblePreview[side][i][j].setBackground(faceColorTexts[side].getBackground());
    }
}
