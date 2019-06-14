package com.example.kartoffel.playlisttogether;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.kartoffel.playlisttogether.local.client.Serverconnection;
import com.example.kartoffel.playlisttogether.local.server.Server;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    TextView inputView;
    Button addButton;

    ListView view_playlist;
    ArrayAdapter<String> adapter;

    ArrayList<String> playListContent = new ArrayList<String>();


    boolean isHost;

    //For when I am Host
    Server server;
    //For when I am Client < Tho the Host uses this too to connect to his localhost server
    Serverconnection serverconnection;

    private static final String CLIENT_ID = "63b7692b4ab646b9bfa075ff6990673d";
    private static final String REDIRECT_URI = "https://app.jamboozle.tk/callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        ImageView ljammer = findViewById(R.id.jammer_loading);
        Glide.with(this)
                .asGif()
                .load(R.drawable.jammer_standing)
                .into(ljammer);

        spotifyConnect();

    }

    private void spotifyConnect() {
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("Player", "Connected with Spotify! Yay!");

                        // Subscribe to PlayerState
                        mSpotifyAppRemote.getPlayerApi()
                                .subscribeToPlayerState()
                                .setEventCallback(playerState -> {
                                    final Track track = playerState.track;
                                    if (track != null) {
                                        Log.d("Player", track.name + " by " + track.artist.name);
                                        Log.d("Player", playerState.playbackPosition + "/" + track.duration);
                                    }
                                });
                        joinOrHost();
                        handleIntentContents();
                        //playSample();
                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("Player", throwable.getMessage(), throwable);
                        // Something went wrong when attempting to connect! Handle errors here
                        AlertDialog alertDialog = new AlertDialog.Builder(PlayerActivity.this).create();
                        alertDialog.setTitle("You need to have Spotify (Premium) on your Phone!");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Oh shit!",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        if (!getIntent().getBooleanExtra("HostRoom", false))
                                            joinServer();
                                    }
                                });
                        try {
                            alertDialog.show();
                        }catch (Exception e){
                            //Activity not running any more
                        }
                    }
                });

    }

    private void joinOrHost(){
        if (Server.getServer() == null && Intent.ACTION_SEND.equals(getIntent().getAction())) {
            AlertDialog alertDialog = new AlertDialog.Builder(PlayerActivity.this).create();
            alertDialog.setTitle("Create or connect to a Jamboozle before sharing links!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Makes sense.",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            return;
        }


        if (getIntent().getBooleanExtra("HostRoom", false)) {
            isHost = true;
            if (Server.getServer() == null) {
                hostServer();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            joinServer();

        } else {
            isHost = false;
            joinServer();
        }
    }



    private void handleIntentContents() {

        setContentView(R.layout.activity_hostplayer);
        inputView = (TextView) findViewById(R.id.inputText);
        addButton = (Button) findViewById(R.id.addSomethingButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serverconnection.sendTrack(inputView.getText().toString());
            }
        });
        view_playlist = (ListView) findViewById(R.id.playlist);
        adapter = new ArrayAdapter<String>(PlayerActivity.this, android.R.layout.simple_list_item_1, playListContent);
        view_playlist.setAdapter(adapter);


        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Log.e("Player", "INTENT TYPE " + type);
            if ("text/plain".equals(type)) {
                serverconnection = Serverconnection.getCurrentConnection(this);
                if (serverconnection != null) {
                    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                    if (sharedText != null) {
                        Log.d("Player", "Recieved ShareIntent: " + sharedText);
                        serverconnection.sendTrack(sharedText);
                    }
                }
            }
        }
    }

    private void hostServer() {
        Intent intent = getIntent();
        String roomname = intent.getStringExtra("Roomname");
        String password = intent.getStringExtra("Password");
        this.server = Server.hostServer(this, roomname, password);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        joinServer();
    }

    public void joinServer() {
        Intent intent = getIntent();
        String ipAdress;
        if (isHost) {
            ipAdress = Server.getServer().getIpAdress();
        } else {
            ipAdress = intent.getStringExtra("IpAdress");
        }
        serverconnection = new Serverconnection(this, ipAdress);
    }

    public void playSample() {
        // Play a playlist
        mSpotifyAppRemote.getPlayerApi().queue("spotify:track:13hQj3kA6hLlKR9fBK9wCq");
        mSpotifyAppRemote.getPlayerApi().resume();
    }


    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }


    public void dataRecieve(String read) {

    }

    public void addTrack(String spotifyTrackUrl) {

        Log.d("PlayerActivity", "Recieved Track in Player (that may be myself): " + spotifyTrackUrl);
        String song = URLInfo.spotifyInfo(spotifyTrackUrl);
        if(song == null)
            return;
        playListContent.add(song);
        if(playListContent.size()<1) {
            //NOT WORKING
            mSpotifyAppRemote.getPlayerApi().play("spotify:track:" + URLInfo.spotifyTrackURI(spotifyTrackUrl));
        }
        else{
            mSpotifyAppRemote.getPlayerApi().queue("spotify:track:" + URLInfo.spotifyTrackURI(spotifyTrackUrl));
            mSpotifyAppRemote.getPlayerApi().resume();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Stuff that updates the UI
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * After Going to Home Screen or other App Reconnect to Spotify must happen lol
     */

    @Override
    public void onResume(){
        super.onResume();
        spotifyConnect();

    }
}
