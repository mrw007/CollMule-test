package com.example.myapplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aigerimzhalgasbekova on 04/04/2017.
 */

public class Comparison {

    public double setDistSc(double dist){
        double distSc = 0;
        if (dist<2 && dist>=0){
            distSc = 9.0;
        }
        else if (dist<5 && dist>=2){
            distSc = 7.0;
        }
        else if (dist<8 && dist>=5){
            distSc = 5.0;
        }
        else if (dist<10 && dist>=8){
            distSc = 3.0;
        }
        else if (dist>=10){
            distSc = 1.0;
        }
        return distSc;
    }

    public double setRssiSc(int rssi){
        double rssiSc = 0;
        if (rssi<=-25 && rssi>=-45){
            rssiSc = 9.0;
        }
        else if (rssi<=-46 && rssi>=-60){
            rssiSc = 7.0;
        }
        else if (rssi<=-61 && rssi>=-70){
            rssiSc = 5.0;
        }
        else if (rssi<=-71 && rssi>=-80){
            rssiSc = 3.0;
        }
        else if (rssi<-80){
            rssiSc = 1.0;
        }
        else{
            rssiSc = 0;
        }
        return rssiSc;
    }

    public double setPowerlvlSc(int powerlvl){
        double powerlvlSc = 0;
        if (powerlvl<30){
            powerlvlSc = 9.0;
        }
        else if (powerlvl<50 && powerlvl>=30){
            powerlvlSc = 7.0;
        }
        else if (powerlvl<70 && powerlvl>=50){
            powerlvlSc = 5.0;
        }
        else if (powerlvl<90 && powerlvl>=70){
            powerlvlSc = 3.0;
        }
        else if (powerlvl<=100 && powerlvl>=90){
            powerlvlSc = 1.0;
        }
        return powerlvlSc;
    }

    public double setSenAccSc(String senacc){
        double senaccSc = 0.0;
        if (senacc=="T"){
            senaccSc = 1.0;
        } else if (senacc=="T!"){
            senaccSc = 3.0;
        } else if (senacc=="TF"){
            senaccSc = 5.0;
        } else if (senacc=="F!"){
            senaccSc = 7.0;
        } else {
            senaccSc = 9.0;
        }
        return senaccSc;
    }

    public double compare(double a, double b){
        double val = 0;
        if(a==b){
            val = 1.00;
        }
        else if (a==9 && b==7){
            val = 3.00;
        }
        else if (a==9 && b==5){
            val = 5.00;
        }
        else if (a==9 && b==3){
            val = 7.00;
        }
        else if (a>b && b==1){
            val = a;
        }
        else if (a==1 && a<b){
            val = 1.0 / b;
        }
        else if (a==7 && b==9){
            val = 1.0 / 3.0;
        }
        else if (a==7 && b==5){
            val = 3.00;
        }
        else if (a==7 && b==3){
            val = 5.00;
        }
        else if (a==5 && b==9){
            val = 1.0 / 5.0;
        }
        else if (a==5 && b==7){
            val = 1.0 / 3.0;
        }
        else if (a==5 && b==3){
            val = 3.00;
        }
        else if (a==3 && b==9){
            val = 1.0 / 7.0;
        }
        else if (a==3 && b==7){
            val = 1.0 / 5.0;
        }
        else if (a==3 && b==5){
            val = 1.0 / 3.0;
        }
        return val;
    }

    //metrics
    Metrics m = new Metrics();
    //devices with metrics
    HashMap<String, ArrayList> metrics = m.metrics();
    //rssi values
    ArrayList<Integer> rssi = m.retrRSSI();
    //score all rssi values
    public ArrayList<Double> rssiScAr(){
        ArrayList<Double> r = new ArrayList<>();
        double v = 0;
        for (int i=0; i<rssi.size(); i++){
            v = setRssiSc(rssi.get(i));
            r.add(v);
        }
        return r;
    }

    //distance values
    List<Double> dist = m.distsAr();
    //score all distance values
    public ArrayList<Double> distScAr(){
        ArrayList<Double> d = new ArrayList<>();
        double v = 0;
        for (int i=0; i<dist.size(); i++){
            v = setDistSc(dist.get(i));
            d.add(v);
        }
        return d;
    }

    //powerlevel values
    ArrayList<Integer> pwrlvl = m.retrPwrlvl();
    //score all powerlevel values
    public ArrayList<Double> pwrScAr(){
        ArrayList<Double> p = new ArrayList<>();
        double v = 0;
        for (int i=0; i<pwrlvl.size(); i++){
            v = setPowerlvlSc(pwrlvl.get(i));
            p.add(v);
        }
        return p;
    }

    //sensor accuracy values
    ArrayList<String> senacc = m.retrSenAcc();
    //score all sensor accuracy values
    public ArrayList<Double> accScAr(){
        ArrayList<Double> a = new ArrayList<>();
        double v = 0;
        for (int i=0; i<senacc.size(); i++){
            v = setSenAccSc(senacc.get(i));
            a.add(v);
        }
        return a;
    }

    //scores of average values
    public ArrayList averSc(){
        ArrayList avs = new ArrayList();
        avs.add(0, setDistSc(m.avDist()));
        avs.add(1, setRssiSc(m.avRSSI));
        avs.add(2, setPowerlvlSc(m.avPowerlvl));
        avs.add(3, setSenAccSc(m.avAcc()));
        return avs;
    }
    //comparison among values of different devices
    public ArrayList<Double> compArr(ArrayList<Double> a){
        ArrayList <Double> c = new ArrayList<>();
        double r = 0;
        for (int i=0; i<a.size(); i++){
            for (int j= i+1; j<a.size(); j++){
                if (i==a.size()-1){
                    break;
                } else {
                    r = compare(a.get(i), a.get(j));
                }

                c.add(r);
            }
        }
        return c;
    }

    ArrayList compRssi = compArr(rssiScAr());
    ArrayList compDist = compArr(distScAr());
    ArrayList compPwr = compArr(pwrScAr());
    ArrayList compAcc = compArr(accScAr());

    //comparison of metrics averages
    ArrayList compAv = compArr(averSc());
}
