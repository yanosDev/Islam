package de.yanos.islam.data.repositories.source

import de.yanos.core.utils.IODispatcher
import de.yanos.islam.data.api.QuranApi
import de.yanos.islam.data.api.saveFile
import de.yanos.islam.data.model.alquran.QuranAudioResponse
import de.yanos.islam.data.model.alquran.QuranTextResponse
import de.yanos.islam.util.LoadState
import de.yanos.islam.util.localFile
import de.yanos.islam.util.localResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import timber.log.Timber
import java.io.File
import javax.inject.Inject

interface RemoteQuranSource {
    suspend fun loadQuranTranslation(): LoadState<QuranTextResponse>
    suspend fun loadQuranTransliteration(): LoadState<QuranTextResponse>
    suspend fun loadQuranAudio(): LoadState<QuranAudioResponse>
    suspend fun downloadAudio(url: String): LoadState<File>
}

class RemoteQuranSourceImpl @Inject constructor(
    private val api: QuranApi,
    private val filesDir: File,
    @IODispatcher private val dispatcher: CoroutineDispatcher
) : RemoteQuranSource {

    override suspend fun loadQuranTranslation(): LoadState<QuranTextResponse> {
        return withContext(dispatcher) {
            try {
                val response = api.loadQuranTranslation().awaitResponse()
                localResponse(response)
            } catch (e: Exception) {
                Timber.e(e)
                LoadState.Failure(e)
            }
        }
    }

    override suspend fun loadQuranTransliteration(): LoadState<QuranTextResponse> {
        return withContext(dispatcher) {
            try {
                val response = api.loadQuranTransliteration().awaitResponse()
                localResponse(response)
            } catch (e: Exception) {
                Timber.e(e)
                LoadState.Failure(e)
            }
        }
    }

    override suspend fun loadQuranAudio(): LoadState<QuranAudioResponse> {
        return withContext(dispatcher) {
            try {
                val response = api.loadQuranAudio().awaitResponse()
                localResponse(response)
            } catch (e: Exception) {
                Timber.e(e)
                LoadState.Failure(e)
            }
        }
    }

    override suspend fun downloadAudio(url: String): LoadState<File> {
        return withContext(dispatcher) {
            try {
                val destinationFile = url.localFile(filesDir)
                val response = api.downloadAudio(url)
                destinationFile.parentFile?.mkdirs()
                destinationFile.createNewFile()
                response.saveFile(destinationFile)
                LoadState.Data(destinationFile)
            } catch (e: Exception) {
                Timber.e(e)
                LoadState.Failure(e)
            }
        }
    }
}