package com.be4em.auto90.object;

/**
 * Created by mamdouhelnakeeb on 5/8/17.
 */

public class ChatItem {

    public String userID;
    public String userName;
    public String msg;

    public ChatItem (String userID, String userName, String msg){
        this.userID = userID;
        this.userName = userName;
        this.msg = msg;
    }
}
