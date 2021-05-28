package com.udindev.untitled.testingarief.notification.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.udindev.untitled.R;
import com.udindev.untitled.testingarief.notification.model.Token;
import com.udindev.untitled.testingarief.notification.ui.NotificationActivity;

// Kelas ini menangani penerimaan notifikasi
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    public static final int REQUEST_MY_NOTIFICATION = 100;

    /* Kelas ini merupakan kelas service yang berjalan di latar belakang
    mendeteksi setiap ada notifikasi baru yang diterima*/
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        showNotification(remoteMessage);
    }

    private void showNotification(RemoteMessage remoteMessage) {
        String CHANNEL_ID = "channel_01";
        String CHANNEL_NAME = "Notifikasi baru";

        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");
        String responseLogId = remoteMessage.getData().get("responseLogId");
        int numberOfCalls = Integer.parseInt(remoteMessage.getData().get("numberOfCalls"));

        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra("extra_response_log_id", responseLogId);
        intent.putExtra("extra_number_of_calls", numberOfCalls);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_MY_NOTIFICATION, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setColor(ContextCompat.getColor(this, android.R.color.transparent))
                .setSound(defaultSoundUri)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            builder.setChannelId(CHANNEL_ID);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(notificationChannel);
        }

        Notification notification = builder.build();
        if (notificationManager != null) notificationManager.notify(REQUEST_MY_NOTIFICATION, notification);
    }

    @Override
    /* Menangani setiap ada perubahan token (identifier dari perangkat tertentu)
    Ada 2 skenario fungsi ini dipanggil:
    1) Ada token baru yang dibuat saat pertama kali launch
    2) Setiap token berubah
    Ada 3 skenario token dapat berubah:
    A) Perangkat baru
    B) Instal ulang
    C) Setelah membersihkan app data*/
    public void onNewToken(@NonNull String newToken) {
        super.onNewToken(newToken);
        Log.d(TAG, "New token: " + newToken);
        sendRegistrationToServer(newToken);
    }

    // Simpan dan perbarui token baru ke database server
    public static void sendRegistrationToServer(String newToken) {
        Log.d(TAG, "sendRegistrationToServer called: " + newToken);
        /* Token ini dikueri saat akan mengirimkan notifikasi
        sebagai penerima notifikasi*/
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("token");
            Token token = new Token(newToken);
            databaseReference.child(firebaseUser.getUid())
                    .setValue(token)
                    .addOnSuccessListener(unused -> Log.d(TAG, "sendRegistrationToServer onSuccess"))
                    .addOnFailureListener(e -> Log.e(TAG, "sendRegistrationToServer onFailure", e));
        } else Log.w(TAG, "sendRegistrationToServer cancelled: no user logged in");
    }
}
