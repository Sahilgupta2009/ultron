package com.example.ultron

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val isAwake = mutableStateOf(false)
    private val systemText = mutableStateOf("SYSTEM ONLINE.\nTAP ANYWHERE TO ACTIVATE ULTRON CORE")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setDecorFitsSystemWindows(false) // Fullscreen immersive mode

        setContent {
            UltronScreen(
                isAwake = isAwake.value,
                statusText = systemText.value,
                onTap = { triggerUltron() }
            )
        }
    }

    private fun triggerUltron() {
        if (isAwake.value) return
        
        isAwake.value = true
        systemText.value = "ULTRON CORE ACTIVE."

        // Keep the orb visible for 6 seconds, then reset to standby
        lifecycleScope.launch {
            delay(6000)
            isAwake.value = false
            systemText.value = "SYSTEM ONLINE.\nTAP ANYWHERE TO ACTIVATE ULTRON CORE"
        }
    }
}

@Composable
fun UltronScreen(isAwake: Boolean, statusText: String, onTap: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030303))
            .clickable { onTap() },
        contentAlignment = Alignment.Center
    ) {
        // Futuristic Status Text
        Text(
            text = statusText,
            color = Color(0xFF00FFCC).copy(alpha = 0.6f),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            textAlign = TextAlign.Left,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 24.dp)
        )

        // Animated Orb Component
        AnimatedVisibility(
            visible = isAwake,
            enter = fadeIn(tween(800)) + scaleIn(tween(800, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(500)) + scaleOut(tween(500))
        ) {
            GlowingOrb()
        }
    }
}

@Composable
fun GlowingOrb() {
    val infiniteTransition = rememberInfiniteTransition(label = "ultron_anim")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    Canvas(modifier = Modifier.size(320.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 3

        // 1. Outer Glow Field
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.35f), Color.Transparent),
                center = center,
                radius = radius * 1.9f * scale
            )
        )

        // 2. Outer Rotating Cyber Ring
        rotate(degrees = rotation, pivot = center) {
            drawCircle(
                color = Color(0xFFB026FF).copy(alpha = 0.8f),
                radius = radius * 1.4f,
                style = Stroke(width = 6.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 20f)))
            )
            drawCircle(
                color = Color(0xFF00E5FF),
                radius = radius * 1.2f,
                style = Stroke(width = 10.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 40f)))
            )
        }

        // 3. Glowing Core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFFFFF), Color(0xFF00E5FF), Color(0xFF4A00E0)),
                center = center,
                radius = radius * scale
            )
        )
    }
}
