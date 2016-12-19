package com.rtoth.password.android;

import com.rtoth.password.core.PasswordManager;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import java.io.File;
import java.io.IOException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity
{
    public static final String PASSWORD_FILE = LoginActivity.class.getName() + ".PASSWORD_FILE";
    public static final String MASTER_PASSWORD = LoginActivity.class.getName() + ".MASTER_PASSWORD";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private AuthenticationTask authTask = null;

    // UI references.
    private EditText passwordView;
    private View progressView;
    private View loginFormView;

    private File passwordFile;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        passwordFile = getPasswordFile();

        passwordView = (EditText) findViewById(R.id.password);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == R.id.login || id == EditorInfo.IME_NULL)
                {
                    attemptAuthentication();
                    return true;
                }
                return false;
            }
        });

        Button submitButton = (Button) findViewById(R.id.submit_password_button);
        submitButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                attemptAuthentication();
            }
        });

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);
    }

    private File getPasswordFile()
    {
        File file = new File(getFilesDir().getAbsolutePath() + File.separator + "test-data");
        if (!file.exists())
        {
            boolean creationResult = true;
            try
            {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists())
                {
                    creationResult = parent.mkdirs();
                }
                creationResult &= file.createNewFile();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
                creationResult = false;
            }

            if (!creationResult)
            {
                throw new IllegalStateException("Unable to create " + file.getAbsolutePath());
            }
        }

        return file;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptAuthentication()
    {
        if (authTask != null)
        {
            return;
        }

        // Reset errors.
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password))
        {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            authTask = new AuthenticationTask(password);
            authTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
        else
        {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void launchMainActivity(PasswordManager passwordManager)
    {
        UserEnvironment.getInstance().setPasswordManager(passwordManager);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class AuthenticationTask extends AsyncTask<Void, Void, Boolean>
    {
        private final String password;

        private PasswordManager passwordManager;

        AuthenticationTask(String password)
        {
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            // TODO: attempt authentication against a network service.

            try
            {
                // Simulate network access.
                Thread.sleep(2000);
            }
            catch (InterruptedException e)
            {
                return false;
            }

            try
            {
                // Validate the password by creating a new password manager
                passwordManager = new PasswordManager(passwordFile, password);
                return true;
            }
            catch (IOException | EncryptionOperationNotPossibleException e)
            {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            authTask = null;
            showProgress(false);

            if (success)
            {
                launchMainActivity(passwordManager);
            }
            else
            {
                passwordView.setError(getString(R.string.error_invalid_password));
                passwordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled()
        {
            authTask = null;
            showProgress(false);
        }
    }
}

