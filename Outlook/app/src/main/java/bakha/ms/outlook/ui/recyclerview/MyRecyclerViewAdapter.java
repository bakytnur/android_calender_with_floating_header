package bakha.ms.outlook.ui.recyclerview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import bakha.ms.outlook.data.Event;
import bakha.ms.outlook.data.OutlookManager;
import bakha.ms.outlook.data.TimeSlot;
import bakha.ms.outlook.data.helper.Utils;
import bakha.ms.outlook.ui.R;
import bakha.ms.outlook.ui.view.DateChangeListener;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.CustomViewHolder> {
    private final OutlookManager mOutlookManager;
    private List<Calendar> mCalendarDateList;
    private Map<Integer, Event> mEvents;
    private Context mContext;
    private Map<String, List<TimeSlot>> mTimeSlots;
    private String mMonthsInFull[];
    private DateChangeListener mDateChangeListener;
    private Drawable mPreviousBackground;

    public MyRecyclerViewAdapter(Context context, DateChangeListener dateChangeListener) {
        mOutlookManager = OutlookManager.getInstance(context);
        mCalendarDateList = mOutlookManager.getCalendarDateList();
        mEvents = mOutlookManager.getEvents();
        mContext = context;
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

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.calendar_grid_item, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        int height = viewGroup.getMeasuredHeight() / 2;
        view.setMinimumHeight(height);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int position) {
        Calendar feedItem = mCalendarDateList.get(position);

        int selectedPosition = mOutlookManager.getPosition();
        Calendar selectedDate = mOutlookManager.getCalendarDateList().get(selectedPosition);
        Calendar todayDate = mOutlookManager.getTodayDate();

        customViewHolder.monthTextView.setVisibility(View.GONE);
        customViewHolder.yearTextView.setVisibility(View.GONE);
        customViewHolder.eventIndicator.setVisibility(View.GONE);
        customViewHolder.yearTextView.setTextColor(mContext.getResources().getColor(R.color.colorGridText));
        customViewHolder.monthTextView.setTextColor(mContext.getResources().getColor(R.color.colorGridText));
        customViewHolder.dayTextView.setTextColor(mContext.getResources().getColor(android.R.color.darker_gray));

        final Calendar calendarDate = mCalendarDateList.get(position);
        int dayOfMonth = calendarDate.get(Calendar.DAY_OF_MONTH);
        int month = calendarDate.get(Calendar.MONTH);
        int year = calendarDate.get(Calendar.YEAR);

        customViewHolder.dayTextView.setText(String.valueOf(dayOfMonth));
        customViewHolder.monthTextView.setText(mMonthsInFull[month]);
        customViewHolder.yearTextView.setText(String.valueOf(year));

        if (month % 2 == 0) {
            //customViewHolder.setBackgroundColor(mContext.getResources().getColor(R.color.colorGridBg));
        } else {
            //customViewHolder.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
        }

        // check if there is an event on this day
        Calendar calendar = mCalendarDateList.get(position);
        String dateKey = Utils.makeKeyForDate(calendar);
        List<TimeSlot> timeSlots = mTimeSlots.get(dateKey);
        if (timeSlots != null && timeSlots.size() > 0) {
            customViewHolder.eventIndicator.setVisibility(View.VISIBLE);
        }

       // mPreviousBackground = customViewHolder.getBackground();
        customViewHolder.circleLayout.setBackground(mPreviousBackground);

        if (todayDate.compareTo(calendarDate) == 0) {
            customViewHolder.dayTextView.setTextColor(mContext.getResources().getColor(R.color.colorGridText));
        }

        if (calendarDate.compareTo(selectedDate) == 0) {
            customViewHolder.dayTextView.setTextColor(mContext.getResources().getColor(android.R.color.white));
            customViewHolder.circleLayout.setBackground(mContext.getResources().getDrawable(R.drawable.current_sell_background));
        } else if (dayOfMonth == 1) {
            customViewHolder.dayTextView.setTextColor(mContext.getResources().getColor(R.color.colorGridText));
            customViewHolder.monthTextView.setVisibility(View.VISIBLE);
            if (year != todayDate.get(Calendar.YEAR)) {
                customViewHolder.yearTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mCalendarDateList.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView monthTextView;
        protected TextView dayTextView;
        protected TextView yearTextView;
        protected RelativeLayout circleLayout;
        protected View eventIndicator;

        public CustomViewHolder(View view) {
            super(view);
            monthTextView = (TextView) view
                    .findViewById(R.id.month_text_view);
            dayTextView = (TextView) view
                    .findViewById(R.id.day_text_view);
            yearTextView = (TextView) view
                    .findViewById(R.id.year_text_view);
            circleLayout = (RelativeLayout) view
                    .findViewById(R.id.circle_layout);
            eventIndicator = view.findViewById(R.id.event_indicator);
        }
    }
}