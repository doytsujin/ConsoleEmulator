package com.rtoth.console;

import java.io.File;
import java.io.IOException;

/**
 * Created by rtoth on 10/17/2016.
 */
public class ListDirectoryContents
{
    public static String execute(String location)
    {
        StringBuilder builder = new StringBuilder();

        try
        {
            File file = new File(location);
            FileUtilities.assertBasicFilePermissions(file);
            if (file.isDirectory())
            {
                FileUtilities.assertDirectoryPermissions(file);
                for (File subdirectory : file.listFiles())
                {
                    builder.append(getDetails(subdirectory));
                    builder.append("\n");
                }
            }
            else
            {
                builder.append(getDetails(file));
            }
        }
        catch (IOException ioe)
        {
            builder.append("Error: ");
            builder.append(ioe.getMessage());
            builder.append("\n");
        }

        return builder.toString();
    }

    private static String getDetails(File file) throws IOException
    {
        StringBuilder builder = new StringBuilder();

        builder.append(file.canRead() ? "r" : "-");
        builder.append(file.canWrite() ? "w" : "-");
        builder.append(file.canExecute() ? "x" : "-");
        builder.append("\t");
        builder.append(file.length());
        builder.append("\t");
        builder.append(file.getName());
        builder.append("\n");

        return builder.toString();
    }
}
