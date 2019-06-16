package com.dans.apps.bitsa.network;

import com.dans.apps.bitsa.utils.LogUtils;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AccessTokenInterceptor implements Interceptor {
    String TAG = "AccessTokenInterceptor";
    private String token;

    public AccessTokenInterceptor(String token) {
        this.token = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        LogUtils.d(TAG,"###### intercept called .. ");
        Request request = chain.request();
        Request authenticatedRequest = request.newBuilder().
                header("Authorization","Bearer "+token).build();

        return chain.proceed(authenticatedRequest);
    }
}
