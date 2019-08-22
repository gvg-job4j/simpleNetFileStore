package ru.gvg.server_side;

import ru.gvg.common.Consts;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
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
     * Open port on server.
     */
    private int port = Consts.PORT;
    /**
     * Area for text messages.
     */
    private JTextArea textArea;
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
     * Metod create new thread for server work.
     *
     * @param textArea Area for text messages.
     * @param port     Port where server socket will be opened.
     */
    MultiThreadServer(JTextArea textArea, int port) {
        this.port = port;
        this.textArea = textArea;
        threadList = new ArrayList<>();
    }

    /**
     * Create server socket, create threads for connected clients.
     */
    @Override
    public void run() {
        try {
            InetAddress address = InetAddress.getLocalHost();//172.16.172.252
            serverSocket = new ServerSocket(port, 2);
            textArea.append(Consts.formatForDate.format(new Date()) + ". Server started. IP: " + address + ", port: " + serverSocket.getLocalPort() + "\n");
            while (true) {
                try {
                    Socket client = serverSocket.accept();
                    ServerThread sThread = new ServerThread(this, client, textArea);
                    ex.execute(sThread);
                    threadList.add(sThread);
                } catch (IOException e) {
                    break;
                }
            }
        } catch (IOException e) {
            textArea.append(Consts.formatForDate.format(new Date()) + ". Not find free port! Server not started!\n");
            e.printStackTrace();
            this.interrupt();
        }
    }

    /**
     * Metod close server socket.
     */
    void stopCurrentServer() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                textArea.append(Consts.formatForDate.format(new Date()) + ". Closing - DONE.\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ex.shutdown();
        this.interrupt();
    }

    /**
     * Metod return current list with clients threads.
     *
     * @return
     */
    public ArrayList<ServerThread> getThreadList() {
        return threadList;
    }

}
