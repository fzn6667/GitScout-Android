package com.faizan.gitscout

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RepoAdapter(private val repos: List<Repository>) : RecyclerView.Adapter<RepoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvRepoName)
        val desc: TextView = view.findViewById(R.id.tvRepoDesc)
        val lang: TextView = view.findViewById(R.id.tvLang)
        val stars: TextView = view.findViewById(R.id.tvStars)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.repo_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val repo = repos[position]
        holder.name.text = repo.name
        holder.desc.text = repo.description ?: "No description available"
        holder.lang.text = repo.language ?: "N/A"
        holder.stars.text = "⭐ ${repo.stargazers_count}"

        //
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(repo.html_url))
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = repos.size
}