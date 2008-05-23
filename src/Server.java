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
import java.net.*;
import java.io.*;
import javax.swing.text.*;
import java.applet.*;
import java.text.*;
import java.util.*;
import javax.swing.border.Border;

public class Server extends NetcubeMode{
    // stuff not in Client
    AudioClip readyClip;
    ScrambleAlg scrambleAlg;
    ServerSocket serverSocket;
    Thread connectionListener;

//**********************************************************************************************************************

    public Server(String puzzle, String countdown, OptionsMenu optionsMenu){
        super(optionsMenu);

        // configure Contentpane
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        // configure JFrame
        setTitle(APP_TITLE + " Server");

        try { //configure readyClip
            readyClip = Applet.newAudioClip(getClass().getResource("ready.wav"));
        } catch(NullPointerException ex){JOptionPane.showMessageDialog(this, "ready.wav not found. There will be no 'ready' sound.");}

        // GUI Object creation
        serverIpText.setText("N/A");
        connectButton.setText("Start Server");

        puzzleCombo.setSelectedItem(puzzle);
        countdownCombo.setSelectedItem(countdown);
        startButton.setText("Start Timer");

        scrambleAlg = new ScrambleAlg();

        // set bounds
        super.setTheBounds();

        // add to contentPane
        super.addTheContent(contentPane);
        contentPane.add(readyColor);
        contentPane.add(remoteStatusLabel);

        // add ActionListeners
        super.addActionListeners();
        usernameText.addActionListener(this);
        serverPortText.addActionListener(this);
        puzzleCombo.addActionListener(this);
        countdownCombo.addActionListener(this);

        // GUI preperation
        super.prepGUI();
        serverIpText.setEnabled(false);

        // hide GUI
        hideGUI();
    } // end constructor

//**********************************************************************************************************************

