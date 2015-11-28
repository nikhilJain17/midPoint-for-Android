package com.example.nikhil.myapplication;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.Toast;

public class ReviewsActivity extends ActionBarActivity {

    ExpandableListView reviewListView; // to hold the reviews fam
    ExpandableListAdapter reviewListAdapter;
    List reviewListTitle;
    HashMap reviewListDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);


        reviewListView = (ExpandableListView) findViewById(R.id.reviewListView);

        reviewListDetail = new HashMap();
        // some dummy data
        reviewListDetail.put("first","Hello World!");
        reviewListDetail.put("second","Goodbye World!");

        // init the titles to be the keys of the hashmap
        reviewListTitle = new ArrayList(reviewListDetail.keySet());

        // attach the adapter
        reviewListView.setAdapter(reviewListAdapter);

        // when you expand a list
        reviewListView.setOnGroupExpandListener(new OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(), reviewListTitle.get(groupPosition) + " List Expanded.", Toast.LENGTH_SHORT).show();
            }
        });


        reviewListView


    } // end of oncreate


} // end of class
