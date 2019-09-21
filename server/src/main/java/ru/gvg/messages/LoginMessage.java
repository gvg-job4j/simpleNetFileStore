package ru.gvg.messages;

import ru.gvg.common.UserActionEnum;

import java.io.Serializable;

public class LoginMessage implements Serializable {

    /**
     * Encrypted string for authorization.
     */
    private String strongName;
    /**
     * User action.
     */
    private UserActionEnum userActionEnum;

    /**
     * Constructor, initializing user action and string for authorization.
     *
     * @param strongName Encrypted string for authorization.
     * @param userAction User action.
     */
    public LoginMessage(String strongName, UserActionEnum userAction) {
        this.strongName = strongName;
        this.userActionEnum = userAction;
    }

    /**
     * Metod returns string for authorization.
     *
     * @return String for authorization.
     */
    public String getStrongName() {
        return strongName;
    }

    /**
     * Metod returns user action.
     *
     * @return User action.
     */
    public UserActionEnum getUserActionEnum() {
        return userActionEnum;
    }
}