    public void actionPerformed(ActionEvent e){
        Object source = e.getSource();

        if(source == connectButton || source == usernameText || source == serverPortText){
            if(usernameText.getText().equals("")){
                JOptionPane.showMessageDialog(this, "Username should not be blank.");
                return;
            }
            startServerConnection();
        } else if(source == sendMessageButton || source == chatText){
            if(chatText.getText().equals("")) return;
            try{
                chatDoc.insertString(chatDoc.getLength(), (usernameText.getText()+": "), redStyle);
                chatDoc.insertString(chatDoc.getLength(), (chatText.getText()+"\n"), blackStyle);
                out.println("C" + chatText.getText());
                out.flush();
                chatPane.setCaretPosition(chatDoc.getLength());
                chatText.setText("");
                if(isTyping){
                    isTyping = false;
                    out.println("I");
                    out.flush();
                }
            } catch(BadLocationException ex){
                System.out.println(ex.getMessage());
            }
        } else if(source == puzzleCombo){
            out.println("P" + puzzleCombo.getSelectedItem());
            out.flush();
            generateNewScramble();
        } else if(source == countdownCombo){
            out.println("T" + countdownCombo.getSelectedItem());
            out.flush();
        } else if(source == startButton){
            if(startButton.getText().equals("Start Timer")){
                //change buttons and clear times
                remoteTime = "none";
                localTimeLabel.setText("");
                remoteTimeLabel.setText("");
                startButton.setText("Stop Timer");
                puzzleCombo.setEnabled(false);
                countdownCombo.setEnabled(false);
                startButton.setEnabled(false);
                localSessionDetailButton.setEnabled(false);
                localAverageDetailButton.setEnabled(false);
                remoteSessionDetailButton.setEnabled(false);
                remoteAverageDetailButton.setEnabled(false);
                popButton.setEnabled(false);
                sendMessageButton.setEnabled(false);
                chatText.setEnabled(false);
                //start countdown / timer on server AND client
                countdown = Integer.parseInt(countdownCombo.getSelectedItem()+"");
                timerLabel.setForeground(Color.red);
                timerThread = new java.util.Timer();
                out.println("X" + "GO!");
                out.flush();
                timerThread.schedule(new RunCountdown(), 0, 1000);
            } else if(startButton.getText().equals("Stop Timer")){
                //show on localTime and send time to client
                stopTime = System.currentTimeMillis();
                startButton.setEnabled(false);
                popButton.setEnabled(false);
                localTimeLabel.setForeground(Color.blue);
                timerLabel.setForeground(Color.black);
                localTimeLabel.setText(ssxx.format((stopTime-startTime)/1000F));
                out.println("N" + ssxx.format((stopTime-startTime)/1000F));
                out.flush();

                //increment stuff
                acceptsSincePop++;
                localCubesSolved++;

                //if everyone is done, then stop the timer update stats
                if(!remoteTime.equals("none")){
                    timerThread.cancel();
                    timerLabel.setText("");
                    //change buttons
                    startButton.setText("Start Timer");
                    readyColor.setBackground(Color.red);
                    puzzleCombo.setEnabled(true);
                    countdownCombo.setEnabled(true);
                    popButton.setEnabled(false);
                    chatText.setEnabled(true);
                    sendMessageButton.setEnabled(true);
                    localSessionDetailButton.setEnabled(true);
                    remoteSessionDetailButton.setEnabled(true);
                    if(localCubesSolved >= 12)
                        localAverageDetailButton.setEnabled(true);
                    if(remoteCubesSolved >= 12)
                        remoteAverageDetailButton.setEnabled(true);
                    // move to next solve
                    updateStats();
                    generateNewScramble();
                }
            }
        } else if(source == popButton){
            //show on localTime and send time to client
            acceptsSincePop = 0;
            localNumOfPops++;
            popButton.setText("Popped");
            startButton.setEnabled(false);
            popButton.setEnabled(false);
            localTimeLabel.setForeground(Color.blue);
            timerLabel.setForeground(Color.black);
            localTimeLabel.setText("POP");
            out.println("N" + "POP");
            out.flush();

            //if everyone is done, then stop the timer update stats
            if(!remoteTime.equals("none")){
                timerThread.cancel();
                timerLabel.setText("");
                //change buttons
                startButton.setText("Start Timer");
                readyColor.setBackground(Color.red);
                puzzleCombo.setEnabled(true);
                countdownCombo.setEnabled(true);
                popButton.setEnabled(false);
                chatText.setEnabled(true);
                sendMessageButton.setEnabled(true);
                localSessionDetailButton.setEnabled(true);
                remoteSessionDetailButton.setEnabled(true);
                if(localCubesSolved >= 12)
                    localAverageDetailButton.setEnabled(true);
                if(remoteCubesSolved >= 12)
                    remoteAverageDetailButton.setEnabled(true);
                // move to next solve
                updateStats();
                generateNewScramble();
            }
        } else if(source == localSessionDetailButton){
            DetailedView win = new DetailedView("Session Times", getLocalSessionView(), optionsMenu.textBackgrColorX);
            win.setVisible(true);
        } else if(source == localAverageDetailButton){
            DetailedView win = new DetailedView("Rolling Average", getLocalAverageView(), optionsMenu.textBackgrColorX);
            win.setVisible(true);
        } else if(source == remoteSessionDetailButton){
            DetailedView win = new DetailedView("Session Times", getRemoteSessionView(), optionsMenu.textBackgrColorX);
            win.setVisible(true);
        } else if(source == remoteAverageDetailButton){
            DetailedView win = new DetailedView("Rolling Average", getRemoteAverageView(), optionsMenu.textBackgrColorX);
            win.setVisible(true);
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
            JOptionPane.showMessageDialog(this, "Client has disconnected from this server.");
            reset();
            connectButton.setText("Start Server");
            chatText.setText("");
            connectButton.setEnabled(true);
            usernameText.setEnabled(true);
            serverPortText.setEnabled(true);
            //handicapText.setEnabled(true);
            sendMessageButton.setEnabled(false);
            chatText.setEnabled(false);
        } finally {
            chatListener = null;
            validate();
            try{
                in.close();
                out.close();
                clientSocket.close();
                serverSocket.close();
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
            remoteStatusLabel.setText(remoteUsername + "'s Status");
        } else if(prefix.equals("R")){ // indicate client is Ready
            if(data.equalsIgnoreCase("true")){
                readyClip.play();
                startButton.setEnabled(true);
                puzzleCombo.setEnabled(false);
                countdownCombo.setEnabled(false);
                readyColor.setBackground(new Color(0,180,0)); // was 0,110,0
            } else {
                readyClip.play();
                startButton.setEnabled(false);
                puzzleCombo.setEnabled(true);
                countdownCombo.setEnabled(true);
                readyColor.setBackground(Color.red);
            }
        } else if(prefix.equals("N")){ // pass finished time
            remoteTime = data;
            //if everyone is done, then stop the timer update stats
            if(!localTimeLabel.getText().equals("")){
                timerThread.cancel();
                timerLabel.setText("");
                //change buttons
                startButton.setText("Start Timer");
                readyColor.setBackground(Color.red);
                puzzleCombo.setEnabled(true);
                countdownCombo.setEnabled(true);
                popButton.setEnabled(false);
                chatText.setEnabled(true);
                sendMessageButton.setEnabled(true);
                localSessionDetailButton.setEnabled(true);
                remoteSessionDetailButton.setEnabled(true);
                if(localCubesSolved >= 12)
                    localAverageDetailButton.setEnabled(true);
                if(remoteCubesSolved >= 12)
                    remoteAverageDetailButton.setEnabled(true);
                // move to next solve
                updateStats();
                generateNewScramble();
            }
        } else if(prefix.equals("I")){ // toggle Is-typing icon
            remoteIsTyping = !remoteIsTyping;
            if(remoteIsTyping) userIsTyping.setIcon(typeOn);
            else userIsTyping.setIcon(typeOff);
        }
    } // end performedAction

//**********************************************************************************************************************

    protected void hideGUI(){
        super.hideGUI();
        readyColor.setVisible(false);
        remoteStatusLabel.setVisible(false);
    } // end hideGUI

//**********************************************************************************************************************

    protected void showGUI(){
        super.showGUI();
        readyColor.setVisible(true);
        remoteStatusLabel.setVisible(true);
    } // end showGUI

//**********************************************************************************************************************
//**********************************************************************************************************************
//**********************************************************************************************************************

    private void generateNewScramble(){
        scrambleText.setFont(puzzleCombo.getSelectedItem() == "Megaminx" ? smAlgFont : lgAlgFont);
        newAlg = scrambleAlg.generateAlg(puzzleCombo.getSelectedItem()+"");
        scrambleText.setText(newAlg.replaceAll(ALG_BREAK, "\n"));
        out.println("S" + newAlg);
        out.flush();
    } // end generateNewScramble

//**********************************************************************************************************************

    private void startServerConnection(){
        try{
            connectButton.setText("LISTENING");
            connectButton.setEnabled(false);
            usernameText.setEnabled(false);
            serverPortText.setEnabled(false);
            //handicapText.setEnabled(false);

            serverSocket = new ServerSocket(Integer.parseInt(serverPortText.getText()));
            connectionListener = new ConnectionListener(serverSocket, this);
            connectionListener.setDaemon(true);
            connectionListener.start();
        } catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Cannot start server. Information may be entered incorrectly.");
            hideGUI();
            connectButton.setText("Start Server");
            connectButton.setEnabled(true);
            // client only: serverIpText.setEnabled(true);
            usernameText.setEnabled(true);
            serverPortText.setEnabled(true);
            //handicapText.setEnabled(true);
            sendMessageButton.setEnabled(false);
            chatText.setEnabled(false);
        }
    }

//**********************************************************************************************************************

