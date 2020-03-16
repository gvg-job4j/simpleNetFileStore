package ru.gvg.client;

import ru.gvg.common.Consts;
import ru.gvg.messages.AnswerMessage;
import ru.gvg.messages.MessageManager;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.text.ParseException;

/**
 * @author Valeriy Gyrievskikh
 * @since 02.09.2019
 */
public class ClientStartFrame extends JFrame {
    /**
     * Current client socket.
     */
    private Socket clientSocket;
    /**
     * Field to enter the server ip-address.
     */
    private JTextField ipAdress = new JTextField("localhost", 8);
    /**
     * Field to enter the server port.
     */
    private JTextField port;
    /**
     * Exit button.
     */
    private JButton exitButton;
    /**
     * Register button.
     */
    private JButton registerButton;
    /**
     * Login button.
     */
    private JButton loginButton;
    /**
     * Cnage password button.
     */
    private JButton changePassButton;
    /**
     * Get file button.
     */
    private JButton getFileButton;
    /**
     * Field to enter the file id.
     */
    private JTextField idField;
    /**
     * Autoconnect value.
     */
    private JCheckBox autoConnect;
    /**
     * Field to enter the user login.
     */
    private JTextField login = new JTextField(12);
    /**
     * Field to enter the user password.
     */
    private JPasswordField password = new JPasswordField(12);
    /**
     * New user password.
     */
    private JPasswordField newPass = new JPasswordField(12);
    /**
     * Messages area.
     */
    private JTextArea textArea;
    /**
     * Break connect value.
     */
    private boolean breakConnect = false;
    /**
     * Connected value.
     */
    private boolean connected;
    /**
     * List of user files.
     */
    private File[] userFiles;
    /**
     * User size value.
     */
    private int userSize;

    /**
     * Method opens client start window.
     *
     * @param args Parameter list.
     */
    public static void main(String[] args) {
        new ClientStartFrame();
    }

    /**
     * Method returns the exit button.
     *
     * @return Exit button.
     */
    public JButton getExitButton() {
        return exitButton;
    }

    /**
     * Method returns the register button.
     *
     * @return Register button.
     */
    public JButton getRegisterButton() {
        return registerButton;
    }

    /**
     * Method returns the login button.
     *
     * @return Login button.
     */
    public JButton getLoginButton() {
        return loginButton;
    }

    /**
     * Method returns the change password button.
     *
     * @return Change password button.
     */
    public JButton getChangePassButton() {
        return changePassButton;
    }

    /**
     * Method returns the get file button.
     *
     * @return Get file button.
     */
    public JButton getGetFileButton() {
        return getFileButton;
    }

    /**
     * Method returns the user login.
     *
     * @return User login.
     */
    public JTextField getLogin() {
        return login;
    }

    /**
     * Method returns the user password.
     *
     * @return User password.
     */
    public JPasswordField getPassword() {
        return password;
    }

    /**
     * Method returns the new user password.
     *
     * @return New user password.
     */
    public JPasswordField getNewPass() {
        return newPass;
    }

    /**
     * Method returns the client socket.
     *
     * @return Client socket.
     */
    public Socket getClientSocket() {
        return clientSocket;
    }

    /**
     * Method returns the file ID field.
     *
     * @return File ID field.
     */
    public JTextField getIdField() {
        return idField;
    }

    /**
     * Method returns the frame text area.
     *
     * @return Frame text area.
     */
    public JTextArea getTextArea() {
        return textArea;
    }

    /**
     * Method returns the auto-connect flag.
     *
     * @return Auto-connect flag.
     */
    public JCheckBox getAutoConnect() {
        return autoConnect;
    }

    /**
     * Method returns whether the connection is terminated.
     *
     * @return Connection is terminated
     */
    public boolean isBreakConnect() {
        return breakConnect;
    }

    /**
     * Method sets whether the connection is terminated.
     */
    public void setBreakConnect(boolean breakConnect) {
        this.breakConnect = breakConnect;
    }

