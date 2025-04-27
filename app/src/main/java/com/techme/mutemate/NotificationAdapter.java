package com.techme.mutemate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public static final String ACTION_NEW_NOTIFICATION = "com.techme.mutemate.NEW_NOTIFICATION";
    public static final String EXTRA_NOTIFICATION_TITLE = "notification_title";
    public static final String EXTRA_NOTIFICATION_MESSAGE = "notification_message";

    private final Context context;
    private final List<String> notifications = new ArrayList<>();
    private final BroadcastReceiver notificationReceiver;

    public NotificationAdapter(Context context) {
        this.context = context;

        // Initialize the BroadcastReceiver to listen for new notifications
        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_NEW_NOTIFICATION.equals(intent.getAction())) {
                    String title = intent.getStringExtra(EXTRA_NOTIFICATION_TITLE);
                    String message = intent.getStringExtra(EXTRA_NOTIFICATION_MESSAGE);
                    addNotification(title + ": " + message);
                }
            }
        };
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.textView.setText(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void addNotification(String notification) {
        notifications.add(0, notification); // Add to the top of the list
        notifyItemInserted(0);
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_NEW_NOTIFICATION);
        LocalBroadcastManager.getInstance(context).registerReceiver(notificationReceiver, filter);
    }

    public void unregisterReceiver() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(notificationReceiver);
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}