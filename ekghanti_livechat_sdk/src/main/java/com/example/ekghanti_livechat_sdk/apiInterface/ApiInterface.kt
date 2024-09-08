package com.example.ekchanti_chat_sdk.apiInterface

import com.example.ekchanti_chat_sdk.model.chat.ChatData
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Query
interface ApiInterface {
    @Headers(
        "Accept: */*",
        "Content-Type: application/json"
    )
    @GET("d5d894f4-2e2e-481e-9067-8062db83d0d7")
    fun getData(): Call<ChatData>

    @Multipart
    @POST("uploadImage")
    fun uploadImage(
        @Part image: MultipartBody.Part
    ): Call<String>
}