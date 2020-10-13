package ru.gvg.client;

import ru.gvg.common.Consts;
import ru.gvg.common.FileActionEnum;
import ru.gvg.messages.*;
import ru.gvg.serverside.FileManager;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Handles user selection in the client window.
 *
 * @author Valeriy Gyrievskikh
 * @since 15.01.2020
 */
public class ClientActionHandler {

    /**
     * Client window.
     */
    private final ClientGUI frame;

    /**
     * Constructor, set client window.
     *
     * @param frame Client window.
     */
    public ClientActionHandler(ClientGUI frame) {
        this.frame = frame;
    }

    /**
     * Metod processed the selected user action.
     *
     * @param type    Selected user action.
     * @param newName New file name (if user action is RENAME or TRANSFER), or null.
     * @param tecPath Current path to the file.
     */
    void sendFileMessage(FileActionEnum type, String newName, String tecPath) {
        if (type.equals(FileActionEnum.SEND)) {
            handleActionSendFile();
        } else if (type.equals(FileActionEnum.GET)) {
            handleActionGetFile();
        } else if (type.equals(FileActionEnum.GET_ID)) {
            handleActionGetID();
        } else if (type.equals(FileActionEnum.RENAME)) {
            handleActionRenameFile(newName);
        } else if (type.equals(FileActionEnum.TRANSFER)) {
            handleActionTransferFile(newName, tecPath);
        } else if (type.equals(FileActionEnum.DELETE)) {
            handleActionDeleteFile();
        } else if (type.equals(FileActionEnum.REFRESH)) {
            handleActionRefresh(tecPath);
        }
    }

    /**
     * Method saves the selected file to the server.
     */
    private void handleActionSendFile() {
        File file = chooseFile(JFileChooser.FILES_AND_DIRECTORIES);
        if (file != null) {
            int freeSpace = Consts.USER_SIZE - frame.getUserSize();
            int fileSize = convertToMb((int) file.length());
            if (fileSize <= freeSpace) {
                String filename = file.getName();
                if (frame.getSelectedPath().length() != 0 && frame.getTecNode() != null
                        && frame.getTecNode().getAllowsChildren()) {
                    filename = frame.getSelectedPath().toString() + File.separator + file.getName();
                }
                frame.getProgressBar().showBar();
                AnswerMessage inMessage = (AnswerMessage) FileManager.sendFile(file.getAbsolutePath(), filename, frame.getClientSocket());
                handleAnswerMessage(inMessage);
                frame.getProgressBar().hideBar();
            } else {
                JOptionPane.showMessageDialog(frame, "File size: " + fileSize + " MB, free space: "
                        + freeSpace + " MB. Transfer canselled.", "", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Method gets file from server to local disk.
     */
    private void handleActionGetFile() {
        File file = chooseFile(JFileChooser.DIRECTORIES_ONLY);
        if (file != null) {
            if (frame.getTecNode() == null || !frame.getTecNode().isLeaf()) {
                frame.getTextArea().append(Consts.DATE_FORMAT.format(new Date()) + ". File not selected!\n");
            } else {
                frame.getProgressBar().showBar();
                try {
                    String filename = "";
                    StringBuilder currentFullFileName = new StringBuilder(frame.getTecNode().toString());
                    TreeNode currentNode = frame.getTecNode();
                    while (currentNode.getParent() != null) {
                        currentFullFileName.insert(0, File.separator).insert(0, currentNode.getParent().toString());
                        currentNode = currentNode.getParent();
                    }
                    MessageManager.sendMessage(frame.getClientSocket(), new FileMessage(currentFullFileName.toString(), FileActionEnum.GET, file.getAbsolutePath(), null));
                    while (true) {
                        Messaging inMessage = MessageManager.getMessage(frame.getClientSocket());
                        if (inMessage instanceof AnswerMessage) {
                            AnswerMessage message = (AnswerMessage) inMessage;
                            frame.getTextArea().append(Consts.DATE_FORMAT.format(new Date()) + ". " + message.getMsg() + "\n");
                            String text = "File saved as: " + filename;
                            JOptionPane.showMessageDialog(null, text, "", JOptionPane.INFORMATION_MESSAGE);
                            break;
                        } else {
                            TransferFileMessage message = (TransferFileMessage) inMessage;
                            if (filename.isEmpty()) {
                                filename = message.getName();
                            }
                            MessageHandler.handleTransferFileMessageOnClient(frame.getClientSocket(), message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                frame.getProgressBar().hideBar();
            }
        }
    }

    /**
     * Method gets the file ID.
     */
    private void handleActionGetID() {
        if (frame.getTecNode() == null || !frame.getTecNode().isLeaf()) {
            frame.getTextArea().append(Consts.DATE_FORMAT.format(new Date()) + ". File not selected!\n");
        } else {
            FileMessage fm = new FileMessage(frame.getSelectedPath().toString(), FileActionEnum.GET_ID, null, null);
            MessageManager.sendMessage(frame.getClientSocket(), fm);
            AnswerMessage inMessage = (AnswerMessage) MessageManager.getMessage(frame.getClientSocket());
            frame.getTextArea().append(Consts.DATE_FORMAT.format(new Date()) + ". File ID: " + inMessage.getMsg() + "\n");
            JOptionPane.showMessageDialog(frame, "File ID: "
                    + inMessage.getMsg() + "\n", "", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Method renames the file.
     *
     * @param newName New file name.
     */
    private void handleActionRenameFile(String newName) {
        String filename = frame.getSelectedPath().toString();
        if (frame.getSelectedPath().length() == 0 || frame.getTecNode() == null || !frame.getTecNode().isLeaf()) {
            frame.getTextArea().append(Consts.DATE_FORMAT.format(new Date()) + ". File not selected!" + "\n");
        } else {
            FileMessage fm = new FileMessage(filename, FileActionEnum.RENAME, newName, null);
            MessageManager.sendMessage(frame.getClientSocket(), fm);
            AnswerMessage inMessage = (AnswerMessage) MessageManager.getMessage(frame.getClientSocket());
            handleAnswerMessage(inMessage);
        }
    }

    /**
     * Method thransfers the file.
     *
     * @param newName New file name.
     * @param tecPath Current path to the file.
     */
    private void handleActionTransferFile(String newName, String tecPath) {
        FileMessage fm = new FileMessage(tecPath, FileActionEnum.TRANSFER, newName, tecPath);
        MessageManager.sendMessage(frame.getClientSocket(), fm);
        AnswerMessage inMessage = (AnswerMessage) MessageManager.getMessage(frame.getClientSocket());
        handleAnswerMessage(inMessage);
    }

    /**
     * Method deletes the file.
     */
    private void handleActionDeleteFile() {
        String pathToFile = frame.getSelectedPath().toString();
        FileMessage fm = new FileMessage(pathToFile, FileActionEnum.DELETE, null, null);
        MessageManager.sendMessage(frame.getClientSocket(), fm);
        AnswerMessage inMessage = (AnswerMessage) MessageManager.getMessage(frame.getClientSocket());
        handleAnswerMessage(inMessage);
    }

    /**
     * Method updates the file data.
     *
     * @param tecPath Current path to the file.
     */
    private void handleActionRefresh(String tecPath) {
        if (frame.getSelectedPath().length() != 0 && frame.getTecNode().getAllowsChildren()) {
            return;
        }
        frame.getProgressBar().showBar();
        FileMessage fm = new FileMessage(tecPath, FileActionEnum.REFRESH, null, null);
        MessageManager.sendMessage(frame.getClientSocket(), fm);
        AnswerMessage aMsg = (AnswerMessage) MessageManager.getMessage(frame.getClientSocket());
        if (aMsg.isResult()) {
            frame.getTextArea().append(aMsg.getMsg() + "\n");
            File file = new File(aMsg.getMsg());
            String filename = makeFileName(true, file);
            if (file.exists() && file.isFile()) {
                AnswerMessage ansMsg = (AnswerMessage) FileManager.sendFile(file.getAbsolutePath(), filename, frame.getClientSocket());
                handleAnswerMessage(ansMsg);
            }
        }
        frame.getProgressBar().hideBar();
    }

    /**
     * Method generates the file name with paths.
     *
     * @param refreshing Indicates the file data is updated.
     * @param file       Current file.
     * @return New file name.
     */
    private String makeFileName(boolean refreshing, File file) {
        String filename = frame.getLogin() + File.separator + file.getName();
        if (frame.getSelectedPath().length() != 0 && frame.getTecNode().getAllowsChildren()) {
            filename = frame.getSelectedPath().toString() + File.separator + file.getName();
        } else if (frame.getTecNode() != null && !frame.getTecNode().getAllowsChildren()) {
            int index = frame.getSelectedPath().lastIndexOf(frame.getTecNode().toString());
            if (index != -1) {
                StringBuilder newSelectedPath = frame.getSelectedPath().delete(index, frame.getSelectedPath().length());
                filename = newSelectedPath.toString() + (refreshing ? file.getName() : File.separator + file.getName());
            } else {
                filename = frame.getSelectedPath().toString() + (refreshing ? file.getName() : File.separator + file.getName());
            }
        }
        return filename;
    }

    /**
     * Method handles the answer from server.
     *
     * @param ansMsg Message from server.
     */
    private void handleAnswerMessage(AnswerMessage ansMsg) {
        if (ansMsg != null) {
            if (ansMsg.isResult() && ansMsg.getFiles() != null) {
                frame.setUserSize(convertToMb(ansMsg.getSize()));
                frame.refreshTree(ansMsg.getFiles());
            }
            frame.getTextArea().append(Consts.DATE_FORMAT.format(new Date()) + ". " + ansMsg.getMsg() + "\n");
        } else {
            frame.getTextArea().append(Consts.DATE_FORMAT.format(new Date()) + ". Something wrong...\n");
        }
    }

    /**
     * Method converts the size data into MB.
     *
     * @param userSize Occupied space on server.
     * @return Occupied space in MB.
     */
    private int convertToMb(int userSize) {
        return (userSize / (1024 * 1024));
    }

    /**
     * Method returns selected file.
     *
     * @param selectionMode Selection mode.
     * @return File or null.
     */
    private File chooseFile(int selectionMode) {
        File file = null;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(selectionMode);
        int ret = fileChooser.showDialog(frame, "Select file");
        if (ret == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        }
        return file;
    }

    /**
     * Metod send message {@code FolderMessage} with parameters.
     *
     * @param create Folder creation flag.
     * @param delete Folder deletion flag.
     */
    void sendFolderMessage(boolean create, boolean delete) {
        FolderMessage fdm = null;
        if (delete) {
            fdm = new FolderMessage(getPathToFolder(false), false, true, null);
        } else {
            String dialogText = create ? "Input folder name:" : "Input new folder name:";
            String newFolderName = JOptionPane.showInputDialog(dialogText);
            if (newFolderName != null) {
                String folderName = create ? getPathToFolder(true) : frame.getSelectedPath().toString() + File.separator;
                fdm = new FolderMessage(folderName, create, false, newFolderName);
            }
        }
        MessageManager.sendMessage(frame.getClientSocket(), fdm);
        AnswerMessage inMessage = (AnswerMessage) MessageManager.getMessage(frame.getClientSocket());
        handleAnswerMessage(inMessage);
    }

    /**
     * Method get path to user folder.
     *
     * @param create Folder creation flag.
     * @return Folder path.
     */
    private String getPathToFolder(boolean create) {
        String folderName = frame.getLogin() + (create ? File.separator : "");
        if (frame.getSelectedPath().length() != 0 && frame.getTecNode().getAllowsChildren()) {
            folderName = frame.getSelectedPath().toString() + (create ? File.separator : "");
        } else if (frame.getTecNode() != null && !frame.getTecNode().getAllowsChildren()) {
            int index = frame.getSelectedPath().lastIndexOf(frame.getTecNode().toString());
            StringBuilder newSelectedPath = frame.getSelectedPath().delete(index, frame.getSelectedPath().length());
            folderName = newSelectedPath.toString() + (create ? File.separator : "");
        }
        return folderName;
    }
}
