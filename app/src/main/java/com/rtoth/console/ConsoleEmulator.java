package com.rtoth.console;

import com.google.common.collect.EvictingQueue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import android.os.Environment;
import android.support.annotation.NonNull;

/**
 * Created by rtoth on 10/17/2016.
 */
public class ConsoleEmulator
{
    private static final String PROMPT_FORMAT_STR = "%s@android:%s$ ";

    private final String user;

    private final EvictingQueue<String> buffer;

    private File currentDirectory;

    public ConsoleEmulator(@NonNull String user, int bufferSize)
    {
        this.user = user;
        this.buffer = EvictingQueue.create(bufferSize);
        this.currentDirectory = Environment.getRootDirectory();
        try
        {
            FileUtilities.assertDirectoryPermissions(currentDirectory);
        }
        catch (IOException ioe)
        {
            throw new IllegalStateException("Unable to create console.", ioe);
        }
    }

    public void execute(String commandStr)
    {
        // First append the current prompt since that will want to be part of the history
        buffer.add(getPrompt());

        String[] commandPlusArgs = commandStr.split("\\s+");
        String command = commandPlusArgs[0];
        String[] args = Arrays.copyOfRange(commandPlusArgs, 1, commandPlusArgs.length);

        String result;
        switch (command)
        {
            case "ls":
            {
                if (args.length == 1)
                {
                    result = listDirectory(args[0]);
                }
                else if (args.length == 0)
                {
                    result = listDirectory(currentDirectory.getAbsolutePath());
                }
                else
                {
                    result = "Usage: ls <directory>";
                }
                break;
            }
            case "pwd":
            {
                if (args.length == 0)
                {
                    result = currentDirectory.getAbsolutePath();
                }
                else
                {
                    result = "Usage: pwd";
                }
                break;
            }
            case "cd":
            {
                if (args.length == 1)
                {
                    result = changeDirectory(args[0]);
                }
                else
                {
                    result = "Usage: cd <directory>";
                }
                break;
            }
            case "clear":
            {
                if (args.length == 0)
                {
                    result = "";
                }
                else
                {
                    result = "Usage: clear";
                }
                break;
            }
            default:
            {
                result = getUnknownCommandMessage(commandStr);
                break;
            }
        }
        if (result != null && !result.trim().isEmpty())
        {
            buffer.add(result);
        }
    }

    public String getContent()
    {
        StringBuilder builder = new StringBuilder();
        for (String output : buffer)
        {
            builder.append(output);
            builder.append("\n");
        }
        builder.append(getPrompt());

        return builder.toString();
    }

    private String getPrompt()
    {
        return String.format(PROMPT_FORMAT_STR, user, currentDirectory.getAbsolutePath());
    }

    private String listDirectory(String location)
    {
        StringBuilder builder = new StringBuilder();

        try
        {
            File file = getCanonicalFile(location);
            FileUtilities.assertBasicFilePermissions(file);
            if (file.isDirectory())
            {
                FileUtilities.assertDirectoryPermissions(file);
                for (File subdirectory : file.listFiles())
                {
                    builder.append(FileUtilities.getDetails(subdirectory));
                    builder.append("\n");
                }
            }
            else
            {
                builder.append(FileUtilities.getDetails(file));
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

    private String changeDirectory(String location)
    {
        try
        {
            File directory = getCanonicalFile(location);
            FileUtilities.assertDirectoryPermissions(directory);
            currentDirectory = directory;
            return "";
        }
        catch (IOException ioe)
        {
            return "Error: " + ioe.getMessage();
        }
    }

    private File getCanonicalFile(String location) throws IOException
    {
        File file = new File(location);
        if (file.isAbsolute())
        {
            return file.getCanonicalFile();
        }
        else
        {
            return new File(currentDirectory.getAbsolutePath() + File.separator + location)
                .getCanonicalFile();
        }
    }

    private String getUnknownCommandMessage(String command)
    {
        return String.format("Unrecognized command: '%s'", command);
    }
}
