package com.snippet.com.snippet.debug;

import android.util.Log;

/**
 * Created by Jorge on 10/13/15.
 */
public class Debug {
    public static boolean isDebugging;


    public  static void DEBUG(String tag, String message){
        Log.d(tag, message);
    }

}
