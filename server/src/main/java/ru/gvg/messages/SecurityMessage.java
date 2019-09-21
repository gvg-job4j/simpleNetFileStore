package ru.gvg.messages;

import java.io.Serializable;
import java.security.Key;

public class SecurityMessage implements Serializable {
    /**
     * Publc key for encryption.
     */
    private Key key;

    /**
     * Constructor, initializing a public key.
     *
     * @param key Generated public key.
     */
    public SecurityMessage(Key key) {
        this.key = key;
    }

    public SecurityMessage() {

    }

    /**
     * Metod returns public key.
     *
     * @return Public key.
     */
    public Key getKey() {
        return key;
    }
}
