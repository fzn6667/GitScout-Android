package com.faizan.gitscout

data class Repository(
    val name: String,
    val description: String?,
    val language: String?,
    val stargazers_count: Int,
    val html_url: String
)