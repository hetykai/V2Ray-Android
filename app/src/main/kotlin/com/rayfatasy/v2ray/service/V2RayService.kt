package com.rayfatasy.v2ray.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.IBinder
import android.os.StrictMode
import android.preference.PreferenceManager
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import com.orhanobut.logger.Logger
import com.rayfatasy.v2ray.event.*
import com.rayfatasy.v2ray.ui.MainActivity
import go.libv2ray.Libv2ray
import org.jetbrains.anko.startService

class V2RayService : Service() {
    companion object {
        fun startV2Ray(context: Context) {
            context.startService<V2RayService>()
        }

        fun stopV2Ray() {
            Bus.send(StopV2RayEvent)
        }

        fun sendCheckStatusEvent() {
            Bus.send(CheckV2RayStatusEvent)
        }
    }

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

        Bus.observe<VpnServiceSendSelfEvent>()
                .subscribe {
                    vpnService = it.vpnService
                    vpnCheckIsReady()
                }
                .registerInBus(this)

        Bus.observe<StopV2RayEvent>()
                .subscribe {
                    stopV2Ray()
                }
                .registerInBus(this)

        Bus.observe<VpnServiceStatusEvent>()
                .filter { !it.isRunning }
                .subscribe { stopV2Ray() }
                .registerInBus(this)

        Bus.observe<CheckV2RayStatusEvent>()
                .subscribe {
                    val prepare = VpnService.prepare(this)
                    val isRunning = prepare == null && vpnService != null && v2rayPoint.isRunning
                    Bus.send(V2RayStatusEvent(isRunning))
                }
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
        return 1
    }

    private fun vpnCheckIsReady() {
        val prepare = VpnService.prepare(this)

        if (prepare != null) {
            Bus.send(VpnPrepareEvent(prepare) {
                if (it)
                    vpnCheckIsReady()
                else
                    v2rayPoint.StopLoop()
            })
            return
        }

        if (this.vpnService != null) {
            v2rayPoint.VpnSupportReady()
            Bus.send(V2RayStatusEvent(true))
        }
    }

    private fun startV2ray() {
        Logger.d(v2rayPoint)
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
        vpnService = null
        Bus.send(V2RayStatusEvent(false))
        stopSelf()
    }

    private inner class V2RayCallback : Libv2ray.V2RayCallbacks, Libv2ray.V2RayVPNServiceSupportsSet {
        override fun Shutdown() = 0L

        override fun GetVPNFd() = vpnService!!.getFd().toLong()

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
