package com.example.myfilimapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adure.hassabi01.core.colors.CustomColors
import com.adure.hassabi01.core.colors.LocalCustomColors
import com.example.myfilimapp.R
import com.example.myfilimapp.utility.AppFonts
import kotlin.math.roundToInt

/**
 * this is for topAppBar
 * each page this will change name and add button
 */

@Composable
fun HomeTopBar2(
    userName: String,
    subtitle: String,
    profileImage: Int,
    onSearchClick: () -> Unit,
    isModify: Boolean = false,
    onNotificationClick: () -> Unit,
) {

    val customColors = LocalCustomColors.current


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(customColors.commonWhiteColor)
            .padding(top = 30.dp)
            .padding(horizontal = 16.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 👤 Profile Image
        Image(
            painter = painterResource(profileImage),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            CommonText(
                text = "Hi, $userName",
                fontSize = 18.sp,
                color = Color.Black
            )

            CommonText(
                text = subtitle,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }


        IconButton(onClick = onSearchClick) {
            Image(
                painter = painterResource(R.drawable.search),
                contentDescription = null,
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }


        Box {
            IconButton(onClick = onNotificationClick) {
                Image(
                    painter = painterResource(R.drawable.notification),
                    contentDescription = null,
                    modifier = Modifier
                        .size(25.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}


@Composable
fun HomeTopBar(
    userName: String,
    subtitle: String,
    profileImage: Int,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    isModify: Boolean = false,
    onBackClick: () -> Unit = {}
) {

    val customColors = LocalCustomColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(customColors.commonWhiteColor)
            .padding(top = 30.dp)
            .padding(horizontal = 16.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (isModify) {

            // 🔙 Back Button
            IconButton(onClick = onBackClick) {
                Image(
                    painter =  painterResource(R.drawable.back) ,
                    contentDescription = "Previous",
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 🎵 Only One Title
            CommonText(
                text = userName, // pass song title here
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

        } else {

            // 👤 Profile Image
            Image(
                painter = painterResource(profileImage),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                CommonText(
                    text = "Hi, $userName",
                    fontSize = 18.sp,
                    color = Color.Black
                )

                CommonText(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            // 🔍 Search
            IconButton(onClick = onSearchClick) {
                Image(
                    painter = painterResource(R.drawable.search),

                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    colorFilter = ColorFilter.tint(customColors.appCommonColor)
                )
            }

            // 🔔 Notification
            IconButton(onClick = onNotificationClick) {
                Image(
                    painter = painterResource(R.drawable.notification),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    colorFilter = ColorFilter.tint(customColors.appCommonColor)
                )
            }
        }
    }
}

// ── Single Band Column ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandColumn(
    modifier: Modifier,
    label: String,
    gain: Float,
    onGainChange: (Float) -> Unit,
    color: CustomColors
) {
    val displayVal = gain.roundToInt()

    // Local drag offset — only for smooth visual during drag
    var dragging by remember { mutableStateOf(false) }
    var localGain by remember(gain) { mutableStateOf(gain) }
    // When not dragging, sync with external state
    LaunchedEffect(gain) {
        if (!dragging) localGain = gain
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        CommonText(
            text = if (displayVal > 0) "+$displayVal" else "$displayVal",
            fontFamily = AppFonts.fontInterRegular, fontSize = 8.sp, color = color.appCommonColor,
            textAlign = TextAlign.Center, maxLines = 1,
            modifier = Modifier.fillMaxWidth().height(13.dp)
        )
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.width(3.dp).fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(color.TrackColor).border(1.dp, color.TrackBorder, RoundedCornerShape(2.dp))
            )

            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Track background
                Box(
                    modifier = Modifier.width(3.dp).fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(color.RailBg)
                        .border(1.dp, color.RailBg, RoundedCornerShape(2.dp))
                )

                // ✅ Custom vertical drag — no rotation trick
                val sliderHeightPx = with(LocalDensity.current) { 130.dp.toPx() }

                Box(
                    modifier = Modifier
                        .width(40.dp)   // wide touch area
                        .fillMaxHeight()
                        .pointerInput(Unit) {
//                            detectDragGestures { _, dragAmount ->
//                                // drag UP = positive gain, drag DOWN = negative gain
//                                val delta = -dragAmount.y / sliderHeightPx * 24f
//                                val newGain = (gain + delta).coerceIn(-12f, 12f)
//                                onGainChange(newGain)
//                            }
                            detectDragGestures(
                                onDragStart = { dragging = true },
                                onDragEnd = {
                                    dragging = false
                                    onGainChange(localGain)   // commit once on release
                                },
                                onDragCancel = {
                                    dragging = false
                                    localGain = gain           // revert on cancel
                                },
                                onDrag = { _, dragAmount ->
                                    // Map drag pixels to dB range (-12 to +12)
                                    val delta = -dragAmount.y / size.height * 24f
                                    localGain = (localGain + delta).coerceIn(-12f, 12f)
                                }
                            )

                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Thumb position — map gain (-12..+12) to vertical offset
                    val fraction = (gain + 12f) / 24f          // 0f (bottom) to 1f (top)
                    val thumbOffsetDp = with(LocalDensity.current) {
                        val trackHeightPx = 130.dp.toPx()
                        val offsetPx = trackHeightPx * (0.5f - fraction)  // centre = 0, top = negative
                        offsetPx.toDp()
                    }
                    Box(
                        modifier = Modifier
                            .offset(y = thumbOffsetDp)
                            .size(width = 19.dp, height = 12.dp)
                            .shadow(3.dp, RoundedCornerShape(3.dp))
                            .clip(RoundedCornerShape(3.dp))
                            .background(Brush.verticalGradient(listOf(color.nobTop, color.equKnobBot)))
                            .border(1.dp, color.nobTop, RoundedCornerShape(3.dp))
                    )
                }
            }

        }
        CommonText(
            text = label, fontFamily = AppFonts.fontInterRegular, fontSize = 7.sp,
            color = color.appCommonColor, textAlign = TextAlign.Center, maxLines = 1,
            modifier = Modifier.fillMaxWidth().height(12.dp)
        )
    }
}
