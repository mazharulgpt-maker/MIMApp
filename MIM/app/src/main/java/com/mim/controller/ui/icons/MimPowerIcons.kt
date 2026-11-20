package com.mim.controller.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mim.controller.data.PowerIconStyle

@Composable
fun MimPowerGlyph(
    style: PowerIconStyle,
    tint: Color,
    size: Dp = 36.dp,
    contentDescription: String = "Lock screen"
) {
    when (style) {
        PowerIconStyle.STANDARD_POWER -> {
            Icon(
                imageVector = Icons.Rounded.PowerSettingsNew,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(size)
            )
        }

        PowerIconStyle.CIRCLE_POWER -> {
            Canvas(modifier = Modifier.size(size)) {
                val strokeWidth = this.size.minDimension * 0.085f
                val inset = strokeWidth * 1.4f
                // Outer ring
                drawCircle(
                    color = tint.copy(alpha = 0.35f),
                    radius = (this.size.minDimension - strokeWidth) / 2f,
                    style = Stroke(width = strokeWidth * 0.8f)
                )
                // Inner power arc
                drawArc(
                    color = tint,
                    startAngle = -55f,
                    sweepAngle = 290f,
                    useCenter = false,
                    topLeft = Offset(inset * 1.8f, inset * 1.8f),
                    size = Size(
                        this.size.width - inset * 3.6f,
                        this.size.height - inset * 3.6f
                    ),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                // Vertical stem
                drawLine(
                    color = tint,
                    start = Offset(center.x, inset * 1.3f),
                    end = Offset(center.x, center.y),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        PowerIconStyle.LOCK_SHIELD -> {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(size * 0.88f)
            )
        }

        PowerIconStyle.SLEEP_MOON -> {
            Icon(
                imageVector = Icons.Rounded.Bedtime,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(size * 0.88f)
            )
        }

        PowerIconStyle.MINIMAL_DOT -> {
            Canvas(modifier = Modifier.size(size)) {
                val strokeWidth = this.size.minDimension * 0.08f
                drawCircle(
                    color = tint,
                    radius = this.size.minDimension * 0.38f,
                    style = Stroke(width = strokeWidth)
                )
                drawLine(
                    color = tint,
                    start = Offset(center.x, center.y - this.size.minDimension * 0.16f),
                    end = Offset(center.x, center.y + this.size.minDimension * 0.16f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
