package com.rtoth.console;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.io.File;
import java.io.IOException;

import android.support.annotation.NonNull;
import android.support.annotation.PluralsRes;

/**
 * Contains various utilities for performing operations on files.
 * TODO: Should probably add some unit tests.
 *
 * @author rtoth
 */
public final class FileUtilities
{
    /**
     * Private constructor for utility class.
     */
    private FileUtilities()
    {
        // Nothing to see here.
    }

    /**
     * Verify that the provided file exists, and has read permissions.
     *
     * @param file File to verify. Cannot be {@code null}.
     *
     * @throws IOException if {@code file} does not exist or cannot be read.
     * @throws NullPointerException if {@code file} is {@code null}.
     */
    public static void assertBasicFilePermissions(@NonNull File file) throws IOException
    {
        Preconditions.checkNotNull(file, "file cannot be null.");

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

    /**
     * Verify that the provided file is a directory, in addition to
     * {@link #assertBasicFilePermissions(File) basic permissions}.
     *
     * @param directory File to verify. Cannot be {@code null}.
     *
     * @throws IOException if {@code file} does not exist, cannot be read, or is not
     *                     a directory.
     * @throws NullPointerException if {@code file} is {@code null}.
     */
    public static void assertDirectoryPermissions(@NonNull File directory) throws IOException
    {
        Preconditions.checkNotNull(directory, "directory cannot be null.");

        assertBasicFilePermissions(directory);
        if (!directory.isDirectory())
        {
            throw new IOException(
                String.format("'%s' is not a directory.", directory));
        }
    }
}
