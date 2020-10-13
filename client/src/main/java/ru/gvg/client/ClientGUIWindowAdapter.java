package ru.gvg.client;

import ru.gvg.messages.AnswerMessage;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Valeriy Gyrievskikh
 * @since 21.10.2019
 */
public class ClientGUIWindowAdapter implements WindowListener {

    /**
     * User window.
     */
    private ClientGUI myFrame;

    /**
     * Constructor, set user window.
     *
     * @param myFrame User window.
     */
    public ClientGUIWindowAdapter(ClientGUI myFrame) {
        this.myFrame = myFrame;
    }

    /**
     * Method handles closing the user window.
     *
     * @param event Window event.
     */
    @Override
    public void windowClosing(WindowEvent event) {
        try {
            Socket tecSocket = myFrame.getClientSocket();
            if (tecSocket != null && !tecSocket.isClosed()) {
                String msg = "User: " + myFrame.getLogin() + " disconnected";
                AnswerMessage aMsg = new AnswerMessage(true, msg, null, 0);
                ObjectOutputStream oos = new ObjectOutputStream(tecSocket.getOutputStream());
                oos.writeObject(aMsg);
                oos.flush();
                oos.close();
                if (!tecSocket.isClosed()) {
                    tecSocket.close();
                }
            }
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
