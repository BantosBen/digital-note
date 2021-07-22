package com.sanj.digote.models;

public class NoteModel {

    private String id, title, note, date, color;

    public NoteModel() {
    }

    public NoteModel(String id, String title, String note, String date, String color) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.date = date;
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public String getDate() {
        return date;
    }
}
