package com.rayfatasy.v2ray.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.rayfatasy.v2ray.R
import com.rayfatasy.v2ray.getConfigFile
import com.rayfatasy.v2ray.service.V2RayService
import com.rayfatasy.v2ray.util.ConfigUtil
import kotlinx.android.synthetic.main.item_recycler_main.view.*
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.onClick
import org.jetbrains.anko.startActivity

class MainRecyclerAdapter(ctx: Context, var configs: Array<String>) : RecyclerView.Adapter<MainRecyclerAdapter.MainViewHolder>() {
    private val preference = ctx.defaultSharedPreferences

    override fun getItemCount() = configs.size

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val name = configs[position]
        val conf = holder.address.context.getConfigFile(name).readText()

        holder.name.text = name
        holder.address.text = ConfigUtil.readAddressFromConfig(conf)
        holder.radio.isChecked = name == preference.getString(V2RayService.PREF_CURR_CONFIG, "")

        holder.radio.onClick {
            preference.edit().putString(V2RayService.PREF_CURR_CONFIG, name).apply()
            notifyDataSetChanged()
        }

        holder.infoContainer.onClick {
            holder.infoContainer.context.startActivity<TextActivity>("title" to name, "text" to conf)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(parent.context.layoutInflater
                .inflate(R.layout.item_recycler_main, parent, false))
    }

    class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val radio = itemView.btn_radio!!
        val name = itemView.tv_name!!
        val address = itemView.tv_address!!
        val infoContainer = itemView.info_container!!
    }
}
