package ru.gvg.serverside;

import ru.gvg.common.Consts;
import ru.gvg.messages.MessageHandler;
import ru.gvg.messages.MessageManager;
import ru.gvg.messages.Messaging;
import ru.gvg.messages.TransferFileMessage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static ru.gvg.common.Consts.FILE_SIZE;

/**
 * Operations with files.
 *
 * @author Valeriy Gyrievskikh
 * @since 28.04.2019
 */
public class FileManager {

    /**
     * Method saving data to a file.
     *
     * @param dir File path.
     * @param arr Data for saving.
     * @return Operation result.
     */
    public static boolean saveFileOnDisk(String dir, byte[] arr) {
        boolean saved = false;
        try {
            Files.write(Paths.get(dir), arr, StandardOpenOption.CREATE);
            saved = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return saved;
    }

    /**
     * Method checks for a file at the specified path.
     *
     * @param path File path.
     * @return Operation result.
     */
    public static boolean verifyPathForFile(String path) {
        return Files.exists(Paths.get(path));
    }

    /**
     * Method gets the file structure for specified user.
     *
     * @param user User name.
     * @return Array of files.
     */
    public static File[] getUserFileStructure(String user) {
        File[] userFiles = null;
        if (user != null) {
            File file = new File(Consts.DIR_PATH + user);
            userFiles = file.listFiles();
        }
        return userFiles;
    }

    /**
     * Method calculates used space for specified user.
     *
     * @param user  User name.
     * @param files Array of files.
     * @return Used space.
     */
    public static int getUserFileSize(String user, File[] files) {
        int size = 0;
        if (user != null && files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    size += files[i].length();
                } else {
                    size += getUserFileSize(user, files[i].listFiles());
                }
            }
        }
        return size;
    }

    /**
     * Method creates folder with specified name.
     *
     * @param nameDir Folder name.
     * @return Operation result.
     */
    public static boolean makeDir(String nameDir) {
        boolean makeDir = false;
        try {
            Files.createDirectory(Paths.get(nameDir));
            makeDir = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return makeDir;
    }

    /**
     * Mehtod retrieves the file at the specified path.
     *
     * @param path File path.
     * @return Received file (or null).
     */
    public static File getFileOnServer(String path) {
        File file = new File(path);
        if (!(file.exists() && file.isFile())) {
            file = null;
        }
        return file;
    }

    /**
     * Method deletes file at the specified path.
     *
     * @param path File path.
     * @return Operation result.
     */
    public static boolean deleteFileOnServer(String path) {
        File file = new File(path);
        return file.exists() && file.isFile() && file.delete();
    }

    /**
     * Method renamed selected file.
     *
     * @param file    Selected file.
     * @param newName New name.
     * @return Operation result.
     */
    public static boolean renameFileOnServer(File file, String newName) {
        String tecName = file.getAbsolutePath();
        StringBuilder nameBuilder = new StringBuilder(tecName);
        int index1 = tecName.lastIndexOf(File.separator);
        nameBuilder.replace(index1 + 1, tecName.length(), newName);
        return file.renameTo(new File(nameBuilder.toString()));
    }

    /**
     * Method transfered selected file to another folder.
     *
     * @param file    Selected file.
     * @param newName New folder.
     * @return Operation result.
     */
    public static boolean transferFileOnServer(File file, String newName) {
        String tecName = file.getAbsolutePath();
        int index1 = tecName.lastIndexOf(File.separator);
        String newFileName = Consts.DIR_PATH + newName + tecName.substring(index1);
        return file.renameTo(new File(newFileName));
    }

    /**
     * Method renamed selected folder.
     *
     * @param folder  Selected folder.
     * @param newName New folder name.
     * @return Operation result.
     */
    public static boolean renameFolderOnServer(File folder, String newName) {
        boolean renamed = false;
        File file = new File(folder.getAbsolutePath());
        if (file.exists() && file.isDirectory() && file.listFiles().length == 0) {
            String newFolderName = createNewPathForFile(file, newName);
            if (file.renameTo(new File(newFolderName))) {
                renamed = true;
            }
        }
        return renamed;
    }

    /**
     * Method deletes a folder on the server.
     *
     * @param nameDir Folder name to delete.
     * @return Operation result.
     */
    public static boolean deleteDirOnServer(String nameDir) {
        boolean deleted = false;
        File file = new File(nameDir);
        if (file.exists() && file.isDirectory() && file.listFiles().length == 0) {
            try {
                deleted = Files.deleteIfExists(Paths.get(nameDir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return deleted;
    }

    /**
     * Method retrieves folder at the specified path.
     *
     * @param path Specified path.
     * @return Folder (or null).
     */
    public static File getFolderOnServer(String path) {
        File file = new File(path);
        if (!(file.exists() && file.isDirectory())) {
            file = null;
        }
        return file;
    }

    /**
     * Method created path for selected file.
     *
     * @param file    Selected file.
     * @param newName New file name.
     * @return File absolute pathname.
     */
    public static String createNewPathForFile(File file, String newName) {
        String tecName = file.getAbsolutePath();
        StringBuilder nameBuilder = new StringBuilder(tecName);
        int index1 = tecName.lastIndexOf(File.separator);
        int index2 = tecName.lastIndexOf(".");
        if (index2 != -1) {
            nameBuilder.replace(index1 + 1, index2, newName);
        } else {
            nameBuilder.replace(index1 + 1, tecName.length(), newName);
        }
        return nameBuilder.toString();
    }

    /**
     * Method creates path on the server for the selected file.
     *
     * @param localName Path to the file on the client.
     * @param userUID   User UID.
     * @return Path on the server.
     */
    public static String changePathFromLocalToServer(String localName, String userUID) {
        StringBuilder sb = new StringBuilder(localName);
        if (sb.indexOf(File.separator) != -1) {
            sb.replace(0, sb.indexOf(File.separator), userUID);
        } else {
            sb.insert(0, userUID + File.separator);
        }
        return Consts.DIR_PATH + sb.toString();
    }

    /**
     * Method gets the file name from the path to the file on the client.
     *
     * @param localName Path to the file on the client.
     * @return File name.
     */
    public static String getFileName(String localName) {
        StringBuilder sb = new StringBuilder(localName);
        if (sb.lastIndexOf(File.separator) != -1) {
            sb.replace(0, sb.lastIndexOf(File.separator), "");
        }
        return sb.toString();
    }

    /**
     * Method passes the file data through the socket.
     *
     * @param currentPathToFile Current path to file.
     * @param filename          File name.
     * @param socket            Current socket.
     * @return message.
     */
    public static Messaging sendFile(String currentPathToFile, String filename, Socket socket) {
        Messaging inMessage = null;
        try {
            long fileSize = (long) Files.getAttribute(Paths.get(currentPathToFile), "size");
            InputStream is = new BufferedInputStream(Files.newInputStream(Paths.get(currentPathToFile)));
            byte[] fileInputData = new byte[FILE_SIZE];
            int bytes = is.read(fileInputData);
            int current = 1;
            while (bytes != -1) {
                if (bytes < fileInputData.length - 1) {
                    fileInputData = Arrays.copyOf(fileInputData, bytes);
                }
                Messaging outMessage = MessageHandler.createTransferFileMessage(filename, null, currentPathToFile, fileSize, fileInputData, current, current);
                current++;
                MessageManager.sendMessage(socket, outMessage);
                inMessage = MessageManager.getMessage(socket);
                bytes = is.read(fileInputData);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inMessage;
    }

    /**
     * Method writes a file to disk.
     *
     * @param inMessage  message ({@code TransferFileMessage}) with file data.
     * @param pathToFile Path to the file on disk.
     * @return File size.
     * @throws IOException Possible exception.
     */
    public static long writeFile(TransferFileMessage inMessage, String pathToFile) throws IOException {
        long fileSize = 0;
        Path fileOnServer = Paths.get(pathToFile);
        byte[] data = inMessage.getData();
        if (inMessage.getCurrent() == 1) {
            if (Files.exists(fileOnServer) && Files.isRegularFile(fileOnServer)) {
                Files.delete(fileOnServer);
            }
            Files.write(fileOnServer, data, StandardOpenOption.CREATE);
        } else {
            Files.write(fileOnServer, data, StandardOpenOption.APPEND);
        }
        fileSize = (long) Files.getAttribute(fileOnServer, "size");
        return fileSize;
    }
}
