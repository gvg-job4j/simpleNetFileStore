package ru.gvg.serverside;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.gvg.common.Consts;
import ru.gvg.messages.AnswerMessage;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

import static org.junit.Assert.*;

/**
 * @author Valeriy Gyrievskikh
 * @since 21.08.2019
 */
public class MultiThreadServerTest {

    private MultiThreadServer server;

    @Before
    public void createNewServer() {
        server = new MultiThreadServer(new JTextArea());
    }

    @After
    public void stopCurrentServer() {
        if (server != null) {
            server.stopCurrentServer();
            server = null;
        }
    }

    @Test
    public void whenServerWorkThenSocketNotNull() {
        server.start();
        Socket clientSocket = null;
        try {
            clientSocket = new Socket("172.16.172.252", Consts.PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertNotNull(clientSocket);
    }

    @Test
    public void whenClientConnectedThenServerSendAnswer() {
        server.start();
        String msg = "";
        try (Socket socket = new Socket("172.16.172.252", Consts.PORT)) {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            msg = ((AnswerMessage) in.readObject()).getMsg();
            in.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        assertTrue(msg.contains("Socket ready!"));
    }

    @Test
    public void whenServerStopThenServerThreadIsInterrupted() {
        server.start();
        server.stopCurrentServer();
        assertTrue(server.isInterrupted());
    }
}