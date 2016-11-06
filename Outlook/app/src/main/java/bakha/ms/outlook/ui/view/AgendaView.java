package bakha.ms.outlook.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

public class AgendaView extends ExpandableListView {
    public AgendaView(Context context) {
        super(context);
    }

    public AgendaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AgendaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
