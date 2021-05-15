package com.bxd.socketchatclientside.frag_recent_chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bxd.socketchatclientside.ClientConst;
import com.bxd.socketchatclientside.MainActivity;
import com.bxd.socketchatclientside.R;
import com.bxd.socketchatclientside.data_manager.ClientMessage;
import com.bxd.socketchatclientside.data_manager.MessageDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment show information about recent clients chatted with host client.
 */
public class RecentChatFragment extends Fragment {

    RecyclerView recyclerViewChat;

    ChatAdapter adapter;
    LinearLayoutManager layoutManager;

    MessageDAO messageDAO;

    private volatile boolean isRun;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recent_chat, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {

        recyclerViewChat = view.findViewById(R.id.recyclerViewChat);
        List<ClientMessage> list = new ArrayList<>();
        adapter = new ChatAdapter(list, getActivity());
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewChat.setAdapter(adapter);
        recyclerViewChat.setLayoutManager(layoutManager);
        messageDAO = MainActivity.database.messageDAO();
        isRun = true;

    }

    /**
     * Update the list recent chat continuously.
     */
    private void update() {

        new Thread(()->{

            while (true) {

                if (!isRun) {
                    Thread.interrupted();
                    break;
                }

                List<ClientMessage> list = messageDAO.getRecentChat(MainActivity.hostUserID);
                getActivity().runOnUiThread(()->{
                    adapter.update(list);
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();

    }

    @Override
    public void onStart() {
        super.onStart();
        isRun = true;
        update();
    }

    @Override
    public void onPause() {
        super.onPause();
        isRun = false;
    }
}
