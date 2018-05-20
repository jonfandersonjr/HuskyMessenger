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

    public static final String UPDATE = "UPDATE!";
    public static final String MESSAGE_UPDATE = "New Message!";
    public static final String CONNECTION_UPDATE = "New Connection Request!";

    //60 seconds - 1 minute is the minimum...
    private static final int POLL_INTERVAL = 10_000;

    private static final String TAG = "PullService";

    private static String mUsername = "";
    private static String mChatName = "";

    private boolean isInForeground;

    ArrayList<String> connectionRequests = new ArrayList<>();

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
        //checkNewMessages();
        checkNewConnectionRequests();
        return true;
    }

    /**
     * Prepares a notification if the app is in the background.
     * @param s
     */
    private void buildNotification(String s, int notificationType) {
        NotificationCompat.Builder mBuilder = null;
        Intent notifyIntent = null;

        //******Chat notification == 0, Connection notification == 1*****//
        if (notificationType == 0) {
            mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_chat)
                        .setContentTitle("New Message in " + s + "!")
                        .setContentText("Click to chat!");
            // Creates an Intent for the Activity
            notifyIntent = new Intent(this, HomeActivity.class);
            notifyIntent.putExtra(getString(R.string.keys_chat_notification), s);
        } else if (notificationType == 1) {
            mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_person_add)
                        .setContentTitle(s + " sent you a new connection request!")
                        .setContentText("Click to view your requests!");
            notifyIntent = new Intent(this, HomeActivity.class);
            notifyIntent.putExtra(getString(R.string.keys_connection_notification), s);
        }

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

    private void checkNewConnectionRequests() {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_post_get_requests2))
                .build();

        JSONObject msg = new JSONObject();
        try {
            msg.put(getString(R.string.keys_json_username), mUsername);
        } catch (JSONException e) {
            Log.wtf("JSON EXCEPTION", e.toString());
        }

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleGetConnectionRequestsOnPre)
                .onPostExecute(this::handleGetConnectionRequestsOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    public void handleGetConnectionRequestsOnPre() {

    }

    /**
     * Read through the list of chats and see if the most recent is from
     * somebody else and came recently.
     * @param result of finding chats
     */
    public void handleGetConnectionRequestsOnPost(String result) {

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_requests))) {
                    try {

                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_requests));
                        ArrayList<String> newRequests = new ArrayList<>();

                        for (int i = 0; i < jReqs.length(); i++) {
                            String request = jReqs.getJSONObject(i).getString("username");
                            if (!connectionRequests.contains(request)) {
                                connectionRequests.add(request);
                                if (isInForeground) {
                                    newRequests.add(request);
                                } else {
                                    buildNotification(request, 1);
                                }
                            }
                        }

                        if (isInForeground) {
                            Intent intent = new Intent(CONNECTION_UPDATE);
                            int i = 0;
                            String[] connectKeys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
                            for (String s : newRequests) {
                                intent.putExtra(connectKeys[i], s);
                            }
                            sendBroadcast(intent);
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



    private void checkNewMessages() {
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

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleGetChatsOnPre)
                .onPostExecute(this::handleGetChatsOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }


    public void handleGetChatsOnPre() {

    }

    public void handleGetChatsOnPost(String result) {

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
                            mChatName = jReqs.getJSONObject(i).getString("name");

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

    /**
     * Read through the list of chats and see if the most recent is from
     * somebody else and came recently.
     * @param result of finding chats
     */
    public void handleGetMessagesOnPost(String result) {

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_messages))) {
                    try {
                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_messages));
<<<<<<< HEAD
                        for (int i = 0; i < jReqs.length(); i++) {
                            String messageFrom = jReqs.getJSONObject(i).getString("username");
                            String time = jReqs.getJSONObject(i).getString("timestamp");

                            if (messageFrom != mUsername && isMessageWithinLastMinute(time)) {
                                if (isInForeground) {
                                    Log.e(TAG, "Inside app sending notification");
                                    Intent intent = new Intent(RECEIVED_UPDATE);
                                    //add bundle to send the response to any receivers
                                    Log.wtf("****TEST****", messageFrom);
                                    intent.putExtra(getString(R.string.keys_extra_results), mChatName);
                                    sendBroadcast(intent);
                                } else {
                                    Log.e(TAG, "Out of app building notification");
                                    buildNotification(mChatName);
                                }
                                break;
=======
                        String messageFrom = jReqs.getJSONObject(jReqs.length()-1).getString("username");
                        if (!messageFrom.equals(mUsername) && true)//chat came recently) {
                            if (isInForeground) {
                                Intent intent = new Intent(MESSAGE_UPDATE);
                                intent.putExtra(getString(R.string.keys_extra_results), messageFrom);
                                sendBroadcast(intent);
                            } else {
                                buildNotification(mChatName, 0);
>>>>>>> f41a7ad11edc831905f1a747842c92956cc4ae88
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

<<<<<<< HEAD
    public boolean isMessageWithinLastMinute(String time) {
       String[] date = time.split("\n");
        Log.i("DATEEEEEEE",date[0]);
        Log.i("Hour",date[1].substring(3,5));
        Log.i("Mintue",date[1].substring(6,8));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = new Date();
        date1.setMinutes(date1.getMinutes());
        String currentDateTime = dateFormat.format(date1);
        //Log.i("CHECKTIMER",currentDateTime);
        int currentMinute = Integer.parseInt(currentDateTime.substring(14,16));
        int messageMinute = Integer.parseInt(date[1].substring(6,8));

        Log.i("Cur",currentDateTime.substring(14,16));
        Log.i("Cur",date[1].substring(6,8));
        String dateCheck = currentDateTime.substring(0,10);
        if(dateCheck.equals(date) && date[1].substring(3,5).equals(currentDateTime.substring(11,13)) ) {
            Log.i("MESSAGE IN THIS HOUR","MESSAGE IN THIS HOUR");
            if(currentMinute ==messageMinute || currentMinute-1 == messageMinute ) {
                return true;
            }
            }
        return false;
    }
=======
>>>>>>> f41a7ad11edc831905f1a747842c92956cc4ae88


    public void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }


    public static void setUsername(final String theString) {
        mUsername = theString;
    }


}
