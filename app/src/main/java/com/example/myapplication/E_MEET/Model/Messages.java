package com.example.myapplication.E_MEET.Model;

public class Messages {

    public String date, time, from, message, type;

    public  Messages()
    {

    }


    public Messages(String date, String time, String from, String message, String type) {
        this.date = date;
        this.time = time;
        this.from = from;
        this.message = message;
        this.type = type;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
