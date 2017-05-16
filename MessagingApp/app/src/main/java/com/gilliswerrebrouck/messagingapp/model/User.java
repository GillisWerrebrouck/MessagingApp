package com.gilliswerrebrouck.messagingapp.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gillis on 3/05/2017.
 */

public class User {
    //region firebase properties
    private DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("users");
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser fbUser;
    //endregion

    //region properties
    private String username;
    private String email;
    private String password;
    private String uid;
    //endregion

    //region getter(s)
    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public String getUid() {
        return this.uid;
    }

    public FirebaseUser getFbUser() {
        return this.fbUser;
    }
    //endregion

    //region setter(s)
    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    //endregion setters

    //region constructor(s)
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
    public User(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public User(FirebaseUser fbuser) {
        this.username = fbuser.getDisplayName();
        this.email = fbuser.getEmail();
        this.uid = fbuser.getUid();
        this.fbUser = fbuser;
    }
    //endregion

    //region method(s)
    public void register(){
        // key-value-pair for the user info
        Map<String, Object> mapUser = new HashMap<String, Object>();
        mapUser.put("username", this.username);
        // set uid
        this.uid = fbUser.getUid();
        // save user
        users.child(uid).updateChildren(mapUser);
    }

    public void signIn() {
        // key-value-pair for the user info
        Map<String, Object> mapUser = new HashMap<String, Object>();
        mapUser.put("status", "online");
        // set uid
        this.uid = fbUser.getUid();
        // save user
        users.child(uid).updateChildren(mapUser);
    }

    public void signOff() {
        // key-value-pair for the user info
        Map<String, Object> mapUser = new HashMap<String, Object>();
        mapUser.put("status", "offline");
        // set uid
        this.uid = fbUser.getUid();
        // save user
        users.child(uid).updateChildren(mapUser);

        firebaseAuth.signOut();
    }
    //endregion
}
