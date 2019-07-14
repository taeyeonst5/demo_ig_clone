package com.allen_chou.instagramclone.Fragment;

import com.allen_chou.instagramclone.Notification.MyResponse;
import com.allen_chou.instagramclone.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAuLkquw8:APA91bGrDEMgh2Jd4AyZ0IBF41vKMTvnpEcTxrIa8a9tROZA02im4FIfQW0nFCTNUuqBnWkYTNbd9P-yzeVOAnuGGRJ3AwdcA6OrPAlyEwTxVx17ecGWMdA0AH191zPqmzNCXfTIch7N"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
