package com.udindev.untitled.testingarief.notification.rest;

import com.udindev.untitled.testingarief.notification.model.Sender;
import com.udindev.untitled.testingarief.notification.response.MyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import static com.udindev.untitled.BuildConfig.SERVER_KEY;

public interface ApiService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=" + SERVER_KEY
            }
    )

    @POST("fcm/send")
    // Fungsi ini dipanggil ketika akan mengirim notifikasi (isi notifikasi dan token dibungkus dalam kelas Sender)
    Call<MyResponse> sendNotification(@Body Sender body);
}