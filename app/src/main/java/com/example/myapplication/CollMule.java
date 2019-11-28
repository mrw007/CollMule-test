package com.example.myapplication;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by aigerimzhalgasbekova on 14/04/2017.
 */

public class CollMule {

    Comparison c = new Comparison();
    Metrics metr = new Metrics();
    ArrayList<Double> metricsPr = new ArrayList<>();
    ArrayList<Double> rssiPr = new ArrayList<>();
    ArrayList<Double> distPr = new ArrayList<>();
    ArrayList<Double> pwrPr = new ArrayList<>();
    ArrayList<Double> accPr = new ArrayList<>();

    //addresses of ordered devices
    public ArrayList<String> orderedDev(){
        Log.i("Ordered addresses&weigh", ""+sorted);
        Set<String> sorted_k = sorted.keySet();
        ArrayList<String> sorted_kArr = new ArrayList<>(sorted_k);
        return sorted_kArr;
    }

    //sensor addresses with weights
    public HashMap<String, Double> senW(){
        HashMap<String, Double> order = new HashMap<>();
        ThreadPriority T1 = new ThreadPriority("Thread");
        T1.start();
        try {
            //T1.join();
            Thread.sleep(110);
            for (int i = 0; i<metr.addrAv().size(); i++){
                long timeStart = System.nanoTime();
                Log.i("Time Started senW", ""+timeStart);
                Log.i("bla", ""+metr.addrAv()+senPriority);
                order.put(metr.addrSort.get(i), senPriority.get(i));

                long timeElapsed = System.nanoTime()-timeStart;
                Log.i("Time Elapsed senW", ""+timeElapsed);
            }
        } catch (Exception e){
            System.out.println("Interrupted");
        }
        return order;
    }
    //method for ascending sort of the distances
    //@return the sorted list
    //sorting Map like Hashtable and HashMap by values in Java
    public Map<String, Double> sorted = sortByValues(senW());

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
    //calculation of snesors' weights
    public ArrayList<Double> senPriority;

    private class ThreadPriority extends Thread{
        private Thread t;
        private String threadName;

        ThreadPriority( String name) {
            threadName = name;
        }

        public void run(){
                long timeStart = System.currentTimeMillis();
                Log.i("ThreadPr Time Started", ""+timeStart);
                ArrayList<Double> sensors = new ArrayList<>();
                metricsPriority();
                distPriority();
                rssiPriority();
                pwrPriority();
                accPriority();
                double pr;
                for (int i=0; i<c.rssiScAr().size(); i++){
                    pr = metricsPr.get(0)*distPr.get(i) + metricsPr.get(1)*rssiPr.get(i) + metricsPr.get(2)*pwrPr.get(i) + metricsPr.get(3)*accPr.get(i);
                    sensors.add(i, pr);
                }
                senPriority = sensors;

                long timeElapsed = System.currentTimeMillis()-timeStart;
                Log.i("ThreadPr Time Elapsed", ""+timeElapsed);
        }

        public void start () {
            System.out.println("Starting " +  threadName );
            if (t == null) {
                t = new Thread (this, threadName);
                t.start ();
            }
        }
    }
    //Priority of metrics with respect to the goal
    public void metricsPriority(){

        String labels[] = {"Distance ", "RSSI  ", "PowerLevel   ", "SensorAccuracy    "};
        int nrVx = labels.length;

        AHP ahp = new AHP(nrVx);
        System.out.println(ahp);

        int d = ahp.getNrOfPairwiseComparisons();

        double compArray[] = ahp.getPairwiseComparisonArray();

        //metrics' averages
        ArrayList<Double> avs = c.compAv;

        // Set the pairwise comparison values
        for (int i=0; i<c.compAv.size(); i++){
            compArray[i] = avs.get(i);
        }

        ahp.setPairwiseComparisonArray(compArray);

        for (int i = 0; i < ahp.getNrOfPairwiseComparisons(); i++) {
            System.out.print("Metrics: Importance of " + labels[ahp.getIndicesForPairwiseComparison(i)[0]] + " compared to ");
            System.out.print(labels[ahp.getIndicesForPairwiseComparison(i)[1]] + "= ");
            System.out.println(ahp.getPairwiseComparisonArray()[i]);
        }

        System.out.println("\n" + ahp + "\n");

        System.out.println("Consistency Index: " + ahp.getConsistencyIndex());
        System.out.println("Consistency Ratio: " + ahp.getConsistencyRatio() + "%");
        System.out.println();
        System.out.println("Weights: ");
        for (int k=0; k<ahp.getWeights().length; k++) {
            System.out.println(labels[k] + ": " + ahp.getWeights()[k] * 100);
        }

        //add the values if the consistency is acceptable
        if (ahp.getConsistencyRatio()<10){
            for (int k=0; k<ahp.getWeights().length; k++) {
                metricsPr.add(k, ahp.getWeights()[k]);
            }
        } else {
            Log.i("Consistency is", "not acceptable");
        }
    }

