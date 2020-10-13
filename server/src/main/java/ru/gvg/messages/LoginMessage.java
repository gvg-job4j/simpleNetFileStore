package ru.gvg.messages;

import ru.gvg.common.UserActionEnum;

import java.io.Serializable;

/**
 * Creates a message with information about user and user action.
 *
 * @author Valeriy Gyrievskikh
 * @since 01.03.2019
 */
public class LoginMessage implements Serializable, Messaging {

    /**
     * Encrypted string for authorization.
     */
    private String strongName;
    /**
     * User action.
     */
    private UserActionEnum userActionEnum;

    /**
     * Initializes message with parameters.
     *
     * @param strongName Encrypted string for authorization.
     * @param userAction User action.
     */
    public LoginMessage(String strongName, UserActionEnum userAction) {
        this.strongName = strongName;
        this.userActionEnum = userAction;
    }

    /**
     * Method returns string for authorization.
     *
     * @return String for authorization.
     */
    public String getStrongName() {
        return strongName;
    }

    /**
     * Method returns user action.
     *
     * @return User action.
     */
    public UserActionEnum getUserActionEnum() {
        return userActionEnum;
    }
}
