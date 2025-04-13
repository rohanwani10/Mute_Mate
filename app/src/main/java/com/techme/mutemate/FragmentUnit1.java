package com.techme.mutemate;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class FragmentUnit1 extends Fragment {

    ImageView l1, l2, l3, l4, l5, l6, ch1_test;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unit1, container, false);

        l1 = view.findViewById(R.id.level1);
        l2 = view.findViewById(R.id.level2);
        l3 = view.findViewById(R.id.level3);
        l4 = view.findViewById(R.id.level4);
        l5 = view.findViewById(R.id.level5);
        l6 = view.findViewById(R.id.level6);
        ch1_test = view.findViewById(R.id.test);

        l1.setOnClickListener(v -> LearningModule.showDialog("Level 1 - Importance of Sign Language", "Start +100 XP", 1));
        l2.setOnClickListener(v -> LearningModule.showDialog("Level 2 - Alphabets", "Start +100 XP",2));
        l3.setOnClickListener(v -> LearningModule.showDialog("Level 3 - Numbers (1-20)", "Start +100 XP",3));
        l4.setOnClickListener(v -> LearningModule.showDialog("Level 4 - Numbers (21-100)", "Start +100 XP",4));
        l5.setOnClickListener(v -> LearningModule.showDialog("   Level 5 - Colors in Sign Language   ", "Start +100 XP",5));
        l6.setOnClickListener(v -> LearningModule.showDialog("Level 6 - Emotions through Signs", "Start +100 XP",6));
        ch1_test.setOnClickListener(v -> LearningModule.showDialog("Test 1 - The Beginning", "Start +500 XP", 0));

        return view;
    }
}
