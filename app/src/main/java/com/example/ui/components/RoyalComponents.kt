package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SubtleEmerald

@Composable
fun RoyalAtmosphereBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.background
    val primaryGlow = MaterialTheme.colorScheme.primary
    val secondaryGlow = MaterialTheme.colorScheme.secondary
    val tertiaryGlow = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            secondaryGlow.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.18f, size.height * 0.12f),
                        radius = size.maxDimension * 0.55f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryGlow.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.88f, size.height * 0.34f),
                        radius = size.maxDimension * 0.45f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            tertiaryGlow.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.92f),
                        radius = size.maxDimension * 0.42f
                    )
                )
            },
        content = content
    )
}

@Composable
fun RoyalCrestLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "royal_crest_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit"
    )

    val effectivePulse = if (animated) pulse else 1f
    val effectiveRotation = if (animated) rotation else 0f

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w / 2f, h / 2f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonBlue.copy(alpha = 0.28f * effectivePulse.coerceAtMost(1.05f)),
                        RoyalBlue.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = (w / 2f) * 1.15f
                )
            )

            rotate(effectiveRotation, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            NeonBlue.copy(alpha = 0.9f),
                            RoyalBlue.copy(alpha = 0.25f),
                            SubtleEmerald.copy(alpha = 0.75f),
                            Color.Transparent,
                            NeonBlue.copy(alpha = 0.9f)
                        )
                    ),
                    radius = w * 0.44f,
                    style = Stroke(width = w * 0.025f, cap = StrokeCap.Round)
                )
            }

            // Geometric Crown + Neural Diamond Core
            val crownPath = Path().apply {
                moveTo(w * 0.24f, h * 0.64f)
                lineTo(w * 0.20f, h * 0.36f)
                lineTo(w * 0.37f, h * 0.50f)
                lineTo(w * 0.50f, h * 0.26f)
                lineTo(w * 0.63f, h * 0.50f)
                lineTo(w * 0.80f, h * 0.36f)
                lineTo(w * 0.76f, h * 0.64f)
                close()
            }
            drawPath(
                path = crownPath,
                brush = Brush.linearGradient(
                    colors = listOf(ElectricCyan, RoyalBlue, NeonBlue),
                    start = Offset(w * 0.2f, h * 0.25f),
                    end = Offset(w * 0.8f, h * 0.7f)
                ),
                style = Stroke(
                    width = w * 0.042f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Crown base bar
            drawLine(
                brush = Brush.horizontalGradient(listOf(RoyalBlue, ElectricCyan, SubtleEmerald)),
                start = Offset(w * 0.26f, h * 0.72f),
                end = Offset(w * 0.74f, h * 0.72f),
                strokeWidth = w * 0.042f,
                cap = StrokeCap.Round
            )

            // Subtle emerald neural node at apex
            drawCircle(
                color = SubtleEmerald,
                radius = w * 0.042f,
                center = Offset(w * 0.50f, h * 0.54f)
            )
        }
    }
}

@Composable
fun RoyalLuxuryCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 22.dp,
    borderBrush: Brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.24f)
        )
    ),
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f),
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = modifier
        .clip(shape)
        .background(containerColor)
        .border(width = 1.dp, brush = borderBrush, shape = shape)
        .let {
            if (onClick != null) {
                it
                    .minimumInteractiveComponentSize()
                    .clickable(role = Role.Button, onClick = onClick)
            } else {
                it
            }
        }
        .padding(contentPadding)

    Column(modifier = baseModifier, content = content)
}

@Composable
fun RoyalPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(16.dp)
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .height(54.dp)
            .minimumInteractiveComponentSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (enabled && !isLoading) {
                        Brush.horizontalGradient(
                            colors = listOf(RoyalBlue, Color(0xFF0EA5E9))
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        )
                    },
                    shape = shape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    if (leadingIcon != null) {
                        leadingIcon()
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Composable
fun RoyalOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f)
                )
            )
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .height(54.dp)
            .minimumInteractiveComponentSize()
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun RoyalStatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.tertiary
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = accentColor.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.42f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor
            )
        }
    }
}

@Composable
fun RoyalLoadingIndicator(
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        RoyalCrestLogo(size = 72.dp, animated = true)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
