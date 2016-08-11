package com.rayfatasy.v2ray.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.ListPreference
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import com.github.jorgecastilloprz.listeners.FABProgressListener
import com.rayfatasy.v2ray.R
import com.rayfatasy.v2ray.event.VpnPrepareEvent
import com.rayfatasy.v2ray.service.V2RayService
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity(), FABProgressListener {


    companion object {
        const val REQUEST_CODE_VPN_PREPARE = 0
    }

    var isServiceActive: Boolean = false

    private lateinit var vpnPrepareCallback: (Boolean) -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Bus.observe<VpnPrepareEvent>()
                .subscribe {
                    vpnPrepareCallback = it.callback
                    startActivityForResult(it.intent, REQUEST_CODE_VPN_PREPARE)
                }
                .registerInBus(this)
        fabProgressCircle.attachListener(this)
        fab.setOnClickListener {
            if (!isServiceActive) {
                fabProgressCircle.show()
                V2RayService.startV2Ray(ctx)
                isServiceActive = true
                fabProgressCircle.beginFinalAnimation()

            } else {
                fabProgressCircle.show()
                V2RayService.stopV2Ray()
                isServiceActive = false
                fab.setImageResource(R.drawable.ic_action_logo)
                fabProgressCircle.beginFinalAnimation()
            }

        }


    }

    override fun onResume() {
        super.onResume()
        V2RayService.checkStatusEvent {
            if (it) {
                fab.setBackgroundColor(Color.parseColor("#000000"))
                fab.setImageResource(R.drawable.ic_check_24dp)
                isServiceActive = true
            } else {
                fab.setImageResource(R.drawable.ic_action_logo)
                isServiceActive = false
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Bus.unregister(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_VPN_PREPARE ->
                vpnPrepareCallback(resultCode == Activity.RESULT_OK)
        }
    }

    class MainFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

        companion object {
            const val PREF_SERVER_ADDRESS = "pref_server_address"
            const val PREF_SERVER_PORT = "pref_server_port"
            const val PREF_USER_ID = "pref_user_id"
            const val PREF_USER_ALTER_ID = "pref_user_alter_id"
            const val PREF_USER_EMAIL = "pref_user_email"
            const val PREF_STREAM_NETWORK = "pref_stream_network"
        }

        private val preference by lazy { preferenceManager.sharedPreferences }

        val serverAddress by lazy { findPreference(PREF_SERVER_ADDRESS) as EditTextPreference }
        val serverPort by lazy { findPreference(PREF_SERVER_PORT) as EditTextPreference }
        val userId by lazy { findPreference(PREF_USER_ID) as EditTextPreference }
        val userAlterId by lazy { findPreference(PREF_USER_ALTER_ID) as EditTextPreference }
        val userEmail by lazy { findPreference(PREF_USER_EMAIL) as EditTextPreference }
        val streamNetwork by lazy { findPreference(PREF_STREAM_NETWORK) as ListPreference }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_main)
        }

        override fun onResume() {
            super.onResume()
            serverAddress.summary = preference.getString(PREF_SERVER_ADDRESS, "v2ray.cool")
            serverPort.summary = preference.getString(PREF_SERVER_PORT, "10086")
            userId.summary = preference.getString(PREF_USER_ID, "23ad6b10-8d1a-40f7-8ad0-e3e35cd38297")
            userAlterId.summary = preference.getString(PREF_USER_ALTER_ID, "64")
            userEmail.summary = preference.getString(PREF_USER_EMAIL, "")
            streamNetwork.summary = streamNetwork.entry
            preference.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preference.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(pref: SharedPreferences?, key: String?) {
            when (key) {
                PREF_SERVER_ADDRESS -> serverAddress.summary = preference
                        .getString(key, "v2ray.cool")

                PREF_SERVER_PORT -> serverPort.summary = preference
                        .getString(key, "10086")

                PREF_USER_ID -> userId.summary = preference
                        .getString(key, "23ad6b10-8d1a-40f7-8ad0-e3e35cd38297")

                PREF_USER_ALTER_ID -> userAlterId.summary = preference
                        .getString(key, "64")

                PREF_USER_EMAIL -> userEmail.summary = preference
                        .getString(key, "")

                PREF_STREAM_NETWORK -> streamNetwork.summary = streamNetwork.entry
            }
        }
    }

    override fun onFABProgressAnimationEnd() {
        fab.isClickable = true
        if (isServiceActive) {
            fab.setImageResource(R.drawable.ic_check_24dp)
            toast("连接成功")
        } else {
            toast("已停止连接")
        }


    }
}