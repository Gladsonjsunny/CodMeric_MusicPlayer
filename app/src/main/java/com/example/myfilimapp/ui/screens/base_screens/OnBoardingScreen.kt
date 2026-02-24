package com.example.myfilimapp.ui.screens.base_screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.adure.hassabi01.core.colors.LocalCustomColors
import com.example.myfilimapp.model.request.items
import com.example.myfilimapp.navigation.NavigationScreen
import com.example.myfilimapp.ui.components.CommonText
import com.example.myfilimapp.utility.AppFonts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnBoardingScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val customColors = LocalCustomColors.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { items.size })

    // Track if user is manually interacting
    var userInteracting by remember { mutableStateOf(false) }
    val isLastPage = pagerState.currentPage == items.lastIndex

    // Detect drag/touch interaction
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is DragInteraction.Start, is PressInteraction.Press -> userInteracting = true
                is DragInteraction.Stop, is DragInteraction.Cancel,
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    // Small delay so the page settles before re-enabling
                    delay(500)
                    userInteracting = false
                }
            }
        }
    }

    LaunchedEffect(userInteracting) {
        if (!userInteracting) {
            while (true) {
                delay(3000)
                if (!userInteracting) {
                    val nextPage = (pagerState.currentPage + 1) % items.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(customColors.commonWhiteColor)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,          // ✅ Space between pages
            userScrollEnabled = true,
            modifier = Modifier
                .weight(1f)
                .indication(interactionSource, null) // attach interaction tracking
        ) { page ->
            val item = items[page]
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = item.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(380.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                )
                Spacer(modifier = Modifier.height(24.dp))
                CommonText(
                    text = item.title,
                    fontSize = 20.sp,
                    fontFamily = AppFonts.fontInterRegular,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                CommonText(
                    text = item.description,
                    fontSize = 14.sp,
                    color = customColors.commonBlackColor,
                    textAlign = TextAlign.Center,
                    fontFamily = AppFonts.fontInterRegular,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // DOT INDICATOR
        Row(horizontalArrangement = Arrangement.Center) {
            repeat(items.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) customColors.appCommonColor else Color.LightGray
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            CommonText(
                text = if (isLastPage) "Get Started" else "Next",
                color = Color(0xFF6A5AE0),
                modifier = Modifier.clickable {
                    if (pagerState.currentPage == items.lastIndex) {
                        navController.navigate(NavigationScreen.HomeScreen.route) {
                            popUpTo(NavigationScreen.OnBoardingScreen.route) { inclusive = true }
                        }
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}