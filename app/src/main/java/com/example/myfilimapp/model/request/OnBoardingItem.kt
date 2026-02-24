package com.example.myfilimapp.model.request

import com.example.myfilimapp.R

data class OnBoardingItem(
    val image: Int,
    val title: String,
    val description: String
)



val items = listOf(
    OnBoardingItem(
        image = R.drawable.musicbg1,
        title = "Stream Unlimited Music Anytime",
        description = "Discover millions of songs, albums, and playlists from your favorite artists."
    ),
    OnBoardingItem(
        image = R.drawable.music2bg,
        title = "Create & Share Your Playlists",
        description = "Build your personal collection and share your vibe with friends."
    ),
    OnBoardingItem(
        image = R.drawable.music3bg,
        title = "Enjoy Music Offline Anywhere",
        description = "Download your favorite tracks and listen without internet."
    )
)
