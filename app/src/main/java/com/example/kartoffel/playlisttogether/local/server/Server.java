package com.example.kartoffel.playlisttogether.local.server;

import android.app.Activity;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import com.example.kartoffel.playlisttogether.Protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static android.content.Context.WIFI_SERVICE;

public class Server {


    private static final int BROADCAST_PORT = 8887;
    private static Server server = null;

    Activity activity_host;

    String roomname;
    String password;
    String ipAdress;

    ServerPlaylist playlist;

    ServerSocket serverSocket;
    BroadcastingInfoThread broadcaster;
    AcceptingClientsThread acceptor;
    ArrayList<Clientconnection> connectedClients = new ArrayList<Clientconnection>();

    public static Server hostServer(Activity creator, String roomname, String password) {
        if (server != null) {
            server.shutdown();
        }
        server = new Server(creator);
        server.roomname = roomname;
        server.password = password;
        server.playlist = new ServerPlaylist();
        return server;

    }

    public static Server getServer() {
        return server;
    }

    private Server(Activity creator) {
        this.activity_host = creator;
        try {
            serverSocket = new ServerSocket(8888);
        } catch (IOException e) {
            e.printStackTrace();
        }
        acceptor = new AcceptingClientsThread();
        broadcaster = new BroadcastingInfoThread();

        acceptor.start();
        broadcaster.start();
    }


    public void shutdown() {
        try {
            serverSocket.close();
            for (Clientconnection c : connectedClients) {
                c.shutdown();
            }
            connectedClients.clear();
            acceptor.interrupt();
            broadcaster.interrupt();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Pass msg from client to all connected clients including himself
    public void dataRecieve(String msg) {

        if (msg == null) {
            Log.e("Serverconnection", "Null Message recieved, doing nothing with it!");
            return;
        }

        if (msg.startsWith(Protocol.TRACK)) {
            playlist.addTrack(msg.substring(Protocol.TRACK.length()));
            sendToAllClients(msg);
            return;
        }
        Log.e("Serverconnection", "Unkown Command Recieved, doing nothing with it!");
    }

    private void sendToAllClients(String msg) {
        Log.d("Server", "Sending [" + msg + "] to all clients (incl myself)");
        for (Clientconnection client : connectedClients) {
            client.send(msg);
        }
    }


    public String getIpAdress() {
        return this.ipAdress;
    }

    private String getOwnIpAdress() {
        WifiManager wifiMgr = (WifiManager) activity_host.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String ipAddress = Formatter.formatIpAddress(ip);
        this.ipAdress = ipAddress;
        return ipAddress;
    }

    private InetAddress getBroadcastAddress() {
        /**
         try {
         InetAddress addr = InetAddress.getByName("255.255.255.255");
         return addr;
         } catch (UnknownHostException e) {
         e.printStackTrace();
         }
         return null;
         */

        WifiManager wifi = (WifiManager) activity_host.getApplicationContext().getSystemService(WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();

        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) (broadcast >> (k * 8));
        try {
            return InetAddress.getByAddress(quads);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;

    }

    class BroadcastingInfoThread extends Thread {


        //Example Broadcast String: "Jamboozle at: |146.176.75.75|Nexus 5X|5"
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String roomInfo = "Jamboozle at: |" + getOwnIpAdress() + "|" + roomname + "|" + connectedClients.size();
                    InetAddress broadcast = getBroadcastAddress();
                    DatagramPacket packet = new DatagramPacket(roomInfo.getBytes(), roomInfo.getBytes().length, broadcast,
                            BROADCAST_PORT);
                    DatagramSocket socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    Log.e("Broadcast Thread", "Broadcasting Info on " + String.valueOf(broadcast) + ":" + BROADCAST_PORT + "  >>  Content = " + roomInfo);
                    socket.send(packet);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

            }
        }
    }

    class AcceptingClientsThread extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    Log.d("Server", "Client connected " + clientSocket.getInetAddress());
                    Clientconnection newClient = new Clientconnection(server, clientSocket);
                    newClient.sendPlaylist(playlist);
                    for (Clientconnection c : connectedClients) {
                        if (c.clientSocket.getInetAddress().equals(newClient.clientSocket.getInetAddress())) {
                            Log.d("Server", "Client already connected to Server. Quitting old connection and Replacing.");
                            connectedClients.remove(c);
                            c.shutdown();
                            break;
                        }
                    }
                    connectedClients.add(newClient);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
