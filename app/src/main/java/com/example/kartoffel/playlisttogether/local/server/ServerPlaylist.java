package com.example.kartoffel.playlisttogether.local.server;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ServerPlaylist {

    Queue<ServerTrack> tracklist;

    public ServerPlaylist(){
        tracklist = new LinkedList<ServerTrack>();
    }

    public void addTrack(String trackname){
        tracklist.add(new ServerTrack(trackname));
    }

    public List<ServerTrack> getPlaylist(){
        return (List) this.tracklist;
    }

}
