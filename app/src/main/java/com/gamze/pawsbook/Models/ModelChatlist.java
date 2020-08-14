package com.gamze.pawsbook.Models;

public class ModelChatlist {
    String id; //bu id chatList getirmek için kullanılacak, sender/receiver uid

    public ModelChatlist(String id) {
        this.id = id;
    }

    public ModelChatlist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
