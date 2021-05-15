import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Convert json messages between client and server to a message object
 * for easier handling data.
 */
public class Message {

    private String senderID;
    private String receiverID;
    private String content;
    private long sentTime;

    public Message(String senderID, String receiverID, String content, long sentTime) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.content = content;
        this.sentTime = sentTime;
    }

    public Message(String senderID, String receiverID, String content) {

        this.senderID = senderID;
        this.receiverID = receiverID;
        this.content = content;
        sentTime = System.currentTimeMillis();

    }

    public Message(JSONObject jsonMessage) {

        this.senderID = (String) jsonMessage.get(ServerConst.SENDER_ID);
        this.receiverID = (String) jsonMessage.get(ServerConst.RECEIVER_ID);
        this.content = (String) jsonMessage.get(ServerConst.CONTENT);
        sentTime = (long) jsonMessage.get(ServerConst.SENT_TIME);

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


    public JSONObject toJSONObject() {

        JSONObject jsonData = new JSONObject();
        jsonData.put(ServerConst.SENDER_ID, senderID);
        jsonData.put(ServerConst.RECEIVER_ID, receiverID);
        jsonData.put(ServerConst.CONTENT, content);
        jsonData.put(ServerConst.SENT_TIME, sentTime);

        return jsonData;
    }

    @Override
    public String toString() {
        return this.senderID
                + " " + this.receiverID + " " + this.content + " " + this.sentTime;
    }
}
