package com.rayfatasy.v2ray.service

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.eightbitlab.rxbus.Bus
import com.rayfatasy.v2ray.event.VpnServiceEvent

class V2RayVpnService : VpnService() {

    private lateinit var mInterface: ParcelFileDescriptor
    fun getfd(): Int = mInterface.fd

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Bus.send(VpnServiceEvent(true, this))
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onRevoke() {
        Bus.send(VpnServiceEvent(false))
        super.onRevoke()
    }

    fun setup(parameters: String) {
        // If the old interface has exactly the same parameters, use it!
        // Configure a builder while parsing the parameters.
        val builder = Builder()
        for (parameter in parameters.split(" ")) {
            val fields = parameter.split(",")
            try {
                when (fields[0][0]) {
                    'm' -> builder.setMtu(java.lang.Short.parseShort(fields[1]).toInt())
                    'a' -> builder.addAddress(fields[1], Integer.parseInt(fields[2]))
                    'r' -> builder.addRoute(fields[1], Integer.parseInt(fields[2]))
                    'd' -> builder.addDnsServer(fields[1])
                    's' -> builder.addSearchDomain(fields[1])
                }
            } catch (e: Exception) {
                throw IllegalArgumentException("Bad parameter: " + parameter)
            }

        }
        // Close the old interface since the parameters have been changed.
        try {
            mInterface.close()
        } catch (ignored: Exception) {
        }

        // Create a new interface using the builder and save the parameters.
        mInterface = builder.establish()
        Log.i("VPNService", "New interface: " + parameters)
    }
}

