package com.example.nikhil.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;

    // these are the averages of all the points the user entered
    double midPointLat;
    double midPointLong;
    LatLng midPoint;

    // these are the places of interests and its coordinates
    ArrayList<Double> poiLatArray;
    ArrayList<Double> poiLongArray;

    int numOfUserLocations; // how many places the user entered (addresses)

    // place id to be used in DetailsActivity
    // specifically in the Google Places Detail Api
    ArrayList<String> placeIdArray;
    String placeId;

    // type of place user wants to visit
    String typeOfPlace;

    // This will be passed through the bundle to the DetailsActivity to display said details actively
    String rootJsonStr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        placeIdArray = new ArrayList<>();

        poiLatArray = new ArrayList<>();
        poiLongArray = new ArrayList<>();

        // set up google places api connection
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        /// Get the bundle

        Bundle bundle = getIntent().getBundleExtra("bundle");

        // stuff stored in the bundle
        double[] doble = bundle.getDoubleArray("positions");
        ArrayList<String> namesList = bundle.getStringArrayList("place_names");
        typeOfPlace = bundle.getString("type");

        numOfUserLocations = namesList.size();


        int namesListIndex = 0; // for naming the points plotted on the map

        // check that names were transfered properly
        for (String s : namesList)
            Log.d("Name in namesList", s);


        // Prevents ArrayOutOfBoundsException, stops crashing if user doesn't enter a location and hits submit
        if (doble.length > 0) {

            // to plot the midPoint, these variables will hold the averages
            // initialize them with the first lat and lng
            midPointLat = doble[0];
            midPointLong = doble[1];

            // Plot the bundles

            LatLng plotter;

            for (int i = 0; i < doble.length; i++) {

                Log.d("Doubles passed:", Double.toString(doble[i]));

                // Since we already initialized them to tbe the first values in the array, u need to subtract them to get rid of duplicates
                if (i == 0) {
                    midPointLat -= midPointLat;
                } else if (i == 1) {
                    midPointLong -= midPointLong;
                }


                // all even indexes are latitude
                // all odd indexes are longitude
                // want to get lat-long pairs


                // Longitudes
                if (i % 2 == 1) {

                    Log.d("Longitude: ", Double.toString(doble[i]));

                    // plot the position
                    plotter = new LatLng(doble[i - 1], doble[i]);

                    if (namesListIndex < namesList.size()) {
                        String title = namesList.get(namesListIndex);
                        Log.d("Title for point: ", title);

                        // add marker for places mallu entered
                        mMap.addMarker(new MarkerOptions().position(plotter).title(title).icon(BitmapDescriptorFactory.fromResource(R.mipmap.home_variant)));

                        namesListIndex++;

                    }


                    midPointLong += doble[i];

                }


                // Latitudes
                else if (i % 2 == 0) {

                    midPointLat += doble[i];


                }


            }

            // at last, the tru midpoints
            midPointLat = midPointLat / (doble.length / 2);
            midPointLong = midPointLong / (doble.length / 2);


            // TODO Plot the midpoint and maybe the locations around it too, in a circle

            // draw the midpoint
            midPoint = new LatLng(midPointLat, midPointLong);
            mMap.addMarker(new MarkerOptions().position(midPoint).draggable(false).title("midPoint").icon(BitmapDescriptorFactory.fromResource(R.mipmap.radio_tower))); // draw a radio tower for the midpoint

            // TODO Change the icon of the midpoint pls, make a custom one

            Log.d("midPoint Lat: ", Double.toString(midPointLat));
            Log.d("midPoint Long; ", Double.toString(midPointLong));



            // Center the camera on the midpoint (why not let it fly)
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(midPoint, 14, 0, 0)));


            // Connect to the Google Places API and get back the place_ids for each place
            // this also plots the surrounding pois, not just one
            PlacesApiTask task = new PlacesApiTask();
            task.execute();



        }


    }

    // get user location
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


    /**
     * If the user presses the midpoint, then they can view more detailed info on that place*/
    @Override
    public boolean onMarkerClick(Marker marker) {

        // display the name of the place
        marker.showInfoWindow();

        // 0 to M are all the id's of the user's location inputs, where M is the last input
        // M + 1 is the id of the midpoint
        // M + 2 to P are the ids of the places
        // that is what we want

        // marker ids are m0, m1, m8... etc
        String markerId = marker.getId();
        markerId = markerId.replace("m", ""); // get rid of the m
        int markerNum = Integer.parseInt(markerId);

        // indeed, the thing the user clicked on is in fact a place of interest
        // TODO numOfUserLocations + 1????
        if (markerNum > numOfUserLocations + 1) {

            // launch detailsactivity with the appropriate place_id from the arraylist
            // like bruh
            Intent intent = new Intent(this, DetailsActivity.class);

            Bundle datum = new Bundle();
            String placeIdToSend = placeIdArray.get(markerNum - numOfUserLocations - 1);
            Log.d("Place_ID sending: ", placeIdToSend);
            datum.putString("place_id", placeIdToSend);

            intent.putExtra("datum", datum);

            startActivity(intent);

        }

//
//        // verify that the user clicked the midpoint
//        if (marker.getTitle().equals("midPoint")) {
//
//            // TODO Check if placepicker has been updated with specific places
//            // TODO cause that thing is awesome
////            launchPlacePicker();
//
////            // create an intent with the intent of starting the detailsactivity
////
//            Intent intent = new Intent(this, DetailsActivity.class);
//
//            Bundle datum = new Bundle();
//            datum.putString("place_id", placeId);
//            intent.putExtra("datum", datum);
//
//            startActivity(intent);
//        }

        return true;
    }

    private void launchPlacePicker() {
        // set up place picker
        int PLACE_PICKER_REQUEST = 1;
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        // TODO: Get radius that user wants to search
        // For now, just search within 10 latitude units
        LatLng southwestBoundary = new LatLng(midPoint.latitude - 0.3, midPoint.longitude - 0.3);
        LatLng northeastBoundary = new LatLng(midPoint.latitude + 0.3, midPoint.longitude + 0.3);

        builder.setLatLngBounds(new LatLngBounds(southwestBoundary, northeastBoundary));

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // place picking like a boss
        if (requestCode == 1) {

            if (resultCode == RESULT_OK) {


                Bundle placeData = new Bundle();
                Place place = PlacePicker.getPlace(data, this);

                // gather some data about the place

                String name = (String) place.getName();
                String address = (String) place.getAddress();
                String phoneNumber = (String) place.getPhoneNumber();
                int priceLevel = place.getPriceLevel();
                float rating = place.getRating();
//                String website = place.getWebsiteUri().toString();


                // store it in a bundle

                placeData.putString("name", name);
                placeData.putString("address", address);
                placeData.putString("phoneNumber", phoneNumber);
                placeData.putInt("priceLevel", priceLevel);
                placeData.putFloat("rating", rating);
//                placeData.putString("website", website);

                // start the DetailsActivity that displays this info

                Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                intent.putExtra("placeData", placeData);
                startActivity(intent);

            } // end of is result code ok
        }


    } // end of func

    public class PlacesApiTask extends AsyncTask<Void, Void, Void> {


        String unparsedJSON = null;

        @Override
        protected Void doInBackground(Void... params) {

            // Connect to the API and consume it (viciously)
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            try {


                // Construct the URL for the Google Places Search API
                String baseURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=";
                String location = Double.toString(midPointLat) + "," + Double.toString(midPointLong);
                String types = "&types=" + typeOfPlace;
                // TODO Add a slider to allow the user to choose the radius they want to search in
                String KEY = "&rankby=distance&key=AIzaSyDYQAZn43BK_TUtIy1OhDn95Vb4R2OFmVg";

                String URLstring = baseURL + location + types + KEY;

                URL url = new URL(URLstring);

                Log.d("URL: ", URLstring);

                // open the connection and create the request
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

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

                unparsedJSON = buffer.toString();
                rootJsonStr = unparsedJSON;
                Log.d("Raw JSON", unparsedJSON);



            }

            catch (IOException e) {
                e.printStackTrace();
            }

            finally {

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (final IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            // parse the JSON for the place id
            getPlaceId();

            // get the lats and longs to plot on the map of ALL the places of interest
            getLatLongs();

            return null;

        } // end of doInBackground


        private void getLatLongs() {

            try {
                JSONObject rootJson = new JSONObject(unparsedJSON);
                JSONArray resultsArray = rootJson.getJSONArray("results");

                // iterate through the array
                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject result = resultsArray.getJSONObject(i);
                    JSONObject geometry = result.getJSONObject("geometry");
                    JSONObject location = geometry.getJSONObject("location");

                    final String name = result.getString("name");
                    final double lat = location.getDouble("lat");
                    final double lng = location.getDouble("lng");

                    // store the coordinates BAAACK TO BAACK
                    poiLatArray.add(i, lat);
                    poiLatArray.add(i, lng);


                    // plot that cheese on the maaaap
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {


                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(name)).showInfoWindow();

                        } // end of run
                    }); // end of runOnUiThread




                } // end of for loop in results array



            } // end of try
            catch (Exception e) {
                e.printStackTrace();
            }

        } // end of getLatLongs()

        // Check if over query limit for web service
        private void overQueryLimit() {

            try {
                JSONObject rootJson = new JSONObject(unparsedJSON);
                String err = rootJson.getString("status");
                Log.d("Status: ", err);

                if (err.equals("OVER_QUERY_LIMIT")) {
                    // DISPLAY ERROR MESSAGE ON SCREEN in new thread


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MapsActivity.this, "Over API query limit! Error 100", Toast.LENGTH_SHORT).show();
                                    TextView view = (TextView) findViewById(R.id.mapInfoTV);
                                    view.setText(view.getText().toString() + "\nOver API query limit! Error 100");
                                }
                            });
                        }
                    }).start(); // end of new Thread

                } // end of if

            }
            catch (Exception e) {
                e.printStackTrace();
            }


        }

        // check if request was invalid
        private void invalidRequest() {

            try {
                JSONObject rootJson = new JSONObject(unparsedJSON);
                String err = rootJson.getString("status");
                Log.d("Status: ", err);

                if (err.equals("INVALID_REQUEST")) {
                    // DISPLAY ERROR MESSAGE ON SCREEN in new thread


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MapsActivity.this, "Invalid request! Try modifying your search", Toast.LENGTH_SHORT).show();
                                    TextView view = (TextView) findViewById(R.id.mapInfoTV);
                                    view.setText(view.getText().toString() + "\nInvalid request! Try modifying your search");
                                }
                            });
                        }
                    }).start(); // end of new Thread

                } // end of if

            }
            catch (Exception e) {
                e.printStackTrace();
            }


        }



        // Check if raw json indicates there are zero results
        private void zeroResults() {

            boolean isZeroResults = false;

            try {
                JSONObject rootJson = new JSONObject(unparsedJSON);
                String err = rootJson.getString("status");
                Log.d("Status: ", err);

                if (err.equals("ZERO_RESULTS")) {
                    // DISPLAY ERROR MESSAGE ON SCREEN in new thread
                    isZeroResults = true;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MapsActivity.this, "Sorry, no results =(", Toast.LENGTH_SHORT).show();
                                    TextView view = (TextView) findViewById(R.id.mapInfoTV);
                                    view.setText(view.getText().toString() + "\nSorry, no results found.");
                                }
                            });
                        }
                    }).start(); // end of new Thread

                } // end of if

            }
            catch (Exception e) {
                e.printStackTrace();
            }

//            return isZeroResults;

        } // end of zeroResults


        private void getPlaceId() {

            // check if there are zero results from the api call and handle it
            zeroResults();

            // check if we are over teh query limit
            overQueryLimit();

            // check if there is an invalid request
            invalidRequest();

            // parse the JSON data and put it in a bundle ready to be passed on to DetailInfoActivity
            // Only get the place_id, the places details will be handled in the DetailsActivity
            try {
                JSONObject rootJson = new JSONObject(unparsedJSON);
                JSONArray resultsArray = rootJson.getJSONArray("results");

                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject result = resultsArray.getJSONObject(i);
                    placeId = result.getString("place_id");
                    Log.d("Place ID: ", placeId);

                    placeIdArray.add(i, placeId); // push to the arraylist
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        } // end of getPlaceId

    } // end of class PlacesApiTask


































    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setOnMarkerClickListener(this);

    }
}
