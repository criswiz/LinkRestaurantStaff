package com.sensei.linkrestaurantstaff.Interface;

import com.sensei.linkrestaurantstaff.Model.FCMResponse;
import com.sensei.linkrestaurantstaff.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "ContentType:application/json",
            "Authorization:key=AAAAIP71o_4:APA91bHxD2DPuSP-cGSv0pY3xSgOahJgGzSIbmNLghOK9cKt4pcXNiw1KhbDUs6ZTQmvLs47YvW8h2keB3TQskoGHB1nw46zzXr-gMi54S1Xuw42y5T63V-IcscA4i0_Zmmde_M6dZd2"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
