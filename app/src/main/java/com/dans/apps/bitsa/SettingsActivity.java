package com.dans.apps.bitsa;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.dans.apps.bitsa.model.Contact;
import com.dans.apps.bitsa.model.User;
import com.dans.apps.bitsa.ui.EnterPhoneNumberDialogFragment;
import com.dans.apps.bitsa.utils.LogUtils;
import com.dans.apps.bitsa.utils.UiUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;

public class SettingsActivity extends AppCompatActivity {

    public static final String KEY_PHONE_NUMBER = "billing_phone_number";
    public static final String KEY_PRIVACY_POLICY="privacy_policy";
    public static final String KEY_CONTACT_US="contact_us";

    public static final String KEY_CLASS = "class";
    String fromClass;
    FrameLayout container;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Toolbar toolbar = findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.settings);
        container = findViewById(R.id.container);

        if(savedInstanceState == null){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container,new SettingsFragment());
            transaction.commit();
        }
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            fromClass = bundle.getString(KEY_CLASS);
        }

    }

    public static class SettingsFragment extends androidx.preference.PreferenceFragmentCompat{
        String TAG = "SettingsFragment";

        Preference phoneNumber;
        Preference privacyPolicy;
        Preference contactUs;

        DatabaseReference phoneNumberReference;
        DatabaseReference contactReference;
        Contact contacts;
        String userPhoneNumber;
        FirebaseAuth auth;
        User user;
        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            auth = FirebaseAuth.getInstance();
            contacts = new Contact();
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences,rootKey);
            phoneNumber =findPreference(KEY_PHONE_NUMBER);
            privacyPolicy = findPreference(KEY_PRIVACY_POLICY);
            contactUs = findPreference(KEY_CONTACT_US);

            if(userPhoneNumber!=null){
                phoneNumber.setSummary(userPhoneNumber);
            }

            phoneNumber.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    FragmentManager manager = getFragmentManager();
                    EnterPhoneNumberDialogFragment fragment = EnterPhoneNumberDialogFragment.newInstance(new EnterPhoneNumberDialogFragment.onAction() {
                        @Override
                        public void onCancelled() {
                            Toast.makeText(getActivity(),R.string.error_occured,Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onSuccess() {
                            Toast.makeText(getActivity(),R.string.success,Toast.LENGTH_SHORT).show();
                        }
                    },user);
                    fragment.show(manager,"enter_phone_number_dialog_fragment");
                    return false;
                }
            });

            privacyPolicy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    try{
                        Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(Constants.PRIVACY_POLICY_URL));
                        startActivity(intent);
                    }catch (Exception ignored){}
                    return true;
                }
            });

            contactUs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    String []choice = {"Call","Email"};
                    builder.setItems(choice, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0:
                                    if((contacts == null) || (contacts!=null && TextUtils.isEmpty(contacts.getPhone()))){
                                        Toast.makeText(getActivity(),R.string.error_occured,Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    try {
                                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contacts.getPhone()));
                                        startActivity(intent);
                                    }catch (Exception e){
                                        Toast.makeText(getActivity(),R.string.phone_call_failed,Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case 1:
                                    if((contacts == null)||(contacts!=null && TextUtils.isEmpty(contacts.getEmail()))){
                                        Toast.makeText(getActivity(),R.string.error_occured,Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    try{
                                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                                "mailto",contacts.getEmail(), null));
                                        startActivity(Intent.createChooser(emailIntent, "Send email..."));
                                    }catch (Exception e){
                                        Toast.makeText(getActivity(),R.string.email_send_failed,Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                            }
                        }
                    });
                    builder.show();
                    return false;
                }
            });
        }

        @Override
        public void onStart() {
            super.onStart();
            if(auth.getCurrentUser()!=null) {
                phoneNumberReference = FirebaseDatabase.getInstance().
                        getReference().child(Constants.PATHS.users);
                contactReference = FirebaseDatabase.getInstance().getReference(Constants.PATHS.contacts);
                contactReference.addValueEventListener(contactListener);
                phoneNumberReference.orderByChild("email").
                        equalTo(auth.getCurrentUser().getEmail()).
                        addValueEventListener(PhoneNumberListener);
            }
        }


        private ValueEventListener contactListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    contacts = snapshot.getValue(Contact.class);
                    contacts.setId(snapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        private ValueEventListener PhoneNumberListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LogUtils.d(TAG,"phone number is "+dataSnapshot.toString());
                if(dataSnapshot.getValue()!=null) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        user = child.getValue(User.class);
                        user.setId(child.getKey());
                        userPhoneNumber = user.getPhoneNumber();
                        if (isAdded()) {
                            phoneNumber.setSummary(userPhoneNumber);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        @Override
        public void onStop() {
            super.onStop();
            phoneNumberReference.removeEventListener(PhoneNumberListener);
        }

    }


    @Override
    public Intent getSupportParentActivityIntent() {
        return getParentActivityIntentImpl();
    }
    @Override
    public Intent getParentActivityIntent() {
        return getParentActivityIntentImpl();
    }
    private Intent getParentActivityIntentImpl() {
        Class<?> clazz = null;
        Intent intent = null;
        try {
            clazz = Class.forName(fromClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(clazz!=null){
            intent = new Intent(this,clazz);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        return intent;
    }
}
