package com.bxd.socketchatclientside.frag_recent_chat;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bxd.socketchatclientside.ClientConst;
import com.bxd.socketchatclientside.R;
import com.bxd.socketchatclientside.data_manager.ClientMessage;
import com.bxd.socketchatclientside.message_activity.MessageActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatViewHolder extends RecyclerView.ViewHolder {

    TextView textViewChatClient;
    TextView textViewChatContent;
    TextView textViewChatDate;
    ConstraintLayout layoutRecentChat;

    public ChatViewHolder(@NonNull View itemView) {

        super(itemView);
        textViewChatClient = itemView.findViewById(R.id.textViewChatClient);
        textViewChatContent = itemView.findViewById(R.id.textViewChatContent);
        textViewChatDate = itemView.findViewById(R.id.textViewChatDate);
        layoutRecentChat = itemView.findViewById(R.id.layoutRecentChat);

    }

    public void bind(ClientMessage message) {

        String clientName = message.getClientID();

        String content = "";
        if (message.getType() == ClientConst.MESSAGE_TYPE_RECEIVE) {
            content = message.getClientID();
        } else {
            content = message.getHostID();
        }

        content += ": " + message.getContent();

        textViewChatClient.setText(clientName);
        textViewChatContent.setText(content);
        Date date = new Date(message.getSentTime());
        DateFormat dateFormat = new SimpleDateFormat("MMMM d");
        String stringDate = dateFormat.format(date);
        textViewChatDate.setText(stringDate);

    }

}
