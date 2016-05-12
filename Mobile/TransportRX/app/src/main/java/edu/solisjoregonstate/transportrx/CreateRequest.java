package edu.solisjoregonstate.transportrx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateRequest extends AppCompatActivity {

    private static final String TAG = "CreateRequestActivity";

    private EditText nameEditText;
    private EditText mrnEditText;
    private EditText originEditText;
    private EditText destinationEditText;
    private EditText modeEditText;

    String patient_name;
    String patient_mrn;
    String origin;
    String destination;
    String mode;

    String edit;
    String transportID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_request);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        nameEditText = (EditText) findViewById(R.id.editPatientName);
        mrnEditText = (EditText) findViewById(R.id.editPatientMrn);
        originEditText = (EditText) findViewById(R.id.editOrigin);
        destinationEditText = (EditText) findViewById(R.id.editDestination);
        modeEditText = (EditText) findViewById(R.id.editMode);

        // getIntent() is a method from the started activity
        Intent myIntent = getIntent(); // gets the previously created intent
        edit = myIntent.getStringExtra("edit");

        if(edit.equals("true"))
        {
            transportID = myIntent.getStringExtra("transportID");

            //Autofill in all fields with current info

            nameEditText.setText(myIntent.getStringExtra("patient_name"));
            mrnEditText.setText(myIntent.getStringExtra("patient_mrn"));
            originEditText.setText(myIntent.getStringExtra("origin"));
            destinationEditText.setText(myIntent.getStringExtra("destination"));
            modeEditText.setText(myIntent.getStringExtra("mode"));

        }

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                boolean validInput = true;

                patient_name = nameEditText.getText().toString();
                if (!isValidLetters(patient_name)) {
                    nameEditText.setError("Invalid entry!");
                    validInput = false;
                }

                patient_mrn = mrnEditText.getText().toString();
                if (isEmpty(mrnEditText)) {
                    mrnEditText.setError("Invalid entry!");
                    validInput = false;
                }

                origin = originEditText.getText().toString();
                if (isEmpty(originEditText)) {
                    originEditText.setError("Invalid entry!");
                    validInput = false;
                }

                destination = destinationEditText.getText().toString();
                if (isEmpty(destinationEditText)) {
                    destinationEditText.setError("Invalid entry!");
                    validInput = false;
                }

                mode = modeEditText.getText().toString();
                if (isEmpty(modeEditText)) {
                    modeEditText.setError("Invalid entry!");
                    validInput = false;
                }


                if (validInput) {

                    //try to register new transport job

                    if (newTransportRequest())
                    {

                        if(edit.equals("true"))
                        {
                            Intent intent = new Intent(CreateRequest.this,ViewSpecific.class);
                            intent.putExtra("transportID",transportID);
                            startActivity(intent);
                        }

                        finish();
                    }

                }


            }

        });

    }

    public static boolean isValidLetters(String txt) {

        String regx = "^[a-zA-Z-,]+(\\s{0,1}[a-zA-Z-, ])*$";
        Pattern pattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(txt);
        return matcher.find();

    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }

    public boolean newTransportRequest() {



        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(CreateRequest.this);

        final String username = mSettings.getString("user_name", "missing");
        final String session_id = mSettings.getString("session_id", "missing");

        if (username.equals("missing") || session_id.equals("missing")) {
            Toast.makeText(getBaseContext(), "Could not retrieve from sharedPrefs!!", Toast.LENGTH_LONG).show();
            return false;
        } else {

            final String url;

            if(edit.equals("true"))
            {
                url = "https://transportrx-jsolis.appspot.com/transport_requests/" + transportID;
            }
            else
            {
                url = "https://transportrx-jsolis.appspot.com/transport_requests";
            }



            RequestQueue queue = Volley.newRequestQueue(this);

            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (response != null)
                            {
                                try
                                {

                                    JSONObject jsonObject = new JSONObject(response);

                                    final String message = jsonObject.getString("message");

                                    if (message.equals("Transport request was created!"))
                                    {
                                        Toast.makeText(CreateRequest.this, "New Transport Request Created!", Toast.LENGTH_LONG).show();
                                    }
                                    else
                                    {
                                        Toast.makeText(getBaseContext(), "Response => " + response, Toast.LENGTH_LONG).show();
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
                    params.put("patient_name", patient_name);
                    params.put("patient_mrn", patient_mrn);
                    params.put("origin", origin);
                    params.put("destination", destination);
                    params.put("mode", mode);
                    params.put("user_name", username);
                    params.put("session_id",session_id);

                    return params;
                }
            };
            queue.add(postRequest);

        }
        return true;
    }

}
