package com.example.clipboardmanager;

import android.support.annotation.NonNull;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.util.DisplayMetrics;
import java.util.List;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;

public class MainActivity extends AppCompatActivity {
    protected Context context;
    private Content appContent;
    private SearchView appSearch;
    private MenuItem appSearchNote;
    private String textSearch = "";
    protected MenuItem noteStar;
    protected boolean onStar = false;
    protected Toolbar appToolbar;
    private LinearLayout Layout;
    private RecyclerView List;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this.getBaseContext();
        appContent = Content.newSample(context);
        Layout = findViewById(R.id.bg_layout);
        appToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(appToolbar);
        appToolbar.setNavigationIcon(R.drawable.icon_app);
        drawListNotes();
        BroadcastReceiver appReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getBooleanExtra(Content.UPDATE_DB_ADD, false))
                    setNotes();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(appReceiver, new IntentFilter(Content.UPDATE_DB));
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppService.runAppService(this, true, true, 1);
        setNotes();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int idOptions = item.getItemId();
        switch (idOptions) {
            case R.id.search:
                return super.onOptionsItemSelected(item);
            case R.id.star:
                onStarOptions();
                break;
            case R.id.delete_all:
                deleteNotes();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager appSearchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        appSearchNote = menu.findItem(R.id.search);
        appSearch = (SearchView) appSearchNote.getActionView();
        assert appSearchManager != null;
        appSearch.setSearchableInfo(appSearchManager.getSearchableInfo(getComponentName()));
        MenuItemCompat.setOnActionExpandListener(appSearchNote, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                appSearch.setIconified(false);
                appSearch.requestFocus();
                textSearch = appSearch.getQuery().toString();
                setNotes();
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                appSearch.clearFocus();
                textSearch = null;
                setNotes();
                return true;
            }
        });
        appSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                appSearch.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                textSearch = newText;
                setNotes();
                return true;
            }
        });
        appSearch.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                appSearchNote.collapseActionView();
                textSearch = null;
                drawListNotes();
                setNotes();
                return false;
            }
        });
        noteStar = menu.findItem(R.id.star);
        return super.onCreateOptionsMenu(menu);
    }

    public void addClick(View view) {
        final Intent intent = new Intent(this, EditActivity.class).putExtra(NoteActivity.IS_STAR, onStar);
        startActivity(intent);
    }

    private void onStarOptions() {
        onStar = !onStar;
        setMenuStar();
        setNotes();
    }

    protected void setMenuStar() {
        if (noteStar == null) return;
        if (onStar)
            noteStar.setIcon(R.drawable.icon_star_on);
         else
            noteStar.setIcon(R.drawable.icon_star_off);
    }

    protected void setNotes() {
        List<Note> notes;
        if (onStar)
            notes = appContent.getStarNotes(textSearch);
        else
            notes = appContent.getNotes(textSearch);
        ListNotesContent noteText = new ListNotesContent(notes, this);
        List.setAdapter(noteText);
    }

    private void deleteNotes() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.to_delete_all)
                .setMessage(getString(R.string.message_delete_all))
                .setPositiveButton(getString(R.string.message_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                appContent.deleteNotes();
                            }
                        }
                )
                .setNegativeButton(getString(R.string.message_cancel), null)
                .show();
    }

    private void drawListNotes() {
        Layout.removeAllViewsInLayout();
        LayoutInflater layout_F = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layout_F != null)
            layout_F.inflate(R.layout.recyclerview, Layout, true);
        List = findViewById(R.id.list);
        List.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        List.setLayoutManager(layoutManager);
    }

    protected void onEditClickNote(final Context context, final Note note, View button) {
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent newIntent = new Intent(context, NoteActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, note.getText())
                        .putExtra(NoteActivity.IS_STAR, note.getStar())
                        .putExtra(NoteActivity.ACTION_CODE, NoteActivity.EDIT);
                context.startService(newIntent);
                return true;
            }
        });
    }

    protected void onClickNote(final Context context, final Note note, final int code, View button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openIntent = new Intent(context, NoteActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, note.getText())
                        .putExtra(NoteActivity.IS_STAR, note.getStar())
                        .putExtra(NoteActivity.ACTION_CODE, code);
                context.startService(openIntent);
            }
        });
    }

    public class ListNotesContent extends RecyclerView.Adapter<ListNotesContent.NoteContent> {
        private Context context;
        private List<Note> NotesList;

        @NonNull
        @Override
        public NoteContent onCreateViewHolder(ViewGroup parent, int i) {
            View viewNote = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main_note, parent, false);
            return new NoteContent(viewNote);
        }

        @Override
        public void onBindViewHolder(final NoteContent NoteViewHolder, int i) {
            final Note note = NotesList.get(i);
            NoteViewHolder.text.setText(SupportingFunc.divideString(note.getText(), 100));
            NoteViewHolder.date.setText(SupportingFunc.getFormatDate(context, note.getDate()));
            NoteViewHolder.time.setText(SupportingFunc.getFormatTime(context, note.getDate()));
            if (note.getStar())
                NoteViewHolder.star.setImageResource(R.drawable.icon_star_on);
            else
                NoteViewHolder.star.setImageResource(R.drawable.icon_star_off);

            onEditClickNote(context, note, NoteViewHolder.text);
            onClickNote(context, note, NoteActivity.COPY, NoteViewHolder.text);
            onClickNote(context, note, NoteActivity.DELETE, NoteViewHolder.delete);
            onClickNote(context, note, NoteActivity.SHARE, NoteViewHolder.share);
            NoteViewHolder.star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appContent.changeStar(note);
                    if (note.getStar())
                        NoteViewHolder.star.setImageResource(R.drawable.icon_star_on);
                    else {
                        NoteViewHolder.star.setImageResource(R.drawable.icon_star_off);
                        if (onStar)
                            remove(note);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return NotesList.size();
        }

        public ListNotesContent(List<Note> noteList, Context context) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            this.context = context;
            this.NotesList = noteList;
        }

        public void remove(Note clipObject) {
            int pos = NotesList.indexOf(clipObject);
            if (pos == -1)
                return;
            NotesList.remove(pos);
            notifyItemRemoved(pos);
        }

        public class NoteContent extends RecyclerView.ViewHolder {
            protected TextView text;
            protected TextView date;
            protected TextView time;
            protected ImageButton delete;
            protected ImageButton share;
            protected ImageButton star;

            public NoteContent(View v) {
                super(v);
                text = v.findViewById(R.id.main_text);
                date = v.findViewById(R.id.main_date);
                time = v.findViewById(R.id.main_time);
                delete = v.findViewById(R.id.main_delete);
                share = v.findViewById(R.id.main_share);
                star = v.findViewById(R.id.main_star);
            }
        }
    }
}
