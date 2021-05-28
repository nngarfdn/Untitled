package com.udindev.untitled.testingarief.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.udindev.untitled.data.model.Hospital;
import com.udindev.untitled.data.model.ResponseLog;
import com.udindev.untitled.databinding.ActivitySendNotificationBinding;
import com.udindev.untitled.testingarief.notification.model.Notification;
import com.udindev.untitled.testingarief.notification.model.Sender;
import com.udindev.untitled.testingarief.notification.model.Token;
import com.udindev.untitled.testingarief.notification.response.MyResponse;
import com.udindev.untitled.testingarief.notification.rest.ApiConfig;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.udindev.untitled.testingarief.utils.Constants.DELAY;
import static com.udindev.untitled.testingarief.utils.DateUtils.getCurrentDate;
import static com.udindev.untitled.testingarief.utils.DateUtils.getCurrentTime;

// Struktur id: uid user + tanggal + jam
public class SendNotificationActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private ActivitySendNotificationBinding binding;
    private boolean isRespondedByYes = false;
    private int numberOfCalls = 0;
    private String responseLogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser patient = FirebaseAuth.getInstance().getCurrentUser();
        responseLogId = patient.getUid() + "-" + getCurrentDate() + "-" + getCurrentTime();

        monitorHospitalLastResponse(responseLogId);
        List<Hospital> hospitalList = getNearestHospitals(); // Nanti bisa pakai viewmodel + repository
        findHospital(hospitalList, responseLogId);
    }

    private void monitorHospitalLastResponse(String responseLogId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("responses")
                .child(responseLogId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                GenericTypeIndicator<HashMap<String, ResponseLog>> objectsGTypeInd = new GenericTypeIndicator<HashMap<String, ResponseLog>>() {};
                Map<String, ResponseLog> objectHashMap = snapshot.getValue(objectsGTypeInd);
                ArrayList<ResponseLog> responseLogList = new ArrayList<>(objectHashMap.values());
                String lastResponse = responseLogList.get(responseLogList.size()-1).getResponse();
                if (lastResponse.equals("yes")) isRespondedByYes = true;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private List<Hospital> getNearestHospitals() {
        List<Hospital> hospitalList = new ArrayList<>();
        hospitalList.add(new Hospital("0", "SDN 1 Sanden",  -7.894125,110.338075));
        hospitalList.add(new Hospital("1", "SMPN 2 Pandak",  -7.9402, 110.21917));
        hospitalList.add(new Hospital("2", "SMP TD 2  Dlingo", -7.93343, 110.42871));
        return hospitalList;
    }

    private void findHospital(List<Hospital> hospitalList, String responseLogId){
        while (!isRespondedByYes) {
            for (int i = 0; i < hospitalList.size(); i++){
                if (isRespondedByYes) break;
                numberOfCalls++;
                startCountdown(numberOfCalls);
                callHospital(hospitalList.get(i).getId(), responseLogId, numberOfCalls);
            }
        }
    }

    private void startCountdown(int numberOfCalls) {
        new CountDownTimer(DELAY, 1000) {
            @Override
            public void onTick(long l) {
                binding.tvCountdown.setText("Sedang menunggu respons dalam " + l/1000 + " detik");
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference("responses")
                        .child("idnya").child("log" + numberOfCalls)
                        .child("remainingWaitingTime");
                reference.setValue(l/1000);
            }

            @Override
            public void onFinish() {
                if (isRespondedByYes) {
                    showHospitalFoundDialog();
                }
            }
        }.start();
    }

    private void callHospital(String hospitalUid, String responseLogId, int numberOfCalls) {
        Log.d(TAG, "callHospital called");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("token");
        // Muat token si penerima berdasarkan id pengguna
        Query query = reference.orderByKey().equalTo(hospitalUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "callHospital onDataChange: success get receiver token (" + dataSnapshot.getChildrenCount() + ")");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);

                    Log.d(TAG, "callHospital: is token not null = " + (token != null));
                    if (token != null) {
                        String title = "NN BUTUH PERTOLONGAN!!!";
                        String message = "Buka notifikasi untuk berikan respon YA";
                        // Kirim juga data pasien (tapi ini belum)
                        Notification notification = new Notification(title, message, responseLogId, numberOfCalls);
                        Sender sender = new Sender(notification, token.getToken());

                        Call<MyResponse> client = ApiConfig.getApiService().sendNotification(sender);
                        client.enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(@NotNull Call<MyResponse> call, @NotNull Response<MyResponse> response) {
                                if (response.code() == 200 && response.body() != null) {
                                    if (response.body().success != 1) {
                                        Log.w(TAG, "callHospital onResponse: failed send notification");
                                    } else {
                                        Log.d(TAG, "callHospital onResponse: notification sent");
                                    }
                                } else {
                                    Log.w(TAG, "callHospital onResponse: error code " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(@NotNull Call<MyResponse> call, @NotNull Throwable t) {
                                Log.e(TAG, "callHospital onFailure", t);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "callHospital onCancelled", error.toException());
            }
        });
    }

    private void showHospitalFoundDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Rumah Sakit Segera Tiba")
                .setMessage("RS NN sedang menuju lokasimu")
                .setCancelable(false)
                .setPositiveButton("Ya", (dialogInterface, i) -> onBackPressed()).create().show();
    }
}