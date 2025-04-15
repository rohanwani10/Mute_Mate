package com.techme.mutemate;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Setting extends AppCompatActivity {
    //    Firebase variables
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    private final String BASE_URL = "https://rohanwani10.github.io/TechMe_MuteMate/#/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        Set Name and Email of user to edit and check
        TextView setting_user_name = findViewById(R.id.setting_user_name);
        TextView setting_user_email = findViewById(R.id.setting_user_email);

        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        if (user != null) {
            String userId = user.getUid();
            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        //Here, HelperClass.class tells Firebase that the data should be mapped to an instance of HelperClass.
                        HelperClass userData = snapshot.getValue(HelperClass.class);
                        if (userData != null) {
                            setting_user_name.setText(userData.getUsername());
                            setting_user_email.setText(userData.getEmail());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(Setting.this, "Failed to User Data. Pl. try Later. ", Toast.LENGTH_SHORT).show();
                }
            });

        }

//      Return Home
        TextView returnHome = findViewById(R.id.returnHome);
        returnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Setting.this, MainActivity.class));
            }
        });


//        Edit Profile Button
        CardView profile = findViewById(R.id.EditProfile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Setting.this, ProfileActivity.class);
                startActivity(intent);

            }
        });

//        Notification
        LinearLayout Notification = findViewById(R.id.Notification);
        Notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//               navigate to the settings of the device
                Intent intent = new Intent();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra("app_package", getPackageName());
                    intent.putExtra("app_uid", getApplicationInfo().uid);
                } else {
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                }

                startActivity(intent);
            }
        });

//        Terms and condition
        LinearLayout terms = findViewById(R.id.termsandcondition);
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BASE_URL + "termsandcondition"));
                startActivity(intent);

            }
        });

//        Privacy and policy
        LinearLayout privacy_and_policy = findViewById(R.id.privacy_and_policy);
        privacy_and_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BASE_URL + "privacyandpolicy"));
                startActivity(intent);
            }
        });

//        Support
        LinearLayout support = findViewById(R.id.support);
        support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BASE_URL + "support"));
                startActivity(intent);
            }
        });

//        Community Guidelines
        LinearLayout guidlines = findViewById(R.id.community_guidelines);
        guidlines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BASE_URL + "guidelines"));
                startActivity(intent);
            }
        });

//        Logout Button
        LinearLayout logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Setting.this, LoginActivity.class));
            }
        });

    }

}