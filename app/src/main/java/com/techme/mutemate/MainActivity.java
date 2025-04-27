package com.techme.mutemate;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
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


public class MainActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Handler().postDelayed(this::animateStartup, 300);

        // Set up click listeners for interactive elements
        setupClickListeners();


        ImageView Profile = findViewById(R.id.profileIcon);
        ImageView Notification_btn = findViewById(R.id.notificationButton);
        ImageView Speech_text = findViewById(R.id.speech_text);

        androidx.cardview.widget.CardView videoCard1 = findViewById(R.id.videoCard1);
        androidx.cardview.widget.CardView videoCard2 = findViewById(R.id.videoCard2);
        androidx.cardview.widget.CardView videoCard3 = findViewById(R.id.videoCard3);
        androidx.cardview.widget.CardView videoCard4 = findViewById(R.id.videoCard4);

        videoCard1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/qcdivQfA41Y?si=81eVuVOouwzZ7PHF"));
                intent.setPackage("com.google.android.youtube"); // Optional: force to open in YouTube app
                startActivity(intent);
            }
        });

        videoCard2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/vnH2BmcSRMA?si=EvFDEsiZLhj1h5h7"));
                intent.setPackage("com.google.android.youtube");
                startActivity(intent);
            }
        });

        videoCard3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/VtbYvVDItvg?si=3g388ghyN5GkKzRb"));
                intent.setPackage("com.google.android.youtube");
                startActivity(intent);
            }
        });

        videoCard4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/drs0_jcKr5w?si=vs9he80xOfwjZV84"));
                intent.setPackage("com.google.android.youtube");
                startActivity(intent);
            }
        });



        Speech_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, Speech_to_text_Activity.class);
                startActivity(intent);
            }
        });
        Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "hello", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, Setting.class);
                startActivity(intent);
            }
        });

        Notification_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Notification.class);
                startActivity(intent);
            }
        });

        ImageView learning_module = findViewById(R.id.moduleIcon);
        learning_module.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LearningModule.class);
                startActivity(intent);
            }
        });

        TextView welcomeText = findViewById(R.id.welcomeText);
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
                            welcomeText.setText("Hi, "+userData.getUsername());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Failed to User Data. Pl. try Later. ", Toast.LENGTH_SHORT).show();
                }
            });

        }

        // In your Activity or Fragment
        // RecyclerView recyclerView = findViewById(R.id.recyclerView); // Make sure to add ID to your RecyclerView
        //NotificationAdapter notificationAdapter = new NotificationAdapter(this);
        //
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setAdapter(notificationAdapter);
        //
        //// Register the receiver in onResume
        //@Override
        //protected void onResume() {
        //    super.onResume();
        //    notificationAdapter.registerReceiver();
        //}
        //
        //// Unregister in onPause
        //@Override
        //protected void onPause() {
        //    super.onPause();
        //    notificationAdapter.unregisterReceiver();
        //}
        //
        //// Method to send a notification
        //private void sendNotification(String title, String message) {
        //    Intent intent = new Intent(NotificationAdapter.ACTION_NEW_NOTIFICATION);
        //    intent.putExtra(NotificationAdapter.EXTRA_NOTIFICATION_TITLE, title);
        //    intent.putExtra(NotificationAdapter.EXTRA_NOTIFICATION_MESSAGE, message);
        //    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        //}
    }

    private void animateStartup() {
        // Animate header section - Find view directly instead of using tags
        TextView welcomeText = findViewById(R.id.welcomeText);
        if (welcomeText != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            welcomeText.startAnimation(fadeIn);
        }

        // Animate search bar with slide up and fade
        CardView searchBar = findViewById(R.id.searchBarCard);
        if (searchBar != null) {
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            AnimationSet searchBarAnim = new AnimationSet(true);
            searchBarAnim.addAnimation(fadeIn);
            searchBarAnim.addAnimation(slideUp);
            searchBar.startAnimation(searchBarAnim);
        }

        // Animate "Explore more" card
        CardView exploreCard = findViewById(R.id.exploreCard);
        if (exploreCard != null) {
            exploreCard.setScaleX(0.8f);
            exploreCard.setScaleY(0.8f);
            exploreCard.setAlpha(0f);

            exploreCard.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(600).setStartDelay(300).setInterpolator(new OvershootInterpolator()).start();
        }

        // Animate popular lessons section
        LinearLayout popularSection = findViewById(R.id.popularSection);
        HorizontalScrollView videosScroll = findViewById(R.id.videosScrollView);

        if (popularSection != null) {
            popularSection.setAlpha(0f);
            popularSection.animate().alpha(1f).setDuration(500).setStartDelay(500).start();
        }

        // Animate videos from right
        if (videosScroll != null) {
            videosScroll.setAlpha(0f);
            Animation slideFromRight = AnimationUtils.loadAnimation(this, R.anim.from_right);
            slideFromRight.setStartOffset(700);
            videosScroll.startAnimation(slideFromRight);
            videosScroll.animate().alpha(1f).setDuration(500).start();
        }

        // Animate testimonials section
        LinearLayout testimonialSection = findViewById(R.id.testimonialSection);
        HorizontalScrollView testimonialScroll = findViewById(R.id.testimonialScrollView);

        if (testimonialSection != null) {
            testimonialSection.setAlpha(0f);
            testimonialSection.animate().alpha(1f).setDuration(500).setStartDelay(900).start();
        }

        // Animate testimonial cards from right
        if (testimonialScroll != null) {
            testimonialScroll.setAlpha(0f);
            Animation slideTestimonials = AnimationUtils.loadAnimation(this, R.anim.from_right);
            slideTestimonials.setStartOffset(1100);
            testimonialScroll.startAnimation(slideTestimonials);
            testimonialScroll.animate().alpha(1f).setDuration(500).start();
        }

        // Animate bottom navigation
        CardView bottomNav = findViewById(R.id.bottomNavCard);
        if (bottomNav != null) {
            bottomNav.setTranslationY(100f);
            bottomNav.animate().translationY(0f).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator()).setStartDelay(300).start();
        }
    }

    private void setupClickListeners() {
        // Add touch animations to bottom navigation icons
        ImageView homeIcon = findViewById(R.id.homeIcon);
        ImageView moduleIcon = findViewById(R.id.moduleIcon);
        ImageView speech_text = findViewById(R.id.speech_text);
        ImageView profileIcon = findViewById(R.id.profileIcon);

        setupButtonAnimation(homeIcon);
        setupButtonAnimation(moduleIcon);
        setupButtonAnimation(speech_text);
        setupButtonAnimation(profileIcon);

        // Add animations to notification button
        ImageView notificationButton = findViewById(R.id.notificationButton);
        setupButtonAnimation(notificationButton);

        // Add animations to cards
        CardView exploreCard = findViewById(R.id.main).findViewWithTag("exploreCard");
        setupCardClickAnimation(exploreCard);

        // Find all video cards in the horizontal scroll view
        LinearLayout videosContainer = findViewById(R.id.main).findViewWithTag("videosContainer");
        for (int i = 0; i < videosContainer.getChildCount(); i++) {
            if (videosContainer.getChildAt(i) instanceof CardView) {
                setupCardClickAnimation((CardView) videosContainer.getChildAt(i));
            }
        }

        // Find all testimonial cards and add animations
        LinearLayout testimonialsContainer = findViewById(R.id.main).findViewWithTag("testimonialsContainer");
        for (int i = 0; i < testimonialsContainer.getChildCount(); i++) {
            if (testimonialsContainer.getChildAt(i) instanceof CardView) {
                setupCardClickAnimation((CardView) testimonialsContainer.getChildAt(i));
            }
        }
    }

    private void setupButtonAnimation(View view) {
        Animation buttonPress = AnimationUtils.loadAnimation(this, R.anim.button_press);
        Animation buttonRelease = AnimationUtils.loadAnimation(this, R.anim.button_release);

        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.startAnimation(buttonPress);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.startAnimation(buttonRelease);
                v.performClick();
                return true;
            }
            return false;
        });
    }

    private void setupCardClickAnimation(CardView cardView) {
        cardView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(v, "scaleX", 0.95f);
                ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(v, "scaleY", 0.95f);
                scaleDownX.setDuration(100);
                scaleDownY.setDuration(100);
                AnimatorSet scaleDown = new AnimatorSet();
                scaleDown.play(scaleDownX).with(scaleDownY);
                scaleDown.start();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(v, "scaleX", 1f);
                ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(v, "scaleY", 1f);
                scaleUpX.setDuration(100);
                scaleUpY.setDuration(100);
                AnimatorSet scaleUp = new AnimatorSet();
                scaleUp.play(scaleUpX).with(scaleUpY);
                scaleUp.start();

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.performClick();
                }
                return true;
            }
            return false;
        });
    }
}