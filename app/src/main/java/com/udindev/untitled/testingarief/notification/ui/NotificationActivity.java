package com.udindev.untitled.testingarief.notification.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.udindev.untitled.R;
import com.udindev.untitled.databinding.ActivityNotificationBinding;
import com.udindev.untitled.testingarief.notification.model.Notification;
import com.udindev.untitled.testingarief.notification.model.Sender;
import com.udindev.untitled.testingarief.notification.model.Token;
import com.udindev.untitled.testingarief.notification.response.MyResponse;
import com.udindev.untitled.testingarief.notification.rest.ApiConfig;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.udindev.untitled.testingarief.notification.service.MyFirebaseMessagingService.sendRegistrationToServer;

// Dokumentasi: https://firebase.google.com/docs/cloud-messaging/http-server-ref
public class NotificationActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private static final String DUMMY_UID = "sJklpBYLwUaZd8E2Rr6mNYlI8uu2";

    private ActivityNotificationBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) launchLogin();

        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Dapatkan token untuk notifikasi
        getToken();

        binding.edtUid.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable editable) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.btnSend.setEnabled(!charSequence.toString().isEmpty());
            }
        });

        binding.btnSend.setEnabled(false);
        binding.btnSend.setOnClickListener(view -> {
            String receiverId = binding.edtUid.getText().toString();
            String notificationTitle = "Halo";
            String notificationMessage = "Anda mendapatkan notif baru";
            sendNotification(receiverId, notificationTitle, notificationMessage);
        });

        binding.btnLogout.setOnClickListener(view -> logout());

        binding.edtUid.setText(DUMMY_UID);
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                sendRegistrationToServer(token);
            } else {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
            }
        });
    }

    private void sendNotification(String receiverUid, final String title, final String message) {
        Log.d(TAG, "sendNotification called");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("token");
        // Muat token si penerima berdasarkan id pengguna
        Query query = reference.orderByKey().equalTo(receiverUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "sendNotification onDataChange: success get receiver token (" + dataSnapshot.getChildrenCount() + ")");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);

                    Log.d(TAG, "sendNotification: is token not null = " + (token != null));
                    if (token != null){
                        Notification notification = new Notification(title, message);
                        Sender sender = new Sender(notification, token.getToken());

                        Call<MyResponse> client = ApiConfig.getApiService().sendNotification(sender);
                        client.enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(@NotNull Call<MyResponse> call, @NotNull Response<MyResponse> response) {
                                if (response.code() == 200 && response.body() != null) {
                                    if (response.body().success != 1) {
                                        Log.w(TAG, "sendNotification onResponse: failed send notification");
                                    } else {
                                        Log.d(TAG, "sendNotification onResponse: notification sent");
                                    }
                                } else {
                                    Log.w(TAG, "sendNotification onResponse: error code " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(@NotNull Call<MyResponse> call, @NotNull Throwable t) {
                                Log.e(TAG, "sendNotification onFailure", t);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "sendNotification onCancelled", error.toException());
            }
        });
    }

    private void logout() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInClient.signOut();
        firebaseAuth.signOut();

        launchLogin();
    }

    private void launchLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}