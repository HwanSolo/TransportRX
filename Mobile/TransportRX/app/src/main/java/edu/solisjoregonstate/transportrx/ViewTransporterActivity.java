package edu.solisjoregonstate.transportrx;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

public class ViewTransporterActivity extends AppCompatActivity {

    ArrayList<String> data = new ArrayList<String>();
    ArrayList<String> phoneNumbers = new ArrayList<String>();
    ListView listView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transporter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        GetTransporters();

    }

    public void GetTransporters()
    {

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list);

        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(ViewTransporterActivity.this);


        final String username = mSettings.getString("user_name", "missing");
        final String session_id = mSettings.getString("session_id", "missing");


        if (username.equals("missing") || session_id.equals("missing"))
        {
            Toast.makeText(ViewTransporterActivity.this, "Validation Failed!", Toast.LENGTH_LONG).show();
        }
        else
        {

            String url = "https://transportrx-jsolis.appspot.com/transporter?user_name=" + username + "&session_id="
                    + session_id;


            RequestQueue queue = Volley.newRequestQueue(ViewTransporterActivity.this);


            // prepare the Request
            StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // display response
                            Log.e("Response", response);
//                            Toast.makeText(getContext(), "Response => " + response, Toast.LENGTH_LONG).show();

                            try
                            {
                                data.clear();

                                JsonArray jArray = new JsonParser().parse(response).getAsJsonArray();
                                for (int i=0;i<jArray.size();i++) {
                                    JsonObject jsonObject = jArray.get(i).getAsJsonObject();
                                    String status = jsonObject.get("status").getAsString();
                                    String name = jsonObject.get("name").getAsString();
                                    String user_name = jsonObject.get("user_name").getAsString();
                                    String phone = jsonObject.get("phone").toString();

                                    String item = "Name: " + name + " Username: " + user_name + "\n"
                                            + "Status: " + status ;
                                    data.add(item);

                                    if(phone == null)
                                    {
                                        phoneNumbers.add("no number");
                                    }
                                    else
                                    {
                                        phoneNumbers.add(jsonObject.get("phone").toString());
                                    }

                                }

                                if(data.size() > 0)
                                {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(ViewTransporterActivity.this,
                                            android.R.layout.simple_list_item_1, data);

                                    listView.setAdapter(adapter);

                                    // here you can also define your custom adapter and set it to listView
                                    //according to your own defined layout as items
                                    // ListView Item Click Listener
                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view,
                                                                int position, long id) {

                                            // ListView Clicked item index
                                            int itemPosition = position;

                                            // ListView Clicked item value
                                            String itemValue = (String) listView.getItemAtPosition(position);

                                            if(phoneNumbers.get(itemPosition).toString().equals("no number"))
                                            {
                                                Toast.makeText(ViewTransporterActivity.this, "No number on record", Toast.LENGTH_LONG).show();

                                            } else
                                            {

                                                Intent callIntent = new Intent(Intent.ACTION_CALL);
                                                callIntent.setData(Uri.parse("tel:" + phoneNumbers.get(itemPosition).toString()));
                                                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(callIntent);

                                            }

                                        }

                                    });
                                }
                                else
                                {
                                    Toast.makeText(ViewTransporterActivity.this, "No Data Found", Toast.LENGTH_LONG).show();
                                }

                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                            }

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

}
