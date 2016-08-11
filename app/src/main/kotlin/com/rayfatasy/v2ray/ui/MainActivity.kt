package com.rayfatasy.v2ray.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.support.v7.app.AppCompatActivity
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import com.github.jorgecastilloprz.listeners.FABProgressListener
import com.rayfatasy.v2ray.R
import com.rayfatasy.v2ray.dto.V2RayConfigOutbound
import com.rayfatasy.v2ray.event.V2RayStatusEvent
import com.rayfatasy.v2ray.event.VpnPrepareEvent
import com.rayfatasy.v2ray.service.V2RayService
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity(), FABProgressListener {


    companion object {
        const val PREF_MASTER_SWITCH = "pref_master_switch"
        const val REQUEST_CODE_VPN_PREPARE = 0
    }

    var isFabActive: Boolean = false

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
            fab.show()
            fab.isClickable = false

            if (isFabActive == false) {
                V2RayService.startV2Ray(ctx, V2RayConfigOutbound())
                isFabActive = true
                fabProgressCircle.beginFinalAnimation()
            } else {
                V2RayService.stopV2Ray()
                isFabActive = false
                fabProgressCircle.beginFinalAnimation()
            }

        }

        //成功后使用下面设置动画
        //fabProgressCircle.beginFinalAnimation()

    }

    fun checkVPNStatus(): Boolean {
        var isActive: Boolean = false
        Bus.observe<V2RayStatusEvent>().subscribe {
            isActive = it.isRunning
        }
        return isActive
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

        val masterSwitch by lazy { findPreference(PREF_MASTER_SWITCH) as SwitchPreference }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_main)
            masterSwitch.isChecked = false
        }

        override fun onResume() {
            super.onResume()
            Bus.observe<V2RayStatusEvent>()
                    .subscribe { masterSwitch.isChecked = it.isRunning }
                    .registerInBus(this)
            V2RayService.checkStatusEvent { masterSwitch.isChecked = it }
            preference.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            Bus.unregister(this)
            preference.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(pref: SharedPreferences?, key: String?) {
            when (key) {
                PREF_MASTER_SWITCH -> if (preference.getBoolean(key, false))
                    V2RayService.startV2Ray(ctx, V2RayConfigOutbound())
                else
                    V2RayService.stopV2Ray()
            }
        }
    }

    override fun onFABProgressAnimationEnd() {
        fab.isClickable = true
        toast("连接完成")


    }
}