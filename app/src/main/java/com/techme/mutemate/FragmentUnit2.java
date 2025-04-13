package com.techme.mutemate;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class FragmentUnit2 extends Fragment {

    ImageView l1, l2, l3, l4, l5, l6, ch2_test;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_unit2, container, false);

        l1 = view.findViewById(R.id.ch2_level1);
        l2 = view.findViewById(R.id.ch2_level2);
        l3 = view.findViewById(R.id.ch2_level3);
        l4 = view.findViewById(R.id.ch2_level4);
        l5 = view.findViewById(R.id.ch2_level5);
        l6 = view.findViewById(R.id.ch2_level6);
        ch2_test = view.findViewById(R.id.ch2_test);

        l1.setOnClickListener(v -> LearningModule.showDialog("Level 1 - Family members in Sign Language", "Start +100 XP",7));
        l2.setOnClickListener(v -> LearningModule.showDialog("Level 2 - Basic Greetings : 1", "Start +100 XP",8));
        l3.setOnClickListener(v -> LearningModule.showDialog("Level 3 - Basic Greetings : 2", "Start +100 XP",9));
        l4.setOnClickListener(v -> LearningModule.showDialog("Level 4 - Pets in Sign Language", "Start +100 XP",10));
        l5.setOnClickListener(v -> LearningModule.showDialog("  Level 5 - Wild Animals in Sign Language", "Start +100 XP",11));
        l6.setOnClickListener(v -> LearningModule.showDialog("Level 6 - Days of the Week", "Start +100 XP",12));
        ch2_test.setOnClickListener(v -> LearningModule.showDialog("Test 2 - Know your Surrounding", "Start +500 XP", 0));

        return  view;
    }
}
