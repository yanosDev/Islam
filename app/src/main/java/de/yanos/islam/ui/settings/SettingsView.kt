package de.yanos.islam.ui.settings

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.yanos.islam.R
import de.yanos.islam.util.FontStyle
import de.yanos.islam.util.IslamDivider
import de.yanos.islam.util.IslamRadio
import de.yanos.islam.util.Lottie
import de.yanos.islam.util.labelMedium
import de.yanos.islam.util.titleMedium

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    vm: SettingsViewModel = hiltViewModel()
) {
    var recreate by remember { mutableStateOf(false) }
    if (recreate) {
        (LocalContext.current as? Activity)?.recreate()
        recreate = false
    }
    Column(modifier = modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Lottie(modifier = Modifier.height(160.dp), resId = R.raw.lottie_config, applyColor = false)
        FontSettings(
            currentSize = vm.fontSize,
            onFontSizeChange = {
                vm.updateFontSize(it)
                recreate = true
            },
            currentFontIndex = vm.fontStyle,
            onFontStyleChange = {
                vm.updateFontStyle(it)
                recreate = true
            }
        )
    }
}

@Composable
fun FontSettings(
    modifier: Modifier = Modifier,
    currentSize: Int,
    onFontSizeChange: (Int) -> Unit,
    currentFontIndex: Int,
    onFontStyleChange: (Int) -> Unit
) {
    ElevatedCard(modifier = modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
            Text(text = stringResource(id = R.string.setting_font_title), style = titleMedium())
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
            IslamDivider()
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
            FontSizeSetting(currentSize = currentSize, onFontSizeChange = onFontSizeChange)
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
            IslamDivider()
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
            FontStyleSetting(currentFontIndex = currentFontIndex, onFontStyleChange = onFontStyleChange)
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FontStyleSetting(
    modifier: Modifier = Modifier,
    currentFontIndex: Int,
    onFontStyleChange: (Int) -> Unit
) {
    Column(modifier = modifier) {
        Text(text = stringResource(id = R.string.setting_font_style_title), style = labelMedium())
        FontStyle.values().forEachIndexed { index, font ->
            IslamRadio(isSelected = currentFontIndex == index, text = font.textId) { onFontStyleChange(index) }
        }
    }
}

@Composable
fun FontSizeSetting(
    modifier: Modifier = Modifier,
    currentSize: Int,
    onFontSizeChange: (Int) -> Unit
) {
    Column {
        Text(text = stringResource(id = R.string.setting_font_size_title), style = labelMedium())
        Slider(
            modifier = modifier.padding(vertical = 4.dp),
            value = currentSize.toFloat(),
            valueRange = -2f..2f,
            steps = 3,
            onValueChange = {
                onFontSizeChange(it.toInt())
            }
        )
    }
}