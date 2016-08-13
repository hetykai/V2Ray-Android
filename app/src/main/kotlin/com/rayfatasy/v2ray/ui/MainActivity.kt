package com.rayfatasy.v2ray.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.eightbitlab.rxbus.Bus
import com.eightbitlab.rxbus.registerInBus
import com.rayfatasy.v2ray.R
import com.rayfatasy.v2ray.event.V2RayStatusEvent
import com.rayfatasy.v2ray.event.VpnPrepareEvent
import com.rayfatasy.v2ray.getV2RayApplication
import com.rayfatasy.v2ray.service.V2RayService
import com.rayfatasy.v2ray.util.ConfigUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {


    companion object {
        private const val REQUEST_CODE_VPN_PREPARE = 0

        private const val REQUEST_CODE_FILE_SELECT = 1
    }

    var fabChecked = false
        set(value) {
            field = value
            if (value) {
                fab.setIcon(ResourcesCompat.getDrawable(resources, R.drawable.ic_check_24dp, null), false)
            } else {
                fab.setIcon(ResourcesCompat.getDrawable(resources, R.drawable.ic_action_logo, null), false)
            }
        }

    private lateinit var vpnPrepareCallback: (Boolean) -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_config_content.text = getV2RayApplication().configFile.readText()

        fab.setOnClickListener {
            if (fabChecked) {
                V2RayService.stopV2Ray()
            } else {
                V2RayService.startV2Ray(ctx)
            }
        }

        Bus.observe<VpnPrepareEvent>()
                .subscribe {
                    vpnPrepareCallback = it.callback
                    startActivityForResult(it.intent, REQUEST_CODE_VPN_PREPARE)
                }
                .registerInBus(this)

        Bus.observe<V2RayStatusEvent>()
                .subscribe {
                    fabChecked = it.isRunning
                }
        V2RayService.checkStatusEvent { fabChecked = it }
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

            REQUEST_CODE_FILE_SELECT -> {
                if (resultCode == Activity.RESULT_OK) {
                    val uri = data!!.data
                    val rawInputStream = contentResolver.openInputStream(uri)
                    val rawConfig = rawInputStream.bufferedReader().readText()
                    val retFile = getV2RayApplication().configFile

                    if (!ConfigUtil.validConfig(rawConfig)) {
                        toast(R.string.toast_config_file_invalid)
                        return
                    }

                    if (ConfigUtil.isConfigCompatible(rawConfig)) {
                        retFile.writeText(rawConfig)
                        tv_config_content.text = rawConfig
                    } else {
                        alert(R.string.msg_dialog_convert_config, R.string.title_dialog_convert_config) {
                            positiveButton(android.R.string.ok) {
                                val retConfig = ConfigUtil.convertConfig(rawConfig)
                                retFile.writeText(retConfig)
                                tv_config_content.text = retConfig
                            }

                            negativeButton()

                            show()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item!!.itemId) {
        R.id.add_config -> {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            try {
                startActivityForResult(
                        Intent.createChooser(intent, getString(R.string.title_file_chooser)),
                        REQUEST_CODE_FILE_SELECT)
            } catch (ex: android.content.ActivityNotFoundException) {
                toast(R.string.toast_require_file_manager)
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


}