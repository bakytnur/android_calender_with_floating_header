package bakha.ms.outlook.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import bakha.ms.outlook.ui.R;

public class AgendaViewGroup extends LinearLayout implements IAgendaView {
    private int position;

    public AgendaViewGroup(Context context) {
        super(context);
    }

    public AgendaViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AgendaViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AgendaViewType getType() {
        return AgendaViewType.GROUP_VIEW;
    }

    public TextView getTextView() {
        TextView groupTextView = (TextView)findViewById(R.id.agenda_view_group_text);
        return groupTextView;
    }

    public void setTextView(TextView textView) {
        TextView groupTextView = (TextView)findViewById(R.id.agenda_view_group_text);
        groupTextView.setText(textView.getText());
        groupTextView.setTextColor(textView.getTextColors());
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
    }
}
