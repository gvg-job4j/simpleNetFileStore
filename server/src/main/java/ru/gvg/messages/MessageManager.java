package ru.gvg.messages;

import ru.gvg.common.Consts;
import ru.gvg.serverside.ServerThread;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;

/**
 * @author Valeriy Gyrievskikh
 * @since 05.01.2020
 */
public class MessageManager {
    /**
     * Method sends a message to socket.
     *
     * @param client  Socket for sending a message.
     * @param message Message to send.
     * @return Sign of sending.
     */
    public static boolean sendMessage(Socket client, Messaging message) {
        boolean sended = false;
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(client.getOutputStream());
            oos.writeObject(message);
            oos.flush();
            sended = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sended;
    }

    /**
     * Method received a message from socket.
     *
     * @param client Socket for receiving a message.
     * @return Received message.
     */
    public static Messaging getMessage(Socket client) {
        Messaging message = null;
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(client.getInputStream());
            message = (Messaging) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Method performs initial processing of the received client message.
     *
     * @param serverThread Current server thread.
     * @param inMessage    Received client message.
     * @param sArea        Area for displaying a text message.
     * @return Response message.
     * @throws Exception Possible exception.
     */
    public static Messaging handleClientMessage(ServerThread serverThread, Object inMessage, JTextArea sArea) throws Exception /**/ {
        Messaging outMessage = null;
        if (inMessage instanceof SecurityMessage) {
            outMessage = MessageHandler.handleSecurityMessage(serverThread);
        } else if (inMessage instanceof AnswerMessage) {
            AnswerMessage message = (AnswerMessage) inMessage;
            if (message.isResult()) {
                sArea.append(Consts.DATE_FORMAT.format(new Date()) + ". " + message.getMsg() + "\n");
            } else {
                outMessage = new AnswerMessage(true, Consts.DATE_FORMAT.format(new Date()) + ". Socket ready!");
            }
        } else if (inMessage instanceof LoginMessage) {
            outMessage = MessageHandler.handleLoginMessage((LoginMessage) inMessage, serverThread, sArea);
        } else if (inMessage instanceof FileMessage) {
            outMessage = MessageHandler.handleFileMessage((FileMessage) inMessage, serverThread);
        } else if (inMessage instanceof TransferFileMessage) {
            outMessage = MessageHandler.handleTransferFileMessage((TransferFileMessage) inMessage, serverThread);
        } else if (inMessage instanceof FolderMessage) {
            outMessage = MessageHandler.handleFolderMessage((FolderMessage) inMessage, serverThread);
        }
        return outMessage;
    }
}
