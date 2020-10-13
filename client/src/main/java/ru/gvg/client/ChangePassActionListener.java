package ru.gvg.client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Valeriy Gyrievskikh
 * @since 16.03.2020
 */
public class ChangePassActionListener implements ActionListener {

    /**
     * Opened user frame.
     */
    private ClientStartFrame frame;
    /**
     * Frame for password changing.
     */
    private NewPassFrame newPassFrame;

    /**
     * Constructor, sets opened frames.
     *
     * @param frame        User frame.
     * @param newPassFrame Frame for password changing.
     */
    public ChangePassActionListener(ClientStartFrame frame, NewPassFrame newPassFrame) {
        this.frame = frame;
        this.newPassFrame = newPassFrame;
    }

    /**
     * Method sets new user password.
     *
     * @param e Current user action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getID() == ActionEvent.ACTION_PERFORMED) {
            if (newPassFrame.getNewPass().getPassword().length == 0 || newPassFrame.getCurrentPass().getPassword().length == 0) {
                JOptionPane.showMessageDialog(null, "Not all fields are filled in!", "", JOptionPane.WARNING_MESSAGE);
            } else {
                frame.setNewPass(newPassFrame.getNewPass(), newPassFrame.getCurrentPass());
                newPassFrame.dispose();
                StartFrameActionListener listener = new StartFrameActionListener(frame);
                listener.actionPerformed(new ActionEvent(frame.getChangePassButton(), 1001, "Change password"));
            }
        } else {
            JOptionPane.showMessageDialog(null, "Password change canceled!", "", JOptionPane.WARNING_MESSAGE);
            newPassFrame.dispose();
        }
    }
}
