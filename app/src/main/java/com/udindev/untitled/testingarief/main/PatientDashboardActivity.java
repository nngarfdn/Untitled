package com.udindev.untitled.testingarief.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.udindev.untitled.databinding.ActivityPatientDashboardBinding;

public class PatientDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPatientDashboardBinding binding = ActivityPatientDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSend.setOnClickListener(view -> {
            Intent intent = new Intent(PatientDashboardActivity.this, SendNotificationActivity.class);
            startActivity(intent);
        });
    }
}