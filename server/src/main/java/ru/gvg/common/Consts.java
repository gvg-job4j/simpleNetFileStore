package ru.gvg.common;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Default start values.
 *
 * @author Valeriy Gyrievskikh
 * @since 01.03.2019
 */
public class Consts {

    /**
     * Max file size for one segment.
     */
    public static final int FILE_SIZE = 10485760;
    /**
     * Format for date representation.
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("hh:mm:ss a");
    /**
     * Path for file store folder on server.
     */
    public static final String DIR_PATH = "D:" + File.separator + "FilesDB" + File.separator;
    /**
     * Default port for client connect.
     */
    public static final int PORT = 8089;
    /**
     * Default file store space for user.
     */
    public static final int USER_SIZE = 200;
    /**
     * Default time for user session.
     */
    public static final int SESSION_TIME = 300;
    /**
     * Maximum number of simultaneously opened connections.
     */
    public static final int NTHREADS = 2;

}
