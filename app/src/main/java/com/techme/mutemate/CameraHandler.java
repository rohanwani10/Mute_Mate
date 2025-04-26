package com.techme.mutemate;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.widget.Toast;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraHandler {
    private static final String TAG = "CameraHandler";

    private final Context context;
    private final PreviewView previewView;
    private final LifecycleOwner lifecycleOwner;

    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private Preview preview;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalysis;
    private Camera camera;
    private ExecutorService cameraExecutor;

    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private ImageAnalysis.Analyzer currentAnalyzer;

    public interface CameraInitializedCallback {
        void onCameraInitialized();
    }

    public CameraHandler(Context context, PreviewView previewView, LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.previewView = previewView;
        this.lifecycleOwner = lifecycleOwner;

        cameraExecutor = Executors.newSingleThreadExecutor();
        setupDisplayListener();
    }

    private void setupDisplayListener() {
        // Handle the DisplayManagerGlobal error by conditionally setting up display listener
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12+ (API 31+) - PreviewView's default behavior works correctly
        } else {
            // For older Android versions - workaround for NoSuchMethodError
            try {
                previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);

                // Manual display change listener to avoid problematic method
                DisplayManager displayManager =
                        (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
                displayManager.registerDisplayListener(
                        new DisplayManager.DisplayListener() {
                            @Override
                            public void onDisplayAdded(int displayId) {}

                            @Override
                            public void onDisplayRemoved(int displayId) {}

                            @Override
                            public void onDisplayChanged(int displayId) {
                                // Handle rotation changes manually if needed
                                if (previewView.getDisplay() != null &&
                                        previewView.getDisplay().getDisplayId() == displayId) {
                                    // Update camera UI or orientation as needed
                                }
                            }
                        },
                        new Handler(Looper.getMainLooper())
                );
            } catch (Exception e) {
                Log.e(TAG, "Error setting up display listener", e);
            }
        }
    }

    public void startCamera(CameraInitializedCallback callback) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();

                if (callback != null) {
                    callback.onCameraInitialized();
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: ", e);
                Toast.makeText(context, "Failed to start camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) {
            return;
        }

        // Unbind any previous use cases
        cameraProvider.unbindAll();

        try {
            // Check if the requested camera is available
            if ((lensFacing == CameraSelector.LENS_FACING_BACK && !hasBackCamera()) ||
                    (lensFacing == CameraSelector.LENS_FACING_FRONT && !hasFrontCamera())) {
                Log.e(TAG, "Requested camera not available");
                Toast.makeText(context, "Requested camera not available", Toast.LENGTH_SHORT).show();
                // Default to another camera if available
                if (lensFacing == CameraSelector.LENS_FACING_BACK && hasFrontCamera()) {
                    lensFacing = CameraSelector.LENS_FACING_FRONT;
                } else if (lensFacing == CameraSelector.LENS_FACING_FRONT && hasBackCamera()) {
                    lensFacing = CameraSelector.LENS_FACING_BACK;
                } else {
                    // No cameras available
                    return;
                }
            }

            // Select front or back camera
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build();

            // Set up preview use case
            preview = new Preview.Builder().build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            // Set up image capture use case
            imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build();

            // Set up image analysis use case
            imageAnalysis = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build();

            if (currentAnalyzer != null) {
                imageAnalysis.setAnalyzer(cameraExecutor, currentAnalyzer);
            }

            // Bind all use cases to camera
            camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalysis
            );

            // Set up pinch to zoom
            setupZoomListener();
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
            Toast.makeText(context, "Camera initialization failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupZoomListener() {
        CameraControl cameraControl = camera.getCameraControl();
        CameraInfo cameraInfo = camera.getCameraInfo();

        ScaleGestureDetector.SimpleOnScaleGestureListener listener =
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        if (cameraInfo.getZoomState().getValue() == null) {
                            return false;
                        }

                        float currentZoomRatio = cameraInfo.getZoomState().getValue().getZoomRatio();
                        float delta = detector.getScaleFactor();

                        cameraControl.setZoomRatio(currentZoomRatio * delta);
                        return true;
                    }
                };

        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(context, listener);
        previewView.setOnTouchListener((view, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });
    }

    public void toggleCamera() {
        if ((lensFacing == CameraSelector.LENS_FACING_BACK && hasFrontCamera()) ||
                (lensFacing == CameraSelector.LENS_FACING_FRONT && hasBackCamera())) {
            lensFacing = (lensFacing == CameraSelector.LENS_FACING_BACK) ?
                    CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
            bindCameraUseCases();
        } else {
            Toast.makeText(context, "Camera switch not available", Toast.LENGTH_SHORT).show();
        }
    }

    public void setImageAnalyzer(ImageAnalysis.Analyzer analyzer) {
        currentAnalyzer = analyzer;
        if (imageAnalysis != null && cameraExecutor != null && !cameraExecutor.isShutdown()) {
            imageAnalysis.setAnalyzer(cameraExecutor, currentAnalyzer);
        }
    }

    public void startImageAnalysis() {
        if (imageAnalysis != null && currentAnalyzer != null &&
                cameraExecutor != null && !cameraExecutor.isShutdown()) {
            imageAnalysis.setAnalyzer(cameraExecutor, currentAnalyzer);
        }
    }

    public void stopImageAnalysis() {
        if (imageAnalysis != null) {
            imageAnalysis.clearAnalyzer();
        }
    }

    public void captureImage(ImageCapture.OnImageCapturedCallback callback) {
        if (imageCapture == null || cameraExecutor == null || cameraExecutor.isShutdown()) {
            Log.e(TAG, "Cannot capture image - camera not initialized or executor shutdown");
            return;
        }

        imageCapture.takePicture(cameraExecutor, callback);
    }

    public void captureImage(ImageCapture.OutputFileOptions outputFileOptions,
                             ImageCapture.OnImageSavedCallback callback) {
        if (imageCapture == null || cameraExecutor == null || cameraExecutor.isShutdown()) {
            Log.e(TAG, "Cannot capture image - camera not initialized or executor shutdown");
            return;
        }

        imageCapture.takePicture(outputFileOptions, cameraExecutor, callback);
    }

    public boolean hasFrontCamera() {
        try {
            if (cameraProvider == null) {
                ProcessCameraProvider provider = ProcessCameraProvider.getInstance(context).get();
                return provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA);
            }
            return cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA);
        } catch (Exception e) {
            Log.e(TAG, "Error checking for front camera: ", e);
            return false;
        }
    }

    public boolean hasBackCamera() {
        try {
            if (cameraProvider == null) {
                ProcessCameraProvider provider = ProcessCameraProvider.getInstance(context).get();
                return provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
            }
            return cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
        } catch (Exception e) {
            Log.e(TAG, "Error checking for back camera: ", e);
            return false;
        }
    }

    public int getCurrentLensFacing() {
        return lensFacing;
    }

    public void shutdown() {
        try {
            if (cameraProvider != null) {
                cameraProvider.unbindAll();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error unbinding camera use cases", e);
        } finally {
            if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
                cameraExecutor.shutdown();
            }
        }
    }
}