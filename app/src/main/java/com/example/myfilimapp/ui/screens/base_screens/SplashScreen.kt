package com.example.myfilimapp.ui.screens.base_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.adure.hassabi01.core.colors.LocalCustomColors
import com.example.myfilimapp.navigation.NavigationScreen
import com.example.myfilimapp.ui.components.CommonText
import com.example.myfilimapp.utility.AppFonts
import com.example.myfilimapp.viewmodel.MainActivityViewModel
import com.example.myfilimapp.viewmodel.MusicPlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customColors = LocalCustomColors.current
    val loginViewModel : MainActivityViewModel = hiltViewModel()
    val isLogin by loginViewModel.isLogin.collectAsState(initial = false)

    val versionName = remember {
        context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName
    }


    LaunchedEffect(isLogin) {
        delay(2000)
        val destination = if (isLogin) {
            NavigationScreen.HomeScreen.route
        } else {
            NavigationScreen.OnBoardingScreen.route
        }

        navController.navigate(destination) {
            popUpTo(NavigationScreen.SplashScreen.route) { inclusive = true }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(customColors.appCommonColor),
        contentAlignment = Alignment.Center
    ) {
        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CommonText(
                text = "CODMERIC",
                color = customColors.commonWhiteColor,
                fontSize = 32.sp,
               fontFamily = AppFonts.fontInterSemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            CommonText(
                text = "Enjoy your music",
                color = customColors.commonWhiteColor.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontFamily = AppFonts.fontInterMedium
            )
        }

        CommonText(
            text = "Version $versionName",
            color = customColors.commonWhiteColor.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )


    }

}