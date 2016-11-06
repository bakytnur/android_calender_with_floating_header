package bakha.ms.outlook.data;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bakha.ms.outlook.data.helper.Utils;
import bakha.ms.outlook.db.DatabaseHelper;

/**
 * Singleton class managing all events in outlook calendar
 */
public class OutlookManager {
    private static OutlookManager instance;
    private List<Calendar> calendarDateList;
    private Map<Integer, Event> events;
    private Map<String, List<TimeSlot>> timeSlots;
    private Calendar calendar;
    private Context context;
    private DatabaseHelper dbHelper;
    private int currentYear;
    private int currentMonth;
    private int currentWeekDay;
    private int currentDayOfMonth;
    private GregorianCalendar todayDate;
    private int position;
    private int daysCountAhead;
    private int daysCountBefore;
    private final int[] monthEndings = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    public static final int NEXT_YEAR = 371;
    public static final int PREVIOUS_3_MONTHS = 98;

    private OutlookManager(Context context) {
        dbHelper = new DatabaseHelper(context);
        this.context = context;
        calendar = Calendar.getInstance();
        calendarDateList = new ArrayList<>();
        daysCountAhead = NEXT_YEAR;
        daysCountBefore = PREVIOUS_3_MONTHS;
        todayDate = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        initializeCalendar();
        events = new HashMap<>();
        position = PREVIOUS_3_MONTHS + currentWeekDay;
        timeSlots = new HashMap<>();
    }

    public static OutlookManager getInstance(Context context) {
        if(instance == null){
            instance = new OutlookManager(context);
        }

        return instance;
    }

    private void initializeCalendar() {
        calendarDateList.clear();
        currentMonth = calendar.get(Calendar.MONTH);
        currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        currentWeekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        currentYear = calendar.get(Calendar.YEAR);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();

        int dayOfMonth = currentDayOfMonth;
        int month = currentMonth;
        int year = currentYear;
        int dayOfWeek = currentWeekDay;

        // initialize the days visible after current day
        for (int i = currentWeekDay; i < daysCountAhead; i++) {
            GregorianCalendar calendar = new GregorianCalendar(year, month, dayOfMonth);
            calendarDateList.add(calendar);
            dayOfMonth++;
            dayOfWeek++;

            int monthEndingDay;
            if (month == 1 && gregorianCalendar.isLeapYear(year)) {
                monthEndingDay = 29;
            } else {
                monthEndingDay = monthEndings[month];
            }

            if (dayOfMonth > monthEndingDay) {
                dayOfMonth = 1;
                if (month == 11) {
                    month = 0;
                    year++;
                } else {
                    month++;
                }
            }

            if (dayOfWeek == 7) {
                dayOfWeek = 0;
            }
        }

        dayOfMonth = currentDayOfMonth;
        month = currentMonth;
        year = currentYear;
        dayOfWeek = currentWeekDay;
        // initialize the days visible before current day
        int prevViewableDaysCount = daysCountBefore + currentWeekDay;
        for (int i = 0; i < prevViewableDaysCount; i++) {
            dayOfMonth--;
            dayOfWeek--;

            if (dayOfMonth == 0) {
                if (month == 0) {
                    month = 11;
                    year--;
                } else {
                    month--;
                }

                int monthEndingDay;
                if (month == 2 && gregorianCalendar.isLeapYear(year)) {
                    monthEndingDay = 29;
                } else {
                    monthEndingDay = monthEndings[month];
                }
                dayOfMonth = monthEndingDay;
            }

            if (dayOfWeek < 0) {
                dayOfWeek = 6;
            }

            GregorianCalendar calendar = new GregorianCalendar(year, month, dayOfMonth);
            // always insert at the top position as it is previous day of the current day
            calendarDateList.add(0, calendar);
        }
    }

    public List<Calendar> getCalendarDateList() {
        return calendarDateList;
    }

    public Map<String, List<TimeSlot>> getTimeSlots() {
        return timeSlots;
    }

