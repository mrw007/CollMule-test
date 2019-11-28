package com.example.myapplication;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by aigerimzhalgasbekova on 12/02/2017.
 */

public class ContextManagerRepository extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>>{
    private MainActivity mainActivity;
    public ContextManagerRepository (MainActivity activity) {
        mainActivity = activity;
    }
    //public InputStream is;
    public String loadJSONFromAsset() {
        String json = null;
        try {
            //Log.i("InputStream", is.toString());
            InputStream is = MainActivity.context.getAssets().open("ContextManagerRepository.json");
            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

    protected ArrayList<HashMap<String, String>> doInBackground(String... params){
        ArrayList<HashMap<String, String>> formList = new ArrayList<HashMap<String, String>>();
        try
        {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            JSONArray cdm_jArry = obj.getJSONArray("Sensors Context Data");
            //formList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> cdm_sen;


            for (int i = 0; i < cdm_jArry.length(); i++) {
                JSONObject j_inside = cdm_jArry.getJSONObject(i);
                String MAC_value = j_inside.getString("Address");
                String x_value = j_inside.getString("x");
                String y_value = j_inside.getString("y");
                String z_value = j_inside.getString("z");


                //Add your values in your `ArrayList` as below:
                cdm_sen = new HashMap<String, String>();
                cdm_sen.put("Address", MAC_value);

                cdm_sen.put("x", x_value);
                cdm_sen.put("y", y_value);
                cdm_sen.put("z", z_value);

                formList.add(cdm_sen);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.println(Log.INFO, "check", formList.size()+"");
        return formList;
    }

    @Override
    protected void onPostExecute(ArrayList<HashMap<String, String>> hashMaps) {
        super.onPostExecute(hashMaps);
    }
}
