package com.example.kartoffel.playlisttogether.local.client;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.kartoffel.playlisttogether.ClientServerbrowsingActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class Serverfinder extends Thread {

    public static final int BROADCAST_PORT = 8887;


    private static Serverfinder serverfinder;
    private SearchAnimationThread anim;

    ArrayList<String> servernames = new ArrayList<String>();
    ArrayList<String> connectedUsers = new ArrayList<String>();
    ArrayList<String> ipAdresses = new ArrayList<String>();

    public static Serverfinder getServerfinder(ClientServerbrowsingActivity act) {
        if (serverfinder != null) {
            serverfinder.interrupt();
        }
        serverfinder = new Serverfinder(act);
        return serverfinder;
    }


    private DatagramSocket socket = null;
    private ClientServerbrowsingActivity act;

    public Serverfinder(ClientServerbrowsingActivity act) {
        this.act = act;
        servernames.clear();
        connectedUsers.clear();
        ipAdresses.clear();
        if(anim != null)
            anim.interrupt();
        anim = new SearchAnimationThread();
        act.updateServerlist(servernames, connectedUsers, ipAdresses);
    }

    @Override
    public void interrupt() {
        Log.d("Serverfinder", "Interrupting Serverfinder, Closing socket");
        super.interrupt();
        if(anim != null)
            anim.interrupt();
        if(this.socket != null)
            this.socket.close();
    }

    @Override
    public void run() {
        checkWifiConnection();

        servernames.add("Create New Room");
        connectedUsers.add("Click here to host a fresh Jamboozle!");
        ipAdresses.add("Create New Room");

        servernames.add("Searching...");
        connectedUsers.add("Looking for Jamboozles to join!");
        ipAdresses.add("null");
        anim.start();

        byte[] infoBytes = new byte[100];
        try {
            socket = new DatagramSocket(BROADCAST_PORT);
            DatagramPacket packet = new DatagramPacket(infoBytes, infoBytes.length);
            Log.e("Serverfinder", "STARTED");
            while (!Thread.currentThread().isInterrupted()) {
                act.updateServerlist(servernames, connectedUsers, ipAdresses);

                Log.e("Serverfinder", "Waiting");
                socket.receive(packet);

                String msg = new String(infoBytes, 0, packet.getLength());
                Log.e("Serverfinder", "Recieved: " + msg);

                String[] serverinfo = msg.split("\\|");
                String ipAdress = serverinfo[1];
                String roomname = serverinfo[2];

                //Updating variables of listed rooms here:
                if (ipAdresses.contains(ipAdress) && servernames.contains(roomname)) {
                    Log.d("Serverfinder", "Recieved Room already listed, updating usercount!");
                    int index = ipAdresses.indexOf(ipAdress);

                    String usercount = serverinfo[3] + " active Jammers";
                    connectedUsers.remove(index);
                    connectedUsers.add(index, usercount);
                    continue;
                }


                //Adding new Rooms here:
                String usercount = serverinfo[3] + " active Jammers";
                Log.d("Serverfinder", "New Room found " + ipAdress + " " + roomname + " " + usercount);

                //Add them at Index so that "Searching" item will stay last in list
                servernames.add(servernames.size() - 1, roomname);
                connectedUsers.add(connectedUsers.size() - 1, usercount);
                ipAdresses.add(ipAdresses.size() - 1, ipAdress);

                act.updateServerlist(servernames, connectedUsers, ipAdresses);
            }
            Log.e("Serverfinder", "ENDED");
            anim.interrupt();

        } catch (SocketException se) {
            se.printStackTrace();
            anim.interrupt();
            Log.e("Serverfinder", "ENDED");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            anim.interrupt();
            Log.e("Serverfinder", "ENDED");
        }

    }

    private void checkWifiConnection() {
        ConnectivityManager connManager = (ConnectivityManager) act.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {
            Log.e("Wifi Status", "Wifi not connected!");
            //Throw error - only local network currently supported
        } else {
            Log.e("Wifi Status", "Wifi network detected!");
        }
    }

    class SearchAnimationThread extends Thread {
        @Override
        public void run() {
            Log.d("Serverfinder","Thread started (Animation)");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    servernames.remove(servernames.size() - 1);
                    servernames.add("Searching.");
                    act.updateServerlist(servernames, connectedUsers, ipAdresses);
                    Thread.sleep(1000);
                    servernames.remove(servernames.size() - 1);
                    servernames.add("Searching..");
                    act.updateServerlist(servernames, connectedUsers, ipAdresses);
                    Thread.sleep(1000);
                    servernames.remove(servernames.size() - 1);
                    servernames.add("Searching...");
                    act.updateServerlist(servernames, connectedUsers, ipAdresses);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                   Log.d("Serverfinder", "Thread interrupted (Animation)");
                   return;
                }
            }

        }

    }
}



