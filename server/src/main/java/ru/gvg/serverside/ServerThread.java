package ru.gvg.serverside;

import ru.gvg.common.Consts;
import ru.gvg.messages.*;

import javax.swing.*;
import java.net.Socket;
import java.sql.*;
import java.util.Date;

/**
 * Creates a thread for the connecting user.
 *
 * @author Valeriy Gyrievskikh
 * @since 17.03.2019
 */
public class ServerThread implements Runnable, AutoCloseable {

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
     * Current user.
     */
    private String user;

    /**
     * Current database tool.
     */
    private DataBaseManager dataBaseManager;

    /**
     * Current security tool.
     */
    private MyDropBoxSecurity security;

    /**
     * Current database connection.
     */
    private Connection connection;

    /**
     * Method returns current database tool.
     *
     * @return Current database tool.
     */
    public DataBaseManager getDataBaseManager() {
        return dataBaseManager;
    }

    /**
     * Method returns current client socket.
     *
     * @return Current client socket.
     */
    public Socket getClient() {
        return client;
    }

    /**
     * Method returns current server.
     *
     * @return Current server.
     */
    public MultiThreadServer getMtSever() {
        return mtSever;
    }

    /**
     * Method creates thread for client.
     *
     * @param mtSever Current server.
     * @param client  Current client.
     * @param sArea   Current area for text messages.
     */
    public ServerThread(MultiThreadServer mtSever, Socket client, JTextArea sArea) {
        this.sArea = sArea;
        this.client = client;
        this.mtSever = mtSever;
        this.dataBaseManager = new DataBaseManager();
    }

    /**
     * Default constructor.
     */
    public ServerThread() {
    }

    /**
     * Method starts processing client messages.
     */
    @Override
    public void run() {
        try {
            this.security = new MyDropBoxSecurity();
            this.connection = dataBaseManager.connectToDataBase();
            Messaging outMessage = new AnswerMessage(true, Consts.DATE_FORMAT.format(new Date()) + ". Socket ready!");
            while (!client.isClosed()) {
                if (MessageManager.sendMessage(client, outMessage)) {
                    Messaging inMessage = MessageManager.getMessage(client);
                    outMessage = MessageManager.handleClientMessage(this, inMessage, sArea);
                    if (inMessage == null || outMessage == null) {
                        break;
                    }
                }
            }
            Thread.currentThread().interrupt();
            mtSever.getThreadList().remove(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method gets current user.
     *
     * @return Current user.
     */
    public String getUser() {
        return user;
    }

    /**
     * Method sets current user for this thread.
     *
     * @param user Current user.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Method gets current security tool.
     *
     * @return Current security tool.
     */
    public MyDropBoxSecurity getSecurity() {
        return security;
    }

    /**
     * Method gets current database connection.
     *
     * @return Current database connection.
     */
    public Connection getConnection() {
        return this.connection;
    }

    /**
     * Method closes current thread.
     */
    @Override
    public void close() {
        if (this.connection != null) {
            this.connection = null;
        }
    }
}
