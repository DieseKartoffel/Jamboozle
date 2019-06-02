package com.example.kartoffel.playlisttogether.server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
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
