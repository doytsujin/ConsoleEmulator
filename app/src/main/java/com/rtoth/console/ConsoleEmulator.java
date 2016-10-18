package com.rtoth.console;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import android.os.Environment;

/**
 * Created by rtoth on 10/17/2016.
 */
public class ConsoleEmulator
{
    private File currentDirectory;

    public ConsoleEmulator()
    {
        currentDirectory = Environment.getRootDirectory();
        try
        {
            FileUtilities.assertDirectoryPermissions(currentDirectory);
        }
        catch (IOException ioe)
        {
            throw new IllegalStateException("Unable to create console.", ioe);
        }
    }

    public String execute(String commandStr)
    {
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
                    result = ListDirectoryContents.execute(getAbsolutePath(args[0]));
                }
                else if (args.length == 0)
                {
                    result = ListDirectoryContents.execute(currentDirectory.getAbsolutePath());
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
        return result;
    }

    private String changeDirectory(String location)
    {
        try
        {
            File directory = new File(getAbsolutePath(location));
            FileUtilities.assertDirectoryPermissions(directory);
            currentDirectory = directory;
            return "";
        }
        catch (IOException ioe)
        {
            return "Error: " + ioe.getMessage();
        }
    }

    private String getAbsolutePath(String location)
    {
        File file = new File(location);
        if (file.isAbsolute())
        {
            return location;
        }
        else
        {
            return new File(currentDirectory.getAbsolutePath() + File.separator + location).getAbsolutePath();
        }
    }

    private String getUnknownCommandMessage(String command)
    {
        return String.format("Unrecognized command: '%s'", command);
    }
}
