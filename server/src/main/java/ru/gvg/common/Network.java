package ru.gvg.common;

import ru.gvg.messages.AnswerMessage;
import ru.gvg.messages.FolderMessage;
import ru.gvg.messages.TransferFileMessage;
import ru.gvg.serverside.WorkWithFiles;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

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
        if (user != null) {
            userFiles = WorkWithFiles.getUserFileStructure(user);
            if (userFiles == null) {
                userSize = 0;
            } else {
                userSize = WorkWithFiles.getUserFileSize(user, userFiles);
            }
        }
        AnswerMessage aMsg = new AnswerMessage(status, msg, userFiles, userSize);
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.writeObject(aMsg);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static AnswerMessage sendTestMessage (Socket clientSocket, String user, boolean status, String msg) {
//
//        AnswerMessage aMsg = new AnswerMessage(status, msg, null, 0);
//        AnswerMessage outMsg = null;
//        ObjectOutputStream oos = null;
//        try {
//            oos = new ObjectOutputStream(clientSocket.getOutputStream());
//            oos.writeObject(aMsg);
//            oos.flush();
//            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
//            Object obj = ois.readObject();
//            if (obj instanceof AnswerMessage) {
//                outMsg = (AnswerMessage) obj;
//            }
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        return outMsg;
//    }

    public static AnswerMessage sendFolderMessage(Socket clientSocket, String folderName, boolean create, boolean delete, String newFolderName) {
        FolderMessage fdm = new FolderMessage(folderName, create, delete, newFolderName);
        return sendMessage(clientSocket, fdm);
    }

    /**
     * @param clientSocket
     * @param fdm
     * @return
     */
    private static AnswerMessage sendMessage(Socket clientSocket, FolderMessage fdm) {
        AnswerMessage ansMsg = null;
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.writeObject(fdm);
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            Object obj = ois.readObject();
            if (obj instanceof AnswerMessage) {
                ansMsg = (AnswerMessage) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ansMsg;
    }

    public static AnswerMessage sendFile(File file, String filename, Socket socket) throws IOException, ClassNotFoundException, InvocationTargetException, InterruptedException {
        AnswerMessage ansMsg = null;
        long fileSize = Files.size(Paths.get(file.getAbsolutePath()));
        if (fileSize <= Consts.FILE_SIZE) {
            TransferFileMessage trFm = new TransferFileMessage(filename, file.getAbsolutePath(), fileSize, Files.readAllBytes(Paths.get(file.getAbsolutePath())), true, true);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(trFm);
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Object obj = ois.readObject();
            if (obj instanceof AnswerMessage) {
                ansMsg = (AnswerMessage) obj;
            }
        } else {
            FileInputStream fis = new FileInputStream(file);
            ArrayList<byte[]> aList = new ArrayList<>();

            int count = (int) (fileSize / Consts.FILE_SIZE);
            int lastSize = (int) (fileSize - (Consts.FILE_SIZE * count));
            int startPos = 0;
            int readByte = 0;
            for (int i = 0; i <= count; i++) {
                byte[] x = {};
//                int fragmentLength = 0;
                if (i == count) {
                    x = new byte[lastSize];
//                    fragmentLength = fis.available();
                } else {
                    x = new byte[Consts.FILE_SIZE];
//                    fragmentLength = Consts.FILE_SIZE;
                }
                readByte = fis.read(x);
                aList.add(x);
                if (readByte > 0) {
                    readByte = 0;
                    startPos += Consts.FILE_SIZE;
                }
            }

            for (int i = 0; i < aList.size(); i++) {
                boolean end = false;
                if (aList.size() - 1 == i) {
                    end = true;
                }
                TransferFileMessage trFm = new TransferFileMessage(filename, file.getAbsolutePath(), fileSize, aList.get(i), true, end);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(trFm);
                oos.flush();
            }
            fis.close();
            aList.clear();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Object obj = ois.readObject();
            if (obj instanceof AnswerMessage) {
                ansMsg = (AnswerMessage) obj;
            }
        }
        return ansMsg;
    }

    public static boolean getFile(TransferFileMessage trFm, String fileName, String pathToFile, Socket socket) throws IOException, ClassNotFoundException {

        boolean gotIt = false;
        File file = new File(pathToFile);
        long size = trFm.getSize();
        ArrayList<byte[]> aList = new ArrayList<>();
        aList.add(trFm.getData());
        while (!trFm.isEndOfFile()) {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            trFm = (TransferFileMessage) ois.readObject();
            aList.add(trFm.getData());
        }

        if (aList.size() > 0) {
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
            for (int i = 0; i < aList.size(); i++) {
                fos.write(aList.get(i));
                fos.flush();
            }
            fos.close();
        }
        aList.clear();

        if (file.exists()) {
            long fileSize = Files.size(Paths.get(file.getAbsolutePath()));
            if (fileSize == size) {
                gotIt = true;
            }
        }
        return gotIt;
    }

    public static boolean getFile(String pathToFile, Socket socket) throws IOException, ClassNotFoundException {
        boolean getFile = false;
        File file = new File(pathToFile);
        if (file.exists()) {
//            AnswerMessage ansMsg = null;
            try {
                AnswerMessage ansMsg = sendFile(file, file.getName(), socket);
                getFile = ansMsg.isResult();
            } catch (InvocationTargetException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return getFile;
    }
}
