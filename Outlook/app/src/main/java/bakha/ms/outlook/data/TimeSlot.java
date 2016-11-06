package bakha.ms.outlook.data;

import java.util.Calendar;

/**
 * If the add_event covers more than one day,
 * we can split the add_event to the time slots.
 * Each time slot represents the add_event on a single day
 */

public class TimeSlot implements Comparable<TimeSlot>{
    private int eventId;
    private int timeSlotId;
    private Calendar startTime;
    private Calendar endTime;

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(int timeSlotId) {
        this.timeSlotId = timeSlotId;
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

    @Override
    public int compareTo(TimeSlot timeSlot) {
        return (int) (this.getStartTime().getTimeInMillis() - timeSlot.getStartTime().getTimeInMillis());
    }
}
