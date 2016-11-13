package bakha.ms.outlook.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;

import bakha.ms.outlook.data.OutlookManager;
import bakha.ms.outlook.ui.R;

public class AgendaView extends RelativeLayout implements AbsListView.OnScrollListener, View.OnTouchListener {
    private static final String TAG = "AgendaView";
    private ExpandableListView mAgendaListView;
    private AgendaViewAdapter mAgendaViewAdapter;
    private AgendaViewGroup mHeaderView;
    private DateChangeListener mDateChangeListener;
    private CalendarExpandListener mExpandListener;
    private OutlookManager mOutlookManager;
    private int mChildHeight;
    private int mGroupViewHeight;
    private float mPrevTouchY;
    private enum Direction {UP, DOWN, NONE};
    private Direction mDirection;

    public AgendaView(Context context) {
        super(context);
        createViews(context, null);
    }

    public AgendaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createViews(context, attrs);
    }

    public AgendaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        createViews(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Creates expandable list view associated with this agenda view
     * Adapter for expandable list view
     * Floating header view
     * @param context
     * @param attrs
     */
    private void createViews(Context context, AttributeSet attrs) {
        if (mAgendaListView == null) {
            mOutlookManager = OutlookManager.getInstance(context);
            mAgendaListView = new ExpandableListView(context, attrs);
            mAgendaListView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mAgendaListView.setSmoothScrollbarEnabled(false);
            mAgendaListView.setGroupIndicator(null);
            mAgendaListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            mAgendaListView.setOnScrollListener(this);
            mAgendaListView.setOnTouchListener(this);
            mAgendaListView.setFastScrollEnabled(false);
            mAgendaListView.setDivider(getResources().getDrawable(android.R.drawable.divider_horizontal_dark));
            addView(mAgendaListView);

            mAgendaViewAdapter = new AgendaViewAdapter(context);
            mAgendaListView.setAdapter(mAgendaViewAdapter);

            LayoutInflater inflater = LayoutInflater.from(context);
            mHeaderView = (AgendaViewGroup) inflater.inflate(R.layout.agenda_view_group, null, false);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mHeaderView.setGravity(ALIGN_PARENT_TOP);
            mHeaderView.setLayoutParams(layoutParams);
            mHeaderView.findViewById(R.id.shadow_view_top).setVisibility(VISIBLE);
            mHeaderView.findViewById(R.id.shadow_view_bottom).setVisibility(VISIBLE);
            addView(mHeaderView, layoutParams);

            int groupCount = mAgendaViewAdapter.getGroupCount();
            for (int i = 0; i < groupCount; i++) {
                mAgendaListView.expandGroup(i);
            }

            mDirection = Direction.NONE;
            mPrevTouchY = 0;
        }
    }

    /**
     * Restore the header view to the top
     */
    private void restoreHeader() {
        mHeaderView.setTranslationY(0);
    }

    /**
     * Update header view text with date from position
     * @param position
     */
    private void updateHeader(int position) {
        mAgendaViewAdapter.setTextForView(mHeaderView.getTextView(), position);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                if (mPrevTouchY != 0){
                    if (y < mPrevTouchY) {
                        mDirection = Direction.UP;
                    } else {
                        mDirection = Direction.DOWN;
                    }
                }
                mPrevTouchY = y;
                break;
            case MotionEvent.ACTION_UP:
                mPrevTouchY = 0;
                break;
        }
        return false;
    }


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        switch (view.getId()) {
            case R.id.agenda_view:
                if (mAgendaListView.getChildAt(0) != null && mAgendaListView.getChildAt(1) != null) {
                    View currentItem = mAgendaListView.getChildAt(0);
                    View nextItem = mAgendaListView.getChildAt(1);

                    AgendaViewType nextItemType = ((IAgendaView) nextItem).getType();
                    AgendaViewType currentItemType = ((IAgendaView) currentItem).getType();

                    if (currentItem instanceof IAgendaView) {
                        if (currentItemType == AgendaViewType.GROUP_VIEW) {
                            if (mGroupViewHeight == 0) {
                                mGroupViewHeight = currentItem.getHeight();
                            }
                        }
                    }

                    int childTop = 0;
                    if (currentItemType == AgendaViewType.CHILD_VIEW) {
                        childTop = currentItem.getTop();
                    }

                    /*** When scrolling UP ***/
                    if (mDirection == Direction.UP) {
                        if (currentItem instanceof IAgendaView) {
                            if (currentItemType == AgendaViewType.GROUP_VIEW) {
                                mChildHeight = nextItem.getHeight();
                            } else {
                                mChildHeight = currentItem.getHeight();
                                if (nextItemType == AgendaViewType.CHILD_VIEW) {
                                    mChildHeight = mChildHeight + nextItem.getHeight();
                                }
                            }
                        }
                    } else if (mDirection == Direction.DOWN) {
                        /*** When scrolling DOWN ***/
                        if (currentItem instanceof IAgendaView) {
                            if (currentItemType == AgendaViewType.CHILD_VIEW) {
                                mChildHeight = currentItem.getHeight();
                                if (nextItemType == AgendaViewType.CHILD_VIEW) {
                                    mChildHeight = mChildHeight + nextItem.getHeight();
                                }
                            }
                        }
                    }

                    int childVisibleHeight = mChildHeight + childTop;
                    int headerOffset = childVisibleHeight - mGroupViewHeight;
                    // avoid over scrolling for header
                    if (headerOffset < 0) {
                        mHeaderView.setTranslationY(headerOffset);
                        if (currentItemType == AgendaViewType.CHILD_VIEW) {
                            if (nextItemType == AgendaViewType.GROUP_VIEW) {
                                int nextItemPosition = ((AgendaViewGroup) nextItem).getPosition();
                                updateHeader(nextItemPosition - 1);
                            }
                        }
                    } else {
                        mHeaderView.setTranslationY(0);
                        if (currentItemType == AgendaViewType.GROUP_VIEW) {
                            int currentItemPosition = ((AgendaViewGroup) currentItem).getPosition();
                            updateHeader(currentItemPosition);
                            restoreHeader();
                        }
                    }

                    /***** update calendar view and update other listeners for the date change *****/
                    if (currentItem instanceof IAgendaView) {
                        if (currentItemType == AgendaViewType.GROUP_VIEW) {
                            int position = ((IAgendaView) currentItem).getPosition();
                            mDateChangeListener.currentDateChanged(R.id.agenda_view, position);
                            mOutlookManager.setPosition(position);
                        }
                    }
                }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (view.getId()) {
            case R.id.agenda_view:
                if (scrollState == SCROLL_STATE_IDLE) {
                    mDirection = Direction.NONE;
                } else if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    mExpandListener.requestCollapse();
                }
            break;
        }
    }

    public void setExpandListener(CalendarExpandListener expandListener) {
        mExpandListener = expandListener;
    }

    public void setDateChangeListener(DateChangeListener dateChangeListener) {
        mDateChangeListener = dateChangeListener;
    }

    public void setSelectedGroup(int selectedGroup) {
        mAgendaListView.setSelectedGroup(selectedGroup);
        updateHeader(selectedGroup);
        restoreHeader();
    }

    public void refresh() {
        if (mAgendaViewAdapter != null) {
            mAgendaViewAdapter.refresh();
            mAgendaViewAdapter.notifyDataSetChanged();
        }
    }
}
