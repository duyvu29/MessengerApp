package com.example.messengerapp.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.messengerapp.R;
import com.example.messengerapp.activities.chatActivity;
import com.example.messengerapp.models.Users;
import com.example.messengerapp.untilyties.Constanst;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;
import java.util.Random;

public class MessageService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM", "onNewToken: "+ token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d("FCM", "Messsa: "+ message.getNotification().getBody());
        Users users = new Users();
        users.id = message.getData().get(Constanst.KEY_USER_ID);
        users.name = message.getData().get(Constanst.KEY_NAME);
        users.token = message.getData().get(Constanst.KEY_FCM_TOKEN);

        int notification = new Random().nextInt();
        String channelId ="chat_message";

        Intent intent = new Intent(this, chatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
;
        intent.putExtra(Constanst.KEY_USER_ID, users);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(users.name);
        builder.setContentText(message.getData().get(Constanst.KEY_MESSAGE));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message.getData().get(Constanst.KEY_MESSAGE)));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            CharSequence charSequence ="Chat Message";
            String channelDescription = "This notification channel is used for chat message notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId,charSequence,importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notification,builder.build());
    }
}
