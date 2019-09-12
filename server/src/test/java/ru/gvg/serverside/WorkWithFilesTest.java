package ru.gvg.serverside;

import org.junit.Before;
import org.junit.Test;
import ru.gvg.common.Consts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * @author Valeriy Gyrievskikh
 * @since 12.09.2019
 */
public class WorkWithFilesTest {

    private String tmpPath = "";
    private byte[] data = new byte[]{1, 2, 3, 4};

    @Before
    public void deleteTestFile() throws IOException {
        tmpPath = System.getProperty("java.io.tmpdir");
        Files.deleteIfExists(Paths.get(tmpPath + File.separator + "test1.txt"));
        Files.deleteIfExists(Paths.get(tmpPath + File.separator + "test2.txt"));
        Files.deleteIfExists(Paths.get(tmpPath + File.separator + "test2"));
        Files.deleteIfExists(Paths.get(tmpPath + File.separator + "test3"));
        Files.deleteIfExists(Paths.get(Consts.DIR_PATH + "test1"));
    }

    @Test
    public void whenSaveFileToTempDirThenTrue() throws Exception {
        assertTrue(WorkWithFiles.saveFileOnDisk(tmpPath + File.separator + "test1.txt", data));
    }

    @Test
    public void whenVerifyPathForErasedFileThenFalse() throws Exception {
        assertFalse(WorkWithFiles.verifyPathForFile(tmpPath + File.separator + "test1.txt"));
    }

    @Test
    public void whenGetNotAuthorizedUserFileStructureThenNull() throws Exception {
        File[] file = WorkWithFiles.getUserFileStructure("Test user");
        assertNull(file);
    }

    @Test
    public void whenMakeNewDirThenTrue() throws Exception {
        assertTrue(WorkWithFiles.makeDir("test1"));
    }

    @Test
    public void whenGetExistingFileOnServerThenTrue() throws Exception {
        WorkWithFiles.saveFileOnDisk(tmpPath + File.separator + "test1.txt", data);
        File testFile = WorkWithFiles.getFileOnServer(tmpPath + File.separator + "test1.txt");
        assertTrue(testFile.exists());
    }

    @Test
    public void whenDeleteExistingFileOnServerThenTrue() throws Exception {
        WorkWithFiles.saveFileOnDisk(tmpPath + File.separator + "test1.txt", data);
        File testFile = WorkWithFiles.getFileOnServer(tmpPath + File.separator + "test1.txt");
        assertTrue(WorkWithFiles.deleteFileOnServer(testFile.getAbsolutePath()));
    }

    @Test
    public void whenRenameExistingFileOnServerThenTrue() throws Exception {
        WorkWithFiles.saveFileOnDisk(tmpPath + File.separator + "test1.txt", data);
        File testFile = WorkWithFiles.getFileOnServer(tmpPath + File.separator + "test1.txt");
        assertTrue(WorkWithFiles.renameFileOnServer(testFile, "test2.txt"));
    }

    @Test
    public void renameFolderOnServer() throws Exception {
        Path dirPath = Files.createDirectory(Paths.get(tmpPath + File.separator + "test1"));
        assertTrue(WorkWithFiles.renameFolderOnServer(new File(dirPath.toString()), "test2"));
    }

    @Test
    public void whenDeleteExistingFolderOnServerThenTrue() throws Exception {
        Path dirPath = Files.createDirectory(Paths.get(tmpPath + File.separator + "test1"));
        assertTrue(WorkWithFiles.deleteDirOnServer(dirPath.toString()));
    }

    @Test
    public void whenGetExistingFolderOnServerThenTrue() throws Exception {
        File dir = WorkWithFiles.getFolderOnServer(tmpPath + File.separator + "text2");
        assertTrue(dir.exists() && dir.isDirectory());
    }

    @Test
    public void createNewPathForFile() throws Exception {
    }

}