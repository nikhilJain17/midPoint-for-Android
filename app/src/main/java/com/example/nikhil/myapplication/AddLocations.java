package com.example.nikhil.myapplication;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;


public class AddLocations extends ActionBarActivity implements ConnectionCallbacks, OnConnectionFailedListener {




    // References to GUI objects declared in XML
    EditText addLocationET;
    EditText typeET;

    ImageView addToListViewButton;
    Button submitButton;

    ListView locationsListView;

    double currentInputLat; // what the user just entered, but geocoded
    double currentInputLong; // these are passed to through the sharedPrefs


    LatLng currentLocation;


    // Some variables that may be needed in many functions
    String userLocationInput;   // what the user enters in addLocationET
    ArrayList userLocationInputArray = new ArrayList(); // array to hold all String inputs
    String typeOfPlaceInput; // what the user enters in typeET;

    // Bundle to transfer info - screw the SharedPreferences
    Bundle infoTransferBundle;
    ArrayList<Double> infoTransferArray;

    private GoogleApiClient mGoogleApiClient;

    PlacesListViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_locations);

        // connect the Google Places SDK here
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();



        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.setBackgroundDrawable(new ColorDrawable(0xff00838F));

        // Initialize the references to GUI objects
        addLocationET = (EditText) findViewById(R.id.addressEditText);
        typeET = (EditText) findViewById(R.id.typeEditText);
        addToListViewButton = (ImageView) findViewById(R.id.addButton);
        submitButton = (Button) findViewById(R.id.submitButton);
        locationsListView = (ListView) findViewById(R.id.listView);

        userLocationInput = ""; // make sure its not null
//        userLocationInputArray.add(""); // make sure its not null to avoid crashes
        addLocationET.setText(""); // make sure its not null for the same reasons


        infoTransferBundle = new Bundle();
        infoTransferArray = new ArrayList<>();

        adapter = new PlacesListViewAdapter(this, userLocationInputArray);
        locationsListView.setAdapter(adapter);


        // set the onclick listener so that if a user clicks on an item in teh lsitview, they can delete it
        // doot doot
        // thanks mr nikhil
        // np
        locationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(AddLocations.this, AlertDialog.THEME_HOLO_DARK);

                builder.setTitle("Delete?");
                builder.setMessage("Are you sure you want to delete " + userLocationInputArray.get(position) + "?");

                builder.setPositiveButton("Ok", new AlertDialog.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        // Update the data
                        userLocationInputArray.remove(position); // trouble spot?

                        // todo get rid of this debugging stuff
                        Toast.makeText(getApplicationContext(), Integer.toString(position), Toast.LENGTH_SHORT).show();


                        // todo end of debugging stuff

//                        TESTDEBUGgeocode();

                        // update the display
                        adapter.notifyDataSetChanged();


                    } // end of onCLick

                }); // end of setPositive button

                builder.setNegativeButton("No", null);

                builder.show();

            } // end of onItemCLick

        }); // end of setonitemclicklistener



