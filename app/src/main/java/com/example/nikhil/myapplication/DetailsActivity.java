package com.example.nikhil.myapplication;

import android.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DetailsActivity extends ActionBarActivity {

    // to access the api
    String placeId;

    // the goodies from the api
    String formatted_address;
    String formatted_phone_number;
    String name;
    String url;
    int numOfReviews;
    String status;

    // Gui References
    TextView nameTV, addressTV, phoneNumberTV;
    ListView reviewListView;

    ArrayAdapter<String> mAdapter;
    ArrayList<String> reviewTextArray; // to be shown on a dialog fragment
    ArrayList<String> ratingsArray; // to be displayed


    /*
    TODO IMPLEMENT AN EXPANDABLE LISTVIEW TO DISPLAY REVIEWS!!!!!!!!
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // make action bar blue
        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.setBackgroundDrawable(new ColorDrawable(0xff536DFE));


        // get the gui elements
        nameTV = (TextView) findViewById(R.id.nameTV);
        addressTV = (TextView) findViewById(R.id.addressTV);
        phoneNumberTV = (TextView) findViewById(R.id.phoneNumberTV);

        // Display the ad via AdMob
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);


        Bundle bundle = getIntent().getBundleExtra("datum");
        placeId = bundle.getString("place_id");

        // Consume the Places Details API
        DetailsApiTask apiTask = new DetailsApiTask();
        apiTask.execute();


        // set up list view components
        reviewListView = (ListView) findViewById(R.id.reviewListView);
        reviewTextArray = new ArrayList<>();



    } // end of onCreate


    // does exactly what it sounds like it does



    // TODO implement later if placepicker gets updated with specific types of places
    private void getPlacePickerBundleData() {

        // Get the bundle data
        Bundle data = getIntent().getBundleExtra("placeData");
        String name = data.getString("name");
        String address = data.getString("address");
        String phoneNumber = data.getString("phoneNumber");
        int priceLevel = data.getInt("priceLevel");
        float rating = data.getFloat("rating");
//        String website = data.getString("website");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_back) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    // Asynchronously access the Google Places Details API
    public class DetailsApiTask extends AsyncTask<Void, Void, Void> {


        String rawJson = null;

        @Override
        protected Void doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            try {
                // construct the url
                String baseUrl = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeId;
                String key = "&key=ENZ_ECET11";
                URL url = new URL(baseUrl + key);

                // connect to the api
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // read the output from the server
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0)
                    return null;

                rawJson = buffer.toString();
                Log.d("Details URL", baseUrl + key);
                Log.d("Details JSON", rawJson);

                // parse JSON
                try {
                    parseJson();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }//

            } // end of try
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {

                if (urlConnection != null)
                    urlConnection.disconnect();

                if (reader != null) {

                    try {
                        reader.close();
                    }// end of try
                    catch (final IOException e) {
                        e.printStackTrace();
                    } // end of catch

                } // end of if reader

            } // end of finally

            return null;

        } // end of doInBackground


        private void parseJson() throws JSONException{

            JSONObject rootJson = new JSONObject(rawJson);
            JSONObject result = rootJson.getJSONObject("result");
            formatted_address = result.getString("formatted_address");
            formatted_phone_number = result.getString("formatted_phone_number");
            name = result.getString("name");
            status = rootJson.getString("status");

            JSONArray reviews = rootJson.getJSONArray("reviews");

            // if there is stuff inside it, then get stuff
            if (reviews.length() > 0) {

                for (int i = 0; i < reviews.length(); i++) {

                    JSONObject review = reviews.getJSONObject(i);

                    int rating = review.getInt("rating");
                    String text = review.getString("text");
                    String author_name = review.getString("author_name");

                    ratingsArray.add(Integer.toString(rating));
                    reviewTextArray.add(text + " ~" + author_name);

                } // end of for

            } // end of if

        } // end of parseJson


        // Display the API data on the screen
        @Override
        protected void onPostExecute(Void aVoid) {

            nameTV.setText(name);
            addressTV.setText(formatted_address);
            phoneNumberTV.setText(formatted_phone_number);

        }
    } // end of DetailsApiTask


} // end of DetailsActivity
