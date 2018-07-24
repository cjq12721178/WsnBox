package com.weisi.tool.wsnbox.bean.update

import android.content.Context
import com.weisi.tool.wsnbox.BuildConfig
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class UpdateServiceFactory {

    fun <T> createServiceFrom(context: Context, serviceClass: Class<T>): T {
        val adapter = Retrofit.Builder()
                .baseUrl(BuildConfig.SERVICE_SERVER_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // 添加Rx适配器
                .addConverterFactory(GsonConverterFactory.create()) // 添加Gson转换器
                .build()
        return adapter.create(serviceClass)
    }
}