/*
 * Copyright (c) 2016 Robert Toth
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.rtoth.password.android;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.rtoth.password.core.PasswordManager;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Main activity for the Password Hero app.
 *
 * @author rtoth
 */
public class MainActivity extends AppCompatActivity
{
    private PasswordManager passwordManager;

    private ApplicationListAdapter applicationListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        passwordManager = UserEnvironment.getInstance().getPasswordManager();
        applicationListAdapter = new ApplicationListAdapter(this, passwordManager.getAvailableApplications());

        ListView applicationListView = (ListView) findViewById(R.id.application_list);
        applicationListView.setAdapter(applicationListAdapter);
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
            case R.id.action_add_application:
            {
                addNewApplication();
                break;
            }
            default:
            {
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void addNewApplication()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Title");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String application = input.getText().toString();
                if (passwordManager.hasPassword(application))
                {
                    AlertDialog.Builder error = new AlertDialog.Builder(MainActivity.this);
                    error.setTitle("Error");
                    error.setTitle("Application Already Exists!");
                    error.setNeutralButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });
                    error.show();
                }
                else
                {
                    passwordManager.generatePassword(application);
                    applicationListAdapter.add(application);
                    showApplicationPassword(application);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showApplicationPassword(String applicationName)
    {
        TextView passwordText = new TextView(this);
        passwordText.setText(passwordManager.getPlaintextPassword(applicationName));
        passwordText.setTextIsSelectable(true);

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Stored Password for " + applicationName);
        alert.setView(passwordText);
        alert.setNeutralButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    /**
     * FIXME: docs
     */
    private class ApplicationListAdapter extends ArrayAdapter<String> {

        private List<String> applications;
        private Context context;

        public ApplicationListAdapter(Context context, List<String> applications) {
            super(context, -1, applications);
            this.context = context;
            this.applications = applications;
        }

        @Override
        // FIXME: Do the ViewHolder thing this warning is suggesting.
        @SuppressWarnings("ViewHolder")
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
                    showApplicationPassword(applicationName);
                }
            });
            ImageButton change = (ImageButton)row.findViewById(R.id.change_application_password);
            change.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // FIXME: Add confirmation dialog
                    passwordManager.changePassword(applicationName);
                    showApplicationPassword(applicationName);
                }
            });
            ImageButton delete = (ImageButton)row.findViewById(R.id.delete_application_password);
            delete.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // FIXME: Add confirmation dialog
                    passwordManager.deletePassword(applicationName);
                    applicationListAdapter.remove(applicationName);
                }
            });

            return row;
        }
    }
}
