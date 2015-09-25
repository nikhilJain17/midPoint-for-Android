package com.example.nikhil.myapplication;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

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

        

    } // end of onCreate()

}
