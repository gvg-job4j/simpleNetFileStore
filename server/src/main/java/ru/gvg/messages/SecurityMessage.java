package ru.gvg.messages;

import java.io.Serializable;
import java.security.Key;

/**
 * Creates a message with information about public key.
 *
 * @author Valeriy Gyrievskikh
 * @since 01.03.2019
 */
public class SecurityMessage implements Serializable, Messaging {
    /**
     * Publc key for encryption.
     */
    private Key key;

    /**
     * Initializes message with parameters.
     *
     * @param key Generated public key.
     */
    public SecurityMessage(Key key) {
        this.key = key;
    }

    /**
     * Initializes message without parameters.
     */
    public SecurityMessage() {

    }

    /**
     * Method returns public key.
     *
     * @return Public key.
     */
    public Key getKey() {
        return key;
    }
}
