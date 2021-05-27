package com.example.clipboardmanager;

import java.util.Date;

public class Note {
    protected String text;
    protected boolean star;
    protected Date date;

    public Note(String text, boolean star, Date date) {
        this.text = text;
        this.star = star;
        this.date = date;
    }

    public Note setStar(boolean star) {
        this.star = star;
        return this;
    }

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }

    public boolean getStar() {
        return star;
    }
}
