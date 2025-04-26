package com.techme.mutemate;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class FragmentUnit4 extends Fragment {

    com.google.android.material.card.MaterialCardView l1, l2, l3, l4, l5, l6, ch4_test;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_unit4, container, false);
        l1 = view.findViewById(R.id.level1);
        l2 = view.findViewById(R.id.level2);
        l3 = view.findViewById(R.id.level3);
        l4 = view.findViewById(R.id.level4);
        l5 = view.findViewById(R.id.level5);
        l6 = view.findViewById(R.id.level6);
        ch4_test = view.findViewById(R.id.test);

        l1.setOnClickListener(v -> LearningModule.showDialog("Level 1 - Speak about You !", "Start +100 XP",19));
        l2.setOnClickListener(v -> LearningModule.showDialog("Level 2 - Initiating the Conversation", "Start +100 XP",20));
        l3.setOnClickListener(v -> LearningModule.showDialog("  Level 3 - Hobbies & Interests : 1", "Start +100 XP",21));
        l4.setOnClickListener(v -> LearningModule.showDialog("  Level 4 - Hobbies & Interests : 2", "Start +100 XP",22));
        l5.setOnClickListener(v -> LearningModule.showDialog("    Level 5 - Building Confidence", "Start +100 XP",23));
        l6.setOnClickListener(v -> LearningModule.showDialog("Level 6 - Essential Signs", "Start +100 XP",24));
        ch4_test.setOnClickListener(v -> LearningModule.showDialog("Test 4 - You and the World", "Start +500 XP",0));
        return view;
    }
}
