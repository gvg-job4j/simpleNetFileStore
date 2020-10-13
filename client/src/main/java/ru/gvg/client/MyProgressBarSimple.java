package ru.gvg.client;

import javax.swing.*;
import java.awt.*;

/**
 * Generates a progress bar for the operations being performed.
 *
 * @author Valeriy Gyrievskikh
 * @since 30.10.2019
 */
public class MyProgressBarSimple {
    /**
     * Frame for bar.
     */
    private final JFrame frame = new JFrame();
    /**
     * Window for bar.
     */
    private final JDialog dialog = new JDialog(frame, "Transfer in process...", false);
    /**
     * Text for bar.
     */
    private final JLabel label = new JLabel("Task in process...");
    /**
     * Panel for bar.
     */
    private final JPanel panel = new JPanel(new GridLayout(1, 0));

    /**
     * Constructor for progress bar.
     */
    public MyProgressBarSimple() {
        Font otherFont = new Font("TimesRoman", Font.BOLD, 20);
        frame.setUndecorated(true);
        dialog.setUndecorated(true);
        label.setVisible(true);
        label.setFont(otherFont);
        dialog.setSize(300, 60);
        panel.setVisible(true);
        panel.setSize(200, 40);
        panel.add(label);
        dialog.add(panel);
        dialog.pack();
        dialog.setDefaultCloseOperation(1);
        int[] coords = ClientStartFrame.getStartCoords(frame);
        dialog.setLocation((int) (coords[0] - dialog.getWidth()) / 2, (int) (coords[1] - dialog.getHeight()) / 2);
    }

    /**
     * Method shows bar.
     */
    public void showBar() {
        dialog.setVisible(true);
    }

    /**
     * Method hides bar.
     */
    public void hideBar() {
        dialog.setVisible(false);
    }
}
