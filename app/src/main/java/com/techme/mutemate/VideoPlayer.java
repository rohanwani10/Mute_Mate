package com.techme.mutemate;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class VideoPlayer extends AppCompatActivity {

    VideoView v;
    ProgressBar img;
    TextView txt;
    FloatingActionButton fBtn;
    EditText notes;
    private FirebaseUser user;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        Intent intent = getIntent();
        String video_path = intent.getStringExtra("path");
        String video_title = intent.getStringExtra("title");

        v = findViewById(R.id.videoView);
        img = findViewById(R.id.progressbar);
        txt = findViewById(R.id.textView);
        notes = findViewById(R.id.editTextTextMultiLine);
        txt.setText(video_title);
        fBtn = findViewById(R.id.floatingActionButton);

        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = databaseReference.child(userId).child(video_title);

            // Fetch previously saved note
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String savedNote = snapshot.getValue(String.class);
                        notes.setText(savedNote); // Display the retrieved note
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(VideoPlayer.this, "Failed to load saved note", Toast.LENGTH_SHORT).show();
                }
            });
        }


        if (video_path != null) {
            File file = new File(video_path);
            if (!file.exists()) {
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri videoUri = Uri.fromFile(file);
            v.setVideoURI(videoUri);

            MediaController mediaController = new MediaController(this);
            v.setMediaController(mediaController);

            v.setOnInfoListener((mp, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    img.setVisibility(View.GONE); // Hide loading when video starts
                    return true;
                }
                return false;
            });

            new Handler().postDelayed(() -> v.start(), 2500);

            v.setOnCompletionListener(mp -> {
                // Update XP
                int val = Integer.parseInt(LearningModule.xp_val.substring(0, LearningModule.xp_val.indexOf('X')));
                val += 100;
                LearningModule.xp_val = (val < 1000) ? "0" + val + "XP" : val + "XP";

                // Handle streak
                int streak = Integer.parseInt(LearningModule.streak_val);
                LocalDateTime current = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formatted = current.format(formatter);

                if (user != null) {
                    String userId = user.getUid();
                    DatabaseReference userRef = databaseReference.child(userId);

                    if (streak == 0) {
                        // First-time streak increment
                        streak += 1;
                        LearningModule.streak_val = String.valueOf(streak);

                        Map<String, Object> updateMap = new HashMap<>();
                        updateMap.put("completion_point", formatted);
                        updateMap.put("streak", LearningModule.streak_val);

                        userRef.updateChildren(updateMap);
                    } else {
                        int finalStreak = streak;
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    HelperClass userData = snapshot.getValue(HelperClass.class);
                                    if (userData != null) {
                                        String pastCompletion = userData.getCompletion_point();
                                        LocalDateTime prev = LocalDateTime.parse(pastCompletion, formatter);
                                        Duration duration = Duration.between(prev, current);

                                        if (duration.toHours() >= 24) { // Adjust time if needed
                                            int updatedStreak = finalStreak + 1;
                                            LearningModule.streak_val = String.valueOf(updatedStreak);

                                            Map<String, Object> updateMap = new HashMap<>();
                                            updateMap.put("completion_point", formatted);
                                            updateMap.put("streak", LearningModule.streak_val);

                                            userRef.updateChildren(updateMap).addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(VideoPlayer.this, "Streak updated!", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(VideoPlayer.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    // XP update
                    Map<String, Object> xpUpdateMap = new HashMap<>();
                    xpUpdateMap.put("xp", LearningModule.xp_val);

                    userRef.updateChildren(xpUpdateMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(VideoPlayer.this, "Video Completed. 100XP earned!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        fBtn.setOnClickListener(view -> {
            String title = txt.getText().toString();
            String n = notes.getText().toString();

            Map<String, Object> map = new HashMap<>();
            map.put(title, n);

            if (user != null) {
                String userId = user.getUid();
                DatabaseReference userRef = databaseReference.child(userId);
                userRef.updateChildren(map).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Note Saved!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
