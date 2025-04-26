package com.techme.mutemate;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.components.containers.Category;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.Collections;

public class SignLanguageAnalyzer implements ImageAnalysis.Analyzer, GestureRecognizerHelper.GestureRecognizerListener {
    private static final String TAG = "SignLanguageAnalyzer";
    
    // Interface for communicating results to the activity
    public interface SignDetectionListener {
        void onSignDetected(String detectedText);
        void onSignDetected(String detectedText, List<NormalizedLandmark> landmarks, float confidence);
        void onProcessingFrame(); // Optional: called when starting to process a frame
        void onError(String error);
        // Add method for hand landmark detection
        void onHandLandmarksDetected(List<NormalizedLandmark> handLandmarks, int width, int height, boolean isRightHand);
    }
    
    private final SignDetectionListener listener;
    private final Executor executor;
    private boolean isProcessing = false;
    private Context context;
    private GestureRecognizerHelper gestureRecognizerHelper;
    
    // For debouncing results - process frames less frequently for a good balance
    private long lastProcessedTimestamp = 0;
    private static final long PROCESSING_INTERVAL_MS = 500; // Set to 500ms (0.5s)
    
    // For sign refreshing - reset detection if no sign for a while
    private long lastSignDetectionTime = 0;
    private static final long SIGN_RESET_TIMEOUT_MS = 1000; // Set to 1 second as requested
    
    // Confidence threshold for detection
    private static final float CONFIDENCE_THRESHOLD = 0.65f; // Slightly increased threshold for more reliable detection
    
    // Keep track of last detected signs to build phrases
    private StringBuilder currentPhrase = new StringBuilder();
    private String lastDetectedSign = "";
    private int sameSignCount = 0;
    private static final int SIGN_CONFIRMATION_THRESHOLD = 1; // Set to 1 for faster detection
    
    // For debugging
    private boolean debugLogging = true; // Set to true to enable debugging logs
    
    // Image dimensions for hand landmarks
    private int imageWidth = 0;
    private int imageHeight = 0;
    
    private boolean isPaused = false;
    
    public SignLanguageAnalyzer(SignDetectionListener listener) {
        this.listener = listener;
        this.executor = Executors.newSingleThreadExecutor();
        Log.d(TAG, "SignLanguageAnalyzer created");
    }
    
