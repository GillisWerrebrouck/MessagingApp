package com.gilliswerrebrouck.messagingapp.view;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.gilliswerrebrouck.messagingapp.R;
import com.gilliswerrebrouck.messagingapp.model.Message;
import com.gilliswerrebrouck.messagingapp.model.Notification;
import com.gilliswerrebrouck.messagingapp.model.User;
import com.gilliswerrebrouck.messagingapp.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageActivity extends AppCompatActivity {

    @BindView(R.id.messages)
    RecyclerView messagesRecyclerView;
    @BindView(R.id.no_messages)
    TextView noMessages;
    @BindView(R.id.newMessage)
    EditText newMessage;

    private MessageRecycleViewAdapter adapter;

    private String messageKey, uid;
    private List<String> userArr = new ArrayList<String>();
    private List<String> timeArr = new ArrayList<String>();
    private List<String> messageArr = new ArrayList<String>();

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private User user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        adapter = new MessageRecycleViewAdapter(userArr, timeArr, messageArr);
        messagesRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(login);
                    finish();
                }
            }
        };

        user = new User(firebaseAuth.getCurrentUser());
        final User finalUser = user;

        if (getIntent().getExtras().get("message_key") != null) {
            messageKey = getIntent().getExtras().get("message_key").toString();
            messageListeners();
        } else if (getIntent().getExtras().get("uid") != null) {
            uid = getIntent().getExtras().get("uid").toString();
        }

        messagesRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                messagesRecyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        });

        newMessage.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String msg = newMessage.getText().toString();
                if (!msg.isEmpty() && actionId == EditorInfo.IME_ACTION_SEND) {
                    String uid = user.getUid();
                    Long tsLong = System.currentTimeMillis();
                    String ts = tsLong.toString();
                    Message message = new Message(uid, msg, ts);
                    sendMessage(message);
                    sendNotification(message);
                    newMessage.setText("");
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void messageListeners() {
        FirebaseUtils.getRootRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUtils.getUsersRef().child(user.getUid()).child("messages").child(messageKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot == null || dataSnapshot.getValue() == null) return;
                        if (dataSnapshot.getValue().toString().equals("true")) {
                            FirebaseUtils.getMessagesRef().child(messageKey).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    userArr.clear();
                                    timeArr.clear();
                                    messageArr.clear();

                                    Iterator messagesIterator = dataSnapshot.getChildren().iterator();

                                    while (messagesIterator.hasNext()) {
                                        DataSnapshot messageInfo = (DataSnapshot) messagesIterator.next();
                                        Iterator messageIterator = messageInfo.getChildren().iterator();

                                        while (messageIterator.hasNext()) {
                                            DataSnapshot currentMessage = (DataSnapshot) messageIterator.next();
                                            if (currentMessage.getKey().equals("uid"))
                                                userArr.add(userArr.size(), currentMessage.getValue().toString());
                                            if (currentMessage.getKey().equals("timestamp"))
                                                timeArr.add(timeArr.size(), currentMessage.getValue().toString());
                                            if (currentMessage.getKey().equals("message"))
                                                messageArr.add(messageArr.size(), currentMessage.getValue().toString());
                                        }
                                    }

                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            Intent messages = new Intent(getApplicationContext(), MessagesActivity.class);
                            startActivity(messages);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(Message message) {
        if (messageArr.isEmpty()) {
            Map<String, Object> mapMessageKey = new HashMap<String, Object>();
            // a random key for the messages
            messageKey = FirebaseUtils.getUsersRef().child(user.getUid()).child("messages").push().getKey();

            FirebaseUtils.getUsersRef().child(user.getUid()).child("messages").updateChildren(mapMessageKey);
            FirebaseUtils.getUsersRef().child(user.getUid()).child("messages").child(messageKey).setValue("true");

            FirebaseUtils.getUsersRef().child(uid).child("messages").updateChildren(mapMessageKey);
            FirebaseUtils.getUsersRef().child(uid).child("messages").child(messageKey).setValue("false");

            FirebaseUtils.getMembersRef().updateChildren(mapMessageKey);
            Map<String, Object> mapMyMessage = new HashMap<String, Object>();
            mapMyMessage.put(user.getUid(), "true");
            FirebaseUtils.getMembersRef().child(messageKey).updateChildren(mapMyMessage);
            Map<String, Object> mapOtherMessage = new HashMap<String, Object>();
            mapOtherMessage.put(uid, "true");
            FirebaseUtils.getMembersRef().child(messageKey).updateChildren(mapOtherMessage);

            FirebaseUtils.getChatsRef().updateChildren(mapMessageKey);
            Map<String, Object> mapChats = new HashMap<String, Object>();
            Map<String, Object> mapLastMsg = new HashMap<String, Object>();
            mapLastMsg.put("message", "");
            mapLastMsg.put("timestamp", "0");
            mapLastMsg.put("uid", user.getUid());
            mapChats.put("last_message", mapLastMsg);
            FirebaseUtils.getChatsRef().child(messageKey).updateChildren(mapChats);

            messageListeners();
        }

        Map<String, Object> mapMessageKey = new HashMap<String, Object>();
        // a random key for each message
        String temp_key = FirebaseUtils.getMessagesRef().child(messageKey).push().getKey();
        FirebaseUtils.getMessagesRef().child(messageKey).updateChildren(mapMessageKey);

        Map<String, Object> mapMessage = new HashMap<String, Object>();
        mapMessage.put("message", message.getMessage());
        mapMessage.put("timestamp", message.getTime());
        mapMessage.put("uid", message.getUid());
        // save message
        FirebaseUtils.getMessagesRef().child(messageKey).child(temp_key).updateChildren(mapMessage);
        FirebaseUtils.getChatsRef().child(messageKey).child("last_message").updateChildren(mapMessage);
    }

    private void sendNotification(final Message message) {
        FirebaseUtils.getMembersRef().child(messageKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dsMembers) {
                FirebaseUtils.getUsersRef().child(message.getUid()).child("username").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Notification notification = new Notification();
                        notification.setSender(dataSnapshot.getValue().toString());
                        notification.setMessage(message.getMessage());
                        notification.setMessageKey(messageKey);

                        Iterator membersIterator = dsMembers.getChildren().iterator();

                        while (membersIterator.hasNext()) {
                            DataSnapshot dsMember = (DataSnapshot) membersIterator.next();
                            String uid = dsMember.getKey();

                            if (!uid.equals(user.getUid())) {
                                notification.setUid(uid);
                                notification.setTopic(uid);
                            }
                        }

                        FirebaseUtils.getNotificationRef().push().setValue(notification);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // make a row for a message
    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView myMessage;
        TextView otherMessage;
        RelativeLayout messageTimeLayout;
        TextView myMessageTime;
        TextView otherMessageTime;

        public MessageViewHolder(final View row) {
            super(row);

            myMessage = (TextView) row.findViewById(R.id.myMessage);
            otherMessage = (TextView) row.findViewById(R.id.otherMessage);
            messageTimeLayout = (RelativeLayout) row.findViewById(R.id.messageTimeLayout);
            myMessageTime = (TextView) row.findViewById(R.id.myMessageTime);
            otherMessageTime = (TextView) row.findViewById(R.id.otherMessageTime);

            myMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int positie = getAdapterPosition();
                    animateMessageTime(messageTimeLayout);
                }
            });

            otherMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int positie = getAdapterPosition();
                    animateMessageTime(messageTimeLayout);
                }
            });
        }
    }

    private void animateMessageTime(View messageTimeLayout) {
        if (messageTimeLayout.getVisibility() == View.VISIBLE) {
            ScaleAnimation anim = new ScaleAnimation(1, 1, 1, 0);
            anim.setFillAfter(true);
            anim.setDuration(350);
            messageTimeLayout.setAnimation(anim);
            messageTimeLayout.setVisibility(View.GONE);
        } else {
            messageTimeLayout.setVisibility(View.VISIBLE);
            ScaleAnimation anim = new ScaleAnimation(1, 1, 0, 1);
            anim.setDuration(500);
            messageTimeLayout.setAnimation(anim);
        }
    }

    // implement MessageViewHolder
    class MessageRecycleViewAdapter extends RecyclerView.Adapter<MessageViewHolder> {
        private List<String> userArr = new ArrayList<String>();
        private List<String> timeArr = new ArrayList<String>();
        private List<String> msgArr = new ArrayList<String>();

        public MessageRecycleViewAdapter(List<String> userArr, List<String> timeArr, List<String> msgArr) {
            this.userArr = userArr;
            this.timeArr = timeArr;
            this.msgArr = msgArr;
        }

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View viewRow = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_message, parent, false);
            MessageViewHolder messageViewHolder = new MessageViewHolder(viewRow);

            return messageViewHolder;
        }

        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {
            holder.setIsRecyclable(false);

            if (!userArr.isEmpty() && userArr.size() > position
                    && !timeArr.isEmpty() && timeArr.size() > position
                    && !msgArr.isEmpty() && msgArr.size() > position) {
                String time = DateUtils.formatDateTime(getApplicationContext(), Long.parseLong(timeArr.get(position)), DateUtils.FORMAT_SHOW_TIME);
                String date = DateUtils.formatDateTime(getApplicationContext(), Long.parseLong(timeArr.get(position)), DateUtils.FORMAT_SHOW_DATE);

                if (firebaseAuth.getCurrentUser().getUid().equals(userArr.get(position))) {
                    holder.myMessage.setText(msgArr.get(position));
                    holder.otherMessage.setVisibility(View.INVISIBLE);
                    holder.otherMessage.setWidth(0);
                    holder.myMessageTime.setText(date + " " + time);
                } else {
                    holder.otherMessage.setText(msgArr.get(position));
                    holder.myMessage.setVisibility(View.INVISIBLE);
                    holder.myMessage.setWidth(0);
                    holder.otherMessageTime.setText(date + " " + time);
                }
            }

            if (userArr.isEmpty()) {
                noMessages.setVisibility(View.VISIBLE);
            } else {
                noMessages.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return msgArr.size();
        }
    }
}