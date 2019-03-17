package ru.gvg.messages;

import java.io.File;

/**
 * Class describes message.
 *
 * @author Valeriy Gyrievskikh
 * @since 01.03.2019
 */
public abstract class AbstractMessage {

    /**
     * Result of processing.
     */
    private boolean result;
    /**
     * Text message.
     */
    private String msg;

    public AbstractMessage(boolean result, String msg) {
        this.result = result;
        this.msg = msg;
    }

    /**
     * Metod return result value.
     *
     * @return result.
     */
    public boolean isResult() {
        return result;
    }

    /**
     * Metod return text message.
     *
     * @return message.
     */
    public String getMsg() {
        return msg;
    }


}
