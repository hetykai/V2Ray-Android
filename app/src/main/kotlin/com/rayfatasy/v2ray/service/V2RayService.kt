package com.rayfatasy.v2ray.service

import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.IBinder
import android.os.StrictMode
import android.preference.PreferenceManager
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import com.orhanobut.logger.Logger
import com.rayfatasy.v2ray.event.StopV2RayEvent
import com.rayfatasy.v2ray.event.VpnPrepareEvent
import com.rayfatasy.v2ray.event.VpnServiceEvent
import com.rayfatasy.v2ray.ui.MainActivity
import go.libv2ray.Libv2ray
import org.jetbrains.anko.startService

class V2RayService : Service() {
    private val v2rayPoint = Libv2ray.NewV2RayPoint()
    private var vpnService: V2RayVpnService? = null
    private val v2rayCallback = V2RayCallback()
    private val preference by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        v2rayPoint.packageName = packageName

        Bus.observe<VpnServiceEvent>()
                .subscribe {
                    if (it.start) {
                        vpnService = it.vpnService
                        val prepare = VpnService.prepare(this)
                    } else {
                        vpnService = null
                    }
                }
                .registerInBus(this)

        Bus.observe<StopV2RayEvent>()
                .subscribe {
                    stopV2Ray()
                    stopSelf()
                }
                .registerInBus(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Bus.unregister(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startV2ray()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun vpnPrepare(): Int {
        startService<V2RayVpnService>()
        val prepare = VpnService.prepare(this)
        if (prepare == null) {
            vpnCheckIsReady()
            return 0
        }
        Bus.send(VpnPrepareEvent(prepare) {
            if (it)
                vpnCheckIsReady()
            else
                v2rayPoint.StopLoop()
        })
        return 1
    }

    private fun vpnCheckIsReady(): Int {
        val prepare = VpnService.prepare(this)
        if (prepare == null && this.vpnService != null) {
            v2rayPoint.VpnSupportReady()
        }
        return 0
    }

    private fun startV2ray() {
        if (!v2rayPoint.isRunning) {
            // show_noti("Freedom shall be portable.")
            v2rayPoint.callbacks = v2rayCallback
            v2rayPoint.vpnSupportSet = v2rayCallback
            val configureFile = preference.getString(MainActivity.PREF_CONFIG_FILE_PATH, "")
            v2rayPoint.configureFile = configureFile
            v2rayPoint.RunLoop()
        }
    }

    private fun stopV2Ray() {
        // resign_noti()
        if (v2rayPoint.isRunning) {
            v2rayPoint.StopLoop()
        }
    }

    private inner class V2RayCallback : Libv2ray.V2RayCallbacks, Libv2ray.V2RayVPNServiceSupportsSet {
        override fun Shutdown() = 0L

        override fun GetVPNFd() = vpnService!!.getfd().toLong()

        override fun Prepare() = vpnPrepare().toLong()

        override fun Protect(l: Long) = (if (vpnService!!.protect(l.toInt())) 0 else 1).toLong()

        override fun OnEmitStatus(l: Long, s: String?): Long {
            Logger.d(s)
            return 0
        }

        override fun Setup(s: String): Long {
            Logger.d(s)
            try {
                vpnService!!.setup(s)
                return 0
            } catch (e: Exception) {
                e.printStackTrace()
                return -1
            }
        }
    }
}
