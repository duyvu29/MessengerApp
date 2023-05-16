package com.example.messengerapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messengerapp.databinding.ItemContainerLayoutBinding;
import com.example.messengerapp.listener.UserListener;
import com.example.messengerapp.models.Users;

import java.util.List;

public class UserAdapter extends  RecyclerView.Adapter<UserAdapter.UsersViewholder>{
    private final List<Users> users;
    private final UserListener userListener;

    public UserAdapter(List<Users> users,UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UsersViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerLayoutBinding itemContainerLayoutBinding = ItemContainerLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UsersViewholder(itemContainerLayoutBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewholder holder, int position) {
       holder.setUsersData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UsersViewholder extends RecyclerView.ViewHolder{

        ItemContainerLayoutBinding binding;

        UsersViewholder(ItemContainerLayoutBinding itemContainerLayoutBinding){
            super(itemContainerLayoutBinding.getRoot());
            binding = itemContainerLayoutBinding;

        }
        void setUsersData(Users user){
            binding.tvName.setText(user.name);
            binding.tvEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            // binding.imageProfile.setImageResource();
            binding.getRoot().setOnClickListener(v -> userListener.onUsrClicked(user));
        }
    }
    private Bitmap getUserImage(String encodeImage){
        byte[] bytes = Base64.decode(encodeImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
