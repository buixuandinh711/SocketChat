package com.bxd.socketchatclientside.message_activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bxd.socketchatclientside.ClientConst;
import com.bxd.socketchatclientside.R;
import com.bxd.socketchatclientside.data_manager.ClientMessage;

import java.util.List;

public class MessageRecyclerAdapter extends RecyclerView.Adapter {

    private List<ClientMessage> listMessages;
    private Context context;

    public MessageRecyclerAdapter(List<ClientMessage> listMessages, Context context) {
        this.listMessages = listMessages;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {

        ClientMessage message = listMessages.get(position);
        return message.getType();

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        if (viewType == ClientConst.MESSAGE_TYPE_SEND) {
            view = LayoutInflater.from(context).inflate(R.layout.view_message_sende, parent, false);
            return new SendMessageViewHolder(view);
        } else if (viewType == ClientConst.MESSAGE_TYPE_RECEIVE) {
            view = LayoutInflater.from(context).inflate(R.layout.view_message_receive, parent, false);
            return new ReceiveMessageViewHolder(view);
        }

        return null;

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ClientMessage message = listMessages.get(position);
        if (holder.getItemViewType() == ClientConst.MESSAGE_TYPE_SEND) {
            ((SendMessageViewHolder) holder).bind(message);
        } else if (holder.getItemViewType() == ClientConst.MESSAGE_TYPE_RECEIVE) {
            ((ReceiveMessageViewHolder) holder).bind(message);
        } else {
            ((SendMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return listMessages.size();
    }

    public void updateList(List<ClientMessage> list) {

        listMessages.clear();
        listMessages.addAll(list);
        notifyDataSetChanged();

    }
}
