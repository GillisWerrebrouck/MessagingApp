package com.gilliswerrebrouck.messagingapp.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gilliswerrebrouck.messagingapp.R;
import com.gilliswerrebrouck.messagingapp.model.User;
import com.gilliswerrebrouck.messagingapp.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessagesActivity extends AppCompatActivity {

    @BindView(R.id.messages)
    RecyclerView messagesRecyclerView;
    @BindView(R.id.no_messages)
    TextView noMessages;

    private MessagesRecycleViewAdapter adapter;

    private List<String> messageKeyArr = new ArrayList<>();
    private List<String> userStatusArr = new ArrayList<>();
    private List<String> lastMessageUserArr = new ArrayList<>();
    private List<String> userArr = new ArrayList<>();
    private List<String> timeArr = new ArrayList<>();
    private List<String> messageArr = new ArrayList<>();

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private User user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        setTitle("Messages");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent users = new Intent(getApplicationContext(), NewMessageActivity.class);
                startActivity(users);
            }
        });

        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        adapter = new MessagesRecycleViewAdapter(messageKeyArr, userStatusArr, lastMessageUserArr, userArr, timeArr, messageArr);
        messagesRecyclerView.setAdapter(adapter);

        DividerItemDecoration decoration = new DividerItemDecoration(messagesRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        messagesRecyclerView.addItemDecoration(decoration);

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

        messagesListeners();
    }

    private void messagesListeners() {
        final User finalUser = user;

        FirebaseUtils.getRootRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUtils.getUsersRef().child(finalUser.getUid()).child("messages").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        messageKeyArr.clear();
                        userStatusArr.clear();
                        lastMessageUserArr.clear();
                        userArr.clear();
                        timeArr.clear();
                        messageArr.clear();

                        Iterator messagesIterator = dataSnapshot.getChildren().iterator();

                        while (messagesIterator.hasNext()) {
                            DataSnapshot dsMessage = (DataSnapshot) messagesIterator.next();
                            final String messageKey = dsMessage.getKey();
                            if (dsMessage.getValue().toString().equals("false")) {
                                continue;
                            }
                            messageKeyArr.add(messageKeyArr.size(), messageKey);

                            FirebaseUtils.getMembersRef().child(messageKey).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterator membersIterator = dataSnapshot.getChildren().iterator();

                                    while (membersIterator.hasNext()) {
                                        DataSnapshot member = (DataSnapshot) membersIterator.next();
                                        String memberKey = member.getKey().toString();
                                        String memberValue = member.getValue().toString();

                                        if (!memberKey.equals(user.getUid()) && memberValue.equals("true")) {
                                            FirebaseUtils.getUsersRef().child(memberKey).child("username").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot == null || dataSnapshot.getValue() == null)
                                                        return;
                                                    userArr.add(userArr.size(), dataSnapshot.getValue().toString());

                                                    adapter.notifyDataSetChanged();
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            FirebaseUtils.getUsersRef().child(memberKey).child("status").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot == null || dataSnapshot.getValue() == null)
                                                        return;
                                                    userStatusArr.add(userStatusArr.size(), dataSnapshot.getValue().toString());

                                                    adapter.notifyDataSetChanged();
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            FirebaseUtils.getChatsRef().child(messageKey).child("last_message").child("message").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot == null || dataSnapshot.getValue() == null)
                                        return;
                                    messageArr.add(messageArr.size(), dataSnapshot.getValue().toString());
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            FirebaseUtils.getChatsRef().child(messageKey).child("last_message").child("uid").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot == null || dataSnapshot.getValue() == null)
                                        return;
                                    String uid = dataSnapshot.getValue().toString();

                                    FirebaseUtils.getUsersRef().child(uid).child("username").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            lastMessageUserArr.add(lastMessageUserArr.size(), dataSnapshot.getValue().toString());

                                            adapter.notifyDataSetChanged();
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

                            FirebaseUtils.getChatsRef().child(messageKey).child("last_message").child("timestamp").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot == null || dataSnapshot.getValue() == null)
                                        return;
                                    timeArr.add(timeArr.size(), dataSnapshot.getValue().toString());

                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        adapter.notifyDataSetChanged();
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
        ImageView userImage;
        TextView username;
        TextView lastMessageTime;
        TextView lastMessageUser;
        TextView lastMessage;
        TextView userStatus;
        TextView userStatusBack;

        public MessageViewHolder(View row) {
            super(row);

            userImage = (ImageView) row.findViewById(R.id.userImage);
            username = (TextView) row.findViewById(R.id.username);
            lastMessageTime = (TextView) row.findViewById(R.id.lastMessageTime);
            lastMessageUser = (TextView) row.findViewById(R.id.lastMessageUser);
            lastMessage = (TextView) row.findViewById(R.id.lastMessage);
            userStatus = (TextView) row.findViewById(R.id.userStatus);
            userStatusBack = (TextView) row.findViewById(R.id.userStatusBack);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int positie = getAdapterPosition();

                    Intent message = new Intent(getApplicationContext(), MessageActivity.class);
                    message.putExtra("message_key", messageKeyArr.get(positie));
                    startActivity(message);
                }
            });
        }
    }

    // implement MessageViewHolder
    class MessagesRecycleViewAdapter extends RecyclerView.Adapter<MessageViewHolder> {
        private List<String> messageKeyArr = new ArrayList<String>();
        private List<String> userStatusArr = new ArrayList<String>();
        private List<String> userArr = new ArrayList<String>();
        private List<String> timeArr = new ArrayList<String>();
        private List<String> messagegArr = new ArrayList<String>();
        private List<String> lastMessageUserArr = new ArrayList<String>();

        public MessagesRecycleViewAdapter(List<String> messageKeyArr, List<String> userStatusArr, List<String> lastMessageUserArr, List<String> userArr, List<String> timeArr, List<String> messagegArr) {
            this.messageKeyArr = messageKeyArr;
            this.userStatusArr = userStatusArr;
            this.userArr = userArr;
            this.timeArr = timeArr;
            this.messagegArr = messagegArr;
            this.lastMessageUserArr = lastMessageUserArr;
        }

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View viewRow = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_messages, parent, false);
            MessageViewHolder messageViewHolder = new MessageViewHolder(viewRow);

            return messageViewHolder;
        }

        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {
            if (!messageKeyArr.isEmpty() && messageKeyArr.size() > position
                    && !userStatusArr.isEmpty() && userStatusArr.size() > position
                    && !userArr.isEmpty() && userArr.size() > position
                    && !timeArr.isEmpty() && timeArr.size() > position
                    && !messagegArr.isEmpty() && messagegArr.size() > position
                    && !lastMessageUserArr.isEmpty() && lastMessageUserArr.size() > position) {
                if (userStatusArr.get(position).equals("online"))
                    holder.userStatus.getBackground().setTint(getColor(R.color.userStatusOnline));
                else if (userStatusArr.get(position).equals("offline"))
                    holder.userStatus.getBackground().setTint(getColor(R.color.userStatusOffline));
                else
                    holder.userStatus.getBackground().setTint(getColor(R.color.userStatusUnknown));

                holder.userStatusBack.getBackground().setTint(getColor(R.color.background));

                holder.username.setText(userArr.get(position));

                String time = DateUtils.formatDateTime(getApplicationContext(), Long.parseLong(timeArr.get(position)), DateUtils.FORMAT_SHOW_TIME);
                String date = DateUtils.formatDateTime(getApplicationContext(), Long.parseLong(timeArr.get(position)), DateUtils.FORMAT_SHOW_DATE);

                holder.lastMessageTime.setText(date + " " + time);

                holder.lastMessageUser.setText(String.format("%s: ", lastMessageUserArr.get(position)));

                holder.lastMessage.setText(messagegArr.get(position));
            }

            if(messageKeyArr.isEmpty()){
                noMessages.setVisibility(View.VISIBLE);
            } else {
                noMessages.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return messageKeyArr.size();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        user.signOff();
    }
}