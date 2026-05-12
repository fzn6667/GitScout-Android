package com.faizan.gitscout

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubService {
    @GET("users/{username}")
    fun getUserDetails(@Path("username") username: String): Call<GitHubUser>


    @GET("users/{username}/repos?sort=updated&per_page=10")
    fun getUserRepos(@Path("username") username: String): Call<List<Repository>>

}