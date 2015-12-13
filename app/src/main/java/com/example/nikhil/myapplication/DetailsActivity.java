package com.example.nikhil.myapplication;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class DetailsActivity extends ActionBarActivity {

    // to access the api
    String placeId;

    // the goodies from the api
    String formatted_address;
    String formatted_phone_number;
    String name;
//    String url;
//    int numOfReviews;
    String status;
    String hours;

    // Gui References
    TextView nameTV, addressTV, phoneNumberTV, hoursTV;
    Button reviewButton, textFriendsButton;

    // Since Bundles can only take 1-dimensional arrays, there have to be 3 separate forking arrays
    String[] reviewTextArr;
    String[] reviewAuthorArr;
    String[] reviewRatingArr;

    // the friend names you want to text
    String[] friendNameArray;
    // their phone numbers from the server
    ArrayList<String> friendNumberArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // make action bar blue
        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.setBackgroundDrawable(new ColorDrawable(0xff536DFE));


        // get the name of the friends
        friendNameArray = getIntent().getStringArrayExtra("friends");
        friendNumberArray = new ArrayList<>();

        if (friendNameArray != null) {
//            Toast.makeText(this, "Not Null" + friendNameArray[0], Toast.LENGTH_SHORT).show();

            // turn into json array to pass to server
            try {

                JSONArray friendNameJson = new JSONArray(friendNameArray);

                // get phone numbers and store in friendNumberArray
                getPhoneNumbers(friendNameJson);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } // end of if



        // get the gui elements
        nameTV = (TextView) findViewById(R.id.nameTV);
        addressTV = (TextView) findViewById(R.id.addressTV);
        phoneNumberTV = (TextView) findViewById(R.id.phoneNumberTV);
        hoursTV = (TextView) findViewById(R.id.hoursTV);

        reviewButton = (Button) findViewById(R.id.reviewsButton);
        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle data = new Bundle();


                data.putStringArray("author_reviews", reviewAuthorArr);
                data.putStringArray("text_reviews", reviewTextArr);
                data.putStringArray("rating_reviews", reviewRatingArr);

                Intent intent = new Intent(getApplicationContext(), ReviewsActivity.class);

                // testing passing it directly
                intent.putExtra("text_reviews", reviewTextArr);
                intent.putExtra("rating_reviews", reviewRatingArr);
                intent.putExtra("author_reviews", reviewAuthorArr);
                intent.putExtra("data",data);

                // TEST DO NOT READ
                intent.putExtra("test", "test");

                startActivity(intent);

            }
        });

        textFriendsButton = (Button) findViewById(R.id.textFriendsButton);
        textFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              // if there are numbers to send to
                if (friendNumberArray != null && friendNameArray.length != 0) {
                    // fetch the Sms Manager
                    SmsManager sms = SmsManager.getDefault();

                    // the message
                    String message = "Want to hang out at " + name + "?";

                    // the phone numbers we want to send to

                    for (String number : friendNumberArray) {
                        sms.sendTextMessage(number, null, message, null, null);
                    }

                    String namesSentTo;
                    Toast.makeText(getApplicationContext(), "Sent texts!", Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(getApplicationContext(), "No friends added to text!", Toast.LENGTH_SHORT).show();
                } // end of else

            } // end of onClick

        });

        // Display the ad via AdMob
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);


        Bundle bundle = getIntent().getBundleExtra("datum");
        placeId = bundle.getString("place_id");

        // Consume the Places Details API
        DetailsApiTask apiTask = new DetailsApiTask();
        apiTask.execute();


    } // end of onCreate


    // get phone numbers from server of friends
    private void getPhoneNumbers(JSONArray friends) {

        try {
            Socket mSocket = IO.socket("http://mytest-darthbatman.rhcloud.com");
            mSocket.connect();
//
//            JSONArray test = new JSONArray();
//            test.put("darthbatman");
//            String[] test = new String[1];
//            test[0] = "Nikhil Jain";

            mSocket.emit("friends numbers needed", friends);

            mSocket.on("friends numbers", new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                    Log.i("Got Friends Numbers", "HALLELUJAH");

                    final JSONArray arr;

                    try {

                        arr = (JSONArray) args[0];

                        for (int i = 0; i < arr.length(); i++) {

                            Log.i("Friend Number:", arr.getString(i));
                            friendNumberArray.add(arr.getString(i));

                        } // end of for

                    }
                    catch (Exception e) {
                        Log.i("Parsing Friends", "ERROR");
                        e.printStackTrace();
                    } // end of try catch

                } // end of call

            }); // end of mSocket.on


        } // end of try
        catch (Exception e) {
            e.printStackTrace();
        } // end of catch

    }


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
                String key = "&key=AIzaSyDYQAZn43BK_TUtIy1OhDn95Vb4R2OFmVg";

                Log.d("DetailsActivity",baseUrl+key);

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


        private void parseJson() throws JSONException {

            hours = "";

            JSONObject rootJson = new JSONObject(rawJson);
            JSONObject result = rootJson.getJSONObject("result");
            formatted_address = result.getString("formatted_address");
            formatted_phone_number = result.getString("formatted_phone_number");
            name = result.getString("name");
            status = rootJson.getString("status");

            JSONArray openHours = result.getJSONObject("opening_hours").getJSONArray("weekday_text");

            for (int i = 0; i < openHours.length(); i++) {
                hours += openHours.getString(i);
                hours += "\n"; // NEWLINE NEWLINE NEWLINE
            }
            // delete the last \n
            int last = hours.lastIndexOf('\n');
            char lastChar = hours.charAt(last);
            lastChar = ' ';

            JSONArray reviews = result.getJSONArray("reviews");

            // if there is stuff inside it, then get stuff
            if (reviews.length() > 0) {

                // Holds author name, text of review, and rating out of 5 (e.g. 4/5, dick/5, etc)
                reviewAuthorArr = new String[reviews.length()];
                reviewRatingArr = new String[reviews.length()];
                reviewTextArr = new String[reviews.length()];

                Log.i("Get Reviews", "Getting Reviews");

                for (int i = 0; i < reviews.length(); i++) {

                    JSONObject review = reviews.getJSONObject(i);

                    String rating = review.getString("rating");
                    String text = review.getString("text");
                    String author_name = review.getString("author_name");


                    reviewRatingArr[i] = rating;
                    reviewTextArr[i] = text;
                    reviewAuthorArr[i] = author_name;

                } // end of for

            } // end of if
            else {
                // make sure the arrays are not null
                reviewAuthorArr = new String[1];
                reviewTextArr = new String[1];
                reviewRatingArr = new String[1];

                reviewAuthorArr[0] = "No reviews";
                reviewRatingArr[0] = "No reviews";
                reviewTextArr[0] = "No reviews";


                Log.i("Get Reviews", "NO Reviews!");

            }






        } // end of parseJson


        // Display the API data on the screen
        @Override
        protected void onPostExecute(Void aVoid) {

            nameTV.setText(name);
            addressTV.setText(formatted_address);
            phoneNumberTV.setText(formatted_phone_number);
            hoursTV.setText(hours);


//            if (openNow.contains("False"))
//                openNowTV.setText("Not Currently Open");
//
//            else if (openNow.contains("True"))
//                openNowTV.setText("Currently Open");

        }
    } // end of DetailsApiTask


} // end of DetailsActivity
