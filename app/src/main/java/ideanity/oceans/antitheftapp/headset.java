package ideanity.oceans.antitheftapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class headset extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver;
    boolean Microphone_Plugged_in = false;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_headset);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                int state;
                if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                    state = intent.getIntExtra("state", -1);
                    if (state == 0) {
                        Microphone_Plugged_in = true;
                        Toast.makeText(getApplicationContext(), "Headset unplugged", Toast.LENGTH_LONG).show();
                        if (mediaPlayer == null) {
                            mediaPlayer = MediaPlayer.create(headset.this, R.raw.beepbeep);
                            mediaPlayer.setLooping(true);
                            mediaPlayer.start();
                        }
                        // Finish the activity when the headset is unplugged
                        finish();
                    }
                    if (state == 1) {
                        Microphone_Plugged_in = false;
                        Toast.makeText(getApplicationContext(), "Headset plugged in", Toast.LENGTH_LONG).show();
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                    }
                }
            }
        };

        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(broadcastReceiver, receiverFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Finish the activity when the user presses the back button
        finish();
    }
}
