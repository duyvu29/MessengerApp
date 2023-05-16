package com.example.messengerapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.BoringLayout;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.messengerapp.R;
import com.example.messengerapp.databinding.ActivitySingUpBinding;
import com.example.messengerapp.untilyties.Constanst;
import com.example.messengerapp.untilyties.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SingUpActivity extends AppCompatActivity {
    private ActivitySingUpBinding binding;
    private String encodeImage;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySingUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //sự kiện
        event();
        //
        preferenceManager = new PreferenceManager(getApplicationContext());
    }
    private void event() {
        binding.tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SingUpActivity. this, SingInActivity.class);
                startActivity(i);
            }
        });
        binding.buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidSignUpDetail()){
                    signUp();
                }
            }
        });
        binding.layoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImage.launch(intent);
            }
        });
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

    }

    private void signUp(){
        loangding(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constanst.KEY_NAME,binding.inputName.getText().toString());
        user.put(Constanst.KEY_MAIL, binding.inputCreateMail.getText().toString());
        user.put(Constanst.KEY_PASSWORD,binding.inputCreatPassword.getText().toString());
        user.put(Constanst.KEY_IMAGE, encodeImage);
        database.collection(Constanst.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loangding(false);
                    preferenceManager.putBoolean(Constanst.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constanst.KEY_USER_ID,documentReference.getId());
                    preferenceManager.putString(Constanst.KEY_NAME,binding.inputName.getText().toString());
                    preferenceManager.putString(Constanst.KEY_IMAGE, encodeImage);
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception ->{
                    loangding(false);
                    showToast(exception.getMessage());

                });

    }
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHegt = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHegt,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG , 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getData() != null){
                  Uri imageUri = result.getData().getData();
                  try {
                      InputStream inputStream = getContentResolver().openInputStream(imageUri);
                      Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                      binding.imageProfile.setImageBitmap(bitmap);
                      binding.tvAddImage.setVisibility(View.GONE);
                      encodeImage = encodeImage(bitmap);

                  } catch (FileNotFoundException e) {
                      e.printStackTrace();
                  }
              }
            });
    private Boolean isValidSignUpDetail(){
        if (encodeImage == null){
            showToast("select profile image");
            return false;
        }
        else if (binding.inputName.getText().toString().trim().isEmpty()){
            showToast("Enter Name");
            return false;
         }else if (binding.inputCreateMail.getText().toString().trim().isEmpty()){
            showToast("Enter Mail");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputCreateMail.getText().toString()).matches()){
            showToast("Enter valid image");
            return false;
        }else if (binding.inputCreatPassword.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()){
            showToast("Comfirm password");
            return false;
        }else if (!binding.inputCreatPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())){
            showToast("Password is confirm password must be same");
            return false;
        }
        else {
            return true;
        }
    }
    private void loangding(Boolean isLoading){
        if (isLoading){
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

}
