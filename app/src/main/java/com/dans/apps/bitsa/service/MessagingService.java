package com.dans.apps.bitsa.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;

import com.dans.apps.bitsa.Constants;
import com.dans.apps.bitsa.MainActivity;
import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.utils.LogUtils;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import androidx.core.app.NotificationCompat;

public class MessagingService extends FirebaseMessagingService{

    String TAG = "MessagingService";
    public static final String TOKEN_SHARED_PREF_KEY="token";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(remoteMessage == null){
            return;
        }

        if(remoteMessage.getNotification()!=null){
            handleNotificationData(remoteMessage);
        }

        if(remoteMessage.getData().size()>0){
            LogUtils.v(TAG,"## data ==> "+remoteMessage.getData().toString());
            handleDataMessage(remoteMessage.getData());

        }
    }

    private void handleDataMessage(Map<String, String> map){
        String title = map.get("title");
        String message = map.get("message");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.KEY_SHOW_TRANSACTIONS,true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(Constants.NOTIFICATION_CHANNEL_ID);
        }
        notificationManager.notify(146, notificationBuilder.build());
    }

    private void handleNotificationData(RemoteMessage remoteMessage) {}

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        LogUtils.d(TAG,"## New token : "+token);
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.TOPICS.GENERAL_NOTIFICATIONS);
        persistToken(token);
    }

    public void persistToken(String token){ // probably,we should send this to the server
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString(TOKEN_SHARED_PREF_KEY,token).apply();
    }
}