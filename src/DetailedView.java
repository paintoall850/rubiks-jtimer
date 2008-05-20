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
import java.util.*;
import java.io.*;
import javax.swing.border.Border;

public class DetailedView extends JFrame implements ActionListener, Constants{
    String printToWindow;
    JButton saveButton;

    public DetailedView(String windowTitle, String printToWindow, Color textBackgrColor){
        this.printToWindow = printToWindow;

        // configure Contentpane
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        // configure JFrame
        setTitle(windowTitle);
        setSize(625, 340);
        setIconImage((new ImageIcon(getClass().getResource("Cow.gif"))).getImage());
        setResizable(false);

        // center frame on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int appWidth = getSize().width, appHeight = getSize().height;
        setLocation((screenSize.width-appWidth)/2, (screenSize.height-appHeight)/2);

        // main textArea
        JTextArea window = new JTextArea();
        window.setEditable(false);
        window.setFont(new Font("Serif", Font.PLAIN, 14));
        window.setBackground(textBackgrColor);
        JScrollPane scrollPane = new JScrollPane(window);
        scrollPane.setBorder(blackLine);
        scrollPane.setBounds(10,10,600,260);
        contentPane.add(scrollPane);
        window.setText(printToWindow);
        window.setCaretPosition(0);

        // save Button
        saveButton = new JButton("Save " + windowTitle);
        saveButton.addActionListener(this);
        saveButton.setBounds(10,280,600,20);
        contentPane.add(saveButton);
    } // end constructor

    public void actionPerformed(ActionEvent e){
        Object source = e.getSource();

        if(source == saveButton)
            saveToFile();
    } // end actionPerformed

    private void saveToFile(){
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new TextFileFilter());
        fc.setAcceptAllFileFilterUsed(false);

        int userChoice = fc.showSaveDialog(DetailedView.this);
        if(userChoice == JFileChooser.APPROVE_OPTION){
            try{
                FileWriter out = new FileWriter(new File((fc.getSelectedFile())+".txt"));
                out.write(printToWindow);
                out.close();
            } catch(IOException e){
                JOptionPane.showMessageDialog(this,"There was an error saving. You may not have write permissions.");
            }
        }
    } // end saveToFile
}
