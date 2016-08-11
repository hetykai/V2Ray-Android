package com.rayfatasy.v2ray

import android.app.Application
import android.content.Context
import com.orhanobut.logger.LogLevel
import com.orhanobut.logger.Logger
import java.io.File

class V2RayApplication : Application() {
    val configFilePath: String by lazy { File(filesDir, "conf.json").absolutePath }

    override fun onCreate() {
        super.onCreate()
        Logger.init().logLevel(if (BuildConfig.DEBUG) LogLevel.FULL else LogLevel.NONE)
    }
}

fun Context.getV2RayApplication() = this.applicationContext as V2RayApplication