package ru.gvg.messages;

import java.io.Serializable;

/**
 * Creates a message with information about an action with a folder.
 *
 * @author Valeriy Gyrievskikh
 * @since 01.03.2019
 */
public class FolderMessage implements Serializable, Messaging {
    /**
     * Current foldername.
     */
    private String name;
    /**
     * New name for the folder.
     */
    private String newName;
    /**
     * Sign of deleting a folder.
     */
    private boolean delete;
    /**
     * Sign of creation.
     */
    private boolean create;

    /**
     * Initializes message with parameters.
     *
     * @param name    Current foldername.
     * @param create  Sign of creation.
     * @param delete  Sign of deleting a folder.
     * @param newName New name for the folder.
     */
    public FolderMessage(String name, boolean create, boolean delete, String newName) {
        this.name = name;
        this.newName = newName;
        this.delete = delete;
        this.create = create;
    }

    /**
     * Method returns сurrent foldername.
     *
     * @return current foldername.
     */
    public String getName() {
        return name;
    }

    /**
     * Method returns new foldername.
     *
     * @return New name for the folder.
     */
    public String getNewName() {
        return newName;
    }

    /**
     * Method returns sign of deleting.
     *
     * @return Sign of deleting a folder.
     */
    public boolean isDelete() {
        return delete;
    }

    /**
     * Method returns sign of creation.
     *
     * @return Sign of creation.
     */
    public boolean isCreate() {
        return create;
    }
}
