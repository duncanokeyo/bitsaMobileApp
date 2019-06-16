package com.dans.apps.bitsa.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.dans.apps.bitsa.R;

import java.util.HashMap;

import androidx.appcompat.app.AlertDialog;

/**
 * Created by duncan on 12/24/17.
 */

public class UiUtils {


    public interface onAction{
        void onCanceled();
        void onOkClicked();
    }

    public static boolean isOnline(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }


    public static void showErrorWithFinishAction(final Activity activity, int messageResID){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.error);
        builder.setMessage(messageResID);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });
        builder.create().setCanceledOnTouchOutside(false);
        builder.show();
    }

    public static void showNotConnectedAlert(Activity activity, final onAction action){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.connection);
        builder.setMessage(R.string.no_connection_message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(action!=null){
                    action.onOkClicked();
                }
            }
        });
        builder.show();
    }

    public static void showNotConnectedAlert(Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.connection);
        builder.setMessage(R.string.no_connection_message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }


    public static void shareText(String subject, String body, Activity host) {
        Intent txtIntent = new Intent(Intent.ACTION_SEND);
        txtIntent .setType("text/plain");
        txtIntent .putExtra(Intent.EXTRA_SUBJECT, subject);
        txtIntent .putExtra(Intent.EXTRA_TEXT, body);
        host.startActivity(Intent.createChooser(txtIntent ,"Share"));
    }

    public static String phoneNumberMinusCode(String phoneNumber){
        if(phoneNumber == null)return phoneNumber;

        int subStringLength = 9;
        int length = phoneNumber.length();
        if(length <= subStringLength){
            return phoneNumber;
        }
        int startIndex = length-subStringLength;
        return phoneNumber.substring(startIndex);
    }

    public static String phoneNumberCountryCode(String phoneNumber){
        if(phoneNumber!=null){
            return phoneNumber.substring(0,phoneNumber.length()-9).replace("+","").trim();
        }
        return "+1";
    }

    public static String prettify(HashMap<String, String> body) {
        StringBuilder builder = new StringBuilder();
        for(String key:body.keySet()){
            builder.append("\n").append(key).append("\n").append(body.get(key));
        }
        return builder.toString();
    }

    //this method will only accept kenyan numbers
    public static String sanitizePhoneNumber(String phoneNumber) {
        if(TextUtils.isEmpty(phoneNumber))return "";
        phoneNumber = phoneNumber.trim();

        if(phoneNumber.startsWith("+")){
            if(!phoneNumber.startsWith("+254")){
                return "";
            }else{
                if(phoneNumber.length()!=13){
                    return "";
                }else{
                    return phoneNumberMinusCode(phoneNumber);
                }
            }
        }

        if(phoneNumber.startsWith("254")){
            if(phoneNumber.length() !=12){
                return "";
            }
        }
        if(phoneNumber.startsWith("0")){
            if(phoneNumber.length()!=10){
                return "";
            }else{
                return phoneNumberMinusCode(phoneNumber);
            }
        }
        return "";
    }
}
