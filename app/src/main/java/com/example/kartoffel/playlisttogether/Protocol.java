package com.example.kartoffel.playlisttogether;

import android.util.Log;

public class Protocol {

    public static final String TRACK = "@TRACK@";
    public static String PKEY = "none";

    public static void setPremiumKey(String key){
        PKEY = key;
        Log.d("Protocol", "New Premium Key set: " + key);
    }

}
