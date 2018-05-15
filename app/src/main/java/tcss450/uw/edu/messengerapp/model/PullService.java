package tcss450.uw.edu.messengerapp.model;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import tcss450.uw.edu.messengerapp.HomeActivity;
import tcss450.uw.edu.messengerapp.R;

public class PullService extends IntentService {


    public static final String RECEIVED_UPDATE = "new show from phish.net!";

    //60 seconds - 1 minute is the minimum...
    private static final int POLL_INTERVAL = 60_000;

    private static final String TAG = "PullService";

    public PullService() {
        super("PullService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d(TAG, "Performing the service");
            checkWebservice(intent.getBooleanExtra(getString(R.string.keys_is_foreground), false));
        }
    }

    public static void startServiceAlarm(Context context, boolean isInForeground) {
        Intent i = new Intent(context, PullService.class);
        i.putExtra(context.getString(R.string.keys_is_foreground), isInForeground);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int startAfter = isInForeground ? POLL_INTERVAL : POLL_INTERVAL * 2;

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP
                , startAfter
                , POLL_INTERVAL, pendingIntent);
        Log.e(TAG, "starting service");
    }

    public static void stopServiceAlarm(Context context) {
        Intent i = new Intent(context, PullService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        Log.e(TAG, "stopping service");
    }


        private boolean checkWebservice(boolean isInForeground) {
        //check a webservice in the background...
        Uri retrieve = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_phish_net_url))
                .appendPath(getString(R.string.ep_phish_version))
                .appendPath(getString(R.string.ep_phish_setlist))
                .appendPath(getString(R.string.ep_phish_random))
                .appendQueryParameter(getString(R.string.ep_api_key_arg),
                        getString(R.string.ep_my_api_key))
                .build();
/*
        Uri retrieve = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_phish))
                .appendQueryParameter("apikey", getString(R.string.ep_my_api_key))
                .build();
*/
        StringBuilder response = new StringBuilder();
        HttpURLConnection urlConnection = null;

        //go out and ask for new messages
        response = new StringBuilder();
        try {

            Log.wtf(TAG, retrieve.toString());

            URL urlObject = new URL(retrieve.toString());
            urlConnection = (HttpURLConnection) urlObject.openConnection();
            InputStream content = urlConnection.getInputStream();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String s;
            while ((s = buffer.readLine()) != null) {
                response.append(s);
            }

        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        if (isInForeground) {
            Intent i = new Intent(RECEIVED_UPDATE);
            //add bundle to send the response to any receivers
            i.putExtra(getString(R.string.keys_extra_results), response.toString());
            sendBroadcast(i);
        } else {
            buildNotification(response.toString());
        }
        return true;
    }



    private void buildNotification(String s) {
        //IMPORT V4 not V7
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_temp_icon)
                        .setContentTitle("New Notification!")
                        .setContentText("Click to open the app!");

        // Creates an Intent for the Activity
        Intent notifyIntent =
                new Intent(this, HomeActivity.class);
        Bundle bundle = new Bundle();
        notifyIntent.putExtra(getString(R.string.keys_extra_results), s);

        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Puts the PendingIntent into the notification builder
        mBuilder.setContentIntent(notifyPendingIntent);
        mBuilder.setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }




}
