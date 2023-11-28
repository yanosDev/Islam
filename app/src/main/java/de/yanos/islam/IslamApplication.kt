package de.yanos.islam

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.media3.session.MediaController
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.HiltAndroidApp
import de.yanos.core.BuildConfig
import de.yanos.core.utils.IODispatcher
import de.yanos.islam.service.queueAudioWorker
import de.yanos.islam.service.queuePeriodicDailyWorker
import de.yanos.islam.service.queueVideoWorker
import de.yanos.islam.util.AppContainer
import de.yanos.islam.util.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class IslamApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationManager: NotificationManager
    @Inject @IODispatcher lateinit var dispatcher: CoroutineDispatcher
    @Inject lateinit var workManager: WorkManager
    @Inject lateinit var mediaControllerFuture: ListenableFuture<MediaController>
    @Inject lateinit var appContainer: AppContainer
    override fun onCreate() {
        StrictMode.setThreadPolicy(
            ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork() // or .detectAll() for all detectable problems
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        createAlarmChannel()
        createDownloadChannel()

        workManager.queueVideoWorker()
        workManager.queuePeriodicDailyWorker()
        mediaControllerFuture.addListener({
            if (mediaControllerFuture.get() != null) {
                appContainer.audioController = mediaControllerFuture.get()
                workManager.queueAudioWorker()
            }
        }, dispatcher.asExecutor())
    }

    private fun createDownloadChannel() {
        val channel = NotificationChannel(
            Constants.CHANNEL_ID_DOWNLOAD,
            Constants.CHANNEL_NAME_DOWNLOAD,
            NotificationManager.IMPORTANCE_HIGH
        )

        notificationManager.createNotificationChannel(channel)
    }

    private fun createAlarmChannel() {
        val channel = NotificationChannel(
            Constants.CHANNEL_ID_ALARM,
            Constants.CHANNEL_NAME_ALARM,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.INFO else Log.ERROR)
            .setWorkerFactory(workerFactory)
            .build()

    }
}