    public Calendar getTodayDate() {
        return todayDate;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getCurrentWeekDay() {
        return currentWeekDay;
    }

    public Map<Integer, Event> getEvents() {
        return events;
    }

    /**
     * Database read and write might be heavy,
     * So initial reading will be done in background thread
     */
    private class GetEventsTask extends AsyncTask<Context, Void, Map<Integer, Event>> {

        @Override
        protected Map<Integer, Event> doInBackground(Context... params) {
            DatabaseHelper dbHelper = new DatabaseHelper(params[0]);
            Map<Integer, Event> events = dbHelper.getEvents();
            Iterator iterator = events.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry pair = (Map.Entry)iterator.next();
                Event event = (Event)pair.getValue();
                // create time slots
                event.createTimeSlots((GregorianCalendar) event.getStartTime(), (GregorianCalendar) event.getEndTime());
                updateTimeSlots(event);
            }
            dbHelper.closeDatabase();
            return events;
        }

        @Override
        protected void onPostExecute(Map<Integer, Event> eventsFromDb) {
            events = eventsFromDb;
        }
    }

    /**
     * Load existing events from db
     */
    public void loadEvents() {
        timeSlots.clear();
        new GetEventsTask().execute(context);
    }

    public void createEvent(Event event) {
        if (event == null) {
            return;
        }

        // create event into the db
        int eventId = dbHelper.createEvent(event);
        event.setEventId(eventId);
        // create time slots
        event.createTimeSlots((GregorianCalendar) event.getStartTime(), (GregorianCalendar) event.getEndTime());
        updateTimeSlots(event);
    }

    public void updateEvent(Event event) {
        if (event == null) {
            return;
        }

        // delete event from db
        dbHelper.updateEvent(event);
        // delete previous time slots of the event
        deleteTimeSlots(event);
        // create time slots
        event.createTimeSlots((GregorianCalendar) event.getStartTime(), (GregorianCalendar) event.getEndTime());
        updateTimeSlots(event);
    }

    /**
     * To see how many timeSlots are there on each day,
     * timeSlots are grouped by date and saved into the map
     * @param event
     */
    private void updateTimeSlots(Event event) {
        List<TimeSlot> timeSlots = event.getTimeSlots();
        for (TimeSlot timeSlot : timeSlots) {
            // Prepare a key. The key will be yyyy-MM-dd to retrieve the each timeSlots on that day
            Calendar startTime = timeSlot.getStartTime();
            String dateKey = Utils.makeKeyForDate(startTime);
            List<TimeSlot> existingTimeSlots = this.timeSlots.get(dateKey);
            if (existingTimeSlots == null || existingTimeSlots.size() == 0) {
                existingTimeSlots = new ArrayList<>();
            }
            existingTimeSlots.add(timeSlot);
            // Sort the items based on the start time
            Collections.sort(existingTimeSlots);
            this.timeSlots.put(dateKey, existingTimeSlots);
        }

        events.put(event.getEventId(), event);
    }

    /**
     * Remove the previously set time slots
     * @param event
     */
    private void deleteTimeSlots(Event event) {
        List<TimeSlot> timeSlots = event.getTimeSlots();
        if (timeSlots == null || timeSlots.size() == 0) {
            return;
        }

        for (TimeSlot timeSlot : timeSlots) {
            // Prepare a key. The key will be yyyy-MM-dd to retrieve the each timeSlots on that day
            Calendar startTime = timeSlot.getStartTime();
            String dateKey = Utils.makeKeyForDate(startTime);
            List<TimeSlot> existingTimeSlots = this.timeSlots.get(dateKey);
            if (existingTimeSlots == null || existingTimeSlots.size() == 0) {
                return;
            }

            existingTimeSlots.remove(timeSlot);
            this.timeSlots.put(dateKey, existingTimeSlots);
        }
    }

    public void deleteEvent(Event event) {
        if (event == null) {
            return;
        }

        // delete event from db
        dbHelper.deleteEvent(event.getEventId());
        // delete time slots of the event
        deleteTimeSlots(event);
        // delete event
        events.remove(event.getEventId());
    }

    public void closeDb() {
        dbHelper.closeDatabase();
    }
}
