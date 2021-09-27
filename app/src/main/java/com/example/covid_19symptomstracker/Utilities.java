package com.example.covid_19symptomstracker;

import java.util.ArrayList;
import java.util.List;

public class Utilities {

    public static List<Float> simpleMovingAverage(List<Float> arr){

        List<Float> SMA = new ArrayList<>();
        double WINDOW = 12;
        double sum = 0;
        int counter = 0;

        for (int i=0; i<arr.size(); i++){
            sum+=arr.get(i);
            counter++;
            if (counter>WINDOW){
                sum-=arr.get(i-12);
            }
            SMA.add((float) (sum/WINDOW));
        }
        return SMA;
    }

    public static List<Float> differential(List<Float> arr){

        List<Float> diffArr = new ArrayList<>();
        for(int i=0; i<arr.size(); i++){
            if(i+1<arr.size()) {
                diffArr.add(Math.abs(arr.get(i+1) - arr.get(i)));
            }

        }
        return diffArr;
    }

    public static float zeroCrossing(List<Float> arr){

        float zeroCrossings = 0;
        boolean left;
        float sum =0;
        float avg;
        int n = arr.size();

        for(int i=0; i<n; i++) {
            sum = sum + arr.get(i);
        }
        avg = sum/n;

        if(arr.get(0) >= avg) {
            left = true;

            for(int i=0;i<arr.size();i++) {
                if(left) {
                    if(arr.get(i) < avg){
                        zeroCrossings++;
                        left = false;
                    }
                }
                else {
                    if(arr.get(i) >= avg) {
                        zeroCrossings++;
                        left = true;
                    }
                }
            }
        }
        else {
            left = false;

            for(int i=0;i<arr.size();i++) {
                if(left){
                    if(arr.get(i) < avg) {
                        zeroCrossings++;
                        left = false;
                    }
                }
                else{
                    if(arr.get(i) >= avg) {
                        zeroCrossings++;
                        left = true;
                    }
                }
            }
        }
        return zeroCrossings;
    }
}
