package edu.solisjoregonstate.transportrx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


/**
 * Created by Jsolis on 11/29/2015.
 *
 * credit: http://haidermushtaq.com/tutorial-for-creating-android-swiping-application-using-tabbed-activity-on-android-studio/
 */
public class ViewPendingFragment extends Fragment {

    ArrayList<String> data = new ArrayList<String>();
    ArrayList<String> keys = new ArrayList<String>();
    ListView listView ;

    public ViewPendingFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ViewPendingFragment newInstance() {
        ViewPendingFragment fragment = new ViewPendingFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.my_view_transports, container, false);



        // Get ListView object from xml
        listView = (ListView) rootView.findViewById(R.id.list);

        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(getContext());


        final String username = mSettings.getString("user_name", "missing");
        final String session_id = mSettings.getString("session_id", "missing");


        if (username.equals("missing") || session_id.equals("missing"))
        {
            Toast.makeText(getContext(), "Validation Failed!", Toast.LENGTH_LONG).show();
        }
        else
        {

            String url = "https://transportrx-jsolis.appspot.com/transport_requests?user_name=" + username + "&session_id="
                    + session_id +"&filter=pending";


            RequestQueue queue = Volley.newRequestQueue(getContext());


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
                                    String origin = jsonObject.get("origin").getAsString();
                                    String destination = jsonObject.get("destination").getAsString();
                                    String status = jsonObject.get("status").getAsString();
                                    String patient_name = jsonObject.get("patient_name").getAsString();
                                    String patient_mrn = jsonObject.get("patient_mrn").getAsString();
                                    String mode = jsonObject.get("mode").getAsString();
                                    String dateTime = jsonObject.get("issued_time").getAsString();


                                    String item = "Patient: " + patient_name + " MRN: " + patient_mrn + "\n"
                                            + "Route: " + origin + "=>" + destination + " Mode: " + mode + "\n"
                                             + "Status: " + status;
                                    data.add(item);
                                    keys.add(jsonObject.get("key").getAsString());


                                }


                                if(data.size() > 0)
                                {
                                     ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
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

                                // Show Alert
//                                Toast.makeText(getContext(),
//                                        "Position :" + itemPosition + " Key:" + keys.get(itemPosition).toString(), Toast.LENGTH_LONG)
//                                        .show();

                                            Intent intent = new Intent(getContext(), ViewSpecific.class);
                                            intent.putExtra("transportID",keys.get(itemPosition).toString());
                                            startActivity(intent);

                                            //finish();



                                        }

                                    });
                                }
                                else
                                {
                                    Toast.makeText(getContext(), "No Data Found", Toast.LENGTH_LONG).show();
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


        return rootView;
    }


}
