package com.udindev.untitled.testingarief.notification.model;

// Membawa isi notifikasi dan token si penerima
public class Sender {

    // Jangan ubah nama kolom agar bisa diurai sebagai JSON
    public final Notification data;
    public final String to; // receiver token

    public Sender(Notification data, String to) {
        this.data = data;
        this.to = to;
    }
}
