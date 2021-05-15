package com.bxd.socketchatclientside;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bxd.socketchatclientside.data_manager.MessageDatabase;
import com.bxd.socketchatclientside.frag_recent_chat.RecentChatFragment;
import com.bxd.socketchatclientside.frag_search.SearchFragment;

import static com.bxd.socketchatclientside.ClientConst.CODE_LOGOUT;
import static com.bxd.socketchatclientside.ClientConst.KEY_LOGIN;
import static com.bxd.socketchatclientside.ClientConst.KEY_USERNAME;
import static com.bxd.socketchatclientside.ClientConst.UNKNOWN_CLIENT_NAME;

/**
 * Main activity of the application.
 * The app always start from this class.
 * Contain recent chats and searching clients interface.
 */
public class MainActivity extends AppCompatActivity {

    public static String hostUserID = UNKNOWN_CLIENT_NAME;

    public static MessageDatabase database;
    SocketManager socketManager;

    private FragmentManager fragmentManager;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        checkLogin();
        createFragment(ClientConst.FRAGMENT_RECENT_CHAT);
    }

    private void initialize() {
        sharedPreferences = this.getSharedPreferences(ClientConst.REFERENCE_KEY, Context.MODE_PRIVATE);
        database = Room.databaseBuilder(this, MessageDatabase.class, ClientConst.MESSAGE_DB_NAME).build();
        fragmentManager = this.getSupportFragmentManager();

        //The first connection of socket to the server when the app opens.
        socketManager = SocketManager.getInstance();
        if (!socketManager.isConnected()) {
            new Thread(() -> {

                boolean isConnected = socketManager.connect(sharedPreferences.getString(ClientConst.KEY_ADDRESS, ""));
                if (!isConnected) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Connected Failed!", Toast.LENGTH_SHORT).show();
                    });
                }

            }).start();
        }

    }

    /**
     * Create fragment that displays the list of recent chat or list of searched clients.
     * @param fragmentName {@link ClientConst#FRAGMENT_RECENT_CHAT} to display recent chat
     *                     fragment, {@link ClientConst#FRAGMENT_SEARCH} to display search.
     */
    private void createFragment(String fragmentName) {

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;
        if (fragmentName.equals(ClientConst.FRAGMENT_RECENT_CHAT)) {
            fragment = new RecentChatFragment();
        } else if (fragmentName.equals(ClientConst.FRAGMENT_SEARCH)) {
            fragment = new SearchFragment();
        } else {
            fragment = new RecentChatFragment();
        }

        fragmentTransaction.replace(R.id.frameLayoutMain, fragment, fragmentName);
        fragmentTransaction.commit();

    }

    /**
     * Check whether the user login to the application.
     * At the first time open app or not logged in then start {@link LoginActivity}
     * for logging in or registering.
     */
    private void checkLogin() {

        if (!sharedPreferences.contains(KEY_LOGIN)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_LOGIN, false);
            editor.putString(KEY_USERNAME, UNKNOWN_CLIENT_NAME);
            editor.apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean(KEY_LOGIN, false);
//        editor.apply();
        boolean is_login = sharedPreferences.getBoolean(KEY_LOGIN, false);

        if (!is_login) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        hostUserID = sharedPreferences.getString(KEY_USERNAME, UNKNOWN_CLIENT_NAME);
        getSupportActionBar().setTitle(hostUserID);

    }

    /**
     * Log out the app, clear the local message data of the user, send a log out request to the server.
     */
    private void logout() {

        SharedPreferences sharedPreferences = this.getSharedPreferences(ClientConst.REFERENCE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_LOGIN, false);
        editor.putString(KEY_USERNAME, UNKNOWN_CLIENT_NAME);
        editor.apply();
        String hostID = hostUserID;
        new Thread(() -> {
            socketManager.sendRequest(CODE_LOGOUT, "Logout");
            database.messageDAO().deleteHostMessages(hostID);
        }).start();
        hostUserID = UNKNOWN_CLIENT_NAME;
        startActivity(new Intent(this, LoginActivity.class));
        finish();

    }

    /**
     * Create menu provide searching clients, loging out and reconnecting.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.itemMenuSearch);
        MenuItem logoutItem = menu.findItem(R.id.itemMenuLogout);
        MenuItem reconnectItem = menu.findItem(R.id.itemMenuReconnect);
        SearchView searchView = (SearchView) searchItem.getActionView();
        initSearch(searchView);

        logoutItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                logout();
                return false;
            }
        });

        reconnectItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (!socketManager.isConnected() || socketManager.isClosed()) {
                    new Thread(() -> {

                        boolean isConnected = socketManager.connect(sharedPreferences.getString(ClientConst.KEY_ADDRESS, ""));
                        if (isConnected) {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Connected Successfully!", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Connected Failed!", Toast.LENGTH_SHORT).show();
                            });
                        }

                    }).start();
                }

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Provide interface for the searching clients.
     * @param searchView
     */
    private void initSearch(SearchView searchView) {
        searchView.setQueryHint("Search Client");
        searchView.setOnSearchClickListener(v -> {
            createFragment(ClientConst.FRAGMENT_SEARCH);
        });
        searchView.setOnCloseListener(() -> {
            createFragment(ClientConst.FRAGMENT_RECENT_CHAT);
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                SearchFragment searchFragment = (SearchFragment) fragmentManager.findFragmentByTag(ClientConst.FRAGMENT_SEARCH);
                if (searchFragment != null) {
                    String q = newText.trim();
                    if (q.length() > 0) {
                        searchFragment.searchClient(q);
                    }
                }
                return false;
            }
        });
    }
}