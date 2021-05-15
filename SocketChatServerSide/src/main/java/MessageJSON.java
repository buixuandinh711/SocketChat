import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.List;

/**
 * Provide some static functions to handle the json message.
 */
public class MessageJSON {

    public static String createJsonMessage(String requestCode, Message message) {

        String ret = "";

        JSONObject root = new JSONObject();
        root.put(ServerConst.REQUEST_CODE, requestCode);
        JSONObject jsonData = message.toJSONObject();
        root.put(ServerConst.MESSAGE_DATA, jsonData);

        ret = root.toString();

        return ret;
    }

    public static String createJsonMessage(String requestCode, List<Message> listMessages) {

        String ret = "";

        JSONObject root = new JSONObject();
        root.put(ServerConst.REQUEST_CODE, requestCode);
        JSONArray jsonArray = new JSONArray();
        for (Message m: listMessages) {
            jsonArray.add(m.toJSONObject());
        }
        root.put(ServerConst.MESSAGE_DATA, jsonArray);

        ret = root.toString();

        return ret;
    }

    public static Message getMessage(String json) {

        Message message = null;
        try {
            JSONObject root = (JSONObject) JSONValue.parse(json);
            JSONObject data = (JSONObject) root.get(ServerConst.MESSAGE_DATA);
            message = new Message(data);
        } catch (Exception e) {
            System.out.println("Get message from json failed!");
        }

        return message;

    }

    public static String getMessageCode(String message) {

        String ret = "";

        try {
            JSONObject root = (JSONObject) JSONValue.parse(message);
            ret = (String) root.get(ServerConst.REQUEST_CODE);
        } catch (Exception e) {
            System.out.println("Get requestCode from json failed!");
        }

        return ret;

    }

}
