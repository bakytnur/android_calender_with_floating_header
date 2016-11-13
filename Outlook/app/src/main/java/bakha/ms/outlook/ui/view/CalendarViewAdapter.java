package bakha.ms.outlook.ui.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import bakha.ms.outlook.data.OutlookManager;
import bakha.ms.outlook.data.TimeSlot;
import bakha.ms.outlook.data.helper.Utils;
import bakha.ms.outlook.ui.R;

public class CalendarViewAdapter extends BaseAdapter {
    private Context mContext;
    private OutlookManager mOutlookManager;
    private List<Calendar> mCalendarDateList;
    private Map<String, List<TimeSlot>> mTimeSlots;
    private String mMonthsInFull[];
    private DateChangeListener mDateChangeListener;
    private Drawable mPreviousBackground;

    public CalendarViewAdapter(Context context, DateChangeListener dateChangeListener) {
        mContext = context;
        mOutlookManager = OutlookManager.getInstance(mContext);
        mDateChangeListener = dateChangeListener;
        initializeMonths();
        refresh();
    }

    public void refresh() {
        mCalendarDateList = mOutlookManager.getCalendarDateList();
        mTimeSlots = mOutlookManager.getTimeSlots();
    }

    public void initializeMonths() {
        mMonthsInFull = mContext.getResources().getStringArray(R.array.months_in_short);
    }

    private void notifyCurrentDateChanged(int viewId, int position) {
        mDateChangeListener.currentDateChanged(viewId, position);
    }

    @Override
    public int getCount() {
        return mCalendarDateList.size();
    }

    @Override
    public Object getItem(int position) {
        return mCalendarDateList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.calendar_grid_item, parent, false);
        }

        int selectedPosition = mOutlookManager.getPosition();
        Calendar selectedDate = mOutlookManager.getCalendarDateList().get(selectedPosition);
        Calendar todayDate = mOutlookManager.getTodayDate();
        TextView monthTextView = (TextView) convertView
                .findViewById(R.id.month_text_view);
        TextView dayTextView = (TextView) convertView
                .findViewById(R.id.day_text_view);
        TextView yearTextView = (TextView) convertView
                .findViewById(R.id.year_text_view);
        RelativeLayout circleLayout = (RelativeLayout) convertView
                .findViewById(R.id.circle_layout);
        View eventIndicator = convertView.findViewById(R.id.event_indicator);

        monthTextView.setVisibility(View.GONE);
        yearTextView.setVisibility(View.GONE);
        eventIndicator.setVisibility(View.GONE);
        yearTextView.setTextColor(mContext.getResources().getColor(R.color.colorGridText));
        monthTextView.setTextColor(mContext.getResources().getColor(R.color.colorGridText));
        dayTextView.setTextColor(mContext.getResources().getColor(android.R.color.darker_gray));

        final Calendar calendarDate = mCalendarDateList.get(position);
        int dayOfMonth = calendarDate.get(Calendar.DAY_OF_MONTH);
        int month = calendarDate.get(Calendar.MONTH);
        int year = calendarDate.get(Calendar.YEAR);

        dayTextView.setText(String.valueOf(dayOfMonth));
        monthTextView.setText(mMonthsInFull[month]);
        yearTextView.setText(String.valueOf(year));

        if (month % 2 == 0) {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.colorGridBg));
        } else {
            convertView.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
        }

        // check if there is an event on this day
        Calendar calendar = mCalendarDateList.get(position);
        String dateKey = Utils.makeKeyForDate(calendar);
        List<TimeSlot> timeSlots = mTimeSlots.get(dateKey);
        if (timeSlots != null && timeSlots.size() > 0) {
            eventIndicator.setVisibility(View.VISIBLE);
        }

        mPreviousBackground = convertView.getBackground();
        circleLayout.setBackground(mPreviousBackground);

        if (todayDate.compareTo(calendarDate) == 0) {
            dayTextView.setTextColor(mContext.getResources().getColor(R.color.colorGridText));
        }

        if (calendarDate.compareTo(selectedDate) == 0) {
            dayTextView.setTextColor(mContext.getResources().getColor(android.R.color.white));
            circleLayout.setBackground(mContext.getResources().getDrawable(R.drawable.current_sell_background));
        } else if (dayOfMonth == 1) {
            dayTextView.setTextColor(mContext.getResources().getColor(R.color.colorGridText));
            monthTextView.setVisibility(View.VISIBLE);
            if (year != todayDate.get(Calendar.YEAR)) {
                yearTextView.setVisibility(View.VISIBLE);
            }
        }

        convertView.setClickable(true);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOutlookManager.setPosition(position);
                notifyCurrentDateChanged(R.id.outlook_calendar_view, (int) getItemId(position));
                notifyDataSetChanged();
            }
        });

        return convertView;
    }
}
