package ru.gvg.server_side;

import ru.gvg.common.Consts;
import ru.gvg.common.Network;

import javax.swing.*;
import java.net.Socket;
import java.util.Date;

/**
 * Create thread for connecting user.
 *
 * @author Valeriy Gyrievskikh
 * @since 17.03.2019
 */
public class ServerThread implements Runnable {

    /**
     * Current server.
     */
    private MultiThreadServer mtSever;
    /**
     * Area for text messages.
     */
    private JTextArea sArea;
    /**
     * Current client socket.
     */
    private Socket client;

    /**
     * Metod create thread for client.
     *
     * @param mtSever Current server.
     * @param client  Current client.
     * @param sArea   Current area for text messages.
     */
    public ServerThread(MultiThreadServer mtSever, Socket client, JTextArea sArea) {
        this.sArea = sArea;
        this.client = client;
        this.mtSever = mtSever;
    }

    /**
     * Metod start processing client messages.
     */
    @Override
    public void run() {
        Network.sendAnswerMessage(client, null, true, Consts.formatForDate.format(new Date()) + ". Socket ready!");
    }
}
