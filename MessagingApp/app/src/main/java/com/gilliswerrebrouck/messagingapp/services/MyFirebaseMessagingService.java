package com.gilliswerrebrouck.messagingapp.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import com.gilliswerrebrouck.messagingapp.R;
import com.gilliswerrebrouck.messagingapp.view.MessageActivity;
import com.gilliswerrebrouck.messagingapp.view.MessagesActivity;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by gillis on 16/05/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String UID = "uid";
    private static final String SENDER = "sender";
    private static final String MESSAGE = "message";
    private static final String MESSAGEKEY = "messageKey";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //super.onMessageReceived(remoteMessage);

        if(remoteMessage.getData().size() > 0){
            Map<String, String> data = remoteMessage.getData();

            String uid = data.get(UID);
            String sender = data.get(SENDER);
            String message = data.get(MESSAGE);
            String messageKey = data.get(MESSAGEKEY);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setContentTitle(sender);
            mBuilder.setContentText(message);

            Intent messageIntent = new Intent(this, MessageActivity.class);
            messageIntent.putExtra("message_key", messageKey);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

            stackBuilder.addNextIntent(messageIntent);
            PendingIntent messagePendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(messagePendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(0, mBuilder.build());
        }
    }
}
