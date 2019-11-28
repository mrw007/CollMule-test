package com.example.myapplication;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by aigerimzhalgasbekova on 05/04/2017.
 */

public class Metrics {

    ArrayList<HashMap<String, String>> mRepository = MainActivity.senCoord;

    ToJSON tJ = new ToJSON();
    String name = "Res.json";
    //file with scanned devices
    public String defFile = tJ.openJSFile(name).replace("}", "").replace("{", "").replace("\n", "");

    public HashMap<String, ArrayList<String>> devScanned(){
        HashMap<String, ArrayList<String>> myMap= new HashMap<>();
        String[] pairs = defFile.split("], ");
        for (int i=0;i<pairs.length; i++) {
            String pair = pairs[i];
            String[] keyValue = pair.split("=");
            String[] v = keyValue[1].replace("[", "").replace("]", "").split(", ");
            //String.valueOf(keyValue[1]);
            ArrayList<String> info = new ArrayList<>();
            info.add(0, v[0]);
            info.add(1, v[1]);
            //Log.i("info", info.toString());
            myMap.put(keyValue[0], info);
        }
        return myMap;
    }

    //addresses of scaned devices
    public List<String> devAddr(){
        Set<String> devAv_k = devScanned().keySet();
        ArrayList<String> devAv_kArr = new ArrayList<>(devAv_k);
        String devAvAddr = "";
        ArrayList<String> devAdd = new ArrayList<>();
        for (int i=0; i<devAv_kArr.size(); i++){
            if (Integer.parseInt(devScanned().get(devAv_kArr.get(i)).get(0))>=-84){
                devAvAddr = devAv_kArr.get(i);
                devAdd.add(devAvAddr);
            }
        }
        return devAdd;
    }


    //info about available sensors coordinates
    public ArrayList<Map<String, String>> devAvCoord = devAvCoord();
    public ArrayList<Map<String, String>> devAvCoord(){
        long timeStart = System.nanoTime();
        Log.i("Time Started devAvCoord", ""+timeStart);
        ArrayList<Map<String, String>> devices = new ArrayList<>();
        for (int i = 0; i < devAddr().size(); i++) {
            String c = devAddr().get(i);
            for (int j = 0; j < mRepository.size(); j++) {
                String k = mRepository.get(j).get("Address");
                if (c.equals(k)) {
                    HashMap<String, String> info = new HashMap<>();
                    info.put("Address", mRepository.get(j).get("Address"));
                    info.put("x", mRepository.get(j).get("x"));
                    info.put("y", mRepository.get(j).get("y"));
                    info.put("z", mRepository.get(j).get("z"));
                    devices.add(info);
                }
            }
        }
        long timeElapsed = System.nanoTime()-timeStart;
        Log.i("Time Elapsed devAvCoord", ""+timeElapsed);
        return devices;
    }

    //addresses of available devices
    public ArrayList<String> addrAv(){
        ArrayList<String> addr = new ArrayList<>();
        String a;
        for (int i = 0; i < devAvCoord.size(); i++){
            a = devAvCoord.get(i).get("Address");
            addr.add(i, a);
        }
        return addr;
    }

    //sensor's coordinates are retrieved from the ContextManagerRepository.json file
    public ArrayList<Double> senXretr(){
        ArrayList<Double> senX = new ArrayList<Double>();
        double x;
        for (int i=0; i<devAvCoord.size(); i++) {
            //x = retrieved from .json
            x = Double.parseDouble(devAvCoord.get(i).get("x"));
            senX.add(x);
        }
        return senX;
    }
    public ArrayList<Double> senYretr(){
        ArrayList<Double> senY = new ArrayList<Double>();
        double y;
        for (int i=0; i<devAvCoord.size(); i++){
            //x = retrieved from .json
            y = Double.parseDouble(devAvCoord.get(i).get("y"));
            senY.add(y);
        }
        return senY;
    }
    public ArrayList<Double> senZretr(){
        ArrayList<Double> senZ = new ArrayList<Double>();
        double z;
        for (int i=0; i<devAvCoord.size(); i++){
            //x = retrieved from .json
            z = Double.parseDouble(devAvCoord.get(i).get("z"));
            senZ.add(z);
        }
        return senZ;
    }

    //number of RPis available
    int nDev = devAvCoord.size();

    //Location tracker
    Tracker tracker = new Tracker(MainActivity.context);

    //mule's coordinates using Tracker
    double mulX = 53.5; //tracker.latitude;//
    double mulY = 8.6; //tracker.longitude;//
    double mulZ = 2.0;//tracker.altitude;

    //list of all distances
    public HashMap<String,Double> distMap = calcDist(); //new HashMap<Integer, Double>();
    //method for calculation of distances between the mule and sensors
    private HashMap<String, Double> calcDist(){
        HashMap<String,Double> dMap = new HashMap<String, Double>();
        //calculate the distances to all known sensors and save them to the distTable
        double dist = 0;
        for(int i=0; i<devAvCoord.size(); i++) {
            dist = Math.sqrt(Math.pow((senXretr().get(i) - mulX), 2) + Math.pow((senYretr().get(i) - mulY), 2) + Math.pow((senZretr().get(i) - mulZ), 2));
            dMap.put(devAvCoord.get(i).get("Address"), dist);
        }
        //Log.d("TAG", distMap.toString());
        return dMap;
    }


