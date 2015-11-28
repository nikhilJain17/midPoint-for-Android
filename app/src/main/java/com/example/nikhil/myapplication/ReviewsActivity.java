package com.example.nikhil.myapplication;

import android.content.Intent;
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
    HashMap <String, List<String>> reviewListDetail;

    Bundle reviewData; // the raw stuff passed through the intents

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        reviewData = getIntent().getBundleExtra("data");

        reviewListView = (ExpandableListView) findViewById(R.id.reviewListView);

        reviewListDetail = new HashMap();
        // some dummy data
        List<String> first = new ArrayList<>();
        first.add("Hello World");
        first.add("Hello World");
        first.add("Hello World");
        List<String> second = new ArrayList<>();
        second.add("Hello World");
        second.add("Hello World");
        second.add("Hello World");
        reviewListDetail.put("first", first);
        reviewListDetail.put("second", second);

        // extract the actual review data and put it into the reviewListDetail hashmap
//        List<String> ratings = (List) reviewData.get("rating_reviews");
//        List<String> reviewText = (List) reviewData.get("text_reviews");
        String[] ratings = this.getIntent().getStringArrayExtra("rating_reviews");
        String[] reviewText = this.getIntent().getStringArrayExtra("text_reviews");

//        // The size of ratings and reviewText are the same fyi
//        if (ratings != null && reviewText != null) {
            for (int i = 0; i < reviewText.length; i++) {

                // add shit to the thing
                List<String> child = new ArrayList<>();
                child.add(reviewText[i]);
                reviewListDetail.put(ratings[i] + " out of 5", child);

            }
//        }

        // init the titles to be the keys of the hashmap
        reviewListTitle = new ArrayList(reviewListDetail.keySet());

        // init the adapter
        reviewListAdapter = new ReviewsExpandableListAdapter(getApplicationContext(), reviewListTitle, reviewListDetail);

        // attach the adapter
        reviewListView.setAdapter(reviewListAdapter);

        // when you expand a list
        reviewListView.setOnGroupExpandListener(new OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(), reviewListTitle.get(groupPosition) + " List Expanded.", Toast.LENGTH_SHORT).show();
            }
        });

        reviewListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(), reviewListTitle.get(groupPosition) + " List Collapsed.", Toast.LENGTH_SHORT).show();
            }
        });


    } // end of oncreate


} // end of class