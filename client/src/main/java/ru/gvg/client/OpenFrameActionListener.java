package ru.gvg.client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Valeriy Gyrievskikh
 * @since 16.03.2020
 */
public class OpenFrameActionListener implements ActionListener {
    /**
     * Opened user frame.
     */
    private ClientStartFrame frame;

    /**
     * Constructor, sets user frame.
     *
     * @param frame User frame.
     */
    public OpenFrameActionListener(ClientStartFrame frame) {
        this.frame = frame;
    }

    /**
     * Method opened a new window to change the password.
     *
     * @param e User action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getID() == ActionEvent.ACTION_PERFORMED) {
            if (frame.getLogin().getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Username not specified.", "", JOptionPane.WARNING_MESSAGE);
            } else {
                new NewPassFrame(frame);
            }
        }
    }
}
