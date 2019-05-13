package com.txiao.mutemedia;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.txiao.mutemedia.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Util.configure(this.getApplicationContext());

        setContentView(R.layout.activity_main);

        super.onCreate(savedInstanceState);

    }
}
