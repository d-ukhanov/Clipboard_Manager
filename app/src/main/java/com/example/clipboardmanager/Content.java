package com.example.clipboardmanager;

import android.annotation.SuppressLint;
import android.support.annotation.RequiresApi;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ClipboardManager;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static java.lang.Math.min;

public class Content {
    @SuppressLint("StaticFieldLeak")
    private static Content sample = null;
    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private Context context;
    private ClipboardManager CBManager;
    private List<Note> notes;
    private boolean isChange = true;
    public final static String UPDATE_DB = "updateDB";
    public final static String UPDATE_DB_ADD = "updateDbAdd";
    public final static String UPDATE_DB_DELETE = "updateDbDelete";
    private static final String nameDB = "clipboardmanager";
    private static final String nameTable = "noteStorage";
    private static final String textNotes = "content";
    private static final String dateNotes = "date";
    private static final String starNotes = "star";

    private Content(Context context) {
        this.context = context;
        this.dbHelper = new DBHelper(this.context);
        this.CBManager = (ClipboardManager) this.context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public static Content newSample(Context context) {
        if (sample == null)
            sample = new Content(context.getApplicationContext());
        return sample;
    }

    private void openDB() {
        if (database == null || !database.isOpen())
            database = dbHelper.getWritableDatabase();
    }

    private void closeDB() {
        if (database != null && database.isOpen())
            database.close();
    }

    public List<Note> getNotes(int count) {
        List<Note> allNotes = getNotes();
        List<Note> searchNotes = new ArrayList<>();
        count = min(count, allNotes.size());
        for (int i = 0; i < count; i++)
            searchNotes.add(allNotes.get(i));
        return searchNotes;
    }

    public List<Note> getNotes(String textSearch) {
        List<Note> allNotes = getNotes();
        List<Note> searchNotes = new ArrayList<>();
        if ("".equals(textSearch) || (textSearch == null))
            return allNotes;
        for (Note note : allNotes) {
            if (note.getText().contains(textSearch))
                searchNotes.add(note);
        }
        return searchNotes;
    }

    public List<Note> getNotes() {
        if (isChange) {
            openDB();
            String sortDate = dateNotes + " DESC";
            String[] columns = {textNotes, dateNotes, starNotes};
            Cursor cur = database.query(nameTable, columns, null, null, null, null, sortDate);
            notes = new ArrayList<>();
            while (cur.moveToNext())
                notes.add(new Note(cur.getString(0), cur.getInt(2) > 0, new Date(cur.getLong(1))));
            cur.close();
            isChange = false;
        }
        return notes;
    }

    public void deleteNote(String text) {
        database.delete(nameTable, textNotes + "=" + DatabaseUtils.sqlEscapeString(text), null);
        refreshNoteList(true, null);
    }

    public void deleteNotes() {
        isChange = true;
        database.delete(nameTable, starNotes + "='" + 0 + "'", null);
        refreshNoteList(true, null);
    }

    private void addNote(Note note) {
        deleteNote(note.getText());
        long timestamp = note.getDate().getTime();
        ContentValues cont = new ContentValues();
        cont.put(textNotes, note.getText());
        cont.put(starNotes, note.getStar());
        cont.put(dateNotes, timestamp);
        database.insert(nameTable, null, cont);
    }

    public void editNote(String pastNote, String newNote, int isStarNote) {
        if (pastNote == null)
            pastNote = "";
        if (newNote == null)
            newNote = "";
        boolean isStarMod = isStar(pastNote);
        if (isStarNote == 1)
            isStarMod = true;
        if (isStarNote == -1)
            isStarMod = false;

        openDB();
        if (!pastNote.isEmpty())
            deleteNote(pastNote);
        if (!newNote.isEmpty())
            addNote(new Note(newNote, isStarMod, new Date()));
        closeDB();
        isChange = true;
        refreshNoteList(!newNote.isEmpty(), pastNote);
    }

    public List<Note> getStarNotes() {
        List<Note> allNotes = getNotes();
        List<Note> searchNotes = new ArrayList<>();
        for (Note note : allNotes) {
            if (note.getStar())
                searchNotes.add(note);
        }
        return searchNotes;
    }

    public List<Note> getStarNotes(String textSearch) {
        List<Note> allStarNotes = getStarNotes();
        List<Note> searchNotes = new ArrayList<>();
        if ("".equals(textSearch) || (textSearch == null))
            return allStarNotes;
        for (Note note : allStarNotes) {
            if (note.getText().contains(textSearch))
                searchNotes.add(note);
        }
        return searchNotes;
    }

    public List<Note> getStarNotes(int size) {
        List<Note> searchNotes = getStarNotes();
        if (size > searchNotes.size())
            size = searchNotes.size();
        return searchNotes.subList(0, size);
    }

    public boolean isStar(String text) {
        List<Note> allNotes = getNotes();
        for (Note note : allNotes) {
            if (note.getText().equals(text))
                return note.getStar();
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void changeStar(String clip) {
        changeStar(Objects.requireNonNull(getNotesEqualsText(clip)));
    }

    public void changeStar(Note note) {
        openDB();
        deleteNote(note.getText());
        addNote(note.setStar(!note.getStar()));
        closeDB();
        isChange = true;
        refreshNoteList(false, null);
    }

    private Note getNotesEqualsText(String text) {
        List<Note> allNotes = getNotes();
        for (Note note : allNotes) {
            if (note.getText().equals(text))
                return note;
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void updateApp() {
        String topNote;
        if (getNotes().size() > 0)
            topNote = getNotes().get(0).getText();
        else
            topNote = "";
        if (!CBManager.hasPrimaryClip()) {
            CBManager.setText(topNote);
            return;
        }
        String noteText;
        try {
            CharSequence charSequence = Objects.requireNonNull(CBManager.getPrimaryClip()).getItemAt(0).getText();
            noteText = String.valueOf(charSequence);
        }
        catch (Error ignored) {
            CBManager.setText(topNote);
            return;
        }
        if (!topNote.equals(noteText)) {
            CBManager.setText(topNote);
        }
    }

    public static void updateDB(Context context, Boolean add, String deleteText) {
        Intent i = new Intent(UPDATE_DB);
        if (add)
            i.putExtra(UPDATE_DB_ADD, true);
        if (deleteText != null) {
            if (!deleteText.isEmpty())
                i.putExtra(UPDATE_DB_DELETE, deleteText);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    private void refreshNoteList(Boolean add, String deleteText) {
        AppService.runAppService(context, true, false);
        updateDB(context, add, deleteText);
    }



    public static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, nameDB, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + nameTable + " (" + dateNotes + " TIMESTAMP, " + textNotes + " TEXT, " + starNotes + " BOOLEAN" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
