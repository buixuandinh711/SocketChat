package com.bxd.socketchatclientside;

import android.util.Log;

import com.bxd.socketchatclientside.data_manager.ClientMessage;
import com.bxd.socketchatclientside.data_manager.MessageDAO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.bxd.socketchatclientside.ClientConst.*;

/**
 * A singleton class manages the socket of the program.
 */
public class SocketManager {

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private static SocketManager instance;
    /**
     * Data Access Object to access data in database.
     */
    private MessageDAO messageDAO;

    /**
     * A HashMap receives search responses from server.
     * key: code of the search request and response.
     * value: a string contains clients' names separate by a '#' character eg: client1#client2#user123
     * {@link com.bxd.socketchatclientside.frag_search.SearchFragment#searchClient(String)}
     */
    private HashMap<String, String> listSearch = new LinkedHashMap<>();
    /**
     * A HashMap receives login responses from server.
     * key: code of the login request and response.
     * value: a string contains login responses.
     */
    private HashMap<String, String> listLogin = new LinkedHashMap<>();
    /**
     * A HashMap receives register responses from server.
     * key: code of the register request and response.
     * value: a string contains register responses.
     */
    private HashMap<String, String> listRegister = new LinkedHashMap<>();

    /**
     * Set off time between sever and client.
     * Set when initialize connection to server.
     */
    private long timeOffSet = 0;
    /**
     * Unique number for search, login, register,etc... requests.
     */
    public static int requestNumber = 0;

    /**
     * Generate unique number for search, login, register,etc... requests.
     * @return code for requests.
     */
    public static int getRequestNumber() {
        int re = requestNumber;
        requestNumber++;
        return re;
    }

    /**
     * Initialize values of sockets, dataInputstream and dataOutputstream.
     * Connect to a Socket server at IP address {@param ip} and port  {@link ClientConst#DEFAULT_PORT}.
     * @param ip IP address of the socket server.
     * @return true if connect successfully, false if connect failed.
     * @throws java.net.SocketTimeoutException if not reach the socket server in {@link ClientConst#TIME_OUT} milliseconds.
     */
    public boolean connect(String ip) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                try {

                    InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, DEFAULT_PORT);
                    socket = new Socket();
                    socket.connect(inetSocketAddress, 1000);
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    sendRequest(CODE_INIT_CONNECT, "Initialize connection.");
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

                return true;

            }
        });


        boolean ret = false;
        try {
            ret = future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (ret) {
            receiveResponses();
        }

        return ret;

    }

    /**
     * Get the instance of this singleton class.
     * @return an instance of SocketManager.
     */
    public static SocketManager getInstance() {

        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;

    }

    /**
     * Receive response from server continuously.
     * Read the request code of the response to call appropriated function handling the response.
     * @throws IOException if have error while read input from server then end the function and close connection.
     */
    public void receiveResponses() {

        messageDAO = MainActivity.database.messageDAO();

        new Thread(() -> {

            while (true) {

                if (dataInputStream == null) {
                    continue;
                }
                try {
                    String jsonResponse = dataInputStream.readUTF();
                    String requestCode = MessageJSON.getMessageCode(jsonResponse);
                    Log.d("receive_resp", jsonResponse);
                    if (requestCode.contains(CODE_RECENT_MESSAGES)) {

                        JSONObject object = new JSONObject(jsonResponse);
                        JSONArray jsonArray = object.getJSONArray(MESSAGE_DATA);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Message message = new Message(jsonObject);
                            ClientMessage clientMessage = message.toClientMessage();
                            messageDAO.insert(clientMessage);
                        }

                    } else {
                        Message response = MessageJSON.getMessage(jsonResponse);
                        if (requestCode.contains(CODE_SEARCH)) {

                            listSearch.put(requestCode, response.getContent());

                        } else if (requestCode.contains(CODE_LOGIN)) {

                            listLogin.put(requestCode, response.getContent());

                        } else if (requestCode.contains(CODE_REGISTER)) {

                            listRegister.put(requestCode, response.getContent());

                        } else if (requestCode.equals(CODE_MESSAGE)) {

                            String senderID = response.getSenderID();
                            String receiverID = response.getReceiverID();
                            String content = response.getContent();
                            long sentTime = response.getSentTime();

                            ClientMessage clientMessage = new ClientMessage(receiverID, senderID, content, sentTime, MESSAGE_TYPE_RECEIVE);
                            messageDAO.insert(clientMessage);

                        } else if (requestCode.equals(CODE_INIT_CONNECT)) {
                            timeOffSet = response.getSentTime() - System.currentTimeMillis();
                            Log.d("init_conn", timeOffSet + "");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    closeConnect();
                    break;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }).start();

    }

    /**
     * Send message of content {@param content} from host client {@link MainActivity#hostUserID} to another client {@param receiverID}.
     *Save this message into local database.
     * @param receiverID id of the receiver.
     * @param content content of the message.
     * @throws IOException if have an error in writeUTF process.
     */
    public void sendMessage(String receiverID, String content) {

        Message request = new Message(MainActivity.hostUserID, receiverID, content);
        String jsonRequest = MessageJSON.createJsonMessage(CODE_MESSAGE, request);
        Log.d("send_message", jsonRequest);

        new Thread(() -> {

            try {
                dataOutputStream.writeUTF(jsonRequest);
                messageDAO.insert(new ClientMessage(MainActivity.hostUserID, receiverID, content, request.getSentTime() + timeOffSet, ClientConst.MESSAGE_TYPE_SEND));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();

    }

    /**
     * Send a request to server with given request code and request content.
     * @param requestCode
     * @param requestContent
     * @throws IOException if have an error in writeUTF process.
     */
    public void sendRequest(String requestCode, String requestContent) {

        Message request = new Message(MainActivity.hostUserID, SERVER_NAME, requestContent);
        String jsonRequest = MessageJSON.createJsonMessage(requestCode, request);
        Log.d("send_request", jsonRequest);
        try {
            dataOutputStream.writeUTF(jsonRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public HashMap<String, String> getListSearch() {
        return listSearch;
    }

    public HashMap<String, String> getListLogin() {
        return listLogin;
    }

    public HashMap<String, String> getListRegister() {
        return listRegister;
    }

    /**
     * Indicate whether the socket is connected to server.
     * @return true if connected, false if not or socket is null.
     */
    public boolean isConnected() {

        if (socket == null) {
            return false;
        }

        return socket.isConnected();
    }
    /**
     * Indicate whether the socket is closed.
     * @return true if connected, false if not or socket is null.
     */
    public boolean isClosed() {

        if (socket == null) {
            return false;
        }

        return socket.isClosed();
    }

    /**
     * Close socket connection, dataInputStream and dataOutputStream.
     * @throws IOException if has an error when closing occurs.
     */
    public void closeConnect() {
        try {
            if (dataInputStream != null) {
                dataInputStream.close();
            }
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
