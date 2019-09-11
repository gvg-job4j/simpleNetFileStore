package ru.gvg.serverside;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.UUID;

/**
 * @author Valeriy Gyrievskikh
 * @since 23.08.2019
 */
public class BD {

    private String userUID;
    private String userId;

    private Properties properties;

//    public Properties getConfig() {
//        return config;
//    }

    public BD() {
//        try (InputStream in = BD.class.getClassLoader().getResourceAsStream("database.properties")) {
//            config = new Properties();
//            config.load(in);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    //    private HashMap<ServerThread, Connection> connections = new HashMap<>();
//
//    {
//        try {
//            Class.forName("org.sqlite.JDBC");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    private synchronized Connection getConnection(ServerThread thread) {
        return thread.getConnection();
//        Connection connection = null;
//        try {
//            if (connections.containsKey(thread)) {
//                connections.remove(thread);
//            }
//            connection = DriverManager.getConnection("jdbc:sqlite:MyDB.db");
//            connections.put(thread, connection);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return connection;
    }

    public synchronized boolean verifyUser(Connection connection, String log, String pass) throws Exception {
        boolean userFind = false;
//        Connection connection = getConnection(thread);
        if (connection != null) {

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT PASS FROM Users WHERE USER = '" + log + "'");
            if (rs.next()) {
                String passFromBD = rs.getString("PASS");
                if (PasswordAuthentication.check(pass, passFromBD)) {
                    userFind = true;
                } else {
                    userId = null;
                    userUID = null;
                }
            }
            rs.close();
        }
        return userFind;
    }

    public synchronized boolean getUser(Connection connection, String log) throws SQLException {
        boolean userFind = false;
//        Connection connection = getConnection(thread);
        if (connection != null) {

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT id, uid, PASS FROM Users WHERE USER = '" + log + "'");
            if (rs.next()) {
                userId = rs.getString("id");
                userUID = rs.getString("uid");
                userFind = true;
            }
            rs.close();
        }
        return userFind;
    }

