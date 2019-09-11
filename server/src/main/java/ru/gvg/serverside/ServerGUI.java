package ru.gvg.serverside;

import ru.gvg.common.Consts;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.sql.DriverManager;
import java.util.Date;
import java.util.Properties;

/**
 * Server side graphic interface.
 *
 * @author Valeriy Gyrievskikh
 * @since 01.03.2019
 */
public class ServerGUI extends JFrame implements ActionListener {

    /**
     * Area for text messages.
     */
    private JTextArea textArea = new JTextArea();
    /**
     * Server part of the application.
     */
    private MultiThreadServer server;
    /**
     * Buttons that control the operation of the server.
     */
    private JButton startButton, stopButton;

    /**
     * Metod run server GUI.
     *
     * @param args Parameters list.
     */
    public static void main(String[] args) {
        new ServerGUI();
    }

    /**
     * Metod created visual form for server management.
     */
    private ServerGUI() {
        setTitle("Server window");
        setSize(390, 200);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start server");
        startButton.setMnemonic(java.awt.event.KeyEvent.VK_R);
        startButton.addActionListener(this);
        stopButton = new JButton("Stop server");
        stopButton.setMnemonic(java.awt.event.KeyEvent.VK_S);
        stopButton.addActionListener(this);
        stopButton.setEnabled(false);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        textArea.setEditable(false);
        getContentPane().add(buttonPanel, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        setVisible(true);
    }

    /**
     * Metod handle buttons action.
     *
     * @param e Current action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            if (server == null || !server.isAlive()) {
                server = new MultiThreadServer(textArea);
                server.start();
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        } else if (e.getSource() == stopButton) {
            if (server != null && server.isAlive()) {
                server.stopCurrentServer();
            }
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }
}
