package com.bxd.socketchatclientside.message_activity;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bxd.socketchatclientside.R;
import com.bxd.socketchatclientside.data_manager.ClientMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class ReceiveMessageViewHolder extends RecyclerView.ViewHolder {

    TextView textViewTime;
    TextView textViewMessage;

    public ReceiveMessageViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewTime = itemView.findViewById(R.id.textViewReceiveTime);
        textViewMessage = itemView.findViewById(R.id.textViewReceiveMessage);
    }

    public void bind(ClientMessage message) {

        textViewMessage.setText(message.getContent());
        Date date = new Date(message.getSentTime());
        DateFormat dateFormat = new SimpleDateFormat("MMMM d");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String stringDate = dateFormat.format(date);
        String stringTime = timeFormat.format(date);
//        textViewDate.setText(stringDate);
        textViewTime.setText(stringTime);

    }
}