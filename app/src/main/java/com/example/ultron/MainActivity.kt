package com.example.ultron

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import ai.picovoice.porcupine.Porcupine
import ai.picovoice.porcupine.PorcupineManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var porcupineManager: PorcupineManager? = null
    private val isAwake = mutableStateOf(false)
    private val systemText = mutableStateOf("SYSTEM INITIALIZING...")

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startWakeWordEngine()
        } else {
            systemText.value = "MIC PERMISSION DENIED."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setDecorFitsSystemWindows(false) // Fullscreen immersive

        setContent {
            UltronScreen(isAwake = isAwake.value, statusText = systemText.value)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startWakeWordEngine()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startWakeWordEngine() {
        try {
            // =================================================================
            // IMPORTANT: GET YOUR FREE ACCESS KEY FROM console.picovoice.ai
            // Replace this empty string with your key.
            // =================================================================
            val accessKey = "" 
            
            if (accessKey.isEmpty()) {
                systemText.value = "ERROR: NO ACCESS KEY.\nCREATE FREE ACCOUNT AT PICOVOICE.AI\nPASTE KEY IN MAINACTIVITY.KT"
                return
            }

            porcupineManager = PorcupineManager.Builder()
                .setAccessKey(accessKey)
                // Using built-in word so it runs right away on your phone.
                // Say "Computer" to wake it up!
                // To use "Hey Ultron", upload a hey_ultron.ppn to app/src/main/assets/
                // and replace the line below with: .setKeywordPath("hey_ultron.ppn")
                .setKeyword(Porcupine.BuiltInKeyword.COMPUTER) 
                .setSensitivity(0.7f)
                .build(applicationContext) { keywordIndex ->
                    if (keywordIndex == 0) {
                        triggerUltron()
                    }
                }

            porcupineManager?.start()
            systemText.value = "SYSTEM ONLINE.\nAWAITING WAKE WORD: 'COMPUTER'"
        } catch (e: Exception) {
            systemText.value = "INITIALIZATION FAILED:\n${e.message}"
        }
    }

    private fun triggerUltron() {
        isAwake.value = true
        systemText.value = "ULTRON CORE ACTIVE."
        
        // Hide orb after 6 seconds of being awake
        lifecycleScope.launch {
            delay(6000) 
            isAwake.value = false
            systemText.value = "SYSTEM ONLINE.\nAWAITING WAKE WORD: 'COMPUTER'"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        porcupineManager?.stop()
        porcupineManager?.delete()
    }
}

@Composable
fun UltronScreen(isAwake: Boolean, statusText: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030303)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = statusText,
            color = Color(0xFF00FFCC).copy(alpha = 0.5f),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            textAlign = TextAlign.Left,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 24.dp)
        )

        AnimatedVisibility(
            visible = isAwake,
            enter = fadeIn(tween(1000)) + scaleIn(tween(1000, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(500)) + scaleOut(tween(500))
        ) {
            GlowingOrb()
        }
    }
}

@Composable
fun GlowingOrb() {
    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.size(320.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 3

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.3f), Color.Transparent),
                center = center, radius = radius * 1.9f * scale
            )
        )

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

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFFFFF), Color(0xFF00E5FF), Color(0xFF4A00E0)),
                center = center, radius = radius * scale
            )
        )
    }
}
