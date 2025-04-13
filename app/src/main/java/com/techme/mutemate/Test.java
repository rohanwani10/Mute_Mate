package com.techme.mutemate;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class Test extends AppCompatActivity {

    TextView title, accuracy, time_taken;
    RadioGroup q1, q2, q3, q4, q5, q6, q7, q8, q9, q10;
    Button submit, d_btn, reset_btn;
    int[] ansKey = {3,3,0,1,1,2,0,1,0,1};
    int[] selected = {0,0,0,0,0,0,0,0,0,0};
    int score = 0;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    Dialog d;
    LocalTime now, test_done_time;
    Duration time_taken_in_total;
    RadioGroup[] grps;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String test_no = intent.getStringExtra("test_no");
        int test_no_int = Integer.parseInt(test_no);
        String test_title = intent.getStringExtra("test_title");

        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        now = LocalTime.now();

        switch (test_no_int) {
            case 1:
                setContentView(R.layout.activity_test);
                title = findViewById(R.id.textView9);
                title.setText("Test 1 - " + test_title);
                break;
            case 2:
                setContentView(R.layout.activity_test2);
                title = findViewById(R.id.textView9);
                title.setText("Test 2 - " + test_title);
                break;
            case 3:
                setContentView(R.layout.activity_test3);
                title = findViewById(R.id.textView9);
                title.setText("Test 3 - " + test_title);
                break;
            case 4:
                setContentView(R.layout.activity_test4);
                title = findViewById(R.id.textView9);
                title.setText("Test 4 - " + test_title);

                break;
            case 5:
                setContentView(R.layout.activity_test5);
                title = findViewById(R.id.textView9);
                title.setText("Test 5 - " + test_title);
                break;
        }

        q1 = findViewById(R.id.t1q1);
        q2 = findViewById(R.id.t1q2);
        q3 = findViewById(R.id.t1q3);
        q4 = findViewById(R.id.t1q4);
        q5 = findViewById(R.id.t1q5);
        q6 = findViewById(R.id.t1q6);
        q7 = findViewById(R.id.t1q7);
        q8 = findViewById(R.id.t1q8);
        q9 = findViewById(R.id.t1q9);
        q10 = findViewById(R.id.t1q10);
        submit = findViewById(R.id.submit);
        reset_btn = findViewById(R.id.resetBtn);
        grps = new RadioGroup[]{q1, q2, q3, q4, q5, q6, q7, q8, q9, q10};

        Toast.makeText(this, "At least 8 Question should be correct to pass the test !", Toast.LENGTH_SHORT).show();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int SelectedId, position;
                //q1
                SelectedId = q1.getCheckedRadioButtonId();
                if (SelectedId != -1) {
                    View r = findViewById(SelectedId);
                    position = q1.indexOfChild(r);
                    selected[0] = position;
                    if (position == ansKey[0]) {
                        score++;
                    }
                }
                else {
                    selected[0] = -1;
                }

                //q2
                SelectedId = q2.getCheckedRadioButtonId();
                if (SelectedId != -1) {
                    View r = findViewById(SelectedId);
                    position = q2.indexOfChild(r);
                    selected[1] = position;
                    if (position == ansKey[1]) {
                        score++;
                    }
                }
                else {
                    selected[1] = -1;
                }

                //q3
                SelectedId = q3.getCheckedRadioButtonId();
                if (SelectedId != -1) {
                    View r = findViewById(SelectedId);
                    position = q3.indexOfChild(r);
                    selected[2] = position;
                    if (position == ansKey[2]) {
                        score++;
                    }
                }
                else {
                    selected[2] = -1;
                }

                //q4
                SelectedId = q4.getCheckedRadioButtonId();
                if (SelectedId != -1) {
                    View r = findViewById(SelectedId);
                    position = q4.indexOfChild(r);
                    selected[3] = position;
                    if (position == ansKey[3]) {
                        score++;
                    }
                }
                else {
                    selected[3] = -1;
                }

                //q5
                SelectedId = q5.getCheckedRadioButtonId();
                if (SelectedId != -1) {
                    View r = findViewById(SelectedId);
                    position = q5.indexOfChild(r);
                    selected[4] = position;
                    if (position == ansKey[4]) {
                        score++;
                    }
                }
                else {
                    selected[4] = -1;
                }

                //q6
                SelectedId = q6.getCheckedRadioButtonId();
                if (SelectedId != -1) {
                    View r = findViewById(SelectedId);
                    position = q6.indexOfChild(r);
                    selected[5] = position;
                    if (position == ansKey[5]) {
                        score++;
                    }
                }
                else {
                    selected[5] = -1;
                }

                //q7
                SelectedId = q7.getCheckedRadioButtonId();
                if (SelectedId != -1) {
                    View r = findViewById(SelectedId);
                    position = q7.indexOfChild(r);
                    selected[6] = position;
                    if (position == ansKey[6]) {
                        score++;
                    }
                }
                else {
                    selected[6] = -1;
                }

                //q8
                SelectedId = q8.getCheckedRadioButtonId();
                if (SelectedId != -1) {
                    View r = findViewById(SelectedId);
                    position = q8.indexOfChild(r);
                    selected[7] = position;
                    if (position == ansKey[7]) {
                        score++;
                    }
                }
                else {
                    selected[7] = -1;
                }

                //q9
                SelectedId = q9.getCheckedRadioButtonId();
                if (SelectedId != -1) {
                    View r = findViewById(SelectedId);
                    position = q9.indexOfChild(r);
                    selected[8] = position;
                    if (position == ansKey[8]) {
                        score++;
                    }
                }
                else {
                    selected[8] = -1;
                }

                //q10
                SelectedId = q10.getCheckedRadioButtonId();
                if (SelectedId != -1) {
                    View r = findViewById(SelectedId);
                    position = q10.indexOfChild(r);
                    selected[9] = position;
                    if (position == ansKey[9]) {
                        score++;
                    }
                }
                else {
                    selected[9] = -1;
                }

                submit.setEnabled(false);
                submit.setText("Wait...");
                new Handler().postDelayed(() -> {
                    submit.setEnabled(true);
                    submit.setText("Submit");
                    showResult();
                }, 3000);
            }
        });

        reset_btn.setOnClickListener(v -> {
            for (int i = 0; i < 10; i++) {
                RadioGroup r = grps[i];
                r.clearCheck();
                for (int j=0; j<r.getChildCount(); j++) {
                    r.getChildAt(j).setEnabled(true);
                    r.getChildAt(j).setBackgroundColor(getResources().getColor(R.color.none));
                }
                now = LocalTime.now();
                score = 0;
                submit.setEnabled(true);
                submit.setText("Submit");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void showResult() {
        int selectedID;
        RadioButton sele;
        MediaPlayer player;

        for (int i = 0; i < 10; i++) {
            selectedID = grps[i].getCheckedRadioButtonId();
            sele = findViewById(selectedID);
            if (selected[i] != -1) {
                if (selected[i] == ansKey[i]) {
                    sele.setBackgroundColor(getResources().getColor(R.color.green));

                }
                else {
                    sele.setBackgroundColor(getResources().getColor(R.color.red));
                }
            }
        }



        d = new Dialog(this);
        test_done_time = LocalTime.now();
        time_taken_in_total = Duration.between(now, test_done_time);

        if (score >= 8) {

            d.setContentView(R.layout.success_dialog);
            d.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            accuracy = d.findViewById(R.id.d_s_accuracy);
            time_taken = d.findViewById(R.id.d_s_time_taken);
            d_btn = d.findViewById(R.id.okButton);
            d.show();

            player = MediaPlayer.create(this, R.raw.success);
            player.setVolume(0.5f,0.5f);
            player.start();
            int val = Integer.parseInt(LearningModule.xp_val.substring(0, LearningModule.xp_val.indexOf('X')));
            val += 500;
            LearningModule.xp_val = (val < 1000) ? "0" + val + "XP" : val + "XP";
            if (user != null) {
                String userId = user.getUid();
                DatabaseReference userRef = databaseReference.child(userId);

                Map<String, Object> xpUpdateMap = new HashMap<>();
                xpUpdateMap.put("xp", LearningModule.xp_val);

                userRef.updateChildren(xpUpdateMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Test.this, "Test Completed. 500XP earned!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        else {
            d.setContentView(R.layout.fail_dialog);
            d.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            accuracy = d.findViewById(R.id.d_f_accuracy);
            time_taken = d.findViewById(R.id.d_f_time_taken);
            d_btn = d.findViewById(R.id.okButton2);
            d.show();

            player = MediaPlayer.create(this, R.raw.fail);
            player.setVolume(1.0f,1.0f);
            player.start();
            Toast.makeText(Test.this, "Test Failed. Try Again!", Toast.LENGTH_SHORT).show();
        }


        long min = 0;
        long sec = time_taken_in_total.getSeconds();
        String min_str, sec_str;

        while (sec >= 60) {
            min++;
            sec -= 60;
        }

        if (min < 10) {
            min_str = "0" + min;
        }
        else {
            min_str = "" + min;
        }

        if (sec < 10) {
            sec_str = "0" + sec;
        }
        else {
            sec_str = "" + sec;
        }

        time_taken.setText("Time taken - " + min_str + " : " + sec_str);
        accuracy.setText("Accuracy - " + (score * 10) + "%");

        d_btn.setOnClickListener(v -> {
            d.dismiss();
        });

        for (int i = 0; i < 10; i++) {
            RadioGroup r = grps[i];
            for (int j=0; j<r.getChildCount(); j++) {
                r.getChildAt(j).setEnabled(false);
            }
        }

        submit.setEnabled(false);
        submit.setText("Submitted !");
    }
}

