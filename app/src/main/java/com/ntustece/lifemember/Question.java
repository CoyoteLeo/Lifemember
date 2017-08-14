package com.ntustece.lifemember;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class Question extends Activity implements EasyPermissions.PermissionCallbacks {
    TextView question;
    LinearLayout editTextLayout;
    LinearLayout buttonLayout;
    SQLiteOpenHelper dbHelper = new DBHelper(this);
    SQLiteDatabase db;
    int questionType;
    private List<Event> items = new ArrayList<Event>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Google Calendar API */
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

//        questionType = 1;
        // 觸發函數
        getResultsFromApi();
        questionType = (int) ((Math.random() * 10) % 2);

        /* End */
        setContentView(R.layout.question);

        editTextLayout = (LinearLayout) findViewById(R.id.editTextLayout);
        buttonLayout = (LinearLayout) findViewById(R.id.buttonLayout);
        question = (TextView) findViewById(R.id.textViewQuestion);

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
//        int questionType = (int) ((Math.random() * 10) % 2);
        switch (questionType) {
            case 1:
                getResultsFromApi();
                break;
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

    void QT1() {
        if (items.isEmpty()) {
            finish();
        }
        Event event = items.get(0);
        question.setText(event.getSummary());
        Long start = event.getStart().getDateTime().getValue();
        final Button[] button = new Button[4];
        button[0] = (Button) findViewById(R.id.button1);
        button[1] = (Button) findViewById(R.id.button2);
        button[2] = (Button) findViewById(R.id.button3);
        button[3] = (Button) findViewById(R.id.button4);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(start);
        List<String> value = new ArrayList<String>();
        final int ansPos = (int) (Math.random() * 10) % 4;
        button[ansPos].setText(time.get(Calendar.YEAR) + "/" + (time.get(Calendar.MONTH)+1) + "/" + time.get(Calendar.DAY_OF_MONTH));
        value.add(time.get(Calendar.YEAR) + "/" + (time.get(Calendar.MONTH)+1) + "/" + (time.get(Calendar.DAY_OF_MONTH) + 1));
        value.add(time.get(Calendar.YEAR) + "/" + (time.get(Calendar.MONTH)+1) + "/" + (time.get(Calendar.DAY_OF_MONTH) - 2));
        value.add(time.get(Calendar.YEAR) + "/" + (time.get(Calendar.MONTH)+1) + "/" + (time.get(Calendar.DAY_OF_MONTH) - 3));
        for (int i = 0, counter = 0; i < 4; i++) {
            if (i == ansPos)
                continue;
            button[i].setText(value.get(counter));
            counter++;
        }

        for (int i = 0; i < 4; i++) {
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

    /* Google Calendar API */
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY};


    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(Question.this, "No network connection available.",
                    Toast.LENGTH_SHORT).show();
        } else {
            if (questionType == 1)
                new Question.MakeRequestTask(mCredential).execute();
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(Question.this, "This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                Question.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

//    private class MakeRequestTask extends AsyncTask<Void, Void, List<Event>> {

    private class MakeRequestTask extends AsyncTask<Void, Void, Boolean> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    // setApplicationName 不知道會不會有差@@
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return false;
            }
        }

        private Boolean getDataFromApi() throws IOException {
            /* 取得事件list */
            DateTime now = new DateTime(System.currentTimeMillis());
            Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
//                    .setMaxResults()
            // 數量上限
//                    .setTimeMax(new DateTime(System.currentTimeMillis() + 1209600000))
//                    .setTimeMin(new DateTime(System.currentTimeMillis() - 1209600000))
            // TimeMin:時間下限
            // TimeMax:時間上限
//                    .setOrderBy("startTime")
            // 排序依據
//                    .setSingleEvents(true)
//                    .execute();

            items = events.getItems();
            return items.size() > 0;
        }


        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Boolean output) {
            mProgress.hide();
            if (!output) {
                finish();
                Toast.makeText(Question.this, "No results returned.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Question.this, "Get results!!", Toast.LENGTH_SHORT).show();
                QT1();
                editTextLayout.setVisibility(View.GONE);
                buttonLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            GoogleCalendar.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(Question.this, "The following error occurred:\n" + mLastError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Question.this, "Request cancelled.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
