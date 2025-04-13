package com.techme.mutemate;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LearningModule extends AppCompatActivity {

    Button temp;
    TextView unit, unitName;
    ImageView unitImg;

    static Dialog dialog;
    static TextView levelTitle;
    TextView xptext, streaktext;
    static Button levelButton;
    static Context c;
    static String path_video, xp_val, streak_val;
    private DatabaseReference databaseReference;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_module);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.scrollFragmentContainer, new FragmentUnit1())
                .commit();

        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        xptext = findViewById(R.id.XpText);
        streaktext = findViewById(R.id.streakText);

        temp = findViewById(R.id.b1);
        unit = findViewById(R.id.unitNo);
        unitName = findViewById(R.id.unitName);
        unitImg = findViewById(R.id.unitImg);

        dialog = new Dialog(LearningModule.this);
        dialog.setContentView(R.layout.custom_dialog_level);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        levelTitle = dialog.findViewById(R.id.levelTitle);
        levelButton = dialog.findViewById(R.id.startButton);
        c = this;
    }

    public void onStart() {
        super.onStart();
        if (user != null) {
            getRequiredData();
        }
    }

    private void getRequiredData() {
        String userId = user.getUid();
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Here, HelperClass.class tells Firebase that the data should be mapped to an instance of HelperClass.
                    HelperClass userData = snapshot.getValue(HelperClass.class);
                    if (userData != null) {
                        xptext.setText(userData.getXp());
                        xp_val = xptext.getText().toString();
                        streaktext.setText(userData.getStreak());
                        streak_val = streaktext.getText().toString();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LearningModule.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static void showDialog(String title, String btnText, int id) {
        levelTitle.setText(title);
        levelButton.setText(btnText);
        dialog.show();
        levelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(id == 0) {
                    Intent intent = new Intent(c, Test.class);
                    intent.putExtra("test_no", title.substring(title.indexOf(" ") + 1, title.indexOf("-") - 1));
                    intent.putExtra("test_title", title.substring(title.indexOf("-") + 2));
                    c.startActivity(intent);
                }
                else {
                    MyDatabaseHelper myDb = new MyDatabaseHelper(c);
                    path_video = myDb.getVideoPath(id);
                    System.out.println("Video path for ID " + id + ": " + path_video);
                    Intent intent = new Intent(c, VideoPlayer.class);
                    intent.putExtra("path", path_video);
                    intent.putExtra("title", title.substring(title.indexOf("-") + 2));
                    c.startActivity(intent);
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void changeFrag(View v) {
        temp.setBackgroundColor(getResources().getColor(R.color.none));
        temp.setTextColor(getResources().getColor(R.color.Grotto));

        Button b = (Button) v;
        String btnText = b.getText().toString();
        Fragment f = null;

        b.setBackgroundColor(getResources().getColor(R.color.green));
        b.setTextColor(getResources().getColor(R.color.black));

        // Choose the fragment based on button text
        switch (btnText) {
            case "1":
                f = new FragmentUnit1();
                unit.setText("Unit 1");
                unitName.setText("Learning Basics");
                unitImg.setImageResource(R.drawable.abc);
                break;
            case "2":
                f = new FragmentUnit2();
                unit.setText("Unit 2");
                unitName.setText("Know the World");
                unitImg.setImageResource(R.drawable.handshake);
                break;
            case "3":
                f = new FragmentUnit3();
                unit.setText("Unit 3");
                unitName.setText("Everyday Signs");
                unitImg.setImageResource(R.drawable.day_to_day);
                break;
            case "4":
                f = new FragmentUnit4();
                unit.setText("Unit 4");
                unitName.setText("Focus on you !");
                unitImg.setImageResource(R.drawable.focus);
                break;
            case "5":
                f = new FragmentUnit5();
                unit.setText("Unit 5");
                unitName.setText("Communicate more");
                unitImg.setImageResource(R.drawable.convo);
                break;
        }

        int currentX = temp.getLeft();
        int newX = b.getLeft();

        if (newX > currentX) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.scrollFragmentContainer, f)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.scrollFragmentContainer, f)
                    .commit();
        }

        temp = b;
    }

}

