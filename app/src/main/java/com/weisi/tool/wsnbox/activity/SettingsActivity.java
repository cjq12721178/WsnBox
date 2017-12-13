package com.weisi.tool.wsnbox.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.weisi.tool.wsnbox.R;
import com.weisi.tool.wsnbox.fragment.SettingsFragment;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content,
                            new SettingsFragment())
                    .commit();
        }
    }
}
