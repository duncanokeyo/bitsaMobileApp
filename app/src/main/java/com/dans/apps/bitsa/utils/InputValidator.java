package com.dans.apps.bitsa.utils;

import android.util.Patterns;
import android.webkit.URLUtil;

import java.util.regex.Pattern;

public class InputValidator {

    public static boolean isValidEmail(CharSequence charSequence) {
        return Patterns.EMAIL_ADDRESS.matcher(charSequence).matches();
    }

    public static boolean isValidUrl(CharSequence charSequence){
        return URLUtil.isValidUrl((String) charSequence);
    }

    public static boolean isValidMobile(String phone) {
        if(!Pattern.matches("[a-zA-Z]+", phone)) {
            if(phone.length()!=9){
                return false;
            }
            return true;
        }
        return false;
    }
}
