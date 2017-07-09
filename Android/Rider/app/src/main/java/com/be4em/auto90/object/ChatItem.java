package com.be4em.auto90.object;

/**
 * Created by mamdouhelnakeeb on 5/8/17.
 */

public class ChatItem {

    public int msgID;
    public int userID;
    public String userName;
    public String msg;

    public ChatItem (int msgID, int userID, String userName, String msg){
        this.msgID = msgID;
        this.userID = userID;
        this.userName = userName;
        this.msg = msg;
    }
}
