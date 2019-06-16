package com.dans.apps.bitsa.ui;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dans.apps.bitsa.Constants;
import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.model.Contact;
import com.dans.apps.bitsa.model.Semester;
import com.dans.apps.bitsa.utils.InputValidator;
import com.dans.apps.bitsa.utils.UiUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rilixtech.CountryCodePicker;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class GeneralFragment extends Fragment implements View.OnClickListener {

    Contact contact;
    Semester semester;
    TextInputEditText startYear;
    TextInputEditText endYear;
    TextInputEditText semesterNumber;

    TextInputEditText email;
    TextInputEditText phone;
    CountryCodePicker picker;
    DatabaseReference contactReference;
    DatabaseReference semesterReference;

    ScrollView contents;
    ProgressBar progressBar;
    TextView message;
    Button save;

    public GeneralFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_general, container, false);
        startYear = view.findViewById(R.id.start_year);
        endYear = view.findViewById(R.id.end_year);
        semesterNumber = view.findViewById(R.id.semester);

        contents = view.findViewById(R.id.contents);
        progressBar = view.findViewById(R.id.progress_bar);
        message = view.findViewById(R.id.message);
        progressBar.setVisibility(View.GONE);
        message.setVisibility(View.GONE);
        contents.setVisibility(View.VISIBLE);
        email = view.findViewById(R.id.email);
        phone = view.findViewById(R.id.phone);
        picker = view.findViewById(R.id.country_code);
        save = view.findViewById(R.id.save);
        save.setOnClickListener(this);
        fetchContents();
        return view;
    }

    private void fetchContents() {

        contents.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        message.setVisibility(View.VISIBLE);
        message.setText(R.string.fetching_contents);

        contactReference = FirebaseDatabase.getInstance().getReference(Constants.PATHS.contacts);
        contactReference.addValueEventListener(contactListener);
        semesterReference = FirebaseDatabase.getInstance().getReference(Constants.PATHS.semesterDetails);
        semesterReference.addValueEventListener(semesterListener);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(semesterReference!=null){
            semesterReference.removeEventListener(semesterListener);
        }
        if(contactReference!=null){
            contactReference.removeEventListener(contactListener);
        }
    }

    private ValueEventListener contactListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                contact = snapshot.getValue(Contact.class);
                contact.setId(snapshot.getKey());
            }

            contents.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            message.setVisibility(View.GONE);
            message.setText("");

            if(contact!=null){
                email.setText(contact.getEmail());
                phone.setText(UiUtils.phoneNumberMinusCode(contact.getPhone()));
                picker.setCountryForPhoneCode(Integer.valueOf(UiUtils.phoneNumberCountryCode(contact.getPhone())));
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {}
    };

    private ValueEventListener semesterListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                semester = snapshot.getValue(Semester.class);
                semester.setId(snapshot.getKey());
            }

            contents.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            message.setVisibility(View.GONE);
            message.setText("");

            if(semester!=null){
                startYear.setText(String.valueOf(semester.getStartYear()));
                endYear.setText(String.valueOf(semester.getEndYear()));
                semesterNumber.setText(String.valueOf(semester.getNumber()));
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {}
    };


    @Override
    public void onClick(View v) {
        validateEntry();
    }

    private void validateEntry() {
        String enteredStartYear = startYear.getText().toString();
        String enteredEndYearValue = endYear.getText().toString();
        String enteredSemesterNumber = semesterNumber.getText().toString();

        String enteredEmail = email.getText().toString();
        String enteredPhoneNumber = phone.getText().toString();

        String emptyError = getResources().getString(R.string.field_cannot_be_empty);
        String invalidLength = getResources().getString(R.string.invalid_length);
        if(TextUtils.isEmpty(enteredStartYear)){
            startYear.setError(emptyError);
            return;
        }else if(enteredStartYear.length()!=4){
            startYear.setError(invalidLength);
            return;
        }else{
            startYear.setError(null);
        }
        if(TextUtils.isEmpty(enteredEndYearValue)){
            endYear.setError(emptyError);
            return;
        }else if(enteredEndYearValue.length()!=4){
            endYear.setError(invalidLength);
            return;
        }else{
            endYear.setError(null);
        }
        if(TextUtils.isEmpty(enteredSemesterNumber)){
            semesterNumber.setError(emptyError);
            return;
        }else if(enteredSemesterNumber.length()!=1){
            semesterNumber.setError(invalidLength);
            return;
        }else{
            semesterNumber.setError(null);
        }

        if(TextUtils.isEmpty(enteredEmail)){
            email.setError(emptyError);
            return;
        }else if(!InputValidator.isValidEmail(enteredEmail)){
            email.setError(getResources().getString(R.string.fui_invalid_email_address));
            return;
        }else{
            email.setError(null);
        }
        if(TextUtils.isEmpty(enteredPhoneNumber)){
            phone.setError(emptyError);
            return;
        }else if(!InputValidator.isValidMobile(enteredPhoneNumber)){
            phone.setError(getResources().getString(R.string.invalid_phone_number));
            return;
        }

        update(enteredStartYear,enteredEndYearValue,enteredSemesterNumber,enteredPhoneNumber,enteredEmail);
    }

    private void showUpdating(){
        save.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        contents.setVisibility(View.VISIBLE);
        message.setVisibility(View.GONE);
    }

    private void showNotUpdating(){
        save.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        contents.setVisibility(View.VISIBLE);
        message.setVisibility(View.GONE);
    }
    private void update(String enteredStartYear,
                        String enteredEndYearValue,
                        String enteredSemesterNumber,
                        String enteredPhoneNumber,
                        String enteredEmail) {
        showUpdating();
        enteredPhoneNumber = picker.getSelectedCountryCodeWithPlus()+enteredPhoneNumber;
        Contact updateContact = new Contact(enteredEmail,enteredPhoneNumber);
        if(contact==null){
            contactReference.push().setValue(updateContact);
        }else{
            Map<String,Object>updateParams = contact.getUpdateParams(updateContact);
            if(updateParams.size()==0){
                showNotUpdating();
            }else{
                contactReference.child(contact.getId()).updateChildren(updateParams);
            }
        }
        showNotUpdating();
        showUpdating();
        Semester updateSemester = new Semester(Integer.valueOf(enteredSemesterNumber),
                Integer.valueOf(enteredStartYear),Integer.valueOf(enteredEndYearValue));
        if(semester==null){
            semesterReference.push().setValue(updateSemester);
        }else{
            Map<String,Object>updateParams = semester.getUpdateParams(updateSemester);
            if(updateParams.size()==0){
                showNotUpdating();
            }else{
                semesterReference.child(semester.getId()).updateChildren(updateParams);
            }
        }
        showNotUpdating();

    }
}
