package bakha.ms.outlook.ui.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import bakha.ms.outlook.data.OutlookManager;
import bakha.ms.outlook.data.TimeSlot;
import bakha.ms.outlook.data.helper.Utils;
import bakha.ms.outlook.ui.R;

public class CalendarRecyclerViewAdapter extends RecyclerView.Adapter<CalendarRecyclerViewAdapter.DateViewHolder> {
    private final OutlookManager mOutlookManager;
    private List<Calendar> mCalendarDateList;
    private Context mContext;
    private Map<String, List<TimeSlot>> mTimeSlots;
    private String mMonthsInFull[];
    private DateChangeListener mDateChangeListener;
    private Drawable mPreviousBackground;

    public CalendarRecyclerViewAdapter(Context context, DateChangeListener dateChangeListener) {
        mOutlookManager = OutlookManager.getInstance(context);
        mCalendarDateList = mOutlookManager.getCalendarDateList();
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

    private void notifyCurrentDateChanged(int viewId, int position) {
        mDateChangeListener.currentDateChanged(viewId, position);
    }

    @Override
    public DateViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.calendar_grid_item, null);
        DateViewHolder viewHolder = new DateViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DateViewHolder dateViewHolder, int position) {
        int selectedPosition = mOutlookManager.getPosition();
        Calendar selectedDate = mOutlookManager.getCalendarDateList().get(selectedPosition);
        Calendar todayDate = mOutlookManager.getTodayDate();

        dateViewHolder.monthTextView.setVisibility(View.GONE);
        dateViewHolder.yearTextView.setVisibility(View.GONE);
        dateViewHolder.eventIndicator.setVisibility(View.GONE);
        dateViewHolder.yearTextView.setTextColor(mContext.getResources().getColor(R.color.colorGridText));
        dateViewHolder.monthTextView.setTextColor(mContext.getResources().getColor(R.color.colorGridText));
        dateViewHolder.dayTextView.setTextColor(mContext.getResources().getColor(android.R.color.darker_gray));
        dateViewHolder.itemPosition = position;

        final Calendar calendarDate = mCalendarDateList.get(position);
        int dayOfMonth = calendarDate.get(Calendar.DAY_OF_MONTH);
        int month = calendarDate.get(Calendar.MONTH);
        int year = calendarDate.get(Calendar.YEAR);

        dateViewHolder.dayTextView.setText(String.valueOf(dayOfMonth));
        dateViewHolder.monthTextView.setText(mMonthsInFull[month]);
        dateViewHolder.yearTextView.setText(String.valueOf(year));

        if (month % 2 == 0) {
            dateViewHolder.setBackgroundColor(mContext.getResources().getColor(R.color.colorGridBg));
        } else {
            dateViewHolder.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
        }

        // check if there is an event on this day
        Calendar calendar = mCalendarDateList.get(position);
        String dateKey = Utils.makeKeyForDate(calendar);
        List<TimeSlot> timeSlots = mTimeSlots.get(dateKey);
        if (timeSlots != null && timeSlots.size() > 0) {
            dateViewHolder.eventIndicator.setVisibility(View.VISIBLE);
        }

        mPreviousBackground = dateViewHolder.getBackground();
        dateViewHolder.circleLayout.setBackground(mPreviousBackground);

        if (todayDate.compareTo(calendarDate) == 0) {
            dateViewHolder.dayTextView.setTextColor(mContext.getResources().getColor(R.color.colorGridText));
        }

        if (calendarDate.compareTo(selectedDate) == 0) {
            dateViewHolder.dayTextView.setTextColor(mContext.getResources().getColor(android.R.color.white));
            dateViewHolder.circleLayout.setBackground(mContext.getResources().getDrawable(R.drawable.current_sell_background));
        } else if (dayOfMonth == 1) {
            dateViewHolder.dayTextView.setTextColor(mContext.getResources().getColor(R.color.colorGridText));
            dateViewHolder.monthTextView.setVisibility(View.VISIBLE);
            if (year != todayDate.get(Calendar.YEAR)) {
                dateViewHolder.yearTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mCalendarDateList.size();
    }

    class DateViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView monthTextView;
        private TextView dayTextView;
        private TextView yearTextView;
        private RelativeLayout circleLayout;
        private LinearLayout outerLayout;
        private View eventIndicator;
        private int itemPosition;
        private Drawable background;

        public DateViewHolder(View view) {
            super(view);

            outerLayout = (LinearLayout) view.findViewById(R.id.outer_layout);
            monthTextView = (TextView) view
                    .findViewById(R.id.month_text_view);
            dayTextView = (TextView) view
                    .findViewById(R.id.day_text_view);
            yearTextView = (TextView) view
                    .findViewById(R.id.year_text_view);
            circleLayout = (RelativeLayout) view
                    .findViewById(R.id.circle_layout);
            eventIndicator = view.findViewById(R.id.event_indicator);
            background = outerLayout.getBackground();
            circleLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            notifyCurrentDateChanged(R.id.recycler_view, itemPosition);
            mOutlookManager.setPosition(itemPosition);
            notifyDataSetChanged();
        }

        public void setBackgroundColor(int backgroundColor) {
            outerLayout.setBackgroundColor(backgroundColor);
        }

        public Drawable getBackground() {
            return background;
        }
    }
}