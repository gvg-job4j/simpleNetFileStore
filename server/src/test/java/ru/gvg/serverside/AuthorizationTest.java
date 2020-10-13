package ru.gvg.serverside;

import org.junit.Test;
import ru.gvg.messages.LoginMessage;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Valeriy Gyrievskikh
 * @since 17.09.2019
 */
public class AuthorizationTest {

    @Test
    public void whenDecryptEncryptedStringThenTheSame() throws NoSuchAlgorithmException {
        MyDropBoxSecurity security = new MyDropBoxSecurity();
        PublicKey key =  security.getOpenKey();
        String login = "123";
        String loginStr = MyDropBoxSecurity.encrypt(login, key);
        String decrypted = security.decrypt(loginStr);
        assertThat(decrypted, is(login));
    }
}
