package ru.gvg.messages;

import java.io.Serializable;

/**
 * Creates a message with the file data.
 *
 * @author Valeriy Gyrievskikh
 * @since 01.03.2019
 */
public class TransferFileMessage implements Serializable, Messaging {
    /**
     * Current filename.
     */
    private String name;
    /**
     * The new path to the file.
     */
    private String newPath;
    /**
     * The current path to the file.
     */
    private String currentPath;
    /**
     * File data.
     */
    private byte[] data;
    /**
     * File size.
     */
    private long size;
    /**
     * Number of the current part of the file data.
     */
    private int current;
    /**
     * Number of the last part of the file data.
     */
    private int end;

    /**
     * Initializes message with parameters.
     *
     * @param name        Current filename.
     * @param newPath     The new path to the file.
     * @param currentPath The current path to the file.
     * @param size        File size.
     * @param data        File data.
     * @param current     Number of the current part of the file data.
     * @param end         Number of the last part of the file data.
     */
    public TransferFileMessage(String name, String newPath, String currentPath, long size, byte[] data, int current, int end) {
        this.name = name;
        this.newPath = newPath;
        this.currentPath = currentPath;
        this.data = data;
        this.current = current;
        this.end = end;
        this.size = size;
    }

    /**
     * Method returns number of the current part of the file data.
     *
     * @return Number of the current part of the file data.
     */
    public int getCurrent() {
        return current;
    }

    /**
     * Method returns number of the last part of the file data.
     *
     * @return Number of the last part of the file data.
     */
    public int getEnd() {
        return end;
    }

    /**
     * Method returns new path to the file.
     *
     * @return New path to the file.
     */
    public String getNewPath() {
        return newPath;
    }

    /**
     * Method returns current path to the file.
     *
     * @return Current path to the file.
     */
    public String getCurrentPath() {
        return currentPath;
    }

    /**
     * Method returns сurrent filename.
     *
     * @return current filename.
     */
    public String getName() {
        return name;
    }

    /**
     * Method returns file data.
     *
     * @return File data.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Method returns file size.
     *
     * @return File size.
     */
    public long getSize() {
        return size;
    }
}
