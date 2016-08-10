package com.rayfatasy.v2ray

import android.app.Application
import com.orhanobut.logger.LogLevel
import com.orhanobut.logger.Logger

class V2RayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.init().logLevel(if (BuildConfig.DEBUG) LogLevel.FULL else LogLevel.NONE)
    }
}