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
import java.net.*;
import java.io.*;
import javax.swing.text.*;
import java.applet.*;
import java.text.*;
import java.util.*;
import javax.swing.border.Border;

public class Client extends NetcubeMode{

//**********************************************************************************************************************

    public Client(OptionsBox optionsBox, ScrambleGenerator scrambleGenerator, InstructionScreen instructionScreen, AboutScreen aboutScreen){
        super(optionsBox, scrambleGenerator, instructionScreen, aboutScreen);

        // configure Contentpane
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        // configure JFrame
        setTitle(APP_TITLE + " Client");

        // GUI Object creation
        serverIpText.setText("127.0.0.1");
        connectButton.setText("Connect To Server");

        //puzzleCombo.setSelectedItem("3x3x3");
        //countdownCombo.setSelectedItem("15");
        startButton.setText("Stop Timer");

        // set bounds
        super.setTheBounds();

        // add to contentPane
        super.addTheContent(contentPane);
        contentPane.add(localStatusLabel);

        // add ActionListeners
        super.addActionListeners();
        usernameText.addActionListener(this);
        serverIpText.addActionListener(this);
        serverPortText.addActionListener(this);
        localStatusLabel.addActionListener(this);

        // GUI preperation
        super.prepGUI();
        puzzleCombo.setEnabled(false);
        countdownCombo.setEnabled(false);

        // hide GUI
        hideGUI();
    } // end constructor

//**********************************************************************************************************************

    public void actionPerformed(ActionEvent e){
        Object source = e.getSource();

        if(super.commonAction(source)){
            return;
        } else if(source == connectButton || source == usernameText || source == serverIpText || source == serverPortText){
            if(usernameText.getText().equals("")){
                JOptionPane.showMessageDialog(this, "Username should not be blank.");
                return;
            }
            startClientConnection();
        } else if(source == localStatusLabel){
            safePrint("R" + localStatusLabel.isSelected());
        } else if(source == startButton){
            //show on localTime and send the time to server
            stopTime = System.currentTimeMillis();
            startButton.setEnabled(false);
            popButton.setEnabled(false);
            localTimeLabel.setForeground(Color.blue);
            timerLabel.setForeground(Color.black);
            localTimeLabel.setText(RJT_Utils.ssxx_format((stopTime-startTime)/1000D));
            safePrint("N" + RJT_Utils.ssxx_format((stopTime-startTime)/1000D));

            //increment acceptsSincePOp
            acceptsSincePop++;
            localCubesSolved++;

            //if everyone is done, then stop the timer update stats
            if(!(remoteTime.equals("none"))){
                timerThread.cancel();
                timerLabel.setText("");
                //change buttons
                popButton.setEnabled(false);
                localStatusLabel.setSelected(false);
                localStatusLabel.setEnabled(true);
                sendMessageButton.setEnabled(true);
                chatText.setEnabled(true);
                localSessionDetailButton.setEnabled(true);
                remoteSessionDetailButton.setEnabled(true);
                if(localCubesSolved >= 12)
                    localAverageDetailButton.setEnabled(true);
                if(remoteCubesSolved >= 12)
                    remoteAverageDetailButton.setEnabled(true);
                //move to next solve
                updateStats();
            }
        } else if(source == popButton){
            //show on localTime and send the time to server
            acceptsSincePop = 0;
            localNumOfPops++;
            popButton.setText("Popped");
            startButton.setEnabled(false);
            popButton.setEnabled(false);
            localTimeLabel.setForeground(Color.blue);
            timerLabel.setForeground(Color.black);
            localTimeLabel.setText("POP");
            safePrint("N" + "POP");

            //if everyone is done, then stop the timer update stats
            if(!remoteTime.equals("none")){
                timerThread.cancel();
                timerLabel.setText("");
                //change buttons
                popButton.setEnabled(false);
                localStatusLabel.setSelected(false);
                localStatusLabel.setEnabled(true);
                sendMessageButton.setEnabled(true);
                chatText.setEnabled(true);
                localSessionDetailButton.setEnabled(true);
                remoteSessionDetailButton.setEnabled(true);
                if(localCubesSolved >= 12)
                    localAverageDetailButton.setEnabled(true);
                if(remoteCubesSolved >= 12)
                    remoteAverageDetailButton.setEnabled(true);
                // move to next solve
                updateStats();
            }
        }
    } // end actionPerformed

//**********************************************************************************************************************

    public void run(){
        try{
            while(true){
                String data = in.readLine();
                String prefix = data.substring(0, 1);
                String message = data.substring(1, data.length());
                performAction(prefix, message);
            }
        } catch(Exception ex){
            hideGUI();
            JOptionPane.showMessageDialog(this, "You have been disconnected from the server.");
            reset();
            connectButton.setText("Connect To Server");
            chatText.setText("");
            connectButton.setEnabled(true);
            usernameText.setEnabled(true);
            serverPortText.setEnabled(true);
            sendMessageButton.setEnabled(false);
            chatText.setEnabled(false);
        } finally {
            chatListener = null;
            validate();
            try{
                in.close();
                out.close();
                clientSocket.close();
            } catch(IOException ex){
                ex.printStackTrace();
            }
        }
    } // end run

//**********************************************************************************************************************

