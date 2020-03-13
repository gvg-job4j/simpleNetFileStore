package ru.gvg.serverside;

import ru.gvg.common.Consts;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created server socket for connect users.
 *
 * @author Valeriy Gyrievskikh
 * @since 01.03.2019
 */
public class MultiThreadServer extends Thread {
    /**
     * Database connection manager.
     */
    private final DataBaseManager dataBaseManager;

    /**
     * Area for text messages.
     */
    private final JTextArea textArea;

    /**
     * Open socket for client connections.
     */
    private ServerSocket serverSocket;

    /**
     * List with current threads {@code ServerThread}.
     */
    private ArrayList<ServerThread> threadList;

    /**
     * Max count parallel connections.
     */
    private ExecutorService ex = Executors.newFixedThreadPool(Consts.NTHREADS);

    /**
     * Method creates new thread for server work.
     *
     * @param textArea Area for text messages.
     */
    MultiThreadServer(JTextArea textArea) {
        this.textArea = textArea;
        this.threadList = new ArrayList<>();
        this.dataBaseManager = new DataBaseManager();
    }

    /**
     * Method creates server socket, creates threads for connected clients.
     */
    @Override
    public void run() {
        try (InputStream in = DataBaseManager.class.getClassLoader().getResourceAsStream("database.properties")) {
            Properties properties = new Properties();
            properties.load(in);
            if (!dataBaseManager.initDatabase(properties)) {
                textArea.append(Consts.DATE_FORMAT.format(new Date()) + ". Can not connect to database! Server not started!\n");
                this.interrupt();
                return;
            }
        } catch (IOException e) {
            textArea.append(Consts.DATE_FORMAT.format(new Date()) + ". Can not load database properties! Server not started!\n");
            e.printStackTrace();
            this.interrupt();
            return;
        } catch (SQLException | ClassNotFoundException e) {
            textArea.append(Consts.DATE_FORMAT.format(new Date()) + ". Can not connect to database! Server not started!\n");
            e.printStackTrace();
            this.interrupt();
            return;
        } catch (Exception e) {
            textArea.append(Consts.DATE_FORMAT.format(new Date()) + ". Can not init database! Server not started!\n");
            e.printStackTrace();
            this.interrupt();
            return;
        }
        try {
            InetAddress address = InetAddress.getLocalHost();
            serverSocket = new ServerSocket(Consts.PORT, 2);
            textArea.append(Consts.DATE_FORMAT.format(new Date()) + ". Server started. IP: " + address + ", port: " + serverSocket.getLocalPort() + "\n");
            while (!serverSocket.isClosed()) {
                try {
                    Socket client = serverSocket.accept();
                    ServerThread sThread = new ServerThread(this, client, textArea);
                    ex.execute(sThread);
                    threadList.add(sThread);
                } catch (IOException e) {
                    e.printStackTrace();
                    this.interrupt();
                    break;
                }
            }
        } catch (IOException e) {
            textArea.append(Consts.DATE_FORMAT.format(new Date()) + ". Not find free port! Server not started!\n");
            e.printStackTrace();
            this.interrupt();
        }
    }

    /**
     * Metod closes server socket.
     */
    void stopCurrentServer() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                textArea.append(Consts.DATE_FORMAT.format(new Date()) + ". Closing - DONE.\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ex.shutdown();
        this.interrupt();
    }

    /**
     * Metod returns current list with clients threads.
     *
     * @return List with clients threads.
     */
    public ArrayList<ServerThread> getThreadList() {
        return threadList;
    }

}
