package com.faizan.gitscout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class HistoryAdapter(private val list: List<HistoryUser>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.ivHistoryAvatar)
        val name: TextView = view.findViewById(R.id.tvHistoryName)
        val stats: TextView = view.findViewById(R.id.tvHistoryStats)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = list[position]
        holder.name.text = user.username
        holder.stats.text = user.stats
        Glide.with(holder.itemView.context).load(user.avatarUrl).into(holder.img)
    }

    override fun getItemCount() = list.size
}