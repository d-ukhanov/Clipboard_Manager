package com.example.clipboardmanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class TemplateActivity extends AppCompatActivity {
   // public static final String ACTIVITY_OPENED = "opened";
  //  public static final String ACTIVITY_CLOSED = "closed";

    protected SharedPreferences preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preference = PreferenceManager.getDefaultSharedPreferences(this);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    @Override
    protected void onPause() {
        super.onPause();
      //  LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTIVITY_CLOSED));
        AppService.runAppService(this, true, true, -1);
    }

    @Override
    protected void onResume() {
        super.onResume();
      //  LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTIVITY_OPENED));
        AppService.runAppService(this, true, true, 1);
    }
}