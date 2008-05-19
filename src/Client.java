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

public class Client extends NetcubeMode{
    //stuff not in Server
    JCheckBox localStatusLabel;
    Socket socket;

//**********************************************************************************************************************

    public Client(Color textBackgrColor){
        super(textBackgrColor);

        // configure Contentpane
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        // configure JFrame
        setTitle("Rubik's JTimer Client");

        //GUI Object creation
        usernameLabel = new JLabel("Username:");
        serverIpLabel = new JLabel("Server IP:");
        serverPortLabel = new JLabel("Server Port:");
        handicapLabel = new JLabel("Handicap:");
        usernameText = new JTextField();
        serverIpText = new JTextField("127.0.0.1");
        serverPortText = new JTextField("52003");
        handicapText = new JTextField();
        connectButton = new JButton("Connect To Server");

        typeOff = new ImageIcon(getClass().getResource("typeOff.gif"));
        typeOn = new ImageIcon(getClass().getResource("typeOn.gif"));
        userIsTyping = new JLabel(typeOff);

        chatPane = new JTextPane(chatDoc = new DefaultStyledDocument());
        chatPane.setEditable(false);
        chatScrollPane = new JScrollPane(chatPane);
        chatScrollPane.setBorder(blackLine);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        redStyle = chatPane.addStyle("red", null);
        blueStyle = chatPane.addStyle("blue", null);
        blackStyle = chatPane.addStyle("black", null);
        StyleConstants.setForeground(redStyle, Color.red);
        StyleConstants.setForeground(blueStyle, Color.blue);
        StyleConstants.setForeground(blackStyle, Color.black);

        chatText = new JTextField();
        chatText.addKeyListener(this);
        sendMessageButton = new JButton("Send");

        useThisAlgLabel = new JLabel("Use this Scramble Algorithm:");
        useThisAlgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scrambleText = new JTextArea("");
        scrambleText.setFocusable(true);
        scrambleText.setEditable(false);
        scrambleText.setLineWrap(true);
        scrambleText.setWrapStyleWord(true);
        scrambleText.setBackground(myBackgrColor);
        scrambleText.setForeground(Color.black);
        scrambleText.setBorder(blackLine);

        timerLabel = new JLabel("");
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setFont(new Font("Serif", Font.PLAIN, 94));

        localTimeUsernameLabel = new JLabel("<html>Rolling Average: <FONT SIZE=\"5\">N/A</FONT><br>Session Average: N/A<br><br>Score: 0<br>Session Fastest Time: N/A<br>Session Slowest Time: N/A</html>");
        remoteTimeUsernameLabel = new JLabel("<html>Rolling Average: <FONT SIZE=\"5\">N/A</FONT><br>Session Average: N/A<br><br>Score: 0<br>Session Fastest Time: N/A<br>Session Slowest Time: N/A</html>");
        localTimeLabel = new JLabel("");
        remoteTimeLabel = new JLabel("");
        localTimeUsernameLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        remoteTimeUsernameLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        localTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        remoteTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        localTimeLabel.setFont(new Font("Serif", Font.PLAIN, 60));
        remoteTimeLabel.setFont(new Font("Serif", Font.PLAIN, 60));

        localTimeUsernameLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Local Statistics"));
        remoteTimeUsernameLabel.setBorder(BorderFactory.createTitledBorder(theBorder, "Remote Statistics"));

        localAverageDetailButton = new JButton("View Rolling");
        localSessionDetailButton = new JButton("View Session");
        remoteAverageDetailButton = new JButton("View Rolling");
        remoteSessionDetailButton = new JButton("View Session");

        puzzleLabel = new JLabel("Puzzle:");
        puzzleCombo = new JComboBox(puzzleChoices);
        //puzzleCombo.setSelectedItem("3x3x3");
        countdownLabel = new JLabel("Countdown:");
        countdownCombo = new JComboBox(countdownChoices);
        //countdownCombo.setSelectedItem("15");
        startButton = new JButton("Stop Timer");
        popButton = new JButton("POP");

        localStatusLabel = new JCheckBox("I'm ready!");

        bigPicture = new JLabel((new ImageIcon(getClass().getResource("bigPicture.jpg"))));
        smallPicture = new JLabel((new ImageIcon(getClass().getResource("smallPicture.jpg"))));
        bigPicture.setBorder(blackLine);
        smallPicture.setBorder(blackLine);

        //set bounds
        usernameLabel.setBounds(10,10,80,20);
        serverIpLabel.setBounds(10,35,80,20);
        serverPortLabel.setBounds(10,60,80,20);
        handicapLabel.setBounds(10,85,80,20);
        usernameText.setBounds(90,10,80,20);
        serverIpText.setBounds(90,35,80,20);
        serverPortText.setBounds(90,60,80,20);
        handicapText.setBounds(90,85,80,20);
        connectButton.setBounds(10,110,160,20);
        chatScrollPane.setBounds(180,10,350,95);
        userIsTyping.setBounds(180,110,20,20);
        chatText.setBounds(205,110,235,20);
        sendMessageButton.setBounds(450,110,80,20);
        useThisAlgLabel.setBounds(10,140,700,20);
        scrambleText.setBounds(10,160,700,50);
        timerLabel.setBounds(10,220,700,75);
        localTimeUsernameLabel.setBounds(10,305,345,200);
        remoteTimeUsernameLabel.setBounds(365,305,345,200);
        localTimeLabel.setBounds(20,325,325,75);
        remoteTimeLabel.setBounds(375,325,325,75);
        localAverageDetailButton.setBounds(215,405,120,20);
        localSessionDetailButton.setBounds(215,430,120,20);
        remoteAverageDetailButton.setBounds(570,405,120,20);
        remoteSessionDetailButton.setBounds(570,430,120,20);
        puzzleLabel.setBounds(540,15,80,20);
        puzzleCombo.setBounds(540,35,80,20);
        countdownLabel.setBounds(540+90,15,80,20);
        countdownCombo.setBounds(540+90,35,80,20);
        startButton.setBounds(540,60,170,20);
        popButton.setBounds(540,85,170,20);
        smallPicture.setBounds(210,10,500,120);
        bigPicture.setBounds(10,150,700,355);

        localStatusLabel.setBounds(540,110,170,20);

        //add to content pane
        contentPane.add(usernameLabel);
        contentPane.add(serverIpLabel);
        contentPane.add(serverPortLabel);
        contentPane.add(handicapLabel);
        contentPane.add(usernameText);
        contentPane.add(serverIpText);
        contentPane.add(serverPortText);
        contentPane.add(handicapText);
        contentPane.add(connectButton);
        contentPane.add(chatScrollPane);
        contentPane.add(userIsTyping);
        contentPane.add(chatText);
        contentPane.add(sendMessageButton);
        contentPane.add(useThisAlgLabel);
        contentPane.add(scrambleText);
        contentPane.add(timerLabel);
        contentPane.add(localTimeUsernameLabel);
        contentPane.add(remoteTimeUsernameLabel);
        contentPane.add(localTimeLabel);
        contentPane.add(remoteTimeLabel);
        contentPane.add(localAverageDetailButton);
        contentPane.add(localSessionDetailButton);
        contentPane.add(remoteAverageDetailButton);
        contentPane.add(remoteSessionDetailButton);
        contentPane.add(puzzleLabel);
        contentPane.add(puzzleCombo);
        contentPane.add(countdownLabel);
        contentPane.add(countdownCombo);
        contentPane.add(startButton);
        contentPane.add(popButton);
        //contentPane.add(smallPicture);
        //contentPane.add(bigPicture);
        contentPane.add(localStatusLabel);

        //addActionListener
        connectButton.addActionListener(this);
        sendMessageButton.addActionListener(this);
        chatText.addActionListener(this);
        startButton.addActionListener(this);
        popButton.addActionListener(this);
        localSessionDetailButton.addActionListener(this);
        localAverageDetailButton.addActionListener(this);
        remoteSessionDetailButton.addActionListener(this);
        remoteAverageDetailButton.addActionListener(this);
        localStatusLabel.addActionListener(this); // only in client

        //GUI preperation
        chatText.setEnabled(false);
        sendMessageButton.setEnabled(false);
        startButton.setEnabled(false);
        popButton.setEnabled(false);
        localAverageDetailButton.setEnabled(false);
        localSessionDetailButton.setEnabled(false);
        remoteAverageDetailButton.setEnabled(false);
        remoteSessionDetailButton.setEnabled(false);
        puzzleCombo.setEnabled(false); // only in client
        countdownCombo.setEnabled(false); // only in client

        //set everything to defaults
        reset();
        hideGUI();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    } // end constructor

//**********************************************************************************************************************

