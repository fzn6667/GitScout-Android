package com.faizan.gitscout

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {


    private lateinit var historySection: View
    private lateinit var rvHistory: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. UI Initialization
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val btnSearch = findViewById<ImageButton>(R.id.btnSearch)
        val ivAvatar = findViewById<ImageView>(R.id.ivAvatar)
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvBio = findViewById<TextView>(R.id.tvBio)
        val tvLocation = findViewById<TextView>(R.id.tvLocation)
        val tvFollowers = findViewById<TextView>(R.id.tvFollowers)
        val tvRepos = findViewById<TextView>(R.id.tvRepos)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val profileCard = findViewById<View>(R.id.profileCard)
        val btnVisitProfile = findViewById<Button>(R.id.btnVisitProfile)
        val tvReposHeader = findViewById<TextView>(R.id.tvReposHeader)
        val rvRepos = findViewById<RecyclerView>(R.id.rvRepos)

        // Design specific views
        val ivIllustration = findViewById<ImageView>(R.id.ivIllustration)
        historySection = findViewById(R.id.historySection)
        val tvClearHistory = findViewById<TextView>(R.id.tvClearHistory)
        rvHistory = findViewById(R.id.rvHistory)

        // 2. Setup RecyclerViews
        rvRepos.layoutManager = LinearLayoutManager(this)
        rvHistory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Initial Load
        updateHistoryUI()

        // 3. Retrofit Setup
        val service = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubService::class.java)

        // 4. Clear History Action
        tvClearHistory.setOnClickListener {
            val prefs = getSharedPreferences("GitScoutPrefs", MODE_PRIVATE)
            prefs.edit().remove("search_history").apply()
            updateHistoryUI()
            Toast.makeText(this, "History Cleared", Toast.LENGTH_SHORT).show()
        }

        // 5. Search Logic
        btnSearch.setOnClickListener {
            val username = etUsername.text.toString().trim()
            if (username.isEmpty()) {
                Toast.makeText(this, "Username likho!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // UI Reset:
            progressBar.visibility = View.VISIBLE
            profileCard.visibility = View.GONE
            tvReposHeader.visibility = View.GONE
            ivIllustration.visibility = View.GONE
            historySection.visibility = View.GONE

            service.getUserDetails(username).enqueue(object : Callback<GitHubUser> {
                override fun onResponse(call: Call<GitHubUser>, response: Response<GitHubUser>) {
                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!
                        profileCard.visibility = View.VISIBLE

                        // Set Profile Data
                        tvName.text = user.name ?: user.login
                        tvBio.text = user.bio ?: "No bio available"
                        tvLocation.text = user.location ?: "No location"
                        tvFollowers.text = "${user.followers} Followers"
                        tvRepos.text = "${user.public_repos} Repos"
                        Glide.with(this@MainActivity).load(user.avatar_url).into(ivAvatar)

                        btnVisitProfile.setOnClickListener {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(user.html_url)))
                        }

                        // HISTORY LOGIC START
                        val historyUser = HistoryUser(
                            user.login,
                            user.avatar_url,
                            "${user.public_repos} Repos, ${user.followers} Followers"
                        )
                        saveToHistory(historyUser)
                        //
                        //  HISTORY LOGIC END

                        loadUserRepos(service, username, rvRepos, tvReposHeader, progressBar)
                    } else {
                        progressBar.visibility = View.GONE
                        ivIllustration.visibility = View.VISIBLE
                        updateHistoryUI() // Agar error aye to history wapas dikhao
                        Toast.makeText(this@MainActivity, "User not found!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<GitHubUser>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    ivIllustration.visibility = View.VISIBLE
                    updateHistoryUI()
                    Toast.makeText(this@MainActivity, "Network Error!", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun loadUserRepos(service: GitHubService, username: String, rv: RecyclerView, header: View, pb: ProgressBar) {
        service.getUserRepos(username).enqueue(object : Callback<List<Repository>> {
            override fun onResponse(call: Call<List<Repository>>, response: Response<List<Repository>>) {
                pb.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    header.visibility = View.VISIBLE
                    rv.visibility = View.VISIBLE
                    rv.adapter = RepoAdapter(response.body()!!)
                }
            }
            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                pb.visibility = View.GONE
            }
        })
    }

    //  Helper Functions for History Management

    private fun updateHistoryUI() {
        val history = getHistory()
        if (history.isNotEmpty()) {
            historySection.visibility = View.VISIBLE
            rvHistory.adapter = HistoryAdapter(history)
        } else {
            historySection.visibility = View.GONE
        }
    }

    private fun saveToHistory(user: HistoryUser) {
        val prefs = getSharedPreferences("GitScoutPrefs", MODE_PRIVATE)
        val gson = Gson()
        val existingHistory = getHistory().toMutableList()

        // Duplicate check
        existingHistory.removeAll { it.username == user.username }
        // Add to top
        existingHistory.add(0, user)

        // Limit to 10 items
        val limitedHistory = if (existingHistory.size > 10) existingHistory.take(10) else existingHistory

        val json = gson.toJson(limitedHistory)
        prefs.edit().putString("search_history", json).apply()
    }

    private fun getHistory(): List<HistoryUser> {
        val prefs = getSharedPreferences("GitScoutPrefs", MODE_PRIVATE)
        val json = prefs.getString("search_history", null)
        return if (json != null) {
            val type = object : TypeToken<List<HistoryUser>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }
}