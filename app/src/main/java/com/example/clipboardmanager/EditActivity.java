package com.example.clipboardmanager;

import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import java.util.Objects;

public class EditActivity extends AppCompatActivity {
    private Content appContent;
    private InputMethodManager inputNote;

    private String foundNote;
    private EditText editNote;
    private MenuItem noteStar;
    private boolean onStar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar appToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(appToolbar);
        Intent intent = getIntent();
        onStar = intent.getBooleanExtra(NoteActivity.IS_STAR, false);
        foundNote = intent.getStringExtra(Intent.EXTRA_TEXT);
        editNote = findViewById(R.id.note_edit);
        if (foundNote == null || foundNote.equals(getString(R.string.note_new_text)))
            foundNote = "";
        editNote.setText(foundNote);
        editNote.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean input) {
                if (input) {
                    inputNote = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputNote != null) {
                        inputNote.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                }
            }
        });
        appContent = Content.newSample(this);
        String title;
        if (foundNote.isEmpty())
            title = getString(R.string.title_new_note_editor);
        else
            title = getString(R.string.title_editor);

        appToolbar.setLogo(R.drawable.icon_edit);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(getSupportActionBar()).setTitle("  " + title);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        ExitMsg(getString(R.string.message_no_save));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        setStarredIcon();
        noteStar = menu.findItem(R.id.star);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case (R.id.cancel):
                ExitMsg(getString(R.string.message_no_save));
                break;
            case (R.id.delete):
                delete();
                break;
            case (R.id.star):
                onStar = !onStar;
                setStarredIcon();
                break;
            case (R.id.share):
                share();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ExitMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            appContent.updateApp();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            finish();
        else
            finishAndRemoveTask();
    }

    private void delete() {
        appContent.editNote(foundNote, null, 0);
        ExitMsg(getString(R.string.message_delete));
    }

    private void setStarredIcon() {
        if (noteStar == null) return;
        if (onStar)
            noteStar.setIcon(R.drawable.icon_star_on);
        else
            noteStar.setIcon(R.drawable.icon_star_off);
    }

    private void share() {
        String note = editNote.getText().toString();
        Intent newIntent = new Intent(this, NoteActivity.class);
        newIntent.putExtra(Intent.EXTRA_TEXT, note);
        newIntent.putExtra(NoteActivity.ACTION_CODE, NoteActivity.SHARE);
        startService(newIntent);
    }

    public void onClickSave(View view) {
        String note = editNote.getText().toString();
        String msg;
        if (!note.isEmpty())
            msg = getString(R.string.message_copy, note + "\n");
        else
            msg = getString(R.string.message_delete);
        if (onStar)
            appContent.editNote(foundNote, note, 1);
        else
            appContent.editNote(foundNote, note, -1);
        ExitMsg(msg);
    }
}
