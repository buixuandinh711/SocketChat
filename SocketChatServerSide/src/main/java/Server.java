import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class Server {

    ServerSocket serverSocket;
    Database database;
    public List<ClientHandler> listClientHandlers;
    public static List<Message> listMessages = new ArrayList<>();
    public static Map<String, DataOutputStream> listClientOutputStream = new HashMap<>();

    public Server() throws IOException {
        serverSocket = new ServerSocket(8888);
        this.listClientHandlers = new ArrayList<>();
        System.out.println("Server started!\nWaiting for clients.");
        database = Database.getInstance();
        sendMessagePeriodically();
    }

    /**
     * Server create new socket to accept connection from clients.
     * Create new ClientHandler object to handle connection from new thread.
     * @throws IOException if the accepting occurs error.
     */
    private void runs() {

        while (true) {

            try {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                listClientHandlers.add(clientHandler);
                clientHandler.start();
            } catch (IOException ioException) {
                System.out.println("Server runs:" + "Init a client failed!" + ioException.getLocalizedMessage());
            }

        }

    }

    /**
     * Send message from sending clients to receiving client
     * if receiving client is connecting to server.
     */
    private void sendMessagePeriodically() {

        Thread threadSendMessageToClients = new Thread(() -> {
            while (true) {
                int len = listMessages.size();
                for (int i = 0; i < len; i++) {
                    Message message = listMessages.get(i);
                    String receiverID = message.getReceiverID();
                    if (listClientOutputStream.containsKey(receiverID)) {
                        String jsonMessage = MessageJSON.createJsonMessage(ServerConst.CODE_MESSAGE, message);
                        try {
                            listClientOutputStream.get(receiverID).writeUTF(jsonMessage);
                            database.insertMessage(message);
                            listMessages.remove(i);
                            i--;
                            len--;
                            System.out.println("Send a message from " + message.getSenderID() + " to " + message.getReceiverID());
                        } catch (IOException ioException) {
                            System.out.println("Send a message from " + message.getSenderID() + " to " + message.getReceiverID() + " failed!");

                        }
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        threadSendMessageToClients.start();

    }

    /**
     * Get IPv4 addresses of the device running server in connections it connects to.
     * Try these ip addresses to find the address of the device's wifi connection,
     * client must get ip address of server in wifi connection to connect to the server.
     * @return list of IPv4 addresses.
     */
    public static List<String> getIpAddress() {
        List<String> listIpAddress = new ArrayList<>();
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {

                NetworkInterface networkInterface = (NetworkInterface) en.nextElement();

                for (Enumeration enumeration = networkInterface.getInetAddresses(); enumeration.hasMoreElements();) {

                    InetAddress inetAddress = (InetAddress) enumeration.nextElement();

                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String ipAddress= inetAddress.getHostAddress();
                        listIpAddress.add(ipAddress);
                    }
                }

            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }

        try {

            InetAddress inetAddress = InetAddress.getLocalHost();
            String localhostIp = inetAddress.getHostAddress();
            listIpAddress.remove(localhostIp);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return listIpAddress;
    }

    public static void main(String[] args) {
        System.out.println("Try follow ip address to connect to server:");
        for (String s: getIpAddress()) {
            System.out.println(s);;
        }
        System.out.println("--------------------------------------------");
        try {
            Server server = new Server();
            server.runs();
        } catch (IOException ioException) {
            System.out.println("Create server failed!");
        }
    }

}
