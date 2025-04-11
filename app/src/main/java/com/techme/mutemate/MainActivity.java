package com.techme.mutemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView Profile = findViewById(R.id.profile);
        Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "hello", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, Setting.class);
                startActivity(intent);
            }
        });
        // In your Activity or Fragment
        // RecyclerView recyclerView = findViewById(R.id.recyclerView); // Make sure to add ID to your RecyclerView
        //NotificationAdapter notificationAdapter = new NotificationAdapter(this);
        //
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setAdapter(notificationAdapter);
        //
        //// Register the receiver in onResume
        //@Override
        //protected void onResume() {
        //    super.onResume();
        //    notificationAdapter.registerReceiver();
        //}
        //
        //// Unregister in onPause
        //@Override
        //protected void onPause() {
        //    super.onPause();
        //    notificationAdapter.unregisterReceiver();
        //}
        //
        //// Method to send a notification
        //private void sendNotification(String title, String message) {
        //    Intent intent = new Intent(NotificationAdapter.ACTION_NEW_NOTIFICATION);
        //    intent.putExtra(NotificationAdapter.EXTRA_NOTIFICATION_TITLE, title);
        //    intent.putExtra(NotificationAdapter.EXTRA_NOTIFICATION_MESSAGE, message);
        //    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        //}
    }
}