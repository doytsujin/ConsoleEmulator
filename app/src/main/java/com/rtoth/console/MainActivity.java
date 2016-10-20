package com.rtoth.console;

import java.util.Random;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Main activity for the Console Emulator.
 *
 * @author rtoth
 */
public class MainActivity extends AppCompatActivity
{
    /** Random number generator used for randomized behavior. */
    private final Random random = new Random();

    /** FIXME: docs. */
    private final ConsoleEmulator console;

    /** FIXME: docs. */
    private ScrollView consoleScrollView;

    /** FIXME: docs. */
    private TextView consoleBuffer;

    /** FIXME: docs. */
    private EditText consoleInput;

    /**
     * Create a new {@link MainActivity}.
     */
    public MainActivity()
    {
        // TODO: Make these parameters configurable in settings
        console = new ConsoleEmulator("rtoth", 50);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get our view components
        consoleScrollView = (ScrollView) findViewById(R.id.console_scroll_view);
        consoleBuffer = (TextView) findViewById(R.id.console_buffer);
        consoleInput = (EditText) findViewById(R.id.console_input);

        // Set the initial buffer contents
        consoleBuffer.setText(console.getContent());
        // Listen for input from the user
        consoleInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String command = v.getText().toString();
                if (!command.trim().isEmpty())
                {
                    // Execute the command
                    console.execute(command);
                    // Update the buffer
                    consoleBuffer.setText(console.getContent());
                    // Clear the input field
                    consoleInput.setText("");
                    // Scroll the buffer to the bottom and focus back on the
                    // input for the user
                    jumpToBottom(consoleInput);
                }
                return false;
            }
        });
    }

    /**
     * Jump to the top of the console view.
     *
     * @param view View which should receive focus after scrolling.
     *             If {@code null}, nothing is focused.
     */
    public void jumpToTop(final View view)
    {
        if (consoleScrollView != null)
        {
            consoleScrollView.post(new Runnable()
            {
                @Override
                public void run()
                {
                    consoleScrollView.fullScroll(View.FOCUS_UP);
                    if (view != null)
                    {
                        view.requestFocus();
                    }
                }
            });
        }
    }

    /**
     * Jump to the bottom of the console view.
     *
     * @param view View which should receive focus after scrolling.
     *             If {@code null}, nothing is focused.
     */
    public void jumpToBottom(final View view)
    {
        if (consoleScrollView != null)
        {
            consoleScrollView.post(new Runnable()
            {
                @Override
                public void run()
                {
                    consoleScrollView.fullScroll(View.FOCUS_DOWN);
                    if (view != null)
                    {
                        view.requestFocus();
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        System.out.println("R: " + R.class);
        System.out.println("R.id" + R.id.class);
        switch (id)
        {
            case R.id.action_settings:
            {
                // TODO: Somehow display a settings screen
                break;
            }
            case R.id.action_jump_to_top:
            {
                jumpToTop(consoleInput);
                break;
            }
            case R.id.action_jump_to_bottom:
            {
                jumpToBottom(consoleInput);
                break;
            }
            case R.id.action_set_random_user:
            {
                StringBuilder userBuilder = new StringBuilder();
                for (int i = 0; i < (5 + random.nextInt(6)); i++)
                {
                    int ascii = ((int)'a' + random.nextInt(26));
                    userBuilder.append((char) ascii);
                }
                console.setUser(userBuilder.toString());
                consoleBuffer.setText(console.getContent());
                // Scroll the buffer to the bottom and focus back on the
                // input for the user
                jumpToBottom(consoleInput);
                consoleInput.requestFocus();
                break;
            }
            default:
            {
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
