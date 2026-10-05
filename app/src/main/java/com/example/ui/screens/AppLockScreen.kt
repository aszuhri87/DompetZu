package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppLockScreen(
    title: String = "DompetKu Aman",
    subtitle: String = "Masukkan 4 digit PIN sandi Anda",
    onPinEntered: (String) -> Boolean,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    fun handleDigit(digit: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            isError = false
            errorMessage = ""

            if (newPin.length == 4) {
                val correct = onPinEntered(newPin)
                if (correct) {
                    onSuccess()
                } else {
                    isError = true
                    errorMessage = "PIN salah. Silakan coba lagi."
                    enteredPin = ""
                    coroutineScope.launch {
                        shakeOffset.animateTo(20f, animationSpec = tween(50))
                        shakeOffset.animateTo(-20f, animationSpec = tween(50))
                        shakeOffset.animateTo(15f, animationSpec = tween(50))
                        shakeOffset.animateTo(-15f, animationSpec = tween(50))
                        shakeOffset.animateTo(0f, animationSpec = tween(50))
                    }
                }
            }
        }
    }

    fun handleBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            isError = false
            errorMessage = ""
        }
    }

    fun handleClear() {
        enteredPin = ""
        isError = false
        errorMessage = ""
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Slate900,
                        Color(0xFF0D1B2A),
                        Color(0xFF0F172A)
                    )
                )
            )
            .testTag("app_lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Vault Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(EmeraldDark, EmeraldPrimary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Kunci Aplikasi",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // PIN Dots with shake effect
            Row(
                modifier = Modifier
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                    .testTag("pin_dots_row"),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                repeat(4) { index ->
                    val isFilled = index < enteredPin.length
                    val dotColor by animateColorAsState(
                        targetValue = when {
                            isError -> ExpenseRed
                            isFilled -> EmeraldLight
                            else -> Color.White.copy(alpha = 0.25f)
                        },
                        label = "dot_color"
                    )

                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (isFilled || isError) dotColor else Color.Transparent)
                            .border(2.dp, dotColor, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error message
            Box(
                modifier = Modifier.height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isError) {
                    Text(
                        text = errorMessage,
                        color = ExpenseRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Keypad
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Row 1: 1, 2, 3
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadButton(text = "1", onClick = { handleDigit("1") })
                    KeypadButton(text = "2", onClick = { handleDigit("2") })
                    KeypadButton(text = "3", onClick = { handleDigit("3") })
                }
                // Row 2: 4, 5, 6
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadButton(text = "4", onClick = { handleDigit("4") })
                    KeypadButton(text = "5", onClick = { handleDigit("5") })
                    KeypadButton(text = "6", onClick = { handleDigit("6") })
                }
                // Row 3: 7, 8, 9
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadButton(text = "7", onClick = { handleDigit("7") })
                    KeypadButton(text = "8", onClick = { handleDigit("8") })
                    KeypadButton(text = "9", onClick = { handleDigit("9") })
                }
                // Row 4: Clear, 0, Backspace
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadActionButton(
                        onClick = { handleClear() },
                        text = "C"
                    )
                    KeypadButton(text = "0", onClick = { handleDigit("0") })
                    KeypadActionButton(
                        onClick = { handleBackspace() },
                        icon = Icons.AutoMirrored.Filled.Backspace
                    )
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(72.dp)
            .testTag("keypad_btn_$text")
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun KeypadActionButton(
    onClick: () -> Unit,
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(72.dp)
            .testTag(if (text != null) "keypad_action_$text" else "keypad_action_backspace")
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Backspace",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(26.dp)
                )
            } else if (text != null) {
                Text(
                    text = text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}
