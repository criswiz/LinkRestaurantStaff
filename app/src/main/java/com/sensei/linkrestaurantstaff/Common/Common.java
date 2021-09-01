package com.sensei.linkrestaurantstaff.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.sensei.linkrestaurantstaff.Model.Order;
import com.sensei.linkrestaurantstaff.Model.OrderDetail;
import com.sensei.linkrestaurantstaff.Model.RestaurantOwner;
import com.sensei.linkrestaurantstaff.R;

public class Common {

    public static final String REMEMBER_FBID = "REMEMBER FBID";
    public static final String API_KEY_TAG = "API_KEY";
    public static final String NOTIFI_TITLE ="title" ;
    public static final String NOTIFI_CONTENT = "content";
    public static final String API_RESTAURANT_ENDPOINT = "http://ilinkrestaurant-android-app.azurewebsites.net/";
    public static final String API_KEY = "";

    public static RestaurantOwner currentRestaurantOwner;
    public static Order currentOrder;

    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus) {
            case 0:
                return "Placed";
            case 1:
                return "Shipping";
            case 2:
                return "Shipped";
            case -1:
                return "Cancelled";
            default:
                return "Cancelled";
        }
    }

    public static int convertStatusToIndex(int orderStatus) {
        if (orderStatus == -1)
            return 3;
        else
            return orderStatus;
    }

    public static String buildJWT(String apiKey) {
        return new StringBuilder("Bearer")
                .append(" ")
                .append(apiKey).toString();
    }

    public static void showNotification(Context context, int notiId, String title, String body, Intent intent) {
        PendingIntent pendingIntent = null;
        String NOTIFICATION_CHANNEL_ID = "Lint Restaurant Staff";

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context,notiId,intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "LintRestaurantStaff Notifications", NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("Link Restaurant Staff App");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }



        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        builder.setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon));

        if (pendingIntent != null){
            builder.setContentIntent(pendingIntent);
        }

        Notification nNotification = builder.build();

        notificationManager.notify(notiId,nNotification);
    }


    public static String getTopicChannel(int restaurantId) {
        return new StringBuilder("Restaurant_").append(restaurantId).toString();
    }

    public static int convertStringToStatus(String status) {
        if (status.equals("Placed"))
            return 0;
        else if (status.equals("Shipping"))
            return 1;
        else if (status.equals("Shipped"))
            return 2;
        else if (status.equals("Cancelled"))
            return -1;
        return -1;
    }
}
