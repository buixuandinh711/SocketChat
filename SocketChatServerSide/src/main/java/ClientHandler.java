import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * Handle a connect of client in another thread.
 * Get the request from client and return response.
 */
public class ClientHandler extends Thread {

    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private String clientID;

    public ClientHandler(Socket socket) {

        this.socket = socket;
        try {

            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());

            String initRequest = dataInputStream.readUTF();
            Message requestData = MessageJSON.getMessage(initRequest);
            setClientID(requestData.getSenderID());
            sendAResponse(MessageJSON.getMessageCode(initRequest), "Offset time.");
            System.out.println("Client " + this.clientID + " connect to server");

        } catch (IOException ioException) {
            System.out.println("ClientHandler: " + "Client " + this.clientID + " connected failed!");
        }

    }

    /**
     * Receive requests from clients in json message strings.
     * Read the request code to choose appropriate function handle
     * the request then return to client response.
     */
    @Override
    public void run() {

        while (true) {

            try {
                String request =  dataInputStream.readUTF();
                String requestCode = MessageJSON.getMessageCode(request);
                Message requestData =MessageJSON.getMessage(request);
                System.out.println("Receive a " + requestCode + " request.");
                if (requestCode.equals(ServerConst.CODE_MESSAGE)) {
                    sendMessage(requestData);
                } else if (requestCode.contains(ServerConst.CODE_REGISTER)) {
                    register(requestCode, requestData);
                } else if (requestCode.contains(ServerConst.CODE_LOGIN)) {
                    login(requestCode, requestData);
                } else if (request.contains(ServerConst.CODE_SEARCH)) {
                    searchClient(requestCode, requestData);
                } else if (request.contains(ServerConst.CODE_LOGOUT)) {
                    logout();
                } else if (requestCode.equals(ServerConst.CODE_RECENT_MESSAGES)) {
                    responseRecentMessages(requestCode, requestData);
                }

            } catch (IOException ioException) {
                System.out.println("run:" +  "Client " + " closed connection!");
                closeConnect();
                break;
            }

        }

    }

    /**
     * Called when receiving {@link ServerConst#CODE_RECENT_MESSAGES}.
     * Get recent messages the clients chat to then return this messages
     * to client.
     * @param requestCode
     * @param message
     */
    private void responseRecentMessages(String requestCode, Message message) {
        String senderID = message.getContent();
        Database database = Database.getInstance();
        List<Message> listMessages = database.getRecentMessage(senderID);
        String jsonResponse = MessageJSON.createJsonMessage(requestCode, listMessages);
        sendAResponse(jsonResponse);
        for (Message m:listMessages) {
            String jr = MessageJSON.createJsonMessage(requestCode, database.getRecentMessage(m));
            sendAResponse(jr);
        }
    }

    private void logout() {
        Server.listClientOutputStream.remove(clientID);
        System.out.println("client " + clientID + " logout");
        clientID = ServerConst.UNKNOWN_CLIENT_NAME;

    }

    /**
     * Get the userID and password the client send to server, check existence of userID
     * then save them into user_profiles database.
     * @param requestCode {@link ServerConst#CODE_REGISTER}
     * @param requestData message contains userID and password in content field, separated by '#' character.
     */
    private void register(String requestCode, Message requestData) {

        String[] userProfiles = requestData.getContent().split("#");
        String userID = userProfiles[0];
        String password = userProfiles[1];
        Database database = Database.getInstance();
        boolean isCreateSuccessfully = database.createANewClient(userID, password);
        if (isCreateSuccessfully) {
            sendAResponse(requestCode, ServerConst.RESULT_SUCCESSFUL);
            System.out.println("Client " + userID + " register successfully.");
        } else {
            sendAResponse(requestCode, ServerConst.RESULT_FAILED);
        }

    }

    /**
     * Get the userID and password the client send to server, check validate of userID and password
     * then response to client
     * @param requestCode {@link ServerConst#CODE_LOGIN}
     * @param requestData message contains userID and password in content field, separated by '#' character.
     */
    private void login(String requestCode, Message requestData) {

        String[] userProfiles = requestData.getContent().split("#");
        String userID = userProfiles[0];
        String password = userProfiles[1];

//        if (Server.listClientOutputStream.containsKey(userID)) {
//            sendAResponse(requestCode, ServerConst.RESULT_FAILED);
//            return;
//        }

        Database database = Database.getInstance();
        boolean isLoginSuccessfully = database.checkClientLogin(userID, password);
        if (isLoginSuccessfully) {
            setClientID(userID);
            System.out.println("Client " + userID + " login successfully.");
            sendAResponse(requestCode, ServerConst.RESULT_SUCCESSFUL);
        } else {
            sendAResponse(requestCode, ServerConst.RESULT_FAILED);
        }

    }

    /**
     * Get the query the client send to server, search clients matched then
     * return them to clients in a message's content field, separated by '#' character.
     * @param requestCode {@link ServerConst#CODE_LOGIN}
     * @param requestData message contains query string in content field.
     */
    public void searchClient(String requestCode, Message requestData) {

        Database database = Database.getInstance();
        String content = database.searchClient(requestData.getContent());
        sendAResponse(requestCode, content);

    }

    public void sendMessage(Message message) {

        Server.listMessages.add(message);

    }

    public void closeConnect() {
        try {
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
            Server.listClientOutputStream.remove(clientID);
        } catch (IOException ioException) {
            System.out.println("closeConnect: " + "Close connect of " + this.clientID + " failed!");
        }
    }


    public void sendAResponse(String responseCode, String content) {

        Message response = new Message(ServerConst.SERVER_NAME, clientID, content);
        String jsonResponse = MessageJSON.createJsonMessage(responseCode, response);

        try {
            dataOutputStream.writeUTF(jsonResponse);
        } catch (IOException ioException) {
            System.out.println("sendAResponse: " + "Send response " + jsonResponse + "failed!");
        }

    }

    private void sendAResponse(String response) {
        try {
            dataOutputStream.writeUTF(response);
        } catch (IOException ioException) {
            System.out.println("sendAResponse: " + "Send response " + response + "failed!");
        }
    }

    /**
     * Set clientID of the socket connection when initializing connection or
     * logging in successfully them put the dataOutputStream of clients into
     * {@link Server#listClientOutputStream} to receiving messages from other clients.
     * @param clientID
     */
    private void setClientID(String clientID) {

        this.clientID = clientID;

        if (clientID.equals(ServerConst.UNKNOWN_CLIENT_NAME)) {
            return;
        }
        Server.listClientOutputStream.put(clientID, dataOutputStream);

    }
}
