package com.example.kartoffel.playlisttogether.server;

public class ServerTrack {

    String url;
    String name;
    String length;

    public ServerTrack(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

}
