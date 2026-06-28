package com.v2ray.ang.compose

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object AppSnackbar {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _visible = MutableStateFlow(false)
    val visible: StateFlow<Boolean> = _visible.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _type = MutableStateFlow(ToastType.NORMAL)
    val type: StateFlow<ToastType> = _type.asStateFlow()

    fun show(
        message: CharSequence,
        type: ToastType = ToastType.NORMAL,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        scope.launch {
            _message.value = message.toString()
            _type.value = type
            _visible.value = true

            val delayMillis = if (duration == Toast.LENGTH_LONG) 3500L else 2000L
            delay(delayMillis)
            _visible.value = false
        }
    }
}

enum class ToastType {
    NORMAL, SUCCESS, ERROR, INFO
}

@Composable
fun AppSnackbarHost() {
    val visible by AppSnackbar.visible.collectAsState()
    val message by AppSnackbar.message.collectAsState()
    val type by AppSnackbar.type.collectAsState()

    val isDark = LocalDarkTheme.current

    val bgColor = when (type) {
        ToastType.NORMAL -> if (isDark) toastNormalBgDark else toastNormalBgLight
        ToastType.SUCCESS -> toastSuccessBg
        ToastType.ERROR   -> toastErrorBg
        ToastType.INFO    -> toastInfoBg
        else -> if (isDark) toastNormalBgDark else toastNormalBgLight
    }
    val iconText = when (type) {
        ToastType.SUCCESS -> "✓"
        ToastType.ERROR   -> "✕"
        ToastType.INFO    -> "ℹ"
        else -> null
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = bgColor,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (iconText != null) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(50))
                                .background(toastIconCircleBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = iconText,
                                color = toastTextColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Text(
                        text = message,
                        color = toastTextColor,
                        fontSize = 14.sp,
                        maxLines = 8,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
