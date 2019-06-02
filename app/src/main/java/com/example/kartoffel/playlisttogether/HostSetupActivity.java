package com.example.kartoffel.playlisttogether;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HostSetupActivity extends AppCompatActivity {

    Button createButton;
    TextView serverNameView;
    TextView roomPasswordView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hostsetup);

        createButton        = (Button) findViewById(R.id.createRoom);
        serverNameView      = (TextView) findViewById(R.id.roomName);

        serverNameView.setText(android.os.Build.MODEL);

        roomPasswordView    = (TextView) findViewById(R.id.password);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomname = serverNameView.getText().toString();
                if(roomname.length() < 1){
                    AlertDialog alertDialog = new AlertDialog.Builder(HostSetupActivity.this).create();
                    alertDialog.setTitle("Insert a room name");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok sorry",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    return;
                }
                String password = roomPasswordView.getText().toString();

                Intent hostServer = new Intent(HostSetupActivity.this, PlayerActivity.class);
                hostServer.putExtra("Roomname", roomname);
                hostServer.putExtra("Password", password);
                hostServer.putExtra("HostRoom", true);
                startActivity(hostServer);
            }
        });




    }
}
