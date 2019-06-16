package com.dans.apps.bitsa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dans.apps.bitsa.model.Announcement;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


public class NewAnnouncementActivity extends AppCompatActivity implements View.OnClickListener {

    TextInputEditText title;
    TextInputEditText body;
    ProgressBar progressBar;
    MaterialButton action;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.new_announcement);
        action = findViewById(R.id.action);
        progressBar = findViewById(R.id.progress_bar);
        title = findViewById(R.id.title);
        body = findViewById(R.id.body);

        progressBar.setVisibility(View.GONE);


        action.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String enteredTitle = title.getText().toString();
        String enteredBody = body.getText().toString();

        if(TextUtils.isEmpty(enteredTitle)){
            title.setError(getResources().getString(R.string.field_cannot_be_empty));
            return;
        }else{
            title.setError(null);
        }

        if(TextUtils.isEmpty(enteredBody)){
            body.setError(getResources().getString(R.string.field_cannot_be_empty));
            return;
        }else{
            body.setError(null);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){
            Toast.makeText(getApplicationContext(),R.string.error_occured,Toast.LENGTH_SHORT).show();
            finish();
            return;
        }else {
            progressBar.setVisibility(View.VISIBLE);
            action.setVisibility(View.GONE);
            Announcement announcement = new Announcement(enteredTitle,enteredBody,user.getDisplayName(),user.getEmail());
            FirebaseDatabase.getInstance().getReference().child(Constants.PATHS.announcements).push().
                    setValue(announcement).addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!isFinishing()) {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), R.string.announcement_sent, Toast.LENGTH_SHORT).show();
                            finish();
                        }else{
                            action.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }
}
