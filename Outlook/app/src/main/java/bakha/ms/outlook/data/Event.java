package bakha.ms.outlook.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import bakha.ms.outlook.data.helper.Utils;

public class Event {
    private int eventId;
    private String title;
    private boolean isFullDayEvent;
    private String location;
    private String description;
    private Calendar startTime;
    private Calendar endTime;
    private AlertType alertType;
    private Account account;
    private List<TimeSlot> timeSlots;

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isFullDayEvent() {
        return isFullDayEvent;
    }

    public void setFullDayEvent(boolean fullDayEvent) {
        isFullDayEvent = fullDayEvent;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(List<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots;
    }

    /**
     * Divides the add_event to the different time slots as each time slot represents
     * the incident on a single. So, the events are separated by a daily slots
     * @param startTime
     * @param endTime
     */
    public void createTimeSlots(GregorianCalendar startTime, GregorianCalendar endTime) {
        List<TimeSlot> timeSlots = new ArrayList<>();

        GregorianCalendar endOfDay = startTime;

        // check if start and end time of the add_event at the same day
        while (!Utils.compareDates(endOfDay, endTime)){
            TimeSlot timeSlot = new TimeSlot();
            timeSlot.setEventId(eventId);
            timeSlot.setStartTime(startTime);

            endOfDay = new GregorianCalendar(
                    startTime.get(Calendar.YEAR),
                    startTime.get(Calendar.MONTH),
                    startTime.get(Calendar.DAY_OF_MONTH),
                    23, 59, 59
            );

            timeSlot.setEndTime(endOfDay);
            timeSlots.add(timeSlot);

            // shift to the next day after 23:59:59
            endOfDay.add(Calendar.MINUTE, 1);
            startTime = endOfDay;
        }

        // add last time slot
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setEventId(eventId);
        timeSlot.setStartTime(startTime);
        timeSlot.setEndTime(endTime);

        timeSlots.add(timeSlot);
        setTimeSlots(timeSlots);
    }
}
