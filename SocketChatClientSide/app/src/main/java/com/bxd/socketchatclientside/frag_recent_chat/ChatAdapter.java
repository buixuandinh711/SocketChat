package com.bxd.socketchatclientside.frag_recent_chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bxd.socketchatclientside.ClientConst;
import com.bxd.socketchatclientside.R;
import com.bxd.socketchatclientside.data_manager.ClientMessage;
import com.bxd.socketchatclientside.message_activity.MessageActivity;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter {

    List<ClientMessage> listChats;
    Context context;

    public ChatAdapter(List<ClientMessage> listChats, Context context) {
        this.listChats = listChats;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.view_recent_chat, parent, false);

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ClientMessage message = listChats.get(position);
        ChatViewHolder chatViewHolder = (ChatViewHolder) holder;
        chatViewHolder.bind(message);
        chatViewHolder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra(ClientConst.CLIENT_USERID_INTENT_DATA, message.getClientID());
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return listChats.size();
    }

    public void update(List<ClientMessage> list) {

        listChats.clear();
        listChats.addAll(list);
        notifyDataSetChanged();

    }
}
