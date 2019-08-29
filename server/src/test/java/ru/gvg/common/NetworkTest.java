package ru.gvg.common;

import org.junit.Test;
import ru.gvg.messages.AnswerMessage;

import java.io.*;
import java.net.Socket;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Gyrievskikh
 * @since 28.08.2019
 */
public class NetworkTest {
    @Test
    public void whenSendMessageThenGetMessage() throws Exception {
        Socket socket = mock(Socket.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(out);
        Network.sendAnswerMessage(socket, null, false, "Test");
        byte[] data = out.toByteArray();
        ObjectInputStream inObj = new ObjectInputStream(new ByteArrayInputStream(data));
        assertThat(((AnswerMessage) inObj.readObject()).getMsg(), is("Test"));
    }

    @Test
    public void whenSendFolderMessageThenGetMessage() throws Exception {
        Socket socket = mock(Socket.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(out);
        AnswerMessage testMsg = new AnswerMessage(true, "Test", null, 0);
        ByteArrayOutputStream outTest = new ByteArrayOutputStream();
        ObjectOutputStream outTestObj = new ObjectOutputStream(outTest);
        outTestObj.writeObject(testMsg);
        outTestObj.flush();
        byte[] bytes = outTest.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        when(socket.getInputStream()).thenReturn(in);
        AnswerMessage aMsg = Network.sendFolderMessage(socket, "1", false, false, "1");
        assertThat(aMsg.getMsg(), is("Test"));
    }
}