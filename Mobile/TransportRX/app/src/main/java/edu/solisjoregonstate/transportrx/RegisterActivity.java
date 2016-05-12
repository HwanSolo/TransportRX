package edu.solisjoregonstate.transportrx;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    @InjectView(R.id.input_name) EditText _nameText;
    @InjectView(R.id.input_username) EditText _usernameText;
    @InjectView(R.id.input_password)
    EditText _passwordText;
    @InjectView(R.id.btn_signup)
    Button _signupButton;
    @InjectView(R.id.link_login)
    TextView _loginLink;
    private CompoundButton transporter_check;
    public  int authentication_result = 0;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.inject(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });

    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        final String name = _nameText.getText().toString();
        final String username = _usernameText.getText().toString();
        final String password = _passwordText.getText().toString();
        transporter_check = (CompoundButton) findViewById(R.id.transporter_box);
        final String role;

        if (transporter_check.isChecked())
        {
            role = "Transporter";
        }
        else
        {
            role = "Staff";
        }

        String url = "https://transportrx-jsolis.appspot.com/register";

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

                                String message = jsonObject.getString("message");


                                if (message.equals("Registration was successful!"))
                                {
                                    authentication_result = 1;

                                    SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this);

                                    SharedPreferences.Editor editor = mSettings.edit();

                                    editor.putString("user_name", username);
                                    editor.putString("password", password);
                                    editor.apply();

                                }

                                if (message.equals("That username is already in use!"))
                                {
                                    authentication_result = 3;
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
                params.put("name", name);
                params.put("user_name", username);
                params.put("password",password);
                params.put("role",role);
                return params;
            }
        };
        queue.add(postRequest);


        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        if(authentication_result == 1)
                        {
                            onSignupSuccess();
                        }
                        else
                        {
                            onSignupFailed();
                        }

                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {

        Toast.makeText(getBaseContext(), "Username already taken!", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (username.isEmpty()) {
            _usernameText.setError("Enter a username");
            valid = false;
        } else {
            _usernameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

}
