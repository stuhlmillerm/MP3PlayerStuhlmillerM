package com.csit551.mp3playerstuhlmillerm;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    MPHandler mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create our object to manage interaction with the player thread
        mp = new MPHandler(this);
    }

    // Handle the button clicks
    public void onClickPlay(View view) {
        mp.command("start");
    }
    public void onClickPause(View view) {
        mp.command("pause");
    }
    public void onClickResume(View view) {
        mp.command("resume");
    }
    public void onClickStop(View view) {
        mp.command("stop");
    }
    public void onClickRepeat(View view) {
        mp.command("repeat");
    }
    public void onClickStream(View view) {
        EditText urlView = (EditText) findViewById(R.id.streamText);
        mp.stream(urlView.getText().toString());
    }
}