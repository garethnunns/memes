package com.garethnunns.memestagram;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;


import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gareth on 14/05/2017.
 * Where the launcher goes to
 * Allows user to log in
 */

public class LoginActivity extends AppCompatActivity {
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

        getSupportActionBar().setTitle(R.string.welcome);

        login = getSharedPreferences("login",MODE_PRIVATE);

        feed();

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

        if(savedInstanceState != null) {
            UsernameView.setText(savedInstanceState.getString("username"));
            PasswordView.setText(savedInstanceState.getString("password"));
        }

        Button SignInButton = (Button) findViewById(R.id.sign_in_button);
        SignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        LoginFormView = findViewById(R.id.login_form);
        ProgressView = findViewById(R.id.login_progress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.signup,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_up:
                Intent goSignup = new Intent(LoginActivity.this,WebActivity.class);
                goSignup.putExtra(WebActivity.ARG_URL,getApplication().getString(R.string.web)+"signup");
                this.startActivity(goSignup);
                return true;

            case R.id.action_guide:
                Intent goGuide = new Intent(LoginActivity.this,WebActivity.class);
                goGuide.putExtra(WebActivity.ARG_URL,getApplication().getString(R.string.web)+"guide");
                this.startActivity(goGuide);
                return true;

            default:
                break;
        }

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("username",UsernameView.getText().toString());
        savedInstanceState.putString("password",PasswordView.getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }

    private void feed() {
        if(memestagram.loggedIn(getApplicationContext())) {
            Intent goFeed = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(goFeed);
            finish();
        }
    }


    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        UsernameView.setError(null);
        PasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String username = UsernameView.getText().toString();
        final String password = PasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            PasswordView.setError(getString(R.string.error_field_required));
            focusView = PasswordView;
            cancel = true;
        }

        // Check for a username isn't blank
        if (TextUtils.isEmpty(username)) {
            UsernameView.setError(getString(R.string.error_field_required));
            focusView = UsernameView;
            cancel = true;
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cancel)
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        else // check for internet connectivity
            if(cm.getActiveNetworkInfo() == null
                    || !cm.getActiveNetworkInfo().isAvailable()
                    || !cm.getActiveNetworkInfo().isConnected())
                Toast.makeText(getApplicationContext(), getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
        else {
            // Show a progress spinner and perform the user login attempt.
            showProgress(true);

            String url = getString(R.string.api) + "login";

            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonRes = new JSONObject(response);
                                Boolean success = jsonRes.getBoolean("success");
                                if(success) {
                                    String key = jsonRes.getString("key");
                                    Integer id = jsonRes.getInt("user");

                                    // store all of the data in a shared preferences file
                                    SharedPreferences.Editor loginEditor = login.edit();

                                    loginEditor.clear();

                                    loginEditor.putString("username",username);
                                    loginEditor.putString("password",password);
                                    loginEditor.putString("key",key);
                                    loginEditor.putLong("iduser",id);

                                    loginEditor.commit();

                                    feed();
                                    showProgress(false);
                                }
                                else
                                    Toast.makeText(getApplicationContext(), jsonRes.getString("error"), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                System.out.println(response);
                                Toast.makeText(getApplicationContext(), getString(R.string.error_internal), Toast.LENGTH_SHORT).show();
                            }
                            showProgress(false);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), getString(R.string.error_internal), Toast.LENGTH_SHORT).show();
                            showProgress(false);
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<>();
                    // the POST parameters:
                    params.put("username", username);
                    params.put("password", password);
                    return params;
                }
            };
            Volley.newRequestQueue(this).add(postRequest);
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
}