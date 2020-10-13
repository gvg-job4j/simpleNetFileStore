package ru.gvg.serverside;

import javax.crypto.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class MyDropBoxSecurity {

    /**
     * Pair of secret keys.
     */
    KeyPair keyPair;

    /**
     * Constructor, initializing a pair of secret keys.
     *
     * @throws NoSuchAlgorithmException Possible exception.
     */
    public MyDropBoxSecurity() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPair = keyPairGenerator.generateKeyPair();
    }

    /**
     * Method returns public key for encryption.
     *
     * @return Public key.
     */
    public PublicKey getOpenKey() {
        return keyPair.getPublic();
    }

    /**
     * Method encrypts incoming string.
     *
     * @param str Plain text string.
     * @param key Public key.
     * @return Encrypted string in the format Base64.
     */
    public static String encrypt(String str, Key key) {

        String encrypted = null;
        try {
            Cipher ecipher = Cipher.getInstance("RSA");
            ecipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] utf8 = str.getBytes("UTF8");
            byte[] enc = ecipher.doFinal(utf8);
            encrypted = new String(java.util.Base64.getMimeEncoder().encode(enc),
                    StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException
                | UnsupportedEncodingException | IllegalBlockSizeException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return encrypted;
    }

    /**
     * Method decrypts incoming string.
     *
     * @param str Encrypted string in the format Base64.
     * @return Decrypted string.
     */
    public String decrypt(String str) {

        String decrypted = null;
        try {
            Cipher dcipher = Cipher.getInstance("RSA");
            dcipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] dec = Base64.getMimeDecoder().decode(str);
            byte[] utf8 = dcipher.doFinal(dec);
            decrypted = new String(utf8, "UTF8");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IOException | InvalidKeyException
                | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return decrypted;
    }
}
