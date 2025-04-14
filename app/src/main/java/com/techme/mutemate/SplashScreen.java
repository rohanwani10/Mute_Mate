package com.techme.mutemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class SplashScreen extends AppCompatActivity {

    private Animation blinkAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink_loading);

        // Start blinking animation on all tagged views
        ViewGroup root = findViewById(R.id.main); // your root ConstraintLayout ID
        applyBlinkToTaggedViews(root, "blinkingPlaceholder");

//      FireBase check for user login or logout
        FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentuser != null) {
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
        } else {
            startActivity(new Intent(SplashScreen.this, LoginActivity.class));
        }
        finish();

    }
    private void applyBlinkToTaggedViews(ViewGroup root, String tag) {
        List<View> viewsToBlink = new ArrayList<>();
        findViewsWithTag(root, tag, viewsToBlink);
        for (View v : viewsToBlink) {
            v.startAnimation(blinkAnimation);
        }
    }
    private void findViewsWithTag(ViewGroup parent, String tag, List<View> result) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (tag.equals(child.getTag())) {
                result.add(child);
            }
            if (child instanceof ViewGroup) {
                findViewsWithTag((ViewGroup) child, tag, result);
            }
        }
    }
}