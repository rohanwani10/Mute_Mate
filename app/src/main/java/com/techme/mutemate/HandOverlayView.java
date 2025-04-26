package com.techme.mutemate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;

import java.util.ArrayList;
import java.util.List;

/**
 * A view that draws hand landmarks over the camera preview
 * Based on the MediaPipe Gesture Recognizer sample implementation
 */
public class HandOverlayView extends View {
    private static final String TAG = "HandOverlayView";
    
    // For drawing
    private final Paint pointPaint;
    private final Paint linePaint;
    private final Paint testPaint; // Cached test paint
    
    // Result from the GestureRecognizer
    private GestureRecognizerResult result;
    
    // Image and scale information
    private int imageWidth = 1;
    private int imageHeight = 1;
    private float scaleFactor = 1f;
    
    // Constants
    private static final float LANDMARK_STROKE_WIDTH = 8f;
    private static final float LANDMARK_RADIUS = 10f; // Consistent size for landmarks
    private static final boolean DRAW_TEST_BORDER = false; // Toggle for test border
    
    // Connections for hand landmarks - using the same list as MediaPipe
    private static final int[][] HAND_CONNECTIONS = {
            // Thumb connections
            {0, 1}, {1, 2}, {2, 3}, {3, 4},
            // Index finger connections
            {0, 5}, {5, 6}, {6, 7}, {7, 8},
            // Middle finger connections
            {0, 9}, {9, 10}, {10, 11}, {11, 12},
            // Ring finger connections
            {0, 13}, {13, 14}, {14, 15}, {15, 16},
            // Pinky finger connections
            {0, 17}, {17, 18}, {18, 19}, {19, 20},
            // Palm connections
            {0, 5}, {5, 9}, {9, 13}, {13, 17}
    };
    
    // Reusable lists to avoid garbage collection
    private final List<List<NormalizedLandmark>> reuseHandLandmarksList = new ArrayList<>();
    
    public HandOverlayView(Context context) {
        this(context, null);
    }
    
    public HandOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        // Initialize point paint for landmarks with better visibility
        pointPaint = new Paint();
        pointPaint.setColor(Color.GREEN); // Change to brighter green for better visibility
        pointPaint.setStrokeWidth(LANDMARK_STROKE_WIDTH);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true); // Smoother drawing
        
        // Initialize line paint for connections
        linePaint = new Paint();
        linePaint.setColor(Color.CYAN); // Brighter cyan color for better visibility
        linePaint.setStrokeWidth(LANDMARK_STROKE_WIDTH);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true); // Smoother drawing
        
        // Initialize test paint - create once to avoid garbage collection
        testPaint = new Paint();
        testPaint.setColor(Color.RED);
        testPaint.setStyle(Paint.Style.STROKE);
        testPaint.setStrokeWidth(2);
        
        // Make sure we can draw
        setWillNotDraw(false);
        
        Log.d(TAG, "HandOverlayView initialized");
    }
    
    /**
     * Clear all results
     */
    public void clear() {
        result = null;
        invalidate();
    }
    
    /**
     * Set the gesture recognizer results to be visualized
     */
    public void setResults(
            GestureRecognizerResult gestureResult,
            int imageHeight, 
            int imageWidth,
            RunningMode runningMode) {
        
        this.result = gestureResult;
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
        
        // Calculate scale factor based on running mode (same logic as Kotlin implementation)
        switch (runningMode) {
            case IMAGE:
            case VIDEO:
                // Scale down to fit
                scaleFactor = Math.min(getWidth() * 1f / imageWidth, getHeight() * 1f / imageHeight);
                break;
            case LIVE_STREAM:
                // Scale up to fill
                scaleFactor = Math.max(getWidth() * 1f / imageWidth, getHeight() * 1f / imageHeight);
                break;
        }
        
        // Force redraw
        invalidate();
    }
    
    /**
     * Compatibility method for the existing API until we can refactor
     * to use GestureRecognizerResult directly
     */
    public void updateHandLandmarks(List<NormalizedLandmark> landmarks, int width, int height, boolean isRightHand) {
        // Clear existing data if landmarks is null
        if (landmarks == null || landmarks.isEmpty()) {
            clear();
            return;
        }
        
        // Draw directly from the landmarks instead of creating a mock GestureRecognizerResult
        this.imageWidth = width;
        this.imageHeight = height;
        
        // Calculate scale factor for live stream (same as in setResults)
        scaleFactor = Math.max(getWidth() * 1f / width, getHeight() * 1f / height);
        
        // Store landmarks for drawing
        reuseHandLandmarksList.clear();
        reuseHandLandmarksList.add(landmarks);
        
        // Custom drawing without using GestureRecognizerResult
        drawHandLandmarks(reuseHandLandmarksList);
    }
    
    /**
     * Draw hand landmarks directly without using GestureRecognizerResult
     */
    private void drawHandLandmarks(List<List<NormalizedLandmark>> handLandmarksList) {
        // Force redraw to use our custom drawing logic
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (getWidth() == 0 || getHeight() == 0) {
            // Nothing to draw yet
            return;
        }
        
        // Draw landmarks from GestureRecognizerResult if available
        if (result != null && !result.landmarks().isEmpty()) {
            drawLandmarksFromResult(canvas, result.landmarks());
            return;
        }
        
        // Otherwise draw from our stored landmarks list
        if (!reuseHandLandmarksList.isEmpty()) {
            drawLandmarksFromResult(canvas, reuseHandLandmarksList);
        }
    }
    
    /**
     * Common drawing logic for landmarks regardless of source
     */
    private void drawLandmarksFromResult(Canvas canvas, List<List<NormalizedLandmark>> landmarksList) {
        // Local variables to avoid repeated calculations
        final float scaleX = imageWidth * scaleFactor;
        final float scaleY = imageHeight * scaleFactor;
        
        // Draw test border if enabled
        if (DRAW_TEST_BORDER) {
            canvas.drawRect(10, 10, getWidth() - 10, getHeight() - 10, testPaint);
        }
        
        // Draw landmarks for each hand
        for (List<NormalizedLandmark> handLandmarks : landmarksList) {
            // Draw connections first so points appear on top
            for (int[] connection : HAND_CONNECTIONS) {
                if (connection.length == 2 && 
                    connection[0] < handLandmarks.size() && 
                    connection[1] < handLandmarks.size()) {
                    
                    NormalizedLandmark start = handLandmarks.get(connection[0]);
                    NormalizedLandmark end = handLandmarks.get(connection[1]);
                    
                    canvas.drawLine(
                        start.x() * scaleX,
                        start.y() * scaleY,
                        end.x() * scaleX,
                        end.y() * scaleY,
                        linePaint
                    );
                }
            }
            
            // Draw points on top of connections
            for (NormalizedLandmark landmark : handLandmarks) {
                canvas.drawCircle(
                    landmark.x() * scaleX,
                    landmark.y() * scaleY,
                    LANDMARK_RADIUS, // Use constant size
                    pointPaint
                );
            }
        }
    }
}