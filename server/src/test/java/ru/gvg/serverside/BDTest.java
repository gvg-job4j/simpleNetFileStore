package ru.gvg.serverside;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.gvg.common.Consts;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Valeriy Gyrievskikh
 * @since 11.09.2019
 */
public class BDTest {

    private Properties properties;
    private BD testBd;

    @Before
    public void setConnectProperties() {
        try (InputStream in = BD.class.getClassLoader().getResourceAsStream("database.properties")) {
            properties = new Properties();
            properties.load(in);
            testBd = new BD();
            testBd.initDatabase(properties);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void clearDatabase() throws SQLException {
        Connection connection = testBd.connectToDataBase();
        try (Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM Users WHERE USER = 'test';");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void verifyUser() throws Exception {
    }

    @Test
    public void getUser() throws Exception {
        boolean verified = testBd.verifyUser(testBd.connectToDataBase(), "1", "1");
        assertTrue(verified);
    }

    @Test
    public void addUser() throws Exception {
        boolean added = testBd.addUser(testBd.connectToDataBase(), "test", "test");
        assertTrue(added);
    }

    @Test
    public void createUID() throws Exception {
    }

    @Test
    public void getUserUID() throws Exception {
    }

    @Test
    public void changePassword() throws Exception {
    }

    @Test
    public void getFilePathOnServer() throws Exception {
    }

    @Test
    public void addFile() throws Exception {
    }

    @Test
    public void getFileID() throws Exception {
    }

    @Test
    public void getFileLocalPath() throws Exception {
    }

    @Test
    public void deleteFile() throws Exception {
    }

    @Test
    public void renameFile() throws Exception {
    }

    @Test
    public void transferFile() throws Exception {
    }

    @Test
    public void initDatabase() throws Exception {
        BD bd = new BD();
        assertTrue(bd.initDatabase(properties));
    }

    @Test
    public void connectToDataBase() throws Exception {
        assertNotNull(testBd.connectToDataBase());
    }

}