package com.dans.apps.bitsa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dans.apps.bitsa.model.Semester;
import com.dans.apps.bitsa.model.User;
import com.dans.apps.bitsa.service.MpesaPaymentService;
import com.dans.apps.bitsa.utils.InputValidator;
import com.dans.apps.bitsa.utils.LogUtils;
import com.dans.apps.bitsa.utils.MpesaUtils;
import com.dans.apps.bitsa.utils.UiUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MpesaPaymentActivity extends AppCompatActivity implements ValueEventListener,
        View.OnClickListener, MpesaPaymentService.CallBack, ChipGroup.OnCheckedChangeListener {

    private final String TAG = "MpesaPaymentActivity";
    String HIDE_NOTICE_VIEW_PREFERENCE_KEY = "hide_notice";
    FirebaseUser user;
    ProgressBar progressBar;
    MaterialButton pay;
    DatabaseReference reference;
    DatabaseReference semesterReference;

    User currentUser;
    ScrollView paymentContainer;
    TextView message;
    TextInputEditText amount;

    TextView name;
    TextView email;
    TextView billingPhoneNumber;
    LinearLayout noticeView;
    ImageView closeNotice;
    Semester semester;
    static boolean proceedWithPayment = false;
    MpesaPaymentService mpesaService;
    private boolean serviceBound = false;
    SharedPreferences preferences;

    ChipGroup chipGroup;

    int transactionType = Constants.TRANSACTION_TYPE.UNKOWN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpesa_payment);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.mpesa);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pay = findViewById(R.id.action);
        progressBar = findViewById(R.id.progress_bar);
        user = FirebaseAuth.getInstance().getCurrentUser();
        paymentContainer = findViewById(R.id.payment_container);
        message = findViewById(R.id.message);
        amount = findViewById(R.id.amount);
        name = findViewById(R.id.name);
        email = findViewById(R.id.billing_email_address);
        billingPhoneNumber = findViewById(R.id.phone_number);
        noticeView = findViewById(R.id.notice_view);
        closeNotice = findViewById(R.id.close_notice);
        chipGroup = findViewById(R.id.chip_group);
        chipGroup.setOnCheckedChangeListener(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        closeNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeNotice();
            }
        });
        pay.setOnClickListener(this);
        //cold fetch user here
        fetchSemester();  //precedence matters here
        fetchUser();
        proceedWithPayment = false;

        boolean hidePreference = preferences.getBoolean(HIDE_NOTICE_VIEW_PREFERENCE_KEY,false);
        if(hidePreference){
            noticeView.setVisibility(View.GONE);
        }
    }


    private void closeNotice() {
        preferences.edit().putBoolean(HIDE_NOTICE_VIEW_PREFERENCE_KEY,true).apply();
        noticeView.setVisibility(View.GONE);
    }


    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this,MpesaPaymentService.class),serviceConnection,
                Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
        if(serviceBound) {
            unbindService(serviceConnection);
        }
        super.onStop();
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MpesaPaymentService.LocalBinder binder = (MpesaPaymentService.LocalBinder) service;
            mpesaService = binder.getService();
            mpesaService.fetchMpesaTransactionDetails();
            mpesaService.initCallback(MpesaPaymentActivity.this);
            serviceBound= true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
            mpesaService.initCallback(null);
            mpesaService =null;
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(reference!=null){
            reference.removeEventListener(this);
        }
        if(semesterReference!=null){
            semesterReference.removeEventListener(semesterEventListener);
        }
    }

    public void fetchUser(){
        showProgress(R.string.fetching_user);
        if(user==null){
            UiUtils.showErrorWithFinishAction(this,R.string.please_singout_and_singin);
            return;
        }
        reference = FirebaseDatabase.getInstance().getReference().
                child(Constants.PATHS.users);
        reference.orderByChild("email").equalTo(user.getEmail());
        reference.addValueEventListener(this);
    }

    private void fetchSemester() {
        semesterReference = FirebaseDatabase.getInstance().getReference(Constants.PATHS.semesterDetails);
        semesterReference.addValueEventListener(semesterEventListener);
    }

    private ValueEventListener semesterEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue()!=null){
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    semester = snapshot.getValue(Semester.class);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


    public void showProgress(int progressMessageResID){
        progressBar.setVisibility(View.VISIBLE);
        message.setVisibility(View.VISIBLE);
        message.setText(progressMessageResID);
        paymentContainer.setVisibility(View.GONE);
    }

    public void hideProgressDisplayMessage(int messageResID){
        progressBar.setVisibility(View.GONE);
        message.setVisibility(View.VISIBLE);
        message.setText(messageResID);
        paymentContainer.setVisibility(View.GONE);
    }

    public void hideProgress(){
        progressBar.setVisibility(View.GONE);
        message.setVisibility(View.GONE);
        message.setText("");
        paymentContainer.setVisibility(View.VISIBLE);
    }


    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if(isFinishing())return;
        if(dataSnapshot.getValue() == null){
            hideProgressDisplayMessage(R.string.error_occured);
            UiUtils.showErrorWithFinishAction(this,R.string.user_not_exist);
            return;
        }

        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
            currentUser = snapshot.getValue(User.class);
            currentUser.setId(snapshot.getKey());
        }

        if(currentUser.getType() == Constants.USER_TYPE.VISITOR){
            chipGroup.setVisibility(View.GONE);
            transactionType = Constants.TRANSACTION_TYPE.CONTRIBUTION;
        }else{
            chipGroup.setVisibility(View.VISIBLE);
            transactionType = Constants.TRANSACTION_TYPE.CLUB_PAYMENT;//default
        }
        populateFields(); ///populate the fields using the current user information
    }

    private void populateFields() {
        hideProgress();
        name.setText("Name: " + currentUser.getName());
        email.setText("Billing email: " + currentUser.getEmail());
        billingPhoneNumber.setText("Billing phone: ");
        isPhoneNumberValid();
    }

    public boolean isPhoneNumberValid(){
        if (TextUtils.isEmpty(currentUser.getPhoneNumber())) {
            showPhoneRelatedAlertDialog(R.string.no_phone_number_set);
            return false;
        } else {
            String sanitizedNumber = UiUtils.sanitizePhoneNumber(currentUser.getPhoneNumber());
            if (TextUtils.isEmpty(sanitizedNumber.trim())) {
                showPhoneRelatedAlertDialog(R.string.invalid_phone_number);
                return false;
            } else {
                billingPhoneNumber.setText("Billing phone: " + currentUser.getPhoneNumber());
                return true;
            }
        }
    }

    private void showPhoneRelatedAlertDialog(int stringRes){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.invalid_phone_number);
        builder.setMessage(stringRes);
        builder.setPositiveButton(R.string.go_to_settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MpesaPaymentActivity.this,SettingsActivity.class);
                intent.putExtra(SettingsActivity.KEY_CLASS,getClass().getName());
                MpesaPaymentActivity.this.startActivity(intent);
            }
        });
        builder.create().setCanceledOnTouchOutside(false);
        builder.show();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {}

    @Override
    public void onClick(View view) {
        startPaymentProcedure();
    }

    private void startPaymentProcedure() {
        String enteredAmount = amount.getText().toString();

        String fieldEmptyError = getResources().getString(R.string.field_cannot_be_empty);
        if(TextUtils.isEmpty(enteredAmount)){
            amount.setError(fieldEmptyError);
            return;
        }else{
            amount.setError(null);
        }

        if(!isPhoneNumberValid()){
            return;
        }

        if(transactionType == Constants.TRANSACTION_TYPE.UNKOWN){
            Toast.makeText(getApplicationContext(),R.string.select_transaction_type,Toast.LENGTH_SHORT).show();
            return;
        }
        int amountValue = Integer.valueOf(enteredAmount);
        if(!MpesaUtils.isRigthAmount(amountValue)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.invalid_amount);
            builder.setMessage(R.string.invalid_amount_info);
            builder.setPositiveButton(R.string.ok,null);
            builder.show();
            return;
        }

        String phoneNumber = currentUser.getPhoneNumber();
        if(!TextUtils.isEmpty(phoneNumber)){
            pay.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            phoneNumber = phoneNumber.replaceAll("\\+","").trim();
            mpesaService.initPayment(currentUser, phoneNumber, amountValue, transactionType, semester);

        }

    }

    public void hideProgressShowButton(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                pay.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onPreconditionCheckFailed(String message) {
        hideProgressShowButton();
        Toast.makeText(this,R.string.error_processing_mpesa_request,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNoInternetConnection() {
        hideProgressShowButton();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UiUtils.showNotConnectedAlert(MpesaPaymentActivity.this);
            }
        });

    }

    @Override
    public void onMpesaProcessRequestFailed(String message) {
        hideProgressShowButton();
        LogUtils.d(TAG,message);
        Toast.makeText(this,R.string.error_processing_mpesa_request,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMpesaProcessedRequest(String message) {
        //todo finish this part now
        LogUtils.d(TAG,message);
        hideProgressShowButton();
    }

    @Override
    public void onCheckedChanged(ChipGroup group, int checkedId) {
        if(checkedId == R.id.contribution){
            transactionType = Constants.TRANSACTION_TYPE.CONTRIBUTION;
        }else if (checkedId == R.id.club_fee_payment){
            transactionType = Constants.TRANSACTION_TYPE.CLUB_PAYMENT;
        }
    }
}
