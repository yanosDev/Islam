package de.yanos.islam.ui.knowledge.topics.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.yanos.core.utils.IODispatcher
import de.yanos.islam.data.database.dao.QuizDao
import de.yanos.islam.data.database.dao.SearchDao
import de.yanos.islam.data.model.Quiz
import de.yanos.islam.data.model.Search
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchQuestionsViewModel @Inject constructor(
    @IODispatcher private val dispatcher: CoroutineDispatcher,
    private val quizDao: QuizDao,
    private val searchDao: SearchDao
) : ViewModel() {
    var query by mutableStateOf("")
    var findings = mutableStateListOf<Quiz>()
    var recentSearches = searchDao.getRecentSearches().distinctUntilChanged()

    fun search(query: String, saveToRecent: Boolean = false) {
        viewModelScope.launch(dispatcher) {
            if (saveToRecent && query.isNotBlank())
                searchDao.insert(Search(query = query))
        }

        if (this.query != query) {
            this.query = query
            if (this.query.isNotBlank()) {
                viewModelScope.launch(dispatcher) {
                    val newFindings = quizDao.findMatches(query)
                    findings.clear()
                    findings.addAll(newFindings)
                }
            }
        }
    }

    fun clearSearch() {
        this.query = ""
        findings.clear()
    }
}