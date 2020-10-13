package ru.gvg.messages;

import ru.gvg.common.Consts;
import ru.gvg.common.FileActionEnum;
import ru.gvg.common.UserActionEnum;
import ru.gvg.serverside.DataBaseManager;
import ru.gvg.serverside.MyDropBoxSecurity;
import ru.gvg.serverside.ServerThread;
import ru.gvg.serverside.FileManager;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.Key;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Valeriy Gyrievskikh
 * @since 26.09.2019
 */
public class MessageHandler {
    /**
     * Method generates a message {@code SecurityMessage} with a public key for the client.
     *
     * @param serverThread Current server thread.
     * @return message.
     */
    public static Messaging handleSecurityMessage(ServerThread serverThread) {
        MyDropBoxSecurity security = serverThread.getSecurity();
        Key key = security.getOpenKey();
        return new SecurityMessage(key);
    }

    /**
     * Method creates a file on the client from the message({@code TransferFileMessage}).
     *
     * @param socket    Socket for sending a message.
     * @param inMessage message ({@code TransferFileMessage}).
     * @throws IOException Possible exception.
     */
    public static void handleTransferFileMessageOnClient(Socket socket, TransferFileMessage inMessage) throws IOException {
        String pathToFile = inMessage.getName();
        long fileSize = FileManager.writeFile(inMessage, pathToFile);
        String text = "";
        if (fileSize == inMessage.getSize()) {
            text = Consts.DATE_FORMAT.format(new Date()) + ". File copied.";
        } else {
            text = Consts.DATE_FORMAT.format(new Date()) + ". Part number " + inMessage.getCurrent() + " copied.";
        }
        MessageManager.sendMessage(socket, new AnswerMessage(false, text, null, 0));
    }

    /**
     * Method creates a file on the server from a message({@code TransferFileMessage}).
     *
     * @param inMessage    message({@code TransferFileMessage}).
     * @param serverThread Current server thread.
     * @return message.
     * @throws SQLException Possible exception.
     * @throws IOException  Possible exception.
     */
    public static Messaging handleTransferFileMessage(TransferFileMessage inMessage, ServerThread serverThread) throws SQLException, IOException {
        Connection connection = serverThread.getConnection();
        DataBaseManager dataBaseManager = serverThread.getDataBaseManager();
        String userUID = dataBaseManager.getUserUID();
        String pathToFile = FileManager.changePathFromLocalToServer(inMessage.getName(), userUID);
        long fileSize = FileManager.writeFile(inMessage, pathToFile);
        String text = "";
        if (fileSize == inMessage.getSize()) {
            text = Consts.DATE_FORMAT.format(new Date()) + ". File copied.";
            dataBaseManager.addFile(connection, inMessage, pathToFile, fileSize, System.currentTimeMillis());
        } else {
            text = Consts.DATE_FORMAT.format(new Date()) + ". Part number " + inMessage.getCurrent() + " copied.";
        }
        return createAnswerMessage(text, userUID);
    }

    /**
     * Method generates a message({@code AnswerMessage}).
     *
     * @param text    String message.
     * @param userUID User ID.
     * @return message.
     */
    private static Messaging createAnswerMessage(String text, String userUID) {
        File[] userFiles = FileManager.getUserFileStructure(userUID);
        int userSize = FileManager.getUserFileSize(userUID, userFiles);
        return new AnswerMessage(true, text, userFiles, userSize);
    }

