package ru.gvg.client;

import ru.gvg.common.Consts;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Timer for the current user session.
 *
 * @author Valeriy Gyrievskikh
 * @since 22.12.2019
 */
class TimerLabel extends JLabel {
    /**
     * Current window.
     */
    private ClientGUI frame;
    /**
     * The timer for the current session.
     */
    private Timer timer = new Timer();
    /**
     * Current timer task.
     */
    private TimerTask timerTask;

    /**
     * Constructor, sets user window.
     *
     * @param frame User window.
     */
    public TimerLabel(ClientGUI frame) {
        this.frame = frame;
        restartTimer();
    }

    /**
     * Method restarts the timer for the current session.
     */
    public void restartTimer() {
        stopTimer();
        timerTask = new TimerTask() {
            private volatile int time = Consts.SESSION_TIME;

            @Override
            public void run() {
                if (--time <= 0) {
                    frame.closeClientGUI("Closed by timeout...");
                }
                SwingUtilities.invokeLater(() -> {
                    int t = time;
                    TimerLabel.this.setText(String.format("%02d:%02d", t / 60, t % 60));
                });
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    /**
     * Method stops the timer.
     */
    public void stopTimer() {
        if (timerTask != null) {
            timerTask.cancel();
        }
    }
}

