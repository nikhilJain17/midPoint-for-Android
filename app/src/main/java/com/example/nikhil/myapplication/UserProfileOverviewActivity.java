package com.example.nikhil.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class UserProfileOverviewActivity extends ActionBarActivity {

    // all hail
    Socket mSocket;

    // messages
    ListView messagesListView;
    ArrayList<String> messages;
    ArrayAdapter<String> adapter;

    // friends
    ListView friendsListView;
    ArrayList<String> friends;
    ArrayAdapter<String> friendsAdapter;

    // add friends
    Button addFriendsButton;
    EditText addFriendsET;


    // sharedpreferences stuff
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;
    final String FILENAME = "aguero";

    String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_overview);

        // add friends
        addFriendsButton = (Button) findViewById(R.id.addFriendButton);
        addFriendsET = (EditText) findViewById(R.id.addFriendET);


        // sharedprefs
        sharedPrefs = getSharedPreferences(FILENAME, 0);
        username = sharedPrefs.getString("username", "ERROR");

        // store the arraylist in the sharedpreferences so that u can delete safely


        // messages
        messagesListView = (ListView) findViewById(R.id.messagesListView);
        messages = new ArrayList<String>();

        // friends
        friendsListView = (ListView) findViewById(R.id.friendsListView);
        friends = new ArrayList<String>();


        // friend requests
        setupAcceptRequests();


        // connect to the server
        try {
            mSocket = IO.socket("http://mytest-darthbatman.rhcloud.com");
            mSocket.connect();
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }

        try {
            extractMessages();
            displayMessages();

            extractFriends();
            displayFriends();
        }
        catch (Exception e) {
            // URISyntaxException
            e.printStackTrace();
        }

    }


    // click handler for the view friends button
    public void onFriendButtonClick(View view) {

        Intent intent = new Intent(this, FriendsActivity.class);
        startActivity(intent);

    }



    // click handler for add friend button
    public void onAddFriendButtonClick(View view) {

        // lol display a success message before you do anything
        Toast.makeText(this, "Friend request sent!", Toast.LENGTH_SHORT).show();

        String name = addFriendsET.getText().toString();

        // emit the event
        mSocket.emit("add friend", username, name);

        // listen for success
        // TODO rip

    }


    // set ontouch listener for accepting requests on the listview
    private void setupAcceptRequests() {

        // set the ontouch listener
        messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                final String theirUsername = messages.get(position);

                // show an alertdialog
                AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileOverviewActivity.this, AlertDialog.THEME_HOLO_DARK);

                builder.setTitle("Accept friend request?");

                // yes, they accept
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // emit the socket.io event
                        mSocket.emit("accept friend request", username, theirUsername);

                        // delete the request from the arraylist and the set
                        messages.remove(position);

                        // update the display
                        adapter.notifyDataSetChanged();

                    } // end of onPositiveButtonCLick

                }); // end of setPositive Button



                // no, they dont accept
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // delete the request from the arraylist
                        messages.remove(position);

                        // update the display
                        adapter.notifyDataSetChanged();

                    }
                }); // end of setNegativeButton

                builder.create();
                builder.show();


            } // end of OnItemClick()



        }); // end of OnItemCLickLIstener;


        // SHOW THE DIALOG

    }





    // access the messages
    private void extractMessages() throws Exception {

        // emit the event that requests messages
        mSocket.emit("check messages", username);

        mSocket.on("new messages", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                JSONArray arr;
                try {
                    arr = (JSONArray) args[0];

                    // log the messages
                    for (int i = 0; i < arr.length(); i++) {

                        String o = arr.getString(i);

                        Log.d("New messages", o);
                        messages.add(o);

                    } // end of for

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }); // end of listener


    }



    // display the messages on the listview
    private void displayMessages() {

       adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, messages);

        messagesListView.setAdapter(adapter);

    }



    // access the friend data
    private void extractFriends() throws Exception {

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

                        Log.d("friends", o);
                        friends.add(o);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } // end of call
        }); // end of friend listener

    } // end of extract friends


    // display the friends
    private void displayFriends() {
        friendsAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, friends);

        friendsListView.setAdapter(friendsAdapter);
    }









    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_profile_overview, menu);
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
