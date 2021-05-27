package com.example.clipboardmanager;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

public class NoteActivity extends IntentService {
    private Content appContent;
    protected Context context;
    public Handler appHandler;
    private Intent intent;

    public final static int COPY = 1;
    public final static int SHARE = 2;
    public final static int EDIT = 3;
    public final static int STAR = 4;
    public final static int DELETE = 5;
    public static String ACTION_CODE = "actionCode";
    public static String IS_STAR = "onStar";

    public NoteActivity() {
        super("NoteActivity");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appHandler = new Handler();
        context = this.getBaseContext();
        appContent = Content.newSample(context);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onHandleIntent(Intent intent) {
        Intent broadcastIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(broadcastIntent);
        this.intent = intent;
        if (intent == null) return;
        String note = intent.getStringExtra(Intent.EXTRA_TEXT);
        int actionNote = intent.getIntExtra(ACTION_CODE, 0);
        switch (actionNote) {
            case EDIT:
                edit(note);
                return;
            case DELETE:
                delete(note);
                return;
            case COPY:
                copy(note);
                return;
            case STAR:
                starOn(note);
                return;
            case SHARE:
                share(note);
                return;
            default:
                break;
        }
    }

    private void edit(final String note) {
        Intent newIntent = new Intent(this, EditActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(IS_STAR, intent.getBooleanExtra(IS_STAR, false))
                .putExtra(Intent.EXTRA_TEXT, note);
        startActivity(newIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void delete(final String note) {
        appContent.editNote(note, null, 0);
        Toast.makeText(NoteActivity.this, R.string.message_delete, Toast.LENGTH_SHORT).show();
        appContent.updateApp();
    }

    private void copy(final String note) {
        appHandler.post(new Runnable() {
            @SuppressLint("StringFormatInvalid")
            @Override
            public void run() {
                ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (cbm != null)
                    cbm.setText(note);
                Toast.makeText(NoteActivity.this, getString(R.string.message_copy, note +"\n"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void starOn(final String note) {
        Content appContent = Content.newSample(this);
        appContent.changeStar(note);
    }

    private void share(final String note) {
        Intent newIntent = new Intent();
        newIntent.setAction(Intent.ACTION_SEND);
        newIntent.setType("text/plain");
        newIntent.putExtra(Intent.EXTRA_TEXT, note);
        Intent sendIntent = Intent.createChooser(newIntent, getString(R.string.to_share)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(sendIntent);
    }
}
