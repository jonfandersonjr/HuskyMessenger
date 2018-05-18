package tcss450.uw.edu.messengerapp.model;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import tcss450.uw.edu.messengerapp.HomeActivity;
import tcss450.uw.edu.messengerapp.R;

public class PullService extends IntentService {


    public static final String RECEIVED_UPDATE = "New Message!";

    //60 seconds - 1 minute is the minimum...
    private static final int POLL_INTERVAL = 60_000;

    private static final String TAG = "PullService";

    private static String mUsername = "";

    private boolean isInForeground;

    private ListenManager mListenManager;

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

    /**
     * Makes call to web service and sends foreground notifications
     * @param inForeground
     * @return
     */
    private boolean checkWebservice(boolean inForeground) {

        isInForeground = inForeground;

        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_all_chats))
                .build();

        JSONObject msg = new JSONObject();
        try {
            msg.put("username", mUsername);
        } catch (JSONException e) {
            Log.wtf("JSON EXCEPTION", e.toString());
        }

        Log.e(TAG, "Sending getAllChats request");
        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleGetChatsOnPre)
                .onPostExecute(this::handleGetChatsOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();




/*

        StringBuilder response = new StringBuilder();
        HttpURLConnection urlConnection = null;

        //go out and ask for new messages

        try {
            Log.wtf(TAG, retrieve.toString());

            /*
            mListenManager = new ListenManager.Builder(retrieve.toString(), this::publishRequests)
                    .setExceptionHandler(this::handleError)
                    .setDelay(5000)
                    .build();


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
            Log.wtf("****TEST****", response.toString());
            i.putExtra(getString(R.string.keys_extra_results), response.toString());
            sendBroadcast(i);
        } else {
            buildNotification(response.toString());
        }

        */

        return true;
    }

    /**
     * Prepares a notification if the app is in the background.
     * @param s
     */
    private void buildNotification(String s) {
        //IMPORT V4 not V7
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_temp_icon)
                        .setContentTitle("New Message from " + s + "!")
                        .setContentText("Click to chat!");

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

    public void handleGetChatsOnPre() {

    }

    public void handleGetChatsOnPost(String result) {
        Log.e(TAG, "Inside getAllChats request post");

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_chats))) {
                    try {
                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_chats));
                        for (int i = 0; i < jReqs.length(); i++) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = new Date();
                            date.setMinutes(date.getMinutes()-1);
                            String currentDateTime = dateFormat.format(date);

                            int chatid = jReqs.getJSONObject(i).getInt("chatid");

                            //build the web service URL
                            Uri uri = new Uri.Builder()
                                    .scheme("https")
                                    .appendPath(getString(R.string.ep_base_url))
                                    .appendPath(getString(R.string.ep_post_get_messages))
                                    .build();

                            JSONObject msg = new JSONObject();
                            try {
                                msg.put("chatId", chatid);
                                msg.put("after", currentDateTime);
                            } catch (JSONException e) {
                                Log.wtf("JSON EXCEPTION", e.toString());
                            }


                            Log.e(TAG, "Sending getAllNewMessages request");

                            new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                                    .onPreExecute(this::handleGetMessagesOnPre)
                                    .onPostExecute(this::handleGetMessagesOnPost)
                                    .onCancelled(this::handleErrorsInTask)
                                    .build().execute();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result + System.lineSeparator() + e.getMessage());
        }
    }

    public void handleGetMessagesOnPre() {

    }

    public void handleGetMessagesOnPost(String result) {

        Log.e(TAG, "Inside getNewMessages post");

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            ArrayList<String> results = new ArrayList<String>();

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_messages))) {
                    try {
                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_messages));
                        for (int i = 0; i < jReqs.length(); i++) {
                            String messageFrom = jReqs.getJSONObject(i).getString("username");

                            if (messageFrom != mUsername) {
                                if (isInForeground) {
                                    Log.e(TAG, "Inside app sending notification");
                                    Intent intent = new Intent(RECEIVED_UPDATE);
                                    //add bundle to send the response to any receivers
                                    Log.wtf("****TEST****", messageFrom);
                                    intent.putExtra(getString(R.string.keys_extra_results), messageFrom);
                                    sendBroadcast(intent);
                                } else {
                                    Log.e(TAG, "Out of app building notification");
                                    buildNotification(messageFrom);
                                }
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result + System.lineSeparator() + e.getMessage());
        }
    }

    public void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }


    public static void setUsername(final String theString) {
        mUsername = theString;
    }


}
