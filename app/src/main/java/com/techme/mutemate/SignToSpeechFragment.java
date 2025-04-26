package com.techme.mutemate;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.List;

public class SignToSpeechFragment extends Fragment implements SignLanguageAnalyzer.SignDetectionListener {
    private static final String TAG = "SignToSpeechFragment";

    // UI Components
    private PreviewView previewView;
    private TextView tvDetectedText;
    private Button btnSpeak;
    private Button btnClear;
    private CardView cameraCard;
    private CardView textResultCard;
    private FloatingActionButton btnToggleCamera;
    
    // Add HandOverlayView reference
    private HandOverlayView handOverlayView;

    // Camera handling
    private CameraHandler cameraHandler;
    private SignLanguageAnalyzer signLanguageAnalyzer;

    // Phrase building
    private StringBuilder currentPhrase = new StringBuilder();
    private String lastDetectedSign = "";
    private long lastSignTimestamp = 0;
    private static final long SAME_SIGN_TIMEOUT_MS = 1000; // Set to 1 second as requested
    private static final String BACKSPACE_SIGN = "Delete"; // Sign name for deletion
    private static final String CLEAR_SIGN = "Clear"; // Sign name to clear all text
    
    // Confidence threshold to reduce false positives
    private static final float MIN_CONFIDENCE_THRESHOLD = 0.7f; // Minimum confidence to accept a sign

    // Callbacks
    private OnFragmentInteractionListener mListener;

    public SignToSpeechFragment() {
        // Required empty public constructor
    }

    public static SignToSpeechFragment newInstance() {
        return new SignToSpeechFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                          Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_to_speech, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize UI components
        initializeViews(view);

        // Set up button click listeners
        setupButtonListeners();

        // Initialize camera when view is created
        initializeCamera();
        
        // Initialize the phrase from any existing text (e.g., after rotation)
        String existingText = tvDetectedText.getText().toString();
        if (existingText != null && !existingText.isEmpty() && 
            !existingText.equals(getString(R.string.text_hint))) {
            currentPhrase = new StringBuilder(existingText);
        }
    }

    private void initializeViews(View view) {
        previewView = view.findViewById(R.id.previewView);
        tvDetectedText = view.findViewById(R.id.tvDetectedText);
        btnSpeak = view.findViewById(R.id.btnSpeak);
        btnClear = view.findViewById(R.id.btnClear);
        btnToggleCamera = view.findViewById(R.id.btnToggleCamera);
        
        // Get the HandOverlayView from layout and hide it
        handOverlayView = view.findViewById(R.id.handOverlayView);
        if (handOverlayView != null) {
            // Disable the hand overlay
            handOverlayView.setVisibility(View.GONE);
            Log.d(TAG, "HandOverlayView found in layout and disabled");
        }

        // Find card containers
        cameraCard = view.findViewById(R.id.cameraCard);
        textResultCard = view.findViewById(R.id.textResultCard);
    }

