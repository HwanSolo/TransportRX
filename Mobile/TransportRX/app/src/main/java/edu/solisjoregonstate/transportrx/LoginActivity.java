package edu.solisjoregonstate.transportrx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

import android.util.Log;
import android.app.ProgressDialog;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


//credit: http://sourcey.com/beautiful-android-login-and-signup-screens-with-material-design/

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    public  int authentication_result = 0;

    @InjectView(R.id.input_username)
    EditText _usernameText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_login)
    Button _loginButton;
    @InjectView(R.id.link_signup)
    TextView _signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        validateIsLoggedIn();

        setContentView(R.layout.activity_login_activity);

        ButterKnife.inject(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });

    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        final String username = _usernameText.getText().toString();
        final String password = _passwordText.getText().toString();


        LoginRequest(username, password);



        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        if (authentication_result == 1) {
                            onLoginSuccess();
                        } else {
                            onLoginFailed();
                        }

                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);

                String username = mSettings.getString("user_name", "missing");
                String password = mSettings.getString("password", "missing");

                if (username.equals("missing") || password.equals("missing"))
                {
//                    Toast.makeText(getBaseContext(), "Login Failed(registy)!", Toast.LENGTH_LONG).show();
                }
                else
                {
                    LoginRequest(username, password);

                    Intent intent = new Intent(this, MyDashboardActivity.class);
                    startActivity(intent);

                    this.finish();
                }

                //Toast.makeText(getBaseContext(), "Registration successful!", Toast.LENGTH_LONG).show();

                // By default we just finish the Activity and log them in automatically
                //this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);

        Intent intent = new Intent(this, MyDashboardActivity.class);
        startActivity(intent);

        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed: Username or password is incorrect.", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        if (username.isEmpty()) {
            _usernameText.setError("Enter a username");
            valid = false;
        } else {
            _usernameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("Must be between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public void LoginRequest(final String username, final String password)
    {
        String url = "https://transportrx-jsolis.appspot.com/login";

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        responseDisplay.setText("Response => " + response);
//                        Toast.makeText(getBaseContext(), "Response => " + response, Toast.LENGTH_LONG).show();


                        if (response != null)
                        {
                            try
                            {

                                JSONObject jsonObject = new JSONObject(response);

                                final String message = jsonObject.getString("message");
                                final String session_id = jsonObject.getString("session_id");
                                final String user_name = jsonObject.getString("username");
                                final String role = jsonObject.getString("role");
                                final String transporter_id = jsonObject.getString("transporter_id");

                                if (message.equals("Login was successful!"))
                                {
                                    authentication_result = 1;
                                    SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);

                                    SharedPreferences.Editor editor = mSettings.edit();

                                    editor.putString("user_name", user_name);
                                    editor.putString("session_id", session_id);
                                    editor.putString("role", role);
                                    editor.putString("transporter_id", transporter_id);
                                    editor.apply();
                                }

                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        Log.e(TAG, response);


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("user_name", username);
                params.put("password",password);
                return params;
            }
        };
        queue.add(postRequest);

    }

    public void validateIsLoggedIn()
    {
        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);


        final String username = mSettings.getString("user_name", "missing");
        final String session_id = mSettings.getString("session_id", "missing");
        final String role = mSettings.getString("role", "missing");


        if (username.equals("missing") || session_id.equals("missing") || role.equals("missing"))
        {
//            Toast.makeText(getBaseContext(), "Validation Failed!", Toast.LENGTH_LONG).show();
        }
        else
        {
            String url = "https://transportrx-jsolis.appspot.com/validate";

            RequestQueue queue = Volley.newRequestQueue(this);

            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

//                        Toast.makeText(getBaseContext(), "Response => " + response, Toast.LENGTH_LONG).show();


                            if (response != null)
                            {
                                try
                                {

                                    JSONObject jsonObject = new JSONObject(response);

                                    final String message = jsonObject.getString("message");
                                    final String session_id = jsonObject.getString("session_id");
                                    final String user_name = jsonObject.getString("username");
                                    final String role = jsonObject.getString("role");

                                    if (message.equals("Authentication successful!"))
                                    {
                                        Intent intent = new Intent(LoginActivity.this, MyDashboardActivity.class);
                                        startActivity(intent);
                                    }

                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            Log.e(TAG, response);


                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<>();
                    // the POST parameters:
                    params.put("user_name", username);
                    params.put("session_id",session_id);
                    return params;
                }
            };
            queue.add(postRequest);
        }
    }

}
