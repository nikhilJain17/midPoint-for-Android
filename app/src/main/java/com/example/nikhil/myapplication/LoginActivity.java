package com.example.nikhil.myapplication;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Intent;
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


        usernameET = (EditText) findViewById(R.id.usernameET);
        passwordET = (EditText) findViewById(R.id.passwordET);


//        Button mLoginButton = (Button) findViewById(R.id.loginButton);
//        mLoginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                // TODO Actually do stuff
//                // TODO This is just a dummy login thing
//                Intent intent = new Intent(getApplicationContext(), AddLocations.class);
//                startActivity(intent);
//
//            }
//        });


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


        // listen for a failure
        mSocket.on("login fail", new Emitter.Listener() {
            @Override
            public void call(Object... args) {


                // TODO Add a display or something..a.wertaw/e we wj


                Log.d("LOGIN", "Failure");

            }
        });

    } // end of onLoginButtonClick()




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
