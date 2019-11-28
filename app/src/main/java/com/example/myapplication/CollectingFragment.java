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

public class CollectingFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        //return inflater.inflate(R.layout.collecting_fragment, container, false);


        View rootView = inflater.inflate(
                R.layout.collecting_fragment, container, false);
        TextView textView = (TextView)
                rootView.findViewById(R.id.res);
        textView.setText(MainActivity.senCoord.toString());
        textView.append("\n"+MainActivity.senCoord);
        textView.append("\n"+MainActivity.senCoord);
        return rootView;
    }
}

