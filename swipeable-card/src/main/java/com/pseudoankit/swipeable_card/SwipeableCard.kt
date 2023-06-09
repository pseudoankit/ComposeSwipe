package com.pseudoankit.swipeable_card

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private const val MIN_OFFSET_TO_REVEAL: Float = 0f
private const val ANIMATION_DURATION = 500

/**
 * wrapper to create a swipeable view
 * @param config [SwipeableCardConfig] configurations of swipeable view
 * @param content actual content inside the view
 * to disable default elevation can pass [SwipeableCardConfig.elevationWhenRevealed] as 0.dp
 */
@Composable
fun SwipeableCard(
    config: SwipeableCardConfig,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    color: Color = MaterialTheme.colors.surface,
    border: BorderStroke? = null,
    content: @Composable () -> Unit
) = with(config) {
    var dragAmount by remember { mutableStateOf(0f) }
    var currentOffset by remember(offsetValue) { mutableStateOf(offsetValue) }
    val displayOffset by remember {
        derivedStateOf {
            when (direction) {
                SwipeableCardConfig.Direction.RTL -> {
                    when {
                        currentOffset > MIN_OFFSET_TO_REVEAL -> -MIN_OFFSET_TO_REVEAL
                        currentOffset < maximumOffsetToReveal -> maximumOffsetToReveal
                        else -> currentOffset
                    }
                }
                SwipeableCardConfig.Direction.LTR -> {
                    when {
                        currentOffset < MIN_OFFSET_TO_REVEAL -> MIN_OFFSET_TO_REVEAL
                        currentOffset > maximumOffsetToReveal -> maximumOffsetToReveal
                        else -> currentOffset
                    }
                }
            }
        }
    }

    Surface(
        modifier = modifier
            .offset { IntOffset((displayOffset).roundToInt(), 0) }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        currentOffset = when (direction) {
                            SwipeableCardConfig.Direction.RTL -> {
                                if (-currentOffset < revealThreshold || dragAmount > 0) MIN_OFFSET_TO_REVEAL else maximumOffsetToReveal
                            }
                            SwipeableCardConfig.Direction.LTR -> {
                                if (currentOffset < revealThreshold || dragAmount < 0) MIN_OFFSET_TO_REVEAL else maximumOffsetToReveal
                            }
                        }
                    }
                ) { change, dragValue ->
                    dragAmount = dragValue
                    currentOffset += dragValue
                    if (change.positionChange() != Offset.Zero) change.consume()
                }
            },
        elevation = animateDpAsState(
            targetValue = if (displayOffset != 0f) elevationWhenRevealed else 0.dp,
            animationSpec = tween(durationMillis = ANIMATION_DURATION)
        ).value,
        shape = shape,
        color = color,
        content = content,
        border = border
    )
}