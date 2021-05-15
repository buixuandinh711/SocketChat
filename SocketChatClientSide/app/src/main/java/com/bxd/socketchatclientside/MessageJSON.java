package com.bxd.socketchatclientside;

import org.json.JSONException;
import org.json.JSONObject;

import static com.bxd.socketchatclientside.ClientConst.*;

/**
 * Provide some static functions to handle the json message.
 */
public abstract class MessageJSON {

    /**
     * Create a json message with given reqeust code and an message object.
     * @param requestCode
     * @param message
     * @return json String form json message between server and client.
     */
    public static String createJsonMessage(String requestCode, Message message) {

        String ret = "";

        JSONObject root = new JSONObject();
        try {

            root.put(ClientConst.REQUEST_CODE, requestCode);

            JSONObject jsonData = message.toJSONObject();
            root.put(MESSAGE_DATA, jsonData);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ret = root.toString();

        return ret;
    }

    /**
     * Get request code from a json message.
     * @param message json message
     * @return request code
     */
    public static String getMessageCode(String message) {

        String ret = "";

        try {
            JSONObject root = new JSONObject(message);
            ret = root.getString(ClientConst.REQUEST_CODE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;

    }

    public static Message getMessage(String json) {

        Message message = null;

        try {
            JSONObject root = new JSONObject(json);
            JSONObject data = root.getJSONObject(MESSAGE_DATA);
            message = new Message(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return message;

    }

}
