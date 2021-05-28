package com.udindev.untitled

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.udindev.untitled.data.model.Hospital
import com.udindev.untitled.databinding.ActivityMainBinding
import com.udindev.untitled.testingarief.main.HospitalDashboardActivity
import com.udindev.untitled.testingarief.main.PatientDashboardActivity
import com.udindev.untitled.testingarief.notification.ui.NotificationActivity


class MainActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "NotificationActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var latLokasiku = -7.93861
        var longLokasiku = 110.25056

        var lok1 = Hospital("0", "SDN 1 Sanden",  -7.894125,110.338075)
        var lok2 = Hospital("1", "SMPN 2 Pandak",  -7.9402, 110.21917)
        var lok3 = Hospital("2", "SMP TD 2  Dlingo", -7.93343, 110.42871)

        val loc1 = Location("")
        loc1.latitude = latLokasiku
        loc1.longitude = longLokasiku

        val loc2 = Location("")
        loc2.latitude = lok1.latitude!!
        loc2.longitude = lok1.longitude!!

        var jarak = loc1.distanceTo(loc2)

        Log.d(TAG, "onCreate: jaraknya $jarak")

        binding.btnNotification.setOnClickListener { view ->
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        binding.btnHospitalDashboard.setOnClickListener { view ->
            val intent = Intent(this, HospitalDashboardActivity::class.java)
            startActivity(intent)
        }

        binding.btnPatientDashboard.setOnClickListener { view ->
            val intent = Intent(this, PatientDashboardActivity::class.java)
            startActivity(intent)
        }
    }
}