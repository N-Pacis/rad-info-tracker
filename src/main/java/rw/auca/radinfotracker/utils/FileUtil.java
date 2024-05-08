package rw.auca.radinfotracker.utils;


import rw.auca.radinfotracker.model.enums.EFileSizeType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUtil {
    /**
     * Get FileSizeType from fileSize
     * @param size File Size in TB, GB, MB, KB
     * @return String of FileSizedTypeEnum
     */
    public static String getFileSizeTypeFromFileSize(long size) {
        if (size >= (1024L * 1024 * 1024 * 1024))
            return EFileSizeType.TB.toString();
        else if (size >= 1024 * 1024 * 1024)
            return EFileSizeType.GB.toString();
        else if (size >= 1024 * 1024)
            return EFileSizeType.MB.toString();
        else if (size >= 1024)
            return EFileSizeType.KB.toString();
        else
            return EFileSizeType.B.toString();
    }



    /**
     * Get formatted fileSize from file Size
     * @param size File size
     * @param type FileSize type
     * @return int formattedFileSize
     */
    public static int getFormattedFileSizeFromFileSize(double size, EFileSizeType type ) {
        if (type == EFileSizeType.TB)
            return (int) (size / (1024L * 1024 * 1024 * 1024));
        else if (type == EFileSizeType.GB)
            return (int) (size / (1024 * 1024 * 1024));
        else if (type == EFileSizeType.MB)
            return (int) (size / (1024 * 1024));
        else if (type == EFileSizeType.KB)
            return (int) (size / (1024));
        else
            return (int) size;
    }

    /**
     * Generate Random UUID
     * @param fileName FileName
     * @return String UUID
     */
    public static String generateUUID(String fileName) {
        int period = fileName.indexOf(".");
        String prefix = fileName.substring(0, period);
        String suffix = fileName.substring(period);

        return prefix + "-" +  UUID.randomUUID().toString().replace("-", "")  + suffix;
    }



}
