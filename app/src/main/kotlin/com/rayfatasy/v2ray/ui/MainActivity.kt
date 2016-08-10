package com.rayfatasy.v2ray.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import com.orhanobut.logger.Logger
import com.rayfatasy.v2ray.R
import com.rayfatasy.v2ray.event.StopV2RayEvent
import com.rayfatasy.v2ray.event.VpnPrepareEvent
import com.rayfatasy.v2ray.service.V2RayService
import com.rayfatasy.v2ray.util.AssetsUtil
import org.jetbrains.anko.startService
import java.io.File

class MainActivity : AppCompatActivity() {
    companion object {
        const val PREF_MASTER_SWITCH = "pref_master_switch"
        const val PREF_CONFIG_FILE_PATH = "pref_config_file_path"
        const val REQUEST_CODE_VPN_PREPARE = 0
    }

    val configFilePath: String by lazy { File(filesDir, "conf_vpnservice.json").absolutePath }

    private lateinit var vpnPrepareCallback: (Boolean) -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: Implement config file generation
        AssetsUtil.copyAsset(assets, "conf_vpnservice.json", configFilePath)
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(PREF_CONFIG_FILE_PATH,
                configFilePath).apply()
        Logger.d(configFilePath)

        Bus.observe<VpnPrepareEvent>()
                .subscribe {
                    vpnPrepareCallback = it.callback
                    startActivityForResult(it.intent, REQUEST_CODE_VPN_PREPARE)
                }
                .registerInBus(this)
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
        private val preference by lazy { preferenceManager.sharedPreferences }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_main)
        }

        override fun onResume() {
            super.onResume()
            preference.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preference.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(pref: SharedPreferences?, key: String?) {
            when (key) {
                PREF_MASTER_SWITCH -> if (preference.getBoolean(key, false))
                    startService<V2RayService>()
                else
                    Bus.send(StopV2RayEvent)
            }
        }
    }
}