package ru.gvg.serverside;

import ru.gvg.common.Consts;
import ru.gvg.messages.TransferFileMessage;

import java.io.File;
import java.sql.*;
import java.util.Properties;
import java.util.UUID;

/**
 * @author Valeriy Gyrievskikh
 * @since 23.08.2019
 */
public class DataBaseManager {

    /**
     * User UID.
     */
    private String userUID;

    /**
     * Database entry id about user.
     */
    private String userId;

    /**
     * Database connection information.
     */
    private static Properties properties;

    /**
     * Default constructor.
     */
    public DataBaseManager() {

    }

    /**
     * Method checks if there is a user record in the database.
     *
     * @param connection Database connection.
     * @param log        User login.
     * @param pass       User password.
     * @return Check result.
     * @throws Exception Possible exception.
     */
    public synchronized boolean verifyUser(Connection connection, String log, String pass) throws Exception {
        boolean userFind = false;
        if (connection != null) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT id, uid, PASS FROM Users "
                    + "WHERE USER = '" + log + "'");
            if (rs.next()) {
                String passFromBD = rs.getString("PASS");
                if (PasswordAuthentication.check(pass, passFromBD)) {
                    userFind = true;
                    userId = rs.getString("id");
                    userUID = rs.getString("uid");
                } else {
                    userId = null;
                    userUID = null;
                }
            }
            rs.close();
            statement.close();
        }
        return userFind;
    }

    /**
     * Method gets user ids from database.
     *
     * @param connection Database connection.
     * @param log        User login.
     * @return Check result.
     * @throws SQLException Possible exception.
     */
    public synchronized boolean getUser(Connection connection, String log) throws SQLException {
        boolean userFind = false;
        if (connection != null) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT id, uid, PASS FROM Users"
                    + " WHERE USER = '" + log + "'");
            if (rs.next()) {
                userId = rs.getString("id");
                userUID = rs.getString("uid");
                userFind = true;
            }
            rs.close();
        }
        return userFind;
    }

    /**
     * Method deletes the user record from the database.
     *
     * @param connection Database connection.
     * @param log        User login.
     * @return Delete result.
     * @throws SQLException Possible exception.
     */
    public synchronized boolean deleteUser(Connection connection, String log) throws SQLException {
        boolean userDeleted = false;
        if (connection != null) {
            Statement statement = connection.createStatement();
            userDeleted = statement.execute("DELETE FROM Users WHERE USER = '" + log + "'");
        }
        return userDeleted;
    }

    /**
     * Method creates a user record in the database.
     *
     * @param connection Database connection.
     * @param log        User login.
     * @param pass       User password.
     * @return Execution result.
     * @throws Exception Possible exception.
     */
    public synchronized boolean addUser(Connection connection, String log, String pass) throws Exception {
        boolean userCreated = false;
        if (!getUser(connection, log)) {
            if (connection != null) {
                try {
                    userUID = createUID();
                    String hashPass = PasswordAuthentication.getSaltedHash(pass);
                    Statement statement = connection.createStatement();
                    FileManager.makeDir(Consts.DIR_PATH + userUID);
                    statement.executeUpdate("INSERT INTO Users (USER, PASS, uid) "
                            + "VALUES('" + log + "','" + hashPass + "','" + userUID + "')");
                    userCreated = true;
                    ResultSet rs = statement.executeQuery("SELECT id FROM Users"
                            + " WHERE USER = '" + log + "' and uid = '" + userUID + "'");
                    if (rs.next()) {
                        userId = rs.getString("id");
                    }
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return userCreated;
    }

    /**
     * Method creates new UID.
     *
     * @return New UID.
     */
    public synchronized String createUID() {
        UUID id = UUID.randomUUID();
        return id.toString();
    }

    /**
     * Method returns user UID.
     *
     * @return User UID.
     */
    public String getUserUID(Connection connection, String login) throws SQLException {
        return getUser(connection, login) ? userUID : null;
    }

    /**
     * Method returns user UID.
     *
     * @return User UID.
     */
    public String getUserUID() {
        return userUID;
    }

    /**
     * Method changes the user's password
     *
     * @param connection Database connection.
     * @param log        User login.
     * @param newPass    New user password.
     * @return Execution result.
     * @throws Exception Possible exception.
     */
    public synchronized boolean changePassword(Connection connection, String log, String newPass) throws Exception {
        boolean passChanged = false;
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                String hashPass = PasswordAuthentication.getSaltedHash(newPass);
                statement.execute("UPDATE users SET PASS = '" + hashPass
                        + "' WHERE USER = '" + log + "' AND uid = '" + userUID + "'");
                passChanged = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return passChanged;
    }

    /**
     * Method gets the file path on the server.
     *
     * @param connection Database connection.
     * @param fileID     File ID.
     * @return File path on the server.
     * @throws SQLException Possible exception.
     */
    public String getFilePathOnServer(Connection connection, String fileID) throws SQLException {
        String pathToFile = null;
        if (connection != null) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT server_path FROM Files WHERE"
                    + " file_id = '" + fileID + "'");
            if (rs.next()) {
                pathToFile = rs.getString("server_path");
            }
            rs.close();
        }
        return pathToFile;
    }

    /**
     * Method checks for file information in the database,
     * and adds or updates file information.
     *
     * @param connection   Database connection.
     * @param inMessage    Message with file data.
     * @param serverPath   File path on server.
     * @param size         File size.
     * @param creationTime Recording time.
     * @return Execution result.
     * @throws SQLException Possible exception.
     */
    public synchronized boolean addFile(Connection connection, TransferFileMessage inMessage, String serverPath, long size, long creationTime) throws SQLException {
        String fileName = inMessage.getName();
        String localPath = inMessage.getCurrentPath();
        boolean fileAdd = false;
        if (connection != null) {
            boolean fileExist = verifyFile(connection, userId, fileName, localPath, serverPath);
            String userId = this.userId;
            Statement statement = connection.createStatement();
            if (fileExist) {
                String str = "UPDATE Files SET creation_time = '" + creationTime + "', last_mod_time = '"
                        + creationTime + "', size = '" + size + "', size = '" + size
                        + "' WHERE server_path = '" + serverPath + "' AND local_path = '" + localPath
                        + "' AND file_name = '" + fileName + "' AND user_id = '" + userId + "'";
                statement.execute(str);
            } else {
                String fileId = createUID();
                String str = "INSERT INTO files "
                        + "(user_id, file_name, local_path, server_path, creation_time,"
                        + " last_mod_time, size, file_id)"
                        + " VALUES('" + userId + "','" + fileName + "','" + localPath
                        + "','" + serverPath + "','" + creationTime + "','" + creationTime
                        + "','" + size + "','" + fileId + "')";
                statement.execute(str);
            }
            fileAdd = true;
        }
        return fileAdd;
    }

    /**
     * Method checks for a file entry.
     *
     * @param connection Database connection.
     * @param userId     User ID.
     * @param fileName   File name.
     * @param localPath  The path to the file on the user's computer.
     * @param serverPath File path on server.
     * @return Check result.
     * @throws SQLException Possible exception.
     */
    private synchronized boolean verifyFile(Connection connection, String userId, String fileName, String localPath, String serverPath) throws SQLException {
        boolean isExist = false;
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

    /**
     * Method gets the file path ID.
     *
     * @param connection Database connection.
     * @param pathToFile Path to file on server.
     * @return File ID.
     * @throws SQLException Possible exception.
     */
    public String getFileID(Connection connection, String pathToFile) throws SQLException {
        String fileID = null;
        if (connection != null) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT file_id FROM Files"
                    + " WHERE server_path = '" + pathToFile + "'");
            if (rs.next()) {
                fileID = rs.getString("file_id");
            }
            rs.close();
        }
        return fileID;
    }

    /**
     * Method gets the path to the file on the user's computer.
     *
     * @param connection Database connection.
     * @param pathToFile Path to file on server.
     * @return Local file path.
     * @throws SQLException Possible exception.
     */
    public String getFileLocalPath(Connection connection, String pathToFile) throws SQLException {
        String filePath = null;
        if (connection != null) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT local_path FROM Files "
                    + "WHERE server_path = '" + pathToFile + "'");
            if (rs.next()) {
                filePath = rs.getString("local_path");
            }
            rs.close();
        }
        return filePath;
    }

    /**
     * Method deletes file on the server.
     *
     * @param connection Database connection.
     * @param pathToFile Current file path.
     * @return Execution result.
     */
    public synchronized boolean deleteFile(Connection connection, String pathToFile) {

        boolean fileDel = false;
        if (connection != null) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DELETE FROM Files WHERE server_path = '" + pathToFile + "' and user_id = '" + userId + "'");
                fileDel = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return fileDel;
    }

    /**
     * Method renames an existing file.
     *
     * @param connection Database connection.
     * @param tecPath    Current file path.
     * @param newPath    New file path.
     * @return Execution result.
     */
    public synchronized boolean renameFile(Connection connection, String tecPath, String newPath) {
        boolean fileRen = false;
        if (connection != null) {
            try (Statement statement = connection.createStatement()) {
                File file = new File(newPath);
                statement.execute("UPDATE Files SET file_name = '"
                        + file.getName() + "', server_path = '" + newPath
                        + "' WHERE server_path = '" + tecPath
                        + "' and user_id = '" + userId + "'");
                fileRen = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return fileRen;
    }

    /**
     * Method renames an existing file.
     *
     * @param connection    Database connection.
     * @param tecPath       Current file path.
     * @param newServerName New file name.
     * @return Execution result.
     */
    public synchronized boolean transferFile(Connection connection, String tecPath, String newServerName) {
        boolean fileRen = false;
        if (connection != null) {
            try (Statement statement = connection.createStatement()) {
                String str = "UPDATE Files SET server_path = '" + newServerName
                        + "' WHERE server_path = '" + tecPath
                        + "' and user_id = '" + userId + "'";
                statement.execute(str);
                fileRen = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return fileRen;
    }

    /**
     * Method tries to connect to the database by property values.
     *
     * @param properties Current properties.
     * @return Execution result.
     * @throws Exception Possible exception.
     */
    public boolean initDatabase(Properties properties) throws Exception {
        this.properties = properties;
        Class.forName(properties.getProperty("driver"));
        Connection connection = DriverManager.getConnection(properties.getProperty("url"));
        return connection != null && initTables(connection);
    }

    /**
     * Method checks database tables.
     *
     * @param connection Database connection.
     * @return Check result.
     * @throws Exception Possible exception.
     */
    private boolean initTables(Connection connection) throws Exception {
        boolean verify = false;
        if (connection != null) {
            Statement statement = connection.createStatement();
            verifyTables(statement);
            verify = true;
        }
        return verify;
    }

    /**
     * Method creates database tables, if they does not exists,
     * and add user record, if table "USERS" is empty.
     *
     * @param statement Created statement.
     * @throws Exception Possible exception.
     */
    private void verifyTables(Statement statement) throws Exception {
        String queryUsers = "CREATE TABLE IF NOT EXISTS USERS("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,"
                + "user VARCHAR(20) UNIQUE NOT NULL, pass VARCHAR(100) NOT NULL, uid VARCHAR(16) NOT NULL);";
        statement.executeUpdate(queryUsers);
        String queryFiles = "CREATE TABLE IF NOT EXISTS FILES(id INTEGER PRIMARY KEY UNIQUE NOT NULL,"
                + " user_id INTEGER REFERENCES users(id) NOT NULL,"
                + "file_name VARCHAR (100) NOT NULL, local_path VARCHAR(100) NOT NULL,"
                + "server_path VARCHAR(100) NOT NULL, creation_time DATETIME NOT NULL,"
                + " last_mod_time DATETIME NOT NULL,"
                + "size INT NOT NULL, file_id VARCHAR(50) UNIQUE NOT NULL);";
        statement.executeUpdate(queryFiles);
    }

    /**
     * Method establishes a database connection.
     *
     * @return Database connection.
     * @throws SQLException Possible exception.
     */
    public Connection connectToDataBase() throws SQLException {
        return DriverManager.getConnection(properties.getProperty("url"));
    }
}
