package com.techme.mutemate;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class Speech_to_text_Activity extends AppCompatActivity implements
        SignToSpeechFragment.OnFragmentInteractionListener,
        SpeechToSignFragment.OnFragmentInteractionListener {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Mode tracking
    private static final int MODE_SIGN_TO_TEXT = 0;
    private static final int MODE_TEXT_TO_SIGN = 1;
    private int currentMode = MODE_SIGN_TO_TEXT;

    // UI Components
    private Spinner spinnerMode;
    private TextView tvTitle;

    // Text to speech
    private TextToSpeech textToSpeech;
    private boolean isTtsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_to_text);

        // Check if model file exists
        checkModelFile();

        // Initialize UI components
        spinnerMode = findViewById(R.id.spinnerMode);
        tvTitle = findViewById(R.id.tvTitle);

        // Initialize Text to Speech
        initTextToSpeech();

        // Request necessary permissions
        requestPermissions();

        // Set up mode spinner
        setupModeSpinner();

        // Default to Sign to Speech mode
        loadSignToSpeechFragment();
    }

    private void checkModelFile() {
        try {
            String[] assetList = getAssets().list("");
            boolean modelFileExists = false;

            if (assetList != null) {
                for (String asset : assetList) {
                    android.util.Log.d("MainActivity", "Asset file: " + asset);
                    if (asset.equals("sign_language.task")) {
                        modelFileExists = true;
                        break;
                    }
                }
            }

            if (modelFileExists) {
                android.util.Log.d("MainActivity", "✓ Model file 'sign_language.task' found in assets");
            } else {
                android.util.Log.e("MainActivity", "❌ Model file 'sign_language.task' NOT found in assets");
                // Show a toast for better visibility of the issue
                android.widget.Toast.makeText(this,
                        "Sign language model file missing. Please add it to assets folder.",
                        android.widget.Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error checking model file: " + e.getMessage());
        }
    }

    private void setupModeSpinner() {
        spinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentMode = position;
                if (currentMode == MODE_SIGN_TO_TEXT) {
                    loadSignToSpeechFragment();
                } else {
                    loadSpeechToSignFragment();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void loadSignToSpeechFragment() {
        tvTitle.setText(R.string.sign_to_speech_title);
        replaceFragment(new SignToSpeechFragment());
    }

    private void loadSpeechToSignFragment() {
        tvTitle.setText(R.string.speech_to_sign_title);
        replaceFragment(new SpeechToSignFragment());
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported");
                } else {
                    isTtsReady = true;
                    Log.d(TAG, "TTS initialized successfully");
                }
            } else {
                Log.e(TAG, "TTS initialization failed");
            }
        });
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissionsList = new ArrayList<>();

            // Add required permissions
            permissionsList.add(Manifest.permission.CAMERA);
            permissionsList.add(Manifest.permission.RECORD_AUDIO);

            // Storage permissions based on Android version
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissionsList.add(Manifest.permission.READ_MEDIA_IMAGES);
            }

            // Check which permissions are not granted
            ArrayList<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissionsList) {
                if (ContextCompat.checkSelfPermission(this, permission) !=
                        PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }

            // Request permissions if needed
            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(
                        this,
                        permissionsToRequest.toArray(new String[0]),
                        PERMISSION_REQUEST_CODE
                );
            } else {
                // All permissions already granted
                loadInitialFragment();
            }
        } else {
            // Pre-Marshmallow, permissions granted at install time
            loadInitialFragment();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                // Permissions granted, load initial fragment
                loadInitialFragment();
            } else {
                // Handle permission denial
                Toast.makeText(this, "Permissions required to use this app",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadInitialFragment() {
        // Load initial fragment based on default mode
        if (currentMode == MODE_SIGN_TO_TEXT) {
            loadFragment(SignToSpeechFragment.newInstance());
        } else {
            loadFragment(SpeechToSignFragment.newInstance());
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up text to speech
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    // SignToSpeechFragment.OnFragmentInteractionListener implementation
    @Override
    public void onSpeakText(String text) {
        if (isTtsReady && textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sign_detection");
        } else {
            Toast.makeText(this, "Text to speech not available", Toast.LENGTH_SHORT).show();
        }
    }
}