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

public class ScrambleGenerator extends JFrame implements ActionListener, Constants{
    JButton generateButton;
    JComboBox puzzleCombo;
    JLabel puzzleLabel, numLabel;
    JTextField numText;
    JRadioButton formatPrint, formatImport;
    ButtonGroup radioGroup;
    ScrambleAlg algGenerator;

    public ScrambleGenerator(){
        // configure Contentpane
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        algGenerator = new ScrambleAlg();

        // configure JFrame
        setTitle("Scramble Generator");
        centerFrameOnScreen(225, 155);
        setIconImage((new ImageIcon(getClass().getResource("Cow.gif"))).getImage());
        setResizable(false);

        puzzleLabel = new JLabel("Puzzle:");
        puzzleCombo = new JComboBox(puzzleChoices);
        //puzzleCombo.setSelectedItem("3x3x3");
        numLabel = new JLabel("# of Scrambles:");
        numText = new JTextField("13");
        generateButton = new JButton("Generate Scrambles");
        generateButton.addActionListener(this);
        formatPrint = new JRadioButton("Format Output for Printing");
        formatImport = new JRadioButton("Format Output for Importing");
        radioGroup = new ButtonGroup();

        puzzleLabel.setBounds(10,5,90,20);
        puzzleCombo.setBounds(10,25,90,20);
        numLabel.setBounds(110,5,100,20);
        numText.setBounds(110,25,100,20);
        formatPrint.setBounds(10,50,200,20);
        formatImport.setBounds(10,70,200,20);
        generateButton.setBounds(10,95,200,20);

        contentPane.add(puzzleLabel);
        contentPane.add(numLabel);
        contentPane.add(puzzleCombo);
        contentPane.add(numText);
        contentPane.add(generateButton);
        contentPane.add(formatPrint);
        contentPane.add(formatImport);

        radioGroup.add(formatPrint);
        radioGroup.add(formatImport);

        formatPrint.setSelected(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
    } // end contructor

//**********************************************************************************************************************

    private void centerFrameOnScreen(int width, int height){
        setSize(width, height);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int appWidth = getSize().width, appHeight = getSize().height;
        setLocation((screenSize.width-appWidth)/2, (screenSize.height-appHeight)/2);
    }

//**********************************************************************************************************************

    public void actionPerformed(ActionEvent e){
        Object source = e.getSource();

        if(source == generateButton){
            int numberOfScrambles = 0;
            try{
                numberOfScrambles = Integer.parseInt(numText.getText());
            } catch(NumberFormatException ex){
                JOptionPane.showMessageDialog(this, "Number of scrambles is invalid. Please enter a positive integer to continue.");
                return;
            }

            if(numberOfScrambles < 1){
                JOptionPane.showMessageDialog(this, "Number of scrambles is invalid. Please enter a positive integer to continue.");
                return;
            }

            String printToFile = "";
            if(formatPrint.isSelected()){
                printToFile = "----- " + APP_TITLE + " Generated Scrambles -----\n\nCube Type: " + puzzleCombo.getSelectedItem() + "\nNumber of Scrambles: " + numberOfScrambles + "\n\nScrambles:\n";
                for(int i=0; i<numberOfScrambles; i++){
                    String newAlg = algGenerator.generateAlg(puzzleCombo.getSelectedItem()+"");
                    newAlg = newAlg.replaceAll(ALG_BREAK, " ");
                    printToFile += (i+1) + ")          " + newAlg + "\n";
                }
            } else {
                for(int i=0; i<numberOfScrambles; i++){
                    String newAlg = algGenerator.generateAlg(puzzleCombo.getSelectedItem()+"");
                    //newAlg = alg.replaceAll(ALG_BREAK, " ");
                    printToFile += newAlg + "\n";
                }
            }
            printToFile = printToFile.replaceAll("\n", System.getProperty("line.separator"));

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
                } catch(IOException ex){
                    JOptionPane.showMessageDialog(this, "There was an error saving. You may not have write permissions.");
                }
            }

            this.setVisible(false);
        }
    } // end actionPerformed
}