    /**
     * Method handles a message({@code FileMessage}).
     *
     * @param fm           message({@code FileMessage}).
     * @param serverThread Current server thread.
     * @return message.
     * @throws SQLException Possible exception.
     */
    public static Messaging handleFileMessage(FileMessage fm, ServerThread serverThread) throws SQLException {
        AnswerMessage aMsg = null;
        Socket client = serverThread.getClient();
        Connection connection = serverThread.getConnection();
        DataBaseManager dataBaseManager = serverThread.getDataBaseManager();
        String userUID = dataBaseManager.getUserUID();
        if (fm.getAction().equals(FileActionEnum.GET_BY_ID)) {
            String pathToFile = dataBaseManager.getFilePathOnServer(connection, fm.getTecPath());
            String filename = fm.getNewName() + FileManager.getFileName(pathToFile);
            aMsg = (AnswerMessage) FileManager.sendFile(pathToFile, filename, client);
        } else {
            String pathToFile = FileManager.changePathFromLocalToServer(fm.getName(), userUID);
            File[] userFiles = FileManager.getUserFileStructure(userUID);
            int userSize = FileManager.getUserFileSize(userUID, userFiles);
            if (fm.getAction().equals(FileActionEnum.DELETE)) {
                if (FileManager.deleteFileOnServer(pathToFile)) {
                    if (dataBaseManager.deleteFile(connection, pathToFile)) {
                        userFiles = FileManager.getUserFileStructure(userUID);
                        userSize = FileManager.getUserFileSize(userUID, userFiles);
                        String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". File " + fm.getName() + " deleted!";
                        aMsg = new AnswerMessage(true, textMsg, userFiles, userSize);
                    } else {
                        String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Error deleting file data " + fm.getName() + " from BD...";
                        aMsg = new AnswerMessage(false, textMsg, userFiles, userSize);
                    }
                } else {
                    String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Error when delete file " + fm.getName() + "...";
                    aMsg = new AnswerMessage(false, textMsg, userFiles, userSize);
                }
            } else if (fm.getAction().equals(FileActionEnum.REFRESH)) {
                String localPath = dataBaseManager.getFileLocalPath(connection, pathToFile);
                if (FileManager.deleteFileOnServer(pathToFile)) {
                    if (dataBaseManager.deleteFile(connection, pathToFile)) {
                        aMsg = new AnswerMessage(true, localPath, null, 0);
                    } else {
                        String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Error deleting file data " + fm.getName() + " from BD...";
                        aMsg = new AnswerMessage(false, textMsg, null, 0);
                    }
                } else {
                    String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Error when delete file " + pathToFile + "...";
                    aMsg = new AnswerMessage(false, textMsg, null, 0);
                }
            } else if (fm.getAction().equals(FileActionEnum.GET)) {
                String filename = fm.getNewName() + FileManager.getFileName(pathToFile);
                aMsg = (AnswerMessage) FileManager.sendFile(pathToFile, filename, client);
            } else if (fm.getAction().equals(FileActionEnum.GET_ID)) {
                String fileID = dataBaseManager.getFileID(connection, pathToFile);
                if (fileID != null) {
                    aMsg = new AnswerMessage(true, fileID, null, 0);
                } else {
                    aMsg = new AnswerMessage(false, "Write about file: " + pathToFile + " not found!", null, 0);
                }
            } else if (fm.getAction().equals(FileActionEnum.RENAME)) {
                File tecFile = FileManager.getFileOnServer(pathToFile);
                if (tecFile != null) {
                    String newPath = FileManager.createNewPathForFile(tecFile, fm.getNewName());
                    if (FileManager.verifyPathForFile(newPath)) {
                        String tecPath = tecFile.getAbsolutePath();
                        if (FileManager.renameFileOnServer(tecFile, newPath)) {
                            boolean renamedInBD = dataBaseManager.renameFile(connection, tecPath, newPath);
                            if (renamedInBD) {
                                userFiles = FileManager.getUserFileStructure(userUID);
                                String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". File " + fm.getName() + "  renamed!";
                                aMsg = new AnswerMessage(true, textMsg, userFiles, userSize);
                            } else {
                                String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Error when write file data " + fm.getName() + " in BD...";
                                aMsg = new AnswerMessage(false, textMsg, userFiles, userSize);
                            }
                        } else {
                            String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Error when renamed file " + fm.getName() + "...";
                            aMsg = new AnswerMessage(false, textMsg, userFiles, userSize);
                        }
                    } else {
                        String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". File with name " + fm.getName() + " already exists! Try another name!";
                        aMsg = new AnswerMessage(false, textMsg, userFiles, userSize);
                    }
                } else {
                    String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". File " + fm.getName() + " not found!";
                    aMsg = new AnswerMessage(false, textMsg, userFiles, userSize);
                }
            } else if (fm.getAction().equals(FileActionEnum.TRANSFER)) {
                File tecFile = FileManager.getFileOnServer(pathToFile);
                String newServerName = FileManager.changePathFromLocalToServer(fm.getNewName(), userUID);
                if (tecFile != null) {
                    String fileName = tecFile.getName();
                    String newPath = Consts.DIR_PATH + newServerName + File.separator + fileName;
                    if (FileManager.transferFileOnServer(tecFile, newServerName)) {
                        if (dataBaseManager.transferFile(connection, pathToFile, newPath)) {
                            String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". File " + fm.getName() + "  transferred!";
                            aMsg = new AnswerMessage(true, textMsg, userFiles, userSize);
                        } else {
                            String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Error when rewrite file data " + fm.getName() + " in BD...";
                            aMsg = new AnswerMessage(false, textMsg, userFiles, userSize);
                        }
                    } else {
                        String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Error when transfer file " + fm.getName() + "...";
                        aMsg = new AnswerMessage(false, textMsg, userFiles, userSize);
                    }
                } else {
                    String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". File " + fm.getName() + " not found!";
                    aMsg = new AnswerMessage(false, textMsg, userFiles, userSize);
                }
            } else if (fm.getAction().equals(FileActionEnum.SEND)) {
                String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". File " + fm.getName() + "  stored on server!";
                aMsg = new AnswerMessage(true, textMsg, userFiles, userSize);
            }
        }
        return aMsg;
    }

