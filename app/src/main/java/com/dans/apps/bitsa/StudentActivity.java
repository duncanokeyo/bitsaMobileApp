package com.dans.apps.bitsa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dans.apps.bitsa.model.User;
import com.dans.apps.bitsa.utils.InputValidator;
import com.dans.apps.bitsa.utils.UiUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener,
        View.OnClickListener,ValueEventListener {

    public static String KEY_ACTION = "action";
    public static String KEY_ID = "id";
    public String id;
    public int ACTION;
    public static int UNKOWN_MAJOR =-1;
    public static int ACTION_ADD = 1;
    public static int ACTION_UPDATE = 2;

    TextInputEditText name;
    TextInputEditText schoolId;
    TextInputEditText email;
    TextInputEditText phoneNumber;
    TextInputEditText role;
    TextInputLayout roleContainer;

    ProgressBar progressBar;
    MaterialButton save;
    Spinner major;

    ScrollView container;
    TextView message;
    User user;

    int selectedMajor=0;

    DatabaseReference studentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        container = findViewById(R.id.container);
        message = findViewById(R.id.message);
        name = findViewById(R.id.name);
        schoolId = findViewById(R.id.school_id);
        email = findViewById(R.id.email);
        phoneNumber = findViewById(R.id.phone_number);
        role = findViewById(R.id.role);
        roleContainer= findViewById(R.id.role_container);
        progressBar = findViewById(R.id.progress_bar);
        save = findViewById(R.id.save);
        major = findViewById(R.id.major);
        roleContainer.setVisibility(View.GONE);
        message.setVisibility(View.GONE);
        role.setFocusableInTouchMode(false);

        progressBar.setVisibility(View.GONE);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            ACTION = bundle.getInt(KEY_ACTION);

            if (ACTION == ACTION_UPDATE) {
                id = bundle.getString(KEY_ID);
                getSupportActionBar().setTitle(R.string.update_student_details);
            } else {
                getSupportActionBar().setTitle(R.string.add_student);
            }
        }
        if(ACTION == ACTION_UPDATE){
            fetchStudent();
        }

        save.setOnClickListener(this);
        major.setOnItemSelectedListener(this);
    }

    private void fetchStudent() {
        showHideFetching(true);
        studentReference=FirebaseDatabase.getInstance().getReference(Constants.PATHS.users).child(id);
        studentReference.addValueEventListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(studentReference!=null){
            studentReference.removeEventListener(this);
        }
    }

    private void showHideFetching(boolean show) {
        if(show){
            progressBar.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);
            message.setText(R.string.fetching_student);
            message.setVisibility(View.VISIBLE);
        }else{
            progressBar.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            message.setText("");
            message.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedMajor = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public void onClick(View v) {
    }

    public void validateInput(){

        String enteredName = name.getText().toString();
        String enteredSchoolId = schoolId.getText().toString();
        String enteredEmail = email.getText().toString();
        String enteredPhone = phoneNumber.getText().toString();
        String enteredMajor = Constants.STUDENT_MAJOR_ARRAY[selectedMajor];

        String emptyError = getResources().getString(R.string.field_cannot_be_empty);
        if(TextUtils.isEmpty(enteredName)){
            name.setError(emptyError);
            return;
        }else{
            name.setError(null);
        }
        if(TextUtils.isEmpty(enteredSchoolId)){
            schoolId.setError(emptyError);
            return;
        }else{
            schoolId.setError(null);
        }
        if(TextUtils.isEmpty(enteredEmail)){
            email.setError(emptyError);
            return;
        }else{
            email.setError(null);
        }

        if(TextUtils.isEmpty(enteredPhone)){
            phoneNumber.setError(emptyError);
            return;
        }else{
            phoneNumber.setError(null);
        }

        if(!InputValidator.isValidEmail(enteredEmail)){
            email.setError(getResources().getString(R.string.fui_invalid_email_address));
            return;
        }
        if(!InputValidator.isValidMobile(enteredPhone)){
            phoneNumber.setError(getResources().getString(R.string.fui_invalid_phone_number));
            return;
        }

        checkUserExists(enteredName,enteredSchoolId,enteredEmail,enteredPhone,enteredMajor);
    }

    private void checkUserExists(final String name,
                                 final String schoolID,
                                 final String email,
                                 final String phone,
                                 final String major) {

        save.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        //first validate if the user with the given id exists
         DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.PATHS.users);
         reference.orderByChild("schoolID").equalTo(schoolID);

         reference.addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if(!isFinishing()){
                     if(dataSnapshot.getValue()!=null){
                         save.setVisibility(View.VISIBLE);
                         progressBar.setVisibility(View.GONE);
                         AlertDialog.Builder builder= new AlertDialog.Builder(StudentActivity.this);
                         builder.setMessage(R.string.student_with_id_exists);
                         builder.show();
                     }else{
                         saveUser(name,schoolID,email,phone,major);
                     }
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {}
         });

    }

    private void saveUser(String name, String schoolID, String email, String phone, String major) {
        phone = "+254"+phone;
        User user = new User(Constants.USER_TYPE.STUDENT,name,schoolID,major,phone,email,Constants.ROLES.NO_ROLE);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.PATHS.users);
        reference.push().setValue(user).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(isFinishing())return;
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),R.string.successfully_added_student,Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(),R.string.error_occured,Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    save.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        showHideFetching(false);
        if(dataSnapshot.getValue()!=null){
            for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                user = snapshot.getValue(User.class);
            }
        }

        if(user!=null){
            name.setText(user.getName());
            schoolId.setText(user.getId());
            email.setText(user.getEmail());
            if(user.getPhoneNumber()!=null){
                phoneNumber.setText(UiUtils.phoneNumberMinusCode(user.getPhoneNumber()));
            }


        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