    public void actionPerformed(ActionEvent e){
        Object source = e.getSource();

        if(source == connectButton){
            try{
                socket = new Socket(serverIpText.getText(), Integer.parseInt(serverPortText.getText()));

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

                localTimeUsernameLabel.setBorder(BorderFactory.createTitledBorder(theBorder, usernameText.getText() + "'s Statistics"));
                out.println("U" + usernameText.getText());
                out.flush();
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
                handicapText.setEnabled(false);
                sendMessageButton.setEnabled(true);
                chatText.setEnabled(true);
            } catch(Exception f){
                JOptionPane.showMessageDialog(this, "Cannot connect to server. Information may be entered incorrectly.");
                hideGUI();
                connectButton.setText("Connect To Server");
                connectButton.setEnabled(true);
                serverIpText.setEnabled(true);
                usernameText.setEnabled(true);
                serverPortText.setEnabled(true);
                handicapText.setEnabled(true);
                sendMessageButton.setEnabled(false);
                chatText.setEnabled(false);
                return;
            }
        } else if(source == sendMessageButton || source == chatText){
            if(chatText.getText().equalsIgnoreCase("")) return;
            try{
                chatDoc.insertString(chatDoc.getLength(),(usernameText.getText() + ": "),redStyle);
                chatDoc.insertString(chatDoc.getLength(),chatText.getText() + "\n",blackStyle);
                out.println("C" + chatText.getText());
                out.flush();
                chatPane.setCaretPosition(chatDoc.getLength());
                chatText.setText("");
                if(isTyping){
                    isTyping = false;
                    out.println("I");
                    out.flush();
                }
            } catch(BadLocationException f){System.out.println(f);}
        } else if(source == localStatusLabel){
            out.println("R" + localStatusLabel.isSelected());
            out.flush();
        } else if(source == startButton){
            //show on localTime and send the time to server
            stopTime = System.currentTimeMillis();
            startButton.setEnabled(false);
            popButton.setEnabled(false);
            localTimeLabel.setForeground(Color.blue);
            timerLabel.setForeground(Color.black);
            localTimeLabel.setText(timeFormat.format((stopTime-startTime)/1000));
            out.println("N" + timeFormat.format((stopTime-startTime)/1000));
            out.flush();

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
            out.println("N" + "POP");
            out.flush();

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
        } else if(source == localSessionDetailButton){
            DetailedView win = new DetailedView("Session Times", getLocalSessionView(), myBackgrColor);
            win.setVisible(true);
        } else if(source == localAverageDetailButton){
            DetailedView win = new DetailedView("Rolling Average", getLocalAverageView(), myBackgrColor);
            win.setVisible(true);
        } else if(source == remoteSessionDetailButton){
            DetailedView win = new DetailedView("Session Times", getRemoteSessionView(), myBackgrColor);
            win.setVisible(true);
        } else if(source == remoteAverageDetailButton){
            DetailedView win = new DetailedView("Rolling Average", getRemoteAverageView(), myBackgrColor);
            win.setVisible(true);
        }
    } // end actionPerformed

//**********************************************************************************************************************

