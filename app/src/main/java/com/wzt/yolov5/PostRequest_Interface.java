package com.wzt.yolov5;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface PostRequest_Interface
{
    @PUT("android/numberOfPeople")
    public Call<Message> getCall(@Body NumberOfPeople numberOfPeople);
}
