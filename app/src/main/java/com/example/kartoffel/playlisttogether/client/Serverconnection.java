package com.example.kartoffel.playlisttogether.client;

import android.app.Activity;
import android.util.Log;

import com.example.kartoffel.playlisttogether.PlayerActivity;
import com.example.kartoffel.playlisttogether.Protocol;
import com.example.kartoffel.playlisttogether.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.PriorityQueue;
import java.util.Queue;

public class Serverconnection {

    private String ipAddr = "noIpAdress";
    private static final int PORT = 8888;

    PlayerActivity player;

    Socket serversocket;
    ListeningThread listener;
    ConnectionThread connection;

    private Queue<String> q = new PriorityQueue<String>();
    private Object lock = new Object();

    static Serverconnection currentConnection;

    public Serverconnection(PlayerActivity player, String ipAddr) {
        this.player = player;
        this.ipAddr = ipAddr;
        connection = new ConnectionThread();
        connection.start();

        currentConnection = this;
    }

    public static Serverconnection getCurrentConnection(PlayerActivity player){
        if(currentConnection == null){
            return null;
        }
        else {
            currentConnection.player = player;
            return currentConnection;
        }

    }

    public void sendTrack(String msg) {
        msg = "@TRACK@" + msg;
        addToSendingQueue(msg);
    }

    private void addToSendingQueue(String msg){
        synchronized (lock) {
            q.add(msg);
            lock.notify();
        }
    }

    //No Networks on Main Thread allowed. causes NetworkOnMainThreadException
    //Waiting thread, notify when new message to server is set in Main thread. This thread then sends it to the server
    class ConnectionThread extends Thread {

        @Override
        public void run() {
            try {
                serversocket = new Socket(ipAddr, PORT);
                listener = new ListeningThread();
                listener.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

            PrintWriter out = null;
            try {
                out = new PrintWriter(serversocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    synchronized (lock) {
                        while (q.peek() == null) {
                            lock.wait();
                        }
                        String msg = q.remove();
                        Log.d("Serverconnection", "Sending ["+msg+"] to the Server");
                        out.println(msg);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    class ListeningThread extends Thread {
        //Listen for messages from server

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(serversocket.getInputStream()));
                while (!Thread.currentThread().isInterrupted()) {
                    if(in.ready()) {
                        String msg = in.readLine();
                        Log.d("Serverconnection", "Message from server: " + msg);

                        if (msg == null) {
                            Log.e("Serverconnection", "Null Message from Server recieved!");
                            continue;
                        }

                        if (msg.startsWith(Protocol.TRACK)) {
                            player.addTrack(msg.substring(Protocol.TRACK.length()));
                            continue;
                        }

                        Log.e("Serverconnection", "Unkown Command. Doing Nothing...");
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }


}
