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
public class AnswerMessage extends AbstractMessage implements Serializable {

    /**
     * Transfer files.
     */
    private File[] files;
    /**
     * Size of files.
     */
    private int size;

    /**
     * Metod create message with parameters.
     *
     * @param result Result of processing.
     * @param msg    Text message.
     * @param files  Transfer files.
     * @param size   Size of files.
     */
    public AnswerMessage(boolean result, String msg, File[] files, int size) {
        super(result, msg);
        this.files = files;
        this.size = size;
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
}
