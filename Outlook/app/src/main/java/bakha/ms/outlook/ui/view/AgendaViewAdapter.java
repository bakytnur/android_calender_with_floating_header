package bakha.ms.outlook.ui.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import bakha.ms.outlook.data.Event;
import bakha.ms.outlook.data.OutlookManager;
import bakha.ms.outlook.data.TimeSlot;
import bakha.ms.outlook.data.helper.Utils;
import bakha.ms.outlook.ui.AddEventActivity;
import bakha.ms.outlook.ui.R;
import bakha.ms.outlook.ui.ViewEventActivity;

public class AgendaViewAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "AgendaViewAdapter";
    private List<Calendar> mCalendarDateList;
    private Map<Integer, Event> mEvents;
    private Map<String, List<TimeSlot>> mTimeSlots;
    private OutlookManager mOutlookManager;
    private Context mContext;
    private String[] mWeekdays;
    private String[] mMonths;

    public AgendaViewAdapter(Context context) {
        mContext = context;
        mOutlookManager = OutlookManager.getInstance(mContext);
        mWeekdays = mContext.getResources().getStringArray(R.array.week_days);
        mMonths = mContext.getResources().getStringArray(R.array.months_in_full);
        refresh();
    }

    public void refresh() {
        mCalendarDateList = mOutlookManager.getCalendarDateList();
        mEvents = mOutlookManager.getEvents();
        mTimeSlots = mOutlookManager.getTimeSlots();
    }

    @Override
    public int getGroupCount() {
        return mCalendarDateList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Calendar calendar = mCalendarDateList.get(groupPosition);

        String dateKey = Utils.makeKeyForDate(calendar);
        List<TimeSlot> timeSlots = mTimeSlots.get(dateKey);
        if (timeSlots == null || timeSlots.size() == 0) {
            return 1;
        }

        return mTimeSlots.get(dateKey).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mCalendarDateList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 10000 * groupPosition +  childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.agenda_view_group, null);
        }

        ((AgendaViewGroup) convertView).setPosition(groupPosition);
        ExpandableListView agendaView = (ExpandableListView) parent;
        agendaView.expandGroup(groupPosition);

        TextView agendaViewGroup = (TextView) convertView
                .findViewById(R.id.agenda_view_group_text);
        setTextForView(agendaViewGroup, groupPosition);
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.agenda_view_child, null);
        }

        ((AgendaViewChild) convertView).setPosition(childPosition);
        ((AgendaViewChild) convertView).setHasContent(true);
        LinearLayout eventViewLayout = (LinearLayout) convertView
                .findViewById(R.id.event_view_layout);
        eventViewLayout.setVisibility(View.GONE);
        LinearLayout locationLayout = (LinearLayout) convertView.findViewById(R.id.location_layout);
        locationLayout.setVisibility(View.GONE);
        TextView agendaViewChild = (TextView) convertView
                .findViewById(R.id.agenda_view_child_text);
        agendaViewChild.setVisibility(View.GONE);
        TextView startTimeText = (TextView) convertView
                .findViewById(R.id.start_time_text);
        TextView durationText = (TextView) convertView
                .findViewById(R.id.duration_text);
        TextView eventTitleText = (TextView) convertView
                .findViewById(R.id.event_name_text);
        TextView eventLocationText = (TextView) convertView
                .findViewById(R.id.event_location_text);
        TextView eventIdText = (TextView) convertView.findViewById(R.id.event_id_text);

        Calendar calendar = mCalendarDateList.get(groupPosition);
        boolean hasEvent = false;

        // check if a time slot exists for this day
        String dateKey = Utils.makeKeyForDate(calendar);
        List<TimeSlot> timeSlots = mTimeSlots.get(dateKey);
        if (timeSlots != null && timeSlots.size() > 0) {
            TimeSlot timeSlot = timeSlots.get(childPosition);
            if (timeSlot != null) {
                Event event = mEvents.get(timeSlot.getEventId());
                eventIdText.setText(String.valueOf(event.getEventId()));
                hasEvent = true;
                eventViewLayout.setVisibility(View.VISIBLE);
                if (event.isFullDayEvent()) {
                    startTimeText.setText(R.string.all_day_event_short);
                    durationText.setText(event.getTimeSlots().size() + "d");
                } else {
                    startTimeText.setText(Utils.getFormattedTime(timeSlot.getStartTime()));
                    durationText.setText(Utils.getDifferenceInHour(timeSlot.getStartTime(), timeSlot.getEndTime()));
                }
                eventTitleText.setText(event.getTitle());
                if (event.getLocation() != null && event.getLocation().length() > 0) {
                    locationLayout.setVisibility(View.VISIBLE);
                    eventLocationText.setText(event.getLocation());
                }
            }
        }

        if (!hasEvent) {
            agendaViewChild.setVisibility(View.VISIBLE);
            ((AgendaViewChild) convertView).setHasContent(false);
        }

        convertView.setClickable(true);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                mOutlookManager.setPosition(groupPosition);
                Calendar calendar = mCalendarDateList.get(groupPosition);
                String dateKey = Utils.makeKeyForDate(calendar);

                if (mTimeSlots.get(dateKey) == null || mTimeSlots.get(dateKey).size() == 0) {
                    intent = new Intent(mContext, AddEventActivity.class);
                    intent.putExtra("mode", AddEventActivity.Mode.CREATE);
                } else {
                    TextView textView = (TextView)v.findViewById(R.id.event_id_text);
                    int eventId = Integer.valueOf(textView.getText().toString());
                    intent = new Intent(mContext, ViewEventActivity.class);
                    intent.putExtra("eventId", eventId);
                    intent.putExtra("childPosition", childPosition);
                }
                mContext.startActivity(intent);
            }
        });
        return convertView;
    }

    /**
     * Set formatted date text to textView
     * @param textView
     * @param position
     */
    public void setTextForView(TextView textView, int position) {
        Calendar calendarDate = mCalendarDateList.get(position);
        Calendar todayDate = mOutlookManager.getTodayDate();
        if (todayDate.compareTo(calendarDate) == 0) {
            textView.setTextColor(mContext.getResources().getColor(R.color.colorGridText));
        } else {
            textView.setTextColor(mContext.getResources().getColor(R.color.colorGroupText));
        }

        textView.setText(getProperDateString(calendarDate));
    }

    /**
     * Convert calendar date to proper readable text
     * @param calendar
     * @return
     */
    private String getProperDateString(Calendar calendar) {
        Calendar today = mOutlookManager.getTodayDate();
        StringBuilder stringBuilder = new StringBuilder();
        int diffInDays = Utils.getDifferenceInDays(today, calendar);
        if (diffInDays == 0) {
            stringBuilder
                    .append(mContext.getResources().getString(R.string.today))
                    .append(", ");
        } else if (diffInDays == 1) {
            stringBuilder
                    .append(mContext.getResources().getString(R.string.tomorrow))
                    .append(", ");
        } else if (diffInDays == -1) {
            stringBuilder
                    .append(mContext.getResources().getString(R.string.yesterday))
                    .append(", ");
        }

        stringBuilder
                .append(mWeekdays[calendar.get(Calendar.DAY_OF_WEEK) - 1])
                .append(", ")
                .append(mMonths[calendar.get(Calendar.MONTH)])
                .append(" ")
                .append(calendar.get(Calendar.DAY_OF_MONTH));
        if (today.get(Calendar.YEAR) != calendar.get(Calendar.YEAR))
            stringBuilder
                    .append(" ")
                    .append(calendar.get(Calendar.YEAR));

        String dateString = stringBuilder.toString();
        return dateString.toUpperCase();
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