    public void initialize(Context context) {
        this.context = context;
        Log.d(TAG, "Initializing SignLanguageAnalyzer");
        
        // Initialize the gesture recognizer helper
        try {
            Log.d(TAG, "Creating GestureRecognizerHelper with model at: " + GestureRecognizerHelper.MP_RECOGNIZER_TASK);
            gestureRecognizerHelper = new GestureRecognizerHelper(
                    GestureRecognizerHelper.DEFAULT_HAND_DETECTION_CONFIDENCE,
                    GestureRecognizerHelper.DEFAULT_HAND_TRACKING_CONFIDENCE,
                    GestureRecognizerHelper.DEFAULT_HAND_PRESENCE_CONFIDENCE,
                    GestureRecognizerHelper.DELEGATE_CPU,
                    RunningMode.LIVE_STREAM,
                    context,
                    this
            );
            Log.d(TAG, "GestureRecognizerHelper created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing gesture recognizer", e);
            if (listener != null) {
                listener.onError("Failed to initialize sign language analyzer: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void analyze(@NonNull ImageProxy image) {
        long currentTimestamp = System.currentTimeMillis();
        
        // Check if we can process this frame based on time interval
        if (currentTimestamp - lastProcessedTimestamp < PROCESSING_INTERVAL_MS) {
            image.close(); // Important: close the image if we're not processing it
            return;
        }
        
        // Update timestamp for next interval check
        lastProcessedTimestamp = currentTimestamp;
        
        // Don't process if we're already processing or paused
        if (isProcessing || isPaused) {
            image.close();
            return;
        }
        
        // Mark as processing to prevent parallel analysis
        isProcessing = true;
        
        try {
            // Process the image with gesture recognizer
            gestureRecognizerHelper.recognizeLiveStream(image);
            // Note: image will be closed inside recognizeLiveStream or in the finally block
        } catch (Exception e) {
            Log.e(TAG, "Error processing image: " + e.getMessage());
            if (listener != null) {
                listener.onError("Error processing image: " + e.getMessage());
            }
            isProcessing = false;
            image.close();
        }
    }
    
    private void processImageBasic(ImageProxy image) {
        // Extract image data safely
        executor.execute(() -> {
            try {
                isProcessing = true;
                // Simple demo response
                String detectedSign = "Hello"; // Placeholder
                
                updateDetectedSignFromText(detectedSign);
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing image", e);
                if (listener != null) {
                    listener.onError("Failed to analyze image: " + e.getMessage());
                }
            } finally {
                isProcessing = false;
                image.close();
            }
        });
    }
    
    /**
     * Updates the detected sign based on the recognition results.
     * 
     * @param result The recognition result to process
     */
    public void updateDetectedSign(GestureRecognizerResult result) {
        // Process only if we have categories and landmarks
        if (result.gestures().isEmpty() || result.landmarks().isEmpty()) {
            if (listener != null) {
                listener.onSignDetected("", Collections.emptyList(), 0.0f);
            }
            isProcessing = false;
            return;
        }

        // Get top gesture categories
        List<Category> categories = result.gestures().get(0);
        
        if (categories.isEmpty()) {
            isProcessing = false;
            return;
        }
        
        Category topCategory = categories.get(0);
        String signName = topCategory.categoryName();
        float confidence = topCategory.score();
        
        // Get landmarks for visualization
        List<NormalizedLandmark> landmarks = result.landmarks().get(0);
        
        // Fast-path for empty sign detection (no sign or unclear gesture)
        if (signName.equals("Empty") || confidence < CONFIDENCE_THRESHOLD) {
            // Reset consecutive count for the same sign if we detect empty
            if (sameSignCount > 0) {
                sameSignCount = 0;
                lastDetectedSign = "";
            }
            
            if (listener != null) {
                listener.onSignDetected("", landmarks, 0.0f);
            }
            isProcessing = false;
            return;
        }
        
        // Stability filtering: only update display when same sign appears consistently
        if (signName.equals(lastDetectedSign)) {
            sameSignCount++;
        } else {
            sameSignCount = 1;
            lastDetectedSign = signName;
        }
        
        // Only notify listener if we've seen the sign multiple times (stability threshold)
        if (sameSignCount >= SIGN_CONFIRMATION_THRESHOLD) {
            lastSignDetectionTime = System.currentTimeMillis();
            
            if (listener != null) {
                listener.onSignDetected(signName, landmarks, confidence);
            }
        }
        
        // Mark as no longer processing
        isProcessing = false;
    }
    
    public void resetDetection() {
        currentPhrase = new StringBuilder();
        lastDetectedSign = "";
        sameSignCount = 0;
        
        // Notify listener of reset directly
        if (listener != null) {
            listener.onSignDetected("");
        }
    }
    
    @Override
    public void onResults(GestureRecognizerHelper.ResultBundle resultBundle) {
        // Quick validation of results
        if (resultBundle == null || resultBundle.results.isEmpty()) {
            isProcessing = false;
            return;
        }
        
        GestureRecognizerResult result = resultBundle.results.get(0);
        
        // Process hand landmarks for visualization (if needed)
        processHandLandmarks(result);
        
        // Update sign detection with the results
        updateDetectedSign(result);
    }
    
    // New method to process hand landmarks for overlay
    private void processHandLandmarks(GestureRecognizerResult result) {
        if (listener == null) return;
        
        try {
            // Process landmarks for each hand
            if (!result.landmarks().isEmpty()) {
                Log.d(TAG, "Found " + result.landmarks().size() + " hands in the landmarks");
                
                // If no hands detected, clear the overlay
                if (result.landmarks().size() == 0) {
                    Log.d(TAG, "No hands detected, clearing overlay");
                    listener.onHandLandmarksDetected(null, imageWidth, imageHeight, false);
                    return;
                }
                
                // Process each hand
                for (int handIndex = 0; handIndex < result.landmarks().size(); handIndex++) {
                    List<NormalizedLandmark> handLandmarks = result.landmarks().get(handIndex);
                    
                    if (handLandmarks != null && !handLandmarks.isEmpty()) {
                        // Determine if this is the right hand 
                        // MediaPipe doesn't directly tell us which hand it is in GestureRecognizer
                        // Typically first detected hand is right and second is left, but not guaranteed
                        boolean isRightHand = (handIndex == 0);
                        
                        // More reliable with multiple hands:
                        // If we have handedness info from MediaPipe
                        // isRightHand = result.handedness().get(handIndex).get(0).categoryName().equals("Right");
                        
                        Log.d(TAG, "Hand " + handIndex + " landmarks detected: " + handLandmarks.size() + 
                              " points, treating as " + (isRightHand ? "right" : "left") + " hand");
                        
                        // Log some landmark positions for debugging
                        if (handLandmarks.size() > 0) {
                            NormalizedLandmark palm = handLandmarks.get(0);
                            Log.d(TAG, "Palm position: x=" + palm.x() + ", y=" + palm.y());
                        }
                        
                        // Send landmarks to UI for drawing overlay
                        listener.onHandLandmarksDetected(handLandmarks, imageWidth, imageHeight, isRightHand);
                    }
                }
            } else {
                Log.d(TAG, "No hand landmarks in result");
                listener.onHandLandmarksDetected(null, imageWidth, imageHeight, false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing hand landmarks", e);
        }
    }
    
    @Override
    public void onError(String error, int errorCode) {
        Log.e(TAG, "Gesture recognizer error: " + error + " (code: " + errorCode + ")");
        if (listener != null) {
            listener.onError("Sign language detection error: " + error);
        }
        isProcessing = false;
    }
    
    public void close() {
        try {
            if (gestureRecognizerHelper != null) {
                gestureRecognizerHelper.clearGestureRecognizer();
                gestureRecognizerHelper = null;
            }
            
            // Shutdown executor service
            if (executor instanceof java.util.concurrent.ExecutorService) {
                ((java.util.concurrent.ExecutorService) executor).shutdown();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error closing SignLanguageAnalyzer", e);
        }
    }
    
    /**
     * Pauses sign detection
     */
    public void pause() {
        isPaused = true;
    }
    
    /**
     * Resumes sign detection
     */
    public void resume() {
        isPaused = false;
    }
    
    /**
     * Returns whether detection is currently paused
     */
    public boolean isPaused() {
        return isPaused;
    }
    
    // For handling simple text-based sign detection (backward compatibility)
    private void updateDetectedSignFromText(String detectedSign) {
        if (detectedSign == null || detectedSign.isEmpty()) {
            if (listener != null) {
                listener.onSignDetected("");
            }
            isProcessing = false;
            return;
        }
        
        // Simple text-based detection just updates UI
        if (listener != null) {
            listener.onSignDetected(detectedSign);
        }
        
        isProcessing = false;
    }
} 