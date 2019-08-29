package ru.gvg.messages;

import java.io.File;
import java.io.Serializable;

/**
 * Create message with array of transferred files (can be empty),
 * result of processing and any text message.
 *
 * @author Valeriy Gyrievskikh
 * @since 01.03.2019
 */
public class AnswerMessage implements Serializable {

    /**
     * Transfer files.
     */
    private File[] files;
    /**
     * Size of files.
     */
    private int size;
    private boolean result;
    private String msg;

    /**
     * Metod create message with parameters.
     *
     * @param result Result of processing.
     * @param msg    Text message.
     * @param files  Transfer files.
     * @param size   Size of files.
     */
    public AnswerMessage(boolean result, String msg, File[] files, int size) {
        this.result = result;
        this.msg = msg;
        this.files = files;
        this.size = size;
    }

    public AnswerMessage(boolean result, String msg, int size) {
        this.result = result;
        this.msg = msg;
        this.size = size;
    }

    public AnswerMessage(boolean result, String msg) {
        this.result = result;
        this.msg = msg;
    }

    /**
     * Metod return transfer files.
     *
     * @return Transfer files.
     */
    public File[] getFiles() {
        return files;
    }

    /**
     * Metod return size of files.
     *
     * @return Size of files.
     */
    public int getSize() {
        return size;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isResult() {
        return result;
    }
}
