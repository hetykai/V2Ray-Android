package com.rayfatasy.v2ray.event

import android.content.Intent
import com.rayfatasy.v2ray.service.V2RayVpnService

object StopV2RayEvent

data class VpnServiceStartEvent(val vpnService: V2RayVpnService? = null)

data class VpnPrepareEvent(val intent: Intent, val callback: (Boolean) -> Unit)
