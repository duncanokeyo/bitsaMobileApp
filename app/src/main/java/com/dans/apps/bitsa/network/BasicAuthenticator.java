package com.dans.apps.bitsa.network;

import com.dans.apps.bitsa.utils.LogUtils;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class BasicAuthenticator implements Interceptor {
    String TAG = "BasicAuthenticator";
    private String credentials;


    public BasicAuthenticator(String userName,String password) {
        this.credentials = Credentials.basic(userName,password);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        LogUtils.d(TAG,"###### intercept called .. ");
        Request request = chain.request();
        Request authenticatedRequest = request.newBuilder().
                header("Authorization",credentials).build();

        return chain.proceed(authenticatedRequest);
    }
}
