package com.bxd.socketchatclientside.data_manager;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MessageDAO {

    @Query("SELECT * FROM messages")
    public List<ClientMessage> getAllMessage();

    @Insert
    public void insert(ClientMessage clientMessage);

    @Query("SELECT * " +
            "FROM messages " +
            "WHERE host_id = :hostID AND client_id = :clientID " +
            "ORDER BY sent_time DESC " +
            "LIMIT 20")
    public List<ClientMessage> getChatMessages(String hostID, String clientID);

    @Query("SELECT * " +
            "FROM messages m1 " +
            "WHERE sent_time = (" +
            "   SELECT MAX(sent_time) " +
            "   FROM messages m2" +
            "   WHERE m2.client_id = m1.client_id)" +
            "AND host_id = :hostID " +
            "ORDER BY sent_time DESC " +
            "LIMIT 10" )
    public List<ClientMessage> getRecentChat(String hostID);


    @Query("DELETE FROM messages WHERE host_id = :hostID")
    public void deleteHostMessages(String hostID);

}
