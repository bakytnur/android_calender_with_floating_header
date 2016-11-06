package bakha.ms.outlook.ui;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Random;

import bakha.ms.outlook.data.Account;
import bakha.ms.outlook.data.AccountManager;
import bakha.ms.outlook.data.AlertType;
import bakha.ms.outlook.data.Event;
import bakha.ms.outlook.data.OutlookManager;
import bakha.ms.outlook.data.helper.Utils;

public class AddEventActivity extends AppCompatActivity implements
        View.OnClickListener, CompoundButton.OnCheckedChangeListener, PopupMenu.OnMenuItemClickListener {
    private EditText mEditEventTitle;
    private EditText mEditEventDescr;
    private TextView mTextAccount;
    private TextView mTextEmail;

    private static TextView mStartDateText;
    private static TextView mEndDateText;
    private static TextView mStartTimeText;
    private static TextView mEndTimeText;
    private Calendar mPrevStartDateTime;
    private TextView mAlertTypeText;
    private ToggleButton mAllDayToggle;
    // the add_event we are creating or editing
    private Event mEvent;
    private AccountManager mAccountManager;
    private OutlookManager mOutlookManager;
    private EditText mEditLocation;
    public enum Mode {
        CREATE, UPDATE
    };
    private Mode mCurrentMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        mCurrentMode = (Mode) getIntent().getSerializableExtra("mode");

        mOutlookManager = OutlookManager.getInstance(this);
        mAccountManager = AccountManager.getInstance();
        mTextAccount = (TextView) findViewById(R.id.account_name);
        mTextEmail = (TextView) findViewById(R.id.account_email);

        mEditEventTitle = (EditText) findViewById(R.id.event_title);
        mEditLocation = (EditText) findViewById(R.id.event_location);

        mStartDateText = (TextView) findViewById(R.id.start_date_text);
        mStartDateText.setOnClickListener(this);
        mEndDateText = (TextView) findViewById(R.id.end_date_text);
        mEndDateText.setOnClickListener(this);
        mStartTimeText = (TextView) findViewById(R.id.start_time_text);
        mStartTimeText.setOnClickListener(this);
        mEndTimeText = (TextView) findViewById(R.id.end_time_text);
        mEndTimeText.setOnClickListener(this);
        mEditEventDescr = (EditText) findViewById(R.id.event_description);

        mAllDayToggle = (ToggleButton) findViewById(R.id.all_day_toggle);
        mAllDayToggle.setOnCheckedChangeListener(this);

        mAlertTypeText = (TextView) findViewById(R.id.event_alert_type);
        mAlertTypeText.setOnClickListener(this);

        if (mCurrentMode == Mode.CREATE) {
            initializeDateTime();
            mEvent = new Event();
            mEvent.setAlertType(AlertType.BEFORE_15_MINUTES);
            mEvent.setAccount(mAccountManager.getCurrentAccount());
        } else {
            getSupportActionBar().setTitle(R.string.edit_event);
            int eventId = getIntent().getIntExtra("eventId", 0);
            OutlookManager outlookManager = OutlookManager.getInstance(this);

            Map<Integer, Event> events = outlookManager.getEvents();
            mEvent = events.get(eventId);
            initializeEditUi();
        }

        formatAccountText();
    }

    private void initializeDateTime() {
        int position = mOutlookManager.getPosition();
        Calendar selectedDate = mOutlookManager.getCalendarDateList().get(position);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(selectedDate.getTime());
        // set default start/end date
        mStartDateText.setText(Utils.getFormattedDate(calendar));
        mEndDateText.setText(Utils.getFormattedDate(calendar));

        calendar.add(Calendar.HOUR, 8);
        mStartTimeText.setText(Utils.getFormattedTime(calendar));
        calendar.add(Calendar.MINUTE, 30);
        mEndTimeText.setText(Utils.getFormattedTime(calendar));
    }

    private void initializeEditUi() {
        mEditEventTitle.setText(mEvent.getTitle());
        mEditEventDescr.setText(mEvent.getDescription());
        mEditLocation.setText(mEvent.getLocation());
        mAlertTypeText.setText(ViewEventActivity.getAlertTypeText(mEvent.getAlertType()));
        mStartDateText.setText(Utils.getFormattedDate(mEvent.getStartTime()));
        mEndDateText.setText(Utils.getFormattedDate(mEvent.getEndTime()));
        mStartTimeText.setText(Utils.getFormattedTime(mEvent.getStartTime()));
        mEndTimeText.setText(Utils.getFormattedTime(mEvent.getEndTime()));
        mPrevStartDateTime = mEvent.getStartTime();
    }

    private void formatAccountText() {
        Account account = mEvent.getAccount();
        mTextAccount.setText(account.getName() + "_" + account.getLastName());
        mTextEmail.setText(account.getEmail());
    }

    private static int validateDates(Calendar startCalendar, Calendar endCalendar) {
        return startCalendar.compareTo(endCalendar);
    }

    /**
     * If start time if later than end time, reset the end time to the start time, and add 30 more minutes
     */
    private static void updateEndTimeIfRequired() {
        String startDateTime = mStartDateText.getText().toString() + " " + mStartTimeText.getText().toString();
        String endDateTime = mEndDateText.getText().toString() + " " + mEndTimeText.getText().toString();
        GregorianCalendar startCalendar = (GregorianCalendar) Utils.getCalendarDateTimeFromText(startDateTime);
        GregorianCalendar endCalendar = (GregorianCalendar) Utils.getCalendarDateTimeFromText(endDateTime);

        if (validateDates(startCalendar, endCalendar) >= 0) {
            endCalendar = startCalendar;
            startCalendar.add(Calendar.MINUTE, 30);
            mEndDateText.setText(mStartDateText.getText());
            mEndTimeText.setText(Utils.getFormattedTime(endCalendar));
        }
    }

    private boolean buildEvent() {
        if (mCurrentMode == Mode.CREATE) {
            mEvent.setEventId(new Random().nextInt(100));
        }
        mEvent.setTitle(mEditEventTitle.getText().toString().trim());
        mEvent.setDescription(mEditEventDescr.getText().toString().trim());
        mEvent.setLocation(mEditLocation.getText().toString().trim());

        String startDateTime = mStartDateText.getText().toString() + " " + mStartTimeText.getText().toString();
        String endDateTime = mEndDateText.getText().toString() + " " + mEndTimeText.getText().toString();
        GregorianCalendar startCalendar = (GregorianCalendar) Utils.getCalendarDateTimeFromText(startDateTime);
        GregorianCalendar endCalendar = (GregorianCalendar) Utils.getCalendarDateTimeFromText(endDateTime);

        if (validateDates(startCalendar, endCalendar) >= 0) {
            showWarningDialog(getString(R.string.dlg_title_date), getString(R.string.date_time_warning));
            return false;
        }

        if (mEvent.getTitle().length() == 0) {
            showWarningDialog(getString(R.string.dlg_title_title), getString(R.string.event_title_warning));
            return false;
        }

        mEvent.setStartTime(startCalendar);
        mEvent.setEndTime(endCalendar);
        return true;
    }

    private void createEvent() {
        mOutlookManager.createEvent(mEvent);
    }

    private void updateEvent() {
        mOutlookManager.updateEvent(mEvent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_done:
                if (buildEvent()) {
                    if (mCurrentMode == Mode.CREATE) {
                        createEvent();
                    } else {
                        cancelNotificationAlarm();
                        updateEvent();
                    }

                    setNotificationAlarm();
                    finish();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private PendingIntent createAlarmIntent(Calendar date) {
        Intent intent = new Intent(this, OutlookNotifyService.class);
        intent.putExtra("title", mEvent.getTitle());
        intent.putExtra("date", date);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private void setNotificationAlarm() {
        if (mEvent.getAlertType() != AlertType.NONE) {
            Calendar calendar = mEvent.getStartTime();
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent pendingIntent = createAlarmIntent(mEvent.getStartTime());
            long triggerAtMillis = calendar.getTimeInMillis() - ViewEventActivity.getTriggerBefore(mEvent.getAlertType());
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, 0, pendingIntent);
        }
    }

    private void cancelNotificationAlarm() {
        if (mEvent.getAlertType() != AlertType.NONE) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent pendingIntent = createAlarmIntent(mPrevStartDateTime);
            alarmManager.cancel(pendingIntent);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // for dynamic menu id
        List<Account> accountList = mAccountManager.getAccounts();
        int order = 0;
        for (Account account : accountList) {
            if (item.getItemId() == account.getAccountId() + order) {
                mEvent.setAccount(account);
                formatAccountText();
                mAccountManager.setCurrentAccount(account);
                return true;
            }
            order ++;
        }

        mAlertTypeText.setText(item.getTitle());
        switch (item.getItemId()) {
            case R.id.none:
                mEvent.setAlertType(AlertType.NONE);
                return true;
            case R.id.on_time:
                mEvent.setAlertType(AlertType.ON_TIME);
                return true;
            case R.id.minutes_before_5:
                mEvent.setAlertType(AlertType.BEFORE_5_MINUTES);
                return true;
            case R.id.minutes_before_10:
                mEvent.setAlertType(AlertType.BEFORE_10_MINUTES);
                return true;
            case R.id.minutes_before_15:
                mEvent.setAlertType(AlertType.BEFORE_15_MINUTES);
                return true;
            case R.id.minutes_before_30:
                mEvent.setAlertType(AlertType.BEFORE_30_MINUTES);
                return true;
            case R.id.hour_before:
                mEvent.setAlertType(AlertType.BEFORE_1_HOUR);
                return true;
            case R.id.day_before:
                mEvent.setAlertType(AlertType.BEFORE_1_DAY);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_date_text:
                showDatePickerDialog(v);
                break;
            case R.id.end_date_text:
                showDatePickerDialog(v);
                break;
            case R.id.start_time_text:
                showTimePickerDialog(v);
                break;
            case R.id.end_time_text:
                showTimePickerDialog(v);
                break;
            case R.id.event_alert_type:
                showAlertTypeMenu(v);
                break;
        }
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("view", v.getId());
        newFragment.setArguments(bundle);
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("view", v.getId());
        newFragment.setArguments(bundle);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showAccountMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(AddEventActivity.this, view);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.drawer, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(AddEventActivity.this);

        List<Account> accountList = mAccountManager.getAccounts();
        int order = 0;
        popupMenu.getMenu().setGroupCheckable(0, true, true);
        for (Account account : accountList) {
            // for dynamic menu id, just set the account id
            popupMenu.getMenu().add(R.id.accounts, account.getAccountId() + order, order, account.getEmail());
            order ++;
        }

        popupMenu.show();
    }

    public void showAlertTypeMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.alert_popup_menu);
        int currentIndex = mEvent.getAlertType().getValue();
        popup.getMenu().getItem(currentIndex).setChecked(true);
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.all_day_toggle) {
            mStartTimeText.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            mEndTimeText.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            mStartTimeText.setText("12:00 AM");
            mEndTimeText.setText("11:59 PM");
        }
    }

    private void showWarningDialog(String title, String message) {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(true)
            .setPositiveButton(
                android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDlg = dlgBuilder.create();
        alertDlg.show();
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        private int mViewId;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar calendar;
            Bundle arguments = this.getArguments();
            mViewId = arguments.getInt("view");
            if (mViewId == R.id.start_date_text) {
                calendar = Utils.getCalendarDateFromText(mStartDateText.getText().toString());
            } else {
                calendar = Utils.getCalendarDateFromText(mEndDateText.getText().toString());
            }

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            if (mViewId == R.id.start_date_text) {
                GregorianCalendar calendar = new GregorianCalendar(year, month, day);
                mStartDateText.setText(Utils.getFormattedDate(calendar));
                updateEndTimeIfRequired();
            } else {
                GregorianCalendar calendar = new GregorianCalendar(year, month, day);
                mEndDateText.setText(Utils.getFormattedDate(calendar));
            }
        }
    };


    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        private int mViewId;

        @Override
        public Dialog onCreateDialog(Bundle args) {
            Calendar calendar;
            Bundle arguments = this.getArguments();
            mViewId = arguments.getInt("view");
            if (mViewId == R.id.start_time_text) {
                calendar = Utils.getCalendarTimeFromText(mStartTimeText.getText().toString());
            } else {
                calendar = Utils.getCalendarTimeFromText(mEndTimeText.getText().toString());
            }
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (mViewId == R.id.start_time_text) {
                GregorianCalendar calendar = new GregorianCalendar(0, 0, 0, hourOfDay, minute);
                mStartTimeText.setText(Utils.getFormattedTime(calendar));
                updateEndTimeIfRequired();
            } else {
                GregorianCalendar calendar = new GregorianCalendar(0, 0, 0, hourOfDay, minute);
                mEndTimeText.setText(Utils.getFormattedTime(calendar));
            }
        }
    };
}
