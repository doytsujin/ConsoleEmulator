package com.rtoth.console;

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
    private final ConsoleEmulator console;

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

        final ScrollView scrollView = (ScrollView) findViewById(R.id.CONSOLE_SCROLL_VIEW);

        final TextView buffer = (TextView) findViewById(R.id.CONSOLE_BUFFER);
        buffer.setText(console.getContent());

        final EditText input = (EditText) findViewById(R.id.CONSOLE_INPUT);
        // Listen for input from the user
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String command = v.getText().toString();
                if (!command.trim().isEmpty())
                {
                    // Execute the command
                    console.execute(command);
                    // Update the buffer
                    buffer.setText(console.getContent());
                    // Clear the input field
                    input.getText().clear();
                    // Scroll the buffer to the bottom and focus back on the
                    // input for the user
                    scrollView.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            scrollView.fullScroll(View.FOCUS_DOWN);
                            input.requestFocus();
                        }
                    });
                    return true;
                }
                return false;
            }
        });
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
