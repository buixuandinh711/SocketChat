package com.bxd.socketchatclientside.data_manager;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public class ClientMessage {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @NonNull
    @ColumnInfo(name = "host_id")
    private String hostID;

    @NonNull
    @ColumnInfo(name = "client_id")
    private String clientID;

    @NonNull
    @ColumnInfo(name = "content")
    private String content;

    @NonNull
    @ColumnInfo(name = "sent_time")
    private long sentTime;

    @NonNull
    @ColumnInfo(name = "type")
    private int type;

    public ClientMessage(@NonNull String hostID, @NonNull String clientID, @NonNull String content, long sentTime, int type) {
        this.hostID = hostID;
        this.clientID = clientID;
        this.content = content;
        this.sentTime = sentTime;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    @NonNull
    public String getContent() {
        return content;
    }

    public void setContent(@NonNull String content) {
        this.content = content;
    }

    public long getSentTime() {
        return sentTime;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    @NonNull
    public String getHostID() {
        return hostID;
    }

    public void setHostID(@NonNull String hostID) {
        this.hostID = hostID;
    }

    @NonNull
    public String getClientID() {
        return clientID;
    }

    public void setClientID(@NonNull String clientID) {
        this.clientID = clientID;
    }

    @NonNull
    @Override
    public String toString() {
        return id + " " + hostID + " " + clientID + " " +  content + " " + sentTime;
    }
}
