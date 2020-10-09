package com.example.nikhil.myapplication;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SignupActivity extends ActionBarActivity {

    EditText nameET;
    EditText usernameET;
    EditText passwordET;
    EditText passwordConfirmET;
    EditText addressET;
    EditText phoneNumberET;

    Button signupButton;

    // testing
    boolean suInUse; // sign up in use


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // style action bar

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2196F3")));

        suInUse = false;

        // Initialize GUI component objects
        nameET = (EditText) findViewById(R.id.nameET);
        usernameET = (EditText) findViewById(R.id.usernameET);
        passwordET = (EditText) findViewById(R.id.passwordET);
        passwordConfirmET = (EditText) findViewById(R.id.passwordConfirmET);
        addressET = (EditText) findViewById(R.id.addressET);
        phoneNumberET = (EditText) findViewById(R.id.phoneNumET);

        signupButton = (Button) findViewById(R.id.signupButton);


        signupButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                try {

//                    if (usernameET.getText().length() < 6)
//                        Toast.makeText(getApplicationContext(), "Username must be longer than 6 letters", Toast.LENGTH_SHORT).show();
//
//
//                    if (addressET.getText().length() == 0)
//                        Toast.makeText(getApplicationContext(), "Invalid address", Toast.LENGTH_SHORT).show();

                    signUp();
                }
                catch (URISyntaxException e) {
                    e.printStackTrace();
                } // end of trycatch

            } // end of onClick


        }); // end of listener





    } // end of onCreate()



    private void signUp() throws URISyntaxException{

        // get the stuff the user entered
        String name = nameET.getText().toString();
        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();
        String passwordConfirm = passwordConfirmET.getText().toString();
        String address = addressET.getText().toString();
        String phoneNum = phoneNumberET.getText().toString();

        // confirm the password is the same in both text fields
        if (password.equals(passwordConfirm) && address.length() > 1 && phoneNum.length() > 9 && username.length() > 1 && name.length() > 1) {

            // send the goods to the server
            Socket mSocket;
            mSocket = IO.socket("http://mytest-darthbatman.rhcloud.com");
            mSocket.connect();

            // not sending phone numbers
            mSocket.emit("sign up attempt", name, username, password, phoneNum, address);

            // listen for success
            mSocket.on("sign up success", new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Created account successfully!", Toast.LENGTH_SHORT).show();
                        } // end of run
                    }); // end of runOnUiThread
                    Log.d("Create Account", "YES");

                }
            });


            // listen for username in use
            mSocket.on("su fail in use", new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                Toast.makeText(getApplicationContext(), "Sorry, username in use", Toast.LENGTH_SHORT).show();
                        }
                    });

                    Log.d("Signup", "signup in use");
                    suInUse = true;

                }
            });


        } // end of if
        else {
            Toast.makeText(this, "Invalid login fields!", Toast.LENGTH_SHORT).show();
        }

    } // end of signUp()



} // end of class
