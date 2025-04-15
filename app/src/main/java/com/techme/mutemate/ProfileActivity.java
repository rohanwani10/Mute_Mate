package com.techme.mutemate;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private Spinner spinner;
    private EditText birthdate, username, email, contact;
    private RadioGroup radioGroupGender;
    private Button btnSave;
    private TextView usernameTitle;

    private DatabaseReference databaseReference;
    private FirebaseUser user;

    private String selectedCountry = "";
    private String selectedGender = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        spinner = findViewById(R.id.spinner_country);
        birthdate = findViewById(R.id.profile_birthdate);
        username = findViewById(R.id.profile_username);
        contact = findViewById(R.id.profile_contact);
        usernameTitle = findViewById(R.id.profile_tv1);
        email = findViewById(R.id.profile_email);
        radioGroupGender = findViewById(R.id.radioGroupgender);
        btnSave = findViewById(R.id.profile_save_btn);


        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        if (user != null) {
            loadUserData();
        }


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.country_list, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCountry = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCountry = "";
            }
        });


        birthdate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                birthdate.setText(selectedDate);
            }, year, month, day);

            datePickerDialog.show();
        });


        radioGroupGender.setOnCheckedChangeListener((group, checkedId) -> {
            for (int i = 0; i < radioGroupGender.getChildCount(); i++) {
                View child = radioGroupGender.getChildAt(i);
                if (child instanceof RadioButton) {
                    ((RadioButton) child).setTextColor(getResources().getColor(R.color.black)); // default color
                }
            }

            RadioButton selectedRadioButton = findViewById(checkedId);
            if (selectedRadioButton != null) {
                selectedRadioButton.setTextColor(getResources().getColor(R.color.teal_700)); // highlighted color
                selectedGender = selectedRadioButton.getText().toString();
            }
        });
        btnSave.setOnClickListener(v -> saveProfileData());
    }

    private void loadUserData() {
        String userId = user.getUid();
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Here, HelperClass.class tells Firebase that the data should be mapped to an instance of HelperClass.
                    HelperClass userData = snapshot.getValue(HelperClass.class);
                    if (userData != null) {
                        usernameTitle.setText(userData.getUsername());
                        username.setText(userData.getUsername());
                        email.setText(userData.getEmail());

                        if (userData.getBirthdate() != null) {
                            birthdate.setText(userData.getBirthdate());
                        }
                        if (userData.getGender() != null) {
                            selectedGender = userData.getGender();
                            // Set the selected gender in the RadioGroup
                            for (int i = 0; i < radioGroupGender.getChildCount(); i++) {
                                RadioButton rb = (RadioButton) radioGroupGender.getChildAt(i);
                                if (rb.getText().toString().equals(selectedGender)) {
                                    rb.setChecked(true);
                                    break;
                                }
                            }
                        }
                        if (userData.getContact() != null) {
                            contact.setText(userData.getContact());
                        }
                        if (userData.getCountry() != null) {
                            selectedCountry = userData.getCountry();
                            // Set the selected country in the Spinner
                            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
                            int position = adapter.getPosition(selectedCountry);
                            spinner.setSelection(position);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileData() {
        String enteredBirthdate = birthdate.getText().toString().trim();
        String enteredContact = contact.getText().toString().trim();
        String enteredUsername = username.getText().toString().trim();


        if (enteredBirthdate.isEmpty() || enteredContact.isEmpty() || selectedGender.isEmpty() || selectedCountry.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = databaseReference.child(userId);
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("username", enteredUsername);
            updateMap.put("birthdate", enteredBirthdate);
            updateMap.put("gender", selectedGender);
            updateMap.put("country", selectedCountry);
            updateMap.put("contact", enteredContact);


            userRef.updateChildren(updateMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Update Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
