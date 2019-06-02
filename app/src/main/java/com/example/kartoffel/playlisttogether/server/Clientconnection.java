package com.example.kartoffel.playlisttogether.server;

import android.util.Log;

import com.example.kartoffel.playlisttogether.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.PriorityQueue;
import java.util.Queue;

public class Clientconnection {

    Server server;
    Socket clientSocket;

    ListeningThread listener;
    SendingThread sender;
    PrintWriter out = null;

    Queue<String> sendingQueue;


    public Clientconnection(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendingQueue = new PriorityQueue<String>();

        sender = new SendingThread();
        sender.start();

        listener = new ListeningThread();
        listener.start();

    }

    public void send(String msg) {
        sendingQueue.add(msg);
    }

    public void sendPlaylist(ServerPlaylist pl){
        for(ServerTrack t : pl.getPlaylist()){
            this.send(Protocol.TRACK + t.getName());
        }

    }

    public void shutdown() {
        try {
            clientSocket.close();
            listener.interrupt();
            sender.interrupt();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    class ListeningThread extends Thread {
        private BufferedReader input;

        public ListeningThread() {
            try {
                this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    Log.d("Clientconnection","Server received message from a client ["+clientSocket.getInetAddress()+"]: "+read);
                    server.dataRecieve(read);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class SendingThread extends Thread{
        @Override
        public void run(){
            while(!Thread.currentThread().isInterrupted()){
                while(!sendingQueue.isEmpty()) {
                    String msg = sendingQueue.remove();
                    Log.d("Clientconnection", "Sending msg ["+msg+"] to client");
                    out.println(msg);
                }
            }
        }


    }


}
