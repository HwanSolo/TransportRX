package edu.solisjoregonstate.transportrx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ViewSpecific extends AppCompatActivity {

    private static final int RC_BARCODE_CAPTURE = 9001;

    private String TAG = "ViewSpecificActivity";
    String patient_name;
    String origin;
    String destination;
    String status;
    String creator;
    String cancel_reason;
    String issued_time;
    String patient_mrn;
    String delay_reason;
    String mode;
    String key;
    String transporter;
    String dateTime;
    String new_status;
    String verify_patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_specific);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        // getIntent() is a method from the started activity
        Intent myIntent = getIntent(); // gets the previously created intent
        final String transportID = myIntent.getStringExtra("transportID");

        getTransportRequest(transportID);

        final Button assign_button = (Button) findViewById(R.id.assign_button);
        assign_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                assignTransporter(transportID);
                ViewSpecific.this.recreate();
            }
        });

        final Button status_button = (Button) findViewById(R.id.update_button);
        status_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click

                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(ViewSpecific.this, status_button);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
//                        Toast.makeText(ViewSpecific.this,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();


                        if (item.getTitle().equals("Delay")) {
                            new_status = "delayed";

                        }

                        if (item.getTitle().equals("Complete")) {
                            new_status = "completed";
                        }

                        if (item.getTitle().equals("Cancel")) {
                            new_status = "canceled";
                        }

                        if (item.getTitle().equals("In Progress")) {
                            new_status = "in progress";
                        }

                        if (item.getTitle().equals("Delete")) {
                            deleteRequest(transportID);
                            finish();
                        }

                        ChangeTransportStatus(new_status, transportID);

                        ViewSpecific.this.recreate();


                        return true;
                    }
                });

                popup.show();//showing popup menu
            }

        });

        final Button edit_button = (Button) findViewById(R.id.edit_button);
        edit_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent intent = new Intent(ViewSpecific.this, CreateRequest.class);
                intent.putExtra("edit","true");
                intent.putExtra("transportID",transportID);
                intent.putExtra("patient_name",patient_name);
                intent.putExtra("patient_mrn",patient_mrn);
                intent.putExtra("origin",origin);
                intent.putExtra("destination", destination);
                intent.putExtra("mode", mode);
                startActivity(intent);

                //ViewSpecific.this.recreate();
                finish();



            }
        });



        final Button verify_button = (Button) findViewById(R.id.verify_button);
        verify_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click

                // launch barcode activity.
                Intent intent = new Intent(ViewSpecific.this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);

                startActivityForResult(intent, RC_BARCODE_CAPTURE);



            }
        });

    }



    public void getTransportRequest(String transportID)
    {
        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(ViewSpecific.this);

        final String username = mSettings.getString("user_name", "missing");
        final String session_id = mSettings.getString("session_id", "missing");

        if (username.equals("missing") || session_id.equals("missing"))
        {
            Toast.makeText(ViewSpecific.this, "Validation Failed!", Toast.LENGTH_LONG).show();
        }
        else
        {
            String url = "https://transportrx-jsolis.appspot.com/transport_requests/" + transportID + "?user_name=" + username + "&session_id="
                    + session_id;

            RequestQueue queue = Volley.newRequestQueue(ViewSpecific.this);

            // prepare the Request
            StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // display response
                            Log.e("Response", response);
//                            Toast.makeText(getContext(), "Response => " + response, Toast.LENGTH_LONG).show();

                            JsonParser parser = new JsonParser();
                            JsonObject jsonObject = parser.parse(response).getAsJsonObject();

                            origin = jsonObject.get("origin").getAsString();
                            destination = jsonObject.get("destination").getAsString();
                            status = jsonObject.get("status").getAsString();
                            patient_name = jsonObject.get("patient_name").getAsString();
                            patient_mrn = jsonObject.get("patient_mrn").getAsString();
                            mode = jsonObject.get("mode").getAsString();
                            dateTime = jsonObject.get("issued_time").getAsString();
                            creator = jsonObject.get("creator").getAsString();
                            JsonArray transporter_array = jsonObject.getAsJsonArray("transporter");
                            for (int i=0;i<transporter_array.size();i++)
                            {
                                if(i < 1)
                                {
                                    transporter = transporter_array.get(i).getAsString();
                                }
                                else if(i < transporter_array.size() && transporter_array.size() > 1 && i > 0)
                                {
                                    transporter = transporter + ", " + transporter_array.get(i).getAsString();
                                }
                            }

                            if(transporter == null)
                            {
                                transporter = "None";
                            }

                            TextView display_name = (TextView)findViewById(R.id.patient_textview);
                            display_name.setText("Patient: " + patient_name);

                            TextView display_mrn  = (TextView)findViewById(R.id.mrn_textview);
                            display_mrn.setText("Patient MRN: " + patient_mrn);

                            TextView display_origin = (TextView)findViewById(R.id.origin_textview);
                            display_origin.setText("Origin: " + origin);

                            TextView display_destination = (TextView)findViewById(R.id.destination_textview);
                            display_destination.setText("Destination: " + destination);

                            TextView display_mode = (TextView)findViewById(R.id.mode_textview);
                            display_mode.setText("Mode: " + mode);

                            TextView display_status = (TextView)findViewById(R.id.status_textview);
                            display_status.setText("Status: " + status);

                            TextView display_dateTime = (TextView)findViewById(R.id.date_textview);
                            display_dateTime.setText("Issued: " + dateTime);

                            TextView display_creator = (TextView)findViewById(R.id.creator_textview);
                            display_creator.setText("Requested by: " + creator);

                            TextView display_transporters = (TextView)findViewById(R.id.transporters_textview);
                            display_transporters.setText("Transporters: " + transporter);

                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //error
                            error.printStackTrace();
                        }
                    }
            );
            // add it to the RequestQueue
            queue.add(getRequest);
        }
    }

    public void assignTransporter(String transportID) {

        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(ViewSpecific.this);

        final String username = mSettings.getString("user_name", "missing");
        final String session_id = mSettings.getString("session_id", "missing");
        final String role = mSettings.getString("role", "missing");
        final String transporter_id = mSettings.getString("transporter_id", "missing");

        if (username.equals("missing") || session_id.equals("missing"))
        {
            Toast.makeText(getBaseContext(), "Validation Failed!", Toast.LENGTH_LONG).show();
        }
        else
        {
            String url = "https://transportrx-jsolis.appspot.com/transport_request_assign/" + transportID;

            RequestQueue queue = Volley.newRequestQueue(this);

            StringRequest putRequest = new StringRequest(Request.Method.PUT, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {

//                           Toast.makeText(getBaseContext(), "Response => " + response, Toast.LENGTH_LONG).show();

                            if (response != null)
                            {
                                try
                                {
                                    JSONObject jsonObject = new JSONObject(response);

                                    final String message = jsonObject.getString("message");

                                    if (message.equals("Transport request assignment was successful!"))
                                    {
                                        Toast.makeText(getBaseContext(), "Transport assignment success!", Toast.LENGTH_LONG).show();
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
                    Map<String, String>  params = new HashMap<String, String>();
                    // the POST parameters:
                    params.put("user_name", username);
                    params.put("session_id",session_id);
                    return params;
                }
            };
            queue.add(putRequest);
        }
    }

    public void ChangeTransportStatus(final String newStatus, String transportID)
    {
        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(ViewSpecific.this);

        final String username = mSettings.getString("user_name", "missing");
        final String session_id = mSettings.getString("session_id", "missing");
        final String role = mSettings.getString("role", "missing");
        final String transporter_id = mSettings.getString("transporter_id", "missing");

        if (username.equals("missing") || session_id.equals("missing"))
        {
            Toast.makeText(getBaseContext(), "Validation Failed!", Toast.LENGTH_LONG).show();
        }
        else
        {
            String url = "https://transportrx-jsolis.appspot.com/transport_request_update/" + transportID;

            RequestQueue queue = Volley.newRequestQueue(this);

            StringRequest putRequest = new StringRequest(Request.Method.PUT, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {

//                           Toast.makeText(getBaseContext(), "Response => " + response, Toast.LENGTH_LONG).show();

                            if (response != null)
                            {
                                try
                                {
                                    JSONObject jsonObject = new JSONObject(response);

                                    final String message = jsonObject.getString("message");

                                    if (message.equals("Update was successful!"))
                                    {
                                        Toast.makeText(getBaseContext(), "Update was successful!", Toast.LENGTH_LONG).show();
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
                    Map<String, String>  params = new HashMap<String, String>();
                    // the POST parameters:
                    params.put("user_name", username);
                    params.put("session_id",session_id);
                    params.put("status",newStatus);
                    return params;
                }
            };
            queue.add(putRequest);
        }
    }


    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    //statusMessage.setText(R.string.barcode_success);
                    //barcodeValue.setText(barcode.displayValue);
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);

                    verify_patient = barcode.displayValue;

                    if(verify_patient.equals(patient_mrn))
                    {
                        Toast.makeText(ViewSpecific.this, "Verification Passed!!" + verify_patient, Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(ViewSpecific.this, "Verification Failed! Check Patient Information!" + verify_patient, Toast.LENGTH_LONG).show();
                    }

                } else {
                    //statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                //statusMessage.setText(String.format(getString(R.string.barcode_error),
                        //CommonStatusCodes.getStatusCodeString(resultCode)));
                Log.d(TAG,CommonStatusCodes.getStatusCodeString(resultCode));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    //THIS IS ONLY FOR ASSIGNMENT REQUIREMENTS! REMOVE THIS METHOD LATER
    public void deleteRequest(String transportID)
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://transportrx-jsolis.appspot.com/transport_requests/" + transportID;

        StringRequest deleteRequest = new StringRequest(Request.Method.DELETE, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error.
                        error.printStackTrace();
                    }
                }
        );
        queue.add(deleteRequest);
    }

}
