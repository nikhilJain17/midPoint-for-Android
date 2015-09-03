package com.example.nikhil.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nikhil on 9/3/15.
 */

public class PlacesListViewAdapter extends ArrayAdapter<String> {

    public PlacesListViewAdapter(Context context, ArrayList<String> values) {

        super(context, R.layout.listview_places, values);

    } // end of constructor



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // inflator to inflate the layout into the view
        LayoutInflater myInflator = LayoutInflater.from(getContext());

        // TODO Recycle view cheese for smooth scrolling
        View view = myInflator.inflate(R.layout.listview_places, parent, false);


        // Construct a row individually
        String place = getItem(position);

        TextView placeTextView = (TextView) view.findViewById(R.id.placesTextView);
        placeTextView.setText(place);


        // return the view so it can be displayed
        return view;

    } // end of getView




}