    /**
     * Method handles a message({@code LoginMessage}).
     *
     * @param lm           message({@code LoginMessage}).
     * @param serverThread Current server thread.
     * @param sArea        Area for displaying messages.
     * @return message.
     * @throws Exception Possible exception.
     */
    public static Messaging handleLoginMessage(LoginMessage lm, ServerThread serverThread, JTextArea sArea) throws Exception {
        AnswerMessage aMsg = null;
        MyDropBoxSecurity security = serverThread.getSecurity();
        String str = security.decrypt(lm.getStrongName());
        String[] userInfo = str.split(";");
        if ((lm.getUserActionEnum().equals(UserActionEnum.CHANGE) && userInfo.length != 3) || userInfo.length != 2) {
            String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Invalid user data format! Connection refused...";
            aMsg = new AnswerMessage(false, textMsg, null, 0);
        }
        if (aMsg == null) {
            Connection connection = serverThread.getConnection();
            DataBaseManager dataBaseManager = serverThread.getDataBaseManager();
            String user = userInfo[0];
            String userUID = dataBaseManager.getUserUID(connection, user);
            File[] userFiles = FileManager.getUserFileStructure(userUID);
            int userSize = FileManager.getUserFileSize(userUID, userFiles);
            if (lm.getUserActionEnum().equals(UserActionEnum.ADD)) {
                if (dataBaseManager.getUser(connection, userInfo[0])) {
                    String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". User already exist! Try login.";
                    aMsg = new AnswerMessage(false, textMsg, null, 0);
                } else if (dataBaseManager.addUser(connection, userInfo[0], userInfo[1])) {
                    String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " added.";
                    aMsg = new AnswerMessage(true, textMsg, userFiles, userSize);
                    sArea.append(Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " connected. \n");
                } else {
                    String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Error add new user: " + user;
                    aMsg = new AnswerMessage(false, textMsg, null, 0);
                }
            } else if (lm.getUserActionEnum().equals(UserActionEnum.GET)) {
                ArrayList<ServerThread> threadList = serverThread.getMtSever().getThreadList();
                boolean userConnected = false;
                for (ServerThread aThreadList : threadList) {
                    if (!aThreadList.equals(serverThread) && aThreadList.getUser().equals(userUID)) {
                        userConnected = true;
                        break;
                    }
                }
                if (userConnected) {
                    String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " already connected! Connection refused.";
                    aMsg = new AnswerMessage(false, textMsg, null, 0);
                } else {
                    if (dataBaseManager.getUser(connection, userInfo[0])) {
                        if (dataBaseManager.verifyUser(connection, userInfo[0], userInfo[1])) {
                            serverThread.setUser(dataBaseManager.getUserUID());
                            String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " connected.";
                            aMsg = new AnswerMessage(true, textMsg, userFiles, userSize);
                            sArea.append(Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " connected. \n");
                        } else {
                            String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Wrong login or password! Connection refused.";
                            aMsg = new AnswerMessage(false, textMsg, null, 0);
                        }
                    } else {
                        String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " not found! Register new user.";
                        aMsg = new AnswerMessage(false, textMsg, null, 0);
                    }
                }
            } else {
                if (dataBaseManager.getUser(connection, userInfo[0])) {
                    if (dataBaseManager.verifyUser(connection, userInfo[0], userInfo[1])) {
                        if (dataBaseManager.changePassword(connection, userInfo[0], userInfo[2])) {
                            String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " connected.";
                            aMsg = new AnswerMessage(true, textMsg, userFiles, userSize);
                            sArea.append(textMsg + "\n");
                        } else {
                            String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Error change password! Connection refused.";
                            aMsg = new AnswerMessage(false, textMsg, null, 0);
                        }
                    } else {
                        String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Wrong login or password! Connection refused.";
                        aMsg = new AnswerMessage(false, textMsg, null, 0);
                    }
                } else {
                    String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". User " + user + " not found! Register new user.";
                    aMsg = new AnswerMessage(false, textMsg, null, 0);
                }
            }
        }
        return aMsg;
    }

