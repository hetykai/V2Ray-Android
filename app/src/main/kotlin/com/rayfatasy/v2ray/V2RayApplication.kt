package com.rayfatasy.v2ray

import android.app.Application
import android.content.Context
import com.orhanobut.logger.LogLevel
import com.orhanobut.logger.Logger
import com.rayfatasy.v2ray.service.V2RayService
import com.rayfatasy.v2ray.util.AssetsUtil
import org.jetbrains.anko.defaultSharedPreferences
import java.io.File

class V2RayApplication : Application() {
    val configFileDir by lazy { File(filesDir, "configs") }

    val configs: Array<String>
        get() = configFileDir.list()

    override fun onCreate() {
        super.onCreate()
        Logger.init().logLevel(if (BuildConfig.DEBUG) LogLevel.FULL else LogLevel.NONE)
        if (!configFileDir.exists() || !configFileDir.isDirectory) {
            configFileDir.mkdirs()
            AssetsUtil.copyAsset(assets, "conf_default.json", File(configFileDir, "default").absolutePath)
            defaultSharedPreferences.edit().putString(V2RayService.PREF_CURR_CONFIG, "default").apply()
        }
    }
}

fun Context.getV2RayApplication() = this.applicationContext as V2RayApplication
fun Context.getConfigFile(name: String) = File(getV2RayApplication().configFileDir, name)
fun Context.getConfigFile() = File(getV2RayApplication().configFileDir,
        defaultSharedPreferences.getString(V2RayService.PREF_CURR_CONFIG, ""))