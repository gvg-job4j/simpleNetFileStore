package ru.gvg.client;

import javax.swing.*;
import java.awt.*;

/**
 * @author Valeriy Gyrievskikh
 * @since 16.03.2020
 */
public class NewPassFrame extends JFrame {

    /**
     * New user password.
     */
    private JPasswordField newPass = new JPasswordField(12);
    /**
     * Current user password.
     */
    private JPasswordField currentPass = new JPasswordField(12);

    /**
     * Method opens a new window to change the password.
     *
     * @param userFrame Current user frame.
     */
    public NewPassFrame(ClientStartFrame userFrame) {
        setSize(300, 100);
        setTitle("Enter new password");
        setResizable(false);
        int[] coords = ClientStartFrame.getStartCoords(this);
        setLocation((int) (coords[0] - getWidth()) / 2, (int) (coords[1] - getHeight()) / 2);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        currentPass.setFont(new Font("TimesRoman", Font.BOLD, 16));
        newPass.setFont(new Font("TimesRoman", Font.BOLD, 16));
        JLabel passText = new JLabel("Current password:");
        JPanel fieldsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldsPanel.add(passText);
        fieldsPanel.add(currentPass);
        JLabel newPassText = new JLabel("New password:");
        fieldsPanel.add(newPassText);
        fieldsPanel.add(newPass);
        JButton setPass = new JButton("OK");
        setPass.addActionListener(new ChangePassActionListener(userFrame, this));
        add(fieldsPanel, BorderLayout.NORTH);
        add(setPass, BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }

    public JPasswordField getNewPass() {
        return newPass;
    }

    public JPasswordField getCurrentPass() {
        return currentPass;
    }
}
