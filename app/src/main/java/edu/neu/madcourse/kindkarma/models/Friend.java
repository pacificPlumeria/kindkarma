package edu.neu.madcourse.kindkarma.models;

public class Friend {
    private String idChatRoom;

    public Friend(String idChatRoom) {
        this.idChatRoom = idChatRoom;
    }

    public Friend() {
    }

    public String getIdChatRoom() {
        return idChatRoom;
    }

    public void setIdChatRoom(String idChatRoom) {
        this.idChatRoom = idChatRoom;
    }
}
