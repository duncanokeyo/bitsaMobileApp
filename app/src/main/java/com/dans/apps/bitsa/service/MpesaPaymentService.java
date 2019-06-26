package com.dans.apps.bitsa.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dans.apps.bitsa.BitsaApplication;
import com.dans.apps.bitsa.Constants;
import com.dans.apps.bitsa.model.MpesaPaymentDetails;
import com.dans.apps.bitsa.model.Semester;
import com.dans.apps.bitsa.model.User;
import com.dans.apps.bitsa.network.BasicAuthenticator;
import com.dans.apps.bitsa.utils.LogUtils;
import com.dans.apps.bitsa.utils.MpesaUtils;
import com.dans.apps.bitsa.utils.UiUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MpesaPaymentService extends Service implements ValueEventListener {
    private static String TAG = "MpesaPaymentService";

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).build();
    private MediaType jsonMediaType = MediaType.parse("application/json;charset=utf-8");
    private MpesaPaymentDetails mpesaPaymentDetails;
    private DatabaseReference reference;
    private SharedPreferences preferences;

    public interface CallBack{

        void onPreconditionCheckFailed(String message);
        void onNoInternetConnection();
        void onMpesaProcessRequestFailed(String message);
        void onMpesaProcessedRequest(String message);
    }

    CallBack callBack;

    private final IBinder binder = new LocalBinder();

    public MpesaPaymentService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public class LocalBinder extends android.os.Binder {
        public MpesaPaymentService getService() {
            return MpesaPaymentService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
       return binder;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void initCallback(CallBack callBack){
        this.callBack = callBack;
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if(dataSnapshot.getValue()!=null){
            for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                mpesaPaymentDetails = snapshot.getValue(MpesaPaymentDetails.class);
            }
        }
        LogUtils.d(TAG,"transactions details ==> "+mpesaPaymentDetails.toString());
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    public void fetchMpesaTransactionDetails(){
        reference = FirebaseDatabase.getInstance().
                getReference(Constants.PATHS.mpesaTransactionDetails);
        reference.addValueEventListener(this);
    }

    public void initPayment(User user, String phoneNumber, int amount, int transactionType, Semester semester) {

        if((phoneNumber==null)){
            callBack.onPreconditionCheckFailed("No phone number set");
            return;
        }

        if(!UiUtils.isOnline(getApplicationContext())){
            callBack.onNoInternetConnection();
            return;
        }

        startPaymentProcess(user,phoneNumber,amount,transactionType,semester);
    }

    private void startPaymentProcess(@NonNull User user, @NonNull String phoneNumber, int amount,
                                     int transactionType, Semester semester) {

        //get the token;
        //todo, instead on making a call to get the accesstoken, we should store this token in an expiring map
        String token = BitsaApplication.getApplication().getToken();
        LogUtils.d(TAG,"## Retrieved access token from cache --> "+token);
        if(token!=null){
            sendPaymentRequest(user,phoneNumber,amount,token, transactionType,semester);
        }else{
            generateAccessToken(user,phoneNumber,amount,transactionType,semester);
        }

    }

    public void generateAccessToken(final User user,final String phoneNumber,
                                    final int amount, final int transactionType,
                                    final Semester semester){
        final String userName = ""; //et this information from the daraja console
        final Strnig passWord = ""; //this too
        final String credentials = Credentials.basic(userName,passWord);
        final Request request = new Request.Builder().url(Constants.MpesaEndPoints.AccessTokenGenerator).
                addHeader("Authorization",credentials).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onMpesaProcessRequestFailed(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    String content = response.body().string();
                    LogUtils.d(TAG,"[#TOKEN GENERATOR]content ==> ["+content+"]");
                    if(content == null){
                        callBack.onMpesaProcessRequestFailed("[#TOKEN GENERATOR]Empty response content");
                    }else{
                        try {
                            JSONObject object = new JSONObject(content);
                            String accessToken = object.getString(Constants.MpesaAuthFields.ACCESS_TOKEN);

                            int expiresIn = object.getInt(Constants.MpesaAuthFields.EXPIRES_IN);
                            LogUtils.d(TAG,"[#TOKEN GENERATOR] expires in "+expiresIn);
                            LogUtils.d(TAG,"[#TOKEN GENERATOR] retrieved accesstoken "+accessToken);

                            if(!accessToken.isEmpty()){
                               // accessToken = accessToken.trim();
                                BitsaApplication.getApplication().putToken(accessToken,expiresIn);
                                sendPaymentRequest(user,phoneNumber,amount,accessToken,transactionType, semester);
                            }else{
                                callBack.onMpesaProcessRequestFailed("[#TOKEN GENERATOR] Empty access token");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callBack.onMpesaProcessRequestFailed("[#TOKEN GENERATOR] "+e.getMessage());
                        }
                    }
                }else {
                    callBack.onMpesaProcessRequestFailed("[#TOKEN GENERATOR] Request not successful");
                }
            }
        });
    }


    private void sendPaymentRequest(final User user,
                                    String phoneNumber,
                                    final int amount, final String accessToken,
                                    final int transactionType,
                                    final Semester semester) {

        String timeStamp = MpesaUtils.getTimestamp();
        String token = preferences.getString(MessagingService.TOKEN_SHARED_PREF_KEY,null);
        String callBackUrl = MpesaUtils.formulateCallbackUrl(Constants.MpesaEndPoints.BaseCallbackUrl,
                user.getEmail(),transactionType,semester,token);

        LogUtils.d(TAG,"CallbackURL --> "+callBackUrl);
        String passWord = MpesaUtils.generatePassword(String.valueOf(174379),
                "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919",timeStamp);
        LogUtils.d(TAG,"password ===> "+passWord);

        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put(Constants.MpesaProcessRequestParams.BUSINESS_SHORT_CODE,String.valueOf(174379));
            jsonObject.put(Constants.MpesaProcessRequestParams.PASSWORD,passWord);
            jsonObject.put(Constants.MpesaProcessRequestParams.TIMESTAMP,timeStamp);
            jsonObject.put(Constants.MpesaProcessRequestParams.TRANSACTION_TYPE,"CustomerPayBillOnline");
            jsonObject.put(Constants.MpesaProcessRequestParams.AMOUNT,String.valueOf(amount));
            jsonObject.put(Constants.MpesaProcessRequestParams.PARTYA,phoneNumber);
            jsonObject.put(Constants.MpesaProcessRequestParams.PARTYB,String.valueOf(174379));
            jsonObject.put(Constants.MpesaProcessRequestParams.PHONE_NUMBER,phoneNumber);
            jsonObject.put(Constants.MpesaProcessRequestParams.CALLBACK_URL,callBackUrl);
            jsonObject.put(Constants.MpesaProcessRequestParams.ACCOUNT_REFERENCE,"test");
            jsonObject.put(Constants.MpesaProcessRequestParams.TRANSACTION_DESCRIPTION,"BitsaClub");
        }catch (Exception ignored){}

        String json = jsonObject.toString();
        LogUtils.d(TAG,"Request body ==> "+json);
        RequestBody body = RequestBody.create(jsonMediaType,json);
        LogUtils.d(TAG,"passing access token ==> "+accessToken);

        final Request request = new Request.Builder()
                .url(Constants.MpesaEndPoints.ProcessRequest).post(body)
                .addHeader("Authorization","Bearer "+accessToken)
                .build();

        LogUtils.d(TAG,"Request header ==> "+request.headers().toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onMpesaProcessRequestFailed("[#PAYMENT REQUEST] "+e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                LogUtils.d(TAG,"[#PAYMENT REQUEST] response -> "+response.toString());
                if(response.isSuccessful()){
                    String content = response.body().string();
                    LogUtils.d(TAG,"[#PAYMENT REQUEST] response content -> "+content);
                    if(content!=null) {
                        try {
                            JSONObject object = new JSONObject(content);
                            String responseCode = object.getString("ResponseCode");
                            LogUtils.d(TAG,"response code ===> "+responseCode);
                            if(responseCode.equals("0")){
                                callBack.onMpesaProcessedRequest(content);
                            }else{
                                callBack.onMpesaProcessRequestFailed("[#PAYMENT REQUEST] response code -> "+responseCode);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else{
                        callBack.onMpesaProcessRequestFailed("[#PAYMENT REQUEST] empty response content");
                    }
                }else{
                    callBack.onMpesaProcessRequestFailed("[#PAYMENT REQUEST] request not successful");
                }
            }
        });
    }
}
