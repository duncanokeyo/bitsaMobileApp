package com.dans.apps.bitsa;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dans.apps.bitsa.model.Announcement;
import com.dans.apps.bitsa.model.Member;
import com.dans.apps.bitsa.model.Project;
import com.dans.apps.bitsa.utils.InputValidator;
import com.dans.apps.bitsa.utils.LogUtils;
import com.dans.apps.bitsa.utils.UiUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rilixtech.CountryCodePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.validation.Validator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class ProjectActivity extends AppCompatActivity implements
        View.OnClickListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener,ValueEventListener {

    public String TAG = "ProjectActivity";

    public static String KEY_ACTION = "action";
    public static String KEY_PROJECT_ID = "project_id";

    public static String KEY_PROJECT_TYPE = "project_type";


    public static int PROJECT_TYPE = 3;
    public static int ACTION_ADD = 1;
    public static int ACTION_UPDATE = 2;

    public int ACTION;
    public String projectID;

    TextInputEditText title;
    TextInputEditText description;
    TextInputEditText startDate;
    TextInputEditText endDate;
    TextInputEditText url;
    TextInputEditText failureReason;
    TextInputLayout failureReasonContainer;
    Spinner projectStatus;

    int selectedProjectStatus = Constants.PROJECT_STATUS.ONGOING;

    ProgressBar progressBar;
    MaterialButton action;

    LinearLayout projectMembersContainer;
    TextView addProjectMember;
    Project project;
    LinearLayout container;
    List<String> projectMembers = new ArrayList<>();

    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_update_project);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        action = findViewById(R.id.action);
        container = findViewById(R.id.main_container);
        progressBar = findViewById(R.id.progress_bar);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        startDate = findViewById(R.id.start_date);
        endDate = findViewById(R.id.end_date);
        startDate.setFocusableInTouchMode(false);
        endDate.setFocusableInTouchMode(false);
        url = findViewById(R.id.url);
        failureReason = findViewById(R.id.failure_reason);
        failureReasonContainer = findViewById(R.id.failure_reason_container);
        projectStatus = findViewById(R.id.project_status);

        progressBar.setVisibility(View.GONE);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            ACTION = bundle.getInt(KEY_ACTION);
            PROJECT_TYPE = bundle.getInt(KEY_PROJECT_TYPE);

            if (ACTION == ACTION_UPDATE) {
                projectID = bundle.getString(KEY_PROJECT_ID);
                getSupportActionBar().setTitle(R.string.update_project);
            } else {
                getSupportActionBar().setTitle(R.string.add_project);
            }
        }

        projectMembersContainer = findViewById(R.id.project_members_container);
        addProjectMember = findViewById(R.id.add_project_members);
        addProjectMember.setOnClickListener(this);
        startDate.setOnClickListener(this);
        endDate.setOnClickListener(this);

     //   projectStatus.setAdapter(new ArrayAdapter<>(this,
       //         android.R.layout.simple_spinner_item, Constants.PROJECT_STATUS_ARRAY));
        failureReasonContainer.setVisibility(View.GONE);
        projectStatus.setOnItemSelectedListener(this);
     //   projectStatus.setOnItemClickListener(this);
        action.setOnClickListener(this);

        if(ACTION == ACTION_UPDATE){
            setViewState(false);
            fetchProject();
        }
    }

    private void setViewState(boolean show) {
        for(int i = 0;i<container.getChildCount();i++){
            container.getChildAt(i).setVisibility(show?View.VISIBLE:View.GONE);
        }
    }

    private void fetchProject() {
        String path = null;
        if (PROJECT_TYPE == Constants.PROJECT_TYPES.BITSA_PROJECT) {
            path = Constants.PATHS.bitsaProjects;
        } else if (PROJECT_TYPE == Constants.PROJECT_TYPES.PERSONAL_PROJECTS) {
            path = Constants.PATHS.studentProjects;
        } else if (PROJECT_TYPE == Constants.PROJECT_TYPES.SENIOR_PROJECTS) {
            path = Constants.PATHS.seniorYearProjects;
        }
        reference = FirebaseDatabase.getInstance().getReference().
                child(path).child(projectID);
        reference.addValueEventListener(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(reference!=null){
            reference.removeEventListener(this);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedProjectStatus = position;
        if (position == Constants.PROJECT_STATUS.FAILED) {
            failureReasonContainer.setVisibility(View.VISIBLE);
        } else {
            failureReasonContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.add_project_members: {
                createNewProjectMemberView();
                break;
            }
            case R.id.action: {
                startValidation();
                break;
            }
            case R.id.start_date:
            case R.id.end_date: {
                final Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                                String dateValue = year + "-" + monthOfYear + 1 + "-" + dayOfMonth;
                                if (v.getId() == R.id.start_date) {
                                    startDate.setText(dateValue);
                                } else {
                                    endDate.setText(dateValue);
                                }
                            }
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        }
    }

    private void startValidation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getApplicationContext(), R.string.error_occured, Toast.LENGTH_SHORT);
            finish();
            return;
        }

        String titleValue = title.getText().toString();
        String descriptionValue = description.getText().toString();
        String startDateValue = startDate.getText().toString();
        String endDateValue = endDate.getText().toString();
        String urlValue = url.getText().toString();
        String failureReasonValue = failureReason.getText().toString();

        if (TextUtils.isEmpty(titleValue)) {
            title.setError(getResources().getString(R.string.field_cannot_be_empty));
            return;
        } else {
            title.setError(null);
        }
        if (TextUtils.isEmpty(descriptionValue)) {
            description.setError(getResources().getString(R.string.field_cannot_be_empty));
            return;
        } else {
            description.setError(null);
        }
        if (TextUtils.isEmpty(startDateValue)) {
            startDate.setError(getResources().getString(R.string.field_cannot_be_empty));
            return;
        } else {
            startDate.setError(null);
        }
        if (TextUtils.isEmpty(endDateValue)) {
            endDate.setError(getResources().getString(R.string.field_cannot_be_empty));
            return;
        } else {
            endDate.setError(null);
        }

        if (!TextUtils.isEmpty(urlValue)) {
            if (!InputValidator.isValidUrl(urlValue)) {
                url.setError(getResources().getString(R.string.invalid_url));
                return;
            } else {
                url.setError(null);
            }
        }

        if (selectedProjectStatus == Constants.PROJECT_STATUS.FAILED) {
            if (TextUtils.isEmpty(failureReasonValue)) {
                failureReason.setError(getResources().getString(R.string.field_cannot_be_empty));
                return;
            } else {
                if (failureReasonValue.length() < 10) {
                    failureReason.setError(getResources().getString(R.string.failure_reason_wrong_length));
                    return;
                }
                failureReason.setError(null);
            }
        }


        for (int i = 0; i < projectMembersContainer.getChildCount(); i++) {
            View view = projectMembersContainer.getChildAt(i);
            TextInputEditText name = view.findViewById(R.id.member_name);
            CountryCodePicker picker = view.findViewById(R.id.country_code);
            TextInputEditText phoneNumber = view.findViewById(R.id.member_phone_number);
            TextInputEditText email = view.findViewById(R.id.member_email);
            TextInputEditText role = view.findViewById(R.id.member_role);

            String nameValue = name.getText().toString();
            String phoneNumberValue = phoneNumber.getText().toString();
            String emailValue = email.getText().toString();
            String roleValue = role.getText().toString();

            if (TextUtils.isEmpty(nameValue)) {
                name.setError(getResources().getString(R.string.field_cannot_be_empty));
                return;
            }

            if (TextUtils.isEmpty(roleValue)) {
                role.setError(getResources().getString(R.string.field_cannot_be_empty));
                return;
            }

            if (!phoneNumberValue.isEmpty()) {
                if (!InputValidator.isValidMobile(phoneNumberValue)) {
                    phoneNumber.setError(getResources().getString(R.string.invalid_phone_number));
                    return;
                } else {
                    phoneNumberValue = picker.getSelectedCountryCodeWithPlus() + phoneNumberValue;
                }
            } else {
                phoneNumberValue = " ";
            }

            if (!emailValue.isEmpty()) {
                if (!InputValidator.isValidEmail(emailValue)) {
                    email.setError(getResources().getString(R.string.fui_invalid_email_address));
                    return;
                }
            } else {
                emailValue = " ";
            }
            /*for(String member:projectMembers){
                String string [] =member.split(":");
                String first = string[0].trim();
                String second = string[1].trim();
                String third = string[2].trim();

                if(first.equalsIgnoreCase(nameValue)){
                    name.setError(getResources().getString(R.string.member_with_given_name_exists));
                    return;
                }else{
                    name.setError(null);
                }

                if(second.equalsIgnoreCase(phoneNumberValue)){
                    phoneNumber.setError(getResources().getString(R.string.member_with_given_number_exists));
                    return;
                }else{
                    phoneNumber.setError(null);
                }

                if(third.equalsIgnoreCase(emailValue)){
                    email.setError(getResources().getString(R.string.member_with_given_email_exists));
                    return;
                }else{
                    email.setError(null);
                }

            }*/

            String projectMember = nameValue + ":" + phoneNumberValue + ":" + emailValue + ":" + roleValue;
            projectMembers.add(projectMember);
        }

        String addedBy = user.getDisplayName();
        Project project = new Project(user.getEmail(),addedBy, titleValue, descriptionValue, selectedProjectStatus, urlValue, projectMembers,
                failureReasonValue, startDateValue, endDateValue);
        String path = null;
        if (PROJECT_TYPE == Constants.PROJECT_TYPES.BITSA_PROJECT) {
            path = Constants.PATHS.bitsaProjects;
        } else if (PROJECT_TYPE == Constants.PROJECT_TYPES.PERSONAL_PROJECTS) {
            path = Constants.PATHS.studentProjects;
        } else if (PROJECT_TYPE == Constants.PROJECT_TYPES.SENIOR_PROJECTS) {
            path = Constants.PATHS.seniorYearProjects;
        }

        if(ACTION == ACTION_ADD) {
            action.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            FirebaseDatabase.getInstance().getReference().child(path).
                    push().setValue(project).addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!isFinishing()) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), R.string.project_added, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            action.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }else{
            Project updateProject = new Project(user.getEmail(),user.getDisplayName(),
                    titleValue,descriptionValue,selectedProjectStatus,urlValue,projectMembers,
                    failureReasonValue,startDateValue,endDateValue);

            if(updateProject.equals(project)){
                LogUtils.d(TAG,"equality test passed");
                return;
            }

            action.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            Map <String,Object>update = updateProject.getUpdateFields(project);

            FirebaseDatabase.getInstance().getReference().child(path).child(projectID).updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!isFinishing()) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), R.string.project_updated, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            action.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }


    }


    private void createNewProjectMemberView() {
        final View view = LayoutInflater.from(this).inflate(R.layout.project_member_item, null);
        projectMembersContainer.addView(view);
        CountryCodePicker picker = view.findViewById(R.id.country_code);
        picker.setDefaultCountryUsingNameCode("254");
        view.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                projectMembersContainer.removeView(view);
            }
        });
    }


    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        LogUtils.d(TAG,"data snapshot ==> "+dataSnapshot.toString());
        if(dataSnapshot.getValue()==null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.no_project_exists_with_the_given_id);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.create().setCanceledOnTouchOutside(false);
            builder.show();
        }else{
            //for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                project = dataSnapshot.getValue(Project.class);
             //   break;
            //}
            LogUtils.d(TAG,"project -- > "+project.toString());

            populateViews(project);
        }
    }

    private void populateViews(Project project) {
        setViewState(true);
        title.setText(project.getTitle());
        startDate.setText(project.getStartDate());
        endDate.setText(project.getEndDate());
        description.setText(project.getDescription());
        url.setText(project.getUrl());
        selectedProjectStatus = project.getStatus();
        projectStatus.setSelection(selectedProjectStatus);
        if(selectedProjectStatus == Constants.PROJECT_STATUS.FAILED){
            failureReasonContainer.setVisibility(View.VISIBLE);
            failureReason.setText(project.getFailureReason());
        }
        List<String>projectMembers = project.getProjectMembers();
        for(String string:projectMembers){
            Member member = new Member();
            member.setFields(string);
            final View view = LayoutInflater.from(this).inflate(R.layout.project_member_item, null);
            projectMembersContainer.addView(view);
            CountryCodePicker picker = view.findViewById(R.id.country_code);
            picker.setDefaultCountryUsingNameCode("254");
            TextInputEditText name = view.findViewById(R.id.member_name);
            TextInputEditText role = view.findViewById(R.id.member_role);
            name.setText(member.getName());
            role.setText(member.getRole());
            if(!TextUtils.isEmpty(member.getPhoneNumber())){
                TextInputEditText phone = view.findViewById(R.id.member_phone_number);
                phone.setText(UiUtils.phoneNumberMinusCode(member.getPhoneNumber()));
                picker.setCountryForPhoneCode(Integer.valueOf(UiUtils.phoneNumberCountryCode(member.getPhoneNumber())));
            }
            if(!TextUtils.isEmpty(member.getEmail())){
                TextInputEditText email = view.findViewById(R.id.member_email);
                email.setText(member.getEmail());
            }
            view.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    projectMembersContainer.removeView(view);
                //    for(String string:)
                }
            });
        }


    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Toast.makeText(getApplicationContext(),R.string.error_occured,Toast.LENGTH_SHORT).show();
        finish();
    }
}
