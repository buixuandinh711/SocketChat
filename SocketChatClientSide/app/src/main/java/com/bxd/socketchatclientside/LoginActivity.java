package com.bxd.socketchatclientside;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginActivity extends AppCompatActivity {

    Button buttonLogin;
    Button buttonRegister;
    EditText editTextUsername;
    EditText editTextPassword;

    SocketManager socketManager;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login");
        this.socketManager = SocketManager.getInstance();
        map();
        initialize();
        clickButtonLogin();
        clickButtonRegister();
    }

    private void initialize() {
        sharedPreferences = this.getSharedPreferences(ClientConst.REFERENCE_KEY, Context.MODE_PRIVATE);
        if (!sharedPreferences.contains(ClientConst.KEY_ADDRESS)) {
            createConnectDialog(false);
        }
    }

    private void map() {
        this.editTextUsername = findViewById(R.id.editTextUsername);
        this.editTextPassword = findViewById(R.id.editTextPassword);
        this.buttonLogin = findViewById(R.id.buttonLogin);
        this.buttonRegister = findViewById(R.id.buttonRegister);
    }

    /**
     * Start MainActivity if login successfully.
     * @param hostUserID id of host that login successfully.
     */
    private void enterMainActivity(String hostUserID) {

        MainActivity.hostUserID = hostUserID;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(ClientConst.KEY_LOGIN, true);
        editor.putString(ClientConst.KEY_USERNAME, hostUserID);
        editor.apply();
        new Thread(()->{
            socketManager.sendRequest(ClientConst.CODE_RECENT_MESSAGES, hostUserID);
        }).start();
        startActivity(new Intent(this, MainActivity.class));
        finish();

    }

    /**
     * Send a login response to the server.
     * login successfully if server response {@link ClientConst#RESULT_SUCCESSFUL} and failed if {@link ClientConst#RESULT_FAILED}
     * in the content field off the message.
     */
    private void clickButtonLogin() {

        buttonLogin.setOnClickListener(v -> {

            String userID = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (!checkValidInput(userID, password)) {
                Toast.makeText(LoginActivity.this, "Username or password is wrong format!", Toast.LENGTH_SHORT).show();
                return;
            }

            String requestCode = ClientConst.CODE_LOGIN + SocketManager.getRequestNumber();
            String requestContent = userID + "#" + password;

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<String> future = executorService.submit((Callable<String>) () -> {
                socketManager.sendRequest(requestCode, requestContent);
                int timeToLive = 0;
                while (timeToLive < 10) {

                    if (socketManager.getListLogin().containsKey(requestCode)) {
                        return socketManager.getListLogin().get(requestCode);
                    }
                    timeToLive++;
                    Thread.sleep(100);

                }
                return ClientConst.RESULT_FAILED;
            });
            try {
                String serverResponse = future.get();

                if (serverResponse.equals(ClientConst.RESULT_SUCCESSFUL)) {
                    enterMainActivity(userID);
                } else if (serverResponse.equals(ClientConst.RESULT_FAILED)) {
                    Toast.makeText(LoginActivity.this, "Login failed!", Toast.LENGTH_SHORT).show();
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });

    }

    /**
     * Send a register response to the server.
     * register successfully if server response {@link ClientConst#RESULT_SUCCESSFUL} and failed if {@link ClientConst#RESULT_FAILED}
     * in the content field off the message.
     */
    private void clickButtonRegister() {

        buttonRegister.setOnClickListener((v) -> {

            String userID = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (userID.length() == 0 || password.length() == 0) {
                Toast.makeText(LoginActivity.this, "Enter username and password!", Toast.LENGTH_SHORT).show();
                return;
            }

            String requestCode = ClientConst.CODE_REGISTER + SocketManager.getRequestNumber();
            String requestContent = userID + "#" + password;

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<String> future = executorService.submit((Callable<String>) () -> {
                socketManager.sendRequest(requestCode, requestContent);
                int timeToLive = 0;
                while (timeToLive < 10) {

                    if (socketManager.getListRegister().containsKey(requestCode)) {
                        return socketManager.getListRegister().get(requestCode);
                    }
                    timeToLive++;
                    Thread.sleep(100);

                }
                return ClientConst.RESULT_FAILED;
            });
            try {
                String serverResponse = future.get();

                if (serverResponse.equals(ClientConst.RESULT_SUCCESSFUL)) {
                    Toast.makeText(LoginActivity.this, "Register successfully!", Toast.LENGTH_SHORT).show();
                } else if (serverResponse.equals(ClientConst.RESULT_FAILED)) {
                    Toast.makeText(LoginActivity.this, "Register failed!", Toast.LENGTH_SHORT).show();
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });

    }

    /**
     * Check validate of the userID and password before send response to server.
     * @param userID
     * @param password
     * @return true if validate userID and password else false.
     */
    private boolean checkValidInput(String userID, String password) {

        if (!userID.matches(ClientConst.REGEX_USERNAME_PASSWORD)
                || !password.matches(ClientConst.REGEX_USERNAME_PASSWORD)) {
            return false;
        }

        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.connect_server_menu, menu);
        MenuItem itemConnect = menu.findItem(R.id.itemConnect);
        itemConnect.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                createConnectDialog(true);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Create an dialog that user can change IP address of the server connect to.
     * Display a toast indicates whether the connection is successful.
     * @param canClose false in the first time open the app, the user must
     *                 enter the IP Address server that socket connects to.
     */
    private void createConnectDialog(boolean canClose) {
        Dialog dialog = new Dialog(LoginActivity.this);
        dialog.setContentView(R.layout.dialog_connect_server);
        dialog.setCanceledOnTouchOutside(canClose);

        Button buttonConnect = dialog.findViewById(R.id.buttonConnect);
        EditText editTextIPAddress = dialog.findViewById(R.id.editTextIPAddress);

        String curentIP = sharedPreferences.getString(ClientConst.KEY_ADDRESS, "");
        editTextIPAddress.setText(curentIP);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = editTextIPAddress.getText().toString().trim();

                if (!ip.matches(ClientConst.REGEX_IP_ADDRESS)) {
                    Toast.makeText(LoginActivity.this, "Invalid IP Address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!ip.equals(curentIP) || !socketManager.isConnected() || socketManager.isClosed()) {
                    if (!ip.equals(curentIP)) {
                        socketManager.closeConnect();
                    }
                    boolean isConnected = socketManager.connect(ip);
                    if (isConnected) {

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(ClientConst.KEY_ADDRESS, ip);
                        editor.apply();
                        dialog.cancel();
                        Toast.makeText(LoginActivity.this, "Connect Successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Connect Failed!", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(LoginActivity.this, "Connected This IP Already!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

}