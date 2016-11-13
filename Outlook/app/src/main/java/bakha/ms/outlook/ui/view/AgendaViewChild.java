package bakha.ms.outlook.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class AgendaViewChild extends LinearLayout implements IAgendaView {
    private int position;
    private boolean hasContent;

    public AgendaViewChild(Context context) {
        super(context);
    }

    public AgendaViewChild(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AgendaViewChild(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public AgendaViewType getType() {
        return AgendaViewType.CHILD_VIEW;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
    }

    public boolean hasContent() {
        return hasContent;
    }

    public void setHasContent(boolean hasContent) {
        this.hasContent = hasContent;
    }
}