////no longer required
//        // get user's location
//        getUserLocation();
    }



    private LatLng getUserLocation(){

        String towers;
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        towers = lm.getBestProvider(criteria, false);

        Location userLocation = lm.getLastKnownLocation(towers);

        if (userLocation != null) {

            String lat = Double.toString(userLocation.getLatitude());
            String longd = Double.toString(userLocation.getLongitude());

            Log.d("User's Location: ", "Lat: " + lat + " Long: " + longd);

            return new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

        }

        else
            Log.d("User Location", "Failed to get user's location");

        return null;

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }



    // Clears the listview
    public void onClearButtonClick(View view) {

        userLocationInputArray.clear();

        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userLocationInputArray);
        locationsListView.setAdapter(adapter);

    }



    // Button listener for the "addToListViewButton"
    public void onAddButtonClick(View view) {

        // 1. Get the user's input
        userLocationInput = addLocationET.getText().toString();
        userLocationInputArray.add(userLocationInput);

        // Create an ArrayAdapter to convert array into ListView & attach it to the ListView

//  ListAdapter adapter = new ArrayAdapter<String>(this, R.layout.listview_places, userLocationInputArray);

        // updoot
        adapter.notifyDataSetChanged();


        // Prevents crashes (sike nah)
        if (!(userLocationInput.equals("")) || (userLocationInput != null)) {


            // For some reason, the fact that I was geocoding in an AsyncTask made the app crash.
            // I had to move it back onto the main thread (unfortunately).
            // It would have been more efficient running in the background.
            // Maybe later I can get it to stop crashing on the background thread.

            // Access the GeocoderTask to geocode
//             GeocodeTask geocoderTask = new GeocodeTask();
//             geocoderTask.execute();

//            TESTDEBUGgeocode();

        }
        // clear the EditText
        addLocationET.setText("");
    }



    public LatLng TESTDEBUGgeocode (String geocodeThis) {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        if (!(geocodeThis.equals("")) && geocodeThis != null) {

            try {

                try {
                    // List of addresses, we only use 1
                    List<Address> addressList = geocoder.getFromLocationName(geocodeThis, 1);

                    // if u dont not have results
                    if (addressList != null) {

//                        Toast.makeText(getApplicationContext(), "AddressList not null", Toast.LENGTH_LONG).show();

                        // Get the first (and only) address in this list
                        Address address = addressList.get(0);

                        // If you don't not have results (ok)
                        if (address != null) {

                            Log.d("GeocoderTask: ", "Address is not null");

                            // Get the latitude and longitude from the address object
                            double DEBUGextraVariablesSuckLat = address.getLatitude();
                            double DEBUGextraVariablesSuckLong = address.getLongitude();

                            Log.i("Lat/Lng of Geocoded: ", Double.toString(DEBUGextraVariablesSuckLat) + " " + Double.toString(DEBUGextraVariablesSuckLong));

                            return new LatLng(DEBUGextraVariablesSuckLat, DEBUGextraVariablesSuckLong);

//                            // add that cheese to the array
//                            infoTransferArray.add(DEBUGextraVariablesSuckLat);
//                            infoTransferArray.add(DEBUGextraVariablesSuckLong);
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            catch (RuntimeException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "No results found for that location!", Toast.LENGTH_LONG).show();
            }
        }

        return null;
    }



    // Submit button event handler
    public void onSubmitButtonClick(View view) {

        // check if user inputted anything for the type of place they want to visit
        if (typeET.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter a type of place you want to visit!", Toast.LENGTH_SHORT).show();
            return;
        }


        // get the user's input for what type of place they want to visit (restaurant, etc)
        typeOfPlaceInput = typeET.getText().toString();

        ///////////////
        Log.i("UserLocaiotnArr", Integer.toString(userLocationInputArray.size()));

        for (int i = 0; i < userLocationInputArray.size(); i++) {
            Log.i("\n\n\nContent of Useray", (String) userLocationInputArray.get(i));
        }

        ///////////////

        Intent intent = new Intent(this, MapsActivity.class);

        if (infoTransferArray != null) {
            // Turn the ArrayList of coordinates we stored everything in to an Array.
            double[] doubleArray = new double[infoTransferArray.size()];


            for (int i = 0; i < infoTransferArray.size(); i++) {
                doubleArray[i] = infoTransferArray.get(i);
            }

            // testing stuff below
            ArrayList<Double> coordinateArrayList = new ArrayList<Double>();

            for (int i = 0; i < userLocationInputArray.size(); i++) {
                // iterate through all places in userLocationInputArray
                // geocode them
                // store them in a double array
                // pass that cheese

                String place = (String) userLocationInputArray.get(i);

                if (place.equals("Your location")) {
                    // add users current location
                    coordinateArrayList.add(currentLocation.latitude);
                    coordinateArrayList.add(currentLocation.longitude);

                }

                else {
                    // geocode their address bruh
                    LatLng latLng = TESTDEBUGgeocode(place);

                    coordinateArrayList.add(latLng.latitude);
                    coordinateArrayList.add(latLng.longitude);
                }

            }

            // convert to array
            double[] doblear = new double[coordinateArrayList.size()];
            for (int i = 0; i < coordinateArrayList.size(); i++) {
                doblear[i] = coordinateArrayList.get(i);
            }



//
//           // Turn the arraylist of names into an array
//           String[] namesArray = new String[userLocationInputArray.size()];
//
//           for (int i = 0; i < userLocationInputArray.size(); i++) {
//               namesArray[i] = (String) userLocationInputArray.get(i);
//               Log.d("Places Entered: ", namesArray[i]);
//           }


//            infoTransferBundle.putDoubleArray("positions", doubleArray);
            infoTransferBundle.putDoubleArray("positions", doblear);
            infoTransferBundle.putStringArrayList("place_names", userLocationInputArray);
            infoTransferBundle.putString("type", typeOfPlaceInput);

            intent.putExtra("bundle", infoTransferBundle);
        }
        startActivity(intent);

    }



    // Your Location button handler, adds your location to the arraylist
    public void onYourLocationButtonClick(View view) {

        currentLocation = getUserLocation();
        userLocationInputArray.add("Your location");
        adapter.notifyDataSetChanged();

    }


    // Menu item click handler
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // show the mof's profile overview
        if (id == R.id.action_viewprofile) {
            Intent intent = new Intent (this, UserProfileOverviewActivity.class);
            startActivity(intent);
        }

        // log that billa out like a log
        else if (id == R.id.action_logout) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }



    // create the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_locations, menu);
        return true;
    }


    // Connection failed while trying to connect to the Google Places Api
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    // connected to the Google Places Api
    @Override
    public void onConnected(Bundle bundle) {

    }

    // connection suspended to the google places api
    @Override
    public void onConnectionSuspended(int i) {

    }


    // Geocoding AsyncTask
    public class GeocodeTask extends AsyncTask<Void, Void, Void> {

        double extraVariablesSuckLat;
        double extraVariablesSuckLong;

        @Override
        protected Void doInBackground(Void... params) {

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

            if (!(userLocationInput.equals("")) && userLocationInput != null) {

                try {

                    try {
                        // List of addresses, we only use 1
                        List<Address> addressList = geocoder.getFromLocationName(userLocationInput, 1);

                        // if u dont not have results
                        if (addressList != null) {

                            Toast.makeText(getApplicationContext(), "AddressList not null", Toast.LENGTH_LONG).show();

                            // Get the first (and only) address in this list
                            Address address = addressList.get(0);

                            // If you don't not have results (ok)
                            if (address != null) {

                                Log.d("GeocoderTask: ", "Address is not null");

                                // Get the latitude and longitude from the address object
                                extraVariablesSuckLat = address.getLatitude();
                                extraVariablesSuckLong = address.getLongitude();

                                Log.i("Lat/Lng of Geocoded: ", Double.toString(extraVariablesSuckLat) + " " + Double.toString(extraVariablesSuckLong));

                                // add that cheese to the array
                                infoTransferArray.add(extraVariablesSuckLat);
                                infoTransferArray.add(extraVariablesSuckLong);
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                catch (RuntimeException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "No results found for that location!", Toast.LENGTH_LONG).show();
                }
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {

            // I could maybe probably delete this...

            currentInputLat = extraVariablesSuckLat;
            currentInputLong = extraVariablesSuckLong;


        }
    }

}
=======
package com.example.nikhil.myapplication;

import java.util.ArrayList;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import org.apache.http.util.CharArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class AddLocations extends ActionBarActivity implements ConnectionCallbacks, OnConnectionFailedListener {



    // sharedpreferences stuff
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;
    final String FILENAME = "aguero";


    // References to GUI objects declared in XML
    EditText addLocationET;
    EditText typeET;

    ImageView addToListViewButton;
    Button submitButton;

    ListView locationsListView;

    double currentInputLat; // what the user just entered, but geocoded
    double currentInputLong; // these are passed to through the sharedPrefs


    LatLng currentLocation;


    // Some variables that may be needed in many functions
    String userLocationInput;   // what the user enters in addLocationET
    ArrayList userLocationInputArray = new ArrayList<String>(); // array to hold all String inputs

//    Vector<String> userLocationInputArray = new Vector<>();
    String typeOfPlaceInput; // what the user enters in typeET;

    // Bundle to transfer info - screw the SharedPreferences
    Bundle infoTransferBundle;
    ArrayList<Double> infoTransferArray;


    private GoogleApiClient mGoogleApiClient;

    PlacesListViewAdapter adapter;

    // to connect to the server
    Socket mSocket;

    // ALL FRIENDS
    private ArrayList<String> getFriendsArray;

    // friends that you want to get the addresses of, temp shit, reinitialized every time
    Set<String> getAddressOfFriends;

    // same shit for current locations
    ArrayList<LatLng> currentLocationMidpoint = new ArrayList<>();

    // every friend that you added to the thing
    Set<String> allFriendsAdded;
    // num of friends you clicked to add address
    int numOfFriends = 0;

    // handles multiple responses from server
    ArrayList<String> multipleAddressesList;

    boolean isAddresses; // if false, using currentlocations

    // global vars for getting cl's of user
    Set<String> currentLocationSet;
    int currentLocationIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_locations);
//
        getAddressOfFriends = new HashSet<>();
        allFriendsAdded = new HashSet<>();
        currentLocationSet = new HashSet<>();

        currentLocationIndex = 0;

        isAddresses = true;

//        // connect the Google Places SDK here
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)           // to get the user's location
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();



        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2196F3")));
        mActionBar.setTitle("midPoint");

        // Initialize the references to GUI objects
        addLocationET = (EditText) findViewById(R.id.addressEditText);
        typeET = (EditText) findViewById(R.id.typeEditText);
        addToListViewButton = (ImageView) findViewById(R.id.addButton);
        submitButton = (Button) findViewById(R.id.submitButton);
        locationsListView = (ListView) findViewById(R.id.listView);

        userLocationInput = ""; // make sure its not null
//        userLocationInputArray.add(""); // make sure its not null to avoid crashes
        addLocationET.setText(""); // make sure its not null for the same reasons

        // initialize getFriends Array
        getFriendsArray = new ArrayList<>();

        infoTransferBundle = new Bundle();
        infoTransferArray = new ArrayList<>();


        adapter = new PlacesListViewAdapter(this, userLocationInputArray);
        locationsListView.setAdapter(adapter);

        try {
            getFriends();
        }
        catch (Exception e) {

        }

        // set the onclick listener so that if a user clicks on an item in teh lsitview, they can delete it
        // doot doot
        // thanks mr nikhil
        // np
        locationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public synchronized void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(AddLocations.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);

                builder.setTitle("Delete?");
                builder.setMessage("Are you sure you want to delete " + userLocationInputArray.get(position) + "?");

                builder.setPositiveButton("Ok", new AlertDialog.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        // Update the data
                        userLocationInputArray.remove(position); // trouble spot?

//                        currentLocationIndex--;

                        // todo get rid of this debugging stuff
//                        Toast.makeText(getApplicationContext(), Integer.toString(position), Toast.LENGTH_SHORT).show();


                        // todo end of debugging stuff

//                        TESTDEBUGgeocode();

                        // update the display
                        adapter.notifyDataSetChanged();

                    } // end of onCLick

                }); // end of setPositive button

                builder.setNegativeButton("No", null);

                builder.show();

            } // end of onItemCLick

        }); // end of setonitemclicklistener



