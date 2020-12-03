package kr.ac.gachon.sw.closeheart.server.object;

import java.util.Calendar;

public class Chat {
    int chatType;
    User chatOwner;
    String chatMsg;
    Calendar chatTime;

    public Chat(int chatType, User chatOwner, String chatMsg, Calendar chatTime) {
        this.chatType = chatType;
        this.chatOwner = chatOwner;
        this.chatMsg = chatMsg;
        this.chatTime = chatTime;
    }

    public User getChatOwner() {
        return chatOwner;
    }

    public String getChatMsg() {
        return chatMsg;
    }

    public Calendar getChatTime() {
        return chatTime;
    }
}
