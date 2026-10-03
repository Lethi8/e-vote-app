package com.example.vota.model;

public class CardItem {
    public final String badge, title, body, action;
    public final Runnable onClick;

    public CardItem(String badge, String title, String body, String action, Runnable onClick) {
        this.badge = badge;
        this.title = title;
        this.body = body;
        this.action = action;
        this.onClick = onClick;
    }
}