package de.yanos.islam.ui.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.yanos.islam.R
import de.yanos.islam.util.LocationPermission
import de.yanos.islam.util.Lottie
import de.yanos.islam.util.NotificationPermission
import de.yanos.islam.util.bodyLarge
import de.yanos.islam.util.goldColor
import de.yanos.islam.util.hasLocationPermission
import de.yanos.islam.util.hasNotificationPermission
import de.yanos.islam.util.titleLarge
import de.yanos.islam.util.titleSmall


@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun InitScreen(
    modifier: Modifier = Modifier,
    downloadingResources: Boolean = true,
    refreshPermission: () -> Unit
) {
    val activity = LocalContext.current as Activity
    val hasLocationPermission by mutableStateOf(hasLocationPermission(activity))
    var showLocationPermission by remember { mutableStateOf(false) }
    val hasNotificationPermission by mutableStateOf(hasNotificationPermission(activity))
    var showNotificationPermission by remember { mutableStateOf(false) }

    if (hasNotificationPermission && hasLocationPermission && !downloadingResources)
        LaunchedEffect(key1 = true) {
            refreshPermission()
        }
    InitContent(modifier,
        hasLocationPermission,
        showLocationPermission,
        hasNotificationPermission,
        showNotificationPermission,
        downloadingResources,
        {
            showLocationPermission = true
        },
        { showNotificationPermission = true }
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun InitContent(
    modifier: Modifier,
    hasLocationPermission: Boolean,
    showLocationPermission: Boolean,
    hasNotificationPermission: Boolean,
    showNotificationPermission: Boolean,
    downloadingResources: Boolean,
    onShowLocationPermissionClick: (Boolean) -> Unit,
    onShowNotificationPermissionClick: (Boolean) -> Unit,
) {
    val activity = LocalContext.current as Activity
    val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
    with(intent) {
        data = Uri.fromParts("package", activity.packageName, null)
        addCategory(Intent.CATEGORY_DEFAULT)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(modifier = Modifier.padding(16.dp), text = stringResource(id = R.string.init_title), style = titleLarge(), color = goldColor())
        Text(modifier = Modifier.padding(16.dp), text = stringResource(id = R.string.init_permission), style = titleSmall(), color = MaterialTheme.colorScheme.onBackground)
        TextButton(onClick = { onShowLocationPermissionClick(true) }, enabled = !hasLocationPermission) {
            Text(
                text = stringResource(id = R.string.init_permission_location),
                style = bodyLarge(),
                color = if (!hasLocationPermission) goldColor() else goldColor().copy(alpha = 0.2f)
            )
        }
        TextButton(onClick = { onShowNotificationPermissionClick(true) }, enabled = !hasNotificationPermission) {
            Text(
                text = stringResource(id = R.string.init_permission_notification),
                style = bodyLarge(),
                color = if (!hasNotificationPermission) goldColor() else goldColor().copy(alpha = 0.2f)
            )
        }
        AnimatedVisibility(visible = showLocationPermission) {
            LocationPermission(onPermissionDenied = {
                if (!activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
                    activity.startActivity(intent)
                onShowLocationPermissionClick(false)
            }
            )
        }
        AnimatedVisibility(visible = showNotificationPermission) {
            NotificationPermission(onPermissionDenied = {
                if (!activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS))
                    activity.startActivity(intent)
                onShowNotificationPermissionClick(false)
            }
            )
        }

        Spacer(modifier = Modifier.height(72.dp))
        Text(modifier = Modifier.padding(16.dp), text = stringResource(id = R.string.init_download), style = titleSmall(), color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(32.dp))
        if (downloadingResources)
            Lottie(modifier = Modifier.height(160.dp), resId = R.raw.lottie_download, applyColor = false)
        else Lottie(modifier = Modifier.height(160.dp), resId = R.raw.lottie_done, applyColor = false)
    }
}