package com.rayfatasy.v2ray.util

import org.apache.commons.validator.routines.InetAddressValidator
import org.json.JSONException
import org.json.JSONObject
import java.util.*

object ConfigUtil {
    val replacementPairs by lazy {
        listOf("port" to 10808,
                "inbound" to JSONObject("""{
                    "protocol": "socks",
                    "listen": "127.0.0.1",
                    "settings": {
                        "auth": "noauth",
                        "udp": true
                    }
                }"""),
                "#lib2ray" to JSONObject("""{
                    "enabled": true,
                    "listener": {
                    "onUp": "#none",
                    "onDown": "#none"
                    },
                    "env": [
                    "V2RaySocksPort=10808"
                    ],
                    "render": [],
                    "escort": [],
                    "vpnservice": {
                    "Target": "${"$"}{datadir}tun2socks",
                    "Args": [
                    "--netif-ipaddr",
                    "26.26.26.2",
                    "--netif-netmask",
                    "255.255.255.0",
                    "--socks-server-addr",
                    "127.0.0.1:${"$"}V2RaySocksPort",
                    "--tunfd",
                    "3",
                    "--tunmtu",
                    "1500",
                    "--sock-path",
                    "/dev/null",
                    "--loglevel",
                    "4",
                    "--enable-udprelay"
                    ],
                    "VPNSetupArg": "m,1500 a,26.26.26.1,24 r,0.0.0.0,0"
                    }
                }"""),
                "log" to JSONObject("""{
                    "loglevel": "warning"
                }""")
        )
    }

    fun validConfig(conf: String): Boolean {
        try {
            val jObj = JSONObject(conf)
            return jObj.has("outbound") and jObj.has("inbound")
        } catch (e: JSONException) {
            return false
        }
    }

    fun isConfigCompatible(conf: String) = JSONObject(conf).has("#lib2ray")

    fun convertConfig(conf: String): String {
        val jObj = JSONObject(conf)
        jObj.putOpt(replacementPairs)
        return jObj.toString()
    }

    fun readDnsServersFromConfig(conf: String, vararg defaultDns: String): Array<out String> {
        val json = JSONObject(conf)

        if (!json.has("dns"))
            return defaultDns
        val dns = json.optJSONObject("dns")

        if (!dns.has("servers"))
            return defaultDns
        val servers = dns.optJSONArray("servers")

        val ret = LinkedList<String>()
        for (i in 0..servers.length() - 1) {
            val e = servers.getString(i)
            if (InetAddressValidator.getInstance().isValid(e))
                ret.add(e)
            else if (e == "localhost")
                NetworkUtil.getDnsServers()?.let { ret.addAll(it) }
        }

        return ret.toTypedArray()
    }
}

fun JSONObject.putOpt(pair: Pair<String, Any>) = putOpt(pair.first, pair.second)!!
fun JSONObject.putOpt(pairs: List<Pair<String, Any>>) = pairs.forEach { putOpt(it) }