    /**
     * Method handles a message({@code FolderMessage}).
     *
     * @param fdm          message({@code FolderMessage}).
     * @param serverThread Current server thread.
     * @return message.
     */
    public static Messaging handleFolderMessage(FolderMessage fdm, ServerThread serverThread) {
        AnswerMessage aMsg = null;
        String userUID = serverThread.getDataBaseManager().getUserUID();
        String pathOnServer = FileManager.changePathFromLocalToServer(fdm.getName(), userUID);
        File[] userFiles = FileManager.getUserFileStructure(userUID);
        int userSize = FileManager.getUserFileSize(userUID, userFiles);
        if (fdm.isCreate()) {
            if (FileManager.makeDir(pathOnServer + fdm.getNewName())) {
                String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Folder " + fdm.getNewName() + "  created!";
                userFiles = FileManager.getUserFileStructure(userUID);
                aMsg = new AnswerMessage(true, textMsg, userFiles, userSize);
            } else {
                String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Error when create folder " + fdm.getName() + "...";
                aMsg = new AnswerMessage(false, textMsg, userFiles, userSize);
            }
        } else if (fdm.isDelete()) {
            if (FileManager.deleteDirOnServer(pathOnServer)) {
                String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Folder " + fdm.getName() + "  deleted!";
                userFiles = FileManager.getUserFileStructure(userUID);
                aMsg = new AnswerMessage(true, textMsg, userFiles, userSize);
            } else {
                String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Error when delete folder " + fdm.getName() + "...";
                aMsg = new AnswerMessage(false, textMsg, userFiles, userSize);
            }
        } else {
            File tecFolder = FileManager.getFolderOnServer(pathOnServer);
            if (tecFolder != null) {
                String newPath = FileManager.createNewPathForFile(tecFolder, fdm.getNewName());
                if (FileManager.verifyPathForFile(newPath)) {
                    if (FileManager.renameFolderOnServer(tecFolder, newPath)) {
                        String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Folder " + fdm.getName() + "  renamed!";
                        userFiles = FileManager.getUserFileStructure(userUID);
                        aMsg = new AnswerMessage(true, textMsg, userFiles, userSize);
                    } else {
                        String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Error when renamed folder " + fdm.getName() + "...";
                        aMsg = new AnswerMessage(false, textMsg, userFiles, userSize);
                    }
                } else {
                    String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Folder with name " + fdm.getName() + " already exists! Try another name!";
                    aMsg = new AnswerMessage(false, textMsg, userFiles, userSize);
                }
            } else {
                String textMsg = Consts.DATE_FORMAT.format(new Date()) + ". Folder " + fdm.getName() + " not found!";
                aMsg = new AnswerMessage(false, textMsg, userFiles, userSize);
            }
        }
        return aMsg;
    }

    /**
     * Method creates a new message({@code TransferFileMessage}).
     *
     * @param filename          File name.
     * @param newPath           New path to the file.
     * @param currentPathToFile Current path to the file.
     * @param size              File size.
     * @param fileInputData     File data.
     * @param current           Current message number.
     * @param end               Last message number.
     * @return message.
     */
    public static Messaging createTransferFileMessage(String filename, String newPath, String currentPathToFile, long size, byte[] fileInputData, int current, int end) {
        return new TransferFileMessage(filename, newPath, currentPathToFile, size, fileInputData, current, end);
    }
}
