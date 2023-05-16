package com.example.messengerapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messengerapp.databinding.ItemContainerResentConversionBinding;
import com.example.messengerapp.listener.ConverListenner;
import com.example.messengerapp.models.ChatMessage;
import com.example.messengerapp.models.Users;

import java.util.List;

public class RecenConcrsationAdapter extends RecyclerView.Adapter<RecenConcrsationAdapter.ConversionViewHolder> {
    private final List <ChatMessage> chatMessages;
    private final ConverListenner converListenner;



    public RecenConcrsationAdapter(List<ChatMessage> chatMessages, ConverListenner converListenner) {
        this.chatMessages = chatMessages;
        this.converListenner = converListenner;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerResentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
             holder.setData(chatMessages.get(position));

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{

        ItemContainerResentConversionBinding binding;

        ConversionViewHolder (ItemContainerResentConversionBinding itemContainerResentConversionBinding){
            super(itemContainerResentConversionBinding.getRoot());
            binding =itemContainerResentConversionBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.imageProfile.setImageBitmap(getConversionIamge(chatMessage.conversionImage));
            binding.tvName.setText(chatMessage.conversionName);
            binding.tvRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                Users users = new Users();
                users.id    = chatMessage.conversionId;
                users.name  =chatMessage.conversionName;
                users.image =chatMessage.conversionImage;
                converListenner.onConversionClieck(users);

            });
        }

    }
    private Bitmap getConversionIamge(String encodeImge){
        byte[] bytes =Base64.decode(encodeImge,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

    }
}
