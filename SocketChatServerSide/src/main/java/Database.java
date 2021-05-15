import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private Connection connection;
    private static Database instance = null;

    private Database() {

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("Load sqlite library failed: " + e);
        }
        String url = "jdbc:sqlite:" + "src/main/resources/database/client_data.db";
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Get connection failed: " + e);
        }

    }

    public static Database getInstance() {

        if (instance == null) {
            instance = new Database();
        }

        return instance;

    }

    public boolean createANewClient(String userID, String password) {

        try {
            Statement statement = connection.createStatement();

            if (checkClientExistence(userID)) {
                return false;
            }
            String insertAClient = "INSERT INTO user_profiles (user_id, password, user_name) VALUES ('"
                    + userID + "', '" + password + "', 'Ko co tg')";
            statement.executeUpdate(insertAClient);

        } catch (SQLException e) {
            System.out.println("Insert a client " + userID + "into database failed!");
            return false;
        }

        return true;
    }

    public boolean checkClientLogin(String userID, String password) {

        try {

            Statement statement = connection.createStatement();
            String queryUserID = "SELECT user_id FROM user_profiles WHERE user_id = '" + userID + "' AND password = '" + password + "'";
            ResultSet resultSetQueryUserID = statement.executeQuery(queryUserID);
            if (resultSetQueryUserID.next()) {
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Check login of client " + userID + " from database failed!");
            return false;
        }

        return false;

    }

    public boolean checkClientExistence(String userID) {

        try {

            Statement statement = connection.createStatement();
            String queryUserID = "SELECT user_id FROM user_profiles WHERE user_id = '" + userID + "'";
            ResultSet resultSetQueryUserID = statement.executeQuery(queryUserID);
            if (resultSetQueryUserID.next()) {
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Check existence of client " + userID + " failed");
            return false;
        }

        return false;
    }

    public String searchClient(String query) {

        String res = "";

        try {

            Statement statement = connection.createStatement();
            String s = "SELECT user_id FROM user_profiles WHERE user_id LIKE '" + query + "%'";
            ResultSet resultSet = statement.executeQuery(s);

            while (resultSet.next()) {
                res += resultSet.getString(1) + "#";
            }


        } catch (SQLException e) {
            System.out.println("Search query " + query + " failed!");
        }

        return res;

    }

    public void insertMessage(Message message) {

        String senderID = message.getSenderID();
        String receiverID = message.getReceiverID();
        String content = message.getContent();
        long sentTime = message.getSentTime();

        String sql = "INSERT INTO messages (sender_id, receiver_id, content, sent_time) " +
                "VALUES ('" + senderID + "', '" + receiverID + "', '" + content + "', "
                + sentTime + ")";
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException throwable) {
            System.out.println("Insert message " + message.toString() + " into database failed!");
        }

    }

    public List<Message> getRecentMessage(String clientID) {

        List<Message> listMessages = new ArrayList<>();
        String sql = "SELECT * " +
                "FROM messages m1 " +
                "WHERE sent_time = " +
                "(SELECT MAX(sent_time) " +
                "FROM messages m2 " +
                "WHERE (m1.sender_id = m2.sender_id AND m1.receiver_id = m2.receiver_id) " +
                "OR (m1.sender_id = m2.receiver_id AND m1.receiver_id = m2.sender_id)) " +
                "AND (sender_id = '" + clientID + "' OR receiver_id = '" + clientID + "')" +
                "ORDER BY sent_time DESC " +
                "LIMIT 10";
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {

                String senderID = resultSet.getString("sender_id");
                String receiverID = resultSet.getString("receiver_id");
                String content = resultSet.getString("content");
                long sentTime = resultSet.getLong("sent_time");

                Message message = new Message(senderID, receiverID, content, sentTime);
                listMessages.add(message);

            }
        } catch (SQLException throwable) {
            System.out.println("Get recent chats of client " + clientID + " failed!");
        }

        return listMessages;

    }

    public List<Message> getRecentMessage(Message message) {

        List<Message> listMessages = new ArrayList<>();
        String sql = "SELECT * " +
                "FROM messages " +
                "WHERE ((sender_id = '" + message.getSenderID() + "' AND receiver_id = '" + message.getReceiverID() + "') " +
                "OR (sender_id = '" + message.getReceiverID() + "' AND receiver_id = '" + message.getSenderID() + "')) " +
                "AND sent_time < " + message.getSentTime() + " " +
                "ORDER BY sent_time DESC " +
                "LIMIT 10";
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {

                String senderID = resultSet.getString("sender_id");
                String receiverID = resultSet.getString("receiver_id");
                String content = resultSet.getString("content");
                long sentTime = resultSet.getLong("sent_time");

                Message m = new Message(senderID, receiverID, content, sentTime);
                listMessages.add(m);

            }
        } catch (SQLException throwable) {
            System.out.println("Get recent messages of clients " + message.getSenderID() + " and "
            + message.getReceiverID() + " failed!");
        }

        return listMessages;

    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void showAllRecords(String tableName) {

        try {

            String sql = "SELECT * FROM " + tableName;
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            ResultSetMetaData metadata = rs.getMetaData();
            int columnCount = metadata.getColumnCount();
            System.out.println();
            while (rs.next()) {
                String row = "";
                for (int i = 1; i <= columnCount; i++) {
                    row += rs.getString(i) + ", ";
                }
                System.out.println(row);

            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void describe(String tableName) {

        String sql = "PRAGMA table_info('" + tableName + "')";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnNum = metaData.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnNum; i++) {
                    System.out.printf(resultSet.getString(i) + " ");
                }
                System.out.println();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    private void deleteAll(String tableName) {

        String sql = "DELETE FROM " + tableName;
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

//    public static void main(String[] args) {
//
//        Database database = Database.getInstance();
//        database.showAllRecords("messages");
//        database.showAllRecords("user_profiles");
//        database.closeConnection();
//
//    }
}
