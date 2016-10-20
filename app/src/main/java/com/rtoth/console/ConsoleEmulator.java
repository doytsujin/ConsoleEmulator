package com.rtoth.console;

import com.google.common.base.Preconditions;
import com.google.common.collect.EvictingQueue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

    /** Circular buffer of historical commands to display. */
    private final EvictingQueue<String> buffer;

    /** Name of the current user of this console. */
    private String user;

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
     * Set the current user of this console to the provided value.
     *
     * @param user New user. Cannot be {@code null}.
     *
     * @throws NullPointerException if {@code user} is {@code null}.
     */
    public void setUser(@NonNull String user)
    {
        this.user = Preconditions.checkNotNull(user, "user cannot be null.");
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
                    buffer.clear();
                    result = "";
                }
                else
                {
                    result = "Usage: clear";
                }
                break;
            }
            case "whoami":
            {
                if (args.length == 0)
                {
                    result = user;
                }
                else
                {
                    result = "Usage: whoami";
                }
                break;
            }
            default:
            {
                result = executeShellCommand(commandStr);
                break;
            }
        }

        if (!result.trim().isEmpty())
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

    // TODO: Implement signaling to kill processes and such

    // TODO: Handle user input for processes

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
     * Execute the provided shell command using the Java Runtime.
     *
     * @param shellCommand Command to execute. Cannot be {@code null}.
     * @return All output (including stdout and stderr) from the execution of
     *         the command. Never {@code null}.
     *
     * @throws NullPointerException if {@code shellCommand} is {@code null}.
     */
    private String executeShellCommand(@NonNull String shellCommand)
    {
        Preconditions.checkNotNull(shellCommand, "command cannot be null.");

        String result;
        try
        {
            Process p = new ProcessBuilder("sh", "-c", shellCommand)
                .directory(currentDirectory)
                .redirectErrorStream(true)
                .start();

            p.waitFor();

            // No need to read stderr since we're redirecting above
            BufferedReader stdOut = new BufferedReader(
                new InputStreamReader(p.getInputStream()));

            StringBuilder output = new StringBuilder();

            String line = stdOut.readLine();
            boolean first = true;
            while (line != null)
            {
                if (!first)
                {
                    output.append("\n");
                }
                output.append(line);
                line = stdOut.readLine();
                first = false;
            }

            result = output.toString();
        }
        catch (IOException | InterruptedException e)
        {
            result = "Error: " + e.getMessage();
        }

        return result;
    }
}
