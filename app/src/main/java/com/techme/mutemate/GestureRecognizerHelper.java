package com.techme.mutemate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;

import androidx.annotation.Nullable;
import androidx.camera.core.ImageProxy;

import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer;
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GestureRecognizerHelper {

    public static final int DELEGATE_CPU = 0;
    public static final int DELEGATE_GPU = 1;
    public static final float DEFAULT_HAND_DETECTION_CONFIDENCE = 0.5f;
    public static final float DEFAULT_HAND_TRACKING_CONFIDENCE = 0.5f;
    public static final float DEFAULT_HAND_PRESENCE_CONFIDENCE = 0.5f;
    public static final int OTHER_ERROR = 0;
    public static final int GPU_ERROR = 1;
    public static final String MP_RECOGNIZER_TASK = "sign_language.task";
    private static final String TAG = "GestureRecognizerHelper";

    private GestureRecognizer gestureRecognizer;
    private final float minHandDetectionConfidence;
    private final float minHandTrackingConfidence;
    private final float minHandPresenceConfidence;
    private final int currentDelegate;
    private final RunningMode runningMode;
    private final Context context;
    private final GestureRecognizerListener gestureRecognizerListener;

    public GestureRecognizerHelper(float minDetection, float minTracking, float minPresence,
                                   int delegate, RunningMode mode, Context ctx,
                                   @Nullable GestureRecognizerListener listener) {
        this.minHandDetectionConfidence = minDetection;
        this.minHandTrackingConfidence = minTracking;
        this.minHandPresenceConfidence = minPresence;
        this.currentDelegate = delegate;
        this.runningMode = mode;
        this.context = ctx;
        this.gestureRecognizerListener = listener;

        setupGestureRecognizer();
    }

    public void clearGestureRecognizer() {
        if (gestureRecognizer != null) {
            gestureRecognizer.close();
            gestureRecognizer = null;
        }
    }

    public void setupGestureRecognizer() {
        BaseOptions.Builder baseOptionBuilder = BaseOptions.builder().setModelAssetPath(MP_RECOGNIZER_TASK);
        if (currentDelegate == DELEGATE_CPU) {
            baseOptionBuilder.setDelegate(Delegate.CPU);
        } else {
            baseOptionBuilder.setDelegate(Delegate.GPU);
        }

        try {
            BaseOptions baseOptions = baseOptionBuilder.build();

            GestureRecognizer.GestureRecognizerOptions.Builder optionsBuilder =
                    GestureRecognizer.GestureRecognizerOptions.builder()
                            .setBaseOptions(baseOptions)
                            .setMinHandDetectionConfidence(minHandDetectionConfidence)
                            .setMinTrackingConfidence(minHandTrackingConfidence)
                            .setMinHandPresenceConfidence(minHandPresenceConfidence)
                            .setRunningMode(runningMode);

            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder.setResultListener(this::returnLivestreamResult);
                optionsBuilder.setErrorListener(this::returnLivestreamError);
            }

            gestureRecognizer = GestureRecognizer.createFromOptions(context, optionsBuilder.build());

        } catch (RuntimeException e) {
            if (gestureRecognizerListener != null) {
                gestureRecognizerListener.onError("Gesture recognizer failed to initialize. " + e.getMessage(),
                        e instanceof RuntimeException ? GPU_ERROR : OTHER_ERROR);
            }
            Log.e(TAG, "Failed to initialize recognizer: " + e.getMessage());
        }
    }

    public void recognizeLiveStream(ImageProxy imageProxy) {
        long frameTime = SystemClock.uptimeMillis();

        try{
            // Create bitmap buffer with reusable bitmap if possible
            Bitmap bitmapBuffer = Bitmap.createBitmap(
                imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888
            );
            
            // Copy out RGB bits from the frame to the bitmap buffer
            bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());
            
            // Create transformation matrix
            Matrix matrix = new Matrix();
            
            // Rotate the frame based on device orientation
            int rotation = imageProxy.getImageInfo().getRotationDegrees();
            matrix.postRotate(rotation);
            
            // Flip image for front camera if needed
            matrix.postScale(-1f, 1f, imageProxy.getWidth() / 2f, imageProxy.getHeight() / 2f);
            
            // Optimize: Create rotated bitmap only if needed
            Bitmap processedBitmap;
            if (rotation == 0 && matrix.isIdentity()) {
                // Skip bitmap creation if no transformation needed
                processedBitmap = bitmapBuffer;
            } else {
                // Create transformed bitmap
                processedBitmap = Bitmap.createBitmap(
                    bitmapBuffer,
                    0,
                    0,
                    bitmapBuffer.getWidth(),
                    bitmapBuffer.getHeight(),
                    matrix,
                    true
                );
                // Recycle original bitmap if we created a new one
                if (bitmapBuffer != processedBitmap) {
                    bitmapBuffer.recycle();
                }
            }
            
            // Convert to MPImage and process
            MPImage mpImage = new BitmapImageBuilder(processedBitmap).build();
            
            // Process the image asynchronously
            recognizeAsync(mpImage, frameTime);
            
            // Clean up if processedBitmap is not the same as bitmapBuffer (which would already be recycled)
            if (processedBitmap != bitmapBuffer) {
                processedBitmap.recycle();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing camera frame: " + e.getMessage(), e);
            if (gestureRecognizerListener != null) {
                gestureRecognizerListener.onError("Error processing camera frame: " + e.getMessage(), OTHER_ERROR);
            }
        } finally {
            imageProxy.close();
        }
    }

    private void recognizeAsync(MPImage mpImage, long frameTime) {
        if (gestureRecognizer != null) {
            Log.d(TAG, "Running gesture recognition inference...");
            gestureRecognizer.recognizeAsync(mpImage, frameTime);
        }
    }

    public ResultBundle recognizeImage(Bitmap image) {
        if (runningMode != RunningMode.IMAGE) {
            throw new IllegalArgumentException("Not in IMAGE mode");
        }

        long startTime = SystemClock.uptimeMillis();
        MPImage mpImage = new BitmapImageBuilder(image).build();

        GestureRecognizerResult result = gestureRecognizer.recognize(mpImage);
        if (result != null) {
            long inferenceTime = SystemClock.uptimeMillis() - startTime;
            return new ResultBundle(List.of(result), inferenceTime, image.getHeight(), image.getWidth());
        } else {
            if (gestureRecognizerListener != null) {
                gestureRecognizerListener.onError("Gesture Recognizer failed to recognize.",0);
            }
            return null;
        }
    }

    public ResultBundle recognizeVideoFile(Uri videoUri, long intervalMs) throws IOException {
        if (runningMode != RunningMode.VIDEO) {
            throw new IllegalArgumentException("Not in VIDEO mode");
        }

        long startTime = SystemClock.uptimeMillis();
        boolean didErrorOccur = false;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, videoUri);
        Long duration = Long.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        Bitmap firstFrame = retriever.getFrameAtTime(0);
        if (duration == null || firstFrame == null) return null;

        int width = firstFrame.getWidth();
        int height = firstFrame.getHeight();
        List<GestureRecognizerResult> resultList = new ArrayList<>();

        long numberOfFrames = duration / intervalMs;

        for (long i = 0; i <= numberOfFrames; i++) {
            long timestamp = i * intervalMs;
            Bitmap frame = retriever.getFrameAtTime(timestamp * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
            if (frame != null) {
                Bitmap argbFrame = frame.getConfig() == Bitmap.Config.ARGB_8888
                        ? frame
                        : frame.copy(Bitmap.Config.ARGB_8888, false);
                MPImage mpImage = new BitmapImageBuilder(argbFrame).build();
                GestureRecognizerResult result = gestureRecognizer.recognizeForVideo(mpImage, timestamp);
                if (result != null) resultList.add(result);
                else didErrorOccur = true;
            } else {
                didErrorOccur = true;
            }
        }

        retriever.release();

        if (didErrorOccur) return null;
        long inferencePerFrame = (SystemClock.uptimeMillis() - startTime) / numberOfFrames;
        return new ResultBundle(resultList, inferencePerFrame, height, width);
    }

    public boolean isClosed() {
        return gestureRecognizer == null;
    }

    private void returnLivestreamResult(GestureRecognizerResult result, MPImage input) {
        Log.d(TAG, "returnLivestreamResult called with result: " + (result != null));
        
        if (result != null && !result.gestures().isEmpty() && !result.gestures().get(0).isEmpty()) {
            String topCategory = result.gestures().get(0).get(0).categoryName();
            float confidence = result.gestures().get(0).get(0).score();
            Log.d(TAG, "Detected: '" + topCategory + "' with confidence: " + confidence);
            
            // Log additional details about the gestures
            for (int i = 0; i < Math.min(3, result.gestures().get(0).size()); i++) {
                String category = result.gestures().get(0).get(i).categoryName();
                float score = result.gestures().get(0).get(i).score();
                Log.d(TAG, "  Gesture option " + (i+1) + ": '" + category + "' (confidence: " + score + ")");
            }
        } else {
            Log.d(TAG, "No gestures detected in this frame");
        }
        
        long finishTime = SystemClock.uptimeMillis();
        long inferenceTime = finishTime - result.timestampMs();

        if (gestureRecognizerListener != null) {
            Log.d(TAG, "Forwarding results to listener");
            gestureRecognizerListener.onResults(new ResultBundle(List.of(result), inferenceTime, input.getHeight(), input.getWidth()));
        } else {
            Log.e(TAG, "Gesture recognizer listener is null, can't forward results");
        }
    }

    private void returnLivestreamError(RuntimeException error) {
        if (gestureRecognizerListener != null) {
            gestureRecognizerListener.onError(error.getMessage(), OTHER_ERROR);
        }
    }

    public static class ResultBundle {
        public final List<GestureRecognizerResult> results;
        public final long inferenceTime;
        public final int inputImageHeight;
        public final int inputImageWidth;

        public ResultBundle(List<GestureRecognizerResult> results, long inferenceTime, int height, int width) {
            this.results = results;
            this.inferenceTime = inferenceTime;
            this.inputImageHeight = height;
            this.inputImageWidth = width;
        }
    }

    public interface GestureRecognizerListener {
        void onError(String error, int errorCode);
        void onResults(ResultBundle resultBundle);
    }
}