    public void run(){
        try{
            while(true){
                String data = in.readLine();
                String prefix = data.substring(0,1);
                String message = data.substring(1,data.length());
                performAction(prefix, message);
            }
        } catch(Exception e){
            hideGUI();
            JOptionPane.showMessageDialog(this, "You have been disconnected from the server.");
            reset();
            connectButton.setText("Connect To Server");
            chatText.setText("");
            connectButton.setEnabled(true);
            usernameText.setEnabled(true);
            serverPortText.setEnabled(true);
            handicapText.setEnabled(true);
            sendMessageButton.setEnabled(false);
            chatText.setEnabled(false);
        } finally {
            chatListener = null;
            validate();
            try{
                in.close();
                out.close();
                socket.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    } // end run

//**********************************************************************************************************************

    private void performAction(String prefix, String data){
        if(prefix.equalsIgnoreCase("C")){
            try{
                chatDoc.insertString(chatDoc.getLength(),remoteUsername + ": ",blueStyle);
                chatDoc.insertString(chatDoc.getLength(),data + "\n",blackStyle);
                chatPane.setCaretPosition(chatDoc.getLength());
                chatSound.play();
            } catch(BadLocationException f){System.out.println(f);}
        }else if(prefix.equalsIgnoreCase("U")){
            remoteUsername = data;
            remoteTimeUsernameLabel.setBorder(BorderFactory.createTitledBorder(theBorder, remoteUsername + "'s Statistics"));
        } else if(prefix.equalsIgnoreCase("P")){
            puzzleCombo.setSelectedItem(data);
        } else if(prefix.equalsIgnoreCase("T")){
            countdownCombo.setSelectedItem(data);
        } else if(prefix.equalsIgnoreCase("S")){
            scrambleText.setFont(puzzleCombo.getSelectedItem() == "Megaminx" ? smAlgFont : lgAlgFont);
            scrambleText.setText(data);
        } else if(prefix.equalsIgnoreCase("N")){
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
        } else if(prefix.equalsIgnoreCase("X")){
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
        } else if(prefix.equalsIgnoreCase("I")){
            remoteIsTyping = !remoteIsTyping;
            if(remoteIsTyping) userIsTyping.setIcon(typeOn);
            else userIsTyping.setIcon(typeOff);
        }
    } // end performedAction

//**********************************************************************************************************************

    private void hideGUI(){
        //usernameLabel.setVisible(true);
        //serverIpLabel.setVisible(true);
        //serverPortLabel.setVisible(true);
        //handicapLabel.setVisible(true);
        //usernameText.setVisible(true);
        //serverIpText.setVisible(true);
        //serverPortText.setVisible(true);
        //handicapText.setVisible(true);
        //connectButton.setVisible(true);
        bigPicture.setVisible(true);
        smallPicture.setVisible(true);

        //hide everything
        chatScrollPane.setVisible(false);
        userIsTyping.setVisible(false);
        chatText.setVisible(false);
        sendMessageButton.setVisible(false);
        puzzleLabel.setVisible(false);
        puzzleCombo.setVisible(false);
        countdownLabel.setVisible(false);
        countdownCombo.setVisible(false);
        startButton.setVisible(false);
        popButton.setVisible(false);
        localStatusLabel.setVisible(false);


        useThisAlgLabel.setVisible(false);
        scrambleText.setVisible(false);
        timerLabel.setVisible(false);
        localTimeLabel.setVisible(false);
        remoteTimeLabel.setVisible(false);
        localTimeUsernameLabel.setVisible(false);
        remoteTimeUsernameLabel.setVisible(false);
        localAverageDetailButton.setVisible(false);
        localSessionDetailButton.setVisible(false);
        remoteSessionDetailButton.setVisible(false);
        remoteAverageDetailButton.setVisible(false);

    } // end hideGUI

//**********************************************************************************************************************

    private void showGUI(){
        //usernameLabel.setVisible(false);
        //serverIpLabel.setVisible(false);
        //serverPortLabel.setVisible(false);
        //handicapLabel.setVisible(false);
        //usernameText.setVisible(false);
        //serverIpText.setVisible(false);
        //serverPortText.setVisible(false);
        //handicapText.setVisible(false);
        //connectButton.setVisible(false);
        bigPicture.setVisible(false);
        smallPicture.setVisible(false);

        //show everything
        chatScrollPane.setVisible(true);
        userIsTyping.setVisible(true);
        chatText.setVisible(true);
        sendMessageButton.setVisible(true);
        puzzleLabel.setVisible(true);
        puzzleCombo.setVisible(true);
        countdownLabel.setVisible(true);
        countdownCombo.setVisible(true);
        startButton.setVisible(true);
        popButton.setVisible(true);
        localStatusLabel.setVisible(true);


        useThisAlgLabel.setVisible(true);
        scrambleText.setVisible(true);
        timerLabel.setVisible(true);
        localTimeLabel.setVisible(true);
        remoteTimeLabel.setVisible(true);
        localTimeUsernameLabel.setVisible(true);
        remoteTimeUsernameLabel.setVisible(true);
        localAverageDetailButton.setVisible(true);
        localSessionDetailButton.setVisible(true);
        remoteSessionDetailButton.setVisible(true);
        remoteAverageDetailButton.setVisible(true);

    } // end showGUI

}
