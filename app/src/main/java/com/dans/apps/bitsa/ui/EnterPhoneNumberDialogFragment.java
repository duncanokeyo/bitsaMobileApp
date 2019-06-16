package com.dans.apps.bitsa.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.dans.apps.bitsa.Constants;
import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.model.User;
import com.dans.apps.bitsa.utils.InputValidator;
import com.dans.apps.bitsa.utils.UiUtils;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class EnterPhoneNumberDialogFragment extends DialogFragment {

    public interface onAction{
        void onCancelled();
        void onSuccess();
    }

    private static onAction callback;
    private static boolean shouldUpdate;
    private static User user;

    public  static EnterPhoneNumberDialogFragment newInstance(onAction action,User currentUser){
        callback = action;
        user = currentUser;
        return new EnterPhoneNumberDialogFragment();
    }
    SharedPreferences preferences;
    ProgressBar progressBar;
    TextView countryCode;
    EditText phoneNumber;
    FirebaseAuth auth;
    DatabaseReference users;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        users = FirebaseDatabase.getInstance().getReference().child("Users");
        auth = FirebaseAuth.getInstance();
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.enter_phone_number);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_enter_phone_number_dialog,null);

        phoneNumber = view.findViewById(R.id.phone_number);
        countryCode = view.findViewById(R.id.county_code);
        progressBar = view.findViewById(R.id.progress_bar);

        builder.setView(view);


        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(callback!=null){
                    callback.onCancelled();
                    dismissAllowingStateLoss();
                }
            }
        });

        builder.setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String number = phoneNumber.getText().toString();
                if(TextUtils.isEmpty(number)){
                    phoneNumber.setError(getResources().getString(R.string.field_cannot_be_empty));
                    return;
                }
                if(!InputValidator.isValidMobile(number)){
                    phoneNumber.setError(getResources().getString(R.string.invalid_phone_number));
                    return;
                }
                number = "+254"+number; //appending kenya country code to the number.

                createUser(number);
            }
        });


        return builder.create();
    }

    private void createUser(final String number) {
        if(!UiUtils.isOnline(getActivity())){
            UiUtils.showNotConnectedAlert(getActivity(), new UiUtils.onAction() {
                @Override
                public void onCanceled() {
                    callback.onCancelled();
                    dismissAllowingStateLoss();
                }

                @Override
                public void onOkClicked() {
                    callback.onCancelled();
                    dismissAllowingStateLoss();
                }
            });

            return;
        }

        final FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser!=null) {
            showProgressBar();

            if(!shouldUpdate) {
                Map<String,Object> update = new HashMap<>();
                update.put("phoneNumber",number);
                FirebaseDatabase.getInstance().getReference().child(Constants.PATHS.users).child(user.getId()).updateChildren(update).
                        addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideProgressBar();
                        callback.onSuccess();
                        dismissAllowingStateLoss();
                    }
                }).addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressBar();
                        Toast.makeText(getActivity(), R.string.error_occured, Toast.LENGTH_SHORT).show();
                        callback.onCancelled();
                        dismissAllowingStateLoss();
                    }
                }).addOnCanceledListener(getActivity(), new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        hideProgressBar();
                        Toast.makeText(getActivity(), R.string.error_occured, Toast.LENGTH_SHORT).show();
                        callback.onCancelled();
                        dismissAllowingStateLoss();
                    }
                });
            }
        }else{
            Toast.makeText(getActivity(),R.string.error_occured,Toast.LENGTH_SHORT).show();
            callback.onCancelled();
            dismissAllowingStateLoss();
        }
    }

    private void showProgressBar(){
        countryCode.setVisibility(View.GONE);
        phoneNumber.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        progressBar.setVisibility(View.GONE);
        phoneNumber.setVisibility(View.VISIBLE);
        countryCode.setVisibility(View.VISIBLE);
    }
}
