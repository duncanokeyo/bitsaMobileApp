package com.dans.apps.bitsa;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.dans.apps.bitsa.model.Role;
import com.dans.apps.bitsa.utils.SelfExpiringHashMap;

import java.util.ArrayList;

public class BitsaApplication extends Application {
    /**
     * This contains the key and token String
     */
    private SelfExpiringHashMap<Integer,String>map;

    static BitsaApplication application;

    ArrayList<Role>roles;

    public static BitsaApplication getApplication(){
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        map = new SelfExpiringHashMap<>();
        roles = new ArrayList<>();

        //todo, fetch data from the server
        roles.add(new Role(1,"President"));
        roles.add(new Role(2,"Vice President"));
        roles.add(new Role(3,"Executive Secretary"));
        roles.add(new Role(4,"Treasurer"));
        roles.add(new Role(5,"Assistant Treasurer"));
        roles.add(new Role(6,"Organizing Secretary"));
        roles.add(new Role(7,"BBIT Representative"));
        roles.add(new Role(8,"Public Relation"));
        roles.add(new Role(9,"Networking Representative"));
        roles.add(new Role(10,"Chaplain"));
        createNotificationChannel();
    }



    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID,
                    name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public String getToken(){
        if(map.containsKey(1))return map.get(1);
        return null;
    }

    public void putToken(String token,int seconds){
        map.put(1,token,seconds*1000);
    }
}
