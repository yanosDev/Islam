package de.yanos.islam

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.annotation.RawRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.yanos.core.utils.IODispatcher
import de.yanos.islam.data.database.IslamDatabase
import de.yanos.islam.data.model.Quiz
import de.yanos.islam.data.model.Schedule
import de.yanos.islam.data.model.Topic
import de.yanos.islam.data.model.TopicResource
import de.yanos.islam.data.model.TopicType
import de.yanos.islam.data.repositories.AwqatRepository
import de.yanos.islam.data.repositories.QuranRepository
import de.yanos.islam.service.DailyScheduleWorker
import de.yanos.islam.util.AppSettings
import de.yanos.islam.util.getCurrentLocation
import de.yanos.islam.util.hasLocationPermission
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.time.Duration
import java.time.LocalDateTime
import java.util.Timer
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.timerTask


@SuppressLint("StaticFieldLeak")
@HiltViewModel
class MainViewModel @Inject constructor(
    private val appSettings: AppSettings,
    private val geocoder: Geocoder,
    private val awqatRepository: AwqatRepository,
    private val quranRepository: QuranRepository,
    @ApplicationContext private val context: Context,
    private val db: IslamDatabase,
    @IODispatcher private val dispatcher: CoroutineDispatcher,
    private val workManager: WorkManager
) : ViewModel() {
    var isReady: Boolean = false
    private val timer: Timer = Timer()

    init {
        timer.scheduleAtFixedRate(
            timerTask()
            {
                viewModelScope.launch(Dispatchers.Main) {
                    if (hasLocationPermission(context))
                        getCurrentLocation(context = context) { lat, lon ->
                            @Suppress("DEPRECATION")
                            geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()?.let { address ->
                                (address.subAdminArea ?: address.adminArea)?.let { name ->
                                    appSettings.lastLocation = name
                                    loadLocationDependentData()
                                }
                            }
                        }
                    else loadLocationIndependentData()
                }
            }, 0, 60000
        )
    }

    private fun loadLocationDependentData() {
        viewModelScope.launch {
            Timber.e("MAIN: loadLocationDependentData")
            appSettings.lastLocation.takeIf { it.isNotBlank() }?.let {
                awqatRepository.fetchCityData(it)
            }
        }
    }

    private suspend fun loadLocationIndependentData() {
        viewModelScope.launch {
            if (!isReady) {
                Timber.e("MAIN: loadLocationIndependentData")
                val tasks = listOf(
                    async { initDB() },
                    async { initDailyWorker() },
                    async { loadQuran() },
                    async { loadDailyAwqatList() },
                ).awaitAll()
                isReady = tasks.all { it }
            }
        }
    }

    private suspend fun loadQuran(): Boolean {
        return withContext(dispatcher) {
            quranRepository.fetchQuran()
        }
    }

    private suspend fun initDailyWorker(): Boolean {
        val now = LocalDateTime.now()
        val delay = when {
            now.hour < 1 -> Duration.ofHours(0L)
            else -> Duration.ofHours(24L - now.hour)
        }.plusMinutes(20)
        val periodicWorkRequest = PeriodicWorkRequestBuilder<DailyScheduleWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay)
            .build()
        workManager.enqueueUniquePeriodicWork("daily", ExistingPeriodicWorkPolicy.UPDATE, periodicWorkRequest)
        return true
    }

    private suspend fun loadDailyAwqatList(): Boolean {
        return withContext(dispatcher) {
            awqatRepository.fetchAwqatLocationIndependentData()
        }
    }

    private suspend fun initDB(): Boolean {
        return withContext(dispatcher) {
            if (!appSettings.isDBInitialized) {
                TopicResource.values().forEach { topic ->
                    topic.raw?.let { raw ->
                        createQuizByTopic(raw, topic.id)
                    }
                    db.topicDao().insert(
                        Topic(
                            id = topic.id, title = topic.title, ordinal = topic.ordinal, parentId = topic.parent, type = when {
                                topic.parent == null && topic.raw != null -> TopicType.MAIN
                                topic.parent == null -> TopicType.GROUP
                                else -> TopicType.SUB
                            }
                        )
                    )
                }

                db.awqatDao().insertSchedules(
                    listOf(
                        Schedule(
                            id = "fajr",
                            ordinal = 0
                        ),
                        Schedule(
                            id = "sunrise",
                            ordinal = 1,
                            enabled = true,
                            relativeTime = -45
                        ),
                        Schedule(
                            id = "dhuhr",
                            ordinal = 2
                        ),
                        Schedule(
                            id = "asr",
                            ordinal = 3
                        ),
                        Schedule(
                            id = "maghrib",
                            ordinal = 4,
                            enabled = true,
                            relativeTime = -5
                        ),
                        Schedule(
                            id = "isha",
                            ordinal = 5
                        ),
                    )
                )

                appSettings.isDBInitialized = true
            }
            delay(1200L)
            true
        }
    }

    private fun createQuizByTopic(@RawRes topicRaw: Int, topicId: Int) {
        val regex = Regex("""\d+|\D+""")
        val inputStream: InputStream = context.resources.openRawResource(topicRaw)
        val br = BufferedReader(InputStreamReader(inputStream))
        val lines = br.readLines()
        lines.forEachIndexed { index, line ->
            if (line.trim().startsWith("*")) {
                val actualLine = mergeLines(lines, index)
                    .replace("*", "")
                    .replace("  ", " ")
                    .trim()
                try {
                    actualLine.split("?").let { (question, answer) ->
                        val parts = regex.findAll(answer).map { it.groupValues.first() }.toList()
                        val builder = StringBuilder()
                        parts.forEach {
                            val text = it.trim()
                            if (text.isNotBlank())
                                when {
                                    !it.matches(Regex("""\d+""")) || builder.isEmpty() -> builder.append(" $text")
                                    else -> builder.append("\n$text ")
                                }
                        }
                        db.quizDao().insert(Quiz(question = question, answer = builder.toString().replace("  ", " ").trim(), topicId = topicId, difficulty = 0))
                    }
                } catch (e: Exception) {
                    Timber.e(actualLine)
                    e.localizedMessage?.let { Timber.e("e", it) }
                }
            }
        }
    }

    private fun mergeLines(lines: List<String>, index: Int): String {
        val currentLine = lines[index]
        return if (index == lines.size - 1 || lines[index + 1].trim().startsWith("*")) {
            currentLine
        } else currentLine.trim() + " " + mergeLines(lines, index + 1).trim()
    }
}