package de.yanos.islam.util

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import de.yanos.islam.R

val goldColor = Color(android.graphics.Color.parseColor("#FFD700"))
val errorColor = Color(android.graphics.Color.parseColor("#FF0000"))
val correctColor = Color(android.graphics.Color.parseColor("#00FF00"))

@Composable
@Preview
fun IslamDivider(alpha: Float = 0.35f) {
    Divider(modifier = Modifier.alpha(alpha), color = goldColor)
}

@Composable
fun IslamCheckBox(
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    isChecked: Boolean,
    onCheckChange: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
        Checkbox(
            enabled = isEnabled,
            checked = isChecked,
            onCheckedChange = onCheckChange
        )
        TextButton(onClick = { onCheckChange(!isChecked) }, enabled = isEnabled) {
            content()
        }
    }
}

@Composable
fun IslamRadio(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    @StringRes text: Int,
    onClick: () -> Unit
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        TextButton(onClick = onClick) {
            Text(modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start, text = stringResource(id = text), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun PrimaryLottie(modifier: Modifier, @RawRes resId: Int) {
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                MaterialTheme.colorScheme.primary.hashCode(),
                BlendModeCompat.SRC_ATOP
            ),
            keyPath = arrayOf(
                "**"
            )
        )
    )
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    val progress by animateLottieCompositionAsState(composition)
    LottieAnimation(
        modifier = modifier,
        composition = composition,
        progress = { progress },
        dynamicProperties = dynamicProperties
    )
}

@Composable
fun PatternedBackgroung(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        BackGroundPattern(modifier = Modifier.matchParentSize())
        content()
    }
}

@Composable
fun BackGroundPattern(modifier: Modifier = Modifier) {
    val pattern = ImageBitmap.imageResource(R.drawable.pattern_2)

    Canvas(
        modifier = modifier.alpha(0.1F)
    ) {
        val paint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            shader = ImageShader(pattern, TileMode.Repeated, TileMode.Repeated)
        }

        drawIntoCanvas {
            it.nativeCanvas.drawPaint(paint)
        }
        paint.reset()
    }
}