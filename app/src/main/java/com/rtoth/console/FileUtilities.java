package com.rtoth.console;

import com.google.common.base.Strings;

import java.io.File;
import java.io.IOException;

import android.support.annotation.NonNull;

/**
 * Created by rtoth on 10/17/2016.
 */
public class FileUtilities
{
    private FileUtilities()
    {
        // Nothing to see here.
    }

    public static void assertBasicFilePermissions(@NonNull File file) throws IOException
    {
        if (!file.exists())
        {
            throw new IOException(
                String.format("'%s' does not exist.", file));
        }
        else if (!file.canRead())
        {
            throw new IOException(
                String.format("'%s' is not readable.", file));
        }
    }

    public static void assertDirectoryPermissions(@NonNull File directory) throws IOException
    {
        assertBasicFilePermissions(directory);
        if (!directory.isDirectory())
        {
            throw new IOException(
                String.format("'%s' is not a directory.", directory));
        }
    }

    public static String getDetails(File file)
    {
        StringBuilder builder = new StringBuilder();

        builder.append(file.canRead() ? "r" : "-");
        builder.append(file.canWrite() ? "w" : "-");
        builder.append(file.canExecute() ? "x" : "-");
        builder.append("\t");
        builder.append(Strings.padStart(String.valueOf(file.length()), 13, ' '));
        builder.append("\t");
        builder.append(file.getName());

        return builder.toString();
    }
}
