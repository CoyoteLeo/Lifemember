package com.ntustece.lifemember;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by petingo on 2017/8/14.
 */

public class Question extends Activity {
    LinearLayout editTextLayout;
    LinearLayout buttonLayout;
    SQLiteOpenHelper dbHelper = new DBHelper(this);
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question);

        TextView question = (TextView) findViewById(R.id.textViewQuestion);

        editTextLayout = (LinearLayout) findViewById(R.id.editTextLayout);
        buttonLayout = (LinearLayout) findViewById(R.id.buttonLayout);

        db = dbHelper.getReadableDatabase();

        Cursor cs = db.rawQuery("Select * from data", null);
        int num = cs.getCount();
        final int itemNO = (int) (Math.random() * 10) % num;
        cs.moveToPosition(itemNO);
        long time = cs.getInt(1);
        String item = cs.getString(2);
        int price = cs.getInt(3);
        String store = cs.getString(4);
        String ticketID = cs.getString(5);

        long now = System.currentTimeMillis() / 1000;

        Log.e("time", String.valueOf(time));
        Log.e("Tick", ticketID);

        // Question Type:
        // 0 : item  => price -- edit
        // 1 : price => item  -- select
        // 3 : activity  => time -- select

        int questionType = (int) ((Math.random() * 10) % 5);
        switch (questionType) {
            case 1:
                editTextLayout.setVisibility(View.GONE);
                buttonLayout.setVisibility(View.VISIBLE);
                break;
            case 0:
            default:
                String when = null;
                long timeDis = now - time;
                if (timeDis > 259200 && timeDis < 345600) {
                    when = "大前天";
                } else if (timeDis > 172800) {
                    when = "前天";
                } else if (timeDis > 86400) {
                    when = "昨天";
                } else {
                    when = "今天";
                }

                question.setText(when + "在" + store + "買的" + item + "價格是？");
                buttonLayout.setVisibility(View.GONE);
                editTextLayout.setVisibility(View.VISIBLE);
                QT0(price);
                break;
        }
    }

    void QT0(final int price) {
        final EditText editTextPrice = (EditText) findViewById(R.id.editTextPrice);
        final Button confirm = (Button) findViewById(R.id.confirm);
        final int[] tryTime = {0};
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int input = Integer.valueOf(editTextPrice.getText().toString());
                if (input == price) {
                    finish();
                } else {
                    editTextPrice.setText("");
                }
                tryTime[0]++;

                //改成選擇題
                if (tryTime[0] == 3) {
                    editTextLayout.setVisibility(View.GONE);
                    buttonLayout.setVisibility(View.VISIBLE);

                    final Button[] button = new Button[4];
                    button[0] = (Button) findViewById(R.id.button1);
                    button[1] = (Button) findViewById(R.id.button2);
                    button[2] = (Button) findViewById(R.id.button3);
                    button[3] = (Button) findViewById(R.id.button4);

                    final int ansPos = (int) (Math.random() * 10) % 4;
                    int i = 1;
                    int tmpPos = ansPos - 1;

                    while (tmpPos >= 0) {
                        button[tmpPos].setText(String.valueOf(price - (10 * i)));
                        i++;
                        tmpPos--;
                    }
                    i = 1;
                    tmpPos = ansPos + 1;
                    while (tmpPos <= 3) {
                         button[tmpPos].setText(String.valueOf(price + (10 * i)));
                        i++;
                        tmpPos++;
                    }
                    button[ansPos].setText(String.valueOf(price));

                    for (i = 0; i < 4; i++) {
                        final int finalI = i;
                        button[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (ansPos == finalI) {
                                    finish();
                                } else {
                                    setUnClickable(button[finalI]);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    void QT1(int type, int ans) {
        final Button[] button = new Button[4];
        button[0] = (Button) findViewById(R.id.button1);
        button[1] = (Button) findViewById(R.id.button2);
        button[2] = (Button) findViewById(R.id.button3);
        button[3] = (Button) findViewById(R.id.button4);

        // ans => setText
        // 答對 => finish()
        // 答錯 => setUnClickable

        final int ansPos = (int) (Math.random() * 10) % 4;

        int i = 1, tmpPos = ansPos;
        while (tmpPos >= 0) {
            button[ansPos].setText(String.valueOf(ans - (10 * i)));
            i++;
            tmpPos--;
        }
        button[ansPos].setText(String.valueOf(ans));

        for (i = 0; i < 4; i++) {
            final int finalI = i;
            button[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ansPos == finalI) {
                         finish();
                    } else {
                        setUnClickable(button[finalI]);
                    }
                }
            });
        }
    }

    void setUnClickable(Button button) {
        button.setClickable(false);
        button.setBackgroundColor(0xaaaaaa);
    }
}
