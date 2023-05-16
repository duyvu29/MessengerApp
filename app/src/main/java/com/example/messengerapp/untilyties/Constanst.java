package com.example.messengerapp.untilyties;

import java.util.HashMap;

public class Constanst {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_MAIL = "mail";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";

    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVE_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";

    public static final String KEY_COLLECTION_CONVERSTATION ="converstations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receivverName";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";

    public static final String KEY_AVAILABILITY = "availability";

    public static final String REMOTE_MSG_AUTHORIZATION = "authorization";
    public static final String REMOTE_MSG_CONTENT_TYP = "content_Type";

    public static final  String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRTION_IDS ="registration_ids";

    public static HashMap < String, String> remoteMsgHeader = null;
    public static HashMap<String,String> getRemoteMsgHeader(){
        if (remoteMsgHeader == null){
            remoteMsgHeader = new HashMap<>();
            remoteMsgHeader.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key =AAAAMR4_XxY:APA91bGAQeSW7wsCW3LWbM7KmE5YQ7gWaeN1sxqAGfPz4syg79-_ePptSQojVULiuBbFA6RmjlHH5FPOTbm5yNXr04T_kk8VcARK2yNel5bBgxpuUru28HHoDcTOs7jBklwr1LYPD8cV "

            );
            remoteMsgHeader.put(
                    REMOTE_MSG_CONTENT_TYP,
                    "application/json"
            );
        }
        return remoteMsgHeader;
    }

}
