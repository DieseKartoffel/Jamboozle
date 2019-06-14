package com.example.kartoffel.playlisttogether;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ServerbrowsingActivity extends AppCompatActivity {

    ServerbrowsingActivity myself = this; //reference for method calls in APIcommunication

    ArrayList<String> servernames;
    ArrayList<String> connectedUsers;
    ArrayList<String> ipAdresses;

    ListView serverListView;
    ListitemAdapter adapter;

    SearchAnimationThread anim;

    @Override
    public void onResume(){
        APIcommunication.updateServers(myself); //will call updateServerList in this class
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serverbrowsing);

       servernames = new ArrayList<String>();
       connectedUsers = new ArrayList<String>();
       ipAdresses = new ArrayList<String>();

        //List Item
        serverListView = (ListView) findViewById(R.id.serverList);
        adapter = new ListitemAdapter(this);
        serverListView.setAdapter(adapter);
        serverListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Click on a Server
                Log.d("Item Click","Clicked Pos: " + position + " ("+servernames.get(position)+") ID: " + id);
                if(position == 0){
                    Intent hostSetup = new Intent(ServerbrowsingActivity.this, HostSetupActivity.class);
                    startActivity(hostSetup);
                    return;
                }
                if(position == servernames.size() - 1){
                    APIcommunication.updateServers(myself); //will call updateServerList in this class
                    return;
                }
                else{
                    // serverfinder.interrupt();

                    //Room Activity that will then connect to server - Same activity as HostServer but Hostflag set to false
                    Intent joinServer = new Intent(ServerbrowsingActivity.this, PlayerActivity.class);
                    joinServer.putExtra("IpAdress", ipAdresses.get(position));
                    joinServer.putExtra("HostRoom", false);
                    startActivity(joinServer);

                }

            }
        });

        servernames.add("Create New Room");
        connectedUsers.add("Click here to host a fresh Jamboozle!");
        ipAdresses.add("Create New Room");

        servernames.add("Searching...");
        connectedUsers.add("Looking for Jamboozles to join!");
        ipAdresses.add("null");
        anim = new SearchAnimationThread();
        anim.start();


        //Listeing for Broadcasting Servers Thread, add them to list
        Log.e("Browser", "Looking for Servers");
    }

    public void updateServerlist(ArrayList<String> servernames, ArrayList<String> connectedUsers, ArrayList<String> ipAdresses) {

        this.servernames = servernames;
        this.connectedUsers = connectedUsers;
        this.ipAdresses = ipAdresses;

        //fix Exception: "Only the original thread that created a view hierarchy can touch its views."
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });

    }


    //Adapter for found rooms
    class ListitemAdapter extends BaseAdapter {

        LayoutInflater mInflater;

        public ListitemAdapter(Context c) {
            mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return servernames.size();
        }

        @Override
        public Object getItem(int position) {
            return servernames.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //Inflate serverbrowser_item and create view with it
            View view = mInflater.inflate(R.layout.serverbrowser_item, null);
            TextView servernameView = (TextView) view.findViewById(R.id.browser_servername);
            TextView connectedUsersView = (TextView) view.findViewById(R.id.browser_connectedusers);

            servernameView.setText(servernames.get(position));
            connectedUsersView.setText(connectedUsers.get(position));

            return view;
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
                    updateServerlist(servernames, connectedUsers, ipAdresses);
                    Thread.sleep(1000);
                    servernames.remove(servernames.size() - 1);
                    servernames.add("Searching..");
                    updateServerlist(servernames, connectedUsers, ipAdresses);
                    Thread.sleep(1000);
                    servernames.remove(servernames.size() - 1);
                    servernames.add("Searching...");
                    updateServerlist(servernames, connectedUsers, ipAdresses);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.d("Serverfinder", "Thread interrupted (Animation)");
                    return;
                }
            }

        }
    }
}
