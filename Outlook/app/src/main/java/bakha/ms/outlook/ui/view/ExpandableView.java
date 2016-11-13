package bakha.ms.outlook.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import bakha.ms.outlook.ui.R;

public class ExpandableView extends LinearLayout{
    private boolean mIsCollapsed;

    public ExpandableView(Context context) {
        super(context);
    }

    public ExpandableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void expand() {
        final int targetHeight = (int) getResources().getDimension(R.dimen.calendar_max_height);
        final int initialHeight = (int) getResources().getDimension(R.dimen.calendar_min_height);

        Animation animation = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    getLayoutParams().height = targetHeight;
                    mIsCollapsed = false;
                } else {
                    // intermediate height will be the initial height + the gap * interpolated time
                    getLayoutParams().height = initialHeight + (int)((targetHeight - initialHeight) * interpolatedTime);
                }
                requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration((int)(targetHeight / getContext().getResources().getDisplayMetrics().density) * 2);
        startAnimation(animation);
    }

    public void collapse() {
        final int targetHeight = (int) getResources().getDimension(R.dimen.calendar_min_height);
        final int initialHeight = (int) getResources().getDimension(R.dimen.calendar_max_height);
        Animation animation = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    getLayoutParams().height = targetHeight;
                    mIsCollapsed = true;
                } else {
                    // intermediate height will be the initial height - the gap * interpolated time
                    getLayoutParams().height = initialHeight - (int)((initialHeight - targetHeight) * interpolatedTime);
                }
                requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration((int)(targetHeight / getContext().getResources().getDisplayMetrics().density) * 2);
        startAnimation(animation);
    }

    public boolean isCollapsed() {
        return mIsCollapsed;
    }

    public void setCollapsed(boolean isCollapsed) {
        mIsCollapsed = isCollapsed;
    }
}

