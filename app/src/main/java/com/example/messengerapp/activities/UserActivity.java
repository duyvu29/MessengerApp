package com.example.messengerapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.messengerapp.adapters.UserAdapter;
import com.example.messengerapp.databinding.ActivityUserBinding;
import com.example.messengerapp.listener.UserListener;
import com.example.messengerapp.models.Users;
import com.example.messengerapp.untilyties.Constanst;
import com.example.messengerapp.untilyties.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends BaseActivity implements UserListener {
    private ActivityUserBinding binding;
    private PreferenceManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        manager = new PreferenceManager(getApplicationContext());
        //get user
        getUsers();
        // event
        event();
    }

    private void event() {
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore databae = FirebaseFirestore.getInstance();
        databae.collection(Constanst.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currenUserId = manager.getString(Constanst.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Users> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currenUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            Users user = new Users();
                            user.name = queryDocumentSnapshot.getString(Constanst.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constanst.KEY_MAIL);
                            user.image = queryDocumentSnapshot.getString(Constanst.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constanst.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            UserAdapter userAdapter = new UserAdapter(users, this);
                            binding.userRcv.setAdapter(userAdapter);
                            binding.userRcv.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage() {
        binding.tverrorMessage.setText(String.format("%s", "No user available"));
        binding.tverrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBarUser.setVisibility(View.VISIBLE);

        } else {
            binding.progressBarUser.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUsrClicked(Users users) {
        Intent intent = new Intent(getApplicationContext(), chatActivity.class);
        intent.putExtra(Constanst.KEY_USER, users);
        startActivity(intent);
        finish();
    }
}