//
//        // get user's location
//        getUserLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {

            mSocket = IO.socket("http://mytest-darthbatman.rhcloud.com");
            mSocket.connect();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //allclearallclearallclearm[rediacted]c[redacted]a[redacted]
        allFriendsAdded.clear();

        multipleAddressesList = new ArrayList<>();

    }

    // check if location services are enabled
    private static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    // Your Location button handler, adds your location to the arraylist
    public void onYourLocationButtonClick(View view) {

        // check if location services are enabled
        if (isLocationEnabled(getApplicationContext())) {

            // NOTE: the current location was retrieved when the connection to the Google Play services was made

//            currentLocation = getUserLocation();
            userLocationInputArray.add("Your location");
            adapter.notifyDataSetChanged();

        }

        else {
            Toast.makeText(this, "Location services not enabled!", Toast.LENGTH_SHORT).show();
        }

    }

    // obsolete
    private LatLng getUserLocation(){

        String towers;
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        towers = lm.getBestProvider(criteria, false);

        Location userLocation = lm.getLastKnownLocation(towers);

        if (userLocation != null) {

            String lat = Double.toString(userLocation.getLatitude());
            String longd = Double.toString(userLocation.getLongitude());

            Log.d("User's Location: ", "Lat: " + lat + " Long: " + longd);

            return new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

        }

        else {
            Log.d("User Location", "Failed to get user's location");
//            return new LatLng(0, 0);
            return null;
        }

    }

    // Connection failed while trying to connect to the Google Places Api
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {


    }

    // connected to the Google Places Api
    @Override
    public void onConnected(Bundle bundle) {
        // get the god damn location ffs
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            Log.d("CurrentLocation: ", "Null");
        }
        else {
            // set the current location
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            Log.d("CurrentLocation: ", Double.toString(currentLocation.latitude) + Double.toString(currentLocation.longitude));


        }

        updateUserLocationInServer(location);

    }

    // sends user's location to server (do not read)
    private void updateUserLocationInServer(Location location) {

        double latitude = 0;
        double longitude = 0;

        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

//        Toast.makeText(getApplicationContext(), Double.toString(latitude) + " " + Double.toString(longitude), Toast.LENGTH_SHORT).show();

        mSocket.emit("latitude sent", latitude);
        mSocket.emit("longitude sent", longitude);

    }

    // connection suspended to the google places api
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


