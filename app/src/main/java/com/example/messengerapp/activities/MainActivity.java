package com.example.messengerapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.messengerapp.adapters.RecenConcrsationAdapter;
import com.example.messengerapp.databinding.ActivityMainBinding;
import com.example.messengerapp.listener.ConverListenner;
import com.example.messengerapp.models.ChatMessage;
import com.example.messengerapp.models.Users;
import com.example.messengerapp.untilyties.Constanst;
import com.example.messengerapp.untilyties.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConverListenner {
      private ActivityMainBinding binding;
      private PreferenceManager manager;
      private List<ChatMessage> converstation;
      private RecenConcrsationAdapter conversationAdapter;
      private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        manager  = new PreferenceManager(getApplicationContext());
        //
        init();
        //
        loaddUserDetails();
        getToken();
        // event
        event();
        //
        listenConversations();
    }
    private void init(){
        converstation = new ArrayList<>();
        conversationAdapter = new RecenConcrsationAdapter( converstation,this);
        binding.converRcvMain.setAdapter(conversationAdapter);
        database = FirebaseFirestore.getInstance();

    }

    private void event() {
        binding.imagePowerOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        binding.btnFloatAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, UserActivity.class);
                startActivity(i);
            }
        });
    }

    private void loaddUserDetails(){
        binding.tvNickName.setText(manager.getString(Constanst.KEY_NAME));
        byte[] bytes = Base64.decode(manager.getString(Constanst.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.imageProfileMain.setImageBitmap(bitmap);
    }

    private void  showToas(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

    }
    private void   listenConversations(){
        database.collection(Constanst.KEY_COLLECTION_CONVERSTATION)
                .whereEqualTo(Constanst.KEY_SENDER_ID,manager.getString(Constanst.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constanst.KEY_COLLECTION_CONVERSTATION)
                .whereEqualTo(Constanst.KEY_RECEIVE_ID,manager.getString(Constanst.KEY_USER_ID))
                .addSnapshotListener(eventListener);

    }
    private final EventListener<QuerySnapshot> eventListener =((value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constanst.KEY_SENDER_ID);
                    String receivedId = documentChange.getDocument().getString(Constanst.KEY_RECEIVE_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receivedId;
                    if (manager.getString(Constanst.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constanst.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constanst.KEY_RECEIVE_ID);
                        chatMessage.conversionName  = documentChange.getDocument().getString(Constanst.KEY_RECEIVER_NAME);

                    }
                    else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constanst.KEY_SENDER_IMAGE);
                        chatMessage.conversionId    = documentChange.getDocument().getString(Constanst.KEY_SENDER_ID);
                        chatMessage.conversionName  = documentChange.getDocument().getString(Constanst.KEY_SENDER_NAME);

                    }
                    chatMessage.message = documentChange.getDocument().getString(Constanst.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constanst.KEY_TIMESTAMP);
                    converstation.add(chatMessage);
                }
                else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0; i < converstation.size(); i ++){
                        String senderId = documentChange.getDocument().getString(Constanst.KEY_SENDER_ID);
                        String receivedId = documentChange.getDocument().getString(Constanst.KEY_RECEIVE_ID);
                        if (converstation.get(i).senderId.equals(senderId) && converstation.get(i).receiverId.equals(receivedId)){
                            converstation.get(i).message = documentChange.getDocument().getString(Constanst.KEY_LAST_MESSAGE);
                            converstation.get(i).dateObject = documentChange.getDocument().getDate(Constanst.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(converstation,(obj1,obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationAdapter.notifyDataSetChanged();
            binding.converRcvMain.smoothScrollToPosition(0);
            binding.converRcvMain.setVisibility(View.VISIBLE);
            binding.progressBarMain.setVisibility(View.GONE);
        }
    });

    private void getToken()
    {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }
    private void updateToken(String token){
        manager.putString(Constanst.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constanst.KEY_COLLECTION_USERS).document(manager.getString(Constanst.KEY_USER_ID));
        documentReference.update(Constanst.KEY_FCM_TOKEN,token)
                .addOnSuccessListener(unused -> showToas("Token updtae succecssfully"))
                .addOnFailureListener(e -> showToas("Unable to update token"));
    }
    private void signOut(){
        showToas("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constanst.KEY_COLLECTION_USERS).document(manager.getString(Constanst.KEY_USER_ID));
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constanst.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    manager.clear();
                    startActivity(new Intent(getApplicationContext(),SingInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToas("Unable to sign out"));

    }

    @Override
    public void onConversionClieck(Users users) {
        Intent intent = new Intent(getApplicationContext(),chatActivity.class);
        intent.putExtra(Constanst.KEY_USER, users);
        startActivity(intent);
    }
}