    private void setupButtonListeners() {
        btnSpeak.setOnClickListener(v -> {
            // Speak the detected text
            speakDetectedText();
        });

        btnToggleCamera.setOnClickListener(v -> {
            if (cameraHandler != null) {
                cameraHandler.toggleCamera();
            }
        });
        
        // Check if btnClear exists (might be added in the layout)
        if (btnClear != null) {
            btnClear.setOnClickListener(v -> {
                clearPhrase();
                Toast.makeText(requireContext(), "Text cleared", Toast.LENGTH_SHORT).show();
            });
            
            // Add long press for delete functionality
            btnClear.setOnLongClickListener(v -> {
                deleteLastCharacter();
                return true;
            });
        }
        
        // Add a long press listener to clear the detected text (as a backup)
        btnSpeak.setOnLongClickListener(v -> {
            clearPhrase();
            Toast.makeText(requireContext(), "Text cleared", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void speakDetectedText() {
        String text = tvDetectedText.getText().toString();
        if (text.isEmpty() || text.equals(getString(R.string.text_hint))) {
            Toast.makeText(requireContext(), "No text to speak", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use the TTS functionality from the activity
        if (mListener != null) {
            mListener.onSpeakText(text);
        } else {
            // Fallback if listener isn't available
            Toast.makeText(requireContext(), "Speaking: " + text, Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeCamera() {
        if (getActivity() == null) return;
        
        // Create and initialize camera handler
        cameraHandler = new CameraHandler(requireContext(), previewView, requireActivity());

        // Create sign language analyzer
        signLanguageAnalyzer = new SignLanguageAnalyzer(this);
        
        // Initialize the analyzer with context
        signLanguageAnalyzer.initialize(requireContext());

        // Start camera with callback when initialized
        cameraHandler.startCamera(this::onCameraReady);
    }

    private void onCameraReady() {
        // Set image analyzer for sign detection
        cameraHandler.setImageAnalyzer(signLanguageAnalyzer);

        // Enable camera toggle button if both cameras available
        btnToggleCamera.setEnabled(
            cameraHandler.hasFrontCamera() && cameraHandler.hasBackCamera());
    }

    // SignLanguageAnalyzer.SignDetectionListener implementation
    @Override
    public void onSignDetected(String detectedText) {
        // Update UI with detected sign on the main thread
        requireActivity().runOnUiThread(() -> {
            if (detectedText != null && !detectedText.isEmpty()) {
                // Process the detected sign and add to phrase
                processDetectedSign(detectedText, 0.7f); // Default confidence
            }
        });
    }

    @Override
    public void onProcessingFrame() {
        // Optional: update UI to show we're processing
    }

    @Override
    public void onError(String error) {
        // Show error message
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
        });
    }
    
    // New method from SignDetectionListener interface
    @Override
    public void onHandLandmarksDetected(List<NormalizedLandmark> handLandmarks, int width, int height, boolean isRightHand) {
        // Disabled hand overlay as requested
        if (handOverlayView != null) {
            // Hide the overlay view entirely
            handOverlayView.setVisibility(View.GONE);
        }
    }

    // New overloaded method from SignDetectionListener interface
    @Override
    public void onSignDetected(String detectedText, List<NormalizedLandmark> landmarks, float confidence) {
        // Update UI with detected sign on the main thread
        requireActivity().runOnUiThread(() -> {
            if (detectedText != null && !detectedText.isEmpty()) {
                // Process the detected sign with confidence information
                processDetectedSign(detectedText, confidence);
                
                // Status updates removed - no need to show confidence information
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume camera when fragment is resumed
        if (cameraHandler != null) {
            cameraHandler.startImageAnalysis();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause camera when fragment is paused
        if (cameraHandler != null) {
            cameraHandler.stopImageAnalysis();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Clean up camera resources
        if (cameraHandler != null) {
            cameraHandler.stopImageAnalysis();
            cameraHandler.shutdown();
        }
        
        // Clean up analyzer
        if (signLanguageAnalyzer != null) {
            signLanguageAnalyzer.close();
        }
    }

    public interface OnFragmentInteractionListener {
        void onSpeakText(String text);
    }

    /**
     * Process a detected sign and add it to the current phrase
     * @param sign The detected sign text
     * @param confidence Confidence level (0-1)
     */
    private void processDetectedSign(String sign, float confidence) {
        // Ignore if confidence is too low
        if (confidence < MIN_CONFIDENCE_THRESHOLD) {
            return;
        }

        // Handle special commands
        if (CLEAR_SIGN.equalsIgnoreCase(sign)) {
            clearPhrase();
            return;
        } else if (BACKSPACE_SIGN.equalsIgnoreCase(sign)) {
            deleteLastCharacter();
            return;
        }

        // Check if it's the same sign as last time and within timeout
        long currentTime = System.currentTimeMillis();
        if (sign.equals(lastDetectedSign) && 
            (currentTime - lastSignTimestamp) < SAME_SIGN_TIMEOUT_MS) {
            // Same sign detected within timeout period, don't add it again
            return;
        }

        // Update our tracking variables
        lastDetectedSign = sign;
        lastSignTimestamp = currentTime;

        // Append the detected sign to the phrase
        if (currentPhrase.length() == 0) {
            // Capitalize the first letter of the phrase
            currentPhrase.append(capitalize(sign));
        } else {
            // Add a space before the next word if needed
            if (!currentPhrase.toString().endsWith(" ")) {
                currentPhrase.append(" ");
            }
            currentPhrase.append(sign.toLowerCase());
        }

        // Update the UI
        tvDetectedText.setText(currentPhrase.toString());
        
        // Optional: provide haptic feedback when a sign is added
        if (getContext() != null) {
            try {
                android.os.Vibrator vibrator = (android.os.Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    // Vibrate for a short time
                    vibrator.vibrate(50);
                }
            } catch (Exception e) {
                Log.e(TAG, "Could not provide haptic feedback", e);
            }
        }
    }
    
    /**
     * Clear the current phrase
     */
    private void clearPhrase() {
        currentPhrase.setLength(0);
        tvDetectedText.setText("");
    }

    /**
     * Delete the last character from the current phrase
     */
    private void deleteLastCharacter() {
        if (currentPhrase.length() > 0) {
            currentPhrase.deleteCharAt(currentPhrase.length() - 1);
            tvDetectedText.setText(currentPhrase.toString());
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
} 