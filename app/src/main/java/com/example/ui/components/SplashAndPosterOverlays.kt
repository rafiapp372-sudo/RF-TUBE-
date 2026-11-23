package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.localization.LocalStrings
import kotlinx.coroutines.delay

@Composable
fun RfTubeSplashScreen(
    onFinished: () -> Unit
) {
    val strings = LocalStrings.current
    val scale = remember { Animatable(0.84f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(320, easing = FastOutSlowInEasing))
        scale.animateTo(1f, animationSpec = tween(420, easing = FastOutSlowInEasing))
        delay(650L)
        alpha.animateTo(0f, animationSpec = tween(250))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B0D14), Color(0xFF1A0910), Color(0xFF0B0D14))
                )
            )
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            RfTubeLogo(
                size = 112.dp,
                modifier = Modifier
                    .border(2.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    .testTag("splash_app_logo")
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = strings.tagline,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
fun WelcomePosterModal(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    var animateIn by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        animateIn = visible
    }

    AnimatedVisibility(
        visible = visible && animateIn,
        enter = fadeIn(animationSpec = tween(350)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.80f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .safeDrawingPadding()
                .padding(16.dp)
                .testTag("welcome_poster_backdrop"),
            contentAlignment = Alignment.Center
        ) {
            val posterSide = minOf(maxWidth, maxHeight - 48.dp, 540.dp).coerceAtLeast(220.dp)

            Box(
                modifier = Modifier
                    .size(posterSide)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* Consume clicks inside poster so only backdrop or X closes */ }
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.rf_tube_welcome_poster),
                    contentDescription = "Welcome to RF TUBE",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("welcome_poster_image")
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.75f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                        .testTag("welcome_poster_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = strings.close,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
