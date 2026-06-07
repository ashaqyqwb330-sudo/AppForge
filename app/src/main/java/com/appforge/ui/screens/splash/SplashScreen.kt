package com.appforge.ui.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.appforge.data.preferences.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun SplashScreen(
    preferencesManager: PreferencesManager,
    onNavigateToActiveApp: () -> Unit,
    onNavigateToAppManager: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000)
        // التحقق من وجود تطبيق نشط محفوظ
        val activeId = preferencesManager.activeAppId.firstOrNull()
        if (activeId != null) {
            onNavigateToActiveApp()
        } else {
            onNavigateToAppManager()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "AppForge Logo",
                modifier = Modifier
                    .size(80.dp)
                    .alpha(alphaAnim),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "AppForge",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.alpha(alphaAnim)
            )
            Text(
                text = "صانع التطبيقات الذكي",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.alpha(alphaAnim)
            )
        }
    }
}
