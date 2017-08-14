package com.ntustece.lifemember;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    public static boolean tag = true;
    private ScreenListener listener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listener = new ScreenListener(this);
        listener.register(new ScreenListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {
                Log.e("zhang", "MainActivity --> onScreenOn--> ");
                if(tag) {
                    Intent showQ = new Intent();
                    showQ.setClass(MainActivity.this, Question.class);
                    startActivity(showQ);
                    tag = false;
                }
            }

            @Override
            public void onScreenOff() {
                Log.e("zhang", "MainActivity --> onScreenOff--> ");
            }

            @Override
            public void onUserPresent() {
                Log.e("zhang", "MainActivity --> onUserPresent--> ");
            }
        });

        checkFirstRun();

        Button buttonTest = (Button) findViewById(R.id.buttonTest);
        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showWD = new Intent();
                showWD.setClass(MainActivity.this, Question.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("Eng", Eng[position]);
//                bundle.putString("Chi", Chi[position]);
//                showWD.putExtras(bundle);
                startActivity(showWD);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void checkFirstRun() {
        Util.copyDB(this);
    }
    
    protected void onDestroy() {
//        if (listener != null) {
//            listener.unregister();
//        }
        super.onDestroy();
    }
}