    private void performAction(String prefix, String data){
        if(prefix.equals("C")){ // Connect
            try{
                chatDoc.insertString(chatDoc.getLength(), remoteUsername + ": ", blueStyle);
                chatDoc.insertString(chatDoc.getLength(), data + "\n", blackStyle);
                chatPane.setCaretPosition(chatDoc.getLength());
                chatSound.play();
            } catch(BadLocationException ex){
                System.out.println(ex.getMessage());
            }
        } else if(prefix.equals("U")){ // pass Username
            remoteUsername = data;
            remoteTimeUsernameLabel.setBorder(BorderFactory.createTitledBorder(theBorder, remoteUsername + "'s Statistics"));
        } else if(prefix.equals("P")){ // Puzzle comboBox choice
            puzzleCombo.setSelectedItem(data);
        } else if(prefix.equals("T")){ // countdown [Time] comboBox choice
            countdownCombo.setSelectedItem(data);
        } else if(prefix.equals("S")){ // pass Scramble alg
            scrambleText.setFont(puzzleCombo.getSelectedItem() == "Megaminx" ? smAlgFont : lgAlgFont);
            newAlg = data;
            scrambleText.setText(newAlg.replaceAll(ALG_BREAK, "\n"));
            updateScramblePanel();
        } else if(prefix.equals("N")){ // pass finished time
            remoteTime = data;
            //if everyone is done, then stop the timer update stats
            if(!(localTimeLabel.getText().equals(""))){
                timerThread.cancel();
                timerLabel.setText("");
                //change buttons
                popButton.setEnabled(false);
                localStatusLabel.setSelected(false);
                localStatusLabel.setEnabled(true);
                sendMessageButton.setEnabled(true);
                chatText.setEnabled(true);
                localSessionDetailButton.setEnabled(true);
                remoteSessionDetailButton.setEnabled(true);
                if(localCubesSolved >= 12)
                    localAverageDetailButton.setEnabled(true);
                if(remoteCubesSolved >= 12)
                    remoteAverageDetailButton.setEnabled(true);
                // move to next solve
                updateStats();
            }
        } else if(prefix.equals("X")){ // start timing
            //change buttons and clear times
            remoteTime = "none";
            localTimeLabel.setText("");
            remoteTimeLabel.setText("");
            startButton.setEnabled(false);
            localStatusLabel.setEnabled(false);
            localSessionDetailButton.setEnabled(false);
            localAverageDetailButton.setEnabled(false);
            remoteSessionDetailButton.setEnabled(false);
            remoteAverageDetailButton.setEnabled(false);
            popButton.setEnabled(false);
            sendMessageButton.setEnabled(false);
            chatText.setEnabled(false);
            //start countdown / timer
            countdown = Integer.parseInt(countdownCombo.getSelectedItem()+"");
            timerLabel.setForeground(Color.red);
            timerThread = new java.util.Timer();
            timerThread.schedule(new RunCountdown(), 0, 1000);
        } else if(prefix.equals("I")){ // toggle Is-typing icon
            remoteIsTyping = !remoteIsTyping;
            if(remoteIsTyping) userIsTyping.setIcon(typeOn);
            else userIsTyping.setIcon(typeOff);
        }
    } // end performedAction

//**********************************************************************************************************************

    protected void hideGUI(){
        super.hideGUI();
        localStatusLabel.setVisible(false);
    } // end hideGUI

//**********************************************************************************************************************

    protected void showGUI(){
        super.showGUI();
        localStatusLabel.setVisible(true);
    } // end showGUI

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private void startClientConnection(){
        try{
            clientSocket = new Socket(serverIpText.getText(), Integer.parseInt(serverPortText.getText()));
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            localTimeUsernameLabel.setBorder(BorderFactory.createTitledBorder(theBorder, usernameText.getText() + "'s Statistics"));
            safePrint("U" + usernameText.getText());
            // server only: generateNewScramble();

            showGUI();
            startupClip.play();
            chatListener = new Thread(this);
            chatListener.start();
            connectButton.setText("CONNECTED");
            connectButton.setEnabled(false);
            serverIpText.setEnabled(false);
            usernameText.setEnabled(false);
            serverPortText.setEnabled(false);
            sendMessageButton.setEnabled(true);
            chatText.setEnabled(true);
        } catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Cannot connect to server. Information may be entered incorrectly.");
            hideGUI();
            connectButton.setText("Connect To Server");
            connectButton.setEnabled(true);
            serverIpText.setEnabled(true);
            usernameText.setEnabled(true);
            serverPortText.setEnabled(true);
            sendMessageButton.setEnabled(false);
            chatText.setEnabled(false);
        }
    }

}
