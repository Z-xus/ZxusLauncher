package io.github.zxus.zxuslauncher.ui.screens

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun HomeScreen(onSwipeUp: () -> Unit, onSwipeDown: () -> Unit) {
    var totalDrag = 0f
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        if (totalDrag < -100) { // Upward swipe
                            onSwipeUp()
                        } else if (totalDrag > 100) { // Downward swipe
                            onSwipeDown()
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Home Screen", style = MaterialTheme.typography.headlineLarge)
    }
}
