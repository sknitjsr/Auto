package com.aaars.b;

import java.util.*;

public class LastRun {
    public ArrayList<String> lastrun;


    public LastRun() {
        lastrun = new ArrayList<String>();
        for(int i = 0; i < 29; i++) {
            lastrun.add("0");
        }




    }
    public void set(int i , String s)
    {
        lastrun.set(i, s);
    }


}
