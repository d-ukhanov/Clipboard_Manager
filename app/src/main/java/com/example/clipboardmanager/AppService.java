package com.example.clipboardmanager;

import android.support.annotation.RequiresApi;
import android.app.PendingIntent;
import android.app.Service;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import android.support.v4.app.NotificationCompat;
import android.content.Context;
import android.content.Intent;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import static java.lang.Math.*;

public class AppService extends Service {
    protected boolean onStar = false;
    public int countNote = 4;
    public final static String serviceStar = "serviceStar";

    private OnPrimaryClipChangedListener listen = new OnPrimaryClipChangedListener() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void onPrimaryClipChanged() {
            checkNote();
        }
    };
    private Content content;
    private ClipboardManager CBManager;

    @Override
    public void onCreate() {
        content = Content.newSample(this.getBaseContext());
        CBManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (CBManager != null) {
            CBManager.addPrimaryClipChangedListener(listen);
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            intent = new Intent();
        }
        if (intent.getBooleanExtra(serviceStar, false)) {
            onStar = !onStar;
        }
        showService();
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showService() {
        List<Note> Notes;
        if (onStar) {
            Notes = content.getStarNotes(countNote - 1);
            Note firstNote;
            if (content.getNotes().size() != 0)
                firstNote = content.getNotes().get(0);
            else
                firstNote = new Note(getString(R.string.note_new_text), onStar, new Date());
            if (Notes.size() == 0)
                Notes.add(0, firstNote);
            else if (!firstNote.getText().equals(Notes.get(0).getText()))
                Notes.add(0, firstNote);
        }
        else
            Notes = content.getNotes(countNote);

        int count = Notes.size();
        if (content.getNotes().size() == 0) {
           if (onStar) {
               Notes.remove(0);
               Notes.add(new Note(getString(R.string.note_new_text), onStar, new Date()));
           }
           else {
               Notes.add(new Note(getString(R.string.note_new_text), onStar, new Date()));
               count += 1;
           }
        }
        else {
            Notes.add(new Note(getString(R.string.note_new_text), onStar, new Date()));
            count += 1;
        }
        count = min(count, (countNote + 1));
        AppServiceNotes appServiceNotes = new AppServiceNotes(this.getBaseContext(), Notes.get(0));
        for (int i = 1; i < count; i++) {
            appServiceNotes.addNotes(Notes.get(i));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.note_title, SupportingFunc.divideString(Notes.get(0).getText(), 200)))
                .setGroup("serviceGroup")
                .setColor(getResources().getColor(R.color.teal_dark))
                .setSmallIcon(R.drawable.service_little_icon)
                .setCustomBigContentView(appServiceNotes.build())
                .setAutoCancel(true)
                .setGroupSummary(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        startForeground(1, builder.build());
    }

    public static void runAppService(Context context, boolean notification, boolean clipboard) {
        runAppService(context, notification, clipboard, 0);
    }

    public static void runAppService(Context context, boolean notification, boolean clipboard, int msg) {
        Intent intent = new Intent(context, AppService.class)
                .putExtra("serviceShow", notification)
                .putExtra("serviceClipboardText", clipboard)
                .putExtra("serviceMsg", msg);
        context.startService(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void checkNote() {
        if (!CBManager.hasPrimaryClip())
            return;
        String noteText;
        try {
            CharSequence charSequence = Objects.requireNonNull(CBManager.getPrimaryClip()).getItemAt(0).getText();
            noteText = String.valueOf(charSequence);
        }
        catch (Error ignored) { return; }
        if (noteText.trim().isEmpty() || noteText.equals("null"))
            return;
        if (content.getNotes().size() > 0) {
            if (noteText.equals(content.getNotes().get(0).getText()))
                return;
        }
        int isStar = content.isStar(noteText) ? 1 : 0;
        content.editNote(null, noteText, isStar);
    }

    private class AppServiceNotes {
        private Context context;
        private List<Note> notes;
        private int requestCode = 0;
        private RemoteViews view;

        public AppServiceNotes(Context context, Note note) {
            this.context = context;
            String thisNote = note.getText();
            notes = new ArrayList<>();
            notes.add(note);
            view = new RemoteViews(this.context.getPackageName(), R.layout.service);
            view.setTextViewText(R.id.cur_note, SupportingFunc.divideString(thisNote,100));

            Intent runShare = new Intent(this.context, NoteActivity.class)
                    .putExtra(Intent.EXTRA_TEXT, thisNote)
                    .putExtra(NoteActivity.ACTION_CODE, NoteActivity.SHARE);
            PendingIntent serviceRunShare = PendingIntent.getService(this.context, requestCode++, runShare, PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.note_share, serviceRunShare);

            Intent runEdit = new Intent(this.context, NoteActivity.class)
                    .putExtra(Intent.EXTRA_TEXT, thisNote)
                    .putExtra(NoteActivity.IS_STAR, note.getStar())
                    .putExtra(NoteActivity.ACTION_CODE, NoteActivity.EDIT);
            PendingIntent serviceRunEdit = PendingIntent.getService(this.context, requestCode++, runEdit, PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.cur_note, serviceRunEdit);

            Intent runStar = new Intent(this.context, AppService.class).putExtra(serviceStar, true);
            PendingIntent serviceRunStar = PendingIntent.getService(this.context, requestCode++, runStar, PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.star, serviceRunStar);
            if (onStar)
                view.setImageViewResource(R.id.star, R.drawable.icon_star_on);
            else
                view.setImageViewResource(R.id.star, R.drawable.icon_star_off);
            view.removeAllViews(R.id.notice_list);
        }

        public void addNotes(Note note) {
            RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.service_note);
            if (note.getStar())
                view.setTextViewText(R.id.note_text, "\uD83C\uDF1F " + SupportingFunc.divideString(note.getText(), 100));
            else
                view.setTextViewText(R.id.note_text, SupportingFunc.divideString(note.getText(), 100));

            Intent runEdit = new Intent(context, NoteActivity.class)
                    .putExtra(Intent.EXTRA_TEXT, note.getText())
                    .putExtra(NoteActivity.IS_STAR, note.getStar())
                    .putExtra(NoteActivity.ACTION_CODE, NoteActivity.EDIT);
            PendingIntent serviceRunEdit = PendingIntent.getService(context, requestCode++, runEdit, PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.note_text, serviceRunEdit);

            if (note.getText().equals(getString(R.string.note_new_text))) {
                view.setTextViewText(R.id.note_text, getString(R.string.note_new_text));
                view.setImageViewResource(R.id.copy_note, 0);
            }
            else {
                notes.add(note);
                Intent runCopy = new Intent(context, NoteActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, note.getText())
                        .putExtra(NoteActivity.IS_STAR, true)
                        .putExtra(NoteActivity.ACTION_CODE, NoteActivity.COPY);
                PendingIntent serviceRunCopy = PendingIntent.getService(context, requestCode++, runCopy, PendingIntent.FLAG_UPDATE_CURRENT);
                view.setOnClickPendingIntent(R.id.copy_note, serviceRunCopy);
            }
            this.view.addView(R.id.notice_list, view);
        }

        public RemoteViews build() {
            return view;
        }
    }
}
