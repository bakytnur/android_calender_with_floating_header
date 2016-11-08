package bakha.ms.outlook.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import bakha.ms.outlook.data.Account;
import bakha.ms.outlook.data.AccountManager;
import bakha.ms.outlook.data.AlertType;
import bakha.ms.outlook.data.Event;

public class DatabaseHelper extends SQLiteOpenHelper {
        // Database Version
        private static final int DATABASE_VERSION = 2;
        // Database Name
        private static final String DATABASE_NAME = "outlookCalendar";
        // Table Names
        private static final String TABLE_EVENTS = "events";
        // Column names
        private static final String KEY_EVENT_ID = "event_id";
        private static final String KEY_EVENT_TITLE = "event_title";
        private static final String KEY_EVENT_FULL_DAY = "event_full_day";
        private static final String KEY_LOCATION = "location";
        private static final String KEY_DESCR = "description";
        private static final String KEY_START_TIME = "start_time";
        private static final String KEY_END_TIME = "end_time";
        private static final String KEY_ALERT_TYPE = "alert_type";
        private static final String KEY_ACCOUNT = "account";


        // Event table
        private static final String CREATE_TABLE_EVENTS = "CREATE TABLE "
                + TABLE_EVENTS + "("
                + KEY_EVENT_ID + " INTEGER PRIMARY KEY,"
                + KEY_EVENT_TITLE + " TEXT,"
                + KEY_EVENT_FULL_DAY + " INTEGER,"
                + KEY_LOCATION + " TEXT,"
                + KEY_DESCR + " TEXT,"
                + KEY_START_TIME + " LONG,"
                + KEY_END_TIME + " LONG,"
                + KEY_ALERT_TYPE + " INTEGER,"
                + KEY_ACCOUNT + " TEXT"
                + ")";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_EVENTS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // on upgrade drop older tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);

            // create new tables
            onCreate(db);
        }

        public int createEvent(Event event) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_EVENT_TITLE, event.getTitle());
            values.put(KEY_EVENT_FULL_DAY, event.isFullDayEvent());
            values.put(KEY_LOCATION, event.getLocation());
            values.put(KEY_DESCR, event.getDescription());
            values.put(KEY_START_TIME, event.getStartTime().getTimeInMillis());
            values.put(KEY_END_TIME, event.getEndTime().getTimeInMillis());
            values.put(KEY_ALERT_TYPE, event.getAlertType().getValue());
            values.put(KEY_ACCOUNT, event.getAccount().getEmail());

            int eventId = (int) db.insert(TABLE_EVENTS, null, values);
            return eventId;
        }

        public int updateEvent(Event event) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_EVENT_TITLE, event.getTitle());
            values.put(KEY_EVENT_FULL_DAY, event.isFullDayEvent());
            values.put(KEY_LOCATION, event.getLocation());
            values.put(KEY_DESCR, event.getDescription());
            values.put(KEY_START_TIME, event.getStartTime().getTimeInMillis());
            values.put(KEY_END_TIME, event.getEndTime().getTimeInMillis());
            values.put(KEY_ALERT_TYPE, event.getAlertType().getValue());
            values.put(KEY_ACCOUNT, event.getAccount().getEmail());

            // updating row
            return db.update(TABLE_EVENTS, values, KEY_EVENT_ID + " = ?",
                    new String[] { String.valueOf(event.getEventId()) });
        }

        public void deleteEvent(long eventId) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_EVENTS, KEY_EVENT_ID + " = ?",
                    new String[] { String.valueOf(eventId) });
        }

        public Map<Integer, Event> getEvents() {
            Map<Integer, Event> events = new HashMap<>();
            String selectQuery = "SELECT  * FROM " + TABLE_EVENTS;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {
                do {
                    Event event = new Event();
                    event.setEventId(c.getInt(c.getColumnIndex(KEY_EVENT_ID)));
                    event.setTitle(c.getString(c.getColumnIndex(KEY_EVENT_TITLE)));
                    event.setFullDayEvent(c.getInt(c.getColumnIndex(KEY_EVENT_FULL_DAY)) == 1 ? true : false);
                    event.setLocation(c.getString(c.getColumnIndex(KEY_LOCATION)));
                    Account account = AccountManager.getInstance().getAccount(c.getString(c.getColumnIndex(KEY_ACCOUNT)));
                    event.setAccount(account);
                    event.setAlertType(AlertType.forValue(c.getInt(c.getColumnIndex(KEY_ALERT_TYPE))));
                    GregorianCalendar startCalendar = new GregorianCalendar();
                    startCalendar.setTimeInMillis(c.getLong(c.getColumnIndex(KEY_START_TIME)));
                    event.setStartTime(startCalendar);
                    GregorianCalendar endCalendar = new GregorianCalendar();
                    endCalendar.setTimeInMillis(c.getLong(c.getColumnIndex(KEY_END_TIME)));
                    event.setEndTime(endCalendar);
                    event.setDescription(c.getString(c.getColumnIndex(KEY_DESCR)));
                    events.put(event.getEventId(), event);
                } while (c.moveToNext());
            }

            return events;
        }

        public void closeDatabase() {
            SQLiteDatabase db = this.getReadableDatabase();
            if (db != null && db.isOpen())
                db.close();
        }
    }