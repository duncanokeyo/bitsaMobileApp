package com.dans.apps.bitsa.utils;

import android.os.RemoteException;
import android.util.Log;

import com.dans.apps.bitsa.BuildConfig;


/**
 * Base Class for logging app content
 * Created by duncan on 6/25/17.
 */

public class LogUtils {

    private static boolean shouldLog = BuildConfig.DEBUG;

    public static void d(String TAG, String message){
        if(shouldLog){
            Log.d("BITSA/"+TAG,message);
        }
    }

    public static void e(String TAG,String message){
        if(shouldLog){
            Log.e("BITSA/"+TAG,message);
        }
    }

    public static void i(String tag, String s) {
        if(shouldLog){
            Log.e("BITSA/"+tag,s);
        }
    }

    public static void v(String tag, String s) {
        if(shouldLog){
            Log.v("BITSA/"+tag,s);
        }
    }

    public static void w(String tag, String s, Exception e) {
        if(shouldLog){
            if(e!=null){
                Log.w("BITSA/"+tag,s,e);
            }else {
                Log.w("BITSA/"+tag,s);
            }
        }
    }

}