    //Priority of sensors with respect to rssi
    public void rssiPriority(){

        String labels[] = new String[c.rssiScAr().size()];
        for (int i=0; i<c.rssiScAr().size(); i++){
            labels[i] = "Sensor"+i+"   ";
        }
        int nrVx = labels.length;

        AHP ahp = new AHP(nrVx);
        System.out.println(ahp);

        int d = ahp.getNrOfPairwiseComparisons();

        double compArray[] = ahp.getPairwiseComparisonArray();

        //metrics' averages
        ArrayList<Double> rssi = c.compRssi;
        Log.i("rssi from compRssi", ""+rssi);

        // Set the pairwise comparison values
        for (int i=0; i<c.compRssi.size(); i++){
            compArray[i] = rssi.get(i);
        }

        ahp.setPairwiseComparisonArray(compArray);

        for (int i = 0; i < ahp.getNrOfPairwiseComparisons(); i++) {
            System.out.print("Rssi: Importance of " + labels[ahp.getIndicesForPairwiseComparison(i)[0]] + " compared to ");
            System.out.print(labels[ahp.getIndicesForPairwiseComparison(i)[1]] + "= ");
            System.out.println(ahp.getPairwiseComparisonArray()[i]);
        }

        System.out.println("\n" + ahp + "\n");

        System.out.println("Consistency Index: " + ahp.getConsistencyIndex());
        System.out.println("Consistency Ratio: " + ahp.getConsistencyRatio() + "%");
        System.out.println();
        System.out.println("Weights: ");
        for (int k=0; k<ahp.getWeights().length; k++) {
            System.out.println(labels[k] + ": " + ahp.getWeights()[k] * 100);
        }

        //add the values if the consistency is acceptable
        if (ahp.getConsistencyRatio()<10){
            double val;
            for (int k=0; k<ahp.getWeights().length; k++) {
                val = ahp.getWeights()[k]*100;
                rssiPr.add(k, val);
            }
        } else {
            Log.i("Consistency is", "not acceptable");
        }
    }

    //Priority of sensors with respect to distance
    public void distPriority(){

        String labels[] = new String[c.distScAr().size()];
        for (int i=0; i<c.distScAr().size(); i++){
            labels[i] = "Sensor"+i+"   ";
        }
        int nrVx = labels.length;

        Log.i("Labels", ""+labels.length);

        AHP ahp = new AHP(nrVx);
        System.out.println(ahp);

        int d = ahp.getNrOfPairwiseComparisons();

        double compArray[] = ahp.getPairwiseComparisonArray();
        Log.i("compArray", ""+compArray.length);

        //metrics' averages
        ArrayList<Double> dist = c.compDist;
        Log.i("Distance in Priority", dist.toString());

        // Set the pairwise comparison values
        for (int i=0; i<c.compDist.size(); i++){
            compArray[i] = dist.get(i);
        }

        ahp.setPairwiseComparisonArray(compArray);

        for (int i = 0; i < ahp.getNrOfPairwiseComparisons(); i++) {
            System.out.print("Dist: Importance of " + labels[ahp.getIndicesForPairwiseComparison(i)[0]] + " compared to ");
            System.out.print(labels[ahp.getIndicesForPairwiseComparison(i)[1]] + "= ");
            System.out.println(ahp.getPairwiseComparisonArray()[i]);
        }

        System.out.println("\n" + ahp + "\n");

        System.out.println("Consistency Index: " + ahp.getConsistencyIndex());
        System.out.println("Consistency Ratio: " + ahp.getConsistencyRatio() + "%");
        System.out.println();
        System.out.println("Weights: ");
        for (int k=0; k<ahp.getWeights().length; k++) {
            System.out.println(labels[k] + ": " + ahp.getWeights()[k] * 100);
        }

        //add the values if the consistency is acceptable
        if (ahp.getConsistencyRatio()<10){
            double val;
            for (int k=0; k<ahp.getWeights().length; k++) {
                val = ahp.getWeights()[k]*100;
                distPr.add(k, val);
            }
        } else {
            Log.i("Consistency is", "not acceptable");
        }
    }

