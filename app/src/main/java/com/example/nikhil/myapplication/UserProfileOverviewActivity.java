package com.example.nikhil.myapplication;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class UserProfileOverviewActivity extends ActionBarActivity {


    Socket mSocket;

    ListView messagesListView;
    ArrayList<String> messages;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_overview);


        messagesListView = (ListView) findViewById(R.id.messagesListView);

        messages = new ArrayList<String>();


        try {
            extractMessages();
            displayMessages();
        }
        catch (Exception e) {
            // URISyntaxException
            e.printStackTrace();
        }

    }



    // access the messages
    private void extractMessages() throws Exception {

        // connect to the server
        mSocket = IO.socket("http://mytest-darthbatman.rhcloud.com");
        mSocket.connect();

        // emit the event that requests messages
        // TODO Store the username to replace the hardcoded string.
        mSocket.emit("check messages", "darthbatman");

        mSocket.on("new messages", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                JSONArray arr;
                try {
                    arr = (JSONArray) args[0];

                    // log the messages
                    for (int i = 0; i < args.length; i++) {

                        String o = arr.getString(i);

                        Log.d("New messages", o);
                        messages.add(o);

                    } // end of for

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }




            }
        }); // end of listener


    }



    // display the messages on the listview
    private void displayMessages() {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, messages);

        messagesListView.setAdapter(adapter);

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
