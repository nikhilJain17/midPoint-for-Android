package com.example.nikhil.myapplication;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

    Button signupButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        // Initialize GUI component objects
        nameET = (EditText) findViewById(R.id.nameET);
        usernameET = (EditText) findViewById(R.id.usernameET);
        passwordET = (EditText) findViewById(R.id.passwordET);
        passwordConfirmET = (EditText) findViewById(R.id.passwordConfirmET);
        addressET = (EditText) findViewById(R.id.addressET);

        signupButton = (Button) findViewById(R.id.signupButton);


        signupButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                try {
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
// TODO Fix this address optional stuff

        // confirm the password is the same in both text fields
        if (password.equals(passwordConfirm)) {

            // TODO Make sure that usernames are not repeated!

            // send the goods to the server
            Socket mSocket;
            mSocket = IO.socket("http://mytest-darthbatman.rhcloud.com");
            mSocket.connect();

            // not sending phone numbers
            mSocket.emit("sign up attempt", name, username, password, 666, address);

            // listen for success
            mSocket.on("sign up success", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    // TODO Display a success message
                    Log.d("Create Account", "YES");
                }
            });

        }

    } // end of signUp()



} // end of class