package com.rayfatasy.v2ray

import android.app.Application
import android.content.Context
import com.orhanobut.logger.LogLevel
import com.orhanobut.logger.Logger
import com.rayfatasy.v2ray.util.AssetsUtil
import java.io.File

class V2RayApplication : Application() {
    val configFile by lazy { File(filesDir, "conf.json") }

    override fun onCreate() {
        super.onCreate()
        Logger.init().logLevel(if (BuildConfig.DEBUG) LogLevel.FULL else LogLevel.NONE)
        if (!configFile.exists())
            AssetsUtil.copyAsset(assets, "conf_default.json", configFile.absolutePath)
    }
}

fun Context.getV2RayApplication() = this.applicationContext as V2RayApplication