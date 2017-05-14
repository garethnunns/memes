package com.garethnunns.memestagram;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
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
 * Created by gareth on 14/05/2017.
 * Where the launcher goes to
 * Allows user to log in
 */

public class LoginActivity extends AppCompatActivity {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask AuthTask = null;

    // UI references.
    private EditText UsernameView;
    private EditText PasswordView;
    private View ProgressView;
    private View LoginFormView;

    private SharedPreferences login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = getSharedPreferences("login",MODE_PRIVATE);

        getSupportActionBar().setTitle(R.string.action_sign_in);

        //feed();

        // Set up the login form.
        UsernameView = (EditText) findViewById(R.id.username);

        PasswordView = (EditText) findViewById(R.id.password);
        PasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button EmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        EmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        LoginFormView = findViewById(R.id.login_form);
        ProgressView = findViewById(R.id.login_progress);
    }

    private void feed() {
        //if(login.contains("key")) {
            Intent goFeed = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(goFeed);
        //}
    }


    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (AuthTask != null) {
            return;
        }

        // Reset errors.
        UsernameView.setError(null);
        PasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = UsernameView.getText().toString();
        String password = PasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            PasswordView.setError(getString(R.string.error_field_required));
            focusView = PasswordView;
            cancel = true;
        }

        // Check for a username isn't blank
        if (TextUtils.isEmpty(email)) {
            UsernameView.setError(getString(R.string.error_field_required));
            focusView = UsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            AuthTask = new UserLoginTask(email, password);
            AuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            LoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            LoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    LoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            ProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            ProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            ProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            LoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String username;
        private final String password;
        public String error = "";

        UserLoginTask(String u, String p) {
            username = u;
            password = p;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                error = "Incorrect username or password";
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(username)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(password);
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            AuthTask = null;
            showProgress(false);

            if (success) {
                // TODO: set login shred pref here (username, password, key, userid)
                feed();
            } else {
                PasswordView.setError(error);
                PasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            AuthTask = null;
            showProgress(false);
        }
    }
}