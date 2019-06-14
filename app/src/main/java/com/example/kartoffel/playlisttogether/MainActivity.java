package com.example.kartoffel.playlisttogether;



import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
                Intent serverbrowsing = new Intent(MainActivity.this, ServerbrowsingActivity.class);
                startActivity(serverbrowsing);
            }
        });

    }
}
