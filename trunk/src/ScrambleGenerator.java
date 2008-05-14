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

public class ScrambleGenerator extends JFrame implements ActionListener{
    JButton generateButton;
    JComboBox cubeCombo;
    JLabel puzzleLabel, numLabel;
    JTextField numText;
    JRadioButton formatPrint, formatImport;
    ButtonGroup radioGroup;
    ScrambleAlgorithm algGenerator;

    public ScrambleGenerator(){
        // configure Contentpane
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        algGenerator = new ScrambleAlgorithm();

        // configure JFrame
        setTitle("Scramble Generator");
        setSize(225,150);
        setIconImage((new ImageIcon(getClass().getResource("Cow.gif"))).getImage());
        setResizable(false);

        // center frame on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int appWidth = getSize().width, appHeight = getSize().height;
        setLocation((screenSize.width-appWidth)/2, (screenSize.height-appHeight)/2);

        puzzleLabel = new JLabel("Puzzle:");
        numLabel = new JLabel("# of Scrambles:");
        String[] cubeChoices = {"2x2x2","3x3x3","4x4x4","5x5x5"/*,"Pyraminx"*/,"Megaminx"};
        cubeCombo = new JComboBox(cubeChoices);
        cubeCombo.setSelectedItem("3x3x3");
        numText = new JTextField("13");
        generateButton = new JButton("Generate Scrambles");
        generateButton.addActionListener(this);
        formatPrint = new JRadioButton("Format Output for Printing");
        formatImport = new JRadioButton("Format Output for Importing");
        radioGroup = new ButtonGroup();

        puzzleLabel.setBounds(10,5,70,20);
        cubeCombo.setBounds(10,25,70,20);
        numLabel.setBounds(90,5,120,20);
        numText.setBounds(90,25,120,20);
        formatPrint.setBounds(10,50,200,20);
        formatImport.setBounds(10,70,200,20);
        generateButton.setBounds(10,95,200,20);

        contentPane.add(puzzleLabel);
        contentPane.add(numLabel);
        contentPane.add(cubeCombo);
        contentPane.add(numText);
        contentPane.add(generateButton);
        contentPane.add(formatPrint);
        contentPane.add(formatImport);

        radioGroup.add(formatPrint);
        radioGroup.add(formatImport);

        formatPrint.setSelected(true);
    } // end contructor

    public void actionPerformed(ActionEvent e){
        Object source = e.getSource();

        if(source == generateButton){
            int numberOfScrambles = 0;
            try{
                numberOfScrambles = Integer.parseInt(numText.getText());
            } catch(NumberFormatException f){
                JOptionPane.showMessageDialog(this,"Number of scrambles is invalid. Please enter an integer to continue.");
                return;
            }

            if(numberOfScrambles < 1){
                JOptionPane.showMessageDialog(this,"Number of scrambles is invalid. Please enter an integer to continue.");
                return;
            }

            String printToFile = "";

            if(formatPrint.isSelected()){
                printToFile = "----- Rubik's JTimer Generated Scrambles -----\r\n\r\nCube Type: " + cubeCombo.getSelectedItem() + "\r\nNumber of Scrambles: " + numberOfScrambles + "\r\n\r\nScrambles:\r\n";
                for(int i=0; i<numberOfScrambles; i++)
                    printToFile = printToFile + (i+1) + ")          " + algGenerator.generateAlg(cubeCombo.getSelectedItem()+"") + "\r\n";
            } else {
                for(int i=0; i<numberOfScrambles; i++)
                    printToFile = printToFile + algGenerator.generateAlg(cubeCombo.getSelectedItem()+"") + "\r\n";
            }

            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new TextFileFilter());
            fc.setAcceptAllFileFilterUsed(false);

            int userChoice = fc.showSaveDialog(ScrambleGenerator.this);
            if(userChoice == JFileChooser.APPROVE_OPTION){
                try{
                    FileWriter fr = new FileWriter(new File((fc.getSelectedFile())+".txt"));
                    BufferedWriter out = new BufferedWriter(fr);
                    out.write(printToFile);
                    out.close();
                } catch(IOException g){JOptionPane.showMessageDialog(this,"There was an error saving. You may not have write permissions.");}
            }
        }
    } // end actionPerformed
}
