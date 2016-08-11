package com.rayfatasy.v2ray.dto

import com.google.gson.annotations.SerializedName

class V2RayConfigOutbound {

    /**
     * protocol : vmess
     * settings : {"vnext":[{"address":"v2ray.cool","port":10086,"users":[{"id":"23ad6b10-8d1a-40f7-8ad0-e3e35cd38297","alterId":64}]}]}
     * streamSettings : {"network":"tcp","security":"none","tlsSettings":{"allowInsecure":true}}
     */

    @SerializedName("protocol")
    var protocol: String = "vmess"
    @SerializedName("settings")
    var settings: SettingsBean = SettingsBean()
    /**
     * network : tcp
     * security : none
     * tlsSettings : {"allowInsecure":true}
     */

    @SerializedName("streamSettings")
    var streamSettings: StreamSettingsBean = StreamSettingsBean()

    class SettingsBean {
        /**
         * address : v2ray.cool
         * port : 10086
         * users : [{"id":"23ad6b10-8d1a-40f7-8ad0-e3e35cd38297","alterId":64}]
         */

        @SerializedName("vnext")
        var vnext: List<VnextBean> = listOf(VnextBean())

        class VnextBean {
            @SerializedName("address")
            var address: String = "v2ray.cool"
            @SerializedName("port")
            var port: Int = 10086
            /**
             * id : 23ad6b10-8d1a-40f7-8ad0-e3e35cd38297
             * alterId : 64
             */

            @SerializedName("users")
            var users: List<UsersBean> = listOf(UsersBean())

            class UsersBean {
                @SerializedName("id")
                var id: String = "23ad6b10-8d1a-40f7-8ad0-e3e35cd38297"
                @SerializedName("alterId")
                var alterId: Int = 64
                @SerializedName("email")
                var email: String = ""
            }
        }
    }

    class StreamSettingsBean {
        @SerializedName("network")
        var network: String = "tcp"
        @SerializedName("security")
        var security: String = "none"
        /**
         * allowInsecure : true
         */

        @SerializedName("tlsSettings")
        var tlsSettings: TlsSettingsBean = TlsSettingsBean()

        class TlsSettingsBean {
            @SerializedName("allowInsecure")
            var isAllowInsecure: Boolean = true
        }
    }
}
