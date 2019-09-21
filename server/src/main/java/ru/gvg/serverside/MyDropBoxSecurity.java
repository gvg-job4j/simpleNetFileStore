package ru.gvg.serverside;

import javax.crypto.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;

public class MyDropBoxSecurity {

    /**
     * Pair of secret keys.
     */
    KeyPair keyPair;

    /**
     * Constructor, initializing a pair of secret keys.
     *
     * @throws NoSuchAlgorithmException
     */
    public MyDropBoxSecurity() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPair = keyPairGenerator.generateKeyPair();
    }

    /**
     * Metod returns public key for encryption.
     *
     * @return Public key.
     */
    public PublicKey getOpenKey() {
        return keyPair.getPublic();
    }

    /**
     * Metod encrypt incoming string.
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
            encrypted = new sun.misc.BASE64Encoder().encode(enc);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return encrypted;
    }

    /**
     * Metod decrypt incoming string.
     *
     * @param str Encrypted string in the format Base64.
     * @return Decrypted string.
     */
    public String decrypt(String str) {

        String decrypted = null;
        try {
            Cipher dcipher = Cipher.getInstance("RSA");
            dcipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
            byte[] utf8 = dcipher.doFinal(dec);
            decrypted = new String(utf8, "UTF8");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return decrypted;
    }
}
