package com.example.myfilimapp.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.myfilimapp.R
import com.example.myfilimapp.ui.screens.base_screens.OnBoardingScreen
import com.example.myfilimapp.ui.screens.base_screens.SplashScreen
import com.example.myfilimapp.ui.screens.favorites.FavoritesScreen
import com.example.myfilimapp.ui.screens.music_screens.MusicPlayerScreen
import com.example.myfilimapp.ui.screens.music_screens.MusicScreen

@Composable
fun NavigationGraph(
    navController: NavHostController,
) {
    NavHost(
        navController =navController,
        startDestination= NavigationScreen.SplashScreen.route
    ) {
        composable (
            route = NavigationScreen.SplashScreen.route
        ){
            SplashScreen(
                navController = navController
            )
        }

        composable (
            route = NavigationScreen.OnBoardingScreen.route
        ){
            OnBoardingScreen(
                navController = navController
            )
        }

        composable (
            route = NavigationScreen.HomeScreen.route,
            ){backStackEntry ->

            MusicScreen(
                navController = navController
            )
        }

        composable (
            route = NavigationScreen.FavoritesScreen.route
        ){
            FavoritesScreen(
                navController = navController
            )
        }
        composable (
            route = NavigationScreen.MyProfileScreen.route
        ){

        }


        composable (
            route = NavigationScreen.MusicPlayerScreen.route,
            arguments = listOf(
                navArgument("uri") { type = NavType.StringType },
                navArgument("index") { type = NavType.StringType }
            )
        ){backStackEntry ->
            val uri = Uri.decode(backStackEntry.arguments?.getString("uri") ?: "")
            val index = Uri.decode(backStackEntry.arguments?.getString("index") ?: "")
            MusicPlayerScreen(
                navController = navController,
                assetPath = uri,
                index = index
            )
        }
    }
}



@Composable
fun navigationTitle(navController: NavController): String {
    return when (currentRoute(navController)) {
        NavigationScreen.HomeScreen.route -> stringResource(id = R.string.dashBoard)
        "MusicPlayer_Screen_route/{uri}" -> stringResource(id = R.string.music_view)
        NavigationScreen.FavoritesScreen.route -> stringResource(id = R.string.music_fav)
        NavigationScreen.MyProfileScreen.route -> stringResource(id = R.string.music_profile)
        else -> {
            stringResource(R.string.app_name)
        }
    }
}


@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route?.substringBeforeLast("/")
}