//
//
//    // task to get current locations from server and display on screen
//    public class MidpointTask extends AsyncTask<JSONArray,Void,Void>   {
//
//        LatLng currentLocation = new LatLng(-2.000000, -2.000000);
//
//        ArrayList<LatLng> latlist = new ArrayList<>();
//
//        int expectedSize = userLocationInputArray.size() + 1;
//
//
//        @Override
//        protected Void doInBackground(JSONArray... params) {
//
//
//            Log.d("SIZE_EXPECTED,", Integer.toString(expectedSize));
//
//            // emit the event
//            mSocket.emit("using current locations", params[0]);
//
//            // listen carefully
//            mSocket.on("midPoint", new Emitter.Listener() {
//                @Override
//                public void call(Object... args) {
//
//                    // this cheeky casting fun is because if location is 0.0, it comes
//                    // in as a forking int, otherwise it comes in as a double
//                    // for additional fun, java has a Double and Integer class that
//                    // are not compatible to be casted between. (shrug emoticon)
//
//                    final double lat = Double.valueOf(args[0].toString());
//                    final double longdick = Double.valueOf(args[1].toString());
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(), Double.toString(lat) + "\n" + Double.toString(longdick),
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//
//                    // set the variable to be
//                    currentLocation = new LatLng(lat, longdick);
//
//                    latlist.add(currentLocation);
//
//                    afterTheFact();
//
//                } // end of call
//
//                // update the gui
//                private void afterTheFact() {
//
//                    synchronized (userLocationInputArray) {
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                // get the most recent addition
//                                LatLng location = latlist.get(latlist.size() - 1);
//
//                                int counter = 0;
//
//                                if (userLocationInputArray.isEmpty()) {
//
//                                    userLocationInputArray.add("Friends Midpoint: " + Double.toString(location.latitude).substring(0) + " "
//                                            + Double.toString(location.longitude).substring(0));
//
//                                    adapter.notifyDataSetChanged();
//
//                                    return;
//                                }
//
//                                while (userLocationInputArray.size() < expectedSize) {
//
//                                    counter++;
//                                    Log.d("SIZE_COUNTER: ", Integer.toString(counter));
//
////             if the default current location of 0,0 DOES NOT come back from server
//                                    if (Double.toString(location.latitude).length() > 6 && Double.toString(location.longitude).length() > 6) {
//
//                                        userLocationInputArray.add("Friends Midpoint: " + Double.toString(location.latitude).substring(0, 6) + " "
//                                                + Double.toString(location.longitude).substring(0, 6));
//
//                                        adapter.notifyDataSetChanged();
//
//                                    }
//                                    // if it DOES (0,0)
//                                    else {
//
//                                        userLocationInputArray.add("Friends Midpoint: " + Double.toString(location.latitude).substring(0) + " "
//                                                + Double.toString(location.longitude).substring(0));
//
//                                        adapter.notifyDataSetChanged();
//
//                                    } // end of else
//
//                                } // end of while
//
//
//                            } // end of run
//                        }); // end of runOnUiThread
//
//                    } // end of synchronized
//
//                } // end of afterTheFact
//
//
//            }); // end of listener
//
//
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//
//
//        }// end of postexecute
//
//    }; // end of MidpointTask
//



    // add some "friends"
    public void onAddFriendButtonClick(View view) {

        // 1. Open Dialog with ListView embedded in it
        // 1.5 display fraaaaaaands fam
        // 2. Get the fraaand the user wants
        // 3. geocode
        // 4. attach to array
        // 5. ???
        // 6. profit

//        final CharSequence[] items = getFriendsArray.toArray();

        // who was clicked???

        // array to display the names
        final CharSequence[] items = new CharSequence[getFriendsArray.size()];

        for (int i = 0; i < items.length; i++)
            items[i] = getFriendsArray.get(i);

        final boolean[] itemSelected = new boolean[getFriendsArray.size()];

        final AlertDialog.Builder bobTheBuilder = new AlertDialog.Builder(AddLocations.this);

        getAddressOfFriends.clear();


//
//        DialogInterface.OnClickListener clicker = new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int which) {
//
//            }
//        };

        final AlertDialog friendsDialog = bobTheBuilder.setTitle("Tap the friends you want to add")
        .setMultiChoiceItems(items, itemSelected, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                // Do not read
                // Store the people clicked in an array
                if (isChecked) {
                    getAddressOfFriends.add(items[which].toString());
                    // add to set to be passed to detailsActivity
                    allFriendsAdded.add(items[which].toString());
                } else {

                    getAddressOfFriends.remove(items[which].toString());
                    allFriendsAdded.remove(items[which].toString());

                }

            }
        })
        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

            // You really do not want to touch this code
            // there was some horseshit where this forking loop would get called more times than needed
            // because of multithreading issues


            @Override
            public void onClick(DialogInterface dialog, int which) {

                Log.i("Get Friends Address", "Confirm button clicked");


                // if user wants addresses
                if (isAddresses) {

                    // send the request to the server to get the firends' addresses on a new thread
                    try {

                        String[] temp = new String[getAddressOfFriends.size()];
                        String killmequickly = ""; // do not read

                        for (int i = 0; i < getAddressOfFriends.size(); i++) {
                            temp[i] = (String) getAddressOfFriends.toArray()[i];
                            killmequickly += temp[i] + "\n";
                        }
                        // If you add 3 friends, make sure you only get 3 results back
                        numOfFriends = 0;

                        for (int i = 0; i < itemSelected.length; i++) {

                            if (itemSelected[i])
                                numOfFriends++;

                        }

                        // do NOT read, patent pending
                        killmequickly = killmequickly.substring(0, killmequickly.length() - 1);
                        // TODO uncomment this later
                        Toast.makeText(getApplicationContext(), killmequickly, Toast.LENGTH_SHORT).show();

                        // send as a JSON Array
                        JSONArray friendsJson = new JSONArray();
                        for (String s : temp)
                            friendsJson.put(s);

                        mSocket.emit("using home address", friendsJson);

                        // listen for a tingly response
                        mSocket.on("geocode addresses", new Emitter.Listener() {

                            @Override
                            public void call(Object... args) {

                                Log.i("Got Friends Addresses", "HALLELUJAH");

                                final JSONArray arr;

                                // extract from server response
                                try {

                                    arr = (JSONArray) args[0];

                                    Log.i("Num of friends added:", Integer.toString(numOfFriends));

                                    // might have to change to c < numOfFriends - 1
                                    for (int c = 0; c < arr.length(); c++) {

                                        if (c < numOfFriends && multipleAddressesList.size() < numOfFriends) {
                                            // store in the list
                                            Log.i("Adding this address: ", arr.getString(c));
                                            multipleAddressesList.add(arr.getString(c));

                                            userLocationInputArray.add(arr.getString(c));

                                            // updates listview
                                            // TODO refactor into private func
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    adapter.notifyDataSetChanged();
                                                    locationsListView.requestLayout();

                                                }
                                            });
                                        }

                                    } // end of for

                                } // end of try
                                catch (Exception e) {
                                    Log.i("Parsing Friends", "ERROR");
                                    e.printStackTrace();
                                }

                            } // end of call

                        }); // end of socket listener


                    } // end of try

                    catch (Exception e) {

                        Log.i("Getting Friends Address", "ERROR");
                        e.printStackTrace();

                    } // end of catch


                    // reset the list after accessing the relevant data
                    // rest in peace memory management
                    multipleAddressesList = new ArrayList<>();

                } // end of ifAddresses

                else {
                    // user wants current locations
                    JSONArray getCLofFriends = new JSONArray();

                    String whyareyouevenreadingthis = "";

                    for (String s : getAddressOfFriends) {
                        getCLofFriends.put(s);
                        whyareyouevenreadingthis += s + "\n";
                    }


//                    MidpointTask mp = new MidpointTask();
//                    mp.execute(getCLofFriends);

                    final JSONArray passJson = getCLofFriends;

                    // emit the event
                    mSocket.emit("using current locations", passJson);

                    // listen carefully
                    mSocket.on("midPoint", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {

                            // this cheeky casting fun is because if location is 0.0, it comes
                            // in as a forking int, otherwise it comes in as a double
                            // for additional fun, java has a Double and Integer class that
                            // are not compatible to be casted between. (shrug emoticon)

                            final double cllat = Double.valueOf(args[0].toString());
                            final double cllong = Double.valueOf(args[1].toString());


                            runOnUiThread(new Runnable() {
                                @Override
                                public synchronized void run() {

//                                    Toast.makeText(getApplicationContext(), Double.toString(cllat) + "/" + Double.toString(cllong),
//                                            Toast.LENGTH_SHORT).show();


                                    // display data
                                    // if it doesn't come back as 0,0
                                    if (Double.toString(cllat).length() > 6 && Double.toString(cllong).length() > 6) {

                                        // Friends Midpoint 12.123 -123.4
                                        // Added Midpoint 3 123.12 -12.12
                                        // (Midpoint #3) 12.123, -123.4

                                        currentLocationSet.add("(Midpoint #" + Integer.toString(currentLocationIndex) + ") " + Double.toString(cllat).substring(0, 6) + ", "
                                                + Double.toString(cllong).substring(0, 6));


                                    } // end of if
                                    // if it DOES (0,0)
                                    else {

                                        currentLocationSet.add("(Midpoint #" + Integer.toString(currentLocationIndex) + ") " + Double.toString(cllat).substring(0) + ", "
                                                + Double.toString(cllong).substring(0));


                                    } // end of else


                                    // update master list
                                    for (String s : currentLocationSet) {

                                        // if it doesnt have it already
                                        if (!userLocationInputArray.contains(s))
                                            userLocationInputArray.add(s);

                                    }

                                    // update gui
                                    adapter.notifyDataSetChanged();
                                    locationsListView.requestLayout();


                                } // end of run()

                            }); // end of runonuithread


                        } // end of call

                    }); // end of listener

                    // proper spot? start at 0? because this is called before msocket.on
                    currentLocationIndex++;
                    // rip in pce memory
                    currentLocationSet = new HashSet<String>();

                } // end of else

            } // end of onCLick
        }).setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                @Override
                public void onClick (DialogInterface dialog,int which){
                    dialog.dismiss();
                }
            }).setNeutralButton("Addresses",null) // end of setNeutralButton
            .create();

            friendsDialog.setOnShowListener(new DialogInterface.OnShowListener()

            {
                @Override
                public void onShow (DialogInterface dialog){

                isAddresses = true;

                final Button type = friendsDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                type.setOnClickListener(new View.OnClickListener() {

//                    isAddresses = true;

                    @Override
                    public void onClick(View view) {

                        isAddresses = !isAddresses;

                        if (!isAddresses) {
                            type.setText("Current Locations");
                            isAddresses = false;
                        } else {
                            type.setText("Addresses");
                            isAddresses = true;
                        }
                        // redraw!
//                        type.requestLayout();
                        type.invalidate();

                    } // end of onClick
                }); // end of setOnClickListener

            }

                ; // end of onShow
            }

            ); // end of setOnShowListener


            friendsDialog.show();

        } // end of addFriendButtonClick()


                // Get user's friends

    private void getFriends() throws Exception {

        String username;


        // sharedprefs
        sharedPrefs = getSharedPreferences(FILENAME, 0);
        username = sharedPrefs.getString("username", "ERROR");


        Socket mSocket = IO.socket("http://mytest-darthbatman.rhcloud.com");
        mSocket.connect();


        // emit the event that requests friends (sounds pretty sad...)
        mSocket.emit("want friends", username);

        // =) tfw u receive friends
        mSocket.on("some friends", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                JSONArray arr; // pirates!
                try {
                    arr = (JSONArray) args[0];

                    // log da friends
                    for (int i = 0; i < arr.length(); i++) {

                        String o = arr.getString(i);

                        Log.d("AddLocations Friends", o);
                        // ama
                        if (!o.contains("would like to be friends"))
                            getFriendsArray.add(o);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } // end of call
        }); // end of friend listener


    } // end of getFriends


    // Clears the listview
    public synchronized void onClearButtonClick(View view) {

        userLocationInputArray.clear();

        currentLocationIndex = 0;

        Log.d("SIZE_ACTUAL_CLRED", Integer.toString(userLocationInputArray.size()));

        // update the display
        adapter.notifyDataSetChanged();
        locationsListView.requestLayout();


//        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userLocationInputArray);
//        locationsListView.setAdapter(adapter);

    }


    // Button listener for the "addToListViewButton"
    public synchronized void onAddButtonClick(View view) {

        // 1. Get the user's input
        userLocationInput = addLocationET.getText().toString();
        userLocationInputArray.add(userLocationInput);

        // Create an ArrayAdapter to convert array into ListView & attach it to the ListView

//  ListAdapter adapter = new ArrayAdapter<String>(this, R.layout.listview_places, userLocationInputArray);

        // updoot
        adapter.notifyDataSetChanged();


        // Prevents crashes (sike nah)
        if (!(userLocationInput.equals("")) || (userLocationInput != null)) {


            // For some reason, the fact that I was geocoding in an AsyncTask made the app crash.
            // I had to move it back onto the main thread (unfortunately).
            // It would have been more efficient running in the background.
            // Maybe later I can get it to stop crashing on the background thread.

            // Access the GeocoderTask to geocode
//             GeocodeTask geocoderTask = new GeocodeTask();
//             geocoderTask.execute();

//            TESTDEBUGgeocode();

        }
        // clear the EditText
        addLocationET.setText("");
    }

    // TODO @codecleanup
    public LatLng TESTDEBUGgeocode(String geocodeThis) {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        if (!(geocodeThis.equals("")) && geocodeThis != null) {

            try {

                try {
                    // List of addresses, we only use 1
                    List<Address> addressList = geocoder.getFromLocationName(geocodeThis, 1);

                    // if u dont not have results
                    if (addressList != null) {

//                        Toast.makeText(getApplicationContext(), "AddressList not null", Toast.LENGTH_LONG).show();

                        // Get the first (and only) address in this list
                        Address address = addressList.get(0);

                        // If you don't not have results (ok)
                        if (address != null) {

                            Log.d("GeocoderTask: ", "Address is not null");

                            // Get the latitude and longitude from the address object
                            double DEBUGextraVariablesSuckLat = address.getLatitude();
                            double DEBUGextraVariablesSuckLong = address.getLongitude();

                            Log.i("Lat/Lng of Geocoded: ", Double.toString(DEBUGextraVariablesSuckLat) + " " + Double.toString(DEBUGextraVariablesSuckLong));

                            return new LatLng(DEBUGextraVariablesSuckLat, DEBUGextraVariablesSuckLong);

//                            // add that cheese to the array
//                            infoTransferArray.add(DEBUGextraVariablesSuckLat);
//                            infoTransferArray.add(DEBUGextraVariablesSuckLong);
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (RuntimeException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "No results found for that location!", Toast.LENGTH_LONG).show();
            }
        }

        return null;
    }


    // Submit button event handler
    public void onSubmitButtonClick(View view) {

        // check if user inputted anything for the type of place they want to visit
        if (typeET.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter a type of place you want to visit!", Toast.LENGTH_SHORT).show();
            return;
        }


        // get the user's input for what type of place they want to visit (restaurant, etc)
        typeOfPlaceInput = typeET.getText().toString();
        // replace all spaces with an underscore
        typeOfPlaceInput = typeOfPlaceInput.replace(' ', '_');
        // make it lowercase
        typeOfPlaceInput = typeOfPlaceInput.toLowerCase();

        ///////////////
        Log.i("UserLocaiotnArr", Integer.toString(userLocationInputArray.size()));

        for (int i = 0; i < userLocationInputArray.size(); i++) {
            Log.i("\n\n\nContent of Useray", (String) userLocationInputArray.get(i));
        }

        ///////////////

        Intent intent = new Intent(this, MapsActivity.class);

        if (infoTransferArray != null) {
            // Turn the ArrayList of coordinates we stored everything in to an Array.
            double[] doubleArray = new double[infoTransferArray.size()];


            for (int i = 0; i < infoTransferArray.size(); i++) {
                doubleArray[i] = infoTransferArray.get(i);
            }

            // testing stuff below
            ArrayList<Double> coordinateArrayList = new ArrayList<Double>();

            for (int i = 0; i < userLocationInputArray.size(); i++) {
                // iterate through all places in userLocationInputArray
                // geocode them
                // store them in a double array
                // pass that cheese

                String place = (String) userLocationInputArray.get(i);

                if (place.equals("Your location")) {

                    // add users current location
                    if (currentLocation != null) {
                        coordinateArrayList.add(currentLocation.latitude);
                        coordinateArrayList.add(currentLocation.longitude);
                    } else {
                        Toast.makeText(this, "Invalid current location", Toast.LENGTH_SHORT).show();
                    }

                } else if (place.contains("Midpoint")) {

                    // (Midpoint #3) 12.123, -123.4
                    // (Midpoint #3) 0.0, 0.0

//                    // TODO make this more robust
//                    // 0.0, 0.0 came back from the server
//                    if (place.length() <= 25 && place.contains("0.0")) {
//                        // throw in the defaults
//                        coordinateArrayList.add(0.0);
//                        coordinateArrayList.add(0.0);
//                    }
                    // default value did NOT come back from server
//                    else {

                        // latitude is from 2 spaces after ) to 1 space before comma
                        String latitude = place.substring(place.indexOf(')') + 2, place.lastIndexOf(',') - 1);

                        // longitude is from 2 spaces after the comma (it goes comma space longitude)
                        String longitude = place.substring(place.lastIndexOf(',') + 2, place.length());

//                        String longitude = place.substring(24, place.length());
//                    Toast.makeText(getApplicationContext(), latitude + "/" + longitude, Toast.LENGTH_SHORT).show();
                        coordinateArrayList.add(Double.parseDouble(latitude));
                        coordinateArrayList.add(Double.parseDouble(longitude));
//                    }
                } else {
                    // geocode their address bruh
                    LatLng latLng = TESTDEBUGgeocode(place);

                    if (latLng != null) {
                        coordinateArrayList.add(latLng.latitude);
                        coordinateArrayList.add(latLng.longitude);
                    } else {
                        Toast.makeText(this, "Invalid current location", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            // convert to array
            double[] doblear = new double[coordinateArrayList.size()];
            for (int i = 0; i < coordinateArrayList.size(); i++) {
                doblear[i] = coordinateArrayList.get(i);
            }


//
//           // Turn the arraylist of names into an array
//           String[] namesArray = new String[userLocationInputArray.size()];
//
//           for (int i = 0; i < userLocationInputArray.size(); i++) {
//               namesArray[i] = (String) userLocationInputArray.get(i);
//               Log.d("Places Entered: ", namesArray[i]);
//           }


//            infoTransferBundle.putDoubleArray("positions", doubleArray);
            infoTransferBundle.putDoubleArray("positions", doblear);
            infoTransferBundle.putStringArrayList("place_names", userLocationInputArray);
            infoTransferBundle.putString("type", typeOfPlaceInput);

            // put friends that will be transferred -> mapsactivity -> detailsactivity
            String[] temp = new String[allFriendsAdded.size()];

            for (int i = 0; i < allFriendsAdded.size(); i++)
                temp[i] = (String) allFriendsAdded.toArray()[i];

            intent.putExtra("friends", temp);
            intent.putExtra("bundle", infoTransferBundle);
        }
        startActivity(intent);

    }


    // Menu item click handler
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // show the mof's profile overview
        if (id == R.id.action_viewprofile) {
            Intent intent = new Intent(this, UserProfileOverviewActivity.class);
            startActivity(intent);
        }

        // log it out like a log(arithm)
        else if (id == R.id.action_logout) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        // show a cheeky alertdialog with the supported verbage
        else if (id == R.id.action_help) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(AddLocations.this);
            final String[] supportedPlaces = new String[]{
                    "Accounting",
                    "Airport",
                    "Amusement Park",
                    "Aquarium",
                    "Art Gallery",
                    "ATM",
                    "Bakery",
                    "Bank",
                    "Bar",
                    "Beauty Salon",
                    "Bicycle Store",
                    "Book Store",
                    "Bowling Alley",
                    "Bus Station",
                    "Cafe",
                    "Campground",
                    "Car Dealer",
                    "Car Rental",
                    "Car Repair",
                    "Car Wash",
                    "Casino",
                    "Cemetery",
                    "Church",
                    "City Hall",
                    "Clothing Store",
                    "Convenience Store",
                    "Courthouse",
                    "Dentist",
                    "Department Store",
                    "Doctor",
                    "Electrician",
                    "Electronics Store",
                    "Embassy",
                    "Establishment",
                    "Finance",
                    "Fire Station",
                    "Florist",
                    "Food",
                    "Funeral Home",
                    "Furniture Store",
                    "Gas Station",
                    "General Contractor",
                    "Grocery or Supermarket",
                    "Gym",
                    "Hair Care",
                    "Hardware Store",
                    "Health",
                    "Hindu Temple",
                    "Home Goods Store",
                    "Hospital",
                    "Insurance Agency",
                    "Jewelry Store",
                    "Laundry",
                    "Lawyer",
                    "Library",
                    "Liquor Store",
                    "Local Government Office",
                    "Locksmith",
                    "Lodging",
                    "Meal Delivery",
                    "Meal Takeaway",
                    "Mosque",
                    "Movie Rental",
                    "Movie Theater",
                    "Moving Company",
                    "Museum",
                    "Night Club",
                    "Painter",
                    "Park",
                    "Parking",
                    "Pet Store",
                    "Pharmacy",
                    "Physiotherapist",
                    "Place of Worship",
                    "Plumber",
                    "Police",
                    "Post Office",
                    "Real Estate Agency",
                    "Restaurant",
                    "Roofing Contractor",
                    "RV Park",
                    "School",
                    "Shoe Store",
                    "Shopping Mall",
                    "Spa",
                    "Stadium",
                    "Storage",
                    "Store",
                    "Subway Station",
                    "Synagogue",
                    "Taxi Stand",
                    "Train Station",
                    "Travel Agency",
                    "University",
                    "Veterinary Care",
                    "Zoo"

            };

            ArrayAdapter<String> adaptarball = new ArrayAdapter<String>(getApplicationContext(), R.layout.listtype, supportedPlaces);

            dialog.setAdapter(adaptarball, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    ayy lmao
                    typeET.setText(supportedPlaces[which]);
                }
            });

            dialog.setInverseBackgroundForced(true);

            dialog.setTitle("Types of places supported");

            dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            dialog.show();

        }

        return super.onOptionsItemSelected(item);
    }


    // create the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_locations, menu);
        return true;
    }


    // TODO @codecleanup /delete?
    // Geocoding AsyncTask
    public class GeocodeTask extends AsyncTask<Void, Void, Void> {

        double extraVariablesSuckLat;
        double extraVariablesSuckLong;

        @Override
        protected Void doInBackground(Void... params) {

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

            if (!(userLocationInput.equals("")) && userLocationInput != null) {

                try {

                    try {
                        // List of addresses, we only use 1
                        List<Address> addressList = geocoder.getFromLocationName(userLocationInput, 1);

                        // if u dont not have results
                        if (addressList != null) {

//                            Toast.makeText(getApplicationContext(), "AddressList not null", Toast.LENGTH_LONG).show();

                            // Get the first (and only) address in this list
                            Address address = addressList.get(0);

                            // If you don't not have results (ok)
                            if (address != null) {

                                Log.d("GeocoderTask: ", "Address is not null");

                                // Get the latitude and longitude from the address object
                                extraVariablesSuckLat = address.getLatitude();
                                extraVariablesSuckLong = address.getLongitude();

                                Log.i("Lat/Lng of Geocoded: ", Double.toString(extraVariablesSuckLat) + " " + Double.toString(extraVariablesSuckLong));

                                // add that cheese to the array
                                infoTransferArray.add(extraVariablesSuckLat);
                                infoTransferArray.add(extraVariablesSuckLong);
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (RuntimeException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "No results found for that location!", Toast.LENGTH_LONG).show();
                }
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {

            // I could maybe probably delete this...

            currentInputLat = extraVariablesSuckLat;
            currentInputLong = extraVariablesSuckLong;


        }
    }


} // end of class


/////////////////////////////////////////////////////
///////////////////// kobe //////////////////////////
