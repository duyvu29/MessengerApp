package com.example.messengerapp.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.messengerapp.R;
import com.example.messengerapp.adapters.ChatAdapter;
import com.example.messengerapp.databinding.ActivityChatBinding;
import com.example.messengerapp.models.ChatMessage;
import com.example.messengerapp.models.Users;
import com.example.messengerapp.network.ApiClient;
import com.example.messengerapp.network.ApiService;
import com.example.messengerapp.untilyties.Constanst;
import com.example.messengerapp.untilyties.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class chatActivity extends BaseActivity {
    private ActivityChatBinding binding;
    private Users receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager manager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private Boolean isReciverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //event
        setEnvent();
        //
        loadReceiverDetails();
        // ánh xạ
        init();
        //
        listenMessages();
    }
    private void  init(){
        manager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodeString(receiverUser.image),
                manager.getString(Constanst.KEY_USER_ID)

        );
        binding.chatRcv.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }
    private void sendMessage(){
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constanst.KEY_SENDER_ID,manager.getString(Constanst.KEY_USER_ID));
        message.put(Constanst.KEY_RECEIVE_ID,receiverUser.id);
        message.put(Constanst.KEY_MESSAGE,binding.inputMessageChat.getText().toString());
        message.put(Constanst.KEY_TIMESTAMP,new Date());
        database.collection(Constanst.KEY_COLLECTION_CHAT).add(message);
        if (conversionId != null){
            updatConversion(binding.inputMessageChat.getText().toString());
        }else {
            HashMap<String ,Object> conversion = new HashMap<>();
            conversion.put(Constanst.KEY_SENDER_ID,manager.getString(Constanst.KEY_USER_ID));
            conversion.put(Constanst.KEY_SENDER_NAME,manager.getString(Constanst.KEY_NAME));
            conversion.put(Constanst.KEY_SENDER_IMAGE,manager.getString(Constanst.KEY_IMAGE));
            conversion.put(Constanst.KEY_RECEIVE_ID,receiverUser.id);
            conversion.put(Constanst.KEY_RECEIVER_NAME,receiverUser.name);
            conversion.put(Constanst.KEY_RECEIVER_IMAGE,receiverUser.image);
            conversion.put(Constanst.KEY_LAST_MESSAGE,binding.inputMessageChat.getText().toString());
            conversion.put(Constanst.KEY_TIMESTAMP,new Date());
            addConversion(conversion);
        }
        if (!isReciverAvailable){
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(Constanst.KEY_USER_ID,manager.getString(Constanst.KEY_USER_ID));
                data.put(Constanst.KEY_NAME,manager.getString(Constanst.KEY_NAME));
                data.put(Constanst.KEY_FCM_TOKEN,manager.getString(Constanst.KEY_FCM_TOKEN));
                data.put(Constanst.KEY_MESSAGE,binding.inputMessageChat.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constanst.REMOTE_MSG_DATA,data);
                data.put(Constanst.REMOTE_MSG_REGISTRTION_IDS,tokens);

                sendNotification(body.toString());


            }catch (Exception exception){
                showToast(exception.getMessage());
            }
        }
        binding.inputMessageChat.setText(null);
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message , Toast.LENGTH_SHORT).show();
    }
    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constanst.getRemoteMsgHeader(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                 if (response.isSuccessful()){
                     Log.d(TAG, "onResponse: " + response);
                     try {
                         if (response.body() != null){
                             JSONObject reponse = new JSONObject(response.body());
                             JSONArray results = reponse.getJSONArray("results");
                             if (reponse.getInt("failure") == 1){
                                 JSONObject error = (JSONObject) results.get(0);
                                 showToast(error.getString("error"));
                                 return;
                             }
                         }

                     } catch (JSONException e ){
                         e.printStackTrace();
                     }
                     showToast("Notification sent successfully");
                 }else{

                 }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                Log.d("error", "onFailure: " + t.getMessage());
                 showToast(t.getMessage());
            }
        });
    }

    private void listenAvailablilityOfRecever(){
        database.collection(Constanst.KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(chatActivity.this,(value, error) -> {
            if (error != null){
                return;
            }
            if (value != null){
                if (value.getLong(Constanst.KEY_AVAILABILITY) != null){
                    int availability = Objects.requireNonNull(
                            value.getLong(Constanst.KEY_AVAILABILITY)
                    ).intValue();
                    isReciverAvailable = availability == 1;
                }
                receiverUser.token = value.getString(Constanst.KEY_FCM_TOKEN);
                if (receiverUser.image == null){
                    receiverUser.image =value.getString(Constanst.KEY_IMAGE);
                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodeString(receiverUser.image));
                    chatAdapter.notifyItemRangeChanged(0,chatMessages.size());
                }
            }
            if (isReciverAvailable){
                binding.tvAvailability.setVisibility(View.VISIBLE);
            }else {
                binding.tvAvailability.setVisibility(View.GONE);
            }

        });
    }
    private void listenMessages(){
        database.collection(Constanst.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constanst.KEY_SENDER_ID,manager.getString(Constanst.KEY_USER_ID))
                .whereEqualTo(Constanst.KEY_RECEIVE_ID,receiverUser.id)
                .addSnapshotListener(eventListener);

        database.collection(Constanst.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constanst.KEY_SENDER_ID,receiverUser.id)
                .whereEqualTo(Constanst.KEY_RECEIVE_ID,manager.getString(Constanst.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    private final EventListener<QuerySnapshot> eventListener =(value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constanst.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constanst.KEY_RECEIVE_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constanst.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constanst.KEY_TIMESTAMP));
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages,(obj1,obj2) -> obj1.dateTime.compareTo(obj2.dateTime));
                if (count == 0){
                    chatAdapter.notifyDataSetChanged();
                }else {
                    chatAdapter.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
                    binding.chatRcv.smoothScrollToPosition(chatMessages.size() -1);
                }
                binding.chatRcv.setVisibility(View.VISIBLE);
        }
        binding.progressBarChat.setVisibility(View.GONE);
        if (conversionId == null){
            checkForConversion();
        }
    };
    private Bitmap getBitmapFromEncodeString (String encodeImage){
        if (encodeImage != null){
            byte[] bytes= Base64.decode(encodeImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }
        else{
            return  null;
        }
    }
    private void loadReceiverDetails(){
        receiverUser = (Users) getIntent().getSerializableExtra(Constanst.KEY_USER);
        binding.tvNameChat.setText(receiverUser.name);
    }
    private void setEnvent(){
        binding.imageBackChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.layoutSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.inputMessageChat.getText().toString().isEmpty()){
                     showToast("Enter chat");
                } else {
                    sendMessage();
                }
                //sendMessage();
            }
        });
    }
    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd,yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
    private void addConversion(HashMap<String, Object> conversion){
        database.collection(Constanst.KEY_COLLECTION_CONVERSTATION)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }
    private void updatConversion(String mesage){
        DocumentReference documentReference = database.collection(Constanst.KEY_COLLECTION_CONVERSTATION).document(conversionId);
        documentReference.update(
                Constanst.KEY_LAST_MESSAGE,mesage,
                Constanst.KEY_TIMESTAMP,new Date()
        );
    }


    private void checkForConversion(){
        if (chatMessages.size() != 0){
            checkForConversionRemotely(
                    manager.getString(Constanst.KEY_USER_ID),receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id, manager.getString(Constanst.KEY_USER_ID)
            );
        }

    }

    private void checkForConversionRemotely(String senderId, String receivedId){
        database.collection(Constanst.KEY_COLLECTION_CONVERSTATION)
                .whereEqualTo(Constanst.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constanst.KEY_RECEIVE_ID,receivedId)
                .get()
                .addOnCompleteListener(converComplete);
    }

    private final OnCompleteListener<QuerySnapshot> converComplete = task -> {
        if  (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailablilityOfRecever();
    }
}