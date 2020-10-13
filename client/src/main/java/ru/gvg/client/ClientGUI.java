package ru.gvg.client;

import ru.gvg.common.Consts;
import ru.gvg.messages.*;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

import java.util.ArrayList;

/**
 * The main user window.
 *
 * @author Valeriy Gyrievskikh
 * @since 20.10.2019
 */
class ClientGUI extends JFrame {

    /**
     * Button "Send file".
     */
    private JButton sendFileButton;
    /**
     * Button "Get ID".
     */
    private JButton getIdButton;
    /**
     * Button "Get file".
     */
    private JButton getFileButton;
    /**
     * Button "Delete file".
     */
    private JButton deleteFileButton;
    /**
     * Button "Refresh file".
     */
    private JButton refreshFileButton;
    /**
     * Button "Rename file".
     */
    private JButton renameFileButton;
    /**
     * Button "Transfer file".
     */
    private JButton transferFileButton;
    /**
     * Button "Create dir".
     */
    private JButton createDirButton;
    /**
     * Button "Rename dir".
     */
    private JButton renameDirButton;
    /**
     * Button "Delete dir".
     */
    private JButton deleteDirButton;
    /**
     * Buttons panel.
     */
    private JPanel buttonsPanel;
    /**
     * Message area.
     */
    private JTextArea textArea;
    /**
     * Center panel.
     */
    private JPanel centerPanel;
    /**
     * Content panel.
     */
    private JPanel contents;
    /**
     * Tree of user files.
     */
    private JTree fileTree;
    /**
     * Selected path.
     */
    private StringBuilder selectedPath;
    /**
     * Tree node.
     */
    private DefaultMutableTreeNode tecNode;
    /**
     * Folder list.
     */
    private ArrayList<String> folderList = new ArrayList<>();
    /**
     * Client socket.
     */
    private Socket clientSocket;
    /**
     * User login.
     */
    private String login;
    /**
     * Size of the used space.
     */
    private int userSize;

    /**
     * Bar showing the amount of space occupied.
     */
    private JProgressBar sizeBar;

    /**
     * Session timer.
     */
    private TimerLabel timerLabel;
    /**
     * Progress bar.
     */
    private MyProgressBarSimple pBar;
    /**
     * Selected file.
     */
    private File file;

    /**
     * Constructor with parameters.
     *
     * @param clientSocket Client socket.
     * @param userFiles    List of user files.
     * @param userSize     Occupied space on server.
     * @param login        User login.
     */
    public ClientGUI(Socket clientSocket, File[] userFiles, int userSize, String login) {
        this();
        this.clientSocket = clientSocket;
        this.login = login;
        this.userSize = convertToMb(userSize);
        refreshTree(userFiles);
    }

