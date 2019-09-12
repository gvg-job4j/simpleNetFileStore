package ru.gvg.serverside;

import ru.gvg.common.Consts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Operations with files.
 *
 * @author Valeriy Gyrievskikh
 * @since 28.04.2019
 */
public class WorkWithFiles {

    /**
     * Metod saving data to a file.
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
     * Metod checks for a file at the specified path.
     *
     * @param path File path.
     * @return Operation result.
     */
    public static boolean verifyPathForFile(String path) {
        return Files.exists(Paths.get(path));
    }

    /**
     * Metod get the file structure for specified user.
     *
     * @param user User name.
     * @return Array of files.
     */
    public static File[] getUserFileStructure(String user) {
        File file = new File(Consts.DIR_PATH + user);
        return file.listFiles();
    }

    /**
     * Metod calculate used space for specified user.
     *
     * @param user  User name.
     * @param files Array of files.
     * @return Used space.
     */
    public static int getUserFileSize(String user, File[] files) {
        int size = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                size += files[i].length();
            } else {
                size += getUserFileSize(user, files[i].listFiles());
            }
        }
        return size;
    }

    /**
     * Metod create folder with specified name.
     *
     * @param nameDir Folder name.
     * @return Operation result.
     */
    public static boolean makeDir(String nameDir) {
        boolean makeDir = false;
        String pathDir = Consts.DIR_PATH + nameDir;
        try {
            Files.createDirectory(Paths.get(pathDir));
            makeDir = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return makeDir;
    }

    /**
     * Metod retrieves the file at the specified path.
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
     * Metod delete file at the specified path.
     *
     * @param path File path.
     * @return Operation result.
     */
    public static boolean deleteFileOnServer(String path) {
        File file = new File(path);
        return file.exists() && file.isFile() && file.delete();
    }

    /**
     * Metod renamed selected file.
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
     * Metod transfer selected file to another folder.
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
     * Metod renamed selected folder.
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
     * Metod deletes a folder on the server.
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
     * Metod retrieves folder at the specified path.
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
     * Metod created path for selected file.
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
}
