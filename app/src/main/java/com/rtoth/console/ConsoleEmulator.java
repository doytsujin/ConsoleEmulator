package com.rtoth.console;

import com.google.common.base.Preconditions;
import com.google.common.collect.EvictingQueue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import android.os.Environment;
import android.support.annotation.NonNull;

/**
 * Represents a single emulated console.
 * TODO: Should probably add some unit tests.
 *
 * @author rtoth
 */
public class ConsoleEmulator
{
    /**
     * Format string for the console's prompt. Filled in with the user and
     * current directory.
     */
    private static final String PROMPT_FORMAT_STR = "%s@android:%s$ ";

    /** Name of the user using this console. */
    private final String user;

    /** Circular buffer of historical commands to display. */
    private final EvictingQueue<String> buffer;

    /** Current working directory. */
    private File currentDirectory;

    /**
     * Create a new {@link ConsoleEmulator} using the provided parameters.
     *
     * @param user Name of the user using this console. Cannot be {@code null}.
     * @param bufferSize Maximum size of the buffer of historical commands. Must
     *                   be &ge; 1.
     *
     * @throws IllegalArgumentException if {@code bufferSize} is &lt; 1.
     * @throws NullPointerException if {@code user} is {@code null}.
     */
    public ConsoleEmulator(@NonNull String user, int bufferSize)
    {
        this.user = Preconditions.checkNotNull(user, "user cannot be null.");
        Preconditions.checkArgument(bufferSize >= 1, "bufferSize must be >= 1.");
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

    /**
     * Attempt to execute the provided command.
     * <p>
     * Output from the command will be appended to the current buffer, along with
     * the prompt at the time the command was executed such that subsequent calls
     * to {@link #getContent()} will return the previous buffer content plus this
     * command's output.
     *
     * @param commandStr Command to execute. Cannot be {@code null}.
     * @return The output of the command, if any. Never {@code null}.
     *
     * @throws NullPointerException if {@code commandStr} is {@code null}.
     */
    public String execute(@NonNull String commandStr)
    {
        Preconditions.checkNotNull(commandStr, "commandStr cannot be null.");

        // First append the current prompt since we want that to be part of the history
        buffer.add(getPrompt() + commandStr);

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
            case "echo":
            {
                if (args.length == 1)
                {
                    String arg = args[0];
                    if (arg.length() >= 2 &&
                        (arg.startsWith("\"") && arg.endsWith("\"") ||
                        arg.startsWith("\'") && arg.endsWith("\'")))
                    {
                        result = arg.substring(1, arg.length() - 1);
                    }
                    else
                    {
                        result = "Usage: echo \"<string>\"";
                    }
                }
                else
                {
                    result = "Usage: echo \"<string>\"";
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

        return result;
    }

    /**
     * Get the current content of the command history buffer including the current
     * and previous prompts and commands.
     * <p>
     * An example of the format in which this content is returned is as follows:
     * <pre>
     *     user@android:/root$ ls
     *     r-x      1234    file1
     *     r-x      1234    file2
     *     user@android:/root$ pwd
     *     /root
     *     user@android:/root$ cd ..
     *     user@android:/$
     * </pre>
     *
     * @return The current content of the command history buffer including the current
     *         and previous prompts and commands. Never {@code null}.
     */
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

    /**
     * Get the current prompt based on the user and current working directory.
     *
     * @return The current prompt. Never {@code null}.
     */
    private String getPrompt()
    {
        return String.format(PROMPT_FORMAT_STR, user, currentDirectory.getAbsolutePath());
    }

    /**
     * Attempt to list the contents of the provided file location.
     * <p>
     * If the provided file is a directory, a detailed listing of its children is returned.
     * If the provided directory is a regular file, only a detailed listing of itself is
     * returned. Otherwise, if the provided location is invalid for any reason (i.e. does
     * not exist, unreadable), an appropriate error message is returned.
     *
     * @param location Location to list. Cannot be {@code null}.
     * @return The command output (on success), or error message (on failure) to append
     *         to the buffer. Never {@code null}.
     *
     * @throws NullPointerException if {@code location} is {@code null}.
     */
    private String listDirectory(@NonNull String location)
    {
        Preconditions.checkNotNull(location, "location cannot be null.");

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

    /**
     * Attempt to change the current working directory to the provided location.
     * <p>
     * If the provided location is invalid for any reason (i.e. does not exist,
     * unreadable, not a directory), the current working directory is not modified,
     * and an appropriate error message is returned.
     *
     * @param location Directory to which to change. Cannot be {@code null}.
     * @return The command output (on success), or error message (on failure) to append
     *         to the buffer. Never {@code null}.
     *
     * @throws NullPointerException if {@code location} is {@code null}.
     */
    private String changeDirectory(@NonNull String location)
    {
        Preconditions.checkNotNull(location, "location cannot be null.");

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

    /**
     * Get the canonical file representation of the provided location.
     *
     * @param location Location for which to get the canonical representation. Cannot
     *                 be {@code null}.
     * @return The canonical file representation of {@code input}. Never {@code null}.
     *
     * @throws IOException if there is a problem getting the canonical location of
     *                     {@code location}.
     * @throws NullPointerException if {@code location} is {@code null}.
     */
    private File getCanonicalFile(@NonNull String location) throws IOException
    {
        Preconditions.checkNotNull(location, "location cannot be null.");

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

    /**
     * Get an 'unknown command' message for the provided command.
     *
     * @param command Unknown command contents. Cannot be {@code null}.
     * @return 'Unknown command' message for the provided command. Never {@code null}.
     *
     * @throws NullPointerException if {@code command} is {@code null}.
     */
    private String getUnknownCommandMessage(@NonNull String command)
    {
        Preconditions.checkNotNull(command, "command cannot be null.");

        return String.format("Unrecognized command: '%s'", command);
    }
}
