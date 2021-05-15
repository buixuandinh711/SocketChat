package com.bxd.socketchatclientside;

import com.bxd.socketchatclientside.data_manager.ClientMessage;

import org.json.JSONException;
import org.json.JSONObject;

import static com.bxd.socketchatclientside.ClientConst.*;

/**
 * Convert json messages between client and server to a message object
 * for easier handling data.
 */
public class Message {

    private String senderID;
    private String receiverID;
    private String content;
    private long sentTime;

    public Message(String senderUsername, String receiverUsername, String content) {

        this.senderID = senderUsername;
        this.receiverID = receiverUsername;
        this.content = content;
        sentTime = System.currentTimeMillis();

    }

    public Message(JSONObject jsonMessage) {

        try {

            this.senderID = jsonMessage.getString(SENDER_ID);
            this.receiverID = jsonMessage.getString(RECEIVER_ID);
            this.content = jsonMessage.getString(CONTENT);
            sentTime = jsonMessage.getLong(SENT_TIME);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String getSenderID() {
        return senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public String getContent() {
        return content;
    }

    public long getSentTime() {
        return sentTime;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public JSONObject toJSONObject() {

        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put(SENDER_ID, senderID);
            jsonData.put(RECEIVER_ID, receiverID);
            jsonData.put(CONTENT, content);
            jsonData.put(SENT_TIME, sentTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonData;
    }

    public ClientMessage toClientMessage() {
        ClientMessage clientMessage = null;
        if (senderID.equals(MainActivity.hostUserID)) {
            clientMessage = new ClientMessage(MainActivity.hostUserID, receiverID, content, sentTime, MESSAGE_TYPE_SEND);
        } else {
            clientMessage = new ClientMessage(MainActivity.hostUserID, senderID, content, sentTime, MESSAGE_TYPE_RECEIVE);
        }
        return clientMessage;
    }

    @Override
    public String toString() {
        return this.senderID
                + " " + this.receiverID + " " + this.content + " " + this.sentTime;
    }
}
