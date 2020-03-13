package ru.gvg.messages;

import ru.gvg.common.FileActionEnum;

import java.io.Serializable;

/**
 * Creates a message with information about an action with a file.
 *
 * @author Valeriy Gyrievskikh
 * @since 01.03.2019
 */
public class FileMessage implements Serializable, Messaging {

    /**
     * Current filename.
     */
    private String name;
    /**
     * Current action with the file.
     */
    private FileActionEnum action;
    /**
     * New name for the file.
     */
    private String newName;
    /**
     * The current path to the file.
     */
    private String tecPath;

    /**
     * Initializes message with parameters.
     *
     * @param name    Current filename.
     * @param action  Current action.
     * @param newName New name for the file.
     * @param tecPath The current path to the file.
     */
    public FileMessage(String name, FileActionEnum action, String newName, String tecPath) {
        this.name = name;
        this.action = action;
        this.newName = newName;
        this.tecPath = tecPath;
    }

    /**
     * Method returns action with the file.
     *
     * @return Current action with the file.
     */
    public FileActionEnum getAction() {
        return action;
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
     * Method returns new filename.
     *
     * @return New name for the file.
     */
    public String getNewName() {
        return newName;
    }

    /**
     * Method returns current path to the file.
     *
     * @return Current path to the file.
     */
    public String getTecPath() {
        return tecPath;
    }

}
