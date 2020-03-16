package ru.gvg.client;

import ru.gvg.common.Consts;
import ru.gvg.common.FileActionEnum;
import ru.gvg.common.UserActionEnum;
import ru.gvg.messages.*;
import ru.gvg.serverside.MyDropBoxSecurity;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Key;
import java.util.Date;

/**
 * @author Valeriy Gyrievskikh
 * @since 02.12.2019
 */
public class StartFrameActionListener implements ActionListener {

    /**
     * Opened user frame.
     */
    private ClientStartFrame frame;

    /**
     * Constructor, sets the user frame.
     *
     * @param frame Opened user frame.
     */
    public StartFrameActionListener(ClientStartFrame frame) {
        this.frame = frame;
    }

    /**
     * Method handles keystrokes.
     *
     * @param e User action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == frame.getExitButton()) {
            handleExit();
        } else {
            lockButtons();
            if (frame.connectToServer()) {
                Socket socket = frame.getClientSocket();
                if (e.getSource() == frame.getGetFileButton()) {
                    if (frame.getIdField().getText().equals("") || frame.getIdField().getText().equals("Enter file ID here...")) {
                        JOptionPane.showMessageDialog(null, "Please enter file ID!", "", JOptionPane.WARNING_MESSAGE);
                    } else {
                        try {
                            getFileOnServerFromID(frame.getIdField().getText(), socket);
                        } catch (IOException ex) {
                            frame.getTextArea().append(Consts.DATE_FORMAT.format(new Date()) + ". Error loading the file! \n");
                            ex.printStackTrace();
                        }
                    }
                } else {
                    UserActionEnum userAction = getCurrentUserAction(e.getSource());
                    if (tryLoginToServer(socket, userAction)) {
                        AnswerMessage inMessage = (AnswerMessage) MessageManager.getMessage(socket);
                        frame.getTextArea().append(Consts.DATE_FORMAT.format(new Date()) + ". " + inMessage.getMsg() + "\n");
                        JOptionPane.showMessageDialog(null, inMessage.getMsg(), "", JOptionPane.WARNING_MESSAGE);
                        if (inMessage.isResult()) {
                            String login = frame.getLogin().getText();
                            frame.dispose();
                            if (inMessage.isResult()) {
                                frame.setUserFiles(inMessage.getFiles());
                                frame.setUserSize(inMessage.getSize());
                            }
                            new ClientGUI(socket, inMessage.getFiles(), inMessage.getSize(), login);
                        }
                    }
                }
            } else {
                frame.getTextArea().append(Consts.DATE_FORMAT.format(new Date()) + ". Unable to connect to server! \n");
            }
            unlockButtons();
        }
    }

    /**
     * Method performs the window closing procedure.
     */
    public void handleExit() {
        Socket socket = frame.getClientSocket();
        if (socket != null && socket.isConnected()) {
            String text = "";
            try {
                text = "Client " + InetAddress.getLocalHost().getHostAddress() + " closed.";
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            MessageManager.sendMessage(socket, new AnswerMessage(true, text));
        }
        frame.closeStartFrame();
        System.exit(0);
    }

    /**
     * Method gets file data on server and save file on local disk.
     *
     * @param fileID File ID.
     * @param socket Client socket.
     * @throws IOException Possible exception.
     */
    private void getFileOnServerFromID(String fileID, Socket socket) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = fileChooser.showDialog(null, "Select file");
        if (ret == JFileChooser.APPROVE_OPTION) {
            String filename = "";
            File file = fileChooser.getSelectedFile();
            MessageManager.sendMessage(socket, new FileMessage(null, FileActionEnum.GET_BY_ID, file.getAbsolutePath(), fileID));
            while (true) {
                Messaging inMessage = MessageManager.getMessage(socket);
                if (inMessage instanceof AnswerMessage) {
                    AnswerMessage message = (AnswerMessage) inMessage;
                    frame.getTextArea().append(Consts.DATE_FORMAT.format(new Date()) + ". " + message.getMsg() + "\n");
                    MessageManager.sendMessage(socket, new AnswerMessage(true, "File transferred!"));
                    String text = "File saved as: " + filename;
                    JOptionPane.showMessageDialog(null, text, "", JOptionPane.INFORMATION_MESSAGE);
                    break;
                } else {
                    TransferFileMessage message = (TransferFileMessage) inMessage;
                    if (filename.isEmpty()) {
                        filename = message.getName();
                    }
                    MessageHandler.handleTransferFileMessageOnClient(socket, message);
                }
            }
            frame.closeStartFrame();
        }
    }

