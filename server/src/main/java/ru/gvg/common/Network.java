package ru.gvg.common;

import ru.gvg.messages.AnswerMessage;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Work with messages.
 *
 * @author Valeriy Gyrievskikh
 * @since 04.03.2019
 */
public class Network {

    /**
     * Metod send message {@code AnswerMessage} with parameters.
     *
     * @param clientSocket Current socket with output data.
     * @param user         Current user.
     * @param status       Current status.
     * @param msg          Text message.
     */
    public static void sendAnswerMessage(Socket clientSocket, String user, boolean status, String msg) {
        File[] userFiles = null;
        int userSize = 0;
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            AnswerMessage aMsg = new AnswerMessage(status, msg, userFiles, userSize);
            oos.writeObject(aMsg);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
