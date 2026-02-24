package com.example.myfilimapp.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfilimapp.R
import com.example.myfilimapp.utility.AppFonts

@Composable
fun CommonOutlinedTextField(
    textState: String,
    onTextChange: (String) -> Unit,
    title: String,
    usernameError: String
) {
    val showFieldError = usernameError.isNotEmpty() && textState.isEmpty()
    OutlinedTextField(
        value = textState,
        onValueChange = {
            onTextChange(it)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(top = 5.dp, bottom = 5.dp)
            .background(
                Color.White,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                if (showFieldError)
                    Color.Red
                else
                    Color.LightGray,
                shape = RoundedCornerShape(5.dp)
            ),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            cursorColor = Color.Black

        ), keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        // isError = usernameError.isNotEmpty(),
        placeholder = {
            if (textState.isEmpty()) Text(
                title,
                color = Color.Gray,
                fontSize = 12.sp,
            )

        }, textStyle = TextStyle(
            color = Color.Black,
            fontSize = 14.sp,

        )
    )
}


@Composable
fun CommonPasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
    placeholderText: String,
    passwordError: String,
) {

    val showFieldError = passwordError.isNotEmpty() && password.isEmpty()
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(top = 5.dp, bottom = 5.dp)
            .background(
                Color.White,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                if (showFieldError)
                    Color.Red
                else
                    Color.LightGray,
                shape = RoundedCornerShape(5.dp)
            ),
        visualTransformation = if (isPasswordVisible)
            VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            cursorColor = Color.Black

        ),
        // isError = passwordError.isNotEmpty(),
        trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
//                Icon(
//                    modifier = Modifier.size(20.dp),
////                    painter = painterResource(
////                        id = if (isPasswordVisible) R.drawable.icon_eye else R.drawable.icon_eye_close
////                    ),
//                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
//                    tint = Color.Gray
//                )
            }
        },
        placeholder = {
            if (password.isEmpty()) Text(
                placeholderText,
                color = Color.Gray,
                fontSize = 12.sp,
            )

        },

        textStyle = TextStyle(
            color = Color.Black,
            fontSize = 14.sp
        )
    )
}


@SuppressLint("ModifierParameter")
@Composable
fun CommonText(
    text: String,
    fontSize: TextUnit = 16.sp,
    fontFamily: FontFamily=FontFamily.Default,
    color: Color = Color.Black,
    fontWeight: FontWeight? = null,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    fontStyle: FontStyle? = FontStyle.Normal
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign,
        fontStyle = fontStyle,
        style = TextStyle(
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight ?: FontWeight.Normal
        )
    )
}

@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    fontSize: TextUnit = 16.sp,
    cornerRadius: Dp = 12.dp,
    gradientColors: List<Color> = listOf(
        Color(0xFF0D47A1), // Dark Blue
        Color(0xFF1976D2)  // Light Blue
    ),
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush = Brush.linearGradient(gradientColors))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun CommonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF6A5AE0),
    textColor: Color = Color.White,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp
        )
    ) {
        CommonText(
            text = text,
            color = textColor,
            fontSize = 16.sp,
           fontFamily = AppFonts.fontInterRegular
        )
    }
}



