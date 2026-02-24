package com.example.myfilimapp.navigation

import android.net.Uri
import com.example.myfilimapp.R


sealed class NavigationScreen(
    val route: String,
    val selectedIcon: Int,
    val unselectedIcon: Int,
    val title: String
){
    data object SplashScreen : NavigationScreen("splash_screen_route",0,0,"Splash Screen")
    data object OnBoardingScreen : NavigationScreen("onBoarding_screen_route",0,0,"On Boarding Screen")
    data object LoginScreen : NavigationScreen("login_screen_route",0,0,"Login Screen")
    data object MusicPlayerScreen : NavigationScreen(
        "MusicPlayer_Screen_route/{uri}/{index}",
        0,
        0,"Music Player Screen"
    ){
        fun createRoute(uri: String,index:String) = "MusicPlayer_Screen_route/${Uri.encode(uri)}/$index"
    }

    data object HomeScreen : NavigationScreen(
        "Home_screen_route",
        R.drawable.home_filled,
        R.drawable.home,
        "Music")

    data object FavoritesScreen : NavigationScreen("Favorites_screen_route", R.drawable.fav_filled,R.drawable.fav,"Favorites")
    data object MyProfileScreen : NavigationScreen("MyProfile_screen_route", R.drawable.profile_filled,R.drawable.profile,"My Profile")


}