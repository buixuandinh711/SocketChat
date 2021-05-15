package com.bxd.socketchatclientside.data_manager;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * This application use Room to manage the local database
 * @see <a href="https://developer.android.com/training/data-storage/room">Room Android</a>
 */
@Database(entities = {ClientMessage.class}, version = 1, exportSchema = false)
public abstract class MessageDatabase extends RoomDatabase {

    public abstract MessageDAO messageDAO();

}