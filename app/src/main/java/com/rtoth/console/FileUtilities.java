package com.rtoth.console;

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
}
