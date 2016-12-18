package com.rtoth.password.android;

import com.google.common.collect.Lists;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Main activity for the Console Emulator.
 *
 * @author rtoth
 */
public class MainActivity extends AppCompatActivity
{
    /** FIXME: docs. */
    private TextView testTextField;

    /**
     * Create a new {@link MainActivity}.
     */
    public MainActivity()
    {
        // What?
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView applicationListView = (ListView) findViewById(R.id.application_list);
        applicationListView.setAdapter(new ApplicationListAdapter(this, Lists.newArrayList("One", "Two", "Three")));
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
        int id = item.getItemId();
        switch (id)
        {
            case R.id.action_settings:
            {
                // TODO: Somehow display a settings screen
                break;
            }
            default:
            {
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private class ApplicationListAdapter extends ArrayAdapter<String> {

        private List<String> applications;
        private Context context;

        public ApplicationListAdapter(Context context, List<String> applications) {
            super(context, -1, applications);
            this.context = context;
            this.applications = applications;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.application_row, parent, false);

            final String applicationName = applications.get(position);

            TextView applicationNameField = (TextView)row.findViewById(R.id.application_name);
            applicationNameField.setText(applicationName);

            ImageButton show = (ImageButton)row.findViewById(R.id.show_application_password);
            show.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    System.out.println("Would have shown password for " + applicationName);
                }
            });
            ImageButton change = (ImageButton)row.findViewById(R.id.change_application_password);
            change.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    System.out.println("Would have changed password for " + applicationName);
                }
            });
            ImageButton delete = (ImageButton)row.findViewById(R.id.delete_application_password);
            delete.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    System.out.println("Would have deleted password for " + applicationName);
                }
            });

            return row;
        }
    }
}