    /**
     * User GUI constructor.
     */
    public ClientGUI() {
        super();
        selectedPath = new StringBuilder();
        setTitle("Client window");
        setSize(600, 600);
        setMinimumSize(new Dimension(300, 300));
        int[] coords = ClientStartFrame.getStartCoords(this);
        setLocation((int) (coords[0] - getWidth()) / 2, (int) (coords[1] - getHeight()) / 2);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new ClientGUIWindowAdapter(this));
        timerLabel = new TimerLabel(this);
        timerLabel.setFont(new Font(timerLabel.getFont().getFontName(), timerLabel.getFont().getStyle(), 25));
        JLabel textTimer = new JLabel("Until the end of the current session remained:");
        textTimer.setFont(new Font(textTimer.getFont().getFontName(), textTimer.getFont().getStyle(), 20));
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        loginPanel.add(textTimer);
        loginPanel.add(timerLabel);
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new GUIListener(this));
        getIdButton = new JButton("Get file ID");
        getIdButton.setToolTipText("Hot key \"I\"");
        getIdButton.addActionListener(new GUIListener(this));
        sendFileButton = new JButton("Send file to storage");
        sendFileButton.setToolTipText("Hot key \"S\"");
        sendFileButton.addActionListener(new GUIListener(this));
        getFileButton = new JButton("Get selected file");
        getFileButton.setToolTipText("Hot key \"G\"");
        getFileButton.addActionListener(new GUIListener(this));
        deleteFileButton = new JButton("Delete selected file");
        deleteFileButton.setToolTipText("Hot key \"Del\"");
        deleteFileButton.addActionListener(new GUIListener(this));
        refreshFileButton = new JButton("Refresh selected file");
        refreshFileButton.setToolTipText("Hot key \"Alt\" + \"R\"");
        refreshFileButton.addActionListener(new GUIListener(this));
        renameFileButton = new JButton("Rename selected file");
        renameFileButton.setToolTipText("Hot key \"R\"");
        renameFileButton.addActionListener(new GUIListener(this));
        transferFileButton = new JButton("Transfer selected file");
        transferFileButton.setToolTipText("Hot key \"T\"");
        transferFileButton.addActionListener(new GUIListener(this));
        createDirButton = new JButton("Create folder");
        createDirButton.setToolTipText("Hot key \"Ctrl\" + \"C\"");
        createDirButton.addActionListener(new GUIListener(this));
        renameDirButton = new JButton("Rename selected folder");
        renameDirButton.setToolTipText("Hot key \"Ctrl\" + \"R\"");
        renameDirButton.addActionListener(new GUIListener(this));
        deleteDirButton = new JButton("Delete selected folder");
        deleteDirButton.setToolTipText("Hot key \"Ctrl\" + \"Del\"");
        deleteDirButton.addActionListener(new GUIListener(this));
        buttonsPanel = new JPanel(new GridLayout(13, 0, 5, 5));
        buttonsPanel.setMinimumSize(buttonsPanel.getSize());
        buttonsPanel.setMaximumSize(buttonsPanel.getSize());
        JLabel fileActions = new JLabel("File actions:");
        fileActions.setBorder(BorderFactory.createBevelBorder(0));
        buttonsPanel.add(fileActions);
        buttonsPanel.add(sendFileButton);
        buttonsPanel.add(getFileButton);
        buttonsPanel.add(getIdButton);
        buttonsPanel.add(renameFileButton);
        buttonsPanel.add(refreshFileButton);
        buttonsPanel.add(transferFileButton);
        buttonsPanel.add(deleteFileButton);
        JLabel folderActions = new JLabel("Folder actions:");
        folderActions.setBorder(BorderFactory.createBevelBorder(0));
        buttonsPanel.add(folderActions);
        buttonsPanel.add(createDirButton);
        buttonsPanel.add(renameDirButton);
        buttonsPanel.add(deleteDirButton);
        buttonsPanel.setBorder(BorderFactory.createBevelBorder(1));
        textArea = new JTextArea(5, 20);
        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        centerPanel.setBorder(BorderFactory.createBevelBorder(1));
        contents = new JPanel(new BorderLayout());
        sizeBar = new JProgressBar();
        sizeBar.setStringPainted(true);
        sizeBar.setString("Used: " + userSize + " МВ from: " + Consts.USER_SIZE + " MB.");
        sizeBar.setMinimum(0);
        sizeBar.setMaximum(Consts.USER_SIZE);
        sizeBar.setValue(userSize);
        sizeBar.setBorder(BorderFactory.createBevelBorder(1));
        fileTree = new JTree(createTreeModel("User files will be here...", null));
        contents.add(new JScrollPane(fileTree), BorderLayout.CENTER);
        contents.add(sizeBar, BorderLayout.NORTH);
        contents.setBorder(BorderFactory.createBevelBorder(1));
        pBar = new MyProgressBarSimple();
        pBar.showBar();
        add(loginPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.SOUTH);
        add(contents, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.EAST);
        setMinimumSize(getSize());
        setVisible(true);
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
     * Method closed client window.
     *
     * @param msg Message for user.
     */
    public void closeClientGUI(String msg) {
        try {
            MessageManager.sendMessage(clientSocket, new AnswerMessage(true, "User disconnected."));
            clientSocket.close();
            clientSocket = null;
            setVisible(false);
            if (msg != null) {
                new ClientStartFrame(msg);
            } else {
                new ClientStartFrame();
            }
            dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method returns used memory size.
     *
     * @return Used memory size.
     */
    public int getUserSize() {
        return userSize;
    }

    /**
     * Method gets client socket.
     *
     * @return Client socket.
     */
    public Socket getClientSocket() {
        return clientSocket;
    }

    /**
     * Method restarts session timer.
     */
    void restartTimer() {
        timerLabel.restartTimer();
    }

    /**
     * Method retrieves the folder name from the folder path.
     *
     * @param tecName Folder path.
     * @return Folder name.
     */
    StringBuilder getFolder(String tecName) {
        StringBuilder nameBuilder = new StringBuilder(selectedPath);
        int index1 = tecName.lastIndexOf(File.separator);
        nameBuilder.delete(index1, nameBuilder.length());
        return nameBuilder;
    }

    /**
     * Method updates the display of the user's file tree.
     *
     * @param userFiles List of user files.
     */
    void refreshTree(File[] userFiles) {
        folderList.clear();
        drawTree(login, userFiles);
        sizeBar.setValue(userSize);
        sizeBar.setString("Used: " + userSize + " МВ from: " + Consts.USER_SIZE + " MB.");
    }

    /**
     * Method draws the user's file tree.
     *
     * @param root  Name of the root directory.
     * @param files List of user files.
     */
    private void drawTree(String root, File[] files) {
        TreeModel model = createTreeModel(root, files);
        fileTree.setModel(model);
        fileTree.addTreeSelectionListener(new SelectionListener());
        fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    /**
     * Method builds user's file tree.
     *
     * @param obj   user object.
     * @param files List of user files.
     * @return Tree model.
     */
    private TreeModel createTreeModel(Object obj, File[] files) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(obj);
        folderList.add(root.toString());
        if (files != null) {
            for (File item : files) {
                if (item.isDirectory()) {
                    addTreeNode(root, item, item.listFiles());
                } else {
                    root.add(new DefaultMutableTreeNode(item.getName(), false));
                }
            }
        }
        return new DefaultTreeModel(root, true);
    }

    /**
     * Method adds file in the tree.
     *
     * @param root  File catalog.
     * @param item  Added file.
     * @param files List of user files.
     */
    private void addTreeNode(DefaultMutableTreeNode root, File item, File[] files) {
        DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode(item.getName(), true);
        StringBuilder sb = new StringBuilder(item.getAbsolutePath());
        if (item.getAbsolutePath().contains(Consts.DIR_PATH)) {
            int lastIndex = Consts.DIR_PATH.length();
            sb = new StringBuilder(item.getAbsolutePath());
            sb.delete(0, lastIndex);
            int index = sb.indexOf(File.separator);
            sb.replace(0, index, folderList.get(0));
        }
        if (!folderList.contains(sb.toString())) {
            folderList.add(sb.toString());
        }
        itemNode.setAllowsChildren(true);
        root.add(itemNode);
        for (File inItem : files) {
            if (inItem.isDirectory()) {
                addTreeNode(itemNode, inItem, inItem.listFiles());
            } else {
                itemNode.add(new DefaultMutableTreeNode(inItem.getName(), false));
            }
        }
    }

    class SelectionListener implements TreeSelectionListener {
        /**
         * Method handles value change by the user.
         */
        public void valueChanged(TreeSelectionEvent e) {
            selectedPath.setLength(0);
            tecNode = null;
            JTree tree = (JTree) e.getSource();
            TreePath[] selected = tree.getSelectionPaths();
            StringBuilder text = new StringBuilder();
            DefaultMutableTreeNode node = null;
            if (selected != null) {
                for (int j = 0; j < selected.length; j++) {
                    TreePath path = selected[j];
                    Object[] nodes = path.getPath();
                    for (int i = 0; i < nodes.length; i++) {
                        node = (DefaultMutableTreeNode) nodes[i];
                        if (i > 0) {
                            text.append(File.separator);
                        }
                        text.append(node.getUserObject());
                    }
                    if (text.length() > 0) {
                        tecNode = node;
                        selectedPath.append(text.toString());
                    }
                }
            }
        }
    }

    /**
     * Method gets selected path.
     *
     * @return Selected path.
     */
    public StringBuilder getSelectedPath() {
        return selectedPath;
    }

    /**
     * Method gets tree node.
     *
     * @return Tree node.
     */
    public DefaultMutableTreeNode getTecNode() {
        return tecNode;
    }

    /**
     * Method gets user login.
     *
     * @return User login.
     */
    public String getLogin() {
        return login;
    }

    /**
     * Method gets button "Send file".
     *
     * @return Button "Send file".
     */
    public JButton getSendFileButton() {
        return sendFileButton;
    }

    /**
     * Method gets button "Get ID".
     *
     * @return Button "Get ID".
     */
    public JButton getGetIdButton() {
        return getIdButton;
    }

    /**
     * Method gets button "Get file".
     *
     * @return Button "Get file".
     */
    public JButton getGetFileButton() {
        return getFileButton;
    }

    /**
     * Method gets button "Delete file".
     *
     * @return Button "Delete file".
     */
    public JButton getDeleteFileButton() {
        return deleteFileButton;
    }

    /**
     * Method gets button "Refresh file".
     *
     * @return Button "Refresh file".
     */
    public JButton getRefreshFileButton() {
        return refreshFileButton;
    }

    /**
     * Method gets button "Rename file".
     *
     * @return Button "Rename file".
     */
    public JButton getRenameFileButton() {
        return renameFileButton;
    }

    /**
     * Method gets button "Transfer file".
     *
     * @return Button "Transfer file".
     */
    public JButton getTransferFileButton() {
        return transferFileButton;
    }

    /**
     * Method gets button "Create dir".
     *
     * @return Button "Create dir".
     */
    public JButton getCreateDirButton() {
        return createDirButton;
    }

    /**
     * Method gets button "Rename dir".
     *
     * @return Button "Rename dir".
     */
    public JButton getRenameDirButton() {
        return renameDirButton;
    }

    /**
     * Method gets button "Delete dir".
     *
     * @return Button "Delete dir".
     */
    public JButton getDeleteDirButton() {
        return deleteDirButton;
    }

    /**
     * Method gets folder list.
     *
     * @return Folder list.
     */
    public ArrayList<String> getFolderList() {
        return folderList;
    }

    /**
     * Method gets progress bar.
     *
     * @return Progress bar.
     */
    public MyProgressBarSimple getProgressBar() {
        return pBar;
    }

    /**
     * Method gets text area.
     *
     * @return Text area.
     */
    public JTextArea getTextArea() {
        return textArea;
    }

    /**
     * Method sets sets the current memory used on the server.
     *
     * @param userSize Current memory used on the server.
     */
    public void setUserSize(int userSize) {
        this.userSize = userSize;
    }
}
