package com.example.myapplication;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by aigerimzhalgasbekova on 29/05/2017.
 */

public class DiscoveryFragment extends Fragment {

    //TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        //Inflate the layout for this fragment
        //return inflater.inflate(R.layout.discovery_fragment, container, false);
        View rootView = inflater.inflate(
                R.layout.discovery_fragment, container, false);
        TextView textView = (TextView)
                rootView.findViewById(R.id.disc);
        textView.setText(MainActivity.btDeviceList.toString());
        return rootView;

    }
}
