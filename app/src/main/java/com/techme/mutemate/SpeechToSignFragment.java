package com.techme.mutemate;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class SpeechToSignFragment extends Fragment {
    private static final String TAG = "SpeechToSignFragment";
    private static final int SPEECH_REQUEST_CODE = 100;

    // UI Components
    private EditText etInputText;
    private Button btnListen;
    private Button btnTranslate;
    private ImageView ivSignGif;
    private CardView signGifCard;
    private CardView textInputCard;

    // Animation handling
    private int currentLetterIndex = 0;
    private String currentText = "";
    private Handler animationHandler = new Handler(Looper.getMainLooper());

    // Callback listener
    private OnFragmentInteractionListener mListener;

    public SpeechToSignFragment() {
        // Required empty public constructor
    }

    public static SpeechToSignFragment newInstance() {
        return new SpeechToSignFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_speech_to_sign, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize UI components
        initializeViews(view);

        // Set up button click listeners
        setupButtonListeners();
    }

    private void initializeViews(View view) {
        etInputText = view.findViewById(R.id.etInputText);
        btnListen = view.findViewById(R.id.btnListen);
        btnTranslate = view.findViewById(R.id.btnTranslate);
        ivSignGif = view.findViewById(R.id.ivSignGif);

        // Find card containers
        signGifCard = view.findViewById(R.id.signGifCard);
        textInputCard = view.findViewById(R.id.textInputCard);

        // Setup clear button
        ImageButton btnClearText = view.findViewById(R.id.btnClearText);
        if (btnClearText != null) {
            btnClearText.setOnClickListener(v -> {
                etInputText.setText("");
                Toast.makeText(requireContext(), "Text cleared", Toast.LENGTH_SHORT).show();
            });
        }

        // Ensure the ImageView has the right properties
        ivSignGif.setScaleType(ImageView.ScaleType.FIT_CENTER);
        
        // Make the containers visible but hide the GIF initially 
        // until we actually have something to display
        signGifCard.setVisibility(View.VISIBLE);
        ivSignGif.setVisibility(View.INVISIBLE);
        
        Log.d(TAG, "Views initialized. ImageView size: " + 
              ivSignGif.getWidth() + "x" + ivSignGif.getHeight());
    }

    private void setupButtonListeners() {
        btnListen.setOnClickListener(v -> {
            // Start speech recognition
            startSpeechRecognition();
        });

        btnTranslate.setOnClickListener(v -> {
            // Translate text to sign
            translateText();
        });
        
        // Add a long-press listener to the Translate button for testing
        btnTranslate.setOnLongClickListener(v -> {
            // This is a test function to load a single GIF directly
            testLoadSpecificGif();
            return true; // Consume the long press
        });
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error: Speech recognition not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    String spokenText = result.get(0);
                    etInputText.setText(spokenText);
                    Toast.makeText(requireContext(), "Speech recognized", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void translateText() {
        currentText = etInputText.getText().toString().trim();
        
        if (currentText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter or speak some text first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Clear any previous translation
        currentLetterIndex = 0;
        
        // Log the text we're going to translate
        Log.d(TAG, "Starting translation for text: '" + currentText + "' (length: " + currentText.length() + ")");
        Toast.makeText(requireContext(), "Translating: " + currentText, Toast.LENGTH_SHORT).show();
        
        // Start displaying signs for the text
        displayNextSign();
    }

    private void displayNextSign() {
        if (currentLetterIndex >= currentText.length()) {
            // Animation sequence complete
            Log.d(TAG, "Translation complete: " + currentText);
            Toast.makeText(requireContext(), "Translation complete: " + currentText, Toast.LENGTH_SHORT).show();
            
            // Stop any ongoing animation
            if (ivSignGif != null && ivSignGif.getDrawable() instanceof GifDrawable) {
                GifDrawable gifDrawable = (GifDrawable) ivSignGif.getDrawable();
                gifDrawable.stop();
            }
            
            // Hide the GIF and clear the image
            ivSignGif.setVisibility(View.INVISIBLE);
            ivSignGif.setImageDrawable(null); // Clear the image
            
            return;
        }
        
        // Get current letter and prepare to display it
        char currentLetter = currentText.charAt(currentLetterIndex);
        String letterStr = String.valueOf(currentLetter);
        
        Log.d(TAG, "Displaying sign for letter '" + letterStr + "' (index: " + currentLetterIndex + " of " + currentText.length() + ")");
        
        // Load and display the GIF for this letter with its natural duration
        loadSignGifWithNaturalDuration(letterStr+"unscreen");
        
        // Move to next letter for the next call
        currentLetterIndex++;

    }

    private void stopSignAnimation() {
        // Remove any pending callbacks
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }
        
        // Stop any currently playing GIF
        if (ivSignGif != null && ivSignGif.getDrawable() instanceof GifDrawable) {
            GifDrawable gifDrawable = (GifDrawable) ivSignGif.getDrawable();
            gifDrawable.stop();
        }
        
        // Reset animation state
        currentLetterIndex = 0;
        
        // Hide the GIF view and clear the image
        if (ivSignGif != null) {
            ivSignGif.setVisibility(View.INVISIBLE);
            ivSignGif.setImageDrawable(null); // Clear the image to free up memory
        }
    }

    private void loadSignGif(String letter) {
        if (getContext() == null) return;
        
        // Default to placeholder if no match
        int gifResourceId;

        // Get the resource ID dynamically based on the letter name
        try {
            // This will look for resources named a, b, c, etc.
            String resourceName = letter;
            if (resourceName.equals(" ")) {
                resourceName = "space"; // Use the space.gif for spaces
            }

            gifResourceId = getResources().getIdentifier(
                    resourceName, "drawable", requireContext().getPackageName());

            // If resource not found, fall back to placeholder
            if (gifResourceId == 0) {
                Log.d(TAG, "Resource not found for letter: " + letter);
                gifResourceId = R.drawable.spaceunscreen;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading gif for letter: " + letter, e);
            gifResourceId = R.drawable.spaceunscreen;
        }

        // Load the GIF using Glide with larger size options
        Glide.with(this)
                .asGif()
                .load(gifResourceId)
                .fitCenter() // Use fitCenter for better proportions in larger display
                .into(ivSignGif);

        // Make sure GIF container is visible
        signGifCard.setVisibility(View.VISIBLE);
    }

    private void loadSignGifWithNaturalDuration(String letter) {
        if (getContext() == null) {
            Log.e(TAG, "Context is null when trying to load GIF for letter: " + letter);
            return;
        }
        
        // Make GIF container and ImageView visible - ensure they're visible every time
        signGifCard.setVisibility(View.VISIBLE);
        ivSignGif.setVisibility(View.VISIBLE);
        
        // Default to placeholder if no match
        int gifResourceId;

        // Get the resource ID dynamically based on the letter name
        try {
            // This will look for resources named a, b, c, etc.
            String resourceName = letter.toLowerCase();
            if (resourceName.equals(" ")) {
                resourceName = "space"; // Use the space.gif for spaces
            }

            Log.d(TAG, "Attempting to load GIF resource: " + resourceName);
            gifResourceId = getResources().getIdentifier(
                    resourceName, "drawable", requireContext().getPackageName());

            // If resource not found, fall back to placeholder
            if (gifResourceId == 0) {
                Log.e(TAG, "Resource not found for letter: " + letter + " (looked for '" + resourceName + "')");
                // Try to load a static letter image or placeholder
                gifResourceId = android.R.drawable.ic_dialog_info;
                Toast.makeText(requireContext(), "Sign for '" + letter + "' not found", Toast.LENGTH_SHORT).show();
                // Use short delay for placeholders
                animationHandler.postDelayed(this::displayNextSign, 500);
                return;
            } else {
                Log.d(TAG, "Found resource ID for " + resourceName + ": " + gifResourceId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading gif for letter: " + letter, e);
            gifResourceId = android.R.drawable.ic_dialog_alert;
            // Use short delay for error cases
            animationHandler.postDelayed(this::displayNextSign, 500);
            return;
        }

        Log.d(TAG, "Using Glide to load GIF with resource ID: " + gifResourceId);

        try {
            // Create a custom target to detect when the GIF animation completes
            DrawableImageViewTarget target = new DrawableImageViewTarget(ivSignGif) {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    super.onResourceReady(resource, transition);
                    
                    if (resource instanceof GifDrawable) {
                        GifDrawable gifDrawable = (GifDrawable) resource;
                        Log.d(TAG, "GIF loaded successfully, setting loop count to 1");
                        
                        // Only play the GIF once
                        gifDrawable.setLoopCount(1);
                        
                        // Register callback to detect when the animation finishes
                        gifDrawable.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                            @Override
                            public void onAnimationEnd(Drawable drawable) {
                                Log.d(TAG, "GIF animation for '" + letter + "' completed naturally");
                                // Move to the next letter when animation finishes
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> displayNextSign());
                                }
                            }
                        });
                    } else {
                        Log.d(TAG, "Resource is not a GifDrawable, using fallback delay");
                        // If not a GIF, use fallback delay
                        animationHandler.postDelayed(SpeechToSignFragment.this::displayNextSign, 1000);
                    }
                }
                
                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    Log.e(TAG, "Failed to load GIF for letter: " + letter);
                    // Move to next letter on failure
                    animationHandler.postDelayed(SpeechToSignFragment.this::displayNextSign, 500);
                }
            };
            
            // Load the GIF using Glide
            Glide.with(requireContext())
                 .asGif() // Explicitly load as GIF 
                 .load(gifResourceId)
                 .override(800, 800) // Request a larger size
                 .diskCacheStrategy(DiskCacheStrategy.RESOURCE) // Cache the processed resource
                 .placeholder(android.R.drawable.ic_menu_gallery)
                 .error(android.R.drawable.ic_dialog_alert)
                         .listener(new RequestListener<GifDrawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                    Log.e(TAG, "Failed to load GIF for letter: " + letter);
                    // Move to next letter on failure
                    animationHandler.postDelayed(SpeechToSignFragment.this::displayNextSign, 500);
                    return false; // Let Glide handle the error drawable
                }

                @Override
                public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                    Log.d(TAG, "GIF loaded successfully, setting loop count to 1");

                    // Only play the GIF once
                    resource.setLoopCount(1);

                    // Register callback to detect when the animation finishes
                    resource.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                        @Override
                        public void onAnimationEnd(Drawable drawable) {
                            Log.d(TAG, "GIF animation for '" + letter + "' completed naturally");
                            // Move to the next letter when animation finishes
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> displayNextSign());
                            }
                        }
                    });
                    return false; // Let Glide handle setting the resource
                }
            })
                    .into(ivSignGif);
            Log.d(TAG, "Glide loading initiated, waiting for animation completion");
            
        } catch (Exception e) {
            Log.e(TAG, "Exception during Glide loading: " + e.getMessage(), e);
            // Move to the next letter if an exception occurs
            animationHandler.postDelayed(this::displayNextSign, 500);
        }
    }

    // Test method to directly load a specific GIF without any sequences or callbacks
    private void testLoadSpecificGif() {
        Toast.makeText(requireContext(), "Testing direct GIF loading...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Testing direct GIF loading");
        
        // Make sure our containers are visible
        signGifCard.setVisibility(View.VISIBLE);
        ivSignGif.setVisibility(View.VISIBLE);
        
        // Try to load the 'a' GIF directly
        try {
            // Create a custom target to detect when the GIF animation completes
            DrawableImageViewTarget target = new DrawableImageViewTarget(ivSignGif) {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    super.onResourceReady(resource, transition);
                    
                    if (resource instanceof GifDrawable) {
                        GifDrawable gifDrawable = (GifDrawable) resource;
                        Log.d(TAG, "Test GIF loaded successfully");
                        
                        // Only play the GIF once
                        gifDrawable.setLoopCount(1);
                        
                        // Register callback to detect when the animation finishes
                        gifDrawable.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                            @Override
                            public void onAnimationEnd(Drawable drawable) {
                                Log.d(TAG, "Test GIF animation completed naturally");
                                Toast.makeText(requireContext(), "GIF animation completed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            };
            
            // Load the 'a.gif' using Glide with animation callbacks
            Glide.with(requireContext())
                 .asGif()
                 .load(R.drawable.aunscreen)
                 .override(800, 800) // Request a larger size for better visibility
                 .diskCacheStrategy(DiskCacheStrategy.NONE) // Skip cache for testing
                 .skipMemoryCache(true) // Skip memory cache for testing
                    .listener(new RequestListener<GifDrawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Failed to load GIF for letter: ");
                            // Move to next letter on failure
                            animationHandler.postDelayed(SpeechToSignFragment.this::displayNextSign, 500);
                            return false; // Let Glide handle the error drawable
                        }

                        @Override
                        public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "GIF loaded successfully, setting loop count to 1");

                            // Only play the GIF once
                            resource.setLoopCount(1);

                            // Register callback to detect when the animation finishes
                            resource.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                                @Override
                                public void onAnimationEnd(Drawable drawable) {
                                    Log.d(TAG, "GIF animation for '"  + "' completed naturally");
                                    // Move to the next letter when animation finishes
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> displayNextSign());
                                    }
                                }
                            });
                            return false; // Let Glide handle setting the resource
                        }
                    })
                    .into(ivSignGif);
            
            Log.d(TAG, "Direct test GIF loading initiated for 'a.gif'");
        } catch (Exception e) {
            Log.e(TAG, "Exception during test GIF loading: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context + 
                    " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop any ongoing animations
        stopSignAnimation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up animation handler
        stopSignAnimation();
    }

    public interface OnFragmentInteractionListener {
        // Add any interaction methods needed
    }
} 