package ideanity.oceans.antitheftapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class notify extends AppCompatActivity {

    Button notifybtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        notifybtn = findViewById(R.id.notify_btn);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "My Notification",
                    "My Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

            notifybtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(notify.this, "My Notification");
                    builder.setContentTitle("My Title");
                    builder.setContentText("Hello from Easy tuto, this is a simple notification");
                    builder.setSmallIcon(R.drawable.ic_launcher_background);
                    builder.setAutoCancel(true);

                    NotificationManagerCompat managerCompat = NotificationManagerCompat.from(notify.this);
                    managerCompat.notify(1,builder.build());
                }

            });
        }
    }
}