    //Priority of sensors with respect to power level
    public void pwrPriority(){

        String labels[] = new String[c.pwrScAr().size()];
        for (int i=0; i<c.pwrScAr().size(); i++){
            labels[i] = "Sensor"+i+"   ";
        }
        int nrVx = labels.length;

        AHP ahp = new AHP(nrVx);
        System.out.println(ahp);

        int d = ahp.getNrOfPairwiseComparisons();

        double compArray[] = ahp.getPairwiseComparisonArray();

        //metrics' averages
        ArrayList<Double> pwr = c.compPwr;

        // Set the pairwise comparison values
        for (int i=0; i<c.compPwr.size(); i++){
            compArray[i] = pwr.get(i);
        }

        ahp.setPairwiseComparisonArray(compArray);

        for (int i = 0; i < ahp.getNrOfPairwiseComparisons(); i++) {
            System.out.print("PWR: Importance of " + labels[ahp.getIndicesForPairwiseComparison(i)[0]] + " compared to ");
            System.out.print(labels[ahp.getIndicesForPairwiseComparison(i)[1]] + "= ");
            System.out.println(ahp.getPairwiseComparisonArray()[i]);
        }

        System.out.println("\n" + ahp + "\n");

        System.out.println("Consistency Index: " + ahp.getConsistencyIndex());
        System.out.println("Consistency Ratio: " + ahp.getConsistencyRatio() + "%");
        System.out.println();
        System.out.println("Weights: ");
        for (int k=0; k<ahp.getWeights().length; k++) {
            System.out.println(labels[k] + ": " + ahp.getWeights()[k] * 100);
        }

        //add the values if the consistency is acceptable
        if (ahp.getConsistencyRatio()<10){
            double val;
            for (int k=0; k<ahp.getWeights().length; k++) {
                val = ahp.getWeights()[k]*100;
                pwrPr.add(k, val);
            }
        } else {
            Log.i("Consistency is", "not acceptable");
        }
    }

    //Priority of sensors with respect to sensor accuracy
    public void accPriority(){

        String labels[] = new String[c.accScAr().size()];
        for (int i=0; i<c.accScAr().size(); i++){
            labels[i] = "Sensor"+i+"   ";
        }
        int nrVx = labels.length;

        AHP ahp = new AHP(nrVx);
        System.out.println(ahp);

        int d = ahp.getNrOfPairwiseComparisons();

        double compArray[] = ahp.getPairwiseComparisonArray();

        //metrics' averages
        ArrayList<Double> acc = c.compAcc;

        // Set the pairwise comparison values
        for (int i=0; i<c.compAcc.size(); i++){
            compArray[i] = acc.get(i);
        }

        ahp.setPairwiseComparisonArray(compArray);

        for (int i = 0; i < ahp.getNrOfPairwiseComparisons(); i++) {
            System.out.print("Accuracy: Importance of " + labels[ahp.getIndicesForPairwiseComparison(i)[0]] + " compared to ");
            System.out.print(labels[ahp.getIndicesForPairwiseComparison(i)[1]] + "= ");
            System.out.println(ahp.getPairwiseComparisonArray()[i]);
        }

        System.out.println("\n" + ahp + "\n");

        System.out.println("Consistency Index: " + ahp.getConsistencyIndex());
        System.out.println("Consistency Ratio: " + ahp.getConsistencyRatio() + "%");
        System.out.println();
        System.out.println("Weights: ");
        for (int k=0; k<ahp.getWeights().length; k++) {
            System.out.println(labels[k] + ": " + ahp.getWeights()[k] * 100);
        }

        //add the values if the consistency is acceptable
        if (ahp.getConsistencyRatio()<10){
            double val;
            for (int k=0; k<ahp.getWeights().length; k++) {
                val = ahp.getWeights()[k]*100;
                accPr.add(k, val);
            }
        } else {
            Log.i("Consistency is", "not acceptable");
        }
    }
}
