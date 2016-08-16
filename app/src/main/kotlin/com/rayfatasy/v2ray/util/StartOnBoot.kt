package com.rayfatasy.v2ray.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rayfatasy.v2ray.service.V2RayService
import com.rayfatasy.v2ray.service.V2RayVpnService
import org.jetbrains.anko.ctx
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.startService

/**
 * Created by Takay on 2016/8/16.
 */
class BootBroadcastReceiver : BroadcastReceiver(){
    override fun onReceive(p0: Context, p1: Intent?) {

        if (p0.defaultSharedPreferences.getBoolean("StartOnBoot",false)){
            V2RayService.startV2Ray(p0.ctx)
        }

    }

}