package com.example.messengerapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.messengerapp.R;
import com.example.messengerapp.databinding.ActivitySingInBinding;
import com.example.messengerapp.untilyties.Constanst;
import com.example.messengerapp.untilyties.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.K;

import java.lang.ref.PhantomReference;
import java.util.HashMap;

public class SingInActivity extends AppCompatActivity {
    private ActivitySingInBinding binding;
    private PreferenceManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySingInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        manager = new PreferenceManager(getApplicationContext());
        if (manager.getBoolean(Constanst.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        event();

    }

    private void event() {
        binding.tvCreateNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SingInActivity.this, SingUpActivity.class);
                startActivity(i);
            }
        });

        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidSingInDetails()){
                signIn();
            }
        });
    }

    /**
     * private void addDataFirestore(){
     * FirebaseFirestore database = FirebaseFirestore.getInstance();
     * HashMap<String, Object> data = new HashMap<>();
     * data.put("first_name", "Vu");
     * data.put("last_name","Duy");
     * database.collection("users")
     * .add(data)
     * .addOnSuccessListener(documentReference -> {
     * Toast.makeText(this, "Data Inert Success", Toast.LENGTH_SHORT).show();
     * })
     * .addOnFailureListener(exception-> {
     * Toast.makeText(getApplicationContext(),exception.getMessage(), Toast.LENGTH_SHORT).show();
     * });
     * }
     **/
    private void signIn() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constanst.KEY_COLLECTION_USERS)
                .whereEqualTo(Constanst.KEY_MAIL,binding.inputEmail.getText().toString())
                .whereEqualTo(Constanst.KEY_PASSWORD,binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocumentChanges().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        manager.putBoolean(Constanst.KEY_IS_SIGNED_IN, true);
                        manager.putString(Constanst.KEY_USER_ID,documentSnapshot.getId());
                        manager.putString(Constanst.KEY_NAME,documentSnapshot.getString(Constanst.KEY_NAME));
                        manager.putString(Constanst.KEY_IMAGE,documentSnapshot.getString(Constanst.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else {
                      loading(false);
                      showToat("Unable to sign in");
                    }
                });


    }
    private void loading(Boolean isloading){
        if (isloading){
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressbarSignIn.setVisibility(View.VISIBLE);
        }
        else {
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressbarSignIn.setVisibility(View.INVISIBLE);
        }

    }
    private void showToat(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSingInDetails() {
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToat("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToat("Enter value email !");
            return false;
        } else if (binding.inputPassword.getText().toString().isEmpty()) {
            showToat("Enter password");
            return false;
        } else {
            return true;
          }
        }

}