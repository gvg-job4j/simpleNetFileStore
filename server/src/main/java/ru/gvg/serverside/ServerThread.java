package ru.gvg.serverside;

import ru.gvg.common.Consts;
import ru.gvg.common.FileActionEnum;
import ru.gvg.common.Network;
import ru.gvg.common.UserActionEnum;
import ru.gvg.messages.*;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

/**
 * Create thread for connecting user.
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
     * Current database connection.
     */
    private BD bd;
    /**
     * Current authorization key.
     */
    private SecretKey threadKey;
    /**
     *
     */
    private MyDropBoxSecurity threadMyDropBoxSecurity;
    /**
     * Current authentication.
     */
    private PasswordAuthentication pa;

    private Connection connection;

//    /**
//     * Metod create thread for client.
//     *
//     * @param mtSever Current server.
//     * @param client  Current client.
//     * @param sArea   Current area for text messages.
//     */
//    public ServerThread(MultiThreadServer mtSever, Socket client, JTextArea sArea) {
//        this.sArea = sArea;
//        this.client = client;
//        this.mtSever = mtSever;
//    }

    /**
     * Metod create thread for client.
     *
     * @param mtSever Current server.
     * @param client  Current client.
     * @param sArea   Current area for text messages.
     */
    public ServerThread(MultiThreadServer mtSever, Socket client, JTextArea sArea, BD bd) {
        this.sArea = sArea;
        this.client = client;
        this.mtSever = mtSever;
//        this.pa = pa;
        this.bd = bd;
    }

    /**
     * Metod start processing client messages.
     */
    @Override
    public void run() {
        try {
            this.connection = bd.connectToDataBase();
            Network.sendAnswerMessage(client, null, true, Consts.DATE_FORMAT.format(new Date()) + ". Socket ready!");
        } catch (SQLException e) {
            Network.sendAnswerMessage(client, null, false, Consts.DATE_FORMAT.format(new Date()) + ". No connect to database!");
            e.printStackTrace();
        }
//        while (!client.isClosed()) {
//            try {
//                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
//                Object obj = ois.readObject();
//                if (obj instanceof TransferFileMessage) {
//                    handleTransferFileMessage((TransferFileMessage) obj);
//                }
//                if (obj instanceof FileMessage) {
//                    handleFileMessage((FileMessage) obj);
//
//                }
//                if (obj instanceof FolderMessage) {
//                    handleFolderMessage((FolderMessage) obj);
//
//                }
//                if (obj instanceof SecurityMessage) {
//                    sendSecurityMessage();
//                }
//
//                if (obj instanceof LoginMessage) {
//                    handleLoginMessage((LoginMessage) obj);
//
//                }
//                if (obj instanceof AnswerMessage) {
//                    AnswerMessage ansmg = (AnswerMessage) obj;
//                    if (ansmg.isResult()) {
//                        sArea.append(Consts.DATE_FORMAT.format(new Date()) + ". " + ansmg.getMsg() + "\n");
//                        break;
//                    }
//                }
//
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//                break;
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                break;
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                break;
//            }
//        }
//        mtSever.getThreadList().remove(this);
    }

    public String getUser() {
        return user;
    }

    public PasswordAuthentication getPa() {
        return pa;
    }

    private String changePathFromLocalToServer(String localName) {
        StringBuilder sb = new StringBuilder(localName);
        if (sb.indexOf(File.pathSeparator) != -1) {
            sb.replace(0, sb.indexOf(File.pathSeparator), bd.getUserUID());
        } else {
            sb.replace(0, sb.length(), bd.getUserUID());
        }
        return sb.toString();
    }

    private void sendSecurityMessage() {
        try {
            threadMyDropBoxSecurity = new MyDropBoxSecurity();
            threadKey = threadMyDropBoxSecurity.getKey();

            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(client.getOutputStream());
                SecurityMessage sMsg = new SecurityMessage(threadKey);
                oos.writeObject(sMsg);
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void handleTransferFileMessage(TransferFileMessage trFm) throws IOException, ClassNotFoundException, SQLException {

        String pathOnServer = changePathFromLocalToServer(trFm.getName());
        String pathToFile = Consts.DIR_PATH + pathOnServer;
        if (trFm.isTransfer() && trFm.isEndOfFile()) {
            if (WorkWithFiles.saveFileOnDisk(pathToFile, trFm.getData())) {
                String fileName = new File(pathToFile).getName();
                if (bd.addFile(connection, fileName, pathToFile, trFm.getPath(), trFm.getSize(), System.currentTimeMillis())) {
                    Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.DATE_FORMAT.format(new Date()) + ". File " + trFm.getName() + " is written on disk");
                } else {
                    Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". Error writing file data " + trFm.getName() + " to BD...");
                }
            } else {
                Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". Error writing file " + trFm.getName() + "...");
            }

        } else if (trFm.isTransfer() && !trFm.isEndOfFile()) {
            String fileName = trFm.getName();
            boolean getFile = Network.getFile(trFm, fileName, pathToFile, client);

            if (getFile) {
                fileName = new File(pathToFile).getName();
                if (bd.addFile(connection, fileName, pathToFile, trFm.getPath(), trFm.getSize(), System.currentTimeMillis())) {
                    Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.DATE_FORMAT.format(new Date()) + ". File " + trFm.getName() + " is written on disk");
                } else {
                    Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". Error writing file data " + trFm.getName() + " to BD...");
                }
            } else {
                Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". Error when write file " + fileName + "...");
            }
        }
    }

    private void handleFileMessage(FileMessage fm) throws IOException, ClassNotFoundException, SQLException {

        //+ get file from ID
        if (fm.getName() == null && fm.getAction().equals(FileActionEnum.GET)) {
            String pathToFile = bd.getFilePathOnServer(connection, fm.getTecPath());
            Network.getFile(pathToFile, client);
            return;
        } // - get file from ID

        String pathOnServer = changePathFromLocalToServer(fm.getName());
        String pathToFile = Consts.DIR_PATH + pathOnServer;

        if (fm.getAction().equals(FileActionEnum.DELETE)) {
            if (WorkWithFiles.deleteFileOnServer(pathToFile)) {
                if (bd.deleteFile(connection, pathToFile)) {
                    Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.DATE_FORMAT.format(new Date()) + ". File " + fm.getName() + " deleted!");
                } else {
                    Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". Error deleting file data " + fm.getName() + " from BD...");
                }
            } else {
                Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". Error when delete file " + fm.getName() + "...");
            }

        } else if (fm.getAction().equals(FileActionEnum.REFRESH)) {
            String localPath = bd.getFileLocalPath(connection, pathToFile);
            if (WorkWithFiles.deleteFileOnServer(pathToFile)) {
                if (bd.deleteFile(connection, pathToFile)) {
                    Network.sendAnswerMessage(client, null, true, localPath);
                } else {
                    Network.sendAnswerMessage(client, null, false, Consts.DATE_FORMAT.format(new Date()) + ". Error deleting file data " + fm.getName() + " from BD...");
                }
            } else {
                Network.sendAnswerMessage(client, null, false, Consts.DATE_FORMAT.format(new Date()) + ". Error when delete file " + pathToFile + "...");
            }

        } else if (fm.getAction().equals(FileActionEnum.GET)) {
            Network.getFile(pathToFile, client);

        } else if (fm.getAction().equals(FileActionEnum.GET_ID)) {
            String fileID = bd.getFileID(connection, pathToFile);
            if (fileID != null) {
                Network.sendAnswerMessage(client, null, true, fileID);
            } else {
                Network.sendAnswerMessage(client, null, false, "Write about file: " + pathToFile + " not found!");
            }

        } else if (fm.getAction().equals(FileActionEnum.RENAME)) {
            File tecFile = WorkWithFiles.getFileOnServer(pathToFile);
            if (tecFile != null) {
                String newPath = WorkWithFiles.createNewPathForFile(tecFile, fm.getNewName());
                if (WorkWithFiles.verifyPathForFile(newPath)) {
                    String tecPath = tecFile.getAbsolutePath();
                    if (WorkWithFiles.renameFileOnServer(tecFile, newPath)) {
                        boolean renamedInBD = bd.renameFile(connection, tecPath, newPath);
                        if (renamedInBD) {
                            Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.DATE_FORMAT.format(new Date()) + ". File " + fm.getName() + "  renamed!");
                        } else {
                            Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". Error when write file data " + fm.getName() + " in BD...");
                        }
                    } else {
                        Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". Error when renamed file " + fm.getName() + "...");
                    }
                } else {
                    Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". File with name " + fm.getName() + " already exists! Try another name!");
                }

            } else {
                Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". File " + fm.getName() + " not found!");
            }

        } else if (fm.getAction().equals(FileActionEnum.TRANSFER)) {
            File tecFile = WorkWithFiles.getFileOnServer(pathToFile);
            String newServerName = changePathFromLocalToServer(fm.getNewName());
            if (tecFile != null) {
                String fileName = tecFile.getName();
                String newPath = Consts.DIR_PATH + newServerName + "\\" + fileName;
                if (WorkWithFiles.transferFileOnServer(tecFile, newServerName)) {
                    if (bd.transferFile(connection, pathToFile, newPath)) {
                        Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.DATE_FORMAT.format(new Date()) + ". File " + fm.getName() + "  transferred!");
                    } else {
                        Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". Error when rewrite file data " + fm.getName() + " in BD...");
                    }
                } else {
                    Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". Error when transfer file " + fm.getName() + "...");
                }
            } else {
                Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". File " + fm.getName() + " not found!");
            }
        }
    }

    private void handleLoginMessage(LoginMessage lm) throws Exception {

        String str = threadMyDropBoxSecurity.decrypt(lm.getStrongName());
        String[] userInfo = str.split(";");
        if (lm.getUserActionEnum().equals(UserActionEnum.CHANGE)) {
            if (userInfo.length != 3) {
                Network.sendAnswerMessage(client, null, false, Consts.DATE_FORMAT.format(new Date()) + ". Invalid user data format! Connection refused...");
                return;
            }
        } else {
            if (userInfo.length != 2) {
                Network.sendAnswerMessage(client, null, false, Consts.DATE_FORMAT.format(new Date()) + ". Invalid user data format! Connection refused...");
                return;
            }
        }
        user = userInfo[0];
        if (lm.getUserActionEnum().equals(UserActionEnum.ADD)) {
            if (bd.getUser(connection, userInfo[0])) {
                Network.sendAnswerMessage(client, null, false, Consts.DATE_FORMAT.format(new Date()) + ". User already exist! Try login.");
            }
            if (bd.addUser(connection, userInfo[0], userInfo[1])) {
                Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " added.");
                sArea.append(Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " connected. \n");

            } else {
                Network.sendAnswerMessage(client, null, false, Consts.DATE_FORMAT.format(new Date()) + ". Error add new user: " + user);
            }

        } else if (lm.getUserActionEnum().equals(UserActionEnum.GET)) {
            ArrayList<ServerThread> threadList = mtSever.getThreadList();

            //+ verify user already connected.
            for (int i = 0; i < threadList.size(); i++) {
                if (threadList.get(i).equals(this)) {
                    continue;
                }
                if (threadList.get(i).getUser().equals(user)) {
                    Network.sendAnswerMessage(client, null, false, Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " already connected! Connection refused.");
                    return;
                }
            } //- verify user already connected.

            if (bd.getUser(connection, userInfo[0])) {
                if (bd.verifyUser(this.connection, userInfo[0], userInfo[1])) {
                    Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " connected.");
                    sArea.append(Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " connected. \n");
                } else {
                    Network.sendAnswerMessage(client, null, false, Consts.DATE_FORMAT.format(new Date()) + ". Wrong login or password! Connection refused.");
                }
            } else {
                Network.sendAnswerMessage(client, null, false, Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " not found! Register new user.");
            }

        } else {
            if (bd.getUser(connection, userInfo[0])) {
                if (bd.verifyUser(this.connection, userInfo[0], userInfo[1])) {
                    if (bd.changePassword(connection, userInfo[0], userInfo[2])) {
                        sArea.append(Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " connected. \n");
                        Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " connected.");
                    } else {
                        Network.sendAnswerMessage(client, null, false, Consts.DATE_FORMAT.format(new Date()) + ". Error change password! Connection refused.");
                    }
                } else {
                    Network.sendAnswerMessage(client, null, false, Consts.DATE_FORMAT.format(new Date()) + ". Wrong login or password! Connection refused.");
                }
            } else {
                Network.sendAnswerMessage(client, null, false, Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " not found! Register new user.");
            }
        }
    }

    private void handleFolderMessage(FolderMessage fdm) {
        String pathOnServer = changePathFromLocalToServer(fdm.getName());
        String pathToDir = Consts.DIR_PATH + pathOnServer;
        if (fdm.isCreate()) {
            if (WorkWithFiles.makeDir(pathOnServer + File.separator + fdm.getNewName())) {
                Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.DATE_FORMAT.format(new Date()) + ". Folder " + fdm.getNewName() + "  created!");
            } else {
                Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". Error when create folder " + fdm.getName() + "...");
            }
        } else if (fdm.isDelete()) {
            if (WorkWithFiles.deleteDirOnServer(pathToDir)) {
                Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.DATE_FORMAT.format(new Date()) + ". Folder " + fdm.getName() + "  deleted!");
            } else {
                Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". Error when delete folder " + fdm.getName() + "...");
            }
        } else {
            File tecFolder = WorkWithFiles.getFolderOnServer(pathToDir);
            if (tecFolder != null) {
                String newPath = WorkWithFiles.createNewPathForFile(tecFolder, fdm.getNewName());
                if (WorkWithFiles.verifyPathForFile(newPath)) {
                    if (WorkWithFiles.renameFolderOnServer(tecFolder, newPath)) {
                        Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.DATE_FORMAT.format(new Date()) + ". Folder " + fdm.getName() + "  renamed!");
                    } else {
                        Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". Error when renamed folder " + fdm.getName() + "...");
                    }
                } else {
                    Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". Folder with name " + fdm.getName() + " already exists! Try another name!");
                }
            } else {
                Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.DATE_FORMAT.format(new Date()) + ". Folder " + fdm.getName() + " not found!");
            }
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public void close() throws Exception {
        if (this.connection != null) {
            this.connection = null;
        }
    }
}
