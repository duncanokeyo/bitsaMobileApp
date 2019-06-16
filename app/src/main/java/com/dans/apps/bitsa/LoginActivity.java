package com.dans.apps.bitsa;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.dans.apps.bitsa.utils.UiUtils;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 134;

    MaterialCardView bitsaOfficialCard;
    MaterialCardView studentCard;
    MaterialCardView visitorCard;


    ProgressBar progressBar;
    RelativeLayout base;

    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        base = findViewById(R.id.base);
        studentCard = findViewById(R.id.student_card);
        bitsaOfficialCard = findViewById(R.id.bitsa_official_card);
        visitorCard = findViewById(R.id.visitor_card);
        progressBar = findViewById(R.id.progress_bar);
        studentCard.setOnClickListener(this);
        progressBar.setVisibility(View.GONE);
        bitsaOfficialCard.setOnClickListener(this);
        visitorCard.setOnClickListener(this);
    }


    public void hideSystemUI(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.student_card:{
                proceedWithLogin(false,false);
                break;
            }
            case R.id.bitsa_official_card:{
                proceedWithLogin(true,false);
                break;
            }
            case R.id.visitor_card:{
                proceedWithLogin(false,true);
                break;
            }
        }
    }

    private void proceedWithLogin(final boolean isClubOfficial,final boolean isVisitor) {
        progressBar.setVisibility(View.GONE);
        if(!UiUtils.isOnline(this)){
            UiUtils.showNotConnectedAlert(this);
        }else {
            if(!isVisitor) {
                Intent intent = new Intent(this, VerificationWithSchoolIDSignInActivity.class);
                intent.putExtra(Constants.KEY_USER_TYPE,
                        isClubOfficial ? Constants.USER_TYPE.CLUB_OFFICIAL : Constants.USER_TYPE.STUDENT);
                startActivityForResult(intent, RC_SIGN_IN);
            }else{
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder().
                                setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                .setLogo(R.drawable.bitsa_300)
                                .setTheme(R.style.Login)
                                .setAvailableProviders(providers)
                                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                .build(),
                        RC_SIGN_IN);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        progressBar.setVisibility(View.GONE);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                checkUserExists();
            } else {
                if (data!=null) {
                    showSnackbar(R.string.sign_in_cancelled);
                    return;
                }
            //    if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
             //       showSnackbar(R.string.no_internet_connection);
              //      return;
               // }
                showSnackbar(R.string.unknown_error);
            }
        }

        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();

    }

    private void checkUserExists() {

    }

    private void showSnackbar(int stringRes) {
        Snackbar snackbar = Snackbar.make(base, stringRes, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

}
