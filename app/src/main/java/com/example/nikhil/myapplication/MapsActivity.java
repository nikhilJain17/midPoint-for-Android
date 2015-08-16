package com.example.nikhil.myapplication;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Set;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    double midPointLat;
    double midPointLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();


    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        /// Get the bundle

        Bundle bundle = getIntent().getBundleExtra("bundle");
        double[] doble = bundle.getDoubleArray("positions");

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
                // I want to get lat-long pairs


                // Longitudes
                if (i % 2 == 1) {

                    Log.d("Longitude: ", Double.toString(doble[i]));

                    // plot the position
                    plotter = new LatLng(doble[i - 1], doble[i]);
                    mMap.addMarker(new MarkerOptions().position(plotter));

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

            LatLng midPoint = new LatLng(midPointLat, midPointLong);
            mMap.addMarker(new MarkerOptions().position(midPoint).draggable(false).title("midPoint"));

            Log.d("midPoint Lat: ", Double.toString(midPointLat));
            Log.d("midPoint Long; ", Double.toString(midPointLong));

            // Connect to the Google Places API and get back some JSON data
            PlacesApiTask task = new PlacesApiTask();
            task.execute();

        }
    }


    /****
     *
     * Asynchronously fetches data from Google Places API
     *
     */

    public class PlacesApiTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            // Connect to the API and consume it (viciously)
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String unparsedJSON = null;


            try {

                // Construct the URL for the Google Places Search API
                String baseURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=";
                String location = Double.toString(midPointLat) + "," + Double.toString(midPointLong);
                String KEY = "&radius=100&key=AIzaSyCjINkJY8LZrDwYtERoTfg0ZIESm63GPR8";

                String URLstring = baseURL + location + KEY;

                URL url = new URL(URLstring);

                Log.d("MapsActivity", URLstring);

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

           return null;

        }
    }


































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

    }
}
