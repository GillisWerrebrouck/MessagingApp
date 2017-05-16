package com.gilliswerrebrouck.messagingapp.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by gillis on 16/05/2017.
 */

public class FirebaseUtils {
    public static DatabaseReference getRootRef(){
        return FirebaseDatabase.getInstance().getReference().getRoot();
    }

    public static DatabaseReference getNotificationRef(){
        return FirebaseDatabase.getInstance().getReference(Constants.NOTIFICATIONS_KEY);
    }

    public static DatabaseReference getChatsRef(){
        return FirebaseDatabase.getInstance().getReference(Constants.CHAT_KEY);
    }

    public static DatabaseReference getMembersRef(){
        return FirebaseDatabase.getInstance().getReference(Constants.MEMBERS_KEY);
    }

    public static DatabaseReference getMessagesRef(){
        return FirebaseDatabase.getInstance().getReference(Constants.MESSAGES_KEY);
    }

    public static DatabaseReference getUsersRef(){
        return FirebaseDatabase.getInstance().getReference(Constants.USERS_KEY);
    }
}
