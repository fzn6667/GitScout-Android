package com.faizan.gitscout


data class GitHubUser(
    val login: String,
    val avatar_url: String,
    val name: String?,
    val public_repos: Int,
    val followers: Int,
    val html_url: String,// Ye wala add kiya
    val bio: String?,      // User ki description
    val location: String?, // City/Countr

)