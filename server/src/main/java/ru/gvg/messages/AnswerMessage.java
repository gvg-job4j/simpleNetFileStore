package ru.gvg.messages;

import java.io.File;
import java.io.Serializable;

/**
 * Creates a message with array of user's files and size of user's files (both can be empty),
 * result of operation and any text message.
 *
 * @author Valeriy Gyrievskikh
 * @since 01.03.2019
 */
public class AnswerMessage implements Serializable, Messaging {

    /**
     * List of user's files.
     */
    private File[] files;
    /**
     * Size of user's files.
     */
    private int size;
    /**
     * Result of operation.
     */
    private boolean result;
    /**
     * Comment on the message.
     */
    private String msg;

    /**
     * Initializes message with parameters.
     *
     * @param result Result of operation.
     * @param msg    Comment on the message.
     * @param files  List of user files.
     * @param size   Size of user files.
     */
    public AnswerMessage(boolean result, String msg, File[] files, int size) {
        this.result = result;
        this.msg = msg;
        this.files = files;
        this.size = size;
    }

    /**
     * Initializes message with parameters.
     *
     * @param result Result of operation.
     * @param msg    Comment on the message.
     * @param size   Size of user files.
     */
    public AnswerMessage(boolean result, String msg, int size) {
        this.result = result;
        this.msg = msg;
        this.size = size;
    }

    /**
     * Initializes message with parameters.
     *
     * @param result Result of operation.
     * @param msg    Comment on the message.
     */
    public AnswerMessage(boolean result, String msg) {
        this.result = result;
        this.msg = msg;
    }

    /**
     * Method returns list of user's files.
     *
     * @return List of user's files.
     */
    public File[] getFiles() {
        return files;
    }

    /**
     * Method returns size of user's files.
     *
     * @return Size of user's files.
     */
    public int getSize() {
        return size;
    }

    /**
     * Method returns comment on the message.
     *
     * @return Comment on the message.
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Method returns size of user's files.
     *
     * @return Size of user's files.
     */
    public boolean isResult() {
        return result;
    }
}
