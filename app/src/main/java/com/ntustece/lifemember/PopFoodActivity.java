package com.ntustece.lifemember;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ntust.smartrefrigerator.listeners.OnNewTagListener;

import java.util.Calendar;

/**
 * Created by petingo on 2017/5/6.
 */

public class PopFoodActivity extends ReadNFCCard {
    public static final int QR_REQUEST = 111;
    public static String result;
    public static String cardID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_food);

        final OnNewTagListener listener = new OnNewTagListener() {
            @Override
            public void onNewTag(Tag tag) {
                cardID = tag.getUuid();
                Toast.makeText(PopFoodActivity.this, cardID, Toast.LENGTH_SHORT).show();
                Calendar calendar = Calendar.getInstance();
                long timeStamp = calendar.getTimeInMillis();
                IntentIntegrator scanIntegrator = new IntentIntegrator(PopFoodActivity.this);
                scanIntegrator.initiateScan();
            }
        };
        registerOnNewTagListener(listener);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            result = scanningResult.getContents();
            String parse[] = result.split(" ");

            if (parse[1].equals(cardID)) {
                Toast.makeText(this, "開始消磁！", Toast.LENGTH_SHORT).show();
                ServerConnection.postNewComment(PopFoodActivity.this, result, "get");
            } else {
                Toast.makeText(this, "不能偷吃別人的東西喔！", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
        PopFoodActivity.this.finish();
    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        TextView tvHint = (TextView) findViewById(R.id.hint);
//
//        if (requestCode == QR_REQUEST) {
//            if (resultCode == RESULT_OK) {
//                String qrData = data.getStringExtra(QRActivity.EXTRA_QR_RESULT);
//                tvHint.setText(qrData);
//            } else {
//                tvHint.setText("Error");
//            }
//        }
//    }
//
//    public void requestQRCodeScan() {
//        Intent qrScanIntent = new Intent(this, QRActivity.class);
//        startActivityForResult(qrScanIntent, QR_REQUEST);
//    }
}
