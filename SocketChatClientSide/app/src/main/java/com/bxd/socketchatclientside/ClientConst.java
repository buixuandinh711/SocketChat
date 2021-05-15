package com.bxd.socketchatclientside;

/**
 * Contain consts of the application.
 */
public interface ClientConst {

    public static final String SERVER_NAME = "$server";
    public static final String UNKNOWN_CLIENT_NAME = "$unknown";
    public static final String RESULT_SUCCESSFUL = "%successful";
    public static final String RESULT_FAILED = "%failed";
    public static final String CODE_LOGIN = "login";
    public static final String CODE_REGISTER = "register";
    public static final String CODE_SEARCH = "search";
    public static final String CODE_INIT_CONNECT = "init";
    public static final String CODE_MESSAGE = "message";
    public static final String CODE_LOGOUT = "logout";
    public static final String CODE_RECENT_MESSAGES = "old_messages";
    public static final int DEFAULT_PORT = 8888;
    public static final int TIME_OUT = 1000;

    public static final String REFERENCE_KEY = "sr_data_login";
    public static final String KEY_LOGIN = "is_login";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_ADDRESS = "ip_address";
    public static final String MESSAGE_DB_NAME = "message_db";
    public static final int MESSAGE_TYPE_SEND = 0;
    public static final int MESSAGE_TYPE_RECEIVE = 1;
    public static final String FRAGMENT_RECENT_CHAT = "frag_recent_chat";
    public static final String FRAGMENT_SEARCH = "frag_search";
    public static final String CLIENT_USERID_INTENT_DATA = "client_data";

    public static final String REQUEST_CODE = "code";
    public static final String SENDER_ID = "sender";
    public static final String RECEIVER_ID = "receiver";
    public static final String CONTENT = "content";
    public static final String SENT_TIME = "sent_time";
    public static final String MESSAGE_DATA = "data";

    public static final String REGEX_USERNAME_PASSWORD = "^[a-zA-Z]\\w{5,19}$";
    public static final String REGEX_IP_ADDRESS = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

}
