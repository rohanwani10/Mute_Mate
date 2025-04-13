package com.techme.mutemate;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class FragmentUnit5 extends Fragment {

    ImageView l1, l2, l3, l4, l5, l6, ch5_test;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_unit5, container, false);
        l1 = view.findViewById(R.id.ch5_level1);
        l2 = view.findViewById(R.id.ch5_level2);
        l3 = view.findViewById(R.id.ch5_level3);
        l4 = view.findViewById(R.id.ch5_level4);
        l5 = view.findViewById(R.id.ch5_level5);
        l6 = view.findViewById(R.id.ch5_level6);
        ch5_test = view.findViewById(R.id.ch5_test);

        l1.setOnClickListener(v -> LearningModule.showDialog("  Level 1 - Signs for Workplaces", "Start +100 XP",25));
        l2.setOnClickListener(v -> LearningModule.showDialog("Level 2 - Signs to use in Schools", "Start +100 XP",26));
        l3.setOnClickListener(v -> LearningModule.showDialog("Level 3 - Signs for Finances", "Start +100 XP",27));
        l4.setOnClickListener(v -> LearningModule.showDialog("Level 4 - Medical Signs", "Start +100 XP",28));
        l5.setOnClickListener(v -> LearningModule.showDialog("Level 5 - Signs for Nature", "Start +100 XP",29));
        l6.setOnClickListener(v -> LearningModule.showDialog("Level 6 - Social Media Signs", "Start +100 XP", 30));
        ch5_test.setOnClickListener(v -> LearningModule.showDialog("Test 5 - Explore the Places", "Start +500 XP", 0));
        return view;
    }
}
