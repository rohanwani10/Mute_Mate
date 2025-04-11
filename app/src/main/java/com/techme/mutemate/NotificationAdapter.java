package com.techme.mutemate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final List<NotificationItem> notifications;
    private final Context context;
    private final NotificationReceiver broadcastReceiver;

    // Action for the broadcast intent
    public static final String ACTION_NEW_NOTIFICATION = "com.yourapplication.ACTION_NEW_NOTIFICATION";
    public static final String EXTRA_NOTIFICATION_TITLE = "notification_title";
    public static final String EXTRA_NOTIFICATION_MESSAGE = "notification_message";

    public NotificationAdapter(Context context) {
        this.context = context;
        this.notifications = new ArrayList<>();
        this.broadcastReceiver = new NotificationReceiver();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem notification = notifications.get(position);
        holder.titleTextView.setText(notification.getTitle());
        holder.messageTextView.setText(notification.getMessage());
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    // Add a new notification and update the UI
    public void addNotification(NotificationItem notification) {
        notifications.add(0, notification); // Add at the top
        notifyItemInserted(0);
    }

    // Register the broadcast receiver
    public void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter(ACTION_NEW_NOTIFICATION);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter);
    }

    // Unregister the broadcast receiver
    public void unregisterReceiver() {
        try {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            // Handle case where receiver wasn't registered
        }
    }

    // ViewHolder class for notification items
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView messageTextView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.notification_title);
            messageTextView = itemView.findViewById(R.id.notification_message);
        }
    }

    // BroadcastReceiver to receive notifications
    private class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_NEW_NOTIFICATION.equals(intent.getAction())) {
                String title = intent.getStringExtra(EXTRA_NOTIFICATION_TITLE);
                String message = intent.getStringExtra(EXTRA_NOTIFICATION_MESSAGE);

                if (title == null) {
                    title = "Notification";
                }
                if (message == null) {
                    message = "";
                }

                NotificationItem notification = new NotificationItem(title, message);
                addNotification(notification);
            }
        }
    }
}