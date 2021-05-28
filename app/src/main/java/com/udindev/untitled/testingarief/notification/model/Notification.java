package com.udindev.untitled.testingarief.notification.model;

// Representasi dari isi notifikasi
public class Notification {

    final String title;
    final String message;
    final String responseLogId;
    final int numberOfCalls;

    public Notification(String title, String message, String responseLogId, int numberOfCalls) {
        this.title = title;
        this.message = message;
        this.responseLogId = responseLogId;
        this.numberOfCalls = numberOfCalls;
    }
}