    public JTextField getIpAdress() {
        return ipAdress;
    }

    /**
     * Method sets the list of user files.
     */
    public void setUserFiles(File[] userFiles) {
        this.userFiles = userFiles;
    }

    /**
     * Method sets the size of user files.
     */
    public void setUserSize(int userSize) {
        this.userSize = userSize;
    }

    /**
     * Constructor, set default values for start window.
     */
    public ClientStartFrame() {
        setTitle("Connect to server");
        setResizable(false);
        setLocation(500, 250);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setKeyListeners();
        setStartWiew();
//        readWriteSystemFile(true);
    }

    /**
     * Method establishes a connection to the server.
     *
     * @return Connection status.
     */
    public boolean connectToServer() {
        boolean connected = false;
        if (clientSocket == null || !clientSocket.isConnected()) {
            try {
                clientSocket = new Socket(ipAdress.getText(), Integer.parseInt(port.getText()));
                AnswerMessage inMessage = (AnswerMessage) MessageManager.getMessage(clientSocket);
                textArea.append(inMessage.getMsg() + "\n");
                connected = true;
            } catch (IOException e1) {
                textArea.append("No connection to the server." + "\n");
                e1.printStackTrace();
            }
        } else {
            connected = true;
        }
        return connected;
    }

    /**
     * Method sets key listeners.
     */
    private void setKeyListeners() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());
        login.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (login.getText().length() >= 20) {
                    e.consume();
                }
            }
        });
        password.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (password.getPassword().length >= 20) {
                    e.consume();
                }
            }
        });
        ipAdress.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (ipAdress.getText().length() >= 15) {
                    e.consume();
                }
            }
        });
    }

    /**
     * Method sets frame elements.
     */
    private void setStartWiew() {
        try {
            port = new JFormattedTextField(new DefaultFormatterFactory(new MaskFormatter("####")), Consts.PORT);
        } catch (ParseException e) {
            port = new JTextField(Integer.toString(Consts.PORT));
        }
        Font otherFont = new Font("TimesRoman", Font.BOLD, 16);
        textArea = new JTextArea(10, 20);
        textArea.setEditable(false);
        textArea.setFont(Font.getFont(Font.DIALOG));
        JPanel fieldsPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel ipText = new JLabel("Server IP:");
        JLabel portText = new JLabel("port:");
        fieldsPanel1.add(ipText);
        fieldsPanel1.add(ipAdress);
        fieldsPanel1.add(portText);
        fieldsPanel1.add(port);
        JLabel loginText = new JLabel("Login:");
        fieldsPanel1.add(loginText);
        fieldsPanel1.add(login);
        JLabel passText = new JLabel("Password:");
        fieldsPanel1.add(passText);
        fieldsPanel1.add(password);
        autoConnect = new JCheckBox();
        autoConnect.setFont(otherFont);
        autoConnect.setToolTipText("For break press \"Ctrl\" + \"Q\"");
        JPanel loginTextPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField addIdTextFiled = new JTextField("Autoconnect");
        addIdTextFiled.setEditable(false);
        addIdTextFiled.setFont(otherFont);
        addIdTextFiled.setToolTipText("For break press \"Ctrl\" + \"Q\"");
        loginTextPanel.add(autoConnect);
        loginTextPanel.add(addIdTextFiled);
        exitButton = new JButton("Exit");
        exitButton.addActionListener(new StartFrameActionListener(this));
        loginButton = new JButton("Login");
        loginButton.addActionListener(new StartFrameActionListener(this));
        registerButton = new JButton("Register");
        registerButton.addActionListener(new StartFrameActionListener(this));
        changePassButton = new JButton("Change password");
        changePassButton.addActionListener(new OpenFrameActionListener(this));
        getFileButton = new JButton("Get file by ID");
        getFileButton.addActionListener(new StartFrameActionListener(this));
        idField = new JTextField(18);
        idField.setToolTipText("Enter file ID here...");
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        buttonsPanel.setBorder(BorderFactory.createBevelBorder(1));
        buttonsPanel.add(loginButton);
        buttonsPanel.add(changePassButton);
        buttonsPanel.add(registerButton);
        buttonsPanel.add(exitButton);
        JPanel logPassPanel = new JPanel(new BorderLayout());
        logPassPanel.add(buttonsPanel, BorderLayout.SOUTH);
        logPassPanel.add(loginTextPanel, BorderLayout.NORTH);
        JPanel getFilePanel = new JPanel(new BorderLayout());
        getFilePanel.add(getFileButton, BorderLayout.WEST);
        getFilePanel.add(idField, BorderLayout.CENTER);
        getFilePanel.setBorder(BorderFactory.createBevelBorder(1));
        JPanel fieldsPanel3 = new JPanel(new BorderLayout());
        fieldsPanel3.add(fieldsPanel1, BorderLayout.NORTH);
        fieldsPanel3.add(logPassPanel, BorderLayout.CENTER);
        fieldsPanel3.add(getFilePanel, BorderLayout.SOUTH);
        fieldsPanel3.setBorder(BorderFactory.createBevelBorder(2));
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(BorderFactory.createBevelBorder(1));
        textPanel.add(new JScrollPane(textArea));
        add(fieldsPanel3, BorderLayout.CENTER);
        add(textPanel, BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }

    /**
     * Constructor, sets input message.
     *
     * @param msg Input message.
     */
    public ClientStartFrame(String msg) {
        this();
        textArea.append(msg + "\n");
    }

    /**
     * Method reads data from a settings file or writes data to a settings file.
     *
     * @param read Indicator of data reading.
     */
    private void readWriteSystemFile(boolean read) {
        File loadFile = new File("System");
        StringBuilder stb = new StringBuilder();
        if (read) {
            if (loadFile.exists()) {
                try {
                    FileReader fr = new FileReader(loadFile);
                    int c = 0;
                    while ((c = fr.read()) != -1) {
                        stb.append((char) c);
                    }
                    String[] fileData = stb.toString().split(";");
                    if (fileData.length > 0) {
                        ipAdress.setText(fileData[0]);
                        port.setText(fileData[1]);
                        if (fileData.length == 3) {
                            login.setText(fileData[2]);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                if (!loadFile.exists()) {
                    loadFile.createNewFile();
                }
                stb.append(ipAdress.getText()).append(";").append(port.getText()).append(";").append(login.getText());
                FileWriter fwr = new FileWriter(loadFile, false);
                fwr.write(stb.toString());
                fwr.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method closes the start client window.
     */
    public void closeStartFrame() {
        setVisible(false);
        MessageManager.sendMessage(clientSocket, new AnswerMessage(true, "User disconnected."));
        dispose();
    }

    /**
     * Method sets new password.
     *
     * @param newPass     New user password.
     * @param currentPass Current user password.
     */
    public void setNewPass(JPasswordField newPass, JPasswordField currentPass) {
        this.newPass = newPass;
        this.password = currentPass;
    }

    /**
     * Method breaks connection attempt if a specific key combination is pressed.
     */
    private class MyDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            breakConnect = false;
            if (e.getKeyCode() == KeyEvent.VK_Q && e.isControlDown()) {
                breakConnect = true;
            }
            return breakConnect;
        }
    }

    /**
     * Method set start coordinates for window.
     *
     * @param frame Client frame.
     * @return Start coordinates.
     */
    public static int[] getStartCoords(JFrame frame) {
        int[] coords = new int[2];
        Toolkit kit = frame.getToolkit();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        Insets in = kit.getScreenInsets(gs[0].getDefaultConfiguration());
        Dimension d = kit.getScreenSize();
        coords[0] = (d.width - in.left - in.right);
        coords[1] = (d.height - in.top - in.bottom);
        return coords;
    }
}
