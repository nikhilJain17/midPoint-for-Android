package com.example.nikhil.myapplication;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class FriendsActivity extends ActionBarActivity {

    Socket mSocket;

    // friends
    ListView friendsListView;
    ArrayList<String> friends;
    ArrayAdapter<String> adapter;

    // sharedpreferences stuff
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;
    final String FILENAME = "aguero";

    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        // sharedprefs
        sharedPrefs = getSharedPreferences(FILENAME, 0);
        username = sharedPrefs.getString("username", "ERROR");

        // listview stuff
        friendsListView = (ListView) findViewById(R.id.friendLV);

        // CONNECT TO SERVER
        try {
            mSocket = IO.socket("http://mytest-darthbatman.rhcloud.com");
            mSocket.connect();

            extractFriends();
            displayFriends();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

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

                        Log.d("FRIENDS", o);
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
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, friends);

        friendsListView.setAdapter(adapter);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends, menu);
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
