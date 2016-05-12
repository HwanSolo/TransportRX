package edu.solisjoregonstate.transportrx;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
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

public class MyDashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MyDashboardActivity";


    private CompoundButton signIn;


    SharedPreferences mSettings;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_dashboard);

        mSettings = PreferenceManager.getDefaultSharedPreferences(MyDashboardActivity.this);

        signIn = (CompoundButton) findViewById(R.id.transporter_status);
        signIn.setChecked(mSettings.getBoolean("signed_in", false));
        signIn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                TelephonyManager tMgr = (TelephonyManager)MyDashboardActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
                String mPhoneNumber = tMgr.getLine1Number();
                //Toast.makeText(getBaseContext(), "Transporter phone => " + mPhoneNumber, Toast.LENGTH_LONG).show();
                setTransporterStatus(signIn.isChecked(), mPhoneNumber);

                SharedPreferences.Editor editor = mSettings.edit();

                editor.putBoolean("signed_in", isChecked);
                editor.apply();
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        View header = navigationView.getHeaderView(0);
        TextView text = (TextView) header.findViewById(R.id.name_textView);
        text.setText(mSettings.getString("user_name", "missing"));

        TextView user = (TextView)findViewById(R.id.username_tv);
        user.setText("Welcome, \n " + mSettings.getString("user_name", "missing") + "!");

        final Button viewTransporters_button = (Button) findViewById(R.id.view_transporters_tv);
        viewTransporters_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent intent = new Intent(MyDashboardActivity.this, ViewTransporterActivity.class);
                startActivity(intent);

            }
        });

        final Button viewRequests_button = (Button) findViewById(R.id.view_requests_tv);
        viewRequests_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent intent = new Intent(MyDashboardActivity.this, ViewRequestsActivity.class);
                startActivity(intent);
            }
        });

        final Button createRequest_button = (Button) findViewById(R.id.create_request_tv);
        createRequest_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent intent = new Intent(MyDashboardActivity.this, CreateRequest.class);
                intent.putExtra("edit","false");
                startActivity(intent);

            }
        });

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
//            super.onBackPressed();
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.logout) {

            //set transporter status to "Inactive"
            setTransporterStatus(false, null);

            SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(MyDashboardActivity.this);

            SharedPreferences.Editor editor = mSettings.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(MyDashboardActivity.this, LoginActivity.class);
            startActivity(intent);

            finish();
//            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();

        if (id == R.id.create_request) {
            // Handle the action
            Intent intent = new Intent(MyDashboardActivity.this,CreateRequest.class);
            intent.putExtra("edit","false");
            startActivity(intent);


        } else if (id == R.id.view_transports) {

            Intent intent = new Intent(MyDashboardActivity.this,ViewRequestsActivity.class);
            startActivity(intent);

        } else if (id == R.id.view_transporters) {

            Intent intent = new Intent(MyDashboardActivity.this,ViewTransporterActivity.class);
            startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setTransporterStatus(boolean signIn, final String phone)
    {
        final String username = mSettings.getString("user_name", "missing");
        final String session_id = mSettings.getString("session_id", "missing");
        final String role = mSettings.getString("role", "missing");
        final String transporter_id = mSettings.getString("transporter_id", "missing");
        final String status;


        if (username.equals("missing") || session_id.equals("missing") || role.equals("missing") || transporter_id.equals("missing"))
        {
            Toast.makeText(getBaseContext(), "Validation Failed!", Toast.LENGTH_LONG).show();
        }
        else
        {
            String url = "https://transportrx-jsolis.appspot.com/transporter_update/" + transporter_id;

            if (signIn)
            {
                status = "Active";
            }
            else{
                status = "Inactive";
            }


            RequestQueue queue = Volley.newRequestQueue(this);

            StringRequest putRequest = new StringRequest(Request.Method.PUT, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {

//                            Toast.makeText(getBaseContext(), "Response => " + response, Toast.LENGTH_LONG).show();


                            if (response != null)
                            {
                                try
                                {

                                    JSONObject jsonObject = new JSONObject(response);

                                    final String message = jsonObject.getString("message");
                                    final String tStatus = jsonObject.getString("status");


                                    if (message.equals("Update was successful!"))
                                    {
//                                        Toast.makeText(getBaseContext(), "Transporter status => " + tStatus, Toast.LENGTH_LONG).show();
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
                    params.put("status", status);
                    params.put("phone", phone);
                    return params;
                }
            };

            queue.add(putRequest);

        }
    }
}
