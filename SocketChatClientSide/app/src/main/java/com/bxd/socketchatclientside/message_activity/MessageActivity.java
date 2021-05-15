package com.bxd.socketchatclientside.message_activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bxd.socketchatclientside.ClientConst;
import com.bxd.socketchatclientside.MainActivity;
import com.bxd.socketchatclientside.R;
import com.bxd.socketchatclientside.SocketManager;
import com.bxd.socketchatclientside.data_manager.ClientMessage;
import com.bxd.socketchatclientside.data_manager.MessageDAO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    RecyclerView recyclerViewMessage;
    EditText editTextMessage;
    Button buttonSendMessage;

    /**
     * UserID of client that host chatting to.
     */
    String clientUserID;

    MessageDAO messageDAO;
    SocketManager socketManager;

    MessageRecyclerAdapter adapter;
    LinearLayoutManager layoutManager;

    Thread threadReceiveMessage;
    /**
     * Indicate whether continue to run the threadReceiveMessage.
     */
    private volatile boolean isStillRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        initialize();

    }

    private void initialize() {

        setBarName();
        map();
        messageDAO = MainActivity.database.messageDAO();
        List<ClientMessage> listClientMessages = new ArrayList<>();
        adapter = new MessageRecyclerAdapter(listClientMessages, this);
        layoutManager = new LinearLayoutManager(this);
        recyclerViewMessage.setAdapter(adapter);
        recyclerViewMessage.setLayoutManager(layoutManager);
        onKeyboardAppear();
        clickButtonSend();
        isStillRunning = true;
        initializeThread();

    }

    /**
     * Set name of action bar to name of chatting client.
     */
    private void setBarName() {

        Intent intent = getIntent();
        clientUserID = intent.getStringExtra(ClientConst.CLIENT_USERID_INTENT_DATA);
        getSupportActionBar().setTitle(clientUserID);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void map() {

        recyclerViewMessage = findViewById(R.id.recyclerViewMessage);
        buttonSendMessage = findViewById(R.id.buttonSend);
        editTextMessage = findViewById(R.id.editTextMessage);
        socketManager = SocketManager.getInstance();

    }

    /**
     * Scroll the message view to bottom when keyboard appears.
     */
    private void onKeyboardAppear() {

        recyclerViewMessage.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    int itemCount = adapter.getItemCount();
                    if (itemCount > 0) {
                        recyclerViewMessage.smoothScrollToPosition(itemCount - 1);
                    }
                }
            }
        });

    }

    /**
     * Send a message when buttonSend is click.
     * @see SocketManager#sendMessage(String, String) .
     */
    private void clickButtonSend() {

        buttonSendMessage.setOnClickListener(v -> {
            int itemCount = adapter.getItemCount();
            if (itemCount > 0) {
                recyclerViewMessage.smoothScrollToPosition(itemCount - 1);
            }
            String message = editTextMessage.getText().toString().trim();
            editTextMessage.setText("");
            if (message.length() > 0) {
                socketManager.sendMessage(clientUserID, message);
            }
        });

    }

    /**
     * Initialize the thread the run the update message from local database continuously.
     */
    private void initializeThread() {
        threadReceiveMessage = new Thread(() -> {

            while (isStillRunning) {
                List<ClientMessage> list = messageDAO.getChatMessages(MainActivity.hostUserID, clientUserID);
                Collections.reverse(list);
                Log.d("thread_message", clientUserID + " is still running");
                runOnUiThread(() -> {

                    boolean canScrollVertically = recyclerViewMessage.canScrollVertically(1);
                    adapter.updateList(list);
                    int itemCount = adapter.getItemCount();
                    if (!canScrollVertically && itemCount > 0) {
                        recyclerViewMessage.smoothScrollToPosition(itemCount - 1);
                    }

                });
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     * return to {@link MainActivity}.
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isStillRunning = true;
        threadReceiveMessage.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isStillRunning = false;
        threadReceiveMessage.interrupt();
    }
}