    /**
     * Method establishes a connection with the server.
     *
     * @param clientSocket Client socket.
     * @param userAction   Current user action.
     * @return A result of connection.
     */
    private boolean tryLoginToServer(Socket clientSocket, UserActionEnum userAction) {
        boolean logIn = false;
        if (MessageManager.sendMessage(clientSocket, new SecurityMessage())) {
            Messaging inMessage = MessageManager.getMessage(clientSocket);
            LoginMessage outMessage = prepareLoginMessage(((SecurityMessage) inMessage).getKey(), userAction);
            logIn = MessageManager.sendMessage(clientSocket, outMessage);
        }
        return logIn;
    }

    /**
     * Method creates message ({@code LoginMessage})
     *
     * @param clientKey  Publc key for encryption.
     * @param userAction Current user action.
     * @return Message.
     */
    private LoginMessage prepareLoginMessage(Key clientKey, UserActionEnum userAction) {
        LoginMessage message = null;
        if (clientKey != null) {
            StringBuilder sb = new StringBuilder();
            String sbPassCurrent = getDataFromPassField(frame.getPassword());
            if (userAction == UserActionEnum.CHANGE) {
                String sbPassNew = getDataFromPassField(frame.getNewPass());
                sb.append(frame.getLogin().getText()).append(";").append(sbPassCurrent).append(";").append(sbPassNew);
            } else {
                sb.append(frame.getLogin().getText()).append(";").append(sbPassCurrent);
            }
            String loginStr = MyDropBoxSecurity.encrypt(sb.toString(), clientKey);
            if (loginStr != null) {
                message = new LoginMessage(loginStr, userAction);
            } else {
                frame.getTextArea().append(Consts.DATE_FORMAT.format(new Date()) + ". Security error! Connection is not allowed...\n");
            }
        } else {
            frame.getTextArea().append(Consts.DATE_FORMAT.format(new Date()) + ". Security key not found! Connection is not allowed...\n");
        }
        return message;
    }

    /**
     * Method пenerates a string from an char array.
     *
     * @param password Char array.
     * @return String.
     */
    private String getDataFromPassField(JPasswordField password) {
        char[] arrayPass = password.getPassword();
        StringBuilder sbPass = new StringBuilder();
        for (int i = 0; i < arrayPass.length; i++) {
            sbPass.append(arrayPass[i]);
        }
        return sbPass.toString();
    }
//
//    /**
//     * Method returns user frame.
//     *
//     * @return User frame.
//     */
//    public ClientStartFrame getFrame() {
//        return this.frame;
//    }

    /**
     * Method returns current user action.
     *
     * @param source The button that was pressed.
     * @return Current user action.
     */
    private UserActionEnum getCurrentUserAction(Object source) {
        UserActionEnum action = UserActionEnum.GET;
        if (source == frame.getRegisterButton()) {
            action = UserActionEnum.ADD;
        }
        if (source == frame.getChangePassButton()) {
            action = UserActionEnum.CHANGE;
        }
        return action;
    }

    /**
     * Method unlocks the buttons.
     */
    private void unlockButtons() {
        frame.getGetFileButton().setEnabled(true);
        frame.getRegisterButton().setEnabled(true);
        frame.getLoginButton().setEnabled(true);
        frame.getChangePassButton().setEnabled(true);
    }

    /**
     * Method locks the buttons.
     */
    private void lockButtons() {
        frame.getGetFileButton().setEnabled(false);
        frame.getRegisterButton().setEnabled(false);
        frame.getLoginButton().setEnabled(false);
        frame.getChangePassButton().setEnabled(false);
    }

}
