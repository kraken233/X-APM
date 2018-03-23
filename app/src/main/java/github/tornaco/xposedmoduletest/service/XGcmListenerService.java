package github.tornaco.xposedmoduletest.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

import org.newstand.logger.Logger;

import java.util.concurrent.atomic.AtomicInteger;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.model.PushMessage;
import github.tornaco.xposedmoduletest.ui.activity.NavigatorActivityBottomNav;
import github.tornaco.xposedmoduletest.util.OSUtil;

/**
 * Created by Tornaco on 2018/3/23 11:56.
 * God bless no bug!
 */


// https://firebase.google.com/docs/cloud-messaging/?authuser=0
// https://github.com/googlesamples/google-services/tree/master/android/gcm/app/src/main/java/gcm/play/android/samples/com/gcmquickstart
public class XGcmListenerService extends GcmListenerService {

    private static final AtomicInteger NOTIFICATION_ID = new AtomicInteger(0);

    private static final String NOTIFICATION_CHANNEL = "github.tornaco.notification.channel.gcm";

    @Override
    public void onMessageReceived(String from, Bundle data) {

        Bundle messageBundle = data.getBundle("notification");

        String message = null;
        if (messageBundle != null) {
            message = messageBundle.getString(PushMessage.DATA_SCHEMA_FIREBASE_BODY);
        }

        Logger.d("onMessageReceived, data: " + data);
        Logger.d("onMessageReceived, messageBundle: " + messageBundle);
        Logger.d("onMessageReceived, from: " + from);
        Logger.d("onMessageReceived, message: " + message);

        PushMessage pushMessage = PushMessage.fromJson(message);
        Logger.d("onMessageReceived, pushMessage: " + pushMessage);

        if (pushMessage != null) {
            sendNotification(pushMessage);
        }
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(PushMessage message) {

        boolean test = message.isTest();
        // Do not show if this is a test and it is user build.
        if (test && !BuildConfig.DEBUG) {
            Logger.w("Skip send notification for user build.");
            return;
        }

        createNotificationChannelForO();

        Intent intent = new Intent(this, NavigatorActivityBottomNav.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_notification_message)
                .setContentTitle(message.getTitle())
                .setContentText(message.getMessage())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID.incrementAndGet(), notificationBuilder.build());
            Logger.w("Send notification for push message.");
        }
    }

    void createNotificationChannelForO() {
        if (OSUtil.isOOrAbove()) {
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(
                            Context.NOTIFICATION_SERVICE);
            NotificationChannel nc = null;
            if (notificationManager != null) {
                nc = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL);
            }
            if (nc != null) {
                return;
            }
            NotificationChannel notificationChannel;
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL,
                    "apm-c",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 200, 200});
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }
}