    public void connectionMade(Socket clientSocket){
        this.clientSocket = clientSocket;
        try{
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            localTimeUsernameLabel.setBorder(BorderFactory.createTitledBorder(theBorder, usernameText.getText() + "'s Statistics"));
            out.println("U" + usernameText.getText());
            out.println("P" + puzzleCombo.getSelectedItem());
            out.println("T" + countdownCombo.getSelectedItem());
            out.flush();
            generateNewScramble();

            showGUI();
            startupClip.play();
            chatListener = new Thread(this);
            chatListener.start();
            connectButton.setText("CONNECTED");
            connectButton.setEnabled(false);
            //client only: serverIpText.setEnabled(false);
            usernameText.setEnabled(false);
            serverPortText.setEnabled(false);
            //handicapText.setEnabled(false);
            sendMessageButton.setEnabled(true);
            chatText.setEnabled(true);
        } catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Cannot start server. Information may be entered incorrectly.");
            System.out.println(ex.getMessage());
            return;
        }
    }

//**********************************************************************************************************************

    private class ConnectionListener extends Thread{
        private ServerSocket serverSocket;
        private Server server;
        private Socket clientSocket;

        public ConnectionListener(ServerSocket serverSocket, Server server){
            this.serverSocket = serverSocket;
            this.server = server;
        }

        public void run(){
            try{
                clientSocket = serverSocket.accept();
                server.connectionMade(clientSocket);
            } catch(Exception ex){
                System.err.println(ex.getMessage());
            }
        }
    }

}
