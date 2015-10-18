package com.example.nikhil.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

//import io.socket.*;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class LoginActivity extends ActionBarActivity {

    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;
    final String FILENAME = "aguero";


    Socket mSocket;

    String enteredUsername;
    String enteredPassword;

    EditText usernameET;
    EditText passwordET;

    final String serverDomain = "http://mytest-darthbatman.rhcloud.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // style the action bar
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setBackgroundDrawable(new ColorDrawable(0xff536DFE));
//        mActionBar.setDisplayShowTitleEnabled(false);
//        mActionBar.setDisplayShowTitleEnabled(true);

        sharedPrefs = getSharedPreferences(FILENAME, 0);
        editor = sharedPrefs.edit();

        usernameET = (EditText) findViewById(R.id.usernameET);
        passwordET = (EditText) findViewById(R.id.passwordET);


        // Button listener for signup
        Button signupButton = (Button) findViewById(R.id.createNewAccountButton);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });



        // Connect to server

//
        try {
//            mSocket = IO.socket("http://mytest-darthbatman.rhcloud.com");
////            mSocket = IO.socket("http://chat.socket.io");
//            mSocket.connect();

            connectToServer();
//
//            // emit test event TODO get rid of this
//            mSocket.emit("attempted login", "darthbatman", "password44");
//
//            // // STOPSHIP: 9/17/15
//            // TODO Finalize login process
//            // TODO THIS IS JUST A TEST
//            // add a lsitener to check if cheese went through
//            mSocket.on("login success", new Emitter.Listener() {
//                @Override
//                public void call(Object... args) {
//
//                    try {
//                        JSONArray obj = (JSONArray) args[0];
//                        Log.d("yarfyl", obj.get(0).toString());
//                        Log.d("yarfyl", obj.get(1).toString());
//                    }
//                    catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            });

        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }



    } // end of onCreate();


    /**
     * Connects to Socket.io server
     * **/
    private void connectToServer() throws URISyntaxException {

        mSocket = IO.socket(serverDomain);
        mSocket.connect();

    } // end of connectToServer();


    /**
     * Button listener for Login button
     * **/
    public void onLoginButtonClick(View v) {

        // emit the "attempted login" event
        enteredUsername = usernameET.getText().toString();
        enteredPassword = passwordET.getText().toString();

        mSocket.emit("attempted login", enteredUsername, enteredPassword);


        // listen for a success
        mSocket.on("login success", new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                    Log.d("LOGIN", "Success");

                    // store the username and password into a sharedpreferences for later usage
                    editor.putString("username", enteredUsername);
                    editor.putString("password", enteredPassword);
                    editor.commit();

                    try {
                        JSONArray obj = (JSONArray) args[0];

                        // Log all the messages
                        for (int i = 0; i < obj.length(); i++) {
                            Log.d("USER MESSAGES", obj.get(i).toString());
                        }

                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }


                    // launch the next activity
                    Intent intent = new Intent(getApplicationContext(), AddLocations.class);
                    startActivity(intent);

                }
            });


        new Thread(new Runnable() {
            @Override
            public void run() {


                // listen for a failure
                mSocket.on("login fail", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Invalid Login", Toast.LENGTH_SHORT).show();
                    }
                });




                        Log.d("LOGIN", "Failure");

                    }
                }); // end of mSocket.on("login fail")

            } // end of run

        }).start(); // end of anonymous thread class



    } // end of onLoginButtonClick()


    class Login extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Looper.prepare();
            Toast.makeText(getApplicationContext(), "Invalid Login", Toast.LENGTH_SHORT).show();
            return null;

        }

    } // end of DisplayErr


//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_login, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
