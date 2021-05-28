package com.udindev.untitled.testingarief.main;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.udindev.untitled.databinding.ActivityHospitalDashboardBinding;

public class HospitalDashboardActivity extends AppCompatActivity {

    private ActivityHospitalDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHospitalDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}