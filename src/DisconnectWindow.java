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

public class DisconnectWindow extends JFrame implements ActionListener{
    private JButton okButton, viewLocalSessionHistoryButton, viewRemoteSessionHistoryButton;
    private NetcubeMode host;
    private String local, remote;
    
    /**
     * Displays a new DisconnectWindow with the given message. Uses the host's filechooser and options.
     * Need to pass the local and remote session strings otherwise reset() is called before displaying any
     * window.
     * @param host The calling instance of NetcubeMode
     * @param message The disconnect message
     * @param local The local session information String
     * @param remote The remote session information String
     */
    public DisconnectWindow(NetcubeMode host, String message, String local, String remote){
        super("Disconnected");

        //set up the window
        int width = 310;
        int height = 130;
        
        RJT_Utils.configureJFrame(this);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        RJT_Utils.centerJFrame(this, width, height);
        
        //set up local variables
        this.host = host;
        this.local = local;
        this.remote = remote;

        Container mainPane = this.getContentPane();
        mainPane.setLayout(null);
        
        //add the message text
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setText(message);
        label.setBounds(0,15,width,15);
        mainPane.add(label);
        
        //set up the buttons
        viewLocalSessionHistoryButton = new JButton("Your History");
        viewLocalSessionHistoryButton.addActionListener(this);
        viewLocalSessionHistoryButton.setBounds(10,40,width/2-15,25);
        viewRemoteSessionHistoryButton = new JButton("Their History");
        viewRemoteSessionHistoryButton.addActionListener(this);
        viewRemoteSessionHistoryButton.setBounds(width/2+5,40,width/2-15,25);
        okButton = new JButton("OK");
        okButton.addActionListener(this);
        okButton.setBounds(width/2-30,70,60,25);
        
        //add the buttons
        mainPane.add(okButton);
        mainPane.add(viewLocalSessionHistoryButton);
        mainPane.add(viewRemoteSessionHistoryButton);
       
        //show the window
        this.setVisible(true);
        okButton.requestFocus();
    }
    
    //handle actions
    public void actionPerformed(ActionEvent evt){
        Object source = evt.getSource();
        if(source == okButton){
            this.dispose();
        }
        if(source == viewLocalSessionHistoryButton){
            System.out.println(host.getLocalSessionView());
            DetailedView win = new DetailedView(host.fc, "Local Session Times", local, host.optionsBox.textBackgrColorX);
            win.setVisible(true);
        }
        if(source == viewRemoteSessionHistoryButton){
            System.out.println(host.getRemoteSessionView());
            DetailedView win = new DetailedView(host.fc, "Remote Session Times", remote, host.optionsBox.textBackgrColorX);
            win.setVisible(true);
        }
    }
}
