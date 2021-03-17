package com.txiao.mutemedia;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Util.configure(this.getApplicationContext());

        setContentView(R.layout.activity_main);

        super.onCreate(savedInstanceState);

    }
}