    public synchronized boolean addUser(Connection connection, String log, String pass) throws Exception {

        boolean userCreated = false;
        if (!getUser(connection, log)) {
//            Connection connection = getConnection(thread);
            if (connection != null) {
                try {
                    userUID = createUID();
                    String hashPass = PasswordAuthentication.getSaltedHash(pass);
                    Statement statement = connection.createStatement();
                    statement.execute("INSERT INTO Users (USER, PASS, uid) VALUES('" + log + "','" + hashPass + "','" + userUID + "')");
                    if (WorkWithFiles.makeDir(userUID)) {
                        userCreated = true;
                        ResultSet rs = statement.executeQuery("SELECT id FROM Users WHERE USER = '" + log + "' and uid = '" + userUID + "'");
                        if (rs.next()) {
                            userId = rs.getString("id");
                        }
                        rs.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return userCreated;
    }

    public synchronized String createUID() {
        UUID id = UUID.randomUUID();
        return id.toString();
    }

    public String getUserUID() {
        return userUID;
    }

    public synchronized boolean changePassword(Connection connection, String log, String newPass) throws Exception {
//        Connection connection = getConnection(thread);
        boolean passChanged = false;
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                String hashPass = PasswordAuthentication.getSaltedHash(newPass);
                statement.execute("UPDATE users SET PASS = '" + hashPass + "' WHERE USER = '" + log + "' AND uid = '" + userUID + "'");
                passChanged = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return passChanged;
    }

    public String getFilePathOnServer(Connection connection, String fileID) throws SQLException {
        String pathToFile = null;
//        Connection connection = getConnection(thread);
        if (connection != null) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT server_path FROM Files WHERE file_id = '" + fileID + "'");
            if (rs.next()) {
                pathToFile = rs.getString("server_path");
            }
            rs.close();
        }
        return pathToFile;
    }

    public synchronized boolean addFile(Connection connection, String fileName, String serverPath, String localPath, long size, long creationTime) throws SQLException {

        boolean fileAdd = false;
        boolean fileExist = verifyFile(connection, userId, fileName, localPath, serverPath);
//        Connection connection = getConnection(thread);
        if (connection != null) {

            String userId = this.userId;
            Statement statement = connection.createStatement();
            if (fileExist) {
                String str = "UPDATE Files SET creation_time = '" + creationTime + "', last_mod_time = '" + creationTime
                        + "', size = '" + size + "', size = '" + size
                        + "' WHERE server_path = '" + serverPath + "' AND local_path = '" + localPath
                        + "' AND file_name = '" + fileName + "' AND user_id = '" + userId + "'";
                statement.execute(str);
            } else {
                String fileId = createUID();
                String str = "INSERT INTO files "
                        + "(user_id, file_name, local_path, server_path, creation_time, last_mod_time, size, file_id)"
                        + " VALUES('" + userId + "','" + fileName + "','" + localPath
                        + "','" + serverPath + "','" + creationTime + "','" + creationTime + "','" + size + "','" + fileId + "')";
                statement.execute(str);
            }
//                userCreated = statement.execute("INSERT INTO users (login, pass) values('" + log + "','" + pass + "'");
            fileAdd = true;

        }
        return fileAdd;

    }

    private synchronized boolean verifyFile(Connection connection, String userId, String fileName, String localPath, String serverPath) throws SQLException {
        boolean isExist = false;
//        Connection connection = getConnection(thread);
        Statement statement = connection.createStatement();
        String str = "SELECT file_id FROM files "
                + "WHERE server_path = '" + serverPath + "' and local_path = '" + localPath
                + "' and file_name = '" + fileName + "' and user_id = '" + userId + "'";
        ResultSet rs = statement.executeQuery(str);
        if (rs.next()) {
            isExist = true;
        }
        rs.close();
        return isExist;
    }

    public String getFileID(Connection connection, String pathToFile) throws SQLException {

        String fileID = null;
//        Connection connection = getConnection(thread);
        if (connection != null) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT file_id FROM Files WHERE server_path = '" + pathToFile + "'");
            if (rs.next()) {
                fileID = rs.getString("file_id");
            }
            rs.close();
        }
        return fileID;
    }

    public String getFileLocalPath(Connection connection, String pathToFile) throws SQLException {

        String filePath = null;
//        Connection connection = getConnection(thread);
        if (connection != null) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT local_path FROM Files WHERE server_path = '" + pathToFile + "'");
            if (rs.next()) {
                filePath = rs.getString("local_path");
            }
            rs.close();
        }
        return filePath;
    }

    public synchronized boolean deleteFile(Connection connection, String pathToFile) {

//        Connection connection = getConnection(thread);
        boolean fileDel = false;
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                statement.execute("DELETE FROM Files WHERE server_path = '" + pathToFile + "' and user_id = '" + userId + "'");
//                userCreated = statement.execute("INSERT INTO users (login, pass) values('" + log + "','" + pass + "'");
                fileDel = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return fileDel;
    }

    public synchronized boolean renameFile(Connection connection, String tecPath, String newPath) {
//        Connection connection = getConnection(thread);
        boolean fileRen = false;
        if (connection != null) {
            try {
                File file = new File(newPath);
                Statement statement = connection.createStatement();
                statement.execute("UPDATE Files SET file_name = '" + file.getName() + "', server_path = '" + newPath + "' WHERE server_path = '" + tecPath + "' and user_id = '" + userId + "'");
                fileRen = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return fileRen;
    }

    public synchronized boolean transferFile(Connection connection, String tecPath, String newServerName) {
//        Connection connection = getConnection(thread);
        boolean fileRen = false;
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                String str = "UPDATE Files SET server_path = '" + newServerName + "' WHERE server_path = '" + tecPath + "' and user_id = '" + userId + "'";
                statement.execute(str);
                fileRen = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return fileRen;
    }

    public boolean initDatabase(Properties properties) throws ClassNotFoundException, SQLException {
        this.properties = properties;
        Class.forName(properties.getProperty("driver"));
        Connection connection = DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password")
        );
        return connection != null && initTables(connection);
    }

    private boolean initTables(Connection connection) throws SQLException {
        boolean verify = false;
        if (connection != null) {
            Statement statement = connection.createStatement();
            verifyTables(statement);
            verify = true;
        }
        return verify;
    }

    private void verifyTables(Statement statement) throws SQLException {
        String queryUsers = "CREATE TABLE IF NOT EXISTS USERS(id INT PRIMARY KEY NOT NULL, user VARCHAR(20) NOT NULL,"
                + "pass VARCHAR(100) NOT NULL, uid VARCHAR(16) NOT NULL);";
        statement.executeUpdate(queryUsers);
        ResultSet rs = statement.executeQuery("SELECT * FROM users;");
        if (!rs.next()) {
            statement.executeUpdate("INSERT INTO users (id, user, pass, uid) VALUES(1, '1',"
                    + "'iJuJpzTzQIrlIg7P0aDGbIazV6iqBrCwU6XznRu9atQ$zMM7OYCGbg13xGbgCsRgdLh0JS5TsVSHZfOnqNKDBkc',"
                    + "'dd021c56-a886-4b04-8c8f-d894210ebe72');");
        }
        String queryFiles = "CREATE TABLE IF NOT EXISTS FILES(id INT PRIMARY KEY NOT NULL, user_id INT REFERENCES users(id) NOT NULL,"
                + "file_name VARCHAR (100) NOT NULL, local_path VARCHAR(100) NOT NULL,"
                + "server_path VARCHAR(100) NOT NULL, creation_time DATETIME NOT NULL, last_mod_time DATETIME NOT NULL,"
                + "size INT, file_id VARCHAR(50), content BLOB);";
        statement.executeUpdate(queryFiles);
    }

    public Connection connectToDataBase() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password")
        );
    }
}
