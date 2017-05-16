package com.gilliswerrebrouck.messagingapp.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
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
import com.gilliswerrebrouck.messagingapp.model.User;
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
    @BindView(R.id.newMessage)
    EditText newMessage;

    // get the root in the firebase db
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot();
    // get the users as root in the firebase db
    private DatabaseReference users = root.child("users");
    // get the messages as root in the firebase db
    private DatabaseReference messages = root.child("messages");
    // get the chats as root in the firebase db
    private DatabaseReference chats = root.child("chats");
    // get the members as root in the firebase db
    private DatabaseReference members = root.child("members");

    private MessageRecycleViewAdapter adapter;

    private String messageKey;
    private List<String> userArr = new ArrayList<String>();
    private List<String> timeArr = new ArrayList<String>();
    private List<String> messageArr = new ArrayList<String>();

    private FirebaseAuth firebaseAuth;
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

        if (firebaseAuth.getCurrentUser() == null) {
            Intent login = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(login);
            finish();
        }

        user = new User(firebaseAuth.getCurrentUser());
        final User finalUser = user;

        messageKey = getIntent().getExtras().get("message_key").toString();

        messageListeners();

        messagesRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                messagesRecyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        });

        newMessage.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String uid = user.getUid();
                    String msg = newMessage.getText().toString();
                    Long tsLong = System.currentTimeMillis();
                    String ts = tsLong.toString();
                    Message message = new Message(uid, msg, ts);
                    sendMessage(message);
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
        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.child(user.getUid()).child("messages").child(messageKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot == null || dataSnapshot.getValue() == null) return;
                        if (dataSnapshot.getValue().toString().equals("true")) {
                            messages.child(messageKey).addValueEventListener(new ValueEventListener() {
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
        if(messageArr.isEmpty()){
            members.child(messageKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterator membersIterator = dataSnapshot.getChildren().iterator();

                    while (membersIterator.hasNext()) {
                        DataSnapshot dsUser = (DataSnapshot) membersIterator.next();
                        String uid = dsUser.getKey();

                        users.child(uid).child("messages").child(messageKey).setValue("true");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        Map<String, Object> mapMessageKey = new HashMap<String, Object>();
        // a random key for each message
        String temp_key = messages.child(messageKey).push().getKey();
        messages.child(messageKey).updateChildren(mapMessageKey);

        Map<String, Object> mapMessage = new HashMap<String, Object>();
        mapMessage.put("message", message.getMessage());
        mapMessage.put("timestamp", message.getTime());
        mapMessage.put("uid", message.getUid());
        // save message
        messages.child(messageKey).child(temp_key).updateChildren(mapMessage);
        chats.child(messageKey).child("last_message").updateChildren(mapMessage);
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

            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    RecyclerView.LayoutParams.WRAP_CONTENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT,
                    0.3f
            );

            if (!userArr.isEmpty() && userArr.size() > position
                    && !timeArr.isEmpty() && timeArr.size() > position
                    && !msgArr.isEmpty() && msgArr.size() > position) {
                String time = DateUtils.formatDateTime(getApplicationContext(), Long.parseLong(timeArr.get(position)), DateUtils.FORMAT_SHOW_TIME);
                String date = DateUtils.formatDateTime(getApplicationContext(), Long.parseLong(timeArr.get(position)), DateUtils.FORMAT_SHOW_DATE);

                if (firebaseAuth.getCurrentUser().getUid().equals(userArr.get(position))) {
                    holder.myMessage.setText(msgArr.get(position));
                    holder.otherMessage.setVisibility(View.INVISIBLE);
                    holder.otherMessage.setLayoutParams(param);
                    holder.myMessageTime.setText(date + " " + time);
                } else {
                    holder.otherMessage.setText(msgArr.get(position));
                    holder.myMessage.setVisibility(View.INVISIBLE);
                    holder.myMessage.setLayoutParams(param);
                    holder.otherMessageTime.setText(date + " " + time);
                }
            }
        }

        @Override
        public int getItemCount() {
            return msgArr.size();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(messageArr.isEmpty()){
            members.child(messageKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterator membersIterator = dataSnapshot.getChildren().iterator();

                    while (membersIterator.hasNext()) {
                        DataSnapshot dsUser = (DataSnapshot) membersIterator.next();
                        String uid = dsUser.getKey();

                        users.child(uid).child("messages").child(messageKey).removeValue();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            messages.child(messageKey).removeValue();
            chats.child(messageKey).removeValue();
            members.child(messageKey).removeValue();

            finish();
        }
    }
}