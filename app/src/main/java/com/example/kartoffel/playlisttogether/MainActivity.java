package com.example.kartoffel.playlisttogether;



import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private Button jamboozle;
    private ImageView jammer;

    private Button enableButton;
    private TextView premiumInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        jammer = (ImageView)findViewById(R.id.jammer);
        jamboozle = (Button)findViewById(R.id.start);
        jamboozle.requestFocus();

        Glide.with(this)
                .asGif()
                .load(R.drawable.jammer_headphones)
                .into(jammer);

        jamboozle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serverbrowsing = new Intent(MainActivity.this, ClientServerbrowsingActivity.class);
                startActivity(serverbrowsing);
            }
        });

        premiumInput = (TextView) findViewById(R.id.premiumPassword);
        enableButton = (Button) findViewById(R.id.premium_enable);

        enableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(premiumInput.getText().length() < 1){
                    return;
                }

                Protocol.setPremiumKey(premiumInput.getText().toString());

                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Premium Key Configured!");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok cool",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                //clear text and dismiss keyboard
                                premiumInput.setText("");
                                premiumInput.clearFocus();
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(premiumInput.getWindowToken(), 0);

                            }
                        });
                alertDialog.show();
            }
        });


    }
}
