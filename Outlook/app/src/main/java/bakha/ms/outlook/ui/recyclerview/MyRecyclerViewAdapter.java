package bakha.ms.outlook.ui.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import bakha.ms.outlook.data.Event;
import bakha.ms.outlook.data.OutlookManager;
import bakha.ms.outlook.data.helper.Utils;
import bakha.ms.outlook.ui.R;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.CustomViewHolder> {
    private final OutlookManager mOutlookManager;
    private List<Calendar> mCalendarList;
    private Map<Integer, Event> mEvents;
    private Context mContext;

    public MyRecyclerViewAdapter(Context context) {
        mOutlookManager = OutlookManager.getInstance(context);
        mCalendarList = mOutlookManager.getCalendarDateList();
        mEvents = mOutlookManager.getEvents();
        mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.agenda_view_group, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        Calendar feedItem = mCalendarList.get(i);

        customViewHolder.textView.setText(Utils.getFormattedDateTime(feedItem));
    }

    @Override
    public int getItemCount() {
        if (mEvents == null || mEvents.size() == 0) {
            return (null != mCalendarList ? 2 * mCalendarList.size() : 0);
        }

        int itemCount = 0;
        int datesCount = mCalendarList.size();
        for (int position = 0; position < datesCount; position ++) {
            Event event = mEvents.get(position);
            if (event != null) {
                if (event.getTimeSlots() != null && event.getTimeSlots().size() > 0) {
                    itemCount = itemCount + event.getTimeSlots().size();
                } else {
                    // No event child
                    itemCount ++;
                }
            } else {
                // No event child
                itemCount ++;
            }

            // Group item
            itemCount++;
        }

        return itemCount;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView textView;

        public CustomViewHolder(View view) {
            super(view);
            this.textView = (TextView) view.findViewById(R.id.agenda_view_group_text);
        }
    }
}