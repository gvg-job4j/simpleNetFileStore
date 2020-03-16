package ru.gvg.client;

import ru.gvg.common.FileActionEnum;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import static java.awt.event.KeyEvent.KEY_RELEASED;

/**
 * Processing keystrokes.
 *
 * @author Valeriy Gyrievskikh
 * @since 20.10.2019
 */
public class GUIListener implements KeyEventDispatcher, ActionListener {

    /**
     * User window.
     */
    private ClientGUI clientFrame;
    /**
     * User action handler.
     */
    private ClientActionHandler handler;

    /**
     * Constructor, set user window.
     *
     * @param clientFrame User window.
     */
    public GUIListener(ClientGUI clientFrame) {
        this.clientFrame = clientFrame;
        this.handler = new ClientActionHandler(clientFrame);
    }

    /**
     * Method handles keystrokes.
     *
     * @param e Key event.
     * @return Result of processing.
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        boolean handled = false;
        if (e.getID() == KEY_RELEASED) {
            if (e.getKeyCode() == KeyEvent.VK_DELETE && e.isControlDown()) {
                deleteFolder();
                handled = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_R && e.isControlDown()) {
                renameFolder();
                handled = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_C && e.isControlDown()) {
                createFolder();
                handled = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_S) {
                sendFile();
                handled = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                deleteFile();
                handled = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_G) {
                getFile();
                handled = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_I) {
                getFileID();
                handled = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_A && e.isAltDown()) {
                refreshFile();
                handled = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_R) {
                renameFile();
                handled = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_T) {
                transferFile();
                handled = true;
            }
        }
        return handled;
    }

    /**
     * Method handles user action events.
     *
     * @param e Action event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        clientFrame.restartTimer();
        if (clientFrame.getClientSocket() != null && !clientFrame.getClientSocket().isClosed()) {
            if (e.getSource() == clientFrame.getGetFileButton()) {
                getFile();
            } else if (e.getSource() == clientFrame.getGetIdButton()) {
                getFileID();
            } else if (e.getSource() == clientFrame.getDeleteFileButton()) {
                deleteFile();
            } else if (e.getSource() == clientFrame.getRenameFileButton()) {
                renameFile();
            } else if (e.getSource() == clientFrame.getTransferFileButton()) {
                transferFile();
            } else if (e.getSource() == clientFrame.getRefreshFileButton()) {
                refreshFile();
            } else if (e.getSource() == clientFrame.getSendFileButton()) {
                sendFile();
            } else if (e.getSource() == clientFrame.getCreateDirButton()) {
                createFolder();
            } else if (e.getSource() == clientFrame.getRenameDirButton()) {
                renameFolder();
            } else if (e.getSource() == clientFrame.getDeleteDirButton()) {
                deleteFolder();
            }
        } else {
            clientFrame.closeClientGUI("Socket closed!");
        }
    }

    /**
     * Method shows a warning message if wrong file path selected.
     *
     * @return Warning message was shown.
     */
    private boolean showFileWarningMessage() {
        boolean isSelected = false;
        if (clientFrame.getSelectedPath().length() == 0 || clientFrame.getTecNode() == null || clientFrame.getTecNode().getAllowsChildren()) {
            JOptionPane.showMessageDialog(clientFrame, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
            isSelected = true;
        }
        return isSelected;
    }

    /**
     * Method shows a warning message if wrong folder path selected.
     *
     * @return Warning message was shown.
     */
    private boolean showFolderWarningMessage() {
        boolean isSelected = false;
        if (clientFrame.getSelectedPath().length() == 0 || clientFrame.getTecNode() == null || !clientFrame.getTecNode().getAllowsChildren()) {
            JOptionPane.showMessageDialog(null, "Folder not selected!\n", "", JOptionPane.WARNING_MESSAGE);
            isSelected = true;
        }
        return isSelected;
    }

    /**
     * Method calls the file upload function.
     */
    private void sendFile() {
        handler.sendFileMessage(FileActionEnum.SEND, null, null);
    }

    /**
     * Method calls the file delete function.
     */
    private void deleteFile() {
        if (!showFileWarningMessage()) {
            int res = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the file?");
            if (res != JOptionPane.NO_OPTION) {
                handler.sendFileMessage(FileActionEnum.DELETE, null, null);
            }
        }
    }

    /**
     * Method calls the function to get the file.
     */
    private void getFile() {
        if (!showFileWarningMessage()) {
            handler.sendFileMessage(FileActionEnum.GET, null, null);
        }
    }

    /**
     * Method calls the function to get the ID.
     */
    private void getFileID() {
        if (!showFileWarningMessage()) {
            handler.sendFileMessage(FileActionEnum.GET_ID, null, null);
        }
    }

    /**
     * Method calls the file rename function.
     */
    private void renameFile() {
        if (!showFileWarningMessage()) {
            String newFileName = JOptionPane.showInputDialog(clientFrame, "Input file name:");
            if (newFileName != null) {
                handler.sendFileMessage(FileActionEnum.RENAME, newFileName, null);
            }
        }
    }

    /**
     * Method calls the file transfer function.
     */
    private void transferFile() {
        if (!showFileWarningMessage()) {
            StringBuilder tecFolder = clientFrame.getSelectedPath();
            if (clientFrame.getTecNode().isLeaf()) {
                tecFolder = clientFrame.getFolder(clientFrame.getSelectedPath().toString());
            }
            String tecPath = tecFolder.toString();
            Object res = JOptionPane.showInputDialog(clientFrame, "Select new folder for file:", "", JOptionPane.QUESTION_MESSAGE,
                    null, clientFrame.getFolderList().toArray(), clientFrame.getFolderList().get(0));
            if (res != null) {
                String newFolder = (String) res;
                if (newFolder.equals(tecPath)) {
                    JOptionPane.showMessageDialog(null, "New folder not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                } else {
                    handler.sendFileMessage(FileActionEnum.TRANSFER, newFolder, tecPath);
                }
            } else {
                JOptionPane.showMessageDialog(null, "New folder not selected!\n", "", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Method calls the function to refresh file data.
     */
    private void refreshFile() {
        if (!showFileWarningMessage()) {
            String tecPath = clientFrame.getSelectedPath().toString();
            handler.sendFileMessage(FileActionEnum.REFRESH, null, tecPath);
        }
    }

    /**
     * Method calls the folder delete function.
     */
    private void deleteFolder() {
        if (!showFolderWarningMessage()) {
            handler.sendFolderMessage(false, true);
        }
    }

    /**
     * Method calls the folder rename function.
     */
    private void renameFolder() {
        if (!showFolderWarningMessage()) {
            handler.sendFolderMessage(false, false);
        }
    }

    /**
     * Method calls the folder create function.
     */
    private void createFolder() {
        handler.sendFolderMessage(true, false);
    }
}
