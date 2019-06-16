package com.dans.apps.bitsa.utils;

import android.util.Base64;
import android.util.Log;

import com.dans.apps.bitsa.model.Semester;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MpesaUtils {

    static  String TAG = "MpesaUtils";


    public static String formulateCallbackUrl(@NonNull String baseUrl,
                                              @NonNull String email, @NonNull int transactionType,
                                              @Nullable Semester semester, @Nullable String token){
        String string = baseUrl+"?email="+email+"&type="+transactionType;
        if(semester!=null){
            string = string+"&semester="+semester.formulateSemester();
        }
        if(token!=null){
            string = string+"&token="+token;
        }
        return string;
    }

    public static String generatePassword(String shortCode,String passKey,String timeStamp){
        String string = shortCode+passKey+timeStamp;
        return Base64.encodeToString(string.getBytes(),Base64.NO_WRAP);
    }

    public static boolean isRigthAmount(int amount){
        return amount<=70000 && amount>0;
    }
    public String getPaymentAmountFromSchoolID(String schoolID){
        return "200";
    }

    public static String getTimestamp(){

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int seconds  = calendar.get(Calendar.SECOND);

        String dayString = day<=9?"0"+day:String.valueOf(day);
        String monthString = month<=9?"0"+month:String.valueOf(month);
        String hourString = hour<=9?"0"+hour:String.valueOf(hour);
        String minuteString = minute<=9?"0"+minute:String.valueOf(minute);
        String secondsString = seconds<=9?"0"+seconds:String.valueOf(seconds);

        String string= year+monthString+dayString+hourString+minuteString+secondsString;
        Log.v(TAG," timestamp ==> "+string);
        return string;
    }
}
