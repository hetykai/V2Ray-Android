package com.rayfatasy.v2ray.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.rayfatasy.v2ray.R
import com.rayfatasy.v2ray.service.V2RayService
import com.rayfatasy.v2ray.util.ConfigUtil
import kotlinx.android.synthetic.main.item_recycler_main.view.*
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.onClick

class MainRecyclerAdapter(ctx: Context, var configs: Array<String>) : RecyclerView.Adapter<MainRecyclerAdapter.MainViewHolder>() {
    private val preference = ctx.defaultSharedPreferences

    override fun getItemCount() = configs.size

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val conf = configs[position]
        holder.name.text = conf
        holder.address.text = ConfigUtil.readAddressByName(holder.address.context, conf)
        holder.radio.isChecked = conf == preference.getString(V2RayService.PREF_CURR_CONFIG, "")
        holder.radio.onClick {
            preference.edit().putString(V2RayService.PREF_CURR_CONFIG, conf).apply()
            notifyDataSetChanged()
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
    }
}
