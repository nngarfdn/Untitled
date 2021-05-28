package com.udindev.untitled.testingarief.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.udindev.untitled.databinding.ActivityReceiveNotificationBinding;

import org.jetbrains.annotations.NotNull;

public class ReceiveNotificationActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private ActivityReceiveNotificationBinding binding;
    private boolean isRespondedByYes = false;
    private String responseLogId;
    private int numberOfCalls = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiveNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        responseLogId = getIntent().getStringExtra("extra_response_log_id");
        numberOfCalls = getIntent().getIntExtra("extra_number_of_calls", 0);

        monitorRemainingWaitingTime(responseLogId, numberOfCalls);
        binding.btnApprove.setOnClickListener(view -> {
            isRespondedByYes = true;
            sendFeedbackToPatient(responseLogId, true);
            showPatientData();
        });
    }

    private void monitorRemainingWaitingTime(String responseLogId, int numberOfCalls) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("responses").child(responseLogId)
                .child("log" + numberOfCalls).child("remainingWaitingTime");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                int remainingWaitingTime = snapshot.getValue(Integer.class);
                binding.tvCountdown.setText("Segera berikan respons dalam " + remainingWaitingTime + " detik");
                if (!isRespondedByYes && remainingWaitingTime == 0) {
                    sendFeedbackToPatient(responseLogId, false);
                    showWaitingTimeExpiredDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void sendFeedbackToPatient(String responseLogId, boolean isRespondedByYes) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("responses")
                .child(responseLogId).child("log" + numberOfCalls).child("response");

        if (isRespondedByYes) reference.setValue("yes");
        else reference.setValue("no response");
    }

    private void showWaitingTimeExpiredDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Waktu tunggu respon telah habis")
                .setMessage("Kamu akan dipanggil kembali jika pasien masih belum mendapatkan rumah sakit")
                .setCancelable(false)
                .setPositiveButton("Ya", (dialogInterface, i) -> onBackPressed()).create().show();
    }

    private void showPatientData() {
        Toast.makeText(this, "Data pasien ditampilkan", Toast.LENGTH_SHORT).show();
    }
}