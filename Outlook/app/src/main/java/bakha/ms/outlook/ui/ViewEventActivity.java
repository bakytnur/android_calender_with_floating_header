package bakha.ms.outlook.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Map;

import bakha.ms.outlook.data.Account;
import bakha.ms.outlook.data.AlertType;
import bakha.ms.outlook.data.Event;
import bakha.ms.outlook.data.OutlookManager;
import bakha.ms.outlook.data.helper.Utils;

public class ViewEventActivity extends AppCompatActivity {
    private static Context mContext;
    private OutlookManager mOutlookManager;
    private Event mEvent;
    private TextView mEventTitleText;
    private TextView mLocationText;
    private TextView mStartDateText;
    private TextView mEndDateText;
    private TextView mStartTimeText;
    private TextView mEndTimeText;
    private TextView mEventDescrText;
    private TextView mAlertTypeText;
    private LinearLayout mLocationLayout;
    private LinearLayout mDescrLayout;
    private TextView mAccountTitleText;
    private TextView mAccountEmailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mContext = this;

        mEventTitleText = (TextView) findViewById(R.id.event_title);
        mLocationText = (TextView) findViewById(R.id.event_location);
        mStartDateText = (TextView) findViewById(R.id.start_date_text);
        mEndDateText = (TextView) findViewById(R.id.end_date_text);
        mStartTimeText = (TextView) findViewById(R.id.start_time_text);
        mEndTimeText = (TextView) findViewById(R.id.end_time_text);
        mEventDescrText = (TextView) findViewById(R.id.event_description);
        mAlertTypeText = (TextView) findViewById(R.id.event_alert_type);
        mLocationLayout = (LinearLayout) findViewById(R.id.location_layout);
        mDescrLayout = (LinearLayout) findViewById(R.id.descr_layout);
        mAccountTitleText = (TextView) findViewById(R.id.account_title_text);
        mAccountEmailText = (TextView) findViewById(R.id.account_email_text);
        int eventId = getIntent().getIntExtra("eventId", 0);
        mOutlookManager = OutlookManager.getInstance(this);

        Map<Integer, Event> events = mOutlookManager.getEvents();
        mEvent = events.get(eventId);
        if (mEvent != null) {
            refresh();
        }
    }

    private void refresh() {
        Calendar startTime = mEvent.getStartTime();
        Calendar endTime = mEvent.getEndTime();
        mEventTitleText.setText(mEvent.getTitle());
        mAlertTypeText.setText(getAlertTypeText(mEvent.getAlertType()));
        mStartDateText.setText(Utils.getFormattedDate(startTime));
        mEndDateText.setText(Utils.getFormattedDate(endTime));
        mStartTimeText.setText(Utils.getFormattedTime(startTime));
        mEndTimeText.setText(Utils.getFormattedTime(endTime));
        Account account = mEvent.getAccount();
        mAccountTitleText.setText(account.getName() + "_" + account.getLastName());
        mAccountEmailText.setText("(" + account.getEmail() + ")");
        if (mEvent.getLocation() != null && mEvent.getLocation().length() > 0) {
            mLocationLayout.setVisibility(View.VISIBLE);
            mLocationText.setText(mEvent.getLocation());
        }

        if (mEvent.getDescription() != null && mEvent.getDescription().length() > 0) {
            mDescrLayout.setVisibility(View.VISIBLE);
            mEventDescrText.setText(mEvent.getDescription());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_edit:
                Intent intent = new Intent(this, AddEventActivity.class);
                intent.putExtra("mode", AddEventActivity.Mode.UPDATE);
                intent.putExtra("eventId", mEvent.getEventId());
                startActivity(intent);
                break;
            case R.id.action_delete:
                AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this)
                        .setTitle(R.string.action_delete)
                        .setMessage(R.string.confirm_delete)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setCancelable(true)
                        .setPositiveButton(
                                android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        mOutlookManager.deleteEvent(mEvent);
                                        dialog.cancel();
                                        finish();
                                    }
                                })
                        .setNegativeButton(
                            android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                AlertDialog alertDlg = dlgBuilder.create();
                alertDlg.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public static String getAlertTypeText(AlertType alertType) {
        String alert;
        switch (alertType) {
            case NONE:
                alert = mContext.getResources().getString(R.string.none);
                break;
            case ON_TIME:
                alert = mContext.getResources().getString(R.string.on_time);
                break;
            case BEFORE_5_MINUTES:
                alert = mContext.getResources().getString(R.string.minutes_before_5);
                break;
            case BEFORE_10_MINUTES:
                alert = mContext.getResources().getString(R.string.minutes_before_10);
                break;
            case BEFORE_15_MINUTES:
                alert = mContext.getResources().getString(R.string.minutes_before_15);
                break;
            case BEFORE_30_MINUTES:
                alert = mContext.getResources().getString(R.string.minutes_before_30);
                break;
            case BEFORE_1_HOUR:
                alert = mContext.getResources().getString(R.string.hour_before);
                break;
            case BEFORE_1_DAY:
                alert = mContext.getResources().getString(R.string.day_before);
                break;
            default:
                alert = mContext.getResources().getString(R.string.minutes_before_15);
                break;
        }
        return alert;
    }

    public static long getTriggerBefore(AlertType alertType) {
        switch (alertType) {
            case ON_TIME:
                return 0;
            case BEFORE_5_MINUTES:
                return 5 * 60 * 1000;
            case BEFORE_10_MINUTES:
                return 10 * 60 * 1000;
            case BEFORE_15_MINUTES:
                return 15 * 60 * 1000;
            case BEFORE_30_MINUTES:
                return 30 * 60 * 1000;
            case BEFORE_1_HOUR:
                return 60 * 60 * 1000;
            case BEFORE_1_DAY:
                return 20 * 60 * 60 * 1000;
            default:
                return 15 * 60 * 1000;
        }
    }
}
