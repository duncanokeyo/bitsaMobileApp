package com.dans.apps.bitsa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.dans.apps.bitsa.model.User;
import com.dans.apps.bitsa.utils.InputValidator;
import com.dans.apps.bitsa.utils.LogUtils;
import com.dans.apps.bitsa.utils.TextHelper;
import com.dans.apps.bitsa.utils.UiUtils;
import com.firebase.ui.auth.FirebaseAuthAnonymousUpgradeException;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rilixtech.CountryCodePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VerificationWithSchoolIDSignInActivity
        extends AppCompatActivity implements View.OnClickListener {

    String TAG = "VerificationWithSchoolIDSignInActivity";

    private final int EMAIL_SCREEN = 0;
    private final int USER_DETAILS_SCREEN = 1;
    private final int WELCOME_BACK_SCREEN = 2;

    ViewFlipper viewFlipper;
    ProgressBar progressBar;
    TextInputEditText email;
    TextInputEditText name;
    TextInputEditText schoolID;
    TextInputEditText password;
    TextInputEditText phoneNumber;

    MaterialButton action;
    FirebaseAuth auth;
    DatabaseReference reference;
    String enteredEmail;
    TextView welcomeBackBody;
    TextInputEditText welcomeBackPassword;
    TextView troubleSigningIn;
    User user;

    int userType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_sign_in);

        viewFlipper = findViewById(R.id.view_flipper);
        progressBar = findViewById(R.id.progress_bar);
        email = findViewById(R.id.email);
        name = findViewById(R.id.name);
        schoolID = findViewById(R.id.school_id);
        password = findViewById(R.id.password);
        action = findViewById(R.id.action);
        phoneNumber = findViewById(R.id.phone_number);

        name.setVisibility(View.GONE);
        welcomeBackBody = findViewById(R.id.welcome_back_password_body);
        welcomeBackPassword = findViewById(R.id.welcome_back_password);
        troubleSigningIn = findViewById(R.id.trouble_signing_in);
        troubleSigningIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword();
            }
        });

        name.setEnabled(false);
        action.setOnClickListener(this);
        progressBar.setVisibility(View.GONE);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child(Constants.PATHS.users);
        setActionButtonText();

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            userType = bundle.getInt(Constants.KEY_USER_TYPE);
        }
    }

    private void forgotPassword() {
        auth.sendPasswordResetEmail(enteredEmail).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showEmailSentDialog(enteredEmail);
                }else{
                    Toast.makeText(VerificationWithSchoolIDSignInActivity.this,
                            R.string.error_sending_email,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showEmailSentDialog(String email) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.fui_title_confirm_recover_password)
                .setMessage(getString(R.string.fui_confirm_recovery_body, email))
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                })
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }


    public void setActionButtonText(){
        View view = viewFlipper.getCurrentView();
        if(view instanceof TextInputLayout){
            action.setText(R.string.proceed);
        }else if(view instanceof LinearLayout){
            action.setText(R.string.save);
        }else if(view instanceof ScrollView){
            action.setText(R.string.sign_in);
        }
    }

    @Override
    public void onClick(View v) {
        View view = viewFlipper.getCurrentView();
        if(view instanceof TextInputLayout){
            checkUserExists();
        }else if (view instanceof LinearLayout){
            saveUser();
        }else if(view instanceof ScrollView){
            signIn();
        }
    }

    private void signIn() {
        final String password = welcomeBackPassword.getText().toString();
        if(TextUtils.isEmpty(password)){
            welcomeBackPassword.setError(getResources().getString(R.string.field_cannot_be_empty));
            return;
        }else {
            welcomeBackPassword.setError(null);
        }

        progressBar.setVisibility(View.VISIBLE);
        action.setVisibility(View.GONE);

        auth.signInWithEmailAndPassword(enteredEmail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    setResult(RESULT_OK);
                    Intent intent = new Intent(VerificationWithSchoolIDSignInActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    progressBar.setVisibility(View.GONE);
                    action.setVisibility(View.VISIBLE);
                    Exception exception = task.getException();
                    if(exception instanceof FirebaseAuthInvalidCredentialsException){
                        welcomeBackPassword.setError(getResources().getString(R.string.fui_error_invalid_password));
                    }else{
                        Toast.makeText(getApplicationContext(),R.string.error_occured,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void saveUser() {
        String enteredName=name.getText().toString();
        String enteredSchoolID=schoolID.getText().toString();
        String enteredPassword=password.getText().toString();
        String enteredPhoneNumber = phoneNumber.getText().toString();

        String fieldEmptyError = getResources().getString(R.string.field_cannot_be_empty);
       // if(TextUtils.isEmpty(enteredName)){
       //     name.setError(fieldEmptyError);
       //     return;
       // }else{
       //     name.setError(null);
       // }
        //if(TextUtils.isEmpty(enteredPhoneNumber)){
        //    phoneNumber.setError(fieldEmptyError);
       // }

        if(!TextUtils.isEmpty(enteredPhoneNumber)){
            if(!InputValidator.isValidMobile(enteredPhoneNumber)){
                phoneNumber.setError(getResources().getString(R.string.fui_invalid_phone_number));
            }else{
                phoneNumber.setError(null);
            }
        }

        if(TextUtils.isEmpty(enteredSchoolID)){
            schoolID.setError(fieldEmptyError);
            return;
        }else{
            schoolID.setError(null);
        }
        if(TextUtils.isEmpty(enteredPassword)){
            password.setError(fieldEmptyError);
            return;
        }else{
            password.setError(null);
        }

       /* if(enteredName.length()<=4){
            name.setError(getResources().getString(R.string.invalid_length));
            return;
        }else{
            name.setError(null);
        }*/
        if(enteredPassword.length()<6){
            password.setError(getResources().getString(R.string.strong_password_prompt));
            return;
        }else{
            password.setError(null);
        }

        progressBar.setVisibility(View.VISIBLE);

        checkIfStudentWithSchoolIDExists(enteredName,enteredPassword,enteredSchoolID,enteredPhoneNumber);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LogUtils.d(TAG,"on back pressed");
        Intent intent = new Intent();
        intent.putExtra("cancelled",true);
        setResult(10,intent);
    }

    private void checkIfStudentWithSchoolIDExists(final String userName, final String password,
                                                  final String schoolID,final String phoneNumber) {
        reference.orderByChild("schoolID").equalTo(schoolID).
                addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(VerificationWithSchoolIDSignInActivity.this);
                    builder.setMessage(R.string.student_with_given_id_not_exist);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(10);
                            finish();
                        }
                    });
                    builder.show();
                }else{
                    name.setVisibility(View.VISIBLE);
                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                        user = snapshot.getValue(User.class);
                        user.setId(snapshot.getKey());
                        name.setText(user.getName());
                        if(user.getPhoneNumber()!=null){
                            VerificationWithSchoolIDSignInActivity.this.phoneNumber.
                                    setText(UiUtils.sanitizePhoneNumber(user.getPhoneNumber()));
                        }
                    }

                    saveUser(name.getText().toString(),
                            password,
                            schoolID,
                            VerificationWithSchoolIDSignInActivity.this.phoneNumber.getText().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VerificationWithSchoolIDSignInActivity.this);
                builder.setMessage(R.string.error_occured);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(10);
                        finish();
                    }
                });
                builder.show();
            }
        });
    }

    private void saveUser(final String enteredName,
                          final String enteredPassword,
                          final String enteredSchoolID,
                          final String phoneNumber) {
        auth.createUserWithEmailAndPassword(enteredEmail,enteredPassword).continueWithTask(new Continuation<AuthResult, Task<AuthResult>>() {
            @Override
            public Task<AuthResult> then(@NonNull Task<AuthResult> task) throws Exception {
                final AuthResult authResult = task.getResult();
                //todo, should this be put here...
                FirebaseUser firebaseUser = authResult.getUser();
                User update = new User(userType,enteredName,enteredSchoolID,"",phoneNumber,enteredEmail,Constants.ROLES.NO_ROLE);
                Map<String,Object>updateFields = user.setUpdateFields(update);
                FirebaseDatabase.getInstance().
                        getReference().child(Constants.PATHS.users).
                        child(user.getId()).updateChildren(updateFields);

                return firebaseUser.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(null).
                        setDisplayName(enteredName).build()).
                        addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                setResult(RESULT_OK);//even if there is an error adding this name,we just p
                                finish();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        setResult(RESULT_OK);
                        finish();
                    }
                }).continueWithTask(new Continuation<Void, Task<AuthResult>>() {
                    @Override
                    public Task<AuthResult> then(@NonNull Task<Void> task) throws Exception {
                        return Tasks.forResult(authResult);
                    }
                });
            }
        });

        auth.createUserWithEmailAndPassword(enteredEmail,enteredPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    setResult(RESULT_OK);
                }else{
                    Exception exception = task.getException();

                    if (exception instanceof FirebaseAuthWeakPasswordException) {
                        password.setError(getResources().getQuantityString(
                                R.plurals.fui_error_weak_password,
                                R.integer.fui_min_password_length));
                    } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                        email.setError(getString(R.string.fui_invalid_email_address));
                    } else if (exception instanceof FirebaseAuthAnonymousUpgradeException) {
                    } else {
                        // General error message, this branch should not be invoked but
                        // covers future API changes
                        email.setError(getString(R.string.fui_email_account_creation_error));
                    }

                }
            }
        });
    }

    private void checkUserExists() {
        progressBar.setVisibility(View.VISIBLE);
        enteredEmail = email.getText().toString();
        if(TextUtils.isEmpty(enteredEmail)){
            email.setError(getResources().getString(R.string.field_cannot_be_empty));
            return;
        }

        if(!InputValidator.isValidEmail(enteredEmail)){
            email.setError(getResources().getString(R.string.fui_invalid_email_address));
            return;
        }

        auth.fetchSignInMethodsForEmail(enteredEmail).addOnCompleteListener(this, new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                progressBar.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    List<String>methods = task.getResult().getSignInMethods();
                    if(methods == null){
                        methods = new ArrayList<>();
                    }

                    LogUtils.d(TAG,"methods size -- > "+methods.size());
                    if(methods.isEmpty()){
                        //this means that there is no user with the email. user does not exist
                        proceedToDetailsEntryScreen();
                    }else {
                        enterPasswordAndSignIn();
                        //user exists
                        //todo we show the screen for login ....
                    }
                }else{
                    if(task.getException()!=null){
                        task.getException().printStackTrace();
                    }
                    setResult(10);
                    finish();
                }
            }
        });

    }

    private void enterPasswordAndSignIn() {
        switchToView(WELCOME_BACK_SCREEN);
        String bodyText =
                getString(R.string.fui_welcome_back_password_prompt_body, enteredEmail);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(bodyText);
        TextHelper.boldAllOccurencesOfText(spannableStringBuilder, bodyText, enteredEmail);
        welcomeBackBody.setText(spannableStringBuilder);

    }

    private void proceedToDetailsEntryScreen() {
        progressBar.setVisibility(View.GONE);
        action.setText(R.string.save);
        switchToView(USER_DETAILS_SCREEN);
    }

    public void switchToView(int child){
        if(viewFlipper.isFlipping()){
            viewFlipper.stopFlipping();
        }
        viewFlipper.setDisplayedChild(child);
    }
}
