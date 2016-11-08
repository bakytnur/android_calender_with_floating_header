package bakha.ms.outlook.ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;
import java.util.GregorianCalendar;

import bakha.ms.outlook.data.AlertType;
import bakha.ms.outlook.data.helper.Utils;

public class OutlookNotifyService extends Service {
        public OutlookNotifyService() {
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        GregorianCalendar date = (GregorianCalendar)intent.getSerializableExtra("date");
        AlertType alertType = (AlertType) intent.getSerializableExtra("alert");
        Calendar now = Calendar.getInstance();
        int diff = Math.abs(Utils.getDifferenceInMinutes(now, date));

        // show notification if the time difference is about 2 minutes
        boolean isOnTimeAlert = alertType == AlertType.ON_TIME && diff >=0 && diff <=2;
        if (date.compareTo(now) >= 0 || isOnTimeAlert) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_alarm_white_24dp)
                            .setContentTitle(intent.getStringExtra("title"))
                            .setContentText(Utils.getFormattedDateTime(date));

            Intent resultIntent = new Intent(this, OutlookActivity.class);
            resultIntent.putExtra("date", date);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            mBuilder.setContentIntent(resultPendingIntent);
            // Sets an ID for the notification
            int mNotificationId = 001;
            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