    public List<Double> distsAr(){
        Collection<Double> dists = sortedD.values();
        ArrayList<Double> distsAr = new ArrayList<>(dists);

        return distsAr;
    }

    //method for ascending sort of the distances
    //@return the sorted list
    //sorting Map like Hashtable and HashMap by values in Java
    public Map<String, Double> sortedD = sortByValues(distMap);

    /*
     * Java method to sort Map in Java by value e.g. HashMap or Hashtable
     * throw NullPointerException if Map contains null values
     * It also sort values even if they are duplicates
     */
    public static <K extends Comparable,V extends Comparable> Map<K,V> sortByValues(Map<K,V> map){
        List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<K,V>>() {

            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        //LinkedHashMap will keep the keys in the order they are inserted
        //which is currently sorted on natural ordering
        Map<K,V> sortedMap = new LinkedHashMap<K, V>();

        for(Map.Entry<K,V> entry: entries){
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    List<String> addrSort = addrSort();
    List<String> addrSort(){
        Set<String> devS_k = sortedD.keySet();
        ArrayList<String> devS_kArr = new ArrayList<>(devS_k);
        return devS_kArr;
    }

    //get RSSI of the RPis from the file with scanned devices
    public ArrayList<Integer> retrRSSI(){
        ArrayList<Integer> senRSSI = new ArrayList<Integer>();
        int rssi;
        for (int i=0; i<addrSort.size(); i++) {
            //x = retrieved from .json
            for (int j=0; j<devAvCoord.size(); j++){
                if (devAvCoord.get(j).get("Address").equals(addrSort.get(i))){
                    rssi = Integer.parseInt(devScanned().get(addrSort.get(i)).get(0));
                    senRSSI.add(rssi);
                }
            }
        }
        return senRSSI;
    }

    //retrieve sensor accuracy
    public ArrayList<String> retrSenAcc(){
        ArrayList<String> senacc = new ArrayList();
        String acc;
        for (int i=0; i<addrSort.size(); i++) {
            //x = retrieved from .json
            for (int j=0; j<devAvCoord.size(); j++){
                if (devAvCoord.get(j).get("Address").equals(addrSort.get(i))){
                    String name = devScanned().get(addrSort.get(i)).get(1);
                    acc = Character.toString(name.charAt(13));
                    senacc.add(acc);
                }
            }
        }
        return senacc;
    }

    //retrieve power level
    public ArrayList<Integer> retrPwrlvl(){
        ArrayList<Integer> pwrlvl = new ArrayList();
        int pwr;
        for (int i=0; i<addrSort.size(); i++) {
            //x = retrieved from .json
            for (int j=0; j<devAvCoord.size(); j++){
                if (devAvCoord.get(j).get("Address").equals(addrSort.get(i))){
                    String name = devScanned().get(addrSort.get(i)).get(1);
                    String p = name.substring(14);
                    pwr = Integer.parseInt(p);
                    pwrlvl.add(pwr);
                }
            }
        }
        return pwrlvl;
    }

    //average of integer values
    public int average(ArrayList<Integer> b){
        int sum = 0;
        for (int i=0; i<b.size(); i++){
            sum += b.get(i);
        }
        return sum / b.size();
    }

    //average RSSI
    public int avRSSI = average(retrRSSI());

    //average Power Level
    public int avPowerlvl = average(retrPwrlvl());

    //average distance
    public double avDist(){
        double sum = 0.0;
        for (int i=0; i<distsAr().size(); i++){
            sum += distsAr().get(i);
        }
        return sum / distsAr().size();
    }

    //average sensor accuracy
    public String avAcc(){
        String acc = "";
        int f = 0;
        int t = 0;
        for (int i=0; i<retrSenAcc().size(); i++){
            if (retrSenAcc().get(i)=="F"){
                f++;
            } else {
                t++;
            }
        }
        if (f>t){
            acc = "F!";
        } else if (f<t){
            acc = "T!";
        } else {
            acc = "TF";
        }
        return acc;
    }

    //average metric values
    public ArrayList avMetrics(){
        ArrayList avs = new ArrayList();
        avs.add(0, avDist());
        avs.add(1, avRSSI);
        avs.add(2, avPowerlvl);
        avs.add(3, avAcc());
        Log.i("averages", avs.toString());
        return avs;
    }

    //RPi's address with metrics
    public HashMap<String, ArrayList> metrics(){
        HashMap<String, ArrayList> m = new HashMap<>();
        for (int i=0; i<devAvCoord.size(); i++){
            ArrayList info = new ArrayList();
            info.add(0, distsAr().get(i));
            info.add(1, retrRSSI().get(i));
            info.add(2, retrPwrlvl().get(i));
            info.add(3, retrSenAcc().get(i));
            m.put(addrSort.get(i), info);
        }

        return m;
    }
}
