package com.weisi.tool.wsnbox.bean.update

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface UpdateService {

    @GET("update/check")
    fun getUpdateInfo(@Query("channel") channel: String): Observable<UpdateInfo>
}