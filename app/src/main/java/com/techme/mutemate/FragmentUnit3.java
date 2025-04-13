package com.techme.mutemate;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class FragmentUnit3 extends Fragment {

    ImageView l1, l2, l3, l4, l5, l6, ch3_test;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_unit3, container, false);
        l1 = view.findViewById(R.id.ch3_level1);
        l2 = view.findViewById(R.id.ch3_level2);
        l3 = view.findViewById(R.id.ch3_level3);
        l4 = view.findViewById(R.id.ch3_level4);
        l5 = view.findViewById(R.id.ch3_level5);
        l6 = view.findViewById(R.id.ch3_level6);
        ch3_test = view.findViewById(R.id.ch3_test);

        l1.setOnClickListener(v -> LearningModule.showDialog("Level 1 - Daily Activities : 1", "Start +100 XP",13));
        l2.setOnClickListener(v -> LearningModule.showDialog("Level 2 - Daily Activities : 2", "Start +100 XP",14));
        l3.setOnClickListener(v -> LearningModule.showDialog("Level 3 - All about Sports", "Start +100 XP",15));
        l4.setOnClickListener(v -> LearningModule.showDialog(" Level 4 - Foods and Beverages ", "Start +100 XP",16));
        l5.setOnClickListener(v -> LearningModule.showDialog("  Level 5 - Giving Introduction : 1  ", "Start +100 XP",17));
        l6.setOnClickListener(v -> LearningModule.showDialog("  Level 6 - Giving Introduction : 2  ", "Start +100 XP",18));
        ch3_test.setOnClickListener(v -> LearningModule.showDialog("Test 3 - Day-to-Day Signs", "Start +500 XP", 0));
        return view;
    }
}
