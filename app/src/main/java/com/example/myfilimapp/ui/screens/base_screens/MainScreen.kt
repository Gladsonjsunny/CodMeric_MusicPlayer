package com.example.myfilimapp.ui.screens.base_screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.adure.hassabi01.core.colors.CustomColors
import com.adure.hassabi01.core.colors.LocalCustomColors
import com.example.myfilimapp.R
import com.example.myfilimapp.navigation.NavigationGraph
import com.example.myfilimapp.navigation.NavigationScreen
import com.example.myfilimapp.navigation.currentRoute
import com.example.myfilimapp.navigation.navigationTitle
import com.example.myfilimapp.ui.components.CommonText
import com.example.myfilimapp.ui.components.HomeTopBar
import com.example.myfilimapp.utility.AppFonts

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val currentRoute = currentRoute(navController)
    val customColors = LocalCustomColors.current


    val hideTopBarAndBottomRoutes = listOf(
        NavigationScreen.SplashScreen.route,
        NavigationScreen.LoginScreen.route,
        NavigationScreen.OnBoardingScreen.route,
    )
    val hideBottomRoutes = listOf(
        NavigationScreen.SplashScreen.route,
        NavigationScreen.LoginScreen.route,
        NavigationScreen.OnBoardingScreen.route,
        "MusicPlayer_Screen_route/{uri}",
    )
    val shouldShowBars = currentRoute !in hideTopBarAndBottomRoutes
    val shouldShowBottomBars = currentRoute !in hideBottomRoutes

    val isMusicPlayer = currentRoute == "MusicPlayer_Screen_route/{uri}"
            || currentRoute ==  NavigationScreen.FavoritesScreen.route
            || currentRoute == NavigationScreen.MyProfileScreen.route



    Scaffold(
        topBar = {
            if (shouldShowBars) {
                HomeTopBar(
                    userName = navigationTitle(navController),
                    subtitle = "Enjoy your music!",
                    profileImage = R.drawable.musicbg1,
                    isModify = isMusicPlayer,
                    onSearchClick = { },
                    onNotificationClick = { },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

        },
        bottomBar = {
            if (shouldShowBottomBars) {
                BottomNavigationBar(
                    navController,modifier,customColors
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {  padding->
        Box(Modifier.padding(padding)
        ) {
            NavigationGraph(navController)
        }
    }
}


@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier,
    customColors: CustomColors
) {
    val items = listOf(
        NavigationScreen.HomeScreen,
        NavigationScreen.FavoritesScreen,
        NavigationScreen.MyProfileScreen
    )
    val currentRoute = currentRoute(navController)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(customColors.commonWhiteColor),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (!isSelected) {
                            navController.navigate(item.route) {

                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                    .padding(6.dp)
            ) {
                Image(
                    painter = painterResource(
                        id = if (isSelected) item.selectedIcon
                        else item.unselectedIcon
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(
                        if (isSelected) customColors.appCommonColor
                        else Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // TEXT BELOW ICON
                CommonText(
                    text = item.title,
                    fontSize = 12.sp,
                    fontFamily = AppFonts.fontInterRegular,
                    color = if (isSelected) customColors.appCommonColor
                    else Color.LightGray
                )
            }
        